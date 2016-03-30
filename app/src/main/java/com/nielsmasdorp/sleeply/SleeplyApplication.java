package com.nielsmasdorp.sleeply;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class SleeplyApplication extends Application {

    @Inject
    Application application;

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        objectGraph = ObjectGraph.create(getModules().toArray());
        objectGraph.inject(this);
    }

    public Application getApplication() {
        return application;
    }

    private List<Object> getModules() {
        return Arrays.asList(new AppModule(this));
    }

    public ObjectGraph createScopedGraph(Object... modules) {
        return objectGraph.plus(modules);
    }
}