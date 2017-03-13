package com.maksim88.easylogin.networks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.maksim88.easylogin.AccessToken;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;

import java.lang.ref.WeakReference;

/**
 * Created by maksim on 14.02.16.
 */
public class GooglePlusNetwork extends SocialNetwork implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private AccessToken mAccessToken;

    private static final int REQUEST_AUTH = 13;


    private WeakReference<FragmentActivity> mActivity;

    public GooglePlusNetwork(FragmentActivity activity) {
        mActivity = new WeakReference<>(activity);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(mActivity.get(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public boolean isConnected() {
       return mGoogleApiClient.isConnected();
    }

    @Override
    public void logout() {
        if (isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        }
    }

    @Override
    public Network getNetwork() {
        return Network.GOOGLE_PLUS;
    }

    @Override
    public AccessToken getAccessToken() {
        return mAccessToken;
    }

    /**
     * Make login request - authorize in Google plus social network
     * @param onLoginCompleteListener listener for login complete
     */
    @Override
    public void requestLogin(OnLoginCompleteListener onLoginCompleteListener) {
        super.requestLogin(onLoginCompleteListener);
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

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                mAccessToken = new AccessToken.Builder(acct.getId())
                        .email(acct.getEmail())
                        .userName(acct.getDisplayName())
                        .userId(acct.getId())
                        .build();
                if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
                    ((OnLoginCompleteListener) mLocalListeners.get(REQUEST_LOGIN)).onLoginSuccess(getNetwork());
                    mLocalListeners.remove(REQUEST_LOGIN);
                }
            }
        } else {
            // Signed out, show unauthenticated UI.
            if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
                mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN,  result.getStatus().getStatusMessage());
                mLocalListeners.remove(REQUEST_LOGIN);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
            ((OnLoginCompleteListener) mLocalListeners.get(REQUEST_LOGIN)).onLoginSuccess(getNetwork());
            mLocalListeners.remove(REQUEST_LOGIN);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
            mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN, null);
            mLocalListeners.remove(REQUEST_LOGIN);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
            mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN, connectionResult.getErrorMessage());
            mLocalListeners.remove(REQUEST_LOGIN);
        }
    }
}
