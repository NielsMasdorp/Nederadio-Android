package com.nielsmasdorp.sleeply.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.nielsmasdorp.sleeply.R;
import com.nielsmasdorp.sleeply.SleeplyApplication;

import java.util.List;

import dagger.ObjectGraph;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public abstract class BaseActivity extends AppCompatActivity {

    private ObjectGraph activityGraph;
    private View customActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGraph = ((SleeplyApplication) getApplication()).createScopedGraph(getModules().toArray());
        activityGraph.inject(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customActivityTitle = inflator.inflate(R.layout.action_bar_title, null);
        getSupportActionBar().setCustomView(customActivityTitle);
    }

    protected abstract List<Object> getModules();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityGraph = null;
    }

    @Override
    public void setTitle(CharSequence title) {
        ((TextView) customActivityTitle.findViewById(R.id.title)).setText(title);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("");
    }
}
