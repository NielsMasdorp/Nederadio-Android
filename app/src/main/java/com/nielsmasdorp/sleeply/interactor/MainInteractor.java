package com.nielsmasdorp.sleeply.interactor;


import com.nielsmasdorp.sleeply.model.Stream;
import com.nielsmasdorp.sleeply.ui.stream.OnStreamServiceListener;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public interface MainInteractor {

    void startService(OnStreamServiceListener listener);

    void unbindService();

    void playStream();

    void nextStream();

    void previousStream();

    void setSleepTimer(int ms);

    void getAllStreams();

    void streamPicked(Stream stream);
}
