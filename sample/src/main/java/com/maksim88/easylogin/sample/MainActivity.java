package com.maksim88.easylogin.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.maksim88.easylogin.AccessToken;
import com.maksim88.easylogin.EasyLogin;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;
import com.maksim88.easylogin.networks.FacebookNetwork;
import com.maksim88.easylogin.networks.GooglePlusNetwork;
import com.maksim88.easylogin.networks.SocialNetwork;
import com.maksim88.easylogin.networks.TwitterNetwork;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements OnLoginCompleteListener {

    private EasyLogin mEasyLogin;

    ArrayList<String> fbScope;

    private LoginButton loginButton;

    private SignInButton gPlusButton;

    private TwitterLoginButton  twitterButton;

    FacebookNetwork facebook;

    TwitterNetwork twitter;

    GooglePlusNetwork gPlusNetwork;

    TextView statusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasyLogin.initialize();
        mEasyLogin = EasyLogin.getInstance();

        // TWITTER

        // Initialization needs to happen before setContentView() if using the LoginButton!
        String twitterKey = BuildConfig.TWITTER_CONSUMER_KEY;
        String twitterSecret = BuildConfig.TWITTER_CONSUMER_SECRET;
        mEasyLogin.addSocialNetwork(new TwitterNetwork(this, twitterKey, twitterSecret));

        setContentView(R.layout.activity_main);

        twitter = (TwitterNetwork) mEasyLogin.getSocialNetwork(SocialNetwork.Network.TWITTER);
        twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitter.requestLogin(twitterButton, this);

        // TWITTER END

        // FACEBOOK
        fbScope = new ArrayList<>();
        fbScope.addAll(Collections.singletonList("public_profile, email"));
        mEasyLogin.addSocialNetwork(new FacebookNetwork(this, fbScope));

        facebook = (FacebookNetwork) mEasyLogin.getSocialNetwork(SocialNetwork.Network.FACEBOOK);
        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebook.requestLogin(loginButton, this);
        // FACEBOOK END

        // G+

        mEasyLogin.addSocialNetwork(new GooglePlusNetwork(this));
        gPlusNetwork = (GooglePlusNetwork) mEasyLogin.getSocialNetwork(SocialNetwork.Network.GOOGLE_PLUS);
        gPlusNetwork.setListener(this);

        gPlusButton = (SignInButton) findViewById(R.id.gplus_sign_in_button);

        gPlusNetwork.setSignInButton(gPlusButton);


        // G+ END

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        statusTextView = (TextView) findViewById(R.id.connected_status);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!gPlusNetwork.isConnected()) {
            gPlusNetwork.silentSignIn();
        } else {
            gPlusButton.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatuses();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mEasyLogin.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoginSuccess(SocialNetwork.Network network) {
        if (network == SocialNetwork.Network.GOOGLE_PLUS) {
            AccessToken token = mEasyLogin.getSocialNetwork(SocialNetwork.Network.GOOGLE_PLUS).getAccessToken();
            Log.d("MAIN", "G+ Login successful: " + token.getToken());
            gPlusButton.setEnabled(false);
        } else if (network == SocialNetwork.Network.FACEBOOK) {
            AccessToken token = mEasyLogin.getSocialNetwork(SocialNetwork.Network.FACEBOOK).getAccessToken();
            Log.d("MAIN", "FACEBOOK Login successful: " + token.getToken());
        } else if (network == SocialNetwork.Network.TWITTER) {
            AccessToken token = mEasyLogin.getSocialNetwork(SocialNetwork.Network.TWITTER).getAccessToken();
            Log.d("MAIN", "TWITTER Login successful: " + token.getToken());
        }
        updateStatuses();
    }

    @Override
    public void onError(SocialNetwork.Network socialNetwork, String errorMessage) {
        Log.e("MAIN", "ERROR!" + socialNetwork + "|||" + errorMessage);
        Toast.makeText(getApplicationContext(), errorMessage,
                Toast.LENGTH_SHORT).show();
    }

    private void updateStatuses() {
        StringBuilder content = new StringBuilder();
        for (SocialNetwork socialNetwork : mEasyLogin.getInitializedSocialNetworks()) {
            content.append(socialNetwork.getNetwork())
                    .append(": ")
                    .append(socialNetwork.isConnected())
                    .append("\n");
        }
        statusTextView.setText(content.toString());
    }

    public void logoutAllNetworks(View view) {
        for (SocialNetwork socialNetwork : mEasyLogin.getInitializedSocialNetworks()) {
            socialNetwork.logout();
        }
        updateStatuses();
    }
}
