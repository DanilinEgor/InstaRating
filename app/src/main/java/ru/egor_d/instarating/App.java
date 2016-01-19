package ru.egor_d.instarating;

import android.app.Application;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import ru.egor_d.instarating.di.AppComponent;
import ru.egor_d.instarating.di.AppModule;
import ru.egor_d.instarating.di.DaggerAppComponent;

public class App extends Application {
    private AppComponent component;
    private static App instance;

    public App() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeathOnNetwork()
                    .build());
        }
    }

    public static App getInstance() {
        return instance;
    }

    public AppComponent component() {
        return component;
    }
}
