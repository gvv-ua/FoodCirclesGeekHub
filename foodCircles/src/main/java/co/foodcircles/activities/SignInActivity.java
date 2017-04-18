package co.foodcircles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import co.foodcircles.R;
import co.foodcircles.exception.NetException2;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.FoodCirclesUtils;

public class SignInActivity extends SocialLoginActivity {
    private EditText email;
    private EditText password;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.signin);
        FontSetter.overrideFonts(this, findViewById(R.id.root));
        TextView t2 = (TextView) findViewById(R.id.forgotpassword);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        email = (EditText) findViewById(R.id.editTextEmail);
        password = (EditText) findViewById(R.id.editTextPassword);
        Button signInButton = (Button) findViewById(R.id.buttonSignIn);
        TextView signUpButton = (TextView) findViewById(R.id.buttonSignUp);
        Button buttonFacebook = (Button) findViewById(R.id.buttonFacebookI);
        Button buttonTwitter = (Button) findViewById(R.id.buttonTwitterI);

        buttonFacebook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken == null) {
                    final FoodCirclesApplication app = (FoodCirclesApplication) getApplicationContext();
                    LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, app.getPermissions());
                } else {
                    getFacebookInfo(accessToken);
                }
            }
        });
        buttonTwitter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String peopleNumber = SignInActivity.this.getIntent().getStringExtra("peopleNumber");
                //new TwitterLogin(SignInActivity.this, peopleNumber).twitterSignUp();
                authClient.authorize(SignInActivity.this, twitterSessionCallback);
            }
        });

        if (!FoodCirclesUtils.getPassword(this).isEmpty()) {
            password.setText(FoodCirclesUtils.getPassword(this));
            new Thread() {
                public void run() {
                    signIn(email.getText().toString(), password.getText().toString());
                }
            }.start();
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        signIn(email.getText().toString(), password.getText().toString());
                    }
                }.start();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
                SignInActivity.this.finish();
            }
        });

    }

    private void signIn(final String email, final String password) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AndroidUtils.showProgress(SignInActivity.this);
            }
        });
        try {
            final String token = Net.signIn(email, password);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FoodCirclesUtils.saveToken(SignInActivity.this, token);
                    FoodCirclesUtils.saveEmail(SignInActivity.this, email);
                    FoodCirclesUtils.savePassword(SignInActivity.this, password);
                    AndroidUtils.dismissProgress();
                    gotoSignedInPage();
                }
            });
        } catch (final NetException2 e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AndroidUtils.alert(SignInActivity.this, "Couldn't sign in: Email not registered");
                    AndroidUtils.dismissProgress();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        authClient.onActivityResult(requestCode, resultCode, data);
    }
}
