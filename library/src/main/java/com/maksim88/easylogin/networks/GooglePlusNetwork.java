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

    private GoogleApiClient mGoogleApiClient;

    private AccessToken mAccessToken;

    private SharedPreferences mSharedPrefs;

    private static final int REQUEST_AUTH = 1337;

    private WeakReference<Activity> mActivity;

    private WeakReference<View> mSignInButton;

    public GooglePlusNetwork(Activity activity) {

        mSharedPrefs = activity.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        mActivity = new WeakReference<>(activity);
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
        mGoogleApiClient = googleApiBuilder
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public boolean isConnected() {
        // GoogleApiClient behaves weirdly with isConnected(), so let's save our own state
        return mSharedPrefs.getBoolean(GPLUS_CONNECTED, false);
    }

    public void silentSignIn() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
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
        mGoogleApiClient.connect();
    }

    /**
     * Use this method if you cannot use autoManage from GoogleApiClient to disconnect in onStop().
     * The easier approach is to pass a FragmentActivity in the Network and let GoogleApiClient manage itself.
     */
    public void disconnectGoogleApiClient() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void logout() {
        if (isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            setSignInButtonEnabled(true);
            mSharedPrefs.edit().putBoolean(GPLUS_CONNECTED, false).apply();
        }
    }

    @Override
    public Network getNetwork() {
        return Network.GOOGLE_PLUS;
    }

    @Nullable
    @Override
    public AccessToken getAccessToken() {
        return mAccessToken;
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
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
         mActivity.get().startActivityForResult(signInIntent, REQUEST_AUTH);
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
        mSharedPrefs.edit().putBoolean(GPLUS_CONNECTED, true).apply();
        setSignInButtonEnabled(false);
        mListener.onLoginSuccess(getNetwork());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mSharedPrefs.edit().putBoolean(GPLUS_CONNECTED, false).apply();
        setSignInButtonEnabled(true);
        mListener.onError(getNetwork(), getStatusCodeString(cause));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mSharedPrefs.edit().putBoolean(GPLUS_CONNECTED, false).apply();
        setSignInButtonEnabled(true);
        mListener.onError(getNetwork(), getStatusCodeString(connectionResult.getErrorCode()));
    }

    public void setSignInButton(View button) {
        mSignInButton = new WeakReference<>(button);
        mSignInButton.get().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected()) {
                    requestLogin(mListener);
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
            mSharedPrefs.edit().putBoolean(GPLUS_CONNECTED, true).apply();
            setSignInButtonEnabled(false);
            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct != null) {
                mAccessToken = new AccessToken.Builder(acct.getId())
                        .email(acct.getEmail())
                        .userName(acct.getDisplayName())
                        .userId(acct.getId())
                        .build();
                mListener.onLoginSuccess(getNetwork());
            }
        } else {
            if (result.getStatus().getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                if (mListener != null) {
                    requestLogin(mListener);
                }
                return;
            }
            mSharedPrefs.edit().putBoolean(GPLUS_CONNECTED, false).apply();
            setSignInButtonEnabled(true);
            mListener.onError(getNetwork(), getStatusCodeString(result.getStatus().getStatusCode()));
        }
    }

    private String getStatusCodeString(int statusCode) {
        return CommonStatusCodes.getStatusCodeString(statusCode);
    }

    private void setSignInButtonEnabled(boolean enabled) {
        if (mSignInButton != null && mSignInButton.get() != null) {
            mSignInButton.get().setEnabled(enabled);
        }
    }
}
