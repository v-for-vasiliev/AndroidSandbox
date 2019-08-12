package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.InjectViewState;

import android.content.Context;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.mvp.MvpBasePresenter;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractor;
import ru.vasiliev.sandbox.visionlabs.repository.VisionLabsPreferences;

@InjectViewState
public class VisionLabsPresenter extends MvpBasePresenter<VisionLabsView> {

    @Inject
    VisionLabsInteractor mVisionLabsInteractor;

    @Inject
    VisionLabsPreferences mPreferences;

    private boolean mEngineLoaded = false;

    private Context mContext;

    VisionLabsPresenter(Context context) {
        mContext = context;
        App.getComponentManager().getVisionLabsComponent().inject(this);
    }

    @Override
    public void attachView(VisionLabsView view) {
        super.attachView(view);
        getViewState().requestPermissions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getComponentManager().releaseVisionLabsComponent();
    }

    void loadEngine() {
        getViewState().showLoader();
        addSubscription(Observable.fromCallable(() -> mVisionLabsInteractor.loadLibraries())
                .filter(result -> {
                    if (!result) {
                        mEngineLoaded = false;
                        getViewState().onEngineLoadError(
                                new RuntimeException("Error load native libraries!"));
                    }
                    return result;
                }).map(result -> mVisionLabsInteractor.unpackResourcesAndInitEngine(mContext))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mEngineLoaded = result;
                    getViewState().onEngineLoadFinished(mEngineLoaded);
                }, throwable -> {
                    mEngineLoaded = false;
                    getViewState().onEngineLoadError(throwable);
                }));
    }

    VisionLabsPreferences getPreferences() {
        return mPreferences;
    }

    public boolean isEngineLoaded() {
        return mEngineLoaded;
    }
}
