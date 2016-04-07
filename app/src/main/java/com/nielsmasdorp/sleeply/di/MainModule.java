package com.nielsmasdorp.sleeply.di;

import com.bumptech.glide.RequestManager;
import com.nielsmasdorp.sleeply.di.AppModule;
import com.nielsmasdorp.sleeply.interactor.MainInteractor;
import com.nielsmasdorp.sleeply.ui.stream.MainPresenter;
import com.nielsmasdorp.sleeply.ui.stream.MainPresenterImpl;
import com.nielsmasdorp.sleeply.ui.stream.MainView;
import com.nielsmasdorp.sleeply.ui.stream.StreamGridAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module
public class MainModule {

    private MainView view;

    public MainModule(MainView view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public MainPresenter provideMainPresenter(MainInteractor interactor) {

        return new MainPresenterImpl(view, interactor);
    }

    @Provides
    @Singleton
    public StreamGridAdapter provideStreamGridAdapter(RequestManager glide) {

        return new StreamGridAdapter(glide);
    }
}
