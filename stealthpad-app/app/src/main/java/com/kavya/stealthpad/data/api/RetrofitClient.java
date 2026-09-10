package com.kavya.stealthpad.data.api;

import com.kavya.stealthpad.utils.AuthInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module    // this annotation tells hilt how to create object of any 3rd party class..
@InstallIn(SingletonComponent.class)    // this ensures that this module live in application scope...
public class RetrofitClient {

    @Provides       // when someone asks for this type call this...
    @Singleton      // create one instance and reuse it ...
    public Retrofit provideRetrofit(OkHttpClient okHttpClient){
        return new Retrofit.Builder().baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
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
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor){
        return new OkHttpClient.Builder().addInterceptor(authInterceptor).build();
    }

}
