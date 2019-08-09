package ru.vasiliev.sandbox.mvp;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;

import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MvpBasePresenter<View extends MvpView> extends MvpPresenter<View> {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected void addSubscription(@NonNull Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}