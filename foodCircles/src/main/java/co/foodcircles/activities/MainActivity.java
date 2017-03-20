package co.foodcircles.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.app.AlertDialog;
import android.view.Window;
import android.widget.Toast;

//import com.sromku.simple.fb.SimpleFacebook;
import com.viewpagerindicator.TabPageIndicator;

import co.foodcircles.R;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends FragmentActivity implements AndroidUtils.GetLocations
{
	public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
	public static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 11;

	private static final String[] CONTENT = new String[] { "NEWS", "FOOD", "YOU" };
	ViewPager pager;
	FoodCirclesApplication app;
	public static Activity mActivity;
//	SimpleFacebook mSimpleFacebook;
	android.location.Location locationGPS = null;
	android.location.Location locationNet = null;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.simple_tabs);
		mActivity=this;
		app = (FoodCirclesApplication) getApplicationContext();
		FragmentPagerAdapter adapter = new GoogleMusicAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		pager.setCurrentItem(getIntent().getIntExtra("tab", 0));
		FontSetter.overrideFonts(this, findViewById(R.id.root));
		if (app.purchasedVoucher)
		{
			FragmentManager fm = getSupportFragmentManager();
			ReceiptDialogFragment receiptDialog = new ReceiptDialogFragment();
			receiptDialog.show(fm, "receipt_dialog");
			pager.setCurrentItem(2);
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		//This launches the receipt fragment and reloads the application
		if (app.needsRestart)
		{
			app.needsRestart = false;
			MainActivity.this.finish();
			startActivity(getIntent());
		}
	}

	class GoogleMusicAdapter extends FragmentPagerAdapter
	{
		public GoogleMusicAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			switch (position)
			{
				case 0:
					return new CarouselFragment();
				case 1:
					return new RestaurantListFragment();
				case 2:
					return new TimelineFragment();
				default:
					return null;
			}
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return CONTENT[position % CONTENT.length].toUpperCase();
		}

		@Override
		public int getCount()
		{
			return CONTENT.length;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//SimpleFacebook.getInstance(this).onActivityResult(this, requestCode, resultCode, data);
	}

	public void setLocationGPS() {
		if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
			LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} else {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
				showExplanationDialog(getString(R.string.need_gps_permission), new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
			} else {
				ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
			}
		}
	}

	public void setLocationNet() {
		if (ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
			LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		} else {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
				showExplanationDialog(getString(R.string.need_location_permission), new String[]{ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
			} else {
				ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
			}
		}
	}

	private void showExplanationDialog(final String title, final String[] permissions, final int requestCode) {
		new AlertDialog.Builder(this)
				.setMessage(title)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						ActivityCompat.requestPermissions(MainActivity.this, permissions, requestCode);
					}
				})
				.setNegativeButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						openPermissionSettings();
					}
				})
				.create()
				.show();
	}

	private void openPermissionSettings() {
		final Intent intent = new Intent();
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(Uri.parse("package:" + getPackageName()));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				} else {
					Toast.makeText(this, R.string.need_gps_permission, Toast.LENGTH_SHORT).show();
				}
			}
			case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				} else {
					Toast.makeText(this, R.string.need_location_permission, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public Location getLocationGPS() {
		return locationGPS;
	}

	public Location getLocationNet() {
		return locationNet;
	}
}
