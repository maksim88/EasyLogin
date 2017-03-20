package com.maksim88.easylogin.networks;

import android.content.Intent;

import com.maksim88.easylogin.AccessToken;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;

/**
 * Created by maksim on 14.02.16.
 */
public abstract class SocialNetwork {

    static final String SHARED_PREFS_NAME = "easylogin_prefs";

    public enum Network {
        FACEBOOK, GOOGLE_PLUS, TWITTER
    }

    OnLoginCompleteListener mListener;

    /**
     * Check if selected social network connected: true or false
     * @return true if connected, else false
     */
    public abstract boolean isConnected();

    public abstract void requestLogin(OnLoginCompleteListener listener);

    public void setListener(OnLoginCompleteListener listener) {
        mListener = listener;
    }

    /**
     * Logout from social network
     */
    public abstract void logout();

    public abstract Network getNetwork();

    public abstract AccessToken getAccessToken();

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

}
