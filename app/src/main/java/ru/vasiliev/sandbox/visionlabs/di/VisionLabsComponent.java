package ru.vasiliev.sandbox.visionlabs.di;

import dagger.Subcomponent;
import ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsPresenter;
import ru.vasiliev.sandbox.visionlabs.presentation.common.PhotoFragment;

/**
 * Date: 12.08.2019
 *
 * @author Kirill Vasiliev
 */
@VisionLabsScope
@Subcomponent(modules = {VisionLabsModule.class})
public interface VisionLabsComponent {

    void inject(VisionLabsPresenter presenter);

    void inject(PhotoFragment fragment);
}
