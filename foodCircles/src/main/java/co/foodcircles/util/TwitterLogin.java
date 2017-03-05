package co.foodcircles.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import co.foodcircles.activities.EmailPromptsActivity;
import co.foodcircles.activities.MainActivity;
import co.foodcircles.net.Net;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterLogin {

	public static Twitter twitter;
	public static RequestToken requestToken;
	public boolean running = false;
	public TwDialogListener mListener;
	public static String twitter_uid;
	Context mContext;
	private boolean onCompleteCalled;
    private String mNumberOfPeople;

	public TwitterLogin(Context context, String numberOfPeople){
		this.mContext = context;
        mNumberOfPeople = numberOfPeople;
	}
	
	public void twitterSignUp() {
		Thread t = new Thread() {
			public void run() {
				try {
					onCompleteCalled = false;
					ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
					configurationBuilder
							.setOAuthConsumerKey(Const.CONSUMER_KEY);
					configurationBuilder
							.setOAuthConsumerSecret(Const.CONSUMER_SECRET);
					Configuration configuration = configurationBuilder.build();
					twitter = new TwitterFactory(configuration).getInstance();
					requestToken = twitter.getOAuthRequestToken(Const.CALLBACK_URL);
					twitterMsgkHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
	}
	
	public interface TwDialogListener {
		public void onComplete(String value);
		public void onError(String value);
	}
	
	private Handler twitterMsgkHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int id = msg.what;
			switch (id) {
			case 0:
				showLoginDialog(requestToken.getAuthenticationURL());
				break;
			}
		}
	};

	private void showLoginDialog(String url) {
		final TwDialogListener listener = new TwDialogListener() {
			@Override
			public void onComplete(String value) {
				if (onCompleteCalled == false) {
					processToken(value);
					onCompleteCalled = true;
				}
			}

			@Override
			public void onError(String value) {
				try{
					mListener.onError("Failed opening authorization page");
				}catch (NullPointerException e) {
					e.printStackTrace();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		new TwitterDialog(mContext, url, listener).show();
	}

	public void processToken(final String callbackUrl) {
		new Thread() {
			@Override
			public void run() {
				Uri uri = Uri.parse(callbackUrl);
				if (uri != null
						&& uri.toString().startsWith(Const.CALLBACK_URL)) {
					String verifier = uri
							.getQueryParameter(Const.IEXTRA_OAUTH_VERIFIER);
					try {
						AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
						String s = String.valueOf(accessToken.getUserId());
						final String token = Net.twittersignIn(s);
						if(FoodCirclesUtils.getEmail(mContext).isEmpty()){
							
							Intent in = new Intent(mContext,EmailPromptsActivity.class);
							in.putExtra("UID", accessToken.getUserId());
                            in.putExtra("peopleNumber", mNumberOfPeople);
							mContext.startActivity(in);
						}else{
							
							FoodCirclesUtils.saveToken(mContext, token);
							gotoSignedInPage();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	private void gotoSignedInPage() {
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.putExtra("tab", 1);
		mContext.startActivity(intent);
		mContext = null;
	}
}
