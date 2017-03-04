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
        twitter.setOnLoginCompleteListener(this);
        twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitter.requestLogin(twitterButton, this);

        // TWITTER END

        // FACEBOOK
        fbScope = new ArrayList<>();
        fbScope.addAll(Collections.singletonList("public_profile, email"));
        mEasyLogin.addSocialNetwork(new FacebookNetwork(this, fbScope));

        facebook = (FacebookNetwork) mEasyLogin.getSocialNetwork(SocialNetwork.Network.FACEBOOK);
        facebook.setOnLoginCompleteListener(this);
        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        // Call this method if you are using the LoginButton provided by facebook
        // It can handle its own state
        if (!facebook.isConnected()) {
            facebook.requestLogin(loginButton, this);
        }
        // FACEBOOK END

        // G+

        mEasyLogin.addSocialNetwork(new GooglePlusNetwork(this));
        gPlusNetwork = (GooglePlusNetwork) mEasyLogin.getSocialNetwork(SocialNetwork.Network.GOOGLE_PLUS);
        gPlusNetwork.setOnLoginCompleteListener(this);

        gPlusButton = (SignInButton) findViewById(R.id.gplus_sign_in_button);
        gPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gPlusNetwork.isConnected()) {
                    gPlusNetwork.requestLogin(MainActivity.this);
                }
            }
        });

        // G+ END

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        statusTextView = (TextView) findViewById(R.id.connected_status);
        setSupportActionBar(toolbar);
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
    public void onError(SocialNetwork.Network socialNetwork, String requestID, String errorMessage, Object data) {
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
}
