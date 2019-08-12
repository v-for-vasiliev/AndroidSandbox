package ru.vasiliev.sandbox.di.modules;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import ru.vasiliev.sandbox.di.scopes.ActivityScope;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractor;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsInteractorImpl;
import ru.visionlab.Resources;
import ru.visionlab.faceengine.PhotoProcessor;

@Module
public class VisionLabsModule {

    @ActivityScope
    @Provides
    VisionLabsInteractor provideInteractor() {
        return new VisionLabsInteractorImpl();
    }

    @ActivityScope
    @Provides
    PhotoProcessor providePhotoProcessor(Context context) {
        return new PhotoProcessor.Builder()
                .pathToData(context.getFilesDir() + Resources.PATH_TO_EXTRACTED_VL_DATA)
                .build(context);
    }
}
