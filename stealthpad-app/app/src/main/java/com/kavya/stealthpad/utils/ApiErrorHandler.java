package com.kavya.stealthpad.utils;

import com.google.gson.Gson;
import com.kavya.stealthpad.data.DataModel.ApiError;
import com.kavya.stealthpad.data.DataModel.ErrorResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
import retrofit2.Response;

@Singleton
public class ApiErrorHandler {

    private final Gson gson;
    private final SessionManager sessionManager;

    @Inject
    public ApiErrorHandler(Gson gson, SessionManager sessionManager) {
        this.gson = gson;
        this.sessionManager = sessionManager;
    }

    public ApiError handleError(Response<?> response) {
        int code = response.code();
        String message = "Something went wrong. Please try again.";

        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                String errorString = errorBody.string();
                ErrorResponse errorResponse = gson.fromJson(errorString, ErrorResponse.class);

                if (errorResponse != null) {
                    if (code == 400 && errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                        // Prefer field-level validation messages
                        Map.Entry<String, String> firstError = errorResponse.getErrors().entrySet().iterator().next();
                        message = firstError.getValue();
                    } else if (errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                        message = errorResponse.getMessage();
                    }
                }
            }
        } catch (Exception e) {
            // Keep generic message if parsing fails
        }

        switch (code) {
            case 401:
                // We don't logout automatically here because some 401s (like login failure)
                // should not trigger a global logout state change in the app.
                // The ViewModel will decide based on the context.
                message = "Invalid credentials or session expired.";
                return new ApiError(code, message, true);
            case 403:
                message = "You don't have permission to perform this action.";
                break;
            case 404:
                if (message.equals("Something went wrong. Please try again.")) {
                    message = "Requested resource was not found.";
                }
                break;
            case 429:
                message = "AI request limit reached. Please try again later.";
                break;
            case 500:
                message = "Something went wrong on the server. Please try again.";
                break;
        }

        return new ApiError(code, message);
    }

    public ApiError handleException(Throwable t) {
        String message = "Something went wrong. Please try again.";
        int code = -1;

        if (t instanceof ConnectException) {
            message = "Unable to connect to the server. Check your internet connection.";
        } else if (t instanceof SocketTimeoutException) {
            message = "The request took too long. Please try again.";
        } else if (t instanceof IOException) {
            message = "Network error. Please try again.";
        }

        return new ApiError(code, message);
    }
}
