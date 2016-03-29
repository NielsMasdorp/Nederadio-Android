package com.nielsmasdorp.sleeply.ui.stream;

import com.nielsmasdorp.sleeply.model.Stream;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public interface MainView {

    void initializeUI(Stream stream, boolean isPlaying);

    void setLoading();

    void setToStopped();

    void setToPlaying();

    void animateTo(Stream currentStream);

    void updateTimer(Long timeLeft);

    void error(String error);
}
