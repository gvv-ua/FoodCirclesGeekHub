package co.foodcircles.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class VenueItemFragment extends Fragment
{
	ImageView itemImage;
	TextView itemName;
	TextView itemFlavorText;
	TextView itemOriginalPrice;
	Button button;

    private boolean mIsVenueNeedToReserve;

    private boolean mIsSubscribed;

    private ProgressDialog progressDialog;

    private String mVenueName;

    private boolean mContinueBrowsing;

    public static VenueItemFragment newInstance(boolean isVenueOnReserve) {
        VenueItemFragment fragment = new VenueItemFragment();
        Bundle args = new Bundle();
        args.putSerializable(RestaurantActivity.IS_VENUE_ON_RESERVE_KEY, isVenueOnReserve);
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
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.venue_profile, null);
		FontSetter.overrideFonts(getActivity(), view);

		itemImage = (ImageView) view.findViewById(R.id.imageView);

		itemName = (TextView) view.findViewById(R.id.textViewItemName);
		itemOriginalPrice = (TextView) view.findViewById(R.id.textViewPrice);
		itemFlavorText = (TextView) view.findViewById(R.id.textViewItemFlavorText);
		button = (Button) view.findViewById(R.id.button);

        if (mIsVenueNeedToReserve) {
            button.setText(getString(R.string.venue_profile_btn_keep_me_posted));
            view.findViewById(R.id.ll_venue_days_left).setVisibility(View.VISIBLE);
            calculateDaysLeft(view);
        }

		final FoodCirclesApplication app = (FoodCirclesApplication) getActivity().getApplicationContext();
		final Venue venue = app.selectedVenue;
        mVenueName = venue.getName();

        if (venue.getVouchersAvailable() == 0) {
            isSubscribed(venue.getSlug());
        }
		
		Offer offer = venue.getOffers().get(0);
		itemImage.setTag(Net.HOST + venue.getImageUrl());
		new DownloadImagesTask().execute(itemImage);
		itemName.setText(offer.getTitle());
		itemFlavorText.setText(offer.getDetails());
		try {
		itemOriginalPrice.setText("" + offer.getFullPrice());
		} catch (Exception e) {			
			itemOriginalPrice.setText("9");
		}
		
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
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

		return view;
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
                String response = Net.unsubscribeVenue(slug, FoodCirclesUtils.getToken(getActivity()));
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
        getActivity().startActivity(new Intent(getActivity(), BuyOptionsActivity.class));
        app.addPoppableActivity(getActivity());
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


    public class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {
		ImageView imageView = null;

		@Override
		protected Bitmap doInBackground(ImageView... imageViews) {
		    this.imageView = imageViews[0];
		    return download_Image((String)imageView.getTag());
		}

		@Override
		protected void onPostExecute(Bitmap result) {
		    imageView.setImageBitmap(result);
		}

		private Bitmap download_Image(String src) {
	        try {
	            URL url = new URL(src);
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setDoInput(true);
	            connection.connect();
	            InputStream input = connection.getInputStream();
	            Bitmap myBitmap = BitmapFactory.decodeStream(input);
	            return myBitmap;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
		}
	}
}
