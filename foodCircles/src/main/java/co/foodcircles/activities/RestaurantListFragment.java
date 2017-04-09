package co.foodcircles.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.List;

import co.foodcircles.R;
import co.foodcircles.adapters.VenueAdapter;
import co.foodcircles.data.CharityList;
import co.foodcircles.data.VenueList;
import co.foodcircles.json.Venue;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.LocationCoordinate;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class RestaurantListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        VenueList.OnDataUpdateSuccessCallback,
        VenueList.OnDataUpdateFailCallback {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 11;
    private static final String TAG = "RestaurantListFragment";

    private VenueAdapter adapter;
    private ProgressDialog progressDialog;

    private MixpanelAPI mixpanel;
    private GoogleApiClient googleApiClient;

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        mixpanel = MixpanelAPI.getInstance(getActivity(), getResources().getString(R.string.mixpanel_token));
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.polaroid_grid, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        FontSetter.overrideFonts(getActivity(), view);
        FoodCirclesApplication app = (FoodCirclesApplication) getActivity().getApplicationContext();

        RecyclerView gridView = (RecyclerView) getActivity().findViewById(R.id.rvVenues);
        gridView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        adapter = new VenueAdapter(VenueList.getInstance().getVenues(), new VenueAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Venue item) {
                if (item.getVouchersAvailable() == 0) {
                    Intent intent = new Intent(RestaurantListFragment.this.getActivity(), RestaurantActivity.class);
                    intent.putExtra(RestaurantActivity.IS_VENUE_ON_RESERVE_KEY, true);
                    intent.putExtra(RestaurantActivity.SELECTED_VENUE_KEY, item);
                    startActivity(intent);
                } else if (item.getOffers().isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Seems like something's wrong here- check the website for this offer!").setTitle("Oops!");
                    builder.setPositiveButton("OK", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    builder.create().show();
                } else {
                    Intent intent = new Intent(RestaurantListFragment.this.getActivity(), RestaurantActivity.class);
                    intent.putExtra(RestaurantActivity.IS_VENUE_ON_RESERVE_KEY, false);
                    intent.putExtra(RestaurantActivity.SELECTED_VENUE_KEY, item);
                    startActivity(intent);
                }
            }
        });
        gridView.setAdapter(adapter);
        progressDialog = ProgressDialog.show(getActivity(), "Please wait", "Loading venues...");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkLocationPermission()) {
            updateVenues(new LocationCoordinate(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), ACCESS_FINE_LOCATION)) {
                showExplanationDialog(getString(R.string.need_gps_permission), new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    private void updateVenues(final LocationCoordinate locationCoordinate) {
        VenueList.getInstance().updateData(locationCoordinate, this, this);
        if (CharityList.getInstance().getCharities().isEmpty()) {
            CharityList.getInstance().updateData();
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        updateVenues(new LocationCoordinate(null));
    }

    private boolean checkLocationPermission() {
        return (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void showExplanationDialog(final String title, final String[] permissions, final int requestCode) {
        new AlertDialog.Builder(getActivity())
                .setMessage(title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(getActivity(), permissions, requestCode);
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
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    updateVenues(new LocationCoordinate(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
                } else {
                    Toast.makeText(getActivity(), R.string.need_gps_permission, Toast.LENGTH_SHORT).show();
                }
            }
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    updateVenues(new LocationCoordinate(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
                } else {
                    Toast.makeText(getActivity(), R.string.need_location_permission, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setWeeklyGoalData(Venue venue) {
        if (getActivity() != null) {
            ProgressBar mPbWeeklyGoal = (ProgressBar) getActivity().findViewById(R.id.pb_weekly_goal);
            mPbWeeklyGoal.setProgress(venue.getPeopleAided());
            mPbWeeklyGoal.setMax(venue.getWeeklyGoal());
            TextView mTvKidsAidedAmount = (TextView) getActivity().findViewById(R.id.tv_amount_kids_aided);
            mTvKidsAidedAmount.setText(String.format("%d", venue.getPeopleAided()));
            String weeklyGoal = getString(R.string.number_meals, venue.getWeeklyGoal());
            TextView mTvMealsWeeklyGoal = (TextView) getActivity().findViewById(R.id.tv_meals_weekly_goal);
            mTvMealsWeeklyGoal.setText(weeklyGoal);
        }
    }

    @Override
    public void onUpdateVenuesSuccess() {
        List<Venue> venues = VenueList.getInstance().getVenues();
        progressDialog.dismiss();
        if (venues.size() > 0) {
            setWeeklyGoalData(venues.get(0));
            adapter.updateAdapter(venues);
        } else if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Sorry!  We can't find any deals in your area.  Email or call your favorite local restaurant with a conscience and invite them to join FoodCircles.net!").setTitle("No Restaurants!");
            builder.setPositiveButton("OK", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.create().show();
        }
    }

    @Override
    public void onUpdateVenuesFailed() {
        MP.track(mixpanel, "Restaurant List", "Failed to load venues");
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Could not load venues.").setTitle("Network Error");
            builder.setPositiveButton("OK", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    RestaurantListFragment.this.getActivity().finish();
                }
            });
            progressDialog.dismiss();
            builder.create().show();
        }
    }
}
