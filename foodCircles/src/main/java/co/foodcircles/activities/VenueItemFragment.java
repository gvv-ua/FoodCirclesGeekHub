package co.foodcircles.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Calendar;

import co.foodcircles.R;
import co.foodcircles.json.Offer;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.FoodCirclesUtils;

/**
 * This fragment is the view that gives detailed information about the deal,
 * including a picture and venue info
 */
public class VenueItemFragment extends Fragment {
    private Button button;
    private boolean mIsVenueNeedToReserve;
    private Venue venue;

    private boolean mIsSubscribed;

    private ProgressDialog progressDialog;

    private String mVenueName;

    private boolean mContinueBrowsing;

    public static VenueItemFragment newInstance(boolean isVenueOnReserve, Venue venue) {
        VenueItemFragment fragment = new VenueItemFragment();
        Bundle args = new Bundle();
        args.putSerializable(RestaurantActivity.IS_VENUE_ON_RESERVE_KEY, isVenueOnReserve);
        args.putParcelable(RestaurantActivity.SELECTED_VENUE_KEY, venue);
        fragment.setArguments(args);
        return fragment;
    }

    public VenueItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsVenueNeedToReserve
                    = getArguments().getBoolean(RestaurantActivity.IS_VENUE_ON_RESERVE_KEY);
            venue = getArguments().getParcelable(RestaurantActivity.SELECTED_VENUE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.venue_profile, null);
        FontSetter.overrideFonts(getActivity(), view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView itemImage = (ImageView) view.findViewById(R.id.ivVenue);

        TextView itemName = (TextView) view.findViewById(R.id.textViewItemName);
        TextView itemOriginalPrice = (TextView) view.findViewById(R.id.textViewPrice);
        TextView itemFlavorText = (TextView) view.findViewById(R.id.textViewItemFlavorText);
        button = (Button) view.findViewById(R.id.button);

        if (mIsVenueNeedToReserve) {
            button.setText(getString(R.string.venue_profile_btn_keep_me_posted));
            view.findViewById(R.id.ll_venue_days_left).setVisibility(View.VISIBLE);
            calculateDaysLeft(view);
        }

        final FoodCirclesApplication app = (FoodCirclesApplication) getActivity().getApplicationContext();
        mVenueName = venue.getName();

        if (venue.getVouchersAvailable() == 0) {
            isSubscribed(venue.getSlug());
        }


        Offer offer = venue.getOffers().get(0);
        Glide.with(getActivity()).load(Net.HOST + venue.getLargeImageUrl()).into(itemImage);

        itemName.setText(offer.getTitle());
        itemFlavorText.setText(offer.getDetails());
        try {
            itemOriginalPrice.setText(String.format("%d", offer.getFullPrice()));
        } catch (Exception e) {
            itemOriginalPrice.setText("9");
        }

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsVenueNeedToReserve) {
                    if (mIsSubscribed) {
                        unsubscribe(venue.getSlug());
                    } else {
                        reserveVenue(venue.getSlug());
                    }
                } else {
                    startBuyOptionsActivity(app);
                }
            }
        });

    }

    private void reserveVenue(final String slug) {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait", "Reserving venues...");
        new AsyncTask<Object, Void, Boolean>() {
            protected Boolean doInBackground(Object... param) {
                try {
                    Net.subscribeVenue(slug, FoodCirclesUtils.getToken(getActivity()));
                    return true;
                } catch (Exception e) {
                    Log.v("", "Error in venue subscribing", e);
                    return false;
                }
            }

            protected void onPostExecute(Boolean success) {
                progressDialog.dismiss();
                if (success) {
                    startReservedActivity();
                }
            }
        }.execute();
    }

    private void startReservedActivity() {
        Intent intent = new Intent(getActivity(), ReservedActivity.class);
        intent.putExtra("venue_name", mVenueName);
        startActivity(intent);
        mContinueBrowsing = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContinueBrowsing) {
            getActivity().finish();
        }
    }

    private void unsubscribe(final String slug) {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait", "Discard reserving...");
        new AsyncTask<Object, Void, Boolean>() {
            protected Boolean doInBackground(Object... param) {
                Net.unsubscribeVenue(slug, FoodCirclesUtils.getToken(getActivity()));
                return true;
            }

            protected void onPostExecute(Boolean success) {
                progressDialog.dismiss();
                if (success) {
                    button.setText(getString(R.string.venue_profile_btn_keep_me_posted));
                    button.setBackgroundResource(R.drawable.keepmeposted_btn_selector);
                    mIsSubscribed = false;
                }
            }
        }.execute();
    }

    private void isSubscribed(final String slug) {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait", "Checking for subscription...");
        new AsyncTask<Object, Void, Boolean>() {
            protected Boolean doInBackground(Object... param) {
                return Net.isSubscribed(slug, FoodCirclesUtils.getToken(getActivity()));
            }

            protected void onPostExecute(Boolean success) {
                progressDialog.dismiss();
                if (success) {
                    button.setText(getString(R.string.venue_profile_btn_nevermind));
                    button.setBackgroundResource(R.drawable.nevermind_btn_selector);
                    mIsSubscribed = true;
                }
            }
        }.execute();
    }

    private void startBuyOptionsActivity(FoodCirclesApplication app) {
        Intent intent = new Intent(getActivity(), BuyOptionsActivity.class);
        intent.putExtra(RestaurantActivity.SELECTED_VENUE_KEY, venue);
        startActivity(intent);
    }

    private void calculateDaysLeft(View v) {
        Calendar c = Calendar.getInstance();
        int daysLeftUntilSaturday = Calendar.SATURDAY - c.get(Calendar.DAY_OF_WEEK);

        TextView daysLeft = (TextView) v.findViewById(R.id.tv_venue_days_left);
        daysLeft.setText(getString(R.string.venue_days_left, daysLeftUntilSaturday));

        LinearLayout llDaysKeftDots = (LinearLayout) v.findViewById(R.id.ll_days_left_dots);
        for (int i = 0; i < Calendar.SATURDAY - daysLeftUntilSaturday; i++) {
            View dot = llDaysKeftDots.getChildAt(i);
            dot.setBackground(getResources().getDrawable(R.drawable.days_left_white));
        }
    }
}
