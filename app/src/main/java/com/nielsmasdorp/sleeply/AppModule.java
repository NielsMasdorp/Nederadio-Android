package com.nielsmasdorp.sleeply;

import android.app.Application;

import com.nielsmasdorp.sleeply.interactor.InteractorModule;
import com.nielsmasdorp.sleeply.storage.StorageModule;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module(
        injects = SleeplyApplication.class,
        includes = {StorageModule.class, InteractorModule.class}
)
public class AppModule {

    private SleeplyApplication app;

    public AppModule(SleeplyApplication app) {
        this.app = app;
    }

    @Provides
    public Application provideApplicationContext() {
        return app;
    }
}
