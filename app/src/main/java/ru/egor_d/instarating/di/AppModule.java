package ru.egor_d.instarating.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.api.IInstagramService;

@Module
public class AppModule {
    private App app;

    public AppModule(final App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return app;
    }

    @Provides
    @Singleton
    OkHttpClient providesOkHttp() {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);
        return client;
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl("https://api.instagram.com/v1/users/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    IInstagramService provideInstagramService(Retrofit retrofit) {
        return retrofit.create(IInstagramService.class);
    }

    @Provides
    @Singleton
    Picasso providePicasso() {
        return Picasso.with(app);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    InstagramRatingPreferenceManager provideInstagramRatingPreferenceManager(
            SharedPreferences preferences,
            Gson gson
    ) {
        return new InstagramRatingPreferenceManager(preferences, gson);
    }
}
