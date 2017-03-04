package com.maksim88.easylogin.networks;

import android.app.Activity;
import android.content.Intent;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.Utility;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by maksim on 14.02.16.
 */
public class FacebookNetwork extends SocialNetwork {

    private WeakReference<Activity> mActivity;
    private CallbackManager mCallbackManager;
    private List<String> mPermissions;

    private com.maksim88.easylogin.AccessToken mAccessToken;

    private FacebookCallback<LoginResult> LoginCallback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {
            mAccessToken = new com.maksim88.easylogin.AccessToken(loginResult.getAccessToken().getToken(), null);
            if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
                ((OnLoginCompleteListener) mLocalListeners.get(REQUEST_LOGIN)).onLoginSuccess(getNetwork());
                mLocalListeners.remove(REQUEST_LOGIN);
            }
        }

        @Override
        public void onCancel() {
            if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
                mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN, null, null);
                mLocalListeners.remove(REQUEST_LOGIN);
            }

        }

        @Override
        public void onError(FacebookException error) {
            if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
                mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN, error.getMessage(), null);
                mLocalListeners.remove(REQUEST_LOGIN);
            }
        }
    };

    public FacebookNetwork(Activity activity, List<String> permissions) {
        mActivity = new WeakReference<>(activity);
        mCallbackManager = CallbackManager.Factory.create();
        String applicationID = Utility.getMetadataApplicationId(mActivity.get());
        mPermissions = permissions;

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
        super.requestLogin(onLoginCompleteListener);
        LoginManager.getInstance().logInWithReadPermissions(mActivity.get(), mPermissions);
        LoginManager.getInstance().registerCallback(mCallbackManager, LoginCallback);
    }

    public void requestLogin(LoginButton button, OnLoginCompleteListener onLoginCompleteListener) {
        super.requestLogin(onLoginCompleteListener);
        button.setReadPermissions(mPermissions);
        button.registerCallback(mCallbackManager, LoginCallback);
    }

    @Override
    public void logout() {
        LoginManager.getInstance().logOut();
    }

    @Override
    public com.maksim88.easylogin.AccessToken getAccessToken() {
        if(com.facebook.AccessToken.getCurrentAccessToken() != null) {
            mAccessToken = new com.maksim88.easylogin.AccessToken(com.facebook.AccessToken.getCurrentAccessToken().getToken(), null);
        }
        return mAccessToken;
    }

    @Override
    public Network getNetwork() {
        return Network.FACEBOOK;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
