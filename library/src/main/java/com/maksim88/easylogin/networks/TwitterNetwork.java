package com.maksim88.easylogin.networks;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
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
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.lang.ref.WeakReference;

/**
 * Created by maksim on 14.02.16.
 */
public class TwitterNetwork extends SocialNetwork {

    private AccessToken accessToken;

    private WeakReference<TwitterLoginButton> loginButton;

    private boolean additionalEmailRequest;

    private Callback<TwitterSession> buttonCallback = new Callback<TwitterSession>() {

        @Override
        public void success(Result<TwitterSession> result) {
            TwitterSession session = result.data;
            TwitterAuthToken authToken = session.getAuthToken();
            String token = authToken.token;
            String secret = authToken.secret;
            AccessToken tempToken = new AccessToken.Builder(token)
                    .secret(secret)
                    .userName(session.getUserName())
                    .userId(String.valueOf(session.getUserId()))
                    .build();
            if (additionalEmailRequest) {
                requestEmail(session, tempToken);
            } else {
                accessToken = tempToken;
                callLoginSuccess();
            }

        }

        @Override
        public void failure(TwitterException e) {
            callLoginFailure(e.getMessage());
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

    public void setAdditionalEmailRequest(boolean additionalEmailRequest) {
        this.additionalEmailRequest = additionalEmailRequest;
    }

    private void requestLogin(TwitterLoginButton button) {
        loginButton = new WeakReference<>(button);
        loginButton.get().setCallback(buttonCallback);
    }


    @Override
    public void logout() {
        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession != null) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            loginButton.get().setEnabled(true);
        }
    }

    @Override
    public com.maksim88.easylogin.AccessToken getAccessToken() {
        return accessToken;
    }

    @Override
    public Network getNetwork() {
        return Network.TWITTER;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (loginButton != null && loginButton.get() != null) {
            loginButton.get().onActivityResult(requestCode, resultCode, data);
        }
    }

    private void callLoginSuccess() {
        loginButton.get().setEnabled(false);
        listener.onLoginSuccess(getNetwork());
    }

    private void callLoginFailure(final String errorMessage) {
        loginButton.get().setEnabled(true);
        listener.onError(getNetwork(), errorMessage);
    }

    private void requestEmail(final TwitterSession session, final AccessToken tempToken) {
        TwitterAuthClient authClient = new TwitterAuthClient();
        authClient.requestEmail(session, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                final String email = result.data;
                if (TextUtils.isEmpty(email)) {
                    logout();
                    callLoginFailure("Before fetching an email, ensure that 'Request email addresses from users' is checked for your Twitter app.");
                    return;
                }
                accessToken = new AccessToken.Builder(tempToken).email(email).build();
                callLoginSuccess();
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e("TwitterNetwork", "Before fetching an email, ensure that 'Request email addresses from users' is checked for your Twitter app.");
                callLoginFailure(exception.getMessage());
            }
        });
    }
}
