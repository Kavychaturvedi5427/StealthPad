package com.kavya.stealthpad.data.api;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module    // this annotation tells hilt how to create object of any 3rd party class..
@InstallIn(SingletonComponent.class)    // this ensures that this module live in application scope...
public class RetrofitClient {

    @Provides       // when someone asks for this type call this...
    @Singleton      // create one instance and reuse it ...
    public Retrofit provideRetrofit(){
        return new Retrofit.Builder().baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);  // this will create and return the retrofit object...
    }

}
