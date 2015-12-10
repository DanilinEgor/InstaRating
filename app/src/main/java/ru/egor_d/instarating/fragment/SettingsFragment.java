package ru.egor_d.instarating.fragment;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.egor_d.instarating.App;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.R;
import ru.egor_d.instarating.activity.MainActivity;

public class SettingsFragment extends Fragment {
    @Inject
    InstagramRatingPreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        App.getInstance().component().inject(this);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.settings_logout)
    public void onLogoutClick() {
        preferenceManager.saveToken("");
        preferenceManager.saveUser(null);
        ((MainActivity) getActivity()).setFragment(new SearchUserFragment());
    }

    @OnClick(R.id.settings_share)
    public void onShareClick() {
        String shareBody = "Hey! Look at "
                + "http://play.google.com/store/apps/details?id=" + getActivity().getPackageName();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "share app"));
    }

    @OnClick(R.id.settings_rate)
    public void onRateClick() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }
    }
}
