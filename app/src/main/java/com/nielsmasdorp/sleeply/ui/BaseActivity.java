package com.nielsmasdorp.sleeply.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.nielsmasdorp.sleeply.R;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public abstract class BaseActivity extends AppCompatActivity {

    private View customActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customActivityTitle = inflator.inflate(R.layout.action_bar_title, null);
        getSupportActionBar().setCustomView(customActivityTitle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setTitle(CharSequence title) {
        ((TextView) customActivityTitle.findViewById(R.id.title)).setText(title);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("");
    }
}
