package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
interface VisionLabsView extends MvpView {

    void showLoader();

    void onEngineLoadFinished(boolean result);

    void onEngineLoadError(Throwable t);
}