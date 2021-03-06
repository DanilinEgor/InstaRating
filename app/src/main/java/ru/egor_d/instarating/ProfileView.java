package ru.egor_d.instarating;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import ru.egor_d.instarating.model.InstagramUser;

public class ProfileView extends LinearLayout {
    @Bind(R.id.profile_view_avatar)
    ImageView avatar;
    @Bind(R.id.profile_view_username)
    TextView username;
    @Bind(R.id.profile_view_posts_count)
    TextView postsCount;
    @Inject
    Picasso picasso;

    public ProfileView(final Context context) {
        super(context);
        if (!isInEditMode()) {
            init();
        }
    }

    public ProfileView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    public ProfileView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProfileView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {
        inflate(getContext(), R.layout.view_profile, this);
        ButterKnife.bind(this);
        App.getInstance().component().inject(this);
    }

    public void bindUser(final InstagramUser user) {
        picasso.load(user.profile_picture).transform(new CropCircleTransformation()).into(avatar);
        username.setText(user.username);
        if (user.counts != null) {
            postsCount.setText(String.valueOf(user.counts.media));
        } else {
            postsCount.setText("-");
        }
    }
}
