package ru.vasiliev.sandbox.di.components;

import dagger.Subcomponent;
import ru.vasiliev.sandbox.di.modules.VisionLabsModule;
import ru.vasiliev.sandbox.di.scopes.VisionLabsScope;
import ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsPresenter;
import ru.vasiliev.sandbox.visionlabs.presentation.common.PhotoFragment;
import ru.visionlab.faceengine.PhotoProcessor;

/**
 * Date: 12.08.2019
 *
 * @author Kirill Vasiliev
 */
@VisionLabsScope
@Subcomponent(modules = {VisionLabsModule.class})
public interface VisionLabsComponent {

    PhotoProcessor getPhotoProcessor();

    void inject(VisionLabsPresenter presenter);

    void inject(PhotoFragment fragment);
}
