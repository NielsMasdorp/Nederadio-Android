package com.nielsmasdorp.sleeply.interactor;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nielsmasdorp.sleeply.R;
import com.nielsmasdorp.sleeply.model.Stream;
import com.nielsmasdorp.sleeply.service.StreamService;
import com.nielsmasdorp.sleeply.ui.stream.OnStreamServiceListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainInteractorImpl implements MainInteractor {

    private final static String TAG = MainInteractorImpl.class.getSimpleName();
    private final static String LAST_STREAM_IDENTIFIER = "last_stream_identifier";

    private Application application;
    private SharedPreferences preferences;
    private ConnectivityManager connectivityManager;

    private StreamService streamService;
    private OnStreamServiceListener presenter;

    private Boolean boundToService = false;

    private List<Stream> streams;
    private Stream currentStream;

    public MainInteractorImpl(Application application, SharedPreferences preferences, ConnectivityManager connectivityManager) {

        this.application = application;
        this.preferences = preferences;
        this.connectivityManager = connectivityManager;

        streams = new ArrayList<>();
        streams.add(new Stream(0, "https://api.soundcloud.com/tracks/110697958/stream", application.getString(R.string.rainy_stream_title), application.getString(R.string.rainy_stream_desc), R.drawable.rain_background));
        streams.add(new Stream(1, "https://api.soundcloud.com/tracks/13262271/stream", application.getString(R.string.ocean_stream_title), application.getString(R.string.ocean_stream_desc), R.drawable.ocean_background));
        streams.add(new Stream(2, "https://api.soundcloud.com/tracks/97924982/stream", application.getString(R.string.forest_stream_title), application.getString(R.string.forest_stream_desc), R.drawable.nature_background));
        streams.add(new Stream(3, "https://api.soundcloud.com/tracks/149844883/stream", application.getString(R.string.meditation_stream_title), application.getString(R.string.meditation_stream_desc), R.drawable.meditation_background));
        streams.add(new Stream(4, "https://api.soundcloud.com/tracks/78048378/stream", application.getString(R.string.delta_waves_stream_title), application.getString(R.string.delta_waves_stream_desc), R.drawable.delta_waves_background));
        streams.add(new Stream(5, "https://api.soundcloud.com/tracks/210719226/stream", application.getString(R.string.lucid_stream_title), application.getString(R.string.lucid_stream_desc), R.drawable.lucid_background));
        streams.add(new Stream(6, "https://api.soundcloud.com/tracks/176629663/stream", application.getString(R.string.autumn_stream_title), application.getString(R.string.autumn_stream_desc), R.drawable.autumn_background));
        streams.add(new Stream(7, "https://api.soundcloud.com/tracks/189015106/stream", application.getString(R.string.void_stream_title), application.getString(R.string.void_stream_desc), R.drawable.void_background));
    }

    @Override
    public void startService(OnStreamServiceListener presenter) {

        this.presenter = presenter;

        Intent intent = new Intent(application, StreamService.class);
        if (!isServiceAlreadyRunning()) {
            Log.i(TAG, "onStart: service not running, starting service.");
            application.startService(intent);
        }
        Log.i(TAG, "onStart: binding to service.");
        application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {

        Log.i(TAG, "onStart: registering broadcast receiver.");
        IntentFilter broadcastIntentFilter = new IntentFilter();
        broadcastIntentFilter.addAction(StreamService.STREAM_DONE_LOADING_INTENT);
        broadcastIntentFilter.addAction(StreamService.TIMER_DONE_INTENT);
        broadcastIntentFilter.addAction(StreamService.TIMER_UPDATE_INTENT);
        LocalBroadcastManager.getInstance(application).registerReceiver((broadcastReceiver), broadcastIntentFilter);
    }

    @Override
    public void unbindService() {

        if (boundToService) {
            application.unbindService(serviceConnection);
        }
        LocalBroadcastManager.getInstance(application).unregisterReceiver(broadcastReceiver);

        preferences.edit().putInt(LAST_STREAM_IDENTIFIER, currentStream.getId()).apply();
    }

    @Override
    public void playStream() {

        if (!streamService.isPlaying()) {
            streamService.playStream(currentStream);
            presenter.setLoading();
            checkIfOnWifi();
        } else {
            streamService.stopStreaming(true);
            presenter.streamStopped();
        }
    }

    @Override
    public void nextStream() {

        int currentStreamId = currentStream.getId();
        if (currentStreamId != (streams.size() - 1)) {
            currentStream = streams.get(currentStreamId + 1);
        } else {
            currentStream = streams.get(0);
        }

        if (streamService.isPlaying()) {
            streamService.stopStreaming(false);
            playStream();
        }

        presenter.animateTo(currentStream);
    }

    @Override
    public void previousStream() {

        int currentStreamId = currentStream.getId();
        if (currentStreamId != 0) {
            currentStream = streams.get(currentStreamId - 1);
        } else {
            currentStream = streams.get(streams.size() - 1);
        }

        if (streamService.isPlaying()) {
            streamService.stopStreaming(false);
            playStream();
        }

        presenter.animateTo(currentStream);
    }

    @Override
    public void setSleepTimer(int option) {

        if (streamService.isPlaying()) {
            streamService.setSleepTimer(calculateMs(option));
        } else {
            presenter.error(application.getString(R.string.start_stream_error_toast));
        }
    }

    /**
     * See if the StreamService is already running in the background.
     *
     * @return boolean indicating if the service runs
     */
    private boolean isServiceAlreadyRunning() {
        ActivityManager manager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StreamService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the intents the broadcast receiver receives
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {

        if (intent.getAction().equals(StreamService.STREAM_DONE_LOADING_INTENT)) {
            boolean success = intent.getBooleanExtra(StreamService.STREAM_DONE_LOADING_SUCCESS, false);
            if (!success) {
                presenter.streamStopped();
            } else {
                presenter.streamPlaying();
            }
        } else if (intent.getAction().equals(StreamService.TIMER_DONE_INTENT)) {
            presenter.streamStopped();
        } else if (intent.getAction().equals(StreamService.TIMER_UPDATE_INTENT)) {
            long timerValue = (long) intent.getIntExtra(StreamService.TIMER_UPDATE_VALUE, 0);
            presenter.updateTimerValue(formatTimer(timerValue));
        }
    }

    private String formatTimer(long timeLeft) {

        if (timeLeft > TimeUnit.HOURS.toMillis(1)) {
            return String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(timeLeft),
                    TimeUnit.MILLISECONDS.toMinutes(timeLeft) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)),
                    TimeUnit.MILLISECONDS.toSeconds(timeLeft) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft)));
        } else {
            return String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(timeLeft) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)),
                    TimeUnit.MILLISECONDS.toSeconds(timeLeft) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft)));
        }
    }

    private int calculateMs(int option) {
        switch (option) {
            case 0:
                return 0;
            case 1:
                return (int) TimeUnit.SECONDS.toMillis(15);
            case 2:
                return (int) TimeUnit.MINUTES.toMillis(20);
            case 3:
                return (int) TimeUnit.MINUTES.toMillis(30);
            case 4:
                return (int) TimeUnit.MINUTES.toMillis(40);
            case 5:
                return (int) TimeUnit.MINUTES.toMillis(50);
            case 6:
                return (int) TimeUnit.HOURS.toMillis(1);
            case 7:
                return (int) TimeUnit.HOURS.toMillis(2);
            case 8:
                return (int) TimeUnit.HOURS.toMillis(3);
            default:
                return 0;
        }
    }

    private void checkIfOnWifi() {

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI) {
                presenter.error(application.getString(R.string.no_wifi_toast));
            }
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "onServiceConnected: successfully bound to service.");
            StreamService.StreamBinder binder = (StreamService.StreamBinder) service;
            streamService = binder.getService();
            boundToService = true;
            currentStream = streamService.getPlayingStream();
            if (currentStream != null) {
                presenter.restoreUI(currentStream, true);
            } else {
                int last = preferences.getInt(LAST_STREAM_IDENTIFIER, 0);
                currentStream = streams.get(last);
                presenter.restoreUI(currentStream, false);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "onServiceDisconnected: disconnected from service.");
            boundToService = false;
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            handleIntent(intent);
        }
    };
}
