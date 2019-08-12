package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.InjectViewState;

import android.content.Context;
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
import ru.visionlab.faceengine.PhotoProcessor;

@InjectViewState
public class VisionLabsPresenter extends MvpBasePresenter<VisionLabsView> {

    enum Mode {
        AUTH, REGISTRATION
    }

    @Inject
    VisionLabsInteractor mVisionLabsInteractor;

    @Inject
    PhotoProcessor mPhotoProcessor;

    @Inject
    VisionLabsPreferences mPreferences;

    @Inject
    VisionLabsRegistrationApi mRegistrationApi;

    @Inject
    VisionLabsVerifyApi mVerifyApi;

    private boolean mEngineLoaded = false;

    private Context mContext;

    VisionLabsPresenter(Context context) {
        mContext = context;
        App.getComponentManager().getVisionLabsComponent().inject(this);
    }

    @Override
    public void attachView(VisionLabsView view) {
        super.attachView(view);
        if (mEngineLoaded) {
            onEngineLoadedSucceeded();
        } else {
            getViewState().requestPermissions();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getComponentManager().releaseVisionLabsComponent();
    }

    boolean isEngineLoaded() {
        return mEngineLoaded;
    }

    Mode getMode() {
        return TextUtils.isEmpty(mPreferences.getAuthDescriptor()) ? Mode.REGISTRATION : Mode.AUTH;
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
                .subscribe(result -> {
                    mEngineLoaded = result;
                    onEngineLoadedSucceeded();
                }, throwable -> {
                    mEngineLoaded = false;
                    getViewState().onEngineLoadError();
                }));
    }

    void onEngineLoadedSucceeded() {
        if (TextUtils.isEmpty(mPreferences.getAuthDescriptor())) {
            getViewState().showRegistration();
        } else {
            getViewState().showAuth();
        }
    }

    PhotoProcessor getPhotoProcessor() {
        return mPhotoProcessor;
    }

    VisionLabsPreferences getPreferences() {
        return mPreferences;
    }

    public VisionLabsRegistrationApi getRegistrationApi() {
        return mRegistrationApi;
    }

    public VisionLabsVerifyApi getVerifyApi() {
        return mVerifyApi;
    }
}
