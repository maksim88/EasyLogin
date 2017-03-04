package com.maksim88.easylogin.networks;

import android.content.Intent;

import com.maksim88.easylogin.AccessToken;
import com.maksim88.easylogin.listener.NetworkListener;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maksim on 14.02.16.
 */
public abstract class SocialNetwork {

    static final String REQUEST_LOGIN = "SocialNetwork.REQUEST_LOGIN";

    public enum Network {
        FACEBOOK, GOOGLE_PLUS, TWITTER
    }

    Map<String, NetworkListener> mLocalListeners = new HashMap<>();

    private Map<String, NetworkListener> mGlobalListeners = new HashMap<>();


    /**
     * Check if selected social network connected: true or false
     * @return true if connected, else false
     */
    public abstract boolean isConnected();

    /**
     * Login to social network using global listener
     */
    public void requestLogin() {
        requestLogin(null);
    }

    /**
     * Login to social network using local listener
     * @param onLoginCompleteListener listener for login complete
     */
    public void requestLogin(OnLoginCompleteListener onLoginCompleteListener) {
        if (isConnected()) {
            throw new RuntimeException("Already connected, please check isConnected() method");
        }
        registerListener(REQUEST_LOGIN, onLoginCompleteListener);
    }

    /**
     * Logout from social network
     */
    public abstract void logout();

    public abstract Network getNetwork();

    public abstract AccessToken getAccessToken();

    private void registerListener(String listenerID, NetworkListener networkListener) {
        if (networkListener != null) {
            mLocalListeners.put(listenerID, networkListener);
        } else {
            mLocalListeners.put(listenerID, mGlobalListeners.get(listenerID));
        }
    }

    public void setOnLoginCompleteListener(OnLoginCompleteListener onLoginCompleteListener) {
        mGlobalListeners.put(REQUEST_LOGIN, onLoginCompleteListener);
    }

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

}
