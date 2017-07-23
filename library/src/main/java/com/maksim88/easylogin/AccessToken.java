package com.maksim88.easylogin;

/**
 * Created by maksim on 14.02.16.
 */
public class AccessToken {

    private final String token;
    private final String secret;
    private final String email;
    private final String userName;
    private final String userId;

    private AccessToken(Builder builder) {
        token = builder.token;
        secret = builder.secret;
        email = builder.email;
        userName = builder.userName;
        userId = builder.userId;
    }

    public String getToken() {
        return token;
    }

    public String getSecret() {
        return secret;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public static class Builder {

        private final String token;
        private String secret;
        private String userName;
        private String email;
        private String userId;

        public Builder(String token) {
            this.token = token;
        }

        public Builder(AccessToken oldToken) {
            token = oldToken.token;
            secret = oldToken.secret;
            email = oldToken.email;
            userName = oldToken.userName;
            userId = oldToken.userId;
        }

        public Builder secret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public AccessToken build() {
            return new AccessToken(this);
        }

    }
}
