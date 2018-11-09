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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //create a Notification channel
        String channelId = "myNotification";
        CharSequence channelName = "Some Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(notificationChannel);


        //build the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "myNotification")
                //.setLargeIcon(BitmapFactory.decodeFile(Context.getFilesDir().getPath("/data/data/edu.byui.team06.proxialert/IMG_2149.JPG"))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("ProxiAlert!")
                .setContentText("John is within 1 mile of your current location! Have time to visit?")
                //.setStyle(new NotificationCompat.BigTextStyle()
                  //      .bigText("John is within 1 mile of your current location! Have time to visit?"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


            //finally notify the user.
// notificationId is a unique int for each notification that you must define
            notificationManager.notify(100, mBuilder.build());


    }

        static public Boolean getDistance(float x1, float y1, float x2, float y2, float distance)
    {
        return distance <= sqrt(Math.pow(x2 - x1, 2) + Math.pow((y2-y1), 2));
    }
}
