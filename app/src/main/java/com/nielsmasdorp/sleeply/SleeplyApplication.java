package com.nielsmasdorp.sleeply;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.nielsmasdorp.sleeply.di.AppModule;
import com.nielsmasdorp.sleeply.di.ApplicationComponent;
import com.nielsmasdorp.sleeply.di.DaggerApplicationComponent;
import com.nielsmasdorp.sleeply.di.InteractorModule;
import com.nielsmasdorp.sleeply.di.MainModule;
import com.nielsmasdorp.sleeply.di.NetworkModule;
import com.nielsmasdorp.sleeply.di.StorageModule;
import com.nielsmasdorp.sleeply.ui.stream.MainView;

import io.fabric.sdk.android.Fabric;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class SleeplyApplication extends Application {

    ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
    }

    public ApplicationComponent provideApplicationComponent(MainView view) {

        applicationComponent = DaggerApplicationComponent.builder()
                .appModule(new AppModule(this))
                .storageModule(new StorageModule())
                .networkModule(new NetworkModule())
                .interactorModule(new InteractorModule())
                .mainModule(new MainModule(view))
                .build();

        return applicationComponent;
    }
}