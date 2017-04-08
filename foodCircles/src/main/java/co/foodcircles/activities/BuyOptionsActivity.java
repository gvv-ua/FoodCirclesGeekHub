package co.foodcircles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalService;

import co.foodcircles.R;
import co.foodcircles.json.Venue;

public class BuyOptionsActivity extends FragmentActivity
{
	private static final String TAG = "BuyOptionsActivity";
	private Venue venue;

	//private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
	//private static final String CONFIG_CLIENT_ID = "ATtEOxB-eX60pOi_fHSv3K2PvAX8LRme-eyngA9l6LRSTIr9SeJHtmpaJL4M";

	private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
	private static final String CONFIG_CLIENT_ID = "AfDW-B4dbATU95f68UknFHxlnKY0OoTEqI8a4ezyRR2Yk_t93DAcyuI-VMs3tajupW2zjqRTk22debxm";


	private static PayPalConfiguration config = new PayPalConfiguration()
			.environment(CONFIG_ENVIRONMENT)
			.clientId(CONFIG_CLIENT_ID);


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
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		intent.putExtra(RestaurantActivity.SELECTED_VENUE_KEY, venue);
		startService(intent);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.buy_options_activity);
		BuyFragment buyFragment = new BuyFragment();
		buyFragment.setArguments(intent.getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, buyFragment).commit();
		Log.i("OnCreate","1");
	}

	@Override
	public void onDestroy()
	{
		stopService(new Intent(this, PayPalService.class));
		super.onDestroy();
	}

	public static PayPalConfiguration getConfig() {
		return config;
	}
}
