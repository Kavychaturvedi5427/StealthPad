package com.kavya.stealthpad.utils;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    @Inject
    public AuthInterceptor(SessionManager sessionManager){
        this.sessionManager = sessionManager;
    }

    // this class automatically add "Authorization", "Bearer " + token to every reques after login...
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        // checking if the request is auth related or not if yes then skip the authorization part and move to the auth endpoints...
        String path = chain.request().url().encodedPath();
        if (path.startsWith("/api/auth")) {
            return chain.proceed(chain.request());
        }

        // creating the bearer token so that it can be used every time a request is made..
        String token = sessionManager.getToken();

        Request.Builder builder = chain.request().newBuilder();

        if(token != null && !token.isEmpty()){
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return chain.proceed(builder.build());
    }
}
