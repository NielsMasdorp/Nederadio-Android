package com.nielsmasdorp.sleeply.view;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nielsmasdorp.sleeply.BuildConfig;
import com.nielsmasdorp.sleeply.R;
import com.nielsmasdorp.sleeply.model.Stream;
import com.nielsmasdorp.sleeply.service.StreamService;
import com.nielsmasdorp.sleeply.service.StreamService.StreamBinder;
import com.nielsmasdorp.sleeply.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.playBtn)
    ImageButton mPlayButton;

    @Bind(R.id.nextBtn)
    ImageButton mNextButton;

    @Bind(R.id.prevBtn)
    ImageButton mPrevButton;

    @Bind(R.id.titleText)
    TextView mTitleText;

    @Bind(R.id.descText)
    TextView mDescText;

    @Bind(R.id.sleepTimerText)
    TextView mSleepTimerText;

    @Bind(R.id.loading)
    ProgressBar mLoadingBar;

    @Bind(R.id.loadingStream)
    ProgressBar mStreamLoading;

    @Bind(R.id.background)
    ImageView mBackground;

    @Bind(R.id.mainUI)
    RelativeLayout mMainUI;

    private StreamService mStreamService;
    private boolean mBound = false;
    private boolean isPlaying = false;
    private Animation fadeOut, fadeIn;
    private List<Stream> mStreams;
    private Stream mCurrentStream;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedPreferences mPrefs;
    private ConnectivityManager mCm;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, StreamService.class);
        if (!isServiceAlreadyRunning()) {
            Log.i(TAG, "onStart: service not running, starting service.");
            startService(intent);
        }
        Log.i(TAG, "onStart: binding to service.");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        registerBroadcastReceiver();
    }

    /**
     * Registers a broadcast receiver to listen to two types of intents
     */
    private void registerBroadcastReceiver() {

        Log.i(TAG, "onStart: registering broadcast receiver.");
        IntentFilter broadcastIntentFilter = new IntentFilter();
        broadcastIntentFilter.addAction(StreamService.STREAM_DONE_LOADING_INTENT);
        broadcastIntentFilter.addAction(StreamService.TIMER_DONE_INTENT);
        broadcastIntentFilter.addAction(StreamService.TIMER_UPDATE_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), broadcastIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        storeCurrent();
        if (mBound) {
            Log.i(TAG, "onStop: unbinding from service.");
            unbindService(mConnection);
            mBound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        generateStreams();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                handleIntent(intent);
            }
        };
    }

    /**
     * Handles the intents the broadcast receiver receives
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {

        if (intent.getAction().equals(StreamService.STREAM_DONE_LOADING_INTENT)) {

            stopStreamLoading();

            boolean success = intent.getBooleanExtra(StreamService.STREAM_DONE_LOADING_SUCCESS, false);
            if (!success) {

                setToNotPlaying();
            }
        } else if (intent.getAction().equals(StreamService.TIMER_DONE_INTENT)) {

            setToNotPlaying();
        } else if (intent.getAction().equals(StreamService.TIMER_UPDATE_INTENT)) {

            long timerValue = (long) intent.getIntExtra(StreamService.TIMER_UPDATE_VALUE, 0);
            setCountDown(timerValue);
        }

    }

    /**
     * Sets the countdown timer to a specific value
     *
     * @param timerValue
     */
    private void setCountDown(long timerValue) {

        if (timerValue > TimeUnit.HOURS.toMillis(1)) {

            mSleepTimerText.setText(String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(timerValue),
                    TimeUnit.MILLISECONDS.toMinutes(timerValue) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timerValue)),
                    TimeUnit.MILLISECONDS.toSeconds(timerValue) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerValue))));
        } else {

            mSleepTimerText.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(timerValue) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timerValue)),
                    TimeUnit.MILLISECONDS.toSeconds(timerValue) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerValue))));
        }
    }

    private void stopStreamLoading() {

        mStreamLoading.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.VISIBLE);
        mPrevButton.setEnabled(true);
        mNextButton.setEnabled(true);
    }

    private void startStreamLoading() {

        mStreamLoading.setVisibility(View.VISIBLE);
        mPlayButton.setVisibility(View.INVISIBLE);
        mNextButton.setEnabled(false);
        mNextButton.setEnabled(false);

    }

    /**
     * Initialize the list of streams
     */
    private void generateStreams() {
        mStreams = new ArrayList<>();
        mStreams.add(new Stream(0, "https://api.soundcloud.com/tracks/110697958/stream", getString(R.string.rainy_stream_title), getString(R.string.rainy_stream_desc), R.drawable.rain_background));
        mStreams.add(new Stream(1, "https://api.soundcloud.com/tracks/13262271/stream", getString(R.string.ocean_stream_title), getString(R.string.ocean_stream_desc), R.drawable.ocean_background));
        mStreams.add(new Stream(2, "https://api.soundcloud.com/tracks/97924982/stream", getString(R.string.forest_stream_title), getString(R.string.forest_stream_desc), R.drawable.nature_background));
        mStreams.add(new Stream(3, "https://api.soundcloud.com/tracks/149844883/stream", getString(R.string.meditation_stream_title), getString(R.string.meditation_stream_desc), R.drawable.meditation_background));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sleep_timer:
                selectSleepTimer();
                break;
            case R.id.menu_email:
                sendEmail();
                break;
            case R.id.menu_about:
                showAboutDialog();
                break;
        }
        return true;
    }

    /**
     * Pick a desired sleep timer
     */
    private void selectSleepTimer() {

        new MaterialDialog.Builder(this)
                .title(R.string.sleep_timer_title)
                .items(R.array.sleep_timer)
                .itemsCallback((dialog, view, which, text) -> {
                    if (isPlaying) {
                        mStreamService.setSleepTimer(calculateMs(which));
                        mSleepTimerText.setText("");
                    } else {
                        Toast.makeText(MainActivity.this, "Start a stream first..", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * How many milliseconds should the sleep timer last
     *
     * @param option selected in list
     * @return milliseconds for timer
     */
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

    @SuppressWarnings("unused")
    @OnClick({R.id.prevBtn, R.id.playBtn, R.id.nextBtn})
    public void musicControls(View view) {
        if (mBound) {
            switch (view.getId()) {
                case R.id.prevBtn:
                    Log.i(TAG, "play: previous stream.");
                    previousStream();
                    break;
                case R.id.playBtn:
                    if (!isPlaying) {
                        Log.i(TAG, "play: starting stream.");
                        startStreaming();
                    } else {
                        Log.i(TAG, "play: stopping stream.");
                        stopStreaming();
                    }
                    break;
                case R.id.nextBtn:
                    Log.i(TAG, "play: next stream.");
                    nextStream();
                    break;
            }
        }
    }

    /**
     * Start playing the next stream
     */
    private void nextStream() {
        int currentStreamId = mCurrentStream.getId();
        if (currentStreamId != (mStreams.size() - 1)) {
            mCurrentStream = mStreams.get(currentStreamId + 1);
        } else {
            mCurrentStream = mStreams.get(0);
        }
        if (isPlaying) {
            startStreaming();
        }
        animateUI();
    }

    /**
     * Stores the current stream in shared preferences
     * for next app start
     */
    private void storeCurrent() {

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(Constants.lastStreamIdentifier, mCurrentStream.getId());
        editor.apply();
    }

    /**
     * Start playing the previous stream
     */
    private void previousStream() {
        int currentStreamId = mCurrentStream.getId();
        if (currentStreamId != 0) {
            mCurrentStream = mStreams.get(currentStreamId - 1);
        } else {
            mCurrentStream = mStreams.get(mStreams.size() - 1);
        }
        if (isPlaying) {
            startStreaming();
        }
        animateUI();
    }

    /**
     * Stop streaming the current stream
     */
    private void stopStreaming() {
        if (mStreamService != null && mBound) mStreamService.stopStreaming();
        setToNotPlaying();
    }

    /**
     * Set to a not playing state
     */
    private void setToNotPlaying() {

        mPlayButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_48dp));
        isPlaying = false;
        mSleepTimerText.setText("");
    }

    /**
     * Set to a playing state
     */
    private void setToPlaying() {

        mPlayButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop_48dp));
        mSleepTimerText.setText("");
        isPlaying = true;
    }

    /**
     * Update the background photo
     * This animates the old image into the new one
     */
    private void animateUI() {

        mBackground.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mBackground.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, mCurrentStream.getImageResource()));
                mTitleText.setText(mCurrentStream.getTitle());
                mDescText.setText(mCurrentStream.getDesc());
                mBackground.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * Start streaming the current stream
     */
    private void startStreaming() {

        mStreamService.playStream(mCurrentStream);
        startStreamLoading();
        mPlayButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop_48dp));
        isPlaying = true;
        checkIfOnWifi();
    }

    private void checkIfOnWifi() {

        NetworkInfo activeNetwork = mCm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI) {
                Toast.makeText(MainActivity.this, R.string.no_wifi_toast, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "onServiceConnected: successfully bound to service.");
            StreamBinder binder = (StreamBinder) service;
            mStreamService = binder.getService();
            mBound = true;
            createStreamView();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "onServiceDisconnected: disconnected from service.");
            mBound = false;
        }
    };

    /**
     * When the activity binds to the service
     * create the UI depending on the song playing at the service
     * if any.
     */
    private void createStreamView() {

        Stream currentPlayingStream = mStreamService.getPlayingStream();

        String title;
        String desc;

        if (currentPlayingStream != null) {
            mCurrentStream = currentPlayingStream;
            title = currentPlayingStream.getTitle();
            desc = currentPlayingStream.getDesc();
            setToPlaying();
        } else {
            int lastStream = mPrefs.getInt(Constants.lastStreamIdentifier, 0);
            mCurrentStream = mStreams.get(lastStream);
            title = mCurrentStream.getTitle();
            desc = mCurrentStream.getDesc();
            setToNotPlaying();
        }

        mTitleText.setText(title);
        mDescText.setText(desc);
        mBackground.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, mCurrentStream.getImageResource()));
        mMainUI.setVisibility(View.VISIBLE);
        mLoadingBar.setVisibility(View.GONE);
    }

    /**
     * See if the StreamService is already running in the background.
     *
     * @return boolean indicating if the service runs
     */
    private boolean isServiceAlreadyRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StreamService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Send an email
     */
    private void sendEmail() {
        Intent requestIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                getString(R.string.email_intent_type), getString(R.string.dev_email_address), null));
        requestIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        startActivity(Intent.createChooser(requestIntent, getString(R.string.email_intent_title)));
    }

    /**
     * Checks wether the user just updated the app to a new version.
     * If this is the case show a "what's new dialog.
     */
    @SuppressWarnings("unused")
    private void checkAppVersion() {

        if (mPrefs.getInt(Constants.versionNumberIdentifier, 1) != BuildConfig.VERSION_CODE) {

            new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.update_title)
                    .content(Html.fromHtml(getString(R.string.update_content)))
                    .positiveText(R.string.update_positive)
                    .build()
                    .show();

            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putInt(Constants.versionNumberIdentifier, BuildConfig.VERSION_CODE);
            editor.apply();
        }
    }

    /**
     * Show the about the app dialog
     */
    private void showAboutDialog() {

        MaterialDialog aboutDialog = new MaterialDialog.Builder(this)
                .title(R.string.about_title)
                .positiveText(R.string.about_positive)
                .content(Html.fromHtml(getResources().getString(R.string.about_message)))
                .build();

        aboutDialog.show();
    }
}
