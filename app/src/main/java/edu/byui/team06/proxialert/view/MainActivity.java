package edu.byui.team06.proxialert.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.byui.team06.proxialert.R;

import static java.lang.Math.sqrt;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    static public Boolean getDistance(float x1, float y1, float x2, float y2, float distance)
    {
        return distance <= sqrt(Math.pow(x2 - x1, 2) + Math.pow((y2-y1), 2));
    }
}
