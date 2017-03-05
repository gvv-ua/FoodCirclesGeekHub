package co.foodcircles.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

import co.foodcircles.R;
import co.foodcircles.exception.NetException2;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.FoodCirclesUtils;
import co.foodcircles.util.TwitterLogin;

public class SignInActivity extends Activity {
	EditText email;
	EditText password;
	Button signInButton;
	TextView signUpButton;
	private Button buttonFacebook;
	private Button buttonTwitter;
	SimpleFacebook mSimpleFacebook;
	boolean signedIn=false;
	Context mcontext;
	String id="";
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.signin);
		FontSetter.overrideFonts(this, findViewById(R.id.root));
		TextView t2 = (TextView) findViewById(R.id.forgotpassword);
	    t2.setMovementMethod(LinkMovementMethod.getInstance());
		mcontext = this;
		email = (EditText) findViewById(R.id.editTextEmail);
		password = (EditText) findViewById(R.id.editTextPassword);
		signInButton = (Button) findViewById(R.id.buttonSignIn);
		signUpButton = (TextView) findViewById(R.id.buttonSignUp);
		buttonFacebook = (Button) findViewById(R.id.buttonFacebookI);
		buttonTwitter = (Button) findViewById(R.id.buttonTwitterI);
		buttonFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			//	if(mSimpleFacebook.isAllPermissionsGranted())Log.i("Persmission","OK");
				
				if (mSimpleFacebook.isLogin()) {
				//	getID();
					//Log.i("id",id);
					mSimpleFacebook.getProfile(mProfileListener); 
				} else {
					mSimpleFacebook.login(mOnLoginListener);
				}
			}
		});
		buttonTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                String peopleNumber = SignInActivity.this.getIntent().getStringExtra("peopleNumber");
				new TwitterLogin(mcontext, peopleNumber).twitterSignUp();
			}
		});

		if (FoodCirclesUtils.getPassword(this).isEmpty() == false) {
			password.setText(FoodCirclesUtils.getPassword(this));
			new Thread() {
				public void run() {
					signIn(email.getText().toString(), password.getText().toString());
				};
			}.start();
		}

		signInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread() {
					public void run() {
						signIn(email.getText().toString(), password.getText().toString());
					};
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
					gotToSignedInPage();
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

	private void gotToSignedInPage() {
		Intent intent = new Intent(SignInActivity.this, MainActivity.class);
		intent.putExtra("tab", 1);
		startActivity(intent);
		SignInActivity.this.finish();
		FoodCirclesApplication app = (FoodCirclesApplication) getApplicationContext();
		app.newTop();
	}

	private void fbSignIn(final String userID,final String emailid) {
		AndroidUtils.showProgress(SignInActivity.this);
		new Thread() {
			public void run() {
				try {
					
					final String token = Net.facebookSignUp(userID,emailid);
					SignInActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.dismissProgress();
							
							FoodCirclesUtils.saveToken(SignInActivity.this,
									token);
							FoodCirclesUtils.saveEmail(SignInActivity.this,
									emailid);
							Intent intent = new Intent(SignInActivity.this,
									MainActivity.class);
							intent.putExtra("tab", 1);
							
							startActivity(intent);
							SignInActivity.this.finish();
						}
					});
				} catch (final NetException2 e) {
					SignInActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.showAlertOk(SignInActivity.this,
									"Sign-in Failed - " + e.getMessage());
							AndroidUtils.dismissProgress();
						}
					});
				}
			};
		}.start();
	}

	 @Override
	 protected void onResume() {
	 	super.onResume();
	 
	 	mSimpleFacebook = SimpleFacebook.getInstance(this);
	 }


	private OnLoginListener mOnLoginListener = new OnLoginListener() {

		@Override
		public void onFail(String reason) {
			Toast.makeText(mcontext, "Facebook Login Failed:" + reason, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onException(Throwable throwable) {
			Toast.makeText(mcontext, "Whoops- we've encountered a problem!", Toast.LENGTH_SHORT).show();
			throwable.printStackTrace();
		}

		@Override
		public void onThinking() {
			// Place here if we want to show progress bar while login is occurring
		}

		@Override
		public void onLogin() {
			//getID();
			//Log.i("id",id);
			mSimpleFacebook.getProfile(mProfileListener); 
		}

		@Override
		public void onNotAcceptingPermissions(Permission.Type type) {
			Toast.makeText(mcontext,"Facebook permissions cancelled!", Toast.LENGTH_SHORT).show();
		}
	};
	private OnProfileListener mProfileListener = new OnProfileListener() {
		@Override
		public void onComplete(Profile profile) {
			final String email = profile.getEmail();
			final String id = profile.getId();
			Log.i("Values",email+"&"+id);
			runOnUiThread(new Runnable() {
				@Override
				public void run() { fbSignIn(id , email); }
			});
		}
	};
	public void getID(){
		Log.i("Starting","GetID");
		Request request = Request.newMeRequest(mSimpleFacebook.getSession(), 
	            new Request.GraphUserCallback() {
			
	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	        	Log.i("OnCompleted","GetID");
	            if (mSimpleFacebook.getSession() == Session.getActiveSession()) {
	                if (user != null) {
	                    // Set the id for the ProfilePictureView
	                    // view that in turn displays the profile picture.
	                	Log.i("User","NotNULL");
	                  id=user.getId();
	                 
	                  
	                  
	                }
	            }
	            if (response.getError() != null) {
	                // Handle errors, will do so later.
	            	Log.i("Error","GetID");
	            }
	        }
	    });
	    request.executeAsync();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
