package com.maksim88.easylogin.networks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.maksim88.easylogin.listener.OnLoginCompleteListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by maksim on 14.02.16.
 */
public class TwitterNetwork extends SocialNetwork {

    private com.maksim88.easylogin.AccessToken mAccessToken;

    private TwitterLoginButton mLoginButton;

    private Callback<TwitterSession> mButtonCallback = new Callback<TwitterSession>() {

        @Override
        public void success(Result<TwitterSession> result) {
            TwitterSession session = result.data;
            TwitterAuthToken authToken = session.getAuthToken();
            String token = authToken.token;
            String secret = authToken.secret;
            mAccessToken = new com.maksim88.easylogin.AccessToken(token, secret);
            if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
                ((OnLoginCompleteListener) mLocalListeners.get(REQUEST_LOGIN)).onLoginSuccess(getNetwork());
                mLocalListeners.remove(REQUEST_LOGIN);
            }
        }

        @Override
        public void failure(TwitterException e) {
            if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
                mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN, e.getMessage(), null);
                mLocalListeners.remove(REQUEST_LOGIN);
            }
        }
    };

    public TwitterNetwork(Activity activity, String consumerKey, String consumerSecret) {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(consumerKey, consumerSecret);
        Fabric.with(activity, new Twitter(authConfig));
    }

    @Override
    public boolean isConnected() {
       return TwitterCore.getInstance().getSessionManager().getActiveSession() != null;
    }

    @Override
    public void requestLogin(OnLoginCompleteListener onLoginCompleteListener) {
        throw new RuntimeException("Call requestLogin() with the TwitterLoginButton reference!");
    }

    public void requestLogin(TwitterLoginButton button, OnLoginCompleteListener onLoginCompleteListener) {
        super.requestLogin(onLoginCompleteListener);
        requestLogin(button);
    }

    private void requestLogin(TwitterLoginButton button) {
        mLoginButton = button;
        mLoginButton.setCallback(mButtonCallback);
    }


    @Override
    public void logout() {
        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession != null) {
            clearCookies(getApplicationContext());
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }
    }

    @Override
    public com.maksim88.easylogin.AccessToken getAccessToken() {
        return mAccessToken;
    }

    @Override
    public Network getNetwork() {
        return Network.TWITTER;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mLoginButton != null) {
            mLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("deprecation")
    private void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}
