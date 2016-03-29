package com.nielsmasdorp.sleeply.ui.stream;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
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
import com.nielsmasdorp.sleeply.R;
import com.nielsmasdorp.sleeply.model.Stream;
import com.nielsmasdorp.sleeply.ui.BaseActivity;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainActivity extends BaseActivity implements MainView {

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

    @Inject
    MainPresenter presenter;

    private Animation fadeOut, fadeIn;

    @Override
    protected List<Object> getModules() {
        return Arrays.asList(new MainModule(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.startService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unBindService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
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

    @SuppressWarnings("unused")
    @OnClick({R.id.prevBtn, R.id.playBtn, R.id.nextBtn})
    public void musicControls(View view) {
        switch (view.getId()) {
            case R.id.prevBtn:
                presenter.previousStream();
                break;
            case R.id.playBtn:
                presenter.playStream();
                break;
            case R.id.nextBtn:
                presenter.nextStream();
                break;
        }
    }

    @Override
    public void initializeUI(Stream stream, boolean isPlaying) {

        mTitleText.setText(stream.getTitle());
        mDescText.setText(stream.getDesc());
        mBackground.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, stream.getImageResource()));

        if (isPlaying) {
            setToPlaying();
        } else {
            setToStopped();
        }

        mMainUI.setVisibility(View.VISIBLE);
        mLoadingBar.setVisibility(View.GONE);
    }

    @Override
    public void setLoading() {

        mStreamLoading.setVisibility(View.VISIBLE);
        mPlayButton.setVisibility(View.INVISIBLE);
        mNextButton.setEnabled(false);
        mPrevButton.setEnabled(false);

        mPlayButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop_48dp));
    }

    @Override
    public void setToPlaying() {

        stopLoading();

        mPlayButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop_48dp));
        mSleepTimerText.setText("");
    }

    @Override
    public void setToStopped() {

        stopLoading();

        mPlayButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_48dp));
        mSleepTimerText.setText("");
    }

    private void stopLoading() {

        mStreamLoading.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.VISIBLE);
        mNextButton.setEnabled(true);
        mPrevButton.setEnabled(true);
    }

    @Override
    public void animateTo(Stream currentStream) {

        mBackground.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mBackground.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, currentStream.getImageResource()));
                mTitleText.setText(currentStream.getTitle());
                mDescText.setText(currentStream.getDesc());
                mBackground.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }

    @Override
    public void updateTimer(Long timeLeft) {

        if (timeLeft > TimeUnit.HOURS.toMillis(1)) {

            mSleepTimerText.setText(String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(timeLeft),
                    TimeUnit.MILLISECONDS.toMinutes(timeLeft) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)),
                    TimeUnit.MILLISECONDS.toSeconds(timeLeft) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft))));
        } else {

            mSleepTimerText.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(timeLeft) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)),
                    TimeUnit.MILLISECONDS.toSeconds(timeLeft) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft))));
        }

    }

    @Override
    public void error(String error) {

        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Pick a desired sleep timer
     */
    private void selectSleepTimer() {

        new MaterialDialog.Builder(this)
                .title(R.string.sleep_timer_title)
                .items(R.array.sleep_timer)
                .itemsCallback((dialog, view, which, text) -> {
                    presenter.setSleepTimer(which);
                    mSleepTimerText.setText("");
                })
                .show();
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
