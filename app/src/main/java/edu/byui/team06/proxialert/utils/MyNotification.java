package edu.byui.team06.proxialert.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.model.ProxiDB;

import static android.support.v4.content.ContextCompat.getSystemService;

public class MyNotification {

    private NotificationManager notifManager;
    private static int notificationCounter = 0;
    private int notificationId;
    private NotificationCompat.Builder nb;

    public MyNotification(ProxiDB task, Context c ) {
        notificationCounter++;
        notificationId = notificationCounter;
        notifManager = getSystemService(c, NotificationManager.class);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* create a MyNotification channel */
            String channelId = "myNotification";
            CharSequence channelName = "Some Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notifManager.createNotificationChannel(notificationChannel);
        }

        //build the notification
        nb = new NotificationCompat.Builder(c, "myNotification")
                .setLargeIcon(BitmapFactory.decodeResource(c.getResources(),
                        R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.proxi_icon_round)
                .setContentTitle("ProxiAlert: You are near "+task.getTask()+ " at " + task.getAddress())
               .setContentText("Task Description: "+task.getDescription())
                .setStyle(new NotificationCompat.BigTextStyle()
                   .bigText("Task Description: "+task.getDescription()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);



        //Intent
        Uri navUri = Uri.parse("google.navigation:q="+task.getLat()+","+task.getLong());
        Intent navigationIntent =new Intent(Intent.ACTION_VIEW, navUri);
        navigationIntent.setPackage("com.google.android.apps.maps");

        PendingIntent contentIntent = PendingIntent.getActivity(c, 0,
                new Intent(navigationIntent), PendingIntent.FLAG_UPDATE_CURRENT);

        nb.setContentIntent(contentIntent);


    }

    public void send()
    {
        notifManager.notify(notificationId, nb.build());
    }


    public int getId() {
        return notificationCounter;
    }

    //finally notify the user.
// notificationId is a unique int for each notification that you must define

}
