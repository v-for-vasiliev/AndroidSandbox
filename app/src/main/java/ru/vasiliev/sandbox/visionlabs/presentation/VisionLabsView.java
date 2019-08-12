package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
interface VisionLabsView extends MvpView {

    void requestPermissions();

    void showLoader();

    void hideLoader();

    void showRegistration();

    void showAuth();

    void onEngineLoadError();
}