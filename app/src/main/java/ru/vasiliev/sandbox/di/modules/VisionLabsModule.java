package ru.vasiliev.sandbox.di.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import proxypref.ProxyPreferences;
import ru.vasiliev.sandbox.di.scopes.VisionLabsScope;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsPreferences;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractor;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractorImpl;

import static ru.vasiliev.sandbox.visionlabs.domain.VisionLabsConfig.PREFERENCES_FILE_NAME;

@Module
public class VisionLabsModule {

    @VisionLabsScope
    @Provides
    VisionLabsInteractor provideInteractor(VisionLabsPreferences preferences) {
        return new VisionLabsInteractorImpl(preferences);
    }

    @VisionLabsScope
    @Provides
    VisionLabsPreferences providePreferences(Context context) {
        return ProxyPreferences.build(VisionLabsPreferences.class,
                context.getSharedPreferences(PREFERENCES_FILE_NAME, 0));
    }

    /*
    // Engine must be loaded before this inject.
    @VisionLabsScope
    @Provides
    PhotoProcessor providePhotoProcessor(Context context) {
        return new PhotoProcessor.Builder()
                .pathToData(context.getFilesDir() + Resources.PATH_TO_EXTRACTED_VL_DATA)
                .build(context);
    }

    // Engine must be loaded before this inject.
    @VisionLabsScope
    @Provides
    VisionLabsVerificationApi provideVerifyApi(PhotoProcessor photoProcessor,
            VisionLabsPreferences preferences) {
        return new VisionLabsVerificationApiLocalImpl(photoProcessor, preferences);
    }

    // Engine must be loaded before this inject.
    @VisionLabsScope
    @Provides
    VisionLabsRegistrationApi provideRegistrationApi(PhotoProcessor photoProcessor,
            VisionLabsPreferences preferences) {
        return new VisionLabsRegistrationApiLocalImpl(photoProcessor, preferences);
    }
    */
}
