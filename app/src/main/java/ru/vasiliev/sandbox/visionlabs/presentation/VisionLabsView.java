package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import ru.vasiliev.sandbox.visionlabs.presentation.registration.FaceNotFoundFragment;

@StateStrategyType(AddToEndSingleStrategy.class)
interface VisionLabsView extends MvpView {

    void requestPermissions();

    void onEngineLoadError();

    void showLoader();

    void hideLoader();

    void showRegistration();

    void showAuth();

    void showPreview();

    void showFaceNotFound(FaceNotFoundFragment.Reason reason);

    void showFaceNotFoundWarn();
}