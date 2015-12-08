package ru.egor_d.instarating.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.ProfileFragmentBuilder;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.SearchUserFragment;
import ru.egor_d.instarating.model.InstagramUser;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.activity_main_menu_home)
    View menuHome;
    @Bind(R.id.activity_main_menu_search)
    View menuSearch;
    @Bind(R.id.activity_main_menu_logout)
    View menuLogout;
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

        setFragment(new SearchUserFragment());

        menuHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ProfileFragmentBuilder builder = new ProfileFragmentBuilder();
                InstagramUser user = preferenceManager.getUser();
                if (user != null) {
                    builder.user(user);
                }
                setFragment(builder.build());
            }
        });
        menuSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setFragment(new SearchUserFragment());
            }
        });
        menuLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                preferenceManager.saveToken("");
                preferenceManager.saveUser(null);
                setFragment(new SearchUserFragment());
            }
        });
    }

    public void setFragment(Fragment fragment) {
        while (getSupportFragmentManager().popBackStackImmediate()) ;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main_container, fragment)
                .commit();
    }

    public void setFragmentWithBackStack(Fragment fragment) {
        while (getSupportFragmentManager().popBackStackImmediate()) ;
        getSupportFragmentManager()
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
                menuHome.setVisibility(View.VISIBLE);
                menuLogout.setVisibility(View.VISIBLE);
                menuSearch.setVisibility(View.VISIBLE);
                break;
            case NONE:
                menuHome.setVisibility(View.GONE);
                menuLogout.setVisibility(View.GONE);
                menuSearch.setVisibility(View.GONE);
        }
    }
}
