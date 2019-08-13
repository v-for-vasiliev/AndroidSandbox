package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.InjectViewState;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.mvp.MvpBasePresenter;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsPreferences;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsRegistrationApi;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsVerifyApi;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractor;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.FaceNotFoundFragment;
import ru.visionlab.Resources;
import ru.visionlab.faceengine.PhotoProcessor;

import static ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsPresenter.Mode.AUTH;
import static ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsPresenter.Mode.REGISTRATION;

@InjectViewState
public class VisionLabsPresenter extends MvpBasePresenter<VisionLabsView> {

    enum Mode {
        AUTH, REGISTRATION
    }

    @Inject
    VisionLabsInteractor mVisionLabsInteractor;

    @Inject
    VisionLabsPreferences mPreferences;

    @Inject
    PhotoProcessor mPhotoProcessor;

    @Inject
    VisionLabsRegistrationApi mRegistrationApi;

    @Inject
    VisionLabsVerifyApi mVerifyApi;

    private boolean mEngineLoaded = false;

    private Context mContext;

    private Bitmap mFrame;

    private long mVerificationStartTime;

    private long mVerificationEndTime;

    private int mFaceAuthFailsCount;

    VisionLabsPresenter(Context context) {
        mContext = context;
        App.getComponentManager().getVisionLabsComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPhotoProcessor.release();
    }

    void releaseComponent() {
        App.getComponentManager().releaseVisionLabsComponent();
    }

    void init() {
        getViewState().requestPermissions();
    }

    boolean isEngineLoaded() {
        return mEngineLoaded;
    }

    void loadEngine() {
        getViewState().showLoader();
        addSubscription(Observable.fromCallable(() -> mVisionLabsInteractor.loadLibraries())
                .filter(result -> {
                    if (!result) {
                        mEngineLoaded = false;
                        getViewState().onEngineLoadError();
                    }
                    return result;
                }).map(result -> mVisionLabsInteractor.unpackResourcesAndInitEngine(mContext))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(engineLoaded -> {
                    mEngineLoaded = engineLoaded;
                    if (mEngineLoaded) {
                        resolveFlow();
                    } else {
                        getViewState().onEngineLoadError();
                    }
                }, throwable -> {
                    mEngineLoaded = false;
                    getViewState().onEngineLoadError();
                }));
    }

    private void resolveFlow() {
        if (TextUtils.isEmpty(mPreferences.getAuthDescriptor())) {
            getViewState().showRegistration();
        } else {
            mFaceAuthFailsCount = 0;
            getViewState().showAuth();
        }
    }

    private Mode getMode() {
        return TextUtils.isEmpty(mPreferences.getAuthDescriptor()) ? Mode.REGISTRATION : Mode.AUTH;
    }

    void onBestFrameReady(Bitmap frame) {
        if (getMode() == REGISTRATION) {
            mFrame = frame;
            getViewState().showPreview();
        } else if (getMode() == AUTH) {
            mVerificationStartTime = System.nanoTime();
            mVerifyApi.verifyPerson();
        }
    }

    void onTimeout(FaceNotFoundFragment.Reason reason) {
        if (getMode() == REGISTRATION) {
            getViewState().showFaceNotFound(reason);
        } else if (getMode() == AUTH) {
            mFaceAuthFailsCount++;
            if (mFaceAuthFailsCount >= 5) {
                getViewState().showFaceNotFound(reason);
            } else {
                getViewState().showFaceNotFoundWarn();
            }
        }
    }

    void onRetry() {
        resolveFlow();
    }

    VisionLabsPreferences getPreferences() {
        return mPreferences;
    }

    PhotoProcessor getPhotoProcessor() {
        //return mPhotoProcessor;
        return new PhotoProcessor.Builder()
                .pathToData(mContext.getFilesDir() + Resources.PATH_TO_EXTRACTED_VL_DATA)
                .build(mContext);
    }

    VisionLabsRegistrationApi getRegistrationApi() {
        return mRegistrationApi;
    }

    Bitmap getFrame() {
        return mFrame;
    }

    long getVerificationStartTime() {
        return mVerificationStartTime;
    }

    long getVerificationEndTime() {
        return mVerificationEndTime;
    }

    void setVerificationEndTime(long verificationEndTime) {
        mVerificationEndTime = verificationEndTime;
    }
}
