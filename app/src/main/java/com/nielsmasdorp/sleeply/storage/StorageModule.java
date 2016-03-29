package com.nielsmasdorp.sleeply.storage;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module(
        complete = false,
        library = true
)
public class StorageModule {

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences(Application application) {

        return PreferenceManager.getDefaultSharedPreferences(application);
    }
}
