package com.nielsmasdorp.sleeply.ui.stream;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nielsmasdorp.sleeply.R;
import com.nielsmasdorp.sleeply.SleeplyApplication;
import com.nielsmasdorp.sleeply.model.Stream;
import com.nielsmasdorp.sleeply.ui.BaseActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainActivity extends BaseActivity implements MainView {

    @Bind(R.id.playBtn)
    ImageButton playButton;
    @Bind(R.id.nextBtn)
    ImageButton nextButton;
    @Bind(R.id.prevBtn)
    ImageButton previousButton;
    @Bind(R.id.showSoundsBtn)
    Button showSoundsButton;
    @Bind(R.id.titleText)
    TextView titleText;
    @Bind(R.id.descText)
    TextView descText;
    @Bind(R.id.sleepTimerText)
    TextView sleepTimerText;
    @Bind(R.id.loading)
    ProgressBar uiLoading;
    @Bind(R.id.loadingStream)
    ProgressBar streamLoading;
    @Bind(R.id.background)
    ImageView background;
    @Bind(R.id.mainUI)
    LinearLayout mainUI;

    @Inject
    MainPresenter presenter;

    @Inject
    StreamGridAdapter gridAdapter;

    private Animation fadeOut, fadeIn;

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
        ((SleeplyApplication) getApplication()).provideApplicationComponent(this).inject(this);
        ButterKnife.bind(this);
        setTitle(getString(R.string.action_bar_title));

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_stream_on_wifi).setChecked(presenter.isStreamWifiOnly());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_stream_on_wifi:
                presenter.setStreamWifiOnly(!item.isChecked());
                invalidateOptionsMenu();
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
    @OnClick({R.id.prevBtn, R.id.playBtn, R.id.nextBtn, R.id.showSoundsBtn, R.id.sleepTimerBtn})
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
            case R.id.showSoundsBtn:
                presenter.getAllStreams();
                break;
            case R.id.sleepTimerBtn:
                selectSleepTimer();
                break;
        }
    }

    @Override
    public void initializeUI(Stream stream, boolean isPlaying) {

        titleText.setText(stream.getTitle());
        descText.setText(stream.getDesc());
        background.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, stream.getBigImgRes()));

        if (isPlaying) {
            setToPlaying();
        } else {
            setToStopped();
        }

        mainUI.setVisibility(View.VISIBLE);
        uiLoading.setVisibility(View.GONE);
    }

    @Override
    public void setLoading() {

        streamLoading.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        showSoundsButton.setEnabled(false);

        playButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_48dp));
    }

    @Override
    public void setToPlaying() {

        stopLoading();

        playButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_48dp));
    }

    @Override
    public void setToStopped() {

        stopLoading();

        playButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_48dp));
        sleepTimerText.setText("");
    }

    private void stopLoading() {

        streamLoading.setVisibility(View.INVISIBLE);
        playButton.setVisibility(View.VISIBLE);
        nextButton.setEnabled(true);
        previousButton.setEnabled(true);
        showSoundsButton.setEnabled(true);
    }

    @Override
    public void animateTo(Stream currentStream) {

        background.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                background.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, currentStream.getBigImgRes()));
                titleText.setText(currentStream.getTitle());
                descText.setText(currentStream.getDesc());
                background.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }

    @Override
    public void updateTimer(String timeLeft) {

        sleepTimerText.setText(timeLeft);
    }

    @Override
    public void error(String error) {

        Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showStreamsDialog(List<Stream> streams) {

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .contentGravity(GravityEnum.CENTER)
                .customView(R.layout.dialog_sounds, false)
                .build();

        RecyclerView grid = (RecyclerView) dialog.getCustomView();
        if (grid != null) {
            grid.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
            gridAdapter.setData(streams, (view, position, dataSet) -> {

                presenter.streamPicked(streams.get(position));
                dialog.dismiss();
            });
            grid.setAdapter(gridAdapter);
        }

        dialog.show();
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
                    sleepTimerText.setText("");
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
