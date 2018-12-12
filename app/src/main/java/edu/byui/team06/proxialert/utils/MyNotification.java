package edu.byui.team06.proxialert.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.model.ProxiDB;

import static android.support.v4.content.ContextCompat.getSystemService;

public class MyNotification {

    private NotificationManager notifManager;
    private static int notificationCounter = 0;
    private int notificationId;
    private NotificationCompat.Builder nb;
    private MediaPlayer notifSound;
    public MyNotification(ProxiDB task, Context c ) {
        notificationCounter++;
        notificationId = notificationCounter;
        notifManager = getSystemService(c, NotificationManager.class);

        File file = new File(task.getAudio());
        file.setReadable(true, false);
        String s = task.getAudio();
        Uri notifUri = Uri.parse( c.getFilesDir() +  task.getAudio());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* create a MyNotification channel */
            AudioAttributes audioAtts = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            String channelId = "myNotification";
            CharSequence channelName = "Some Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationChannel.setSound(notifUri, audioAtts);
            notifManager.createNotificationChannel(notificationChannel);


        }

        notifSound = new MediaPlayer();

        try {
            notifSound.setDataSource(task.getAudio());
        } catch (IOException e) {
            e.printStackTrace();
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setSound(notifUri);



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
        try {
            notifSound.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifSound.start();

    }


    public int getId() {
        return notificationCounter;
    }

    //finally notify the user.
// notificationId is a unique int for each notification that you must define

}
