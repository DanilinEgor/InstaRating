package ru.egor_d.instarating.di;

import javax.inject.Singleton;

import dagger.Component;
import ru.egor_d.instarating.InstagramPhotosAdapter;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.fragment.ProfileFragment;
import ru.egor_d.instarating.ProfileView;
import ru.egor_d.instarating.fragment.SearchUserFragment;
import ru.egor_d.instarating.activity.LoginActivity;
import ru.egor_d.instarating.activity.MainActivity;
import ru.egor_d.instarating.fragment.SettingsFragment;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(ProfileFragment profileFragment);

    void inject(SearchUserFragment searchUserFragment);

    void inject(SettingsFragment settingsFragment);

    void inject(InstagramPhotosAdapter instagramPhotosAdapter);

    void inject(LoginActivity loginActivity);

    void inject(InstagramRatingPreferenceManager instagramRatingPreferenceManager);

    void inject(ProfileView profileView);

    void inject(MainActivity mainActivity);
}
