package ru.egor_d.instarating.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.BuildConfig;
import ru.egor_d.instarating.DividerItemDecoration;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.ProfileView;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.api.IInstagramService;
import ru.egor_d.instarating.model.InstagramResponse;
import ru.egor_d.instarating.model.InstagramUser;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SearchUserActivity extends Activity {
    private final static String TAG = SearchUserActivity.class.getSimpleName();

    @Bind(R.id.activity_search_users_list)
    RecyclerView usersList;
    @Bind(R.id.activity_search_edit_text)
    EditText mUsernameEditText;
    @Bind(R.id.activity_search_login_button)
    Button loginButton;
    @Bind(R.id.activity_search_profile_view)
    ProfileView profileView;

    public final static String USER = "user";
    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private UsersAdapter adapter;
    private String token = "";

    @Inject
    IInstagramService instagramService;
    @Inject
    Picasso picasso;
    @Inject
    InstagramRatingPreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().component().inject(this);
        setContentView(R.layout.activity_search_user);
        ButterKnife.bind(this);
        token = preferenceManager.getToken();
        if (token.isEmpty()) {
            loginButton.setVisibility(View.VISIBLE);
            profileView.setVisibility(View.GONE);
        } else {
            loginButton.setVisibility(View.GONE);
            profileView.setVisibility(View.VISIBLE);
            InstagramUser me = preferenceManager.getUser();
            getMe(token);
            if (me.username != null) {
                profileView.bindUser(me);
            }
        }
        adapter = new UsersAdapter();
        usersList.setAdapter(adapter);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        usersList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 1) {
                    mSubscriptions.add(instagramService.getUserId(s.toString().trim(),
                            token.isEmpty() ? BuildConfig.client_id : "",
                            token)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .subscribe(new Action1<InstagramResponse<List<InstagramUser>>>() {
                                @Override
                                public void call(final InstagramResponse<List<InstagramUser>> listInstagramResponse) {
                                    adapter.setUsers(listInstagramResponse.data);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(final Throwable e) {
                                    Log.e(TAG, e.getMessage(), e);
                                    Toast.makeText(SearchUserActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                }
                            }));
                } else {
                    adapter.setUsers(new ArrayList<InstagramUser>());
                }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivityForResult(new Intent(SearchUserActivity.this, LoginActivity.class), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        token = preferenceManager.getToken();
        if (!token.isEmpty()) {
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
                        profileView.setVisibility(View.VISIBLE);
                        profileView.bindUser(instagramUser);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        Log.e(TAG, "error: ", throwable);
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
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
                    Intent intent = new Intent(SearchUserActivity.this, ProfileActivity.class);
                    intent.putExtra(USER, user);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
}
