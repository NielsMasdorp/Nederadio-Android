package com.nielsmasdorp.sleeply.ui.stream;

import com.nielsmasdorp.sleeply.AppModule;
import com.nielsmasdorp.sleeply.interactor.MainInteractor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module(
        injects = MainActivity.class,
        addsTo = AppModule.class
)
public class MainModule {

    private MainView view;

    public MainModule(MainView view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public MainPresenter provideMainPresenter(MainView view, MainInteractor interactor) {

        return new MainPresenterImpl(view, interactor);
    }

    @Provides
    @Singleton
    public MainView provideMainView() {

        return view;
    }
}
