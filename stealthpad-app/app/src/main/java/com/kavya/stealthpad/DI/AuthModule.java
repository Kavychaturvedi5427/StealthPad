package com.kavya.stealthpad.DI;

import android.content.Context;

import com.kavya.stealthpad.utils.AuthInterceptor;
import com.kavya.stealthpad.utils.SessionManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AuthModule {

    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context){
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    public AuthInterceptor provideAuthInterceptor(SessionManager sessionManager){
        return new AuthInterceptor(sessionManager);
    }
}
