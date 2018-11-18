package com.sellproducts.thiennt.sellstore.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Heper.NotificationHelper;
import com.sellproducts.thiennt.sellstore.OrderStatus;
import com.sellproducts.thiennt.sellstore.R;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging  extends FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sendNotification(remoteMessage);

        if(remoteMessage.getData() == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotification26(remoteMessage);
            } else {
                sendNotification(remoteMessage);
            }
        }
    }

    private void sendNotification26(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();

        String tilte = data.get("tilte");
        String messsage = data.get("message");

        //
        PendingIntent pendingIntent;
        NotificationHelper helper;
        Notification.Builder builder;

        if(Common.currentUser != null) {

            Intent intent = new Intent(this, OrderStatus.class);
            intent.putExtra("userphone", Common.currentUser.getPhone());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.getsellstoreChannelNotification(tilte, messsage, pendingIntent, uri);
            helper.getMannager().notify(new Random().nextInt(), builder.build());
        }
        else
        {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.getsellstoreChannelNotification(tilte, messsage, uri);
            helper.getMannager().notify(new Random().nextInt(), builder.build());
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();

        String tilte = data.get("tilte");
        String messsage = data.get("message");
        if(Common.currentUser != null) {
            Intent intent = new Intent(this, OrderStatus.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(tilte)
                    .setContentText(messsage)
                    .setAutoCancel(true)
                    .setSound(uri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationCompat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationCompat.notify(0, builder.build());
        }else {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(tilte)
                    .setContentText(messsage)
                    .setAutoCancel(true)
                    .setSound(uri);

            NotificationManager notificationCompat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationCompat.notify(0, builder.build());
        }

    }
}
