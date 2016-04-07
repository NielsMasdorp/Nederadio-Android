package com.nielsmasdorp.sleeply.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.nielsmasdorp.sleeply.interactor.MainInteractor;
import com.nielsmasdorp.sleeply.interactor.MainInteractorImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module
public class InteractorModule {

    @Provides
    @Singleton
    public MainInteractor provideMainInteractor(Application application, SharedPreferences preferences, ConnectivityManager connectivityManager) {

        return new MainInteractorImpl(application, preferences, connectivityManager);
    }
}
