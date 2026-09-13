package com.kavya.stealthpad.data.DataModel;

public class ApiError {
    private final int statusCode;
    private final String message;

    public ApiError(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
