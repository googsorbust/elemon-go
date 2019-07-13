package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.MessageFormat;

public class HomeScreen extends AppCompatActivity {

    private int points;
    private final String pointKey = "POINTS";
    private final String pointTemplate = "You have {0} points!";
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        sharedPref = HomeScreen.this.getPreferences(Context.MODE_PRIVATE);
        init();
    }

    public void showDetectionScreen(View view) {
        Intent myIntent = new Intent(HomeScreen.this, DetectorActivity.class);
        HomeScreen.this.startActivity(myIntent);
    }

    public void showMyElementsScreen(View view) {
        Intent myIntent = new Intent(HomeScreen.this, MyElements.class);
        HomeScreen.this.startActivity(myIntent);

    }

    private void init() {
        updatePointsOnView();
    }

    private int getPointsFromSharedPreferences() {
        return sharedPref.getInt(pointKey, -1);
    }

    private void writePointsToSharedPreferences(int newPoints) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(pointKey, newPoints);
        editor.apply();
    }

    private void updatePointsOnView() {
        int result = getPointsFromSharedPreferences();
        points = result >= 0 ? result : 0;

        Object[] params = new Object[]{points};
        String formattedPoints = MessageFormat.format(pointTemplate, params);
        TextView view = findViewById(R.id.pointLabel);
        view.setText(formattedPoints);
    }

    private void addPoints(int pointsToAdd) {
        points += pointsToAdd;
        writePointsToSharedPreferences(points);
        updatePointsOnView();
    }
}
