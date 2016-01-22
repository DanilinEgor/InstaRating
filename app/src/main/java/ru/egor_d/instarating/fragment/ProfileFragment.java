package ru.egor_d.instarating.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

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

@FragmentWithArgs
public class ProfileFragment extends Fragment {
    @Arg(required = false)
    InstagramUser user;

    @Bind(R.id.activity_choose_recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.activity_choose_progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.activity_choose_cancel)
    View cancelButton;
    @Bind(R.id.activity_choose_progress_ll)
    View progressLL;
    @Bind(R.id.activity_choose_profile_view)
    ProfileView mProfileView;

    private final static String TAG = ProfileFragment.class.getSimpleName();

    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private String token = "";

    @Inject
    IInstagramService instagramService;
    @Inject
    InstagramRatingPreferenceManager preferenceManager;

    private InstagramPhotosAdapter adapter;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArgs.inject(this);
        App.getInstance().component().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        token = preferenceManager.getToken();
        mProfileView.setVisibility(View.VISIBLE);

        if (user == null) {
            // opened my profile before me is loaded
            getMe();
        } else {
            mProfileView.bindUser(user);
            mSubscriptions.add(
                    instagramService
                            .getUser(user.id, token) // TODO: если токен пустой, то пихать client_id
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

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new InstagramPhotosAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.setItemAnimator(new LandingAnimator());

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mSubscriptions.clear();
                progressLL.setVisibility(View.GONE);
            }
        });

        return view;
    }

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
                        preferenceManager.saveUser(instagramUser);
                        user = instagramUser;
                        mProfileView.bindUser(instagramUser);
                        mSubscriptions.add(getPhotosSubscription(""));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        Log.e(TAG, "error:", throwable);
                    }
                }));
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
                                    progressLL.setVisibility(View.GONE);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable throwable) {
                                Log.e(TAG, "error: ", throwable);
                                progressLL.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    @Override
    public void onStop() {
        super.onStop();
        progressLL.setVisibility(View.GONE);
        mSubscriptions.unsubscribe();
    }
}
