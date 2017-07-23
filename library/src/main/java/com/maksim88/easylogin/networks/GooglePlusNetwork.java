package com.maksim88.easylogin.networks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.maksim88.easylogin.AccessToken;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;

import java.lang.ref.WeakReference;

/**
 * Created by maksim on 14.02.16.
 */
public class GooglePlusNetwork extends SocialNetwork implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String GPLUS_CONNECTED = "google_plus_connected";

    private static final int REQUEST_AUTH = 1337;

    private GoogleApiClient googleApiClient;

    private AccessToken accessToken;

    private SharedPreferences sharedPrefs;

    private WeakReference<Activity> activity;

    private WeakReference<View> signInButton;

    public GooglePlusNetwork(Activity activity) {

        sharedPrefs = activity.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        this.activity = new WeakReference<>(activity);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleApiClient.Builder googleApiBuilder = new GoogleApiClient.Builder(activity);
        if (activity instanceof FragmentActivity) {
            googleApiBuilder.enableAutoManage((FragmentActivity) activity, this);
        } else {
            googleApiBuilder
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this);
        }
        googleApiClient = googleApiBuilder
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public boolean isConnected() {
        // GoogleApiClient behaves weirdly with isConnected(), so let's save our own state
        return sharedPrefs.getBoolean(GPLUS_CONNECTED, false);
    }

    public void silentSignIn() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (pendingResult.isDone()) {
            // There's immediate result available.
            parseGoogleSignInResult(pendingResult.get());
        } else {
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    parseGoogleSignInResult(result);
                }
            });
        }
    }

    /**
     * Use this method if you cannot use autoManage from GoogleApiClient to connect in onStart().
     * The easier approach is to pass a FragmentActivity in the Network and let GoogleApiClient manage itself.
     */
    public void connectGoogleApiClient() {
        googleApiClient.connect();
    }

    /**
     * Use this method if you cannot use autoManage from GoogleApiClient to disconnect in onStop().
     * The easier approach is to pass a FragmentActivity in the Network and let GoogleApiClient manage itself.
     */
    public void disconnectGoogleApiClient() {
        googleApiClient.disconnect();
    }

    @Override
    public void logout() {
        if (isConnected()) {
            Auth.GoogleSignInApi.signOut(googleApiClient);
            googleApiClient.disconnect();
            setSignInButtonEnabled(true);
            sharedPrefs.edit().putBoolean(GPLUS_CONNECTED, false).apply();
        }
    }

    @Override
    public Network getNetwork() {
        return Network.GOOGLE_PLUS;
    }

    @Nullable
    @Override
    public AccessToken getAccessToken() {
        return accessToken;
        //throw new UnsupportedOperationException("Google Plus does not provide an accessToken, use either requestLogin() or silentSignIn() if already connected");
    }

    /**
     * Make login request - authorize in Google plus social network
     *
     * @param onLoginCompleteListener listener for login complete
     */
    @Override
    public void requestLogin(OnLoginCompleteListener onLoginCompleteListener) {
        setListener(onLoginCompleteListener);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
         activity.get().startActivityForResult(signInIntent, REQUEST_AUTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUTH) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        sharedPrefs.edit().putBoolean(GPLUS_CONNECTED, true).apply();
        setSignInButtonEnabled(false);
        listener.onLoginSuccess(getNetwork());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        sharedPrefs.edit().putBoolean(GPLUS_CONNECTED, false).apply();
        setSignInButtonEnabled(true);
        listener.onError(getNetwork(), getStatusCodeString(cause));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        sharedPrefs.edit().putBoolean(GPLUS_CONNECTED, false).apply();
        setSignInButtonEnabled(true);
        listener.onError(getNetwork(), getStatusCodeString(connectionResult.getErrorCode()));
    }

    public void setSignInButton(View button) {
        signInButton = new WeakReference<>(button);
        signInButton.get().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected()) {
                    requestLogin(listener);
                } else {
                    silentSignIn();
                }
            }
        });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        parseGoogleSignInResult(result);
    }

    private void parseGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            sharedPrefs.edit().putBoolean(GPLUS_CONNECTED, true).apply();
            setSignInButtonEnabled(false);
            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct != null) {
                accessToken = new AccessToken.Builder(acct.getId())
                        .email(acct.getEmail())
                        .userName(acct.getDisplayName())
                        .userId(acct.getId())
                        .build();
                listener.onLoginSuccess(getNetwork());
            }
        } else {
            if (result.getStatus().getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                if (listener != null) {
                    requestLogin(listener);
                }
                return;
            }
            sharedPrefs.edit().putBoolean(GPLUS_CONNECTED, false).apply();
            setSignInButtonEnabled(true);
            listener.onError(getNetwork(), getStatusCodeString(result.getStatus().getStatusCode()));
        }
    }

    private String getStatusCodeString(int statusCode) {
        return CommonStatusCodes.getStatusCodeString(statusCode);
    }

    private void setSignInButtonEnabled(boolean enabled) {
        if (signInButton != null && signInButton.get() != null) {
            signInButton.get().setEnabled(enabled);
        }
    }
}
