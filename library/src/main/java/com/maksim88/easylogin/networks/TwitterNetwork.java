package com.maksim88.easylogin.networks;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.maksim88.easylogin.AccessToken;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.lang.ref.WeakReference;

/**
 * Created by maksim on 14.02.16.
 */
public class TwitterNetwork extends SocialNetwork {

    private com.maksim88.easylogin.AccessToken mAccessToken;

    private WeakReference<TwitterLoginButton> mLoginButton;

    private Callback<TwitterSession> mButtonCallback = new Callback<TwitterSession>() {

        @Override
        public void success(Result<TwitterSession> result) {
            TwitterSession session = result.data;
            TwitterAuthToken authToken = session.getAuthToken();
            String token = authToken.token;
            String secret = authToken.secret;
            mAccessToken = new AccessToken.Builder(token)
                    .secret(secret)
                    .userName(session.getUserName())
                    .userId(String.valueOf(session.getUserId()))
                    .build();
            mLoginButton.get().setEnabled(false);
            mListener.onLoginSuccess(getNetwork());
        }

        @Override
        public void failure(TwitterException e) {
            mLoginButton.get().setEnabled(true);
            mListener.onError(getNetwork(), e.getMessage());
        }
    };

    public TwitterNetwork(Activity activity, String consumerKey, String consumerSecret) {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(consumerKey, consumerSecret);
        TwitterConfig config = new TwitterConfig.Builder(activity.getApplicationContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(authConfig)
                .build();
        Twitter.initialize(config);
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
        setListener(onLoginCompleteListener);
        requestLogin(button);
    }

    private void requestLogin(TwitterLoginButton button) {
        mLoginButton = new WeakReference<>(button);
        mLoginButton.get().setCallback(mButtonCallback);
    }


    @Override
    public void logout() {
        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession != null) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            mLoginButton.get().setEnabled(true);
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
        if (mLoginButton != null && mLoginButton.get() != null) {
            mLoginButton.get().onActivityResult(requestCode, resultCode, data);
        }
    }
}
