package ru.egor_d.instarating.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.api.IInstagramService;
import ru.egor_d.instarating.fragment.ProfileFragmentBuilder;
import ru.egor_d.instarating.fragment.SearchUserFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @Bind(R.id.nav_view)
    NavigationView navigationView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    ImageView avatar;

    @Inject
    InstagramRatingPreferenceManager preferenceManager;
    @Inject
    IInstagramService instagramService;

    String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getInstance().component().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.menu_activity_main_drawer_not_logged);
        avatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.avatar);
        avatar.setImageResource(R.drawable.tw__ic_logo_default);

        mToolbar.setTitle("");

        token = preferenceManager.getToken();
        setFragment(new ProfileFragmentBuilder().build());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*
    <group android:checkableBehavior="single">
        <item
            android:id="@+id/nav_camera"
            android:icon="@drawable/ic_action_home"
            android:title="Profile"/>
        <item
            android:id="@+id/nav_gallery"
            android:icon="@drawable/ic_action_search"
            android:title="Search"/>
        <!--<item-->
        <!--android:id="@+id/nav_manage"-->
        <!--android:icon="@drawable/ic_menu_manage"-->
        <!--android:title="Tools"/>-->
    </group>
    <item>
        <menu>
            <item
                android:id="@+id/nav_share"
                android:icon="@drawable/ic_menu_share"
                android:title="Share"/>
            <item
                android:id="@+id/nav_send"
                android:icon="@drawable/ic_action_grade"
                android:title="Rate"/>
            <item
                android:id="@+id/nav_logout"
                android:icon="@drawable/ic_action_exit"
                android:title="Logout"/>
        </menu>
    </item>
     */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_profile:
                setFragment(new ProfileFragmentBuilder().build());
                break;
            case R.id.nav_search:
                setFragment(new SearchUserFragment());
                break;
            case R.id.nav_share:
                String shareBody = "Hey! Look at "
                        + "http://play.google.com/store/apps/details?id=" + getPackageName();
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "share app"));
                break;
            case R.id.nav_rate:
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                break;
            case R.id.nav_logout:
                preferenceManager.saveToken("");
                preferenceManager.saveUser(null);
                setFragment(new SearchUserFragment());
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setFragment(final Fragment fragment) {
        setFragment(fragment, false);
    }

    public void setFragment(final Fragment fragment, final boolean addToBackStack) {
        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) {
            tr.addToBackStack(fragment.getTag());
        }
        tr.replace(R.id.container, fragment).commit();
    }
}

