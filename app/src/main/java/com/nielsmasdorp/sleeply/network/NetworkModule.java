package com.nielsmasdorp.sleeply.network;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

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
public class NetworkModule {

    @Provides
    @Singleton
    public ConnectivityManager provideConnectivityManager(Application application) {

        return (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
