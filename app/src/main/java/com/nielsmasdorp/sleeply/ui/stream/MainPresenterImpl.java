package com.nielsmasdorp.sleeply.ui.stream;

import com.nielsmasdorp.sleeply.interactor.MainInteractor;
import com.nielsmasdorp.sleeply.model.Stream;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainPresenterImpl implements MainPresenter, OnStreamServiceListener {

    private MainView view;
    private MainInteractor interactor;

    public MainPresenterImpl(MainView view, MainInteractor interactor) {

        this.view = view;
        this.interactor = interactor;
    }

    @Override
    public void startService() {

        interactor.startService(this);
    }

    @Override
    public void unBindService() {

        interactor.unbindService();
    }

    @Override
    public void playStream() {

        interactor.playStream();
    }

    @Override
    public void nextStream() {

        interactor.nextStream();
    }

    @Override
    public void previousStream() {

        interactor.previousStream();
    }

    @Override
    public void setSleepTimer(int ms) {

        interactor.setSleepTimer(ms);
    }

    @Override
    public void streamStopped() {

        view.setToStopped();
    }

    @Override
    public void updateTimerValue(String timeLeft) {

        view.updateTimer(timeLeft);
    }

    @Override
    public void restoreUI(Stream stream, boolean isPlaying) {

        view.initializeUI(stream, isPlaying);
    }

    @Override
    public void setLoading() {

        view.setLoading();
    }

    @Override
    public void streamPlaying() {

        view.setToPlaying();
    }

    @Override
    public void animateTo(Stream currentStream) {

        view.animateTo(currentStream);
    }

    @Override
    public void error(String error) {

        view.error(error);
    }
}
