package co.foodcircles.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.Collections;

import co.foodcircles.R;
import co.foodcircles.adapters.VenueAdapter;
import co.foodcircles.json.Charity;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.SortListByDistance;

public class RestaurantListFragment extends Fragment {
    private VenueAdapter adapter;

    private ProgressDialog progressDialog;
    private String TAG = "RestaurantGridFragment";
    private FoodCirclesApplication app;
    MixpanelAPI mixpanel;
    private AndroidUtils.GetLocations getLocations;

    @Override
    public void onStart() {
        super.onStart();
        mixpanel = MixpanelAPI.getInstance(getActivity(), getResources().getString(R.string.mixpanel_token));
    }

    @Override
    public void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.polaroid_grid, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FontSetter.overrideFonts(getActivity(), view);
        app = (FoodCirclesApplication) getActivity().getApplicationContext();

        if (app.venues == null) {
            app.venues = new ArrayList<Venue>();
            progressDialog = ProgressDialog.show(getActivity(), "Please wait", "Loading venues...");
            getLocations = (AndroidUtils.GetLocations)RestaurantListFragment.this.getActivity();
            getLocations.setLocationGPS();
            getLocations.setLocationNet();

            new AsyncTask<Object, Void, Boolean>() {
                protected Boolean doInBackground(Object... param) {
                    try {
                        Location location = AndroidUtils.getLastBestLocation(getLocations.getLocationGPS(), getLocations.getLocationNet());
                        if (location == null) {
                            app.venues.addAll(Net.getVenues(-85.632823, 42.955202, null));
                        } else {
                            app.venues.addAll(Net.getVenues(location.getLongitude(), location.getLatitude(), null));
                        }
                        app.charities = new ArrayList<Charity>();
                        app.charities.addAll(Net.getCharities());
                        return true;
                    } catch (Exception e) {
                        Log.v(TAG, "Error loading venues", e);
                        return false;
                    }
                }

                private void setWeeklyGoalData() {
                    Venue venue = app.venues.get(0);
                    ProgressBar mPbWeeklyGoal = (ProgressBar)getActivity().findViewById(R.id.pb_weekly_goal);
                    mPbWeeklyGoal.setProgress(venue.getPeopleAided());
                    mPbWeeklyGoal.setMax(venue.getWeeklyGoal());
                    TextView mTvKidsAidedAmount = (TextView)getActivity().findViewById(R.id.tv_amount_kids_aided);
                    mTvKidsAidedAmount.setText("" + venue.getPeopleAided());
                    String weeklyGoal = getString(R.string.number_meals, venue.getWeeklyGoal());
                    TextView mTvMealsWeeklyGoal = (TextView)getActivity().findViewById(R.id.tv_meals_weekly_goal);
                    mTvMealsWeeklyGoal.setText("" + weeklyGoal);
                }

                protected void onPostExecute(Boolean success) {
                    setWeeklyGoalData();
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    if (!success) {
                        MP.track(mixpanel, "Restaurant List", "Failed to load venues");
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Could not load venues.").setTitle("Network Error");
                        builder.setPositiveButton("OK", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                RestaurantListFragment.this.getActivity().finish();
                            }
                        });
                        builder.create().show();
                    } else {
                        MP.track(mixpanel, "Restaurant List", "Loaded venues");
                        if (app.venues.size() == 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("Sorry!  We can't find any deals in your area.  Email or call your favorite local restaurant with a conscience and invite them to join FoodCircles.net!").setTitle("No Restaurants!");
                            builder.setPositiveButton("OK", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            builder.create().show();
                        }
                        Collections.sort(app.venues, new SortListByDistance());
                        adapter.notifyDataSetChanged();
                    }
                }
            }.execute();
        }

        RecyclerView gridView = (RecyclerView) getActivity().findViewById(R.id.rvVenues);
        gridView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        adapter = new VenueAdapter(getActivity(), app.venues, new VenueAdapter.ItemClickListener() {
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
    }
}
