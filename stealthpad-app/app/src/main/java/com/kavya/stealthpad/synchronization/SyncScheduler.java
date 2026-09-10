package com.kavya.stealthpad.synchronization;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class SyncScheduler {

    private static final String PERIODIC_SYNC = "stealthpad_periodic_sync";
    private static final String IMMEDIATE_SYNC = "stealthpad_immediate_sync";

    public static void schedulerPeriodicSync(@NonNull Context context, String userEmail) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        Data inputData = new Data.Builder().putString(SyncWorker.USER_EMAIL, userEmail).build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC_SYNC, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static void syncNow(
            @NonNull Context context,
            String userEmail) {

        Constraints constraints =
                new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

        Data inputData =
                new Data.Builder()
                        .putString(SyncWorker.USER_EMAIL, userEmail)
                        .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(SyncWorker.class)
                        .setConstraints(constraints)
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        IMMEDIATE_SYNC,
                        ExistingWorkPolicy.KEEP,
                        request
                );
    }


}
