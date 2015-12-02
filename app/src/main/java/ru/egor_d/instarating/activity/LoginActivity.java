package ru.egor_d.instarating.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import ru.egor_d.instarating.App;
import ru.egor_d.instarating.BuildConfig;
import ru.egor_d.instarating.InstagramRatingPreferenceManager;
import ru.egor_d.instarating.R;

public class LoginActivity extends Activity {
    @Inject
    InstagramRatingPreferenceManager preferenceManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        App.getInstance().component().inject(this);

        String patternString = BuildConfig.redirect_uri + "/#access_token=.*";
        final Pattern pattern = Pattern.compile(patternString);

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.v("lol", url);
                if (url.startsWith(BuildConfig.redirect_uri)) {
                    Matcher m = pattern.matcher(url);
                    if (m.matches()) {
                        String parts[] = url.split("=");
                        preferenceManager.saveToken(parts[1]);
                        finish();
                    }
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(
                "https://instagram.com/oauth/authorize/?client_id="
                        + BuildConfig.client_id
                        + "&redirect_uri="
                        + BuildConfig.redirect_uri
                        + "&response_type=token"
        );
    }
}
