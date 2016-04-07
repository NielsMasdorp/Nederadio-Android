package com.nielsmasdorp.sleeply.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module
public class StorageModule {

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences(Application application) {

        return PreferenceManager.getDefaultSharedPreferences(application);
    }
}
