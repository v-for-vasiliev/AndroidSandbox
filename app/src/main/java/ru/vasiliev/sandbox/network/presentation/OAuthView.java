package ru.vasiliev.sandbox.network.presentation;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface OAuthView extends MvpView {

    void requestPermissions();

    void onSmsRequestedSuccessfully();
}
