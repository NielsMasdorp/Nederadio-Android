package com.nielsmasdorp.sleeply.interactor;

import android.app.Application;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module(
        library = true,
        complete = false
)
public class InteractorModule {

    @Provides
    @Singleton
    public MainInteractor provideMainInteractor(Application application, SharedPreferences preferences) {

        return new MainInteractorImpl(application, preferences);
    }
}
