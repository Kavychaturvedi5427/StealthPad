package com.kavya.stealthpad.utils;

public final class SyncStatus {

    // Existing statuses — DO NOT CHANGE their values
    public static final int PENDING = 0;
    public static final int SUCCESS = 1;
    public static final int FAILED = 2;

    // New statuses for identifying the operation
    public static final int PENDING_CREATE = 3;
    public static final int PENDING_UPDATE = 4;
    public static final int PENDING_DELETE = 5;

    private SyncStatus() {
        // Prevent instantiation
    }
}