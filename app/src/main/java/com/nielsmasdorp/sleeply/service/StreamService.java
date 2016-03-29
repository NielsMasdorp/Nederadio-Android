package com.nielsmasdorp.sleeply.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.nielsmasdorp.sleeply.R;
import com.nielsmasdorp.sleeply.model.Stream;
import com.nielsmasdorp.sleeply.ui.stream.MainActivity;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class StreamService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = StreamService.class.getSimpleName();

    public static final int NOTIFY_ID = 1;

    public static final String STREAM_DONE_LOADING_INTENT = "stream_done_loading_intent";
    public static final String STREAM_DONE_LOADING_SUCCESS = "stream_done_loading_success";
    public static final String TIMER_UPDATE_INTENT = "timer_update_intent";
    public static final String TIMER_UPDATE_VALUE = "timer_update_value";
    public static final String TIMER_DONE_INTENT = "timer_done_intent";

    public static final String ACTION_STOP = "action_stop";

    private boolean isMediaPlayerPreparing = false;

    private final IBinder mStreamBinder = new StreamBinder();
    private MediaPlayer mPlayer;
    private Stream mCurrentStream;
    private LocalBroadcastManager mBroadcastManager;
    private CountDownTimer mCountDownTimer;

    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    public class StreamBinder extends Binder {
        public StreamService getService() {
            return StreamService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        toBackground();
        return mStreamBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        toBackground();
        super.onRebind(intent);
    }

    private void toBackground() {
        stopForeground(true);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mPlayer.isPlaying() || isMediaPlayerPreparing) {
            toForeground();
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Run the service in the foreground
     * and show a notification
     */
    private void toForeground() {

        RemoteViews notificationView = new RemoteViews(getPackageName(),
                R.layout.notification);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setShowWhen(false)
                .setContent(notificationView);

        notificationView.setImageViewResource(R.id.streamIcon, mCurrentStream.getImageResource());
        notificationView.setTextViewText(R.id.titleTxt, getString(R.string.app_name));
        notificationView.setTextViewText(R.id.descTxt, mCurrentStream.getTitle());

        Intent closeIntent = new Intent(getApplicationContext(), StreamService.class);
        closeIntent.setAction(ACTION_STOP);
        PendingIntent pendingCloseIntent = PendingIntent.getService(getApplicationContext(), 1, closeIntent, 0);

        notificationView.setOnClickPendingIntent(R.id.closeStream, pendingCloseIntent);

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        Notification notification = builder.build();
        startForeground(NOTIFY_ID, notification);
    }

    /**
     * Handle intent from notification
     *
     * @param intent intent to add pending intent to
     */
    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_STOP)) {
            Log.i(TAG, "handleIntent: stopping stream from notification");

            stopStreaming(true);
            toBackground();
            stopSelf();
        }
    }

    public boolean isPlaying() {
        if (mPlayer == null) {
            return false;
        }
        return mPlayer.isPlaying();
    }

    /**
     * Start play a stream
     */
    public void playStream(Stream stream) {

        // If a stream was already running stop it and reset
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }

        mPlayer.reset();

        try {
            isMediaPlayerPreparing = true;
            mPlayer.setDataSource(this, Uri.parse(String.format("%s?client_id=%s", stream.getUrl(), getString(R.string.soundclound_api_key))));
            mPlayer.setLooping(true);
            mCurrentStream = stream;
        } catch (Exception e) {
            Log.e(TAG, "playStream: ", e);
        }
        mPlayer.prepareAsync();
    }

    /**
     * Stop the MediaPlayer if something is streaming
     */
    public void stopStreaming(boolean stopTimer) {

        if (null != mPlayer && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.reset();
        }

        if (stopTimer) stopSleepTimer();
    }

    /**
     * Get the stream that is playing right now, if any
     *
     * @return the playing stream or null
     */
    public Stream getPlayingStream() {

        if (null != mPlayer && mPlayer.isPlaying()) {
            return mCurrentStream;
        }
        return null;
    }

    /**
     * Set a sleep timer
     *
     * @param milliseconds to wait before sleep
     */
    public void setSleepTimer(int milliseconds) {
        Log.i(TAG, "setSleepTimer: setting sleep timer for " + milliseconds + "ms");

        stopSleepTimer();

        if (milliseconds != 0) {

            mCountDownTimer = new CountDownTimer(milliseconds, 1000) {

                public void onTick(long millisUntilFinished) {
                    Intent intent = new Intent(TIMER_UPDATE_INTENT);
                    intent.putExtra(TIMER_UPDATE_VALUE, (int) millisUntilFinished);
                    mBroadcastManager.sendBroadcast(intent);
                }

                public void onFinish() {
                    stopStreaming(true);
                    timerDoneBroadcast();
                    toBackground();
                }

            }.start();
        }
    }

    /**
     * Stop the sleep timer
     */
    public void stopSleepTimer() {

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private void timerDoneBroadcast() {
        Log.i(TAG, "setSleepTimer: sleep timer is done, notifying bindings.");

        Intent intent = new Intent(TIMER_DONE_INTENT);
        mBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "onError: " + what + ", " + extra);

        Toast.makeText(StreamService.this, R.string.stream_error_toast, Toast.LENGTH_SHORT).show();
        mp.reset();
        isMediaPlayerPreparing = false;
        notifyStreamLoaded(false);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        isMediaPlayerPreparing = false;
        notifyStreamLoaded(true);
        mp.start();
    }

    /**
     * Send out a broadcast indicating stream was started with success or couldn't be found
     *
     * @param success
     */
    private void notifyStreamLoaded(boolean success) {

        Intent intent = new Intent(STREAM_DONE_LOADING_INTENT);
        intent.putExtra(STREAM_DONE_LOADING_SUCCESS, success);
        mBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}