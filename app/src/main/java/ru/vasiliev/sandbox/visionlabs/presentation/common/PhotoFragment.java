package ru.vasiliev.sandbox.visionlabs.presentation.common;


import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.R;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsConfig;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.FaceNotFoundFragment;
import ru.vasiliev.sandbox.visionlabs.repository.VisionLabsPreferences;
import ru.vasiliev.sandbox.visionlabs.view.FaceBoundSurfaceView;
import ru.visionlab.faceengine.FaceEngineJNI;
import ru.visionlab.faceengine.PhotoProcessor;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@SuppressWarnings("deprecation")
public class PhotoFragment extends Fragment
        implements Camera.PreviewCallback, PhotoProcessor.Listener {

    protected static final int TIMEOUT = 10;

    protected static final int LIVENESS_TIMEOUT = 15;

    @BindView(R.id.warning)
    TextView warning;

    @BindView(R.id.ZOOM_STATE)
    TextView zoom_state;

    @BindView(R.id.maskedView)
    ImageView mask;

    @BindView(R.id.preview)
    SurfaceView preview;

    @BindView(R.id.faceBoundView)
    FaceBoundSurfaceView faceBoundView;

    @BindView(R.id.sendPlaceholder)
    FrameLayout sendPlaceholder;

    @BindView(R.id.layout1)
    LinearLayout layout1;

    @Inject
    VisionLabsPreferences preferences;

    PhotoProcessor photoProcessor;

    HolderCallback holderCallback;

    Camera camera;

    boolean MASKBIG = false;

    boolean MASKSMALL = false;

    boolean MASKMEDIUM = false;

    int maskTop;

    int maskBottom;

    int maskLeft;

    int maskRight;

    boolean faceInGoodState;

    boolean ShowMsg = true;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.timelog)
    TextView timelog;

    @BindView(R.id.flipCam)
    ImageView flipCamView;

    private Listener listener;

    private boolean ignoreTimeout;

    private boolean FaceLost = false;

    Rect maskRect;

    private boolean checkLiveness = false;

    private boolean cameraFlashIsSupported = false;

    private int cameraID = -1;

    private int cameraFacing = -1;

    private long livenessDelta = 0;

    private long livenessStart = 0;

    public PhotoFragment() {
        maskRect = new android.graphics.Rect();
    }

    public static PhotoFragment newInstance() {

        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setPhotoProcessor(PhotoProcessor photoProcessor) {
        this.photoProcessor = photoProcessor;
    }

    public void enableLivenessCheck(boolean enable) {
        this.checkLiveness = enable;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getComponentManager().getVisionLabsComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        boolean useFrontCamera = false;

        if (preferences.getUseFrontCamera()) {

            int cameraIdTmp = getCameraIdWithFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);

            if (cameraIdTmp == -1) {
                useFrontCamera = false;
            } else {
                useFrontCamera = true;
                cameraID = cameraIdTmp;
                cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
        }

        if (!useFrontCamera) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
            cameraID = getCameraIdWithFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        ButterKnife.bind(this, view);

        flipCamView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCam();
            }
        });

        return view;
    }

    public void showWaitState() {
        mask.setVisibility(View.GONE);
        sendPlaceholder.setVisibility(View.VISIBLE);
        camera.stopPreview();
    }

    public void hideWaitState() {
        if (preferences.getZoomAuth()
                && checkLiveness) {//set mask to visible in auth stage if zoom liveness is enabled
            mask.setVisibility(View.VISIBLE);
        } else {
            mask.setVisibility(preferences.getShowDetection() ? View.INVISIBLE : View.VISIBLE);
        }
        sendPlaceholder.setVisibility(View.GONE);
    }

    private void detectMaskDimensions(Bitmap bitmap) {
        if (MASKSMALL) {
            maskLeft = bitmap.getWidth() / 4;
            maskRight = maskLeft * 3;
            maskTop = bitmap.getHeight() / 4;
            maskBottom = maskTop * 3;
        } else if (MASKMEDIUM) {
            maskLeft = bitmap.getWidth() / 7;
            maskRight = maskLeft * 6;
            maskTop = bitmap.getHeight() / 7;
            maskBottom = maskTop * 6;
        } else if (MASKBIG) {
            maskLeft = 0;
            maskRight = bitmap.getWidth();
            maskTop = bitmap.getHeight() / 8;
            maskBottom = maskTop * 8;
        }

        maskRect.left = maskLeft;
        maskRect.right = maskRight;
        maskRect.top = maskTop;
        maskRect.bottom = maskBottom;
        Log.i("RECT", "RECT FLATTEN" + maskRect.flattenToString());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Фото");
        zoom_state.setTextColor(Color.parseColor("#551A8B"));//dark purple

        if (!checkLiveness) {
            MASKBIG = false;
            MASKMEDIUM = true;
            MASKSMALL = false;
            Picasso.with(getContext()).load(R.drawable.mask).centerCrop().fit()
                    .into(mask, new Callback() {

                        @Override
                        public void onSuccess() {
                            final Bitmap bitmap = ((BitmapDrawable) mask.getDrawable()).getBitmap();
                            detectMaskDimensions(bitmap);
                        }

                        @Override
                        public void onError() {

                        }
                    });
        } else {
            if (preferences.getZoomAuth()) {
                MASKBIG = false;
                MASKMEDIUM = false;
                MASKSMALL = true;
                zoom_state.setVisibility(View.VISIBLE);
                zoom_state.setText(R.string.zoom_out_and_look_straight_at_the_camera);
                Picasso.with(getContext()).load(R.drawable.mask_small).centerCrop().fit()
                        .into(mask, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Bitmap bitmap = ((BitmapDrawable) mask.getDrawable())
                                        .getBitmap();
                                detectMaskDimensions(bitmap);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            } else if (preferences.getEyesAuth() && (!preferences.getShowDetection())) {
                MASKBIG = false;
                MASKMEDIUM = true;
                MASKSMALL = false;
                Log.i("MASK", "Drawing medium mask");
                Picasso.with(getContext()).load(R.drawable.mask).centerCrop().fit()
                        .into(mask, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Bitmap bitmap = ((BitmapDrawable) mask.getDrawable())
                                        .getBitmap();
                                detectMaskDimensions(bitmap);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }

        }
        getActivity().setTitle(getContext().getString(R.string.fragment_photo_title));
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent),
                        PorterDuff.Mode.MULTIPLY);

    }

    @Override
    public void onStart() {
        super.onStart();
        ignoreTimeout = false;
        final long delay = TIMEOUT * DateUtils.SECOND_IN_MILLIS - (System.currentTimeMillis() - Long
                .parseLong(preferences.getStartTime()));
        Observable.timer(Math.max(delay, 0), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(value -> {
                    if (listener != null && !ignoreTimeout) {
                        listener.onTimeout(FaceNotFoundFragment.Reason.NOT_FOUND);
                        listener.onTimeout();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera == null) {
            startCapture();
        }
        photoProcessor.setListener(this);
        photoProcessor.setNeedPortrait(preferences.getNeedPortrait());
        photoProcessor.disableOpenEyesCheck(preferences.getIgnoreEyes());
        hideWaitState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private int getCameraID() {
        return cameraID;
    }

    private void startCapture() {
        camera = Camera.open(getCameraID());
        configureCamera();
        final ViewTreeObserver viewTreeObserver = preview.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                preview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                setPreviewSize(true);
                holderCallback = new HolderCallback();
                preview.getHolder().addCallback(holderCallback);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.setPreviewCallbackWithBuffer(null);
            preview.getHolder().removeCallback(holderCallback);
            holderCallback = null;
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        photoProcessor.removeListeners();
    }


    void setPreviewSize(boolean fullScreen) {
        final int width = preview.getMeasuredWidth();
        final int height = preview.getMeasuredHeight();
        boolean widthIsMax = width > height;

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        rectDisplay.set(0, 0, width, height);

        // RectF первью
        if (widthIsMax) {
            rectPreview
                    .set(0, 0, photoProcessor.getPreviewWidth(), photoProcessor.getPreviewHeight());
        } else {
            rectPreview
                    .set(0, 0, photoProcessor.getPreviewHeight(), photoProcessor.getPreviewWidth());
        }

        Matrix matrix = new Matrix();

        if (!fullScreen) {
            matrix.setRectToRect(rectPreview, rectDisplay, Matrix.ScaleToFit.START);
        } else {
            matrix.setRectToRect(rectDisplay, rectPreview, Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        matrix.mapRect(rectPreview);

        final ViewGroup.LayoutParams layoutParams = preview.getLayoutParams();
        if (layoutParams.width != (int) rectPreview.right
                || layoutParams.height != (int) rectPreview.bottom) {
            layoutParams.height = (int) (rectPreview.bottom);
            layoutParams.width = (int) (rectPreview.right);
            preview.setLayoutParams(layoutParams);
        } else {
            startCameraPreview(preview.getHolder());
        }

    }

    void setCameraDisplayOrientation(int cameraId) {

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            photoProcessor.setMainCamera(true);
            result = ((360 - degrees) + info.orientation);
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            photoProcessor.setMainCamera(false);
            result = ((360 - degrees) - info.orientation);
            result += 360;
        }
        result = result % 360;
        camera.setDisplayOrientation(result);
        photoProcessor.setImageRotation(result);
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        photoProcessor.processFrame(data);
        camera.addCallbackBuffer(photoProcessor.getCallbackBuffer());
    }

    @Override
    public void onLivenessResult(int state, int action) {

        livenessDelta = System.currentTimeMillis() - livenessStart;
        Log.i("LTIMER", "Liveness Delta" + livenessDelta / 1000 + " sec");

        if (state < 0 || livenessDelta / 1000 > LIVENESS_TIMEOUT) {
            if (listener != null) {
                Log.i("LTIMER", "TIMEOUT!!");
                ShowMsg = true;
                listener.onTimeout(FaceNotFoundFragment.Reason.LIVENESS);
            }
        }

        if (state == 0) {//LSDKError::Ok
            zoom_state.setVisibility(View.VISIBLE);
            Log.i("STATE", "STATE IS GOOD");
            zoom_state.setText(R.string.zoom_success);
        } else if (state == 3) {//LSDKError::PreconditionFailed
            Log.i("VIEW", "Precondition failed!");

            if (action == 7) {//LA_EYE
                zoom_state.setVisibility(View.VISIBLE);
                zoom_state.setText(R.string.look_straight_at_the_camer);
                ShowMsg = true;
            }
            if (action == 8) {//LA_ZOOM
                zoom_state.setVisibility(View.VISIBLE);
                zoom_state.setText(R.string.zoom_out_and_look_straight_at_the_camera);
                MASKBIG = false;
                MASKMEDIUM = false;
                MASKSMALL = true;
                Picasso.with(getContext()).load(R.drawable.mask_small).centerCrop().fit()
                        .into(mask, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Bitmap bitmap = ((BitmapDrawable) mask.getDrawable())
                                        .getBitmap();
                                detectMaskDimensions(bitmap);
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }
            FaceEngineJNI.resetLiveness();
            FaceLost = true;
        } else if (state == 2) {//LSDKError::NotReady
            if (FaceLost) {
                livenessStart = System.currentTimeMillis();
                FaceLost = false;
            }
            zoom_state.setVisibility(View.VISIBLE);
            if (action == 7) {
                zoom_state.setText(getString(R.string.close_eyes));
            } else if (action == 8) {
                MASKBIG = true;
                MASKMEDIUM = false;
                MASKSMALL = false;
                zoom_state.setText(getString(R.string.zoom_in_and_look_straight_at_the_camera));
                Picasso.with(getContext()).load(R.drawable.mask_big).centerCrop().fit()
                        .into(mask, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Bitmap bitmap = ((BitmapDrawable) mask.getDrawable())
                                        .getBitmap();
                                detectMaskDimensions(bitmap);
                            }

                            @Override
                            public void onError() {
                            }
                        });
            } else {
                zoom_state.setText("");
            }
        }

        if (listener != null) {
            listener.onLivenessResult(state, action);
        }
    }

    @Override
    public boolean isFaceInsideBorder(Rect rect) {
        if (preferences.getZoomAuth() && checkLiveness) {
            final int width = preview.getMeasuredWidth();
            final int height = preview.getMeasuredHeight();

            float widthCorrection = ((float) width) / photoProcessor.getPreviewHeight();
            float heightCorrection = ((float) height) / photoProcessor.getPreviewWidth();

            int left = (int) (rect.left * widthCorrection);
            int top = (int) (rect.top * heightCorrection);

            int right = (int) (rect.right * widthCorrection);
            int bottom = (int) (rect.bottom * heightCorrection);
            return maskRect.contains(left, top, right, bottom);
        }
        return true;
    }

    @Override
    public void onFaceArea(boolean detected, Rect rect, Boolean fastMove, Boolean isFrontalPose,
            boolean[] qualityStates) {
        boolean showDetectionPreference = preferences.getShowDetection();
        boolean showDetectionRect = showDetectionPreference;

        if (detected) {
            final int width = preview.getMeasuredWidth();
            final int height = preview.getMeasuredHeight();

            float widthCorrection = ((float) width) / photoProcessor.getPreviewHeight();
            float heightCorrection = ((float) height) / photoProcessor.getPreviewWidth();

            int left = (int) (rect.left * widthCorrection);
            int top = (int) (rect.top * heightCorrection);

            int right = (int) (rect.right * widthCorrection);
            int bottom = (int) (rect.bottom * heightCorrection);

            if (showDetectionRect) {
                if (preferences.getZoomAuth() && checkLiveness) {
                    showDetectionRect = false;
                    showDetectionPreference = false;
                    //do not draw rectangle
                } else {
                    Log.i("RECT", "Settings facerect!");
                    faceBoundView.setFaceRect(left, top, right, bottom);
                }
            }

            Rect r = new Rect(left, top, right, bottom);

            Log.i("RECT", "DETECTOR RECT FLATTEN" + r.flattenToString());
            Log.i("RECT", "MASK RECT FLATTEN" + maskRect.flattenToString());
            faceInGoodState = maskRect.isEmpty() || maskRect.contains(left, top, right, bottom);

            Log.i("YASSS", "Containts? " + faceInGoodState);

            if (faceInGoodState) {
                if (!isFrontalPose) {
                    warning.setVisibility(View.VISIBLE);
                    warning.setText(R.string.look_straight_at_the_camer);
                    faceInGoodState = false;
                } else if (fastMove) {
                    warning.setVisibility(View.VISIBLE);
                    warning.setText(R.string.moving_too_fast);
                    faceInGoodState = false;
                } else {
                    warning.setVisibility(View.INVISIBLE);
                    final int[] qualityStatesStringsResId = new int[]{R.string.overdark,
                            R.string.overlight, R.string.overgray, R.string.overblur};
                }

            } else {
                warning.setVisibility(showDetectionPreference ? View.INVISIBLE : View.VISIBLE);
                Log.i("OVAL", "PUT YOUR HEAD INTO OVAL INNER");
                warning.setText(R.string.put_your_head_into_oval);
            }

        } else {
            Log.i("OVAL", "PUT YOUR HEAD INTO OVAL OUT");
            warning.setVisibility(showDetectionPreference ? View.INVISIBLE : View.VISIBLE);
            warning.setText(R.string.put_your_head_into_oval);
            showDetectionRect = false;
        }
        Log.i("RECT", "SHODETECTIONRECT " + showDetectionRect);
        faceBoundView.setVisibility(showDetectionRect ? View.VISIBLE : View.INVISIBLE);

        if (showDetectionRect) {
            faceBoundView.setFaceRectColor(faceInGoodState ? Color.GREEN : Color.RED);
            faceBoundView.invalidate();
        }
    }


    @Override
    public void onBestFrameReady() {
        //faceInGoodState &&
        if (faceInGoodState && listener != null) {
            ignoreTimeout = true;
            Log.i("liv", "checkLiveness is " + checkLiveness);
            //check if we are in auth stage
            if (checkLiveness) {
                if (preferences.getEyesAuth()) {

                    photoProcessor.setEyeLiveness();
                    photoProcessor.startCheckLiveness();
                } else if (preferences.getZoomAuth()) {
                    photoProcessor.setZoomLiveness();
                    photoProcessor.startCheckLivenessZoom();
                }

                livenessStart = System.currentTimeMillis();

            } else {
                submitBestShot();
            }
        } else {
            photoProcessor.resumeSearch();
        }
    }

    @Override
    public void onLivenessSucceed() {
        submitBestShot();
    }

    @Override
    public void onLivenessWaitingOpenedEyes() {
        listener.onLivenessWaitingOpenedEyes();
    }

    private void submitBestShot() {
        listener.onBestFrameReady(photoProcessor.getBestShot());
    }

    @Override
    public void onLuminanceState(PhotoProcessor.LuminanceState state) {
        // Log.i("TEST"," ENTERING LUMINANCE");
        if (!cameraFlashIsSupported) {
            return;
        }

        // TODO:

        final String flashModeNew = state.darknessState == 0 ? Camera.Parameters.FLASH_MODE_TORCH
                : Camera.Parameters.FLASH_MODE_OFF;
        Camera.Parameters parameters = camera.getParameters();

        if (!flashModeNew.equals(parameters.getFlashMode())) {
            parameters.setFlashMode(flashModeNew);
            camera.setParameters(parameters);

            photoProcessor.setFlashTorchState(state.darknessState == 0 ? true : false);
        }
    }

    private Camera.Size getBestPreviewSize() {
        final List<Camera.Size> supportedPreviewSizes = camera.getParameters()
                .getSupportedPreviewSizes();
        final List<Camera.Size> filteredSizes = new ArrayList<>();
        for (Camera.Size previewSize : supportedPreviewSizes) {
            if (previewSize.width <= VisionLabsConfig.PREVIEW_WIDTH
                    && previewSize.height <= VisionLabsConfig.PREVIEW_HEIGHT) {
                filteredSizes.add(previewSize);
            }
        }

        Camera.Size bestSize = filteredSizes.get(0);
        for (int i = 1; i < filteredSizes.size(); i++) {
            Camera.Size currentSize = filteredSizes.get(i);
            if (currentSize.width >= bestSize.width && currentSize.height >= bestSize.height) {
                bestSize = currentSize;
            }
        }
        return bestSize;
    }

    private void configureCamera() {
        //Log.i("TEST"," ENTERING ON START_CAPTURE");
        final Camera.Parameters parameters = camera.getParameters();
        try {
            parameters.setPreviewFormat(ImageFormat.NV21);

            // set focus for video if present
            List<String> focusModes = parameters.getSupportedFocusModes();

            if (null != focusModes && focusModes
                    .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            // check if torch is present
            List<String> flashModes = parameters.getSupportedFlashModes();

            cameraFlashIsSupported = null != flashModes && flashModes
                    .contains(Camera.Parameters.FLASH_MODE_TORCH);

            final Camera.Size bestPreviewSize = getBestPreviewSize();
            photoProcessor.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
            parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
            camera.setParameters(parameters);
        } catch (RuntimeException exception) {
            Toast.makeText(getContext(), R.string.camera_configuration_failed, Toast.LENGTH_SHORT)
                    .show();
        }
        //   Log.i("TEST"," ENTERING ON START_CAPTURE");
    }

    @Override
    public String toString() {
        return "PhotoFragment";
    }

    private void startCameraPreview(SurfaceHolder holder) {
        // Log.i("TEST"," ENTERING ON START_CAMERA_PREVIEW");
        if (camera != null) {
            camera.stopPreview();
            setCameraDisplayOrientation(getCameraID());
            camera.addCallbackBuffer(photoProcessor.getCallbackBuffer());
            camera.setPreviewCallbackWithBuffer(PhotoFragment.this);

            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //  Log.i("TEST"," QUITTING ON START_CAMERA_PREVIEW");
    }

    private int getToggledCameraFacing() {
        return cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
                ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    private int getCameraIdWithFacing(int facing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    private void restartCameraPreview(SurfaceHolder holder) {
        if (camera != null) {

            // check if it is allowed to flip cam (is there cam with another facing)

            // TODO: fix code : cameraID != facing

            // flip cameraID if there's available another one
            int camerasNumber = Camera.getNumberOfCameras();

            boolean ableToFlip = false;
            int newCameraID = -1;

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < camerasNumber; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == getToggledCameraFacing()) {
                    ableToFlip = true;
                    newCameraID = i;
                    break;
                }
            }

            if (!ableToFlip) {
                return;
            }

            camera.stopPreview();
            camera.release();

            cameraID = newCameraID;
            cameraFacing = getToggledCameraFacing();

            camera = Camera.open(getCameraID());

            configureCamera();

            setCameraDisplayOrientation(getCameraID());
            camera.addCallbackBuffer(photoProcessor.getCallbackBuffer());
            camera.setPreviewCallbackWithBuffer(PhotoFragment.this);

            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface Listener {

        void onBestFrameReady(Bitmap bitmap);

        void onTimeout(FaceNotFoundFragment.Reason reason);

        void onNeedCameraPermission();

        void onLivenessWaitingOpenedEyes();

        void onTimeout();

        void onLivenessResult(int state, int action);
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (camera != null) {
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            startCameraPreview(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    private void flipCam() {
        restartCameraPreview(preview.getHolder());
    }
}
