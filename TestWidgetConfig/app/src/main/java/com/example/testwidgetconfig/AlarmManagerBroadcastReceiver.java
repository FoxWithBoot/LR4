package com.example.testwidgetconfig;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("My Log", "AAAAAAAAAAAAAaa");

        //AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //Intent intentt = new Intent(context, AlarmManagerBroadcastReceiver.class);
        //PendingIntent pi= PendingIntent.getBroadcast(context,0, intentt,0);
        //am.cancel(pi);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Будильник")
                .setContentText("Пора!!!");
                //.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                //.setLights(Color.RED, 3000, 3000)
                //.setSound(Uri.parse("uri://sadfasdfasdf.mp3"));
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
