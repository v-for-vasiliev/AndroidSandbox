package ru.vasiliev.sandbox.di.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import proxypref.ProxyPreferences;
import ru.vasiliev.sandbox.di.scopes.VisionLabsScope;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractor;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractorImpl;
import ru.vasiliev.sandbox.visionlabs.repository.VisionLabsPreferences;
import ru.visionlab.Resources;
import ru.visionlab.faceengine.PhotoProcessor;

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
        return ProxyPreferences
                .build(VisionLabsPreferences.class, context.getSharedPreferences("preferences", 0));
    }
}
