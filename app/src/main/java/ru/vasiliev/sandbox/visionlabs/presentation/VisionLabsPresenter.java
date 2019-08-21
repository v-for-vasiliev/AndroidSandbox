package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.InjectViewState;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.app.mvp.MvpBasePresenter;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsPreferences;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsRegistrationApi;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsRegistrationApiLocalImpl;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsVerificationApi;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsVerificationApiLocalImpl;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsConfig;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractor;
import ru.vasiliev.sandbox.visionlabs.domain.model.AuthFailReason;
import ru.vasiliev.sandbox.visionlabs.domain.model.SearchResult;
import ru.vasiliev.sandbox.visionlabs.domain.model.SearchResultPerson;
import ru.vasiliev.sandbox.visionlabs.presentation.auth.FaceNotRecognizedFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.common.PhotoFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.FaceNotFoundFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.SavePhotoFragment;
import ru.visionlab.Resources;
import ru.visionlab.faceengine.PhotoProcessor;

import static ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsPresenter.Mode.AUTH;
import static ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsPresenter.Mode.REGISTRATION;

@InjectViewState
public class VisionLabsPresenter extends MvpBasePresenter<VisionLabsView>
        implements VisionLabsRegistrationApi.Listener, VisionLabsVerificationApi.Listener,
        PhotoFragment.Listener, SavePhotoFragment.Listener, FaceNotFoundFragment.Listener,
        FaceNotRecognizedFragment.Listener {

    private static final int MAX_AUTH_FAILED_ATTEMPTS = 3;

    enum Mode {
        AUTH, REGISTRATION
    }

    @Inject
    VisionLabsInteractor mVisionLabsInteractor;

    @Inject
    VisionLabsPreferences mPreferences;

    private Context mContext;

    private boolean mEngineLoaded = false;

    private PhotoProcessor mPhotoProcessor;

    private VisionLabsRegistrationApi mRegistrationApi;

    private VisionLabsVerificationApi mVerificationApi;

    private Bitmap mFrame;

    private boolean mFaceAuthSucceeded;

    private long mVerificationStartTime;

    private long mVerificationEndTime;

    private boolean mVerificationInProgress;

    private int mFaceAuthFailsCount;

    VisionLabsPresenter(Context context) {
        mContext = context;
        App.getComponentManager().getVisionLabsComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPhotoProcessor.release();
    }

    void releaseComponent() {
        App.getComponentManager().releaseVisionLabsComponent();
    }

    private void init() {
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
                        resolveFlowAndShow();
                    } else {
                        getViewState().onEngineLoadError();
                    }
                }, throwable -> {
                    mEngineLoaded = false;
                    getViewState().onEngineLoadError();
                }));
    }

    private void resolveFlowAndShow() {
        if (TextUtils.isEmpty(mPreferences.getAuthDescriptor())) {
            getViewState().showRegistration();
        } else {
            mFaceAuthFailsCount = 0;
            mFaceAuthSucceeded = false;
            getViewState().showAuth();
        }
    }

    private Mode getMode() {
        return TextUtils.isEmpty(mPreferences.getAuthDescriptor()) ? Mode.REGISTRATION : Mode.AUTH;
    }

    // Registration and auth callbacks

    @Override
    public void onBestFrameReady(Bitmap frame) {
        if (getMode() == REGISTRATION) {
            mFrame = frame;
            getViewState().showPreview();
        } else if (getMode() == AUTH) {
            mVerificationStartTime = System.nanoTime();
            getVerificationApi().verifyPerson();
        }
    }

    @Override
    public void onTimeout(FaceNotFoundFragment.Reason reason) {
        if (getMode() == REGISTRATION) {
            getViewState().showFaceNotFound(reason);
        } else if (getMode() == AUTH) {
            getViewState().showFaceNotFound(reason);
        }
    }

    @Override
    public void onTimeout() {

    }

    @Override
    public void onLivenessWaitingOpenedEyes() {

    }

    @Override
    public void onLivenessResult(int state, int action) {

    }

    @Override
    public void onRetryWhenFaceNotRecognized() {
        resolveFlowAndShow();
    }

    @Override
    public void onRetryWhenFaceNotFound() {
        resolveFlowAndShow();
    }

    @Override
    public void onRetryWhenPhotoAccepted() {
        resolveFlowAndShow();
    }

    @Override
    public void onRegisterUser() {
        getRegistrationApi().registerPerson();
    }


    @Override
    public void onRegistrationSucceeded() {
        getViewState().onRegistrationSucceeded();
    }

    @Override
    public void onRegistrationFailed(Throwable throwable) {
        getViewState().onRegistrationFailed(throwable);
    }

    // Verification callbacks

    @Override
    public void onVerificationSuccess(SearchResult searchResult) {
        if (!isVerificationInProgress()) {
            getViewState().onVerification();
            mVerificationInProgress = true;
            mVerificationEndTime = System.nanoTime();
            final List<SearchResultPerson> persons = searchResult.getPersons();
            if (persons != null && !persons.isEmpty()) {
                final SearchResultPerson person = persons.get(0);
                if (person.similarity > VisionLabsConfig.MIN_SIMILARITY) {
                    if (!mFaceAuthSucceeded) {
                        mFaceAuthSucceeded = true;
                        getViewState().onFaceAuthSucceeded();
                    }
                } else {
                    countFailedAuthAttempts(AuthFailReason.SIMILARITY);
                }
            } else {
                countFailedAuthAttempts(AuthFailReason.SIMILARITY);
            }
        }
    }

    private void countFailedAuthAttempts(AuthFailReason reason) {
        if (mFaceAuthFailsCount < MAX_AUTH_FAILED_ATTEMPTS) {
            mFaceAuthFailsCount++;
            getViewState().onAuthFailedAttempt();
        } else {
            mPreferences.setAuthDescriptor("");
            getViewState().onAuthMaxFailedAttemptsCountReached();
            /*getViewState().onFaceAuthFailed(reason,
                    (int) ((double) (mVerificationEndTime - mVerificationStartTime) / 1e6));*/
        }
    }

    @Override
    public void onVerificationFail(Throwable throwable) {
        if (throwable instanceof VisionLabsRegistrationApiLocalImpl.DescriptorNotExtractedException) {
            getViewState().onFaceAuthFailed(AuthFailReason.DESCRIPTOR_EXTRACTION_ERROR, 0);
        } else {
            getViewState().onFaceAuthFailed(AuthFailReason.OTHER, 0);
            throwable.printStackTrace();
        }
    }

    // Getters and setters

    VisionLabsPreferences getPreferences() {
        return mPreferences;
    }

    synchronized PhotoProcessor getPhotoProcessor() {
        if (mPhotoProcessor == null) {
            mPhotoProcessor = new PhotoProcessor.Builder()
                    .pathToData(mContext.getFilesDir() + Resources.PATH_TO_EXTRACTED_VL_DATA)
                    .build(mContext);
        }
        return mPhotoProcessor;
    }

    private synchronized VisionLabsRegistrationApi getRegistrationApi() {
        if (mRegistrationApi == null) {
            mRegistrationApi = new VisionLabsRegistrationApiLocalImpl(getPhotoProcessor(),
                    mPreferences).setListener(this);
        }
        return mRegistrationApi;
    }

    private synchronized VisionLabsVerificationApi getVerificationApi() {
        if (mVerificationApi == null) {
            mVerificationApi = new VisionLabsVerificationApiLocalImpl(getPhotoProcessor(),
                    mPreferences).setListener(this);
        }
        return mVerificationApi;
    }

    Bitmap getFrame() {
        return mFrame;
    }

    public synchronized boolean isVerificationInProgress() {
        return mVerificationInProgress;
    }

    public synchronized void setVerificationInProgress(boolean verificationInProgress) {
        mVerificationInProgress = verificationInProgress;
    }
}
