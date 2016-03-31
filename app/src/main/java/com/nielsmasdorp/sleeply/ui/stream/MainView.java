package com.nielsmasdorp.sleeply.ui.stream;

import com.nielsmasdorp.sleeply.model.Stream;

import java.util.List;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public interface MainView {

    void initializeUI(Stream stream, boolean isPlaying);

    void setLoading();

    void setToStopped();

    void setToPlaying();

    void animateTo(Stream currentStream);

    void updateTimer(String timeLeft);

    void error(String error);

    void showStreamsDialog(List<Stream> streams, Stream currentStream);
}
