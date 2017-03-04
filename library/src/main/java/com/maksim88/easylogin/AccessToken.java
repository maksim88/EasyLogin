package com.maksim88.easylogin;

/**
 * Created by maksim on 14.02.16.
 */
public class AccessToken {

    private String mToken;

    private String mSecret;

    public AccessToken(String token, String secret) {
        mToken = token;
        mSecret = secret;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public String getSecret() {
        return mSecret;
    }

    public void setSecret(String secret) {
        mSecret = secret;
    }
}
