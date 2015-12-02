package ru.egor_d.instarating.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.BuildConfig;
import ru.egor_d.instarating.InstagramPhotosAdapter;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.ProfileView;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.api.IInstagramService;
import ru.egor_d.instarating.model.InstagramMedia;
import ru.egor_d.instarating.model.InstagramPhoto;
import ru.egor_d.instarating.model.InstagramResponse;
import ru.egor_d.instarating.model.InstagramUser;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ProfileActivity extends Activity {
    private final static String TAG = ProfileActivity.class.getSimpleName();

    @Bind(R.id.activity_choose_recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.activity_choose_progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.activity_choose_profile_view)
    ProfileView profileView;

    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private String token = "";

    @Inject
    IInstagramService instagramService;
    @Inject
    InstagramRatingPreferenceManager preferenceManager;

    private InstagramPhotosAdapter adapter;
    private InstagramUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose);
        App.getInstance().component().inject(this);
        ButterKnife.bind(this);

        token = preferenceManager.getToken();

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new InstagramPhotosAdapter(this);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.setItemAnimator(new LandingAnimator());

        user = getIntent().getParcelableExtra(SearchUserActivity.USER);
        profileView.bindUser(user);
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
                                profileView.bindUser(instagramUser);
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

    private Subscription getPhotosSubscription(String maxId) {
        return instagramService.getPhotosList(
                user.id,
                maxId,
                100,
                token.isEmpty() ? BuildConfig.client_id : "",
                token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
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
                                if (instagramResponse.pagination.next_url != null) {
                                    mSubscriptions.add(getPhotosSubscription(instagramResponse.pagination.next_max_id));
                                } else {
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable throwable) {
                                Log.e(TAG, "error: ", throwable);
                                mProgressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgressBar.setVisibility(View.GONE);
        mSubscriptions.unsubscribe();
    }
}
