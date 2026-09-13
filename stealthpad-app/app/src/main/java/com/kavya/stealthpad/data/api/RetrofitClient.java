package com.kavya.stealthpad.data.api;

import com.google.gson.Gson;
import com.kavya.stealthpad.utils.AuthInterceptor;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module    // this annotation tells hilt how to create object of any 3rd party class..
@InstallIn(SingletonComponent.class)    // this ensures that this module live in application scope...
public class RetrofitClient {

    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }

    @Provides       // when someone asks for this type call this...
    @Singleton      // create one instance and reuse it ...
    public Retrofit provideRetrofit(OkHttpClient okHttpClient, Gson gson){
        return new Retrofit.Builder().baseUrl("https://stealthpad-backend.onrender.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    public AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);  // this will create and return the retrofit object...
    }

    @Provides
    @Singleton
    public NotesApi provideNotesApi(Retrofit retrofit){
        return retrofit.create(NotesApi.class);
    }

    @Provides
    @Singleton
    public AiApi provideAiApi(Retrofit retrofit) {
        return retrofit.create(AiApi.class);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor) {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .build();
    }

}
