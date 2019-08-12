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

@InjectViewState
public class VisionLabsPresenter extends MvpBasePresenter<VisionLabsView> {

    @Inject
    VisionLabsInteractor mVisionLabsInteractor;

    private Context mContext;

    VisionLabsPresenter(Context context) {
        mContext = context;
        App.getComponentManager().getVisionLabsComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showLoader();
        addSubscription(Observable.fromCallable(() -> mVisionLabsInteractor.loadLibraries())
                .filter(result -> {
                    if (!result) {
                        getViewState().onEngineLoadError(
                                new RuntimeException("Error load native libraries!"));
                    }
                    return result;
                }).map(result -> mVisionLabsInteractor.unpackResourcesAndInitEngine(mContext))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> getViewState().onEngineLoadFinished(result),
                        throwable -> getViewState().onEngineLoadError(throwable)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getComponentManager().releaseVisionLabsComponent();
    }
}
