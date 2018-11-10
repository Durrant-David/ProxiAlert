package edu.byui.team06.proxialert.view;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import edu.byui.team06.proxialert.R;
import static android.support.v4.content.ContextCompat.getSystemService;

public class Notification {

    private NotificationManager notifManager;
    private static int notificationCounter = 0;
    private int notificationId;
    private NotificationCompat.Builder nb;

    public Notification(String title, String content, String longText, Context c ) {
        notificationCounter++;
        notificationId = notificationCounter;
        notifManager = getSystemService(c, NotificationManager.class);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* create a Notification channel */
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
                //.setLargeIcon(BitmapFactory.decodeFile(Context.getFilesDir().getPath("/data/data/edu.byui.team06.proxialert/IMG_2149.JPG"))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
               .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                   .bigText(longText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    }

    public void send()
    {
        notifManager.notify(notificationId, nb.build());
    }




    //finally notify the user.
// notificationId is a unique int for each notification that you must define

}
