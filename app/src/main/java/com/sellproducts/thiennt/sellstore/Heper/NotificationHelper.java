package com.sellproducts.thiennt.sellstore.Heper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.sellproducts.thiennt.sellstore.R;


public class NotificationHelper extends ContextWrapper {

    private static final String SELL_STORE_ID = "com.sellproducts.thiennt.sellstore";
    private static final String SELL_STORE_NAME = "Sell Store";

    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CreateChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void CreateChannel() {
        NotificationChannel channel = new NotificationChannel(SELL_STORE_ID,
                SELL_STORE_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getMannager().createNotificationChannel(channel);

    }

    public NotificationManager getMannager() {

        if(manager == null)
        {
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getsellstoreChannelNotification(String title, String body, PendingIntent conteninten, Uri Sounduri)
    {
        return  new Notification.Builder(getApplicationContext(), SELL_STORE_ID)
                .setContentIntent(conteninten)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setSound(Sounduri)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getsellstoreChannelNotification(String title, String body,Uri Sounduri)
    {
        return  new Notification.Builder(getApplicationContext(), SELL_STORE_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setSound(Sounduri)
                .setAutoCancel(false);
    }

}
