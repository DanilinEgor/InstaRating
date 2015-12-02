package ru.egor_d.instarating.di;

import javax.inject.Singleton;

import dagger.Component;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.ProfileView;
import ru.egor_d.instarating.activity.ProfileActivity;
import ru.egor_d.instarating.InstagramPhotosAdapter;
import ru.egor_d.instarating.activity.LoginActivity;
import ru.egor_d.instarating.activity.SearchUserActivity;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(ProfileActivity profileActivity);

    void inject(SearchUserActivity searchUserActivity);

    void inject(InstagramPhotosAdapter instagramPhotosAdapter);

    void inject(LoginActivity loginActivity);

    void inject(InstagramRatingPreferenceManager instagramRatingPreferenceManager);

    void inject(ProfileView profileView);
}
