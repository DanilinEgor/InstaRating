package ru.egor_d.instarating.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.BuildConfig;
import ru.egor_d.instarating.DividerItemDecoration;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.activity.LoginActivity;
import ru.egor_d.instarating.activity.MainActivity;
import ru.egor_d.instarating.api.IInstagramService;
import ru.egor_d.instarating.model.InstagramResponse;
import ru.egor_d.instarating.model.InstagramUser;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SearchUserFragment extends Fragment {
    private final static String TAG = SearchUserFragment.class.getSimpleName();

    @Bind(R.id.activity_search_users_list)
    RecyclerView usersList;
    @Bind(R.id.activity_search_edit_text)
    EditText mUsernameEditText;
    @Bind(R.id.activity_search_login_button)
    View loginButton;

    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private UsersAdapter adapter;
    private String token = "";

    @Inject
    IInstagramService instagramService;
    @Inject
    Picasso picasso;
    @Inject
    InstagramRatingPreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_search_user, container, false);
        ButterKnife.bind(this, view);
        token = preferenceManager.getToken();
        if (token.isEmpty()) {
            ((MainActivity) getActivity()).setMenuVisibility(MainActivity.MenuMode.NONE);
            loginButton.setVisibility(View.VISIBLE);
        } else {
            ((MainActivity) getActivity()).setMenuVisibility(MainActivity.MenuMode.ALL);
            loginButton.setVisibility(View.GONE);
            getMe(token);
        }
        adapter = new UsersAdapter();
        usersList.setAdapter(adapter);
        usersList.setLayoutManager(new LinearLayoutManager(getActivity()));
        usersList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mSubscriptions.add(RxTextView.textChanges(mUsernameEditText)
                .debounce(150, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(final CharSequence charSequence) {
                        if (charSequence.length() > 1) {
                            return true;
                        } else {
                            adapter.setUsers(new ArrayList<InstagramUser>());
                            return false;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .switchMap(new Func1<CharSequence, Observable<InstagramResponse<List<InstagramUser>>>>() {
                    @Override
                    public Observable<InstagramResponse<List<InstagramUser>>> call(final CharSequence s) {
                        return instagramService.getUserId(s.toString().trim(),
                                token.isEmpty() ? BuildConfig.client_id : "",
                                token)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .unsubscribeOn(Schedulers.io());
                    }
                })
                .subscribe(new Action1<InstagramResponse<List<InstagramUser>>>() {
                    @Override
                    public void call(final InstagramResponse<List<InstagramUser>> listInstagramResponse) {
                        adapter.setUsers(listInstagramResponse.data);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable e) {
                        Log.e(TAG, "error: ", e);
                        Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivityForResult(new Intent(getActivity(), LoginActivity.class), 1);
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().component().inject(this);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        token = preferenceManager.getToken();
        if (!token.isEmpty()) {
            ((MainActivity) getActivity()).setMenuVisibility(MainActivity.MenuMode.ALL);
            loginButton.setVisibility(View.GONE);
            getMe(token);
        }
    }

    private void getMe(final String token) {
        mSubscriptions.add(instagramService.getMe(token)
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
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        Log.e(TAG, "error: ", throwable);
                    }
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        mSubscriptions.clear();
    }

    protected class VH extends RecyclerView.ViewHolder {
        @Bind(R.id.item_users_list_username)
        TextView username;
        @Bind(R.id.item_users_list_name)
        TextView name;
        @Bind(R.id.item_users_list_avatar)
        ImageView avatar;
        @Bind(R.id.item_users_list_root_ll)
        View root;

        public VH(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class UsersAdapter extends RecyclerView.Adapter<VH> {
        private List<InstagramUser> users = new ArrayList<>();

        public void setUsers(List<InstagramUser> newUsers) {
            users.clear();
            users.addAll(newUsers);
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(final ViewGroup parent, final int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_users_list, parent, false));
        }

        @Override
        public void onBindViewHolder(final VH holder, final int position) {
            final InstagramUser user = users.get(position);
            holder.username.setText(user.username);
            holder.name.setText(user.full_name);
            picasso.load(user.profile_picture)
                    .transform(new CropCircleTransformation())
                    .into(holder.avatar);

            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ((MainActivity) getActivity()).setFragmentWithBackStack(
                            new ProfileFragmentBuilder().user(user).build()
                    );
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
}
