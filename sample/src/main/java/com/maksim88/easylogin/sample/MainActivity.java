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

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnLoginCompleteListener {

    private EasyLogin easyLogin;

    private SignInButton gPlusButton;

    private GooglePlusNetwork gPlusNetwork;

    private TextView statusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasyLogin.initialize();
        easyLogin = EasyLogin.getInstance();

        // TWITTER

        // Initialization needs to happen before setContentView() if using the LoginButton!
        String twitterKey = BuildConfig.TWITTER_CONSUMER_KEY;
        String twitterSecret = BuildConfig.TWITTER_CONSUMER_SECRET;
        easyLogin.addSocialNetwork(new TwitterNetwork(this, twitterKey, twitterSecret));

        setContentView(R.layout.activity_main);

        TwitterNetwork twitter = (TwitterNetwork) easyLogin.getSocialNetwork(SocialNetwork.Network.TWITTER);
        twitter.setAdditionalEmailRequest(true);
        TwitterLoginButton twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitter.requestLogin(twitterButton, this);

        // TWITTER END

        // FACEBOOK
        List<String> fbScope = Arrays.asList("public_profile", "email");
        easyLogin.addSocialNetwork(new FacebookNetwork(this, fbScope));

        FacebookNetwork facebook = (FacebookNetwork) easyLogin.getSocialNetwork(SocialNetwork.Network.FACEBOOK);
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebook.requestLogin(loginButton, this);
        // FACEBOOK END

        // G+

        easyLogin.addSocialNetwork(new GooglePlusNetwork(this));
        gPlusNetwork = (GooglePlusNetwork) easyLogin.getSocialNetwork(SocialNetwork.Network.GOOGLE_PLUS);
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
        //TODO
//        if (!gPlusNetwork.isConnected()) {
//            gPlusNetwork.silentSignIn();
//        } else {
//            gPlusButton.setEnabled(false);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatuses();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        easyLogin.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoginSuccess(SocialNetwork.Network network) {
        if (network == SocialNetwork.Network.GOOGLE_PLUS) {
            AccessToken token = easyLogin.getSocialNetwork(SocialNetwork.Network.GOOGLE_PLUS).getAccessToken();
            Log.d("MAIN", "G+ Login successful: " + token.getToken() + "|||" + token.getEmail());
            gPlusButton.setEnabled(false);
        } else if (network == SocialNetwork.Network.FACEBOOK) {
            AccessToken token = easyLogin.getSocialNetwork(SocialNetwork.Network.FACEBOOK).getAccessToken();
            Log.d("MAIN", "FACEBOOK Login successful: " + token.getToken() + "|||" + token.getEmail());
        } else if (network == SocialNetwork.Network.TWITTER) {
            AccessToken token = easyLogin.getSocialNetwork(SocialNetwork.Network.TWITTER).getAccessToken();
            Log.d("MAIN", "TWITTER Login successful: " + token.getToken() + "|||" + token.getEmail());
        }
        updateStatuses();
    }

    @Override
    public void onError(SocialNetwork.Network socialNetwork, String errorMessage) {
        Log.e("MAIN", "ERROR!" + socialNetwork + "|||" + errorMessage);
        Toast.makeText(getApplicationContext(), socialNetwork.name() + ": " + errorMessage,
                Toast.LENGTH_SHORT).show();
    }

    private void updateStatuses() {
        StringBuilder content = new StringBuilder();
        for (SocialNetwork socialNetwork : easyLogin.getInitializedSocialNetworks()) {
            content.append(socialNetwork.getNetwork())
                    .append(": ")
                    .append(socialNetwork.isConnected())
                    .append("\n");
        }
        statusTextView.setText(content.toString());
    }

    public void logoutAllNetworks(View view) {
        for (SocialNetwork socialNetwork : easyLogin.getInitializedSocialNetworks()) {
            socialNetwork.logout();
        }
        updateStatuses();
    }
}
