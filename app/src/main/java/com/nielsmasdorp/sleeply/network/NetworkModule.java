package com.nielsmasdorp.sleeply.network;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.nielsmasdorp.sleeply.ui.stream.StreamGridAdapter;

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

    @Provides
    @Singleton
    public RequestManager provideGlideRequestManager(Application application) {

        return Glide.with(application);
    }
}
