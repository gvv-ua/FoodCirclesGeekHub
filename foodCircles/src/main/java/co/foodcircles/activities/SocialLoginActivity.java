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
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONObject;

import co.foodcircles.exception.NetException2;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FoodCirclesUtils;

public class SocialLoginActivity extends Activity {
    CallbackManager fbCallbackManager;
    TwitterAuthClient authClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fbCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(fbCallbackManager, fbCallback);

        authClient = new TwitterAuthClient();
    }

    private void fbSignIn(final String userId, final String emailId) {
        AndroidUtils.showProgress(this);
        new Thread() {
            public void run() {
                try {
                    final String token = Net.facebookSignUp(userId, emailId);
                    SocialLoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtils.dismissProgress();
                            FoodCirclesUtils.saveToken(SocialLoginActivity.this,
                                    token);
                            FoodCirclesUtils.saveEmail(SocialLoginActivity.this, emailId);
                            gotoSignedInPage();
                            SocialLoginActivity.this.finish();
                        }
                    });
                } catch (final NetException2 e) {
                    SocialLoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtils.showAlertOk(SocialLoginActivity.this,
                                    "Sign-up Failed - " + e.getMessage());
                            AndroidUtils.dismissProgress();
                        }
                    });
                }
            }
        }.start();
    }

    public void gotoSignedInPage() {
        Intent intent = new Intent(SocialLoginActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.CURRENT_TAB, MainActivity.TAB_RESTAURANTS);
        startActivity(intent);
        SocialLoginActivity.this.finish();
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

    private void twSingnIn() {
        new Thread() {
            @Override
            public void run() {
                TwitterSession session = Twitter.getInstance().core.getSessionManager().getActiveSession();
                try {
                    final String token = Net.twitterSignIn(session.getUserId());
                        if (token.equals("error")) {
//            authClient.requestEmail(session, new Callback<String>() {
//                @Override
//                public void success(Result<String> result) {
//                    Toast.makeText(getApplicationContext(), "Request Email success", Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void failure(TwitterException exception) {
//                    Toast.makeText(getApplicationContext(), "Request Email fail", Toast.LENGTH_LONG).show();
//                }
//            });

                            Intent intent = new Intent(SocialLoginActivity.this, EmailPromptsActivity.class);
                            intent.putExtra("UID", session.getUserId());
                            //intent.putExtra("peopleNumber", mNumberOfPeople);
                            startActivity(intent);
                        } else {
                            FoodCirclesUtils.saveToken(SocialLoginActivity.this, token);
                            gotoSignedInPage();
                        }
                } catch (NetException2 e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private final FacebookCallback<LoginResult> fbCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            getFacebookInfo(loginResult.getAccessToken());
        }

        @Override
        public void onCancel() {
            Toast.makeText(SocialLoginActivity.this, "Facebook permissions cancelled!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(SocialLoginActivity.this, "Whoops- we've encountered a problem!", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }
    };

    public final com.twitter.sdk.android.core.Callback<TwitterSession> twitterSessionCallback = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            twSingnIn();
        }

        @Override
        public void failure(TwitterException exception) {
            Toast.makeText(SocialLoginActivity.this, "Whoops- we've encountered a problem!", Toast.LENGTH_SHORT).show();
            exception.printStackTrace();
        }
    };

}
