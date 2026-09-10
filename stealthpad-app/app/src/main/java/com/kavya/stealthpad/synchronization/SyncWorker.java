package com.kavya.stealthpad.synchronization;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kavya.stealthpad.utils.SessionManager;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class SyncWorker extends Worker {

    private final SyncManager syncManager;
    private final SessionManager sessionManager;
    private static final String TAG = "SyncWorker";
    public static final String USER_EMAIL = "user_email";

    @AssistedInject
    public SyncWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            SyncManager syncManager,
            SessionManager sessionManager
    ) {
        super(context, workerParams);
        this.syncManager = syncManager;
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Result doWork() {
        String email = getInputData().getString(USER_EMAIL);

        if (email == null) {
            if (!sessionManager.isLoggedIn()) {
                return Result.success();
            }
            email = sessionManager.getEmail();
        }

        if (email == null) {
            Log.e(TAG, "Email is null, cannot sync");
            return Result.failure();
        }

        try {
            syncManager.sync(email);
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "SyncWorker failed", e);
            return Result.retry();
        }
    }
}
