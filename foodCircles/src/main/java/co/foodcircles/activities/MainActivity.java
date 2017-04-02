package co.foodcircles.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.viewpagerindicator.TabPageIndicator;

import co.foodcircles.R;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.LocationCoordinate;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

//import com.sromku.simple.fb.SimpleFacebook;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 11;

    public static final String CURRENT_TAB = "tab";
    private static final String TAG = "MainActivity";
    public static final int TAB_NEWS = 0;
    public static final int TAB_RESTAURANTS = 1;
    public static final int TAB_TIMELINE = 2;

    private static final String[] CONTENT = new String[]{"NEWS", "FOOD", "YOU"};
    private FoodCirclesApplication app;
    //	SimpleFacebook mSimpleFacebook;
    private GoogleApiClient googleApiClient;
    private TabPageIndicator indicator;
    private int currentTab = TAB_NEWS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.simple_tabs);
        app = (FoodCirclesApplication) getApplicationContext();
        indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setVisibility(View.GONE);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //This launches the receipt fragment and reloads the application
        if (app.needsRestart) {
            app.needsRestart = false;
            MainActivity.this.finish();
            Intent intent = getIntent();
            intent.putExtra(MainActivity.CURRENT_TAB, currentTab);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkLocationPermission()) {
            updateTabs(new LocationCoordinate(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                showExplanationDialog(getString(R.string.need_gps_permission), new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        updateTabs(new LocationCoordinate(null));
    }

    private void updateTabs(LocationCoordinate locationCoordinate) {
        FragmentPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(), locationCoordinate);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        pager.setCurrentItem(getIntent().getIntExtra(CURRENT_TAB, currentTab));
        FontSetter.overrideFonts(this, findViewById(R.id.root));

        if (app.purchasedVoucher) {
            FragmentManager fm = getSupportFragmentManager();
            ReceiptDialogFragment receiptDialog = new ReceiptDialogFragment();
            receiptDialog.show(fm, "receipt_dialog");
            pager.setCurrentItem(TAB_TIMELINE);
        }
        indicator.setVisibility(View.VISIBLE);
    }

    class MainPagerAdapter extends FragmentPagerAdapter {
        private final LocationCoordinate locationCoordinate;

        public MainPagerAdapter(FragmentManager fm, LocationCoordinate locationCoordinate) {
            super(fm);
            this.locationCoordinate = locationCoordinate;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CarouselFragment();
                case 1:
                    return RestaurantListFragment.newInstance(locationCoordinate);
                case 2:
                    return new TimelineFragment();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //SimpleFacebook.getInstance(this).onActivityResult(this, requestCode, resultCode, data);
    }

    private boolean checkLocationPermission() {
        return (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED);
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
                    updateTabs(new LocationCoordinate(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
                } else {
                    Toast.makeText(this, R.string.need_gps_permission, Toast.LENGTH_SHORT).show();
                }
            }
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    updateTabs(new LocationCoordinate(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
                } else {
                    Toast.makeText(this, R.string.need_location_permission, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
