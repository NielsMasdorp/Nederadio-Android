package com.nielsmasdorp.sleeply.ui.stream;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public interface MainPresenter {

    void startService();

    void unBindService();

    void playStream();

    void nextStream();

    void previousStream();

    void setSleepTimer(int ms);
}
