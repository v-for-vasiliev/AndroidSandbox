package ru.vasiliev.sandbox.network.presentation;

import com.arellomobile.mvp.InjectViewState;

import javax.inject.Inject;

import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.app.mvp.MvpBasePresenter;
import ru.vasiliev.sandbox.app.utils.RxUtils;
import ru.vasiliev.sandbox.network.domain.OAuthInteractor;

@InjectViewState
public class OAuthPresenter extends MvpBasePresenter<OAuthView> {

    @Inject
    OAuthInteractor mInteractor;

    private boolean mSmsCodeRequested = false;

    public OAuthPresenter() {
        App.getComponentManager().getNetworkComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().requestPermissions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void requestSmsCode(String phone) {
        addSubscription(mInteractor.getSms(phone).compose(RxUtils.applySchedulers())
                .subscribe(oAuthResponse -> getViewState().onSmsRequestedSuccessfully(),
                        Throwable::printStackTrace));
    }

    void submitSmsCode(String code) {
        addSubscription(mInteractor.submitSms(code).compose(RxUtils.applySchedulers())
                .subscribe(oAuthResponse -> {
                }));
    }

    boolean isSmsCodeRequested() {
        return mSmsCodeRequested;
    }
}
