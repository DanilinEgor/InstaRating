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
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.api.IInstagramService;
import ru.egor_d.instarating.fragment.ProfileFragmentBuilder;
import ru.egor_d.instarating.fragment.SearchUserFragment;
import ru.egor_d.instarating.model.InstagramResponse;
import ru.egor_d.instarating.model.InstagramUser;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

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

    @Inject
    InstagramRatingPreferenceManager preferenceManager;
    @Inject
    IInstagramService instagramService;
    @Inject
    Picasso picasso;

    String token = "";
    private CompositeSubscription mSubscriptions = new CompositeSubscription();

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

        token = preferenceManager.getToken();

        refreshNavigationView();

        if (token.isEmpty()) {
            setFragment(new SearchUserFragment());
            navigationView.getMenu().getItem(1).setChecked(true);
        } else {
            setFragment(new ProfileFragmentBuilder().build());
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    private void refreshNavigationView() {
        token = preferenceManager.getToken();
        refreshMenu();
        if (token.isEmpty()) {
            bindUser(null);
        } else {
            getMe();
        }
    }

    private void refreshMenu() {
        navigationView.getMenu().clear();
        if (token.isEmpty()) {
            navigationView.inflateMenu(R.menu.menu_activity_main_drawer_not_logged);
        } else {
            navigationView.inflateMenu(R.menu.menu_activity_main_drawer_logged);
        }
    }

    public void bindUser(InstagramUser user) {
        ImageView avatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_avatar);
        TextView username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
        if (user != null) {
            picasso.load(user.profile_picture).into(avatar);
            username.setText(user.username);
        } else {
            avatar.setImageDrawable(null);
            username.setText(R.string.app_name);
        }
    }

    // TODO: вынести в другой слой
    private void getMe() {
        mSubscriptions.add(instagramService
                .getMe(token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Func1<InstagramResponse<InstagramUser>, InstagramUser>() {
                    @Override
                    public InstagramUser call(final InstagramResponse<InstagramUser> instagramUserInstagramResponse) {
                        return instagramUserInstagramResponse.data;
                    }
                })
                .subscribe(new Action1<InstagramUser>() {
                    @Override
                    public void call(final InstagramUser instagramUser) {
                        instagramUser.profile_picture = instagramUser.profile_picture.replace("s150x150", "s200x200");
                        bindUser(instagramUser);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        Log.e(TAG, "error:", throwable);
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSubscriptions.clear();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_login:
                startActivityForResult(new Intent(this, LoginActivity.class), 1);
                break;
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
                refreshNavigationView();
                setFragment(new SearchUserFragment());
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        refreshNavigationView();
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

