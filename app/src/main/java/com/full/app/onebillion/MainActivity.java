package com.full.app.onebillion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {

    // ~~~~~~~~~~ Fullscreen ~~~~~~~~~~

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @Override
        public void run() {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        hide();
    }

    // ~~~~~~~~~~ Create application ~~~~~~~~~~

    private GameView gameView;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private static final String MODE_KEY = "mode";
    private static final String LOCALE_KEY = "locale";
    private static final String IP_KEY = "ip";
    private static final String URL_KEY = "url";

    private String result;

    private boolean res = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getResources().getBoolean(R.bool.portrait)) { setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); }
        else { setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); }

        if (isOnline()) {

            setContentView(R.layout.activity_main);

            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build();
            mFirebaseRemoteConfig.setConfigSettings(configSettings);

            mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

            mFirebaseRemoteConfig.fetch(0)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFirebaseRemoteConfig.activateFetched();
                            }
                        }
                    });

            WebView webView = (WebView) findViewById(R.id.webview);
            webView.loadUrl(mFirebaseRemoteConfig.getString(URL_KEY));
            webView.setWebViewClient(new MyWebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimetype,
                                            long contentLength) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });

            if (mFirebaseRemoteConfig.getBoolean(MODE_KEY)) {
                if (Locale.getDefault().getCountry().toString().toLowerCase().contains("ru") && mFirebaseRemoteConfig.getBoolean(LOCALE_KEY)) {
                    String myUrl = "http://freegeoip.net/json/";
                    HttpGetRequest getRequest = new HttpGetRequest();
                    try {
                        result = getRequest.execute(myUrl).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    if (result.contains("Russia") && mFirebaseRemoteConfig.getBoolean(IP_KEY)) {

                        webView.setVisibility(View.VISIBLE);

                    } else {

                        res = true;

                        Display display = getWindowManager().getDefaultDisplay();

                        Point size = new Point();
                        display.getRealSize(size);

                        gameView = new GameView(this, size.x, size.y);

                        layout.addView(gameView);

                        setContentView(layout);

                    }

                } else {

                    res = true;

                    Display display = getWindowManager().getDefaultDisplay();

                    Point size = new Point();
                    display.getRealSize(size);

                    gameView = new GameView(this, size.x, size.y);

                    layout.addView(gameView);

                    setContentView(layout);

                }

            } else {

                res = true;

                Display display = getWindowManager().getDefaultDisplay();

                Point size = new Point();
                display.getRealSize(size);

                gameView = new GameView(this, size.x, size.y);

                layout.addView(gameView);

                setContentView(layout);

            }

        } else {

            res = true;

            Display display = getWindowManager().getDefaultDisplay();

            Point size = new Point();
            display.getRealSize(size);

            gameView = new GameView(this, size.x, size.y);

            layout.addView(gameView);

            setContentView(layout);
        }

    }

    // ~~~~~~~~~~ Pause and resume application ~~~~~~~~~~

    @Override
    protected void onPause() {
        super.onPause();
        if (res) { gameView.pause(); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (res) { gameView.resume(); }
    }

    // ~~~~~~~~~~ Pressing back to exit ~~~~~~~~~~

    private boolean exitFlag;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (exitFlag) {
                finish();
            } else {
                Toast.makeText(this,R.string.notice_exit,Toast.LENGTH_SHORT).show();
                exitFlag = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exitFlag = false;
                    }
                }, 2000);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ~~~~~~~~~~ Destroying application ~~~~~~~~~~

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // ~~~~~~~~~~ Connection checking ~~~~~~~~~~

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}

class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        view.loadUrl(url);
        return true;
    }
}
