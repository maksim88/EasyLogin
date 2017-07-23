package com.maksim88.easylogin.networks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.Utility;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by maksim on 14.02.16.
 */
public class FacebookNetwork extends SocialNetwork {

    private static final String EMAIL_PERMISSION_FIELD = "email";
    private static final String NAME_FIELD = "name";

    private WeakReference<Activity> activity;

    private CallbackManager callbackManager;

    private List<String> permissions;

    private com.maksim88.easylogin.AccessToken accessToken;

    private FacebookCallback<LoginResult> loginCallback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {

            final AccessToken fbAccessToken = loginResult.getAccessToken();

            if (permissions.contains(EMAIL_PERMISSION_FIELD)) {
                addEmailToToken(fbAccessToken);
                return;
            }

            String token = fbAccessToken.getToken();
            String userId = fbAccessToken.getUserId();
            accessToken = new com.maksim88.easylogin.AccessToken.Builder(token)
                    .userId(userId)
                    .build();
            listener.onLoginSuccess(getNetwork());
        }

        @Override
        public void onCancel() {
            listener.onError(getNetwork(), null);
        }

        @Override
        public void onError(FacebookException error) {
            listener.onError(getNetwork(), error.getMessage());
        }
    };

    public FacebookNetwork(Activity activity, List<String> permissions) {
        this.activity = new WeakReference<>(activity);
        callbackManager = CallbackManager.Factory.create();
        String applicationID = Utility.getMetadataApplicationId(this.activity.get());
        this.permissions = permissions;

        if (applicationID == null) {
            throw new IllegalStateException("applicationID can't be null\n" +
                    "Please check https://developers.facebook.com/docs/android/getting-started/");
        }
    }

    @Override
    public boolean isConnected() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    @Override
    public void requestLogin(OnLoginCompleteListener onLoginCompleteListener) {
        setListener(onLoginCompleteListener);
        LoginManager.getInstance().logInWithReadPermissions(activity.get(), permissions);
        LoginManager.getInstance().registerCallback(callbackManager, loginCallback);
    }

    public void requestLogin(LoginButton button, OnLoginCompleteListener onLoginCompleteListener) {
        setListener(onLoginCompleteListener);
        button.setReadPermissions(permissions);
        button.registerCallback(callbackManager, loginCallback);
    }

    @Override
    public void logout() {
        LoginManager.getInstance().logOut();
    }

    @Override
    public com.maksim88.easylogin.AccessToken getAccessToken() {
        if (com.facebook.AccessToken.getCurrentAccessToken() != null && accessToken == null) {
            AccessToken facebookToken = AccessToken.getCurrentAccessToken();
            accessToken = new com.maksim88.easylogin.AccessToken.Builder(facebookToken.getToken()).userId(facebookToken.getUserId()).build();
        }
        return accessToken;
    }

    @Override
    public Network getNetwork() {
        return Network.FACEBOOK;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void addEmailToToken(final AccessToken fbAccessToken) {
        GraphRequest meRequest = GraphRequest.newMeRequest(
                fbAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {
                        final String token = fbAccessToken.getToken();
                        final String userId = fbAccessToken.getUserId();
                        if (response.getError() != null) {
                            Log.d("FacebookNetwork", "Error occurred while fetching Facebook email");
                            accessToken = new com.maksim88.easylogin.AccessToken.Builder(token)
                                    .userId(userId)
                                    .build();
                            listener.onLoginSuccess(getNetwork());
                        } else {
                            final String email = me.optString(EMAIL_PERMISSION_FIELD);
                            final String name = me.optString(NAME_FIELD);
                            if (TextUtils.isEmpty(email)) {
                                Log.d("FacebookNetwork", "Email could not be fetched. The user might not have an email or have unchecked the checkbox while connecting.");
                            }
                            accessToken = new com.maksim88.easylogin.AccessToken.Builder(token)
                                    .userId(userId)
                                    .email(email)
                                    .userName(name)
                                    .build();
                            listener.onLoginSuccess(getNetwork());
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", NAME_FIELD + "," + EMAIL_PERMISSION_FIELD);
        meRequest.setParameters(parameters);
        meRequest.executeAsync();
    }
}
