package com.nielsmasdorp.sleeply.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.nielsmasdorp.sleeply.SleeplyApplication;

import java.util.List;

import dagger.ObjectGraph;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public abstract class BaseActivity extends AppCompatActivity {

    private ObjectGraph activityGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGraph = ((SleeplyApplication) getApplication()).createScopedGraph(getModules().toArray());
        activityGraph.inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityGraph = null;
    }

    protected abstract List<Object> getModules();
}
