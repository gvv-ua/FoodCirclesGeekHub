package co.foodcircles.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.foodcircles.R;
import co.foodcircles.json.Charity;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.SortListByDistance;

public class RestaurantListFragment extends Fragment {
    @BindView(R.id.gridView)
    GridView gridView;

    @BindView(R.id.pb_weekly_goal)
    ProgressBar mPbWeeklyGoal;

    @BindView(R.id.tv_amount_kids_aided)
    TextView mTvKidsAidedAmount;

    @BindView(R.id.tv_meals_weekly_goal)
    TextView mTvMealsWeeklyGoal;

    private VenueAdapter adapter;

    private ProgressDialog progressDialog;
    private String TAG = "RestaurantGridFragment";
    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();
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
        ButterKnife.bind(this, view);
        FontSetter.overrideFonts(getActivity(), view);
        app = (FoodCirclesApplication) getActivity().getApplicationContext();
        options = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().showStubImage(R.drawable.transparent_box).showImageForEmptyUri(R.drawable.transparent_box)
                .showImageOnFail(R.drawable.transparent_box).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getApplicationContext()).threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(2 * 1024 * 1024).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator())
                .imageDownloader(new BaseImageDownloader(getActivity().getApplicationContext())).enableLogging().build();
        ImageLoader.getInstance().init(config);
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
                    mPbWeeklyGoal.setProgress(venue.getPeopleAided());
                    mPbWeeklyGoal.setMax(venue.getWeeklyGoal());
                    mTvKidsAidedAmount.setText("" + venue.getPeopleAided());
                    String weeklyGoal = getString(R.string.number_meals, venue.getWeeklyGoal());
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
                        adapter.notifyDataSetChanged();
                    }
                }
            }.execute();
        }

        adapter = new VenueAdapter(app.venues);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                FoodCirclesApplication app = (FoodCirclesApplication) RestaurantListFragment.this.getActivity().getApplicationContext();
                app.selectedVenue = app.venues.get(position);
                if (app.selectedVenue.getVouchersAvailable() == 0) {
                    Intent intent = new Intent(RestaurantListFragment.this.getActivity(), RestaurantActivity.class);
                    intent.putExtra(RestaurantActivity.IS_VENUE_ON_RESERVE_KEY, true);
                    startActivity(intent);
                } else if (app.selectedVenue.getOffers().isEmpty()) {
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
                    startActivity(intent);
                }
            }
        });
        return view;
    }

    private class VenueAdapter extends BaseAdapter {
        List<Venue> venues;

        private class ViewHolder {
            public ImageView logo;
            public TextView name;
            public TextView cuisine;
            public TextView distance;
            public TextView left;
            public TextView soldOut;
        }

        public VenueAdapter(List<Venue> venues) {
            this.venues = venues;
        }

        @Override
        public int getCount() {
            return venues.size();
        }

        @Override
        public Object getItem(int index) {
            return venues.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        //populates the list for each venue
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            Venue venue = venues.get(position);
            if (convertView == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.polaroid, parent, false);
                FontSetter.overrideFonts(parent.getContext(), view);
                holder = new ViewHolder();
                //loads the venue views
                holder.logo = (ImageView) view.findViewById(R.id.imageViewLogo);
                holder.name = (TextView) view.findViewById(R.id.textViewName);
                holder.cuisine = (TextView) view.findViewById(R.id.textViewCuisine);
                holder.soldOut = (TextView) view.findViewById(R.id.SoldOutText);
                holder.distance = (TextView) view.findViewById(R.id.textViewDistance);
                holder.left = (TextView) view.findViewById(R.id.textViewLeft);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (position == 0) Collections.sort(venues, new SortListByDistance());
            imageLoader.displayImage(Net.HOST + venues.get(position).getImageUrl(), holder.logo, options);
            venue.getName();
            holder.name.setText(venue.getName());
            holder.cuisine.setText(venue.getFirstTag());
            holder.distance.setText(venue.getDistance());
            holder.left.setText("" + venue.getVouchersAvailable());
            holder.soldOut.setVisibility(venue.checkEmpty());

            return view;
        }

    }
}
