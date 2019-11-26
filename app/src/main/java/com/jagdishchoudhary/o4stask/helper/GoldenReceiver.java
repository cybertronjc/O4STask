package com.jagdishchoudhary.o4stask.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Build;


import androidx.annotation.RequiresApi;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class GoldenReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        OneTimeWorkRequest simpleRequest = new OneTimeWorkRequest.Builder(GoldenWorker.class)
                .build();

        WorkManager.getInstance().enqueue(simpleRequest);

    }
}
