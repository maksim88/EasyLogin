[![](https://jitpack.io/v/maksim88/EasyLogin.svg)](https://jitpack.io/#maksim88/EasyLogin)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-EasyLogin-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5423)

EasyLogin
============
Easy Login in your app with different social networks.
Currently supported:
- Facebook
- Google Plus
- Twitter


Global Configuration
--------
To be able to use one of the social network connections you need to create an `EasyLogin` instance:
```
EasyLogin.initialize();
EasyLogin easyLogin = EasyLogin.getInstance();
```

Also make sure to call through to `EasyLogin` in  `onActivityResult()` of your Activity:
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  easyLogin.onActivityResult(requestCode, resultCode, data);
}
```

Afterwards you can connect to the social networks one by one.

Facebook Connection
--------

To connect to facebook you need to do the following:
- First of all you need to init a list of permissions you want to take:
    ```
    List<String> fbScope = new ArrayList<>();
    fbScope.addAll(Collections.singletonList("public_profile, email"));
    ```
- Add the facebook social network:
    ```
    easyLogin.addSocialNetwork(new FacebookNetwork(this, fbScope));
    ```
- Add the Facebook LoginButton and the listeners:
     ```
    facebook = (FacebookNetwork) mEasyLogin.getSocialNetwork(SocialNetwork.Network.FACEBOOK);
    
    // Use global listener to get notified in onSuccess() or onError() inside Activity
    facebook.setOnLoginCompleteListener(this);
    
    LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
    // Call this method if you are using the LoginButton provided by facebook
    // It can handle its own state
    if (!facebook.isConnected()) {
        // You can also use a local listener per every social login
        facebook.requestLogin(loginButton, this);
    }
     ```
- In the next step you will get an  `onSuccess()` or  `onError()` callback. The easiest solution is to implement the `OnLoginCompleteListener`in your activity and handle all the connections there.

You also need to make sure to add the Facebook ApplicationId in your manifest:
```
<meta-data android:name="com.facebook.sdk.ApplicationId" android:value="@string/facebook_app_id"/>
```

Twitter Connection
--------

One gotcha of using the Twitter Social connection is to create the `TwitterNetwork` before calling to `setContentView()` as Twitter would complain otherwise.
```
// Initialization needs to happen before setContentView() if using the LoginButton!
easyLogin.addSocialNetwork(new TwitterNetwork(this, twitterKey, twitterSecret));

setContentView(R.layout.activity_main);

twitter = (TwitterNetwork) easyLogin.getSocialNetwork(SocialNetwork.Network.TWITTER);
twitter.setOnLoginCompleteListener(this);
twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
twitter.requestLogin(twitterButton, this);
```

You also need to make sure to add the Fabric API Key your manifest:
```
 <meta-data android:name="io.fabric.ApiKey" android:value="@string/fabric_app_id"/>
```

Google Plus Connection
--------

```
easyLogin.addSocialNetwork(new GooglePlusNetwork(this));
gPlusNetwork = (GooglePlusNetwork) easyLogin.getSocialNetwork(SocialNetwork.Network.GOOGLE_PLUS);
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
```
 
 You also need to include a valid `google-services.json` file in your project to be able to use G+:
 For more information you can consult the [official docs](https://developers.google.com/identity/sign-in/android/start-integrating).
 
Callbacks
--------

```
 public class MainActivity extends AppCompatActivity implements OnLoginCompleteListener {
 
 [...]
 
 @Override
 public void onLoginSuccess(SocialNetwork.Network network) {
     // You can check the network by e.g.: if (network == SocialNetwork.Network.FACEBOOK) 
     AccessToken token = network.getAccessToken();
     Log.d("MAIN", "Login successful: " + token.getToken());
 }
 
 @Override
 public void onError(SocialNetwork.Network socialNetwork, String requestID, String errorMessage) {
     Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
 }
```

Download
--------

The library is available through `jcenter()`.

For now you have to include the fabric repo in your root projects `build.gradle`:

```groovy
allprojects {
		repositories {
			[...]
			maven { url 'https://maven.fabric.io/public' } //currently needed for the twitter lib
		}
	}
```

After that you can easily include the library in your app `build.gradle`:

```groovy
dependencies {
	        compile 'com.maksim88:EasyLogin:v0.3'
	}
```

The builds are also available via `jitpack.io`.
        
License
--------
Licensed under the MIT license. See [LICENSE](LICENSE).
