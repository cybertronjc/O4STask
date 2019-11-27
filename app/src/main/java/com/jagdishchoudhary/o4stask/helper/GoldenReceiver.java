package com.jagdishchoudhary.o4stask.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.widget.Toast;


import androidx.annotation.RequiresApi;

import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.jagdishchoudhary.o4stask.R;

public class GoldenReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context .getSystemService(Context.NOTIFICATION_SERVICE);

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "default")
                .setContentTitle("Golden Hour")
                .setContentText("Hey photographer, it's time for golden hour, Go out and click pictures")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Hey photographer, it's time for golden hour, Go out and click pictures"));

        notificationManager.notify(1, notification.build());

    }



}
