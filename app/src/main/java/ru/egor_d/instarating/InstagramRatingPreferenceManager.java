package ru.egor_d.instarating;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import ru.egor_d.instarating.model.InstagramUser;

public class InstagramRatingPreferenceManager {
    SharedPreferences preferences;
    Gson gson;

    private final String TOKEN_KEY = InstagramRatingPreferenceManager.class.getPackage().getName() + ".token_key";
    private final String USER_KEY = InstagramRatingPreferenceManager.class.getPackage().getName() + ".user_key";

    public InstagramRatingPreferenceManager(SharedPreferences preferences, Gson gson) {
        this.preferences = preferences;
        this.gson = gson;
    }

    public void saveToken(String token) {
        preferences.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return preferences.getString(TOKEN_KEY, "");
    }

    public void saveUser(InstagramUser user) {
        preferences.edit().putString(USER_KEY, gson.toJson(user)).apply();
    }

    public InstagramUser getUser() {
        return gson.fromJson(preferences.getString(USER_KEY, "{}"), InstagramUser.class);
    }
}
