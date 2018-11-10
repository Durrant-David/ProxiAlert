package edu.byui.team06.proxialert.view;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.byui.team06.proxialert.R;

import static java.lang.Math.sqrt;


public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Notification n = new Notification("Test", "This is working",
                "I'm working and this is longer " +
                        "text that can be read if the notification is expanded.",
                this.getApplicationContext());
        n.send();


    }

        static public Boolean getDistance(float x1, float y1, float x2, float y2, float distance)
    {
        return distance <= sqrt(Math.pow(x2 - x1, 2) + Math.pow((y2-y1), 2));
    }
}
