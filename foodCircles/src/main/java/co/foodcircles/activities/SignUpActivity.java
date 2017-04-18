package co.foodcircles.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Calendar;

import co.foodcircles.R;
import co.foodcircles.exception.NetException2;
import co.foodcircles.net.Net;
import co.foodcircles.services.AlarmReceiver;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.FoodCirclesUtils;

public class SignUpActivity extends SocialLoginActivity {
    private EditText email;
    private EditText password;
    private MixpanelAPI mixpanel;
    private String numPeopleString;

    @Override
    public void onStart() {
        super.onStart();
        mixpanel = MixpanelAPI.getInstance(this, getResources().getString(R.string.mixpanel_token));
    }

    @Override
    public void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.signup);

        FontSetter.overrideFonts(this, findViewById(R.id.root));

        final FoodCirclesApplication app = (FoodCirclesApplication) getApplicationContext();
        getPeopleAmount();

        email = (EditText) findViewById(R.id.editTextEmail);
        password = (EditText) findViewById(R.id.editTextPassword);
        Button signUpButton = (Button) findViewById(R.id.buttonSignUp);

        if (!FoodCirclesUtils.getPassword(this).isEmpty()) {
            if (!FoodCirclesUtils.getEmail(this).isEmpty()) {
                final String un = FoodCirclesUtils.getEmail(this);
                final String pw = FoodCirclesUtils.getPassword(this);

                new Thread() {
                    public void run() {
                        signIn(un, pw);
                    }
                }.start();
            }
        }

        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(email.getEditableText().toString(), password
                        .getEditableText().toString());
            }
        });

        TextView signInButton = (TextView) findViewById(R.id.buttonSignIn);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this,
                        SignInActivity.class);
                intent.putExtra("peopleNumber", numPeopleString);
                startActivity(intent);
                SignUpActivity.this.finish();
            }
        });

        Button buttonFacebook = (Button) findViewById(R.id.buttonFacebookU);
        buttonFacebook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken == null) {
                    LoginManager.getInstance().logInWithReadPermissions(SignUpActivity.this, app.getPermissions());
                } else {
                    getFacebookInfo(accessToken);
                }
            }
        });

        Button buttonTwitter = (Button) findViewById(R.id.buttonTwitterU);
        buttonTwitter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                authClient.authorize(SignUpActivity.this, twitterSessionCallback);
            }
        });
        startupNotifications();
    }

    private void getPeopleAmount() {
        AndroidUtils.showProgress(this);
        new AsyncTask<Object, Void, String>() {
            protected String doInBackground(Object... param) {
                return Net.getMailChimp();
            }

            protected void onPostExecute(String peopleAmount) {
                AndroidUtils.dismissProgress();
                TextView countText = (TextView) findViewById(R.id.textViewCount);
                float size = countText.getTextSize();

                numPeopleString = peopleAmount;
                Spannable countSpannable = new SpannableString(String.format(getString(R.string.people_amount), numPeopleString));

                countSpannable.setSpan(new TextAppearanceSpan(SignUpActivity.this,
                                R.style.TextAppearanceLargeBold), 0, numPeopleString.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                countSpannable.setSpan(new TextAppearanceSpan(SignUpActivity.this,
                                R.style.TextAppearanceLargeBold), countSpannable.length() - 24,
                        countSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                countSpannable.setSpan(
                        new ForegroundColorSpan(ContextCompat.getColor(SignUpActivity.this, R.color.dark_font)), 0, countSpannable.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                countSpannable.setSpan(new AbsoluteSizeSpan((int) size), 0,
                        countSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                countText.setText(countSpannable);
            }
        }.execute();
    }

    private void signUp(final String email, final String password) {
        AndroidUtils.showProgress(this);
        new Thread() {
            public void run() {
                try {
                    final String token = Net.signUp(email, password);
                    SignUpActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtils.dismissProgress();
                            FoodCirclesUtils.saveToken(SignUpActivity.this,
                                    token);
                            FoodCirclesUtils.saveEmail(SignUpActivity.this,
                                    email);
                            FoodCirclesUtils.savePassword(SignUpActivity.this,
                                    password);
                            gotoSignedInPage();
                        }
                    });
                } catch (final NetException2 e) {
                    SignUpActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtils.showAlertOk(SignUpActivity.this,
                                    "Sign-up Failed - " + e.getMessage());
                            AndroidUtils.dismissProgress();
                        }
                    });
                }
            }
        }.start();
    }

    private void startupNotifications() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) > 16)
            calendar.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("co.foodcircles.geonotification");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void signIn(final String email, final String password) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AndroidUtils.showProgress(SignUpActivity.this, "Logging In...",
                        "Please wait");
            }
        });
        try {
            final String token = Net.signIn(email, password);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FoodCirclesUtils.saveToken(SignUpActivity.this, token);
                    FoodCirclesUtils.saveEmail(SignUpActivity.this, email);
                    FoodCirclesUtils
                            .savePassword(SignUpActivity.this, password);
                    AndroidUtils.dismissProgress();
                    Intent intent = new Intent(SignUpActivity.this,
                            MainActivity.class);
                    intent.putExtra(MainActivity.CURRENT_TAB, MainActivity.TAB_RESTAURANTS);
                    startActivity(intent);
                    SignUpActivity.this.finish();
                }
            });
        } catch (final NetException2 e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AndroidUtils.alert(SignUpActivity.this,
                            "Oops! Sign-in Failed : " + e.getMessage());
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
