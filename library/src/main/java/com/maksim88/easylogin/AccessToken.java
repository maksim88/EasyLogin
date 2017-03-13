package com.maksim88.easylogin;

/**
 * Created by maksim on 14.02.16.
 */
public class AccessToken {

    private final String mToken;
    private final String mSecret;
    private final String mEmail;
    private final String mUserName;
    private final String mUserId;

    private AccessToken(Builder builder) {
        mToken = builder.mToken;
        mSecret = builder.mSecret;
        mEmail = builder.mEmail;
        mUserName = builder.mUserName;
        mUserId = builder.mUserId;
    }

    public String getToken() {
        return mToken;
    }

    public String getSecret() {
        return mSecret;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUserId() {
        return mUserId;
    }

    public static class Builder {

        private final String mToken;

        private String mSecret;

        private String mUserName;

        private String mEmail;

        private String mUserId;

        public Builder(String token) {
            mToken = token;
        }

        public Builder secret(String secret) {
            mSecret = secret;
            return this;
        }

        public Builder userName(String userName) {
            mUserName = userName;
            return this;
        }

        public Builder userId(String userId) {
            mUserId = userId;
            return this;
        }

        public Builder email(String email) {
            mEmail = email;
            return this;
        }

        public AccessToken build() {
            return new AccessToken(this);
        }

    }
}
