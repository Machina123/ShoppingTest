package com.pcinnovations.shoppingtest;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.view.View;

public class NotificationHandler {
    public void sendNotification(View view, Context context, Intent intent, CharSequence title, CharSequence message) {

        PendingIntent pending = null;
        if(intent != null) { pending = PendingIntent.getActivity(context, 0, intent, 0);}

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(android.R.drawable.ic_menu_save)
                .setLargeIcon(BitmapFactory.decodeResource(view.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentTitle(title)
                .setTicker(title)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentText(message);
        if(intent != null) {
            builder.setContentIntent(pending);
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());
    }

    public void sendNotification(View view, Context context, CharSequence title, CharSequence message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(android.R.drawable.ic_menu_save)
                .setLargeIcon(BitmapFactory.decodeResource(view.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentTitle(title)
                .setTicker(title)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentText(message);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    public void sendNotification(View view, Context context, CharSequence title, CharSequence message, int smallIconID) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(smallIconID)
                .setLargeIcon(BitmapFactory.decodeResource(view.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());
    }
}