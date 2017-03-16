package com.maksim88.easylogin.networks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

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

    private GoogleApiClient mGoogleApiClient;

    private AccessToken mAccessToken;

    private static final int REQUEST_AUTH = 1337;

    private WeakReference<Context> mContext;


    public GooglePlusNetwork(Context context) {

        mContext = new WeakReference<>(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleApiClient.Builder googleApiBuilder = new GoogleApiClient.Builder(context);
        if (context instanceof FragmentActivity) {
            googleApiBuilder.enableAutoManage((FragmentActivity) context, this);
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
       return mGoogleApiClient.isConnected();
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
        }
    }

    @Override
    public Network getNetwork() {
        return Network.GOOGLE_PLUS;
    }

    @Override
    public AccessToken getAccessToken() {
        return mAccessToken;
        //throw new UnsupportedOperationException("Google Plus does not provide an accessToken, use either requestLogin() or silentSignIn() if already connected");
    }

    /**
     * Make login request - authorize in Google plus social network
     * @param onLoginCompleteListener listener for login complete
     */
    @Override
    public void requestLogin(OnLoginCompleteListener onLoginCompleteListener) {
        super.requestLogin(onLoginCompleteListener);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        if (mContext.get() instanceof FragmentActivity) {
            ((FragmentActivity) mContext.get()).startActivityForResult(signInIntent, REQUEST_AUTH);
        }
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
        if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
            ((OnLoginCompleteListener) mLocalListeners.get(REQUEST_LOGIN)).onLoginSuccess(getNetwork());
            mLocalListeners.remove(REQUEST_LOGIN);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
            mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN, getStatusCodeString(cause));
            mLocalListeners.remove(REQUEST_LOGIN);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
            mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN, getStatusCodeString(connectionResult.getErrorCode()));
            mLocalListeners.remove(REQUEST_LOGIN);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        parseGoogleSignInResult(result);
    }

    private void parseGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
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
            if (result.getStatus().getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                requestLogin();
                return;
            }
            // Signed out, show unauthenticated UI.
            if (mLocalListeners.containsKey(REQUEST_LOGIN)) {
                mLocalListeners.get(REQUEST_LOGIN).onError(getNetwork(), REQUEST_LOGIN, getStatusCodeString(result.getStatus().getStatusCode()));
                mLocalListeners.remove(REQUEST_LOGIN);
            }
        }
    }

    private String getStatusCodeString(int statusCode) {
        return CommonStatusCodes.getStatusCodeString(statusCode);
    }

}
