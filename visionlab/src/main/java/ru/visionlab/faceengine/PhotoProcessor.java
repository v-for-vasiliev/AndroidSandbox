package ru.visionlab.faceengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.visionlab.Utils;

public final class PhotoProcessor {

    static {
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("flower");
            System.loadLibrary("PhotoMaker");
            System.loadLibrary("LivenessEngineSDK");
            System.loadLibrary("wrapper");
        } catch (UnsatisfiedLinkError e) {
            Log.e("Luna Mobile", "Native library failed to load: " + e);
            System.exit(1);
        }
    }

    private boolean detectionLost = true;

    private final PhotoMaker photoMaker;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Object lock = new Object();

    private int bestShotHeight;

    private int bestShotWidth;

    private ByteBuffer buffer;

    private volatile boolean BestShotfound = false;

    private volatile boolean checkEyes = false;

    private volatile boolean checkZoomFlow = false;

    private byte[] previewInRGBAFormat;

    private int[] previewInARGBFormat;

    private int[] tempArray;

    int stagesCount;

    private byte[] previewYData;

    private android.graphics.Rect lastFaceBound = null;

    private AtomicBoolean flashTorchEnabled = new AtomicBoolean(false);

    private long luminanceStateCheckingLastTime = -1;

    private RenderScript renderScript;

    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;

    private Allocation renderScriptInAllocation;

    private Allocation renderScriptOutAllocation;

    private int previewWidth;

    private int previewHeight;

    private byte[] firstCallbackBuffer;

    private byte[] secondCallbackBuffer;

    float[] lastScores;

    private IntBuffer bestShotData;

    private boolean needPortrait = false;

    @Nullable
    private Listener listener;

    private int imageRotation;

    private boolean mainCamera;

    private final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            PhotoProcessor.this.work();
        }
    }, "photoProcessor");

    private PhotoProcessor(@NonNull Context context) {
        thread.start();
        photoMaker = new PhotoMaker(context.getFilesDir() + "/vl/data");
        System.out.println("PATH in photoprocessor is " + context.getFilesDir());
        photoMaker.reset();
        photoMaker.setStopAfterBestShot(false);
        renderScript = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB
                .create(renderScript, Element.U8_4(renderScript));
    }

    public class LuminanceState {

        public LuminanceState(int darknessState) {
            this.darknessState = darknessState;
        }

        public int darknessState;
    }

    /**
     * Main image processing loop. It does the following:
     * 1 convert from NV21 input frame to RGBA image format
     * 2 image rotation (preview usually comes upside down)
     * 3 feeding transformed image to photomaker
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void work() {
        while (!thread.isInterrupted()) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    break;
                }

                if (thread.isInterrupted()) {
                    break;
                }

                convertToRGBA(secondCallbackBuffer);

                System.arraycopy(secondCallbackBuffer, 0, previewYData, 0, previewYData.length);
            }

            long start = System.currentTimeMillis();

            Utils.byteToIntArray(previewInRGBAFormat, previewInARGBFormat);

            int width = previewWidth;
            int height = previewHeight;

            if ((imageRotation == 90 && !mainCamera) || (mainCamera && imageRotation == 270)) {
                Utils.rotateClockwise90(previewInARGBFormat, tempArray, previewWidth,
                        previewHeight);
                Utils.flip(previewInARGBFormat, previewWidth, previewHeight);
                width = previewHeight;
                height = previewWidth;
            }

            if ((imageRotation == 180 && !mainCamera) || (mainCamera && imageRotation == 0)) {
                Utils.flip(previewInARGBFormat, previewWidth, previewHeight);
            }

            if ((imageRotation == 270 && !mainCamera) || (mainCamera && imageRotation == 90)) {
                Utils.rotateClockwise90(previewInARGBFormat, tempArray, previewWidth,
                        previewHeight);
                width = previewHeight;
                height = previewWidth;
            }

            buffer.clear();
            IntBuffer intBuffer = buffer.asIntBuffer();
            intBuffer.put(previewInARGBFormat);

            int[] FaceBbox = FaceEngineJNI.getFaceBBOX(buffer.array(), width, height);

            Log.i("PHOTOPROCESSOR", "FACE BBOX is" + Arrays.toString(FaceBbox));

            android.graphics.Rect rect = new android.graphics.Rect(FaceBbox[0], FaceBbox[1],
                    FaceBbox[0] + FaceBbox[2], FaceBbox[1] + FaceBbox[3]);
            if (rect.isEmpty()) {
                detectionLost = true;
            } else {
                detectionLost = false;
            }

//            if(listener!= null && listener.isFaceInsideBorder(rect)) {
            sendFrameToPhotoMaker(width, height);
//            }

            updateArea(width, height);

            if (BestShotfound) {
                if (checkZoomFlow || checkEyes) {
                    ProcessLivenessFlow(width, height);
                }
            }

            if (null != lastFaceBound) {
                processFrameLuminance(lastFaceBound);
            }
        }
    }

    public void setFlashTorchState(boolean enabled) {
        flashTorchEnabled.set(enabled);
    }

    /**
     * Checks luminance state (0 - dark, 1 - light)
     */

    private LuminanceState calcLuminanceState(android.graphics.Rect rect) {

        final android.graphics.Rect correctedRect = new android.graphics.Rect();

        correctedRect.set(Math.max(0, rect.left), Math.max(0, rect.top),
                Math.min(rect.right, previewWidth), Math.min(rect.bottom, previewHeight));

        boolean isFlashTorchEnabled = flashTorchEnabled.get();

        double lowerRate = isFlashTorchEnabled ? 0.05 : 0.15;
        int blackPixelsCount = Utils.countLower(previewYData, previewWidth, correctedRect, 20);

        int rectPixelsCount = (correctedRect.right - correctedRect.left) * (correctedRect.bottom
                - correctedRect.top);

        int darknessState = blackPixelsCount >= (int) (rectPixelsCount * lowerRate) ? 0 : 1;

        return new LuminanceState(darknessState);
    }


    public void ProcessLivenessFlow(int width, int height) {
        //FaceEngineJNI.submitImage(buffer.array(), width, height);
        int result = FaceEngineJNI.checkCurrentStage();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    if (result != 0) {
                        if (detectionLost)//result = LSDKError::PreconditionFailed
                        {
                            listener.onLivenessResult(3, FaceEngineJNI.getLivenessAction());
                        } else {
                            listener.onLivenessResult(result, FaceEngineJNI.getLivenessAction());
                        }
                    } else {
                        listener.onLivenessSucceed();
                    }
                }
            }
        });
    }


    private void sendFrameToPhotoMaker(int width, int height) {
        final ImageView frame = new ImageView(buffer, width, height);
        try {
            photoMaker.submit(frame);
            photoMaker.update();

            //focus timer
            if (photoMaker.haveBestShot() && !BestShotfound) {
                Log.i("PhotoProcessor", "We have bestshot!");
                BestShotfound = true;
                processBestShot();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processFrameLuminance(android.graphics.Rect rect) {

        if (luminanceStateCheckingLastTime == -1
                || System.currentTimeMillis() - luminanceStateCheckingLastTime > 2000) {
            luminanceStateCheckingLastTime = System.currentTimeMillis();

            final LuminanceState luminanceState = calcLuminanceState(rect);

            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onLuminanceState(luminanceState);
                        }
                    }
                });
            }
        }
    }


    public void setListener(@NonNull Listener listener) {
        Log.i("LISTENER", "ENTERING SET LISTENER METHOD ON PHOTOPROC.java");
        this.listener = listener;
        stagesCount = 1;
        photoMaker.reset();
        resumeSearch();
    }

    public void resumeSearch() {
        BestShotfound = false;
        checkEyes = false;
        checkZoomFlow = false;
    }

    public void startCheckLiveness() {
        Log.i("LISTENER", "ENTERING START_CHECK_LIVENESS ON PHOTOPROC.java");
        checkEyes = true;
    }

    public void setZoomLiveness() {
        FaceEngineJNI.setZoomLiveness();
        FaceEngineJNI.startCheck();
    }

    public void setEyeLiveness() {
        FaceEngineJNI.setEyeLiveness();
        FaceEngineJNI.startCheck();
    }

    public void startCheckLivenessZoom() {
        Log.i("LISTENER", "ENTERING START_CHECK_LIVENESS_ZOOM.java");
        checkZoomFlow = true;
    }

    public void removeListeners() {
        listener = null;
        handler.removeCallbacksAndMessages(null);
    }


    public int getPreviewWidth() {
        return previewWidth;
    }


    public int getPreviewHeight() {
        return previewHeight;
    }

    /**
     * Initialize photomaker library
     *
     * @param path path to data folder
     * @return true if library is loaded succesfully, false otherwise
     */
    private boolean loadData(String path) {
        photoMaker.load(path);
        return photoMaker.isLoaded();
    }

    public void setPreviewSize(int width, int height) {
        previewWidth = width;
        previewHeight = height;

        final int previewNV21ArraySize = previewWidth * previewHeight * 3 / 2;
        final int previewRGBAArraySize = previewWidth * previewHeight * 4;
        previewInARGBFormat = new int[previewWidth * previewHeight];

        tempArray = new int[previewInARGBFormat.length];

        renderScriptAllocate(previewNV21ArraySize);

        previewInRGBAFormat = new byte[previewRGBAArraySize];
        firstCallbackBuffer = new byte[previewNV21ArraySize];
        secondCallbackBuffer = new byte[previewNV21ArraySize];
        previewYData = new byte[previewWidth * previewHeight];

        buffer = ByteBuffer.allocateDirect(previewRGBAArraySize).order(ByteOrder.LITTLE_ENDIAN);
    }

    public void release() {
        listener = null;
        handler.removeCallbacksAndMessages(null);
        thread.interrupt();

        if (renderScriptInAllocation != null) {
            renderScriptInAllocation.destroy();
        }

        if (renderScriptOutAllocation != null) {
            renderScriptOutAllocation.destroy();
        }

        renderScript.destroy();
        yuvToRgbIntrinsic.destroy();
    }

    public void processFrame(byte[] data) {
        if (!BestShotfound || checkEyes || checkZoomFlow) {
            synchronized (lock) {
                System.arraycopy(data, 0, secondCallbackBuffer, 0, secondCallbackBuffer.length);
                lock.notify();
            }
        }
    }

    private void convertToRGBA(byte[] data) {
        renderScriptInAllocation.copyFrom(data);

        yuvToRgbIntrinsic.setInput(renderScriptInAllocation);
        yuvToRgbIntrinsic.forEach(renderScriptOutAllocation);

        renderScriptOutAllocation.copyTo(previewInRGBAFormat);
    }

    private void renderScriptAllocate(int previewNV21ArraySize) {
        Type.Builder yuvType = new Type.Builder(renderScript, Element.U8(renderScript))
                .setX(previewNV21ArraySize);
        renderScriptInAllocation = Allocation
                .createTyped(renderScript, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript))
                .setX(previewWidth).setY(previewHeight);
        renderScriptOutAllocation = Allocation
                .createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT);
    }


    private void updateArea(int width, int height) {

        if (photoMaker.haveFaceDetection()) {
            final Rect faceDetection = photoMaker.getFaceDetection();

            final android.graphics.Rect rect = new android.graphics.Rect(faceDetection.getLeft(),
                    faceDetection.getTop(), faceDetection.getRight(), faceDetection.getBottom());

            if (lastFaceBound == null) {
                lastFaceBound = new android.graphics.Rect();
            }

            lastFaceBound.set(rect);

            final boolean fastMovement = false;//!photoMaker.isSlowMovement();
            final boolean isFrontalPose = photoMaker.isFrontalPose();

            final boolean[] qualityStates = photoMaker.getQualityStates();

            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFaceArea(true, rect, fastMovement, isFrontalPose,
                                    qualityStates);
                        }
                    }
                });
            }
        } else {

            lastFaceBound = null;

            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFaceArea(false, null, null, null, null);
                        }
                    }
                });
            }
        }

    }

    public byte[] getCallbackBuffer() {
        return firstCallbackBuffer;
    }

    private void processBestShot() {
        final ImageView imageView = photoMaker.getBestWarpedShot();
        bestShotHeight = imageView.getHeight();
        bestShotWidth = imageView.getWidth();
        final ByteBuffer bestShotByteBuffer = ByteBuffer
                .allocateDirect(bestShotHeight * bestShotWidth * 4);
        imageView.getRecPixels(bestShotByteBuffer);
        imageView.delete();

        byte[] res = new byte[bestShotByteBuffer.capacity()];

        bestShotByteBuffer.position(0);
        bestShotByteBuffer.get(res);

        int[] result = new int[bestShotHeight * bestShotWidth];
        Utils.byteToIntArray(res, result);

        bestShotData = IntBuffer.wrap(result);
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onBestFrameReady();
                    }
                }
            });
        }
    }


    @Nullable
    public Bitmap getBestShot() {
        final Bitmap bestShotBitmap = Bitmap
                .createBitmap(bestShotWidth, bestShotHeight, Bitmap.Config.ARGB_8888);
        bestShotData.position(0);
        bestShotBitmap.copyPixelsFromBuffer(bestShotData);
        return bestShotBitmap;
    }


    public void setNeedPortrait(boolean value) {
        needPortrait = value;
    }


    public void setImageRotation(int imageRotation) {
        this.imageRotation = imageRotation;
    }


    public void setMainCamera(boolean mainCamera) {
        this.mainCamera = mainCamera;
    }

    public void disableOpenEyesCheck(boolean value) {
        photoMaker.disableOpenEyesCheck(value);
    }

    private void setRotationLimit(float rotationLimit) {
        photoMaker.setRotationThreshold(rotationLimit);
    }

    private void setBestShotScoreThreshold(float scoreThreshold) {
        photoMaker.setBestShotScoreThreshold(scoreThreshold);
    }


    private void setPortraitMaxHeight(int height) {
        photoMaker.setPortraitMaxHeight(height);
    }


    private void setSaveBestFrameEnabled(boolean enabled) {
        photoMaker.setSaveBestFrameEnabled(enabled);
    }

    private void setScaleFactor(float scaleFactor) {
        photoMaker.setFrameScaleFactor(scaleFactor);
    }

    public void calcFaceDescriptorFromBestFrame() {
        photoMaker.calcFaceDescriptorFromBestFrame();
    }

    public byte[] getFaceDescriptorByteArray() {
        return photoMaker.getFaceDescriptorByteArray();
    }

    public float matchDescriptors(byte[] descriptor1, byte[] descriptor2) {
        return photoMaker.matchDescriptors(descriptor1, descriptor2);
    }

    public static boolean initFaceEngine(String path) {
        return FaceEngineJNI.initFaceEngine(path);
    }

    public interface Listener {

        void onFaceArea(boolean detected, @Nullable android.graphics.Rect rect,
                @Nullable Boolean fastMove, @Nullable Boolean rotate, boolean[] qualityStates);

        void onBestFrameReady();

        void onLivenessSucceed();

        void onLivenessWaitingOpenedEyes();

        void onLuminanceState(PhotoProcessor.LuminanceState state);

        void onLivenessResult(int state, int action);

        boolean isFaceInsideBorder(@Nullable android.graphics.Rect rect);

    }

    public static class Builder {

        private float scaleFactor = 0.5f;

        private int numberOfFrames = 1;

        private float scoreThreshold = 0.05f;

        private float confidenceScore = 0.01f;

        private float movementThreshold = 0.06f;

        private float rotationLimit = 15.0f;

        private int portraitMaxHeight = 640;

        private boolean saveBestFrameEnabled = true;

        private String path;

        public Builder() {

        }

        public Builder frameScaleFactor(float scaleFactor) {
            this.scaleFactor = scaleFactor;
            return this;
        }

        /**
         * Set maximum number of frames to keep track of the face when good face
         * detection is not available.
         * Tracking is used to predict fac position in case of obscurrance and/or
         * rapid movements of the face.
         *
         * @param numberOfFrames number of frames to track.
         */
        public Builder maxNumberOfFramesWithoutDetection(int numberOfFrames) {
            this.numberOfFrames = numberOfFrames;
            return this;
        }

        /**
         * Set minimum best shot detection score value.
         *
         * @param scoreThreshold score value.
         */
        public Builder bestShotScoreThreshold(float scoreThreshold) {
            this.scoreThreshold = scoreThreshold;
            return this;
        }

        /**
         * Set minimum movement value to prevent blure shots
         *
         * @param movementThreshold movement threshold.
         */
        public Builder movementThreshold(float movementThreshold) {
            this.movementThreshold = movementThreshold;
            return this;
        }

        /**
         * Set maximum head rotation about all three coordiantes axes.
         * If head rotation angles do not exceed the given limit, the
         * head pose is considered frontal, thus eligible for the best
         * shot procedure.
         * Absolute value should be specified; this will allow
         * rotations within [-rotationLimit, rotationLimit] range.
         * Default is 10 degrees.
         *
         * @param rotationLimit rotation limit (in degrees).
         */
        public Builder rotationLimit(float rotationLimit) {
            this.rotationLimit = rotationLimit;
            return this;
        }

        /**
         * Set path to VL engine files
         *
         * @param path path to VL engine files
         */
        public Builder pathToData(String path) {
            this.path = path;
            return this;
        }

        public PhotoProcessor build(Context context) {
            final PhotoProcessor photoProcessor = new PhotoProcessor(context);
            if (TextUtils.isEmpty(path)) {
                photoProcessor.loadData(context.getFilesDir() + "/vl/data");
            } else {
                photoProcessor.loadData(path);
            }
            System.out.println("PATH default" + path);
            photoProcessor.setScaleFactor(scaleFactor);

            photoProcessor.setBestShotScoreThreshold(scoreThreshold);
            photoProcessor.setRotationLimit(rotationLimit);
            photoProcessor.setPortraitMaxHeight(portraitMaxHeight);
            photoProcessor.setSaveBestFrameEnabled(saveBestFrameEnabled);
            return photoProcessor;
        }

    }
}
