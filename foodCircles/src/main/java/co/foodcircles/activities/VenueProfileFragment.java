package co.foodcircles.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import co.foodcircles.R;
import co.foodcircles.json.Social;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;

public class VenueProfileFragment extends Fragment implements OnMarkerClickListener, OnMapClickListener, OnInfoWindowClickListener, OnMapReadyCallback
{
	ImageView itemImageSmall;
	FoodCirclesApplication app;
	Venue venue;
	GoogleMap map;
	MarkerOptions destinationMarker;
	MixpanelAPI mixpanel;

	@Override
	public void onStart()
	{
		super.onStart();
		mixpanel = MixpanelAPI.getInstance(getActivity(), getResources().getString(R.string.mixpanel_token));
	}

	@Override
	public void onDestroy()
	{
		mixpanel.flush();
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.restaurant_profile, null);
		FontSetter.overrideFonts(getActivity(), view);
		app = (FoodCirclesApplication) getActivity().getApplicationContext();
		venue = app.selectedVenue;
		((TextView) view.findViewById(R.id.textViewName)).setText(venue.getName());
		((TextView) view.findViewById(R.id.textViewTags)).setText(venue.getTagsString());
		((TextView) view.findViewById(R.id.textViewHours)).setText("Hours: " + venue.getOpenTimes());
		((TextView) view.findViewById(R.id.textViewDescription)).setText(venue.getDescription());
		((TextView) view.findViewById(R.id.textViewAddress)).setText(venue.getAddress());
		itemImageSmall = (ImageView) view.findViewById(R.id.itemImageSmall);
		itemImageSmall.setTag(Net.HOST + venue.getImageUrl());
		new DownloadImagesTask().execute(itemImageSmall);
		((ImageButton) view.findViewById(R.id.buttonCall)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				MP.track(mixpanel, "Clicked Call");
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:" + venue.getPhone()));
				startActivity(intent);
			}
		});

		((ImageButton) view.findViewById(R.id.buttonWebsite)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				MP.track(mixpanel, "Clicked Website");
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(venue.getWeb()));
				startActivity(i);
			}
		});

		ImageView facebookView = (ImageView) view.findViewById(R.id.imageViewFacebook);
		ImageView twitterView = (ImageView) view.findViewById(R.id.imageViewTwitter);
		ImageView yelpView = (ImageView) view.findViewById(R.id.imageViewYelp);
		facebookView.setVisibility(View.INVISIBLE);
		twitterView.setVisibility(View.INVISIBLE);
		yelpView.setVisibility(View.INVISIBLE);
		
		//Based on the venue's social links, checks for the social button values and makes their buttons visible and clickable.  If there are none, catch the exception.
		try{
			for (int i = 0, ii = this.venue.getSocial().size(); i < ii; i++) {
				final Social currentSocial = venue.getSocial().get(i);
				if (currentSocial.getType().trim().equals("facebook")){
					facebookView.setVisibility(View.VISIBLE);
					facebookView.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v)
						{
							MP.track(mixpanel, "Clicked Facebook");
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(currentSocial.getURL()));
							startActivity(i);
						}
					});
				} else if (currentSocial.getType().trim().equals("twitter")) {
					twitterView.setVisibility(View.VISIBLE);
					twitterView.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							MP.track(mixpanel, "Clicked Twitter");
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(currentSocial.getURL()));
							startActivity(i);
						}
					});
	
				} else if (currentSocial.getType().trim().equals("yelp")) {
					yelpView.setVisibility(View.VISIBLE);
					yelpView.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							MP.track(mixpanel, "Clicked Yelp");
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(currentSocial.getURL()));
							startActivity(i);
						}
					});
				}	
			}
		} catch (Exception e) { }

//		try
//		{
			FragmentManager myFragmentManager = getActivity().getSupportFragmentManager();
			//SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
			getMapFragment().getMapAsync(this);
//		}
//		catch (GooglePlayServicesNotAvailableException e)
//		{
//			Toast.makeText(getActivity(), "Google Play Services are not available at this time. Cannot display map.", Toast.LENGTH_SHORT).show();
//		}
		return view;
	}

    private SupportMapFragment getMapFragment() {
        FragmentManager fm = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            fm = getFragmentManager();
        } else {
            fm = getChildFragmentManager();
        }

        return (SupportMapFragment) fm.findFragmentById(R.id.map);
    }

    @Override
	public void onMapClick(LatLng point)
	{
		map.addMarker(destinationMarker).showInfoWindow();
	}

	@Override
	public void onInfoWindowClick(Marker marker)
	{
		onMarkerClick(marker);
	}

	@Override
	public boolean onMarkerClick(Marker marker)
	{
		MP.track(mixpanel, "Clicked Map");
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + venue.getAddress()));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
		return true;
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		MapsInitializer.initialize(getActivity());
		LatLng destinationLatLng = new LatLng(venue.getLatitude(), venue.getLongitude());
		destinationMarker = new MarkerOptions();
		destinationMarker = destinationMarker.position(destinationLatLng);
		destinationMarker = destinationMarker.title(venue.getName());
		map.addMarker(destinationMarker).showInfoWindow();
		map.setOnMarkerClickListener(this);
		map.setOnMapClickListener(this);
		map.setOnInfoWindowClickListener(this);
		UiSettings settings = map.getUiSettings();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(destinationLatLng, 13.5f, 30f, 112.5f)), 1, null); // bearing
		map.setTrafficEnabled(false);
		settings.setAllGesturesEnabled(false);
		settings.setCompassEnabled(false);
		settings.setMyLocationButtonEnabled(false);
		settings.setRotateGesturesEnabled(false);
		settings.setScrollGesturesEnabled(false);
		settings.setTiltGesturesEnabled(false);
		settings.setZoomControlsEnabled(false);
		settings.setZoomGesturesEnabled(false);

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
		    itemImageSmall.setImageBitmap(result);
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
