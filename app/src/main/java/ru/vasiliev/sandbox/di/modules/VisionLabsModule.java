package ru.vasiliev.sandbox.di.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import proxypref.ProxyPreferences;
import ru.vasiliev.sandbox.di.scopes.VisionLabsScope;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsPreferences;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsRegistrationApi;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsRegistrationApiLocalImpl;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsVerifyApi;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsVerifyApiLocalImpl;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractor;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractorImpl;
import ru.visionlab.Resources;
import ru.visionlab.faceengine.PhotoProcessor;

import static ru.vasiliev.sandbox.visionlabs.domain.VisionLabsConfig.PREFERENCES_FILE_NAME;

@Module
public class VisionLabsModule {

    @VisionLabsScope
    @Provides
    VisionLabsInteractor provideInteractor() {
        return new VisionLabsInteractorImpl();
    }

    @VisionLabsScope
    @Provides
    PhotoProcessor providePhotoProcessor(Context context) {
        return new PhotoProcessor.Builder()
                .pathToData(context.getFilesDir() + Resources.PATH_TO_EXTRACTED_VL_DATA)
                .build(context);
    }

    @VisionLabsScope
    @Provides
    VisionLabsPreferences providePreferences(Context context) {
        return ProxyPreferences.build(VisionLabsPreferences.class,
                context.getSharedPreferences(PREFERENCES_FILE_NAME, 0));
    }

    @VisionLabsScope
    @Provides
    VisionLabsVerifyApi provideVerifyApi(PhotoProcessor photoProcessor,
            VisionLabsPreferences preferences) {
        return new VisionLabsVerifyApiLocalImpl(photoProcessor, preferences);
    }

    @VisionLabsScope
    @Provides
    VisionLabsRegistrationApi provideRegistrationApi(PhotoProcessor photoProcessor,
            VisionLabsPreferences preferences) {
        return new VisionLabsRegistrationApiLocalImpl(photoProcessor, preferences);
    }
}
