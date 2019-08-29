package ru.vasiliev.sandbox.sovestoauth.presentation;

import com.arellomobile.mvp.InjectViewState;

import javax.inject.Inject;

import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.app.mvp.MvpBasePresenter;
import ru.vasiliev.sandbox.sovestoauth.domain.OAuthInteractor;

@InjectViewState
public class OAuthPresenter extends MvpBasePresenter<OAuthView> {

    @Inject
    OAuthInteractor mInteractor;

    private boolean mSmsCodeRequested = false;

    public OAuthPresenter() {
        App.getComponentManager().getOAuthComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean isSmsCodeRequested() {
        return mSmsCodeRequested;
    }
}
