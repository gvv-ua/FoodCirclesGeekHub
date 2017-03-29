package co.foodcircles.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import co.foodcircles.util.TwitterLogin;


public class SignUpActivity extends FacebookLoginActivity {
	EditText email;
	EditText password;
	Button signUpButton;
    Button buttonFacebook;
	Button buttonTwitter;
	TextView signInButton;
	String tempkey;
	MixpanelAPI mixpanel;
	Bundle savedInstanceState;
	boolean signedIn=false;
	Context mContext;

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
		this.savedInstanceState=savedInstanceState;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.signup);

		mContext = this;
		FontSetter.overrideFonts(this, findViewById(R.id.root));

		final FoodCirclesApplication app = (FoodCirclesApplication) getApplicationContext();
		app.addPoppableActivity(this);

//		TextView copyText = (TextView) findViewById(R.id.textViewCopy);
//		Spannable spannable = new SpannableString( "Buy one appetizer or dessert for $1,\n feed one child in need.");
//		spannable.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceLargeBoldItalic), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		spannable.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceLargeBoldItalic), 38, 46, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 38, 46, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		spannable.setSpan(new AbsoluteSizeSpan((int) this.getResources()
//				.getDimension(R.dimen.signup_small_text)), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		spannable.setSpan(new AbsoluteSizeSpan((int) this.getResources()
//				.getDimension(R.dimen.signup_small_text)), 38, 46, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		copyText.setText(spannable);

		TextView countText = (TextView) findViewById(R.id.textViewCount);
		float size = countText.getTextSize();
		final String numPeopleString;
        String peopleAmount;
        peopleAmount = Net.getMailChimp();

        numPeopleString = peopleAmount;
		Spannable countSpannable = new SpannableString(numPeopleString + 
				" people repurpose their Grand Rapids dining.\nToday, it\'s your turn.");
		countSpannable.setSpan(new TextAppearanceSpan(this,
				R.style.TextAppearanceLargeBold), 0, numPeopleString.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		countSpannable.setSpan(new TextAppearanceSpan(this,
				R.style.TextAppearanceLargeBold), countSpannable.length() - 24,
				countSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		countSpannable.setSpan(
				new ForegroundColorSpan(getResources().getColor(
						R.color.dark_font)), 0, countSpannable.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		countSpannable.setSpan(new AbsoluteSizeSpan((int) size), 0,
				countSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		countText.setText(countSpannable);

		email = (EditText) findViewById(R.id.editTextEmail);
		password = (EditText) findViewById(R.id.editTextPassword);
		signUpButton = (Button) findViewById(R.id.buttonSignUp);

		if (FoodCirclesUtils.getPassword(this).isEmpty() == false) {
			if (FoodCirclesUtils.getEmail(this).isEmpty() == false) {
				final String un = FoodCirclesUtils.getEmail(this);
				final String pw = FoodCirclesUtils.getPassword(this);

				new Thread() {
					public void run() {
						signIn(un, pw);
					};
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

		signInButton = (TextView) findViewById(R.id.buttonSignIn);
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

        buttonFacebook = (Button) findViewById(R.id.buttonFacebookU);
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

		buttonTwitter = (Button) findViewById(R.id.buttonTwitterU);
		buttonTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new TwitterLogin(mContext, numPeopleString).twitterSignUp();
			}
		});
		startupNotifications();
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
			};
		}.start();
	}

	private void startupNotifications() {
		Calendar calendar = Calendar.getInstance();
		if(calendar.get(Calendar.HOUR_OF_DAY)>16)
		calendar.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK)+1);
		calendar.set(Calendar.HOUR_OF_DAY, 16);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 00);
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
					intent.putExtra("tab", 1);
					startActivity(intent);
					SignUpActivity.this.finish();
					FoodCirclesApplication app = (FoodCirclesApplication) getApplicationContext();
					app.newTop();
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
	protected void onResume() {
	 	super.onResume();
	 }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
	}

}
