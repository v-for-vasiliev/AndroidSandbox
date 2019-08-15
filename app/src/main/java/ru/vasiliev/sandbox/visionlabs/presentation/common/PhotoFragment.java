package ru.vasiliev.sandbox.visionlabs.presentation.common;


import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
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
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsPreferences;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsConfig;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.FaceNotFoundFragment;
import ru.vasiliev.sandbox.visionlabs.view.FaceBoundSurfaceView;
import ru.visionlab.faceengine.FaceEngineJNI;
import ru.visionlab.faceengine.PhotoProcessor;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PhotoFragment extends Fragment
        implements Camera.PreviewCallback, PhotoProcessor.Listener {

    public static final String TAG = PhotoFragment.class.getName();

    public interface Listener {

        void onBestFrameReady(Bitmap bitmap);

        void onTimeout(FaceNotFoundFragment.Reason reason);

        void onTimeout();

        void onLivenessWaitingOpenedEyes();

        void onLivenessResult(int state, int action);
    }

    protected static final int TIMEOUT = 10;

    protected static final int LIVENESS_TIMEOUT = 15;

    @BindView(R.id.warning)
    TextView mWarning;

    @BindView(R.id.liveness_action)
    TextView mLivenessActionText;

    @BindView(R.id.face_mask)
    ImageView mMaskImage;

    @BindView(R.id.preview)
    SurfaceView mPreview;

    @BindView(R.id.face_bound_view)
    FaceBoundSurfaceView mFaceBoundSurfaceView;

    @BindView(R.id.verification_time)
    TextView mVerificationTimeText;

    @BindView(R.id.flipCam)
    ImageView mFlipCameraButton;

    @Inject
    VisionLabsPreferences mVisionLabsPreferences;

    PhotoProcessor mPhotoProcessor;

    HolderCallback mHolderCallback;

    Camera mCamera;

    boolean mMaskBig = false;

    boolean mMaskSmall = false;

    boolean mMaskMedium = false;

    int mMaskTop;

    int mMaskBottom;

    int mMaskLeft;

    int mMaskRight;

    boolean mFaceInGoodState;

    boolean mShowMsg = true;

    private Listener mListener;

    private boolean mIgnoreTimeout;

    private boolean mFaceLost = false;

    Rect mMaskRect;

    private boolean mCheckLiveness = false;

    private boolean cameraFlashIsSupported = false;

    private int mCameraId = -1;

    private int mCameraFacing = -1;

    private long mLivenessDelta = 0;

    private long mLivenessStart = 0;

    public static PhotoFragment newInstance() {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoFragment() {
        mMaskRect = new android.graphics.Rect();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setPhotoProcessor(PhotoProcessor photoProcessor) {
        mPhotoProcessor = photoProcessor;
    }

    public void enableLivenessCheck(boolean enable) {
        mCheckLiveness = enable;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getComponentManager().getVisionLabsComponent().inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return createView(inflater, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mIgnoreTimeout = false;
        final long delay = TIMEOUT * DateUtils.SECOND_IN_MILLIS - (System.currentTimeMillis() - Long
                .parseLong(mVisionLabsPreferences.getStartTime()));
        Observable.timer(Math.max(delay, 0), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(value -> {
                    if (mListener != null && !mIgnoreTimeout) {
                        mListener.onTimeout(FaceNotFoundFragment.Reason.NOT_FOUND);
                        mListener.onTimeout();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera == null) {
            startCapture();
        }
        mPhotoProcessor.setListener(this);
        mPhotoProcessor.setNeedPortrait(mVisionLabsPreferences.getNeedPortrait());
        mPhotoProcessor.disableOpenEyesCheck(mVisionLabsPreferences.getIgnoreEyes());
        hideWaitState();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mPreview.getHolder().removeCallback(mHolderCallback);
            mHolderCallback = null;
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mPhotoProcessor.removeListeners();
    }

    private View createView(LayoutInflater inflater, ViewGroup container) {
        boolean useFrontCamera = false;

        if (mVisionLabsPreferences.getUseFrontCamera()) {
            int cameraIdTmp = getCameraIdWithFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
            if (cameraIdTmp == -1) {
                useFrontCamera = false;
            } else {
                useFrontCamera = true;
                mCameraId = cameraIdTmp;
                mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
        }

        if (!useFrontCamera) {
            mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
            mCameraId = getCameraIdWithFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        ButterKnife.bind(this, view);

        mFlipCameraButton.setOnClickListener(flipCamView -> flipCam());

        return view;
    }

    private void initView() {
        if (!mCheckLiveness) {
            mMaskBig = false;
            mMaskMedium = true;
            mMaskSmall = false;
            Picasso.with(getContext()).load(R.drawable.mask).centerCrop().fit()
                    .into(mMaskImage, new Callback() {

                        @Override
                        public void onSuccess() {
                            final Bitmap bitmap = ((BitmapDrawable) mMaskImage.getDrawable())
                                    .getBitmap();
                            detectMaskDimensions(bitmap);
                        }

                        @Override
                        public void onError() {

                        }
                    });
        } else {
            if (mVisionLabsPreferences.getZoomAuth()) {
                mMaskBig = false;
                mMaskMedium = false;
                mMaskSmall = true;
                mLivenessActionText.setVisibility(View.VISIBLE);
                mLivenessActionText.setText(R.string.zoom_out_and_look_straight_at_the_camera);
                Picasso.with(getContext()).load(R.drawable.mask_small).centerCrop().fit()
                        .into(mMaskImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Bitmap bitmap = ((BitmapDrawable) mMaskImage.getDrawable())
                                        .getBitmap();
                                detectMaskDimensions(bitmap);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            } else if (mVisionLabsPreferences.getEyesAuth() && (!mVisionLabsPreferences
                    .getShowDetection())) {
                mMaskBig = false;
                mMaskMedium = true;
                mMaskSmall = false;
                Log.i("MASK", "Drawing medium mask");
                Picasso.with(getContext()).load(R.drawable.mask).centerCrop().fit()
                        .into(mMaskImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Bitmap bitmap = ((BitmapDrawable) mMaskImage.getDrawable())
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
    }

    public void pause() {

    }

    public void resume() {

    }

    public void showWaitState() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mPhotoProcessor.removeListeners();
    }

    public void hideWaitState() {
        // Set mask to visible in auth stage if zoom liveness is enabled
        if (mVisionLabsPreferences.getZoomAuth() && mCheckLiveness) {
            mMaskImage.setVisibility(View.VISIBLE);
        } else {
            mMaskImage.setVisibility(
                    mVisionLabsPreferences.getShowDetection() ? View.INVISIBLE : View.VISIBLE);
        }
        if (mCamera != null) {
            mCamera.startPreview();
        }
        mPhotoProcessor.setListener(this);
    }


    private void detectMaskDimensions(Bitmap bitmap) {
        if (mMaskSmall) {
            mMaskLeft = bitmap.getWidth() / 4;
            mMaskRight = mMaskLeft * 3;
            mMaskTop = bitmap.getHeight() / 4;
            mMaskBottom = mMaskTop * 3;
        } else if (mMaskMedium) {
            mMaskLeft = bitmap.getWidth() / 7;
            mMaskRight = mMaskLeft * 6;
            mMaskTop = bitmap.getHeight() / 7;
            mMaskBottom = mMaskTop * 6;
        } else if (mMaskBig) {
            mMaskLeft = 0;
            mMaskRight = bitmap.getWidth();
            mMaskTop = bitmap.getHeight() / 8;
            mMaskBottom = mMaskTop * 8;
        }

        mMaskRect.left = mMaskLeft;
        mMaskRect.right = mMaskRight;
        mMaskRect.top = mMaskTop;
        mMaskRect.bottom = mMaskBottom;
        Log.i("RECT", "RECT FLATTEN" + mMaskRect.flattenToString());
    }

    private int getCameraId() {
        return mCameraId;
    }

    private void startCapture() {
        mCamera = Camera.open(getCameraId());
        configureCamera();
        final ViewTreeObserver viewTreeObserver = mPreview.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPreview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                setPreviewSize(true);
                mHolderCallback = new HolderCallback();
                mPreview.getHolder().addCallback(mHolderCallback);
            }
        });
    }


    void setPreviewSize(boolean fullScreen) {
        final int width = mPreview.getMeasuredWidth();
        final int height = mPreview.getMeasuredHeight();
        boolean widthIsMax = width > height;

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        rectDisplay.set(0, 0, width, height);

        // RectF первью
        if (widthIsMax) {
            rectPreview.set(0, 0, mPhotoProcessor.getPreviewWidth(),
                    mPhotoProcessor.getPreviewHeight());
        } else {
            rectPreview.set(0, 0, mPhotoProcessor.getPreviewHeight(),
                    mPhotoProcessor.getPreviewWidth());
        }

        Matrix matrix = new Matrix();

        if (!fullScreen) {
            matrix.setRectToRect(rectPreview, rectDisplay, Matrix.ScaleToFit.START);
        } else {
            matrix.setRectToRect(rectDisplay, rectPreview, Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        matrix.mapRect(rectPreview);

        final ViewGroup.LayoutParams layoutParams = mPreview.getLayoutParams();
        if (layoutParams.width != (int) rectPreview.right
                || layoutParams.height != (int) rectPreview.bottom) {
            layoutParams.height = (int) (rectPreview.bottom);
            layoutParams.width = (int) (rectPreview.right);
            mPreview.setLayoutParams(layoutParams);
        } else {
            startCameraPreview(mPreview.getHolder());
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
            mPhotoProcessor.setMainCamera(true);
            result = ((360 - degrees) + info.orientation);
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mPhotoProcessor.setMainCamera(false);
            result = ((360 - degrees) - info.orientation);
            result += 360;
        }
        result = result % 360;
        mCamera.setDisplayOrientation(result);
        mPhotoProcessor.setImageRotation(result);
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mPhotoProcessor.processFrame(data);
        camera.addCallbackBuffer(mPhotoProcessor.getCallbackBuffer());
    }

    @Override
    public void onLivenessResult(int state, int action) {

        mLivenessDelta = System.currentTimeMillis() - mLivenessStart;
        Log.i("LTIMER", "Liveness Delta" + mLivenessDelta / 1000 + " sec");

        if (state < 0 || mLivenessDelta / 1000 > LIVENESS_TIMEOUT) {
            if (mListener != null) {
                Log.i("LTIMER", "TIMEOUT!!");
                mShowMsg = true;
                mListener.onTimeout(FaceNotFoundFragment.Reason.LIVENESS);
            }
        }

        if (state == 0) {//LSDKError::Ok
            mLivenessActionText.setVisibility(View.VISIBLE);
            Log.i("STATE", "STATE IS GOOD");
            mLivenessActionText.setText(R.string.zoom_success);
        } else if (state == 3) {//LSDKError::PreconditionFailed
            Log.i("VIEW", "Precondition failed!");

            if (action == 7) {//LA_EYE
                mLivenessActionText.setVisibility(View.VISIBLE);
                mLivenessActionText.setText(R.string.look_straight_at_the_camer);
                mShowMsg = true;
            }
            if (action == 8) {//LA_ZOOM
                mLivenessActionText.setVisibility(View.VISIBLE);
                mLivenessActionText.setText(R.string.zoom_out_and_look_straight_at_the_camera);
                mMaskBig = false;
                mMaskMedium = false;
                mMaskSmall = true;
                Picasso.with(getContext()).load(R.drawable.mask_small).centerCrop().fit()
                        .into(mMaskImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Bitmap bitmap = ((BitmapDrawable) mMaskImage.getDrawable())
                                        .getBitmap();
                                detectMaskDimensions(bitmap);
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }
            FaceEngineJNI.resetLiveness();
            mFaceLost = true;
        } else if (state == 2) {//LSDKError::NotReady
            if (mFaceLost) {
                mLivenessStart = System.currentTimeMillis();
                mFaceLost = false;
            }
            mLivenessActionText.setVisibility(View.VISIBLE);
            if (action == 7) {
                mLivenessActionText.setText(getString(R.string.close_eyes));
            } else if (action == 8) {
                mMaskBig = true;
                mMaskMedium = false;
                mMaskSmall = false;
                mLivenessActionText
                        .setText(getString(R.string.zoom_in_and_look_straight_at_the_camera));
                Picasso.with(getContext()).load(R.drawable.mask_big).centerCrop().fit()
                        .into(mMaskImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Bitmap bitmap = ((BitmapDrawable) mMaskImage.getDrawable())
                                        .getBitmap();
                                detectMaskDimensions(bitmap);
                            }

                            @Override
                            public void onError() {
                            }
                        });
            } else {
                mLivenessActionText.setText("");
            }
        }

        if (mListener != null) {
            mListener.onLivenessResult(state, action);
        }
    }

    @Override
    public boolean isFaceInsideBorder(Rect rect) {
        if (mVisionLabsPreferences.getZoomAuth() && mCheckLiveness) {
            final int width = mPreview.getMeasuredWidth();
            final int height = mPreview.getMeasuredHeight();

            float widthCorrection = ((float) width) / mPhotoProcessor.getPreviewHeight();
            float heightCorrection = ((float) height) / mPhotoProcessor.getPreviewWidth();

            int left = (int) (rect.left * widthCorrection);
            int top = (int) (rect.top * heightCorrection);

            int right = (int) (rect.right * widthCorrection);
            int bottom = (int) (rect.bottom * heightCorrection);
            return mMaskRect.contains(left, top, right, bottom);
        }
        return true;
    }

    @Override
    public void onFaceArea(boolean detected, Rect rect, Boolean fastMove, Boolean isFrontalPose,
            boolean[] qualityStates) {
        boolean showDetectionPreference = mVisionLabsPreferences.getShowDetection();
        boolean showDetectionRect = showDetectionPreference;

        if (detected) {
            final int width = mPreview.getMeasuredWidth();
            final int height = mPreview.getMeasuredHeight();

            float widthCorrection = ((float) width) / mPhotoProcessor.getPreviewHeight();
            float heightCorrection = ((float) height) / mPhotoProcessor.getPreviewWidth();

            int left = (int) (rect.left * widthCorrection);
            int top = (int) (rect.top * heightCorrection);

            int right = (int) (rect.right * widthCorrection);
            int bottom = (int) (rect.bottom * heightCorrection);

            if (showDetectionRect) {
                if (mVisionLabsPreferences.getZoomAuth() && mCheckLiveness) {
                    showDetectionRect = false;
                    showDetectionPreference = false;
                    //do not draw rectangle
                } else {
                    Log.i("RECT", "Settings facerect!");
                    mFaceBoundSurfaceView.setFaceRect(left, top, right, bottom);
                }
            }

            Rect r = new Rect(left, top, right, bottom);

            Log.i("RECT", "DETECTOR RECT FLATTEN" + r.flattenToString());
            Log.i("RECT", "MASK RECT FLATTEN" + mMaskRect.flattenToString());
            mFaceInGoodState = mMaskRect.isEmpty() || mMaskRect.contains(left, top, right, bottom);

            Log.i("YASSS", "Containts? " + mFaceInGoodState);

            if (mFaceInGoodState) {
                if (!isFrontalPose) {
                    mWarning.setVisibility(View.VISIBLE);
                    mWarning.setText(R.string.look_straight_at_the_camer);
                    mFaceInGoodState = false;
                } else if (fastMove) {
                    mWarning.setVisibility(View.VISIBLE);
                    mWarning.setText(R.string.moving_too_fast);
                    mFaceInGoodState = false;
                } else {
                    mWarning.setVisibility(View.INVISIBLE);
                    final int[] qualityStatesStringsResId = new int[]{R.string.overdark,
                            R.string.overlight, R.string.overgray, R.string.overblur};
                }

            } else {
                mWarning.setVisibility(showDetectionPreference ? View.INVISIBLE : View.VISIBLE);
                Log.i("OVAL", "PUT YOUR HEAD INTO OVAL INNER");
                mWarning.setText(R.string.put_your_head_into_oval);
            }

        } else {
            Log.i("OVAL", "PUT YOUR HEAD INTO OVAL OUT");
            mWarning.setVisibility(showDetectionPreference ? View.INVISIBLE : View.VISIBLE);
            mWarning.setText(R.string.put_your_head_into_oval);
            showDetectionRect = false;
        }
        Log.i("RECT", "SHODETECTIONRECT " + showDetectionRect);
        mFaceBoundSurfaceView.setVisibility(showDetectionRect ? View.VISIBLE : View.INVISIBLE);

        if (showDetectionRect) {
            mFaceBoundSurfaceView.setFaceRectColor(mFaceInGoodState ? Color.GREEN : Color.RED);
            mFaceBoundSurfaceView.invalidate();
        }
    }


    @Override
    public void onBestFrameReady() {
        if (mFaceInGoodState && mListener != null) {
            mIgnoreTimeout = true;
            // Check if we are in auth mode
            if (mCheckLiveness) {
                if (mVisionLabsPreferences.getEyesAuth()) {
                    mPhotoProcessor.setEyeLiveness();
                    mPhotoProcessor.startCheckLiveness();
                } else if (mVisionLabsPreferences.getZoomAuth()) {
                    mPhotoProcessor.setZoomLiveness();
                    mPhotoProcessor.startCheckLivenessZoom();
                }
                mLivenessStart = System.currentTimeMillis();
            } else {
                submitBestShot();
            }
        } else {
            mPhotoProcessor.resumeSearch();
        }
    }

    @Override
    public void onLivenessSucceed() {
        submitBestShot();
    }

    @Override
    public void onLivenessWaitingOpenedEyes() {
        mListener.onLivenessWaitingOpenedEyes();
    }

    private void submitBestShot() {
        mLivenessActionText.setText("");
        mListener.onBestFrameReady(mPhotoProcessor.getBestShot());
    }

    @Override
    public void onLuminanceState(PhotoProcessor.LuminanceState state) {
        if (!cameraFlashIsSupported) {
            return;
        }

        final String flashModeNew = state.darknessState == 0 ? Camera.Parameters.FLASH_MODE_TORCH
                : Camera.Parameters.FLASH_MODE_OFF;
        Camera.Parameters parameters = mCamera.getParameters();

        if (!flashModeNew.equals(parameters.getFlashMode())) {
            parameters.setFlashMode(flashModeNew);
            mCamera.setParameters(parameters);

            mPhotoProcessor.setFlashTorchState(state.darknessState == 0 ? true : false);
        }
    }

    private Camera.Size getBestPreviewSize() {
        final List<Camera.Size> supportedPreviewSizes = mCamera.getParameters()
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
        final Camera.Parameters parameters = mCamera.getParameters();
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
            mPhotoProcessor.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
            parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
            mCamera.setParameters(parameters);
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
        if (mCamera != null) {
            mCamera.stopPreview();
            setCameraDisplayOrientation(getCameraId());
            mCamera.addCallbackBuffer(mPhotoProcessor.getCallbackBuffer());
            mCamera.setPreviewCallbackWithBuffer(PhotoFragment.this);

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //  Log.i("TEST"," QUITTING ON START_CAMERA_PREVIEW");
    }

    private int getToggledCameraFacing() {
        return mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
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
        if (mCamera != null) {

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

            mCamera.stopPreview();
            mCamera.release();

            mCameraId = newCameraID;
            mCameraFacing = getToggledCameraFacing();

            mCamera = Camera.open(getCameraId());

            configureCamera();

            setCameraDisplayOrientation(getCameraId());
            mCamera.addCallbackBuffer(mPhotoProcessor.getCallbackBuffer());
            mCamera.setPreviewCallbackWithBuffer(PhotoFragment.this);

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mCamera != null) {
                try {
                    mCamera.setPreviewDisplay(holder);
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
        restartCameraPreview(mPreview.getHolder());
    }
}
