package co.foodcircles.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import co.foodcircles.exception.NetException2;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FoodCirclesUtils;

public class FacebookLoginActivity extends Activity {
    CallbackManager fbCallbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fbCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(fbCallbackManager, fbCallback);
    }

    private void fbSignIn(final String userId, final String emailId) {
        AndroidUtils.showProgress(this);
        new Thread() {
            public void run() {
                try {
                    final String token = Net.facebookSignUp(userId, emailId);
                    FacebookLoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtils.dismissProgress();
                            FoodCirclesUtils.saveToken(FacebookLoginActivity.this,
                                    token);
                            FoodCirclesUtils.saveEmail(FacebookLoginActivity.this, emailId);
                            gotoSignedInPage();
                            FacebookLoginActivity.this.finish();
                        }
                    });
                } catch (final NetException2 e) {
                    FacebookLoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtils.showAlertOk(FacebookLoginActivity.this,
                                    "Sign-up Failed - " + e.getMessage());
                            AndroidUtils.dismissProgress();
                        }
                    });
                }
            }
        }.start();
    }

    public void gotoSignedInPage() {
        Intent intent = new Intent(FacebookLoginActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.CURRENT_TAB, MainActivity.TAB_RESTAURANTS);
        startActivity(intent);
        FacebookLoginActivity.this.finish();
    }

    public void getFacebookInfo(final AccessToken accessToken) {
        Bundle params = new Bundle();
        params.putString("fields", "id, email");
        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (response.getError() == null) {
                    final String email = object.optString("email");
                    final String id = accessToken.getUserId();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fbSignIn(id, email);
                        }
                    });
                }
            }
        });
        graphRequest.setParameters(params);
        graphRequest.executeAsync();
    }

    private final FacebookCallback<LoginResult> fbCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            getFacebookInfo(loginResult.getAccessToken());
        }

        @Override
        public void onCancel() {
            Toast.makeText(FacebookLoginActivity.this, "Facebook permissions cancelled!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(FacebookLoginActivity.this, "Whoops- we've encountered a problem!", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }
    };

}
