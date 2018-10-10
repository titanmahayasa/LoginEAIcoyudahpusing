package com.androidbie.googlelogintutorial;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FacebookLoginActivity extends AppCompatActivity {

    private TextView tvName;
    private ProfilePictureView profilePictureView;
    private LoginButton btnLoginFacebook;
    private Button btnLogoutFacebook;
    private ProgressDialog progressDialog;
    private FrameLayout frameLayoutLoginFacebook;

    //facebook attribute
    private CallbackManager callBackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize the SDK facebook before executing any other operations
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_facebook_login);

        tvName = (TextView) findViewById(R.id.textview_name);
        profilePictureView = (ProfilePictureView) findViewById(R.id.imageview_profile_pict);
        btnLoginFacebook = (LoginButton) findViewById(R.id.fbLogin);
        btnLogoutFacebook = (Button) findViewById(R.id.btn_log_out_facebook);
        frameLayoutLoginFacebook = (FrameLayout) findViewById(R.id.frame_layout_btn_login_facebook);

        //this is for print your hashkey
        printKeyHash(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loging using facebook");

        //callback
        callBackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {

            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                updateUI(newProfile);
            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        btnLoginFacebook.setReadPermissions("public_profile", "email", "user_friends");
        frameLayoutLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();

                btnLoginFacebook.setSoundEffectsEnabled(false);
                btnLoginFacebook.performClick();
                btnLoginFacebook.setPressed(true);
                btnLoginFacebook.invalidate();
                btnLoginFacebook.registerCallback(callBackManager, callBack);

            }
        });

        btnLogoutFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Profile profile = Profile.getCurrentProfile();
                updateUI(profile);
            }
        });
    }


    /**
     * FacebookCallBack
     */
    private FacebookCallback<LoginResult> callBack = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            progressDialog.dismiss();
            Profile profile = Profile.getCurrentProfile();
            updateUI(profile);
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callBackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        updateUI(profile);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    /**
     * this method used to update user interface
     *
     * @param profile
     */
    private void updateUI(Profile profile) {
        if (profile != null) {
            tvName.setText(profile.getName());
            profilePictureView.setProfileId(profile.getId());
            btnLogoutFacebook.setVisibility(View.VISIBLE);
            frameLayoutLoginFacebook.setVisibility(View.GONE);
        } else {
            tvName.setText("Please login");
            profilePictureView.setProfileId(null);
            frameLayoutLoginFacebook.setVisibility(View.VISIBLE);
            btnLogoutFacebook.setVisibility(View.GONE);
        }
    }

    /**
     * this method used to print out the hashkey of your application
     *
     * @param context
     * @return
     */
    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }
}
