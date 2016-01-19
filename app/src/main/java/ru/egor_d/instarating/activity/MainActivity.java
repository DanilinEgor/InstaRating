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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.BuildConfig;
import ru.egor_d.instarating.InstagramPhotosAdapter;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.api.IInstagramService;
import ru.egor_d.instarating.fragment.ProfileFragmentBuilder;
import ru.egor_d.instarating.fragment.SearchUserFragment;
import ru.egor_d.instarating.model.InstagramMedia;
import ru.egor_d.instarating.model.InstagramPhoto;
import ru.egor_d.instarating.model.InstagramResponse;
import ru.egor_d.instarating.model.InstagramUser;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.app_bar)
    AppBarLayout mAppBarLayout;

    @Inject
    InstagramRatingPreferenceManager preferenceManager;
    @Inject
    IInstagramService instagramService;

    String token = "";
    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    InstagramUser user;
    private InstagramPhotosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getInstance().component().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mToolbar.setTitle("");

        token = preferenceManager.getToken();
        setFragment(new ProfileFragmentBuilder().build());

        /*RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new InstagramPhotosAdapter(this);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.setItemAnimator(new LandingAnimator());

        if (user == null) {
            // opened my profile before me is loaded
            mSubscriptions.add(
                    instagramService
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
                                    preferenceManager.saveUser(instagramUser);
                                    user = instagramUser;
                                    mProfileView.bindUser(instagramUser);
                                    mToolbarLayout.setTitle(instagramUser.username);
                                    mSubscriptions.add(getPhotosSubscription(""));
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(final Throwable throwable) {
                                    Log.e(TAG, "error:", throwable);
                                }
                            })
            );
        } else {
            mProfileView.bindUser(user);
            mSubscriptions.add(
                    instagramService
                            .getUser(user.id, token)
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
                                    mProfileView.bindUser(instagramUser);
                                    mToolbarLayout.setTitle(instagramUser.username);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(final Throwable throwable) {
                                    Log.e(TAG, "error:", throwable);
                                }
                            })
            );
            mSubscriptions.add(getPhotosSubscription(""));
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mSubscriptions.clear();
                progressLL.setVisibility(View.GONE);
            }
        });
*/
    }

    private Subscription getPhotosSubscription(String maxId) {
        return instagramService.getPhotosList(
                user.id,
                maxId,
                100,
                token.isEmpty() ? BuildConfig.client_id : "",
                token)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<InstagramResponse<List<InstagramMedia>>>() {
                            @Override
                            public void call(final InstagramResponse<List<InstagramMedia>> instagramResponse) {
                                List<InstagramMedia> mediaList = instagramResponse.data;
                                List<InstagramPhoto> photos = new ArrayList<>();
                                for (InstagramMedia media : mediaList) {
                                    InstagramPhoto photo = new InstagramPhoto();
                                    photo.photo_id = media.id;
                                    photo.likes = media.likes.count;
                                    photo.thumbnail = media.images.thumbnail.url;
                                    photo.low_resolution = media.images.low_resolution.url;
                                    photo.link = media.link;
                                    photos.add(photo);
                                }
                                adapter.addPhotos(photos);
                                if (instagramResponse.pagination.next_max_id != null) {
                                    mSubscriptions.add(getPhotosSubscription(instagramResponse.pagination.next_max_id));
                                } else {
//                                    progressLL.setVisibility(View.GONE);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable throwable) {
                                Log.e(TAG, "error: ", throwable);
//                                progressLL.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    @Override
    protected void onStop() {
        super.onStop();
//        progressLL.setVisibility(View.GONE);
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

        if (id == R.id.nav_profile) {
            setFragment(new ProfileFragmentBuilder().build());
        } else if (id == R.id.nav_search) {
            setFragment(new SearchUserFragment());
        } else if (id == R.id.nav_share) {
            String shareBody = "Hey! Look at "
                    + "http://play.google.com/store/apps/details?id=" + getPackageName();
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "share app"));
        } else if (id == R.id.nav_rate) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        } else if (id == R.id.nav_logout) {
            preferenceManager.saveToken("");
            preferenceManager.saveUser(null);
            setFragment(new SearchUserFragment());
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

