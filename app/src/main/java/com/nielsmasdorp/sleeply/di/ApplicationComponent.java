package com.nielsmasdorp.sleeply.di;

import com.nielsmasdorp.sleeply.ui.stream.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Singleton
@Component(modules = { AppModule.class, StorageModule.class, NetworkModule.class, InteractorModule.class, MainModule.class })
public interface ApplicationComponent {

    void inject(MainActivity mainActivity);
}
