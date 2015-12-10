package ru.egor_d.instarating.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.fragment.ProfileFragmentBuilder;
import ru.egor_d.instarating.fragment.SearchUserFragment;
import ru.egor_d.instarating.fragment.SettingsFragment;
import ru.egor_d.instarating.model.InstagramUser;

public class MainActivity extends Activity {
    @Bind(R.id.activity_main_menu_home)
    View menuHome;
    @Bind(R.id.activity_main_menu_search)
    View menuSearch;
    @Bind(R.id.activity_main_menu_settings)
    View menuSettings;
    @Bind(R.id.activity_main_menu_home_delimiter)
    View menuHomeDelimiter;
    @Bind(R.id.activity_main_menu_search_delimiter)
    View menuSearchDelimiter;
    @Bind(R.id.activity_main_menu_settings_delimiter)
    View menuSettingsDelimiter;
    @Bind(R.id.activity_main_menu_ll)
    View menu;

    @Inject
    InstagramRatingPreferenceManager preferenceManager;

    String token = "";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        App.getInstance().component().inject(this);

        token = preferenceManager.getToken();
        setMenuVisibility(!token.isEmpty() ? MenuMode.ALL : MenuMode.NONE);

        menuHomeDelimiter.setVisibility(View.VISIBLE);
        menuSearchDelimiter.setVisibility(View.GONE);
        menuSettingsDelimiter.setVisibility(View.VISIBLE);

        menuHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ProfileFragmentBuilder builder = new ProfileFragmentBuilder();
                InstagramUser user = preferenceManager.getUser();
                if (user != null) {
                    builder.user(user);
                }
                setFragment(builder.build());
                menuHomeDelimiter.setVisibility(View.GONE);
                menuSearchDelimiter.setVisibility(View.VISIBLE);
                menuSettingsDelimiter.setVisibility(View.VISIBLE);
            }
        });
        menuSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setFragment(new SearchUserFragment());
                menuHomeDelimiter.setVisibility(View.VISIBLE);
                menuSearchDelimiter.setVisibility(View.GONE);
                menuSettingsDelimiter.setVisibility(View.VISIBLE);
            }
        });
        menuSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setFragment(new SettingsFragment());
                menuHomeDelimiter.setVisibility(View.VISIBLE);
                menuSearchDelimiter.setVisibility(View.VISIBLE);
                menuSettingsDelimiter.setVisibility(View.GONE);
            }
        });

        setFragment(new SearchUserFragment());
    }

    public void setFragment(Fragment fragment) {
        while (getFragmentManager().popBackStackImmediate()) ;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main_container, fragment)
                .commit();
    }

    public void setFragmentWithBackStack(Fragment fragment) {
        while (getFragmentManager().popBackStackImmediate()) ;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public enum MenuMode {
        ALL, NONE
    }

    public void setMenuVisibility(MenuMode mode) {
        switch (mode) {
            case ALL:
                menu.setVisibility(View.VISIBLE);
//                menuHome.setVisibility(View.VISIBLE);
//                menuSettings.setVisibility(View.VISIBLE);
//                menuSearch.setVisibility(View.VISIBLE);
                break;
            case NONE:
                menu.setVisibility(View.GONE);
//                menuHome.setVisibility(View.GONE);
//                menuSettings.setVisibility(View.GONE);
//                menuSearch.setVisibility(View.GONE);
        }
    }
}
