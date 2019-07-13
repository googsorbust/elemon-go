package com.frozendevs.periodictable.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.tensorflow.lite.examples.detection.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
    }
}
