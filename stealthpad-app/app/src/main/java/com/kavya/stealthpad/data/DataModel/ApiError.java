package com.kavya.stealthpad.data.DataModel;

public class ApiError {
    private final int statusCode;
    private final String message;
    private boolean shouldLogout = false;

    public ApiError(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public ApiError(int statusCode, String message, boolean shouldLogout) {
        this.statusCode = statusCode;
        this.message = message;
        this.shouldLogout = shouldLogout;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isShouldLogout() {
        return shouldLogout;
    }
}
