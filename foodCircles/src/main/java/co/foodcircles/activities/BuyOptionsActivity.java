package co.foodcircles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;

import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;

import co.foodcircles.R;
import co.foodcircles.json.Venue;

public class BuyOptionsActivity extends FragmentActivity
{
	private static final String TAG = "BuyOptionsActivity";
	private Venue venue;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("OnCreate","1");

		Intent intent = getIntent();
		if (intent != null) {
			venue = intent.getParcelableExtra(RestaurantActivity.SELECTED_VENUE_KEY);
		}

		intent = new Intent(this, PayPalService.class);
		intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, PaymentActivity.ENVIRONMENT_SANDBOX);
		intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, "AS2vSxDNmRzYZ9qH7hiSWAEbpabaDmpKmLlglqJtOKDh5KLBZ6a4N5Ex-ql4");
		intent.putExtra(RestaurantActivity.SELECTED_VENUE_KEY, venue);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.buy_options_activity);
		BuyFragment buyFragment = new BuyFragment();
		buyFragment.setArguments(intent.getExtras());
		//getSupportFragmentManager().beginTransaction().add(R.id.fragment1, buyFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, buyFragment).commit();
		Log.i("OnCreate","1");
	}

	@Override
	public void onDestroy()
	{
		stopService(new Intent(this, PayPalService.class));
		super.onDestroy();
	}
}
