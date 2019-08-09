package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.InjectViewState;

import android.content.Context;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.vasiliev.sandbox.mvp.MvpBasePresenter;
import ru.visionlab.Resources;
import ru.visionlab.faceengine.FaceEngineJNI;
import timber.log.Timber;

@InjectViewState
public class VisionLabsPresenter extends MvpBasePresenter<VisionLabsView> {

    private Context mContext;

    VisionLabsPresenter(Context context) {
        mContext = context;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showLoader();
        addSubscription(Observable.fromCallable(() -> unpackResourcesAndInitFaceEngine(mContext))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> getViewState().onEngineLoadFinished(result),
                        throwable -> getViewState().onEngineLoadError(throwable)));
    }

    private boolean unpackResourcesAndInitFaceEngine(Context context) {

        if (!Resources.createVLDataFolder(context)) {
            return false;
        }

        boolean dataAssetsUnpackedSuccess = Resources
                .createFilesFromAssetFolder(context,
                        Resources.VL_DATA_PACK);

        Timber.d("ASSETS UNPACKED: " + dataAssetsUnpackedSuccess);

        if (!dataAssetsUnpackedSuccess) {
            Timber.d("COULDN'T UNPACK RESOURCES FROM ASSETS");
            return false;
        }

        if (!FaceEngineJNI.initFaceEngine(context.getFilesDir() + "/vl/data")) {
            Timber.d("COULDN'T INIT FACE ENGINE BY PATH: " + context.getFilesDir() + "/vl/data");
            return false;
        } else {
            Timber.d("SUCCESSFULLY INITED FACE ENGINE FROM PATH: " + context.getFilesDir()
                    + "/vl/data");
        }

        return true;
    }
}
