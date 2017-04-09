package co.foodcircles.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

import java.util.List;

import co.foodcircles.R;
import co.foodcircles.json.Social;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.util.FontSetter;

public class VenueProfileFragment extends Fragment implements OnMarkerClickListener, OnMapClickListener, OnInfoWindowClickListener, OnMapReadyCallback {
    private static final String TAG = "VenueProfileFragment";
    private Venue venue;
    private GoogleMap map;
    private MarkerOptions destinationMarker;
    private MixpanelAPI mixpanel;

    private ImageView facebookView;
    private ImageView twitterView;
    private ImageView yelpView;

    public static VenueProfileFragment newInstance(Venue venue) {
        VenueProfileFragment fragment = new VenueProfileFragment();
        Bundle args = new Bundle();
        if (venue != null) {
            args.putParcelable(RestaurantActivity.SELECTED_VENUE_KEY, venue);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            venue = getArguments().getParcelable(RestaurantActivity.SELECTED_VENUE_KEY);
        }
    }

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
        return inflater.inflate(R.layout.restaurant_profile, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FontSetter.overrideFonts(getActivity(), view);
        ((TextView) view.findViewById(R.id.textViewName)).setText(venue.getName());
        ((TextView) view.findViewById(R.id.textViewTags)).setText(venue.getTagsString());
        ((TextView) view.findViewById(R.id.textViewHours)).setText(String.format(getString(R.string.hours_colon), venue.getOpenTimes()));
        ((TextView) view.findViewById(R.id.textViewDescription)).setText(venue.getDescription());
        ((TextView) view.findViewById(R.id.textViewAddress)).setText(venue.getAddress());
        ImageView itemImageSmall = (ImageView) view.findViewById(R.id.itemImageSmall);
        Glide.with(getActivity()).load(Net.HOST + venue.getImageUrl()).into(itemImageSmall);
        (view.findViewById(R.id.buttonCall)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MP.track(mixpanel, "Clicked Call");
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + venue.getPhone()));
                startActivity(intent);
            }
        });

        (view.findViewById(R.id.buttonWebsite)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MP.track(mixpanel, "Clicked Website");
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(venue.getWeb()));
                startActivity(i);
            }
        });

        facebookView = (ImageView) view.findViewById(R.id.imageViewFacebook);
        twitterView = (ImageView) view.findViewById(R.id.imageViewTwitter);
        yelpView = (ImageView) view.findViewById(R.id.imageViewYelp);

        prepareSocialButtons();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void prepareSocialButtons() {
        facebookView.setVisibility(View.INVISIBLE);
        twitterView.setVisibility(View.INVISIBLE);
        yelpView.setVisibility(View.INVISIBLE);

        //Based on the venue's social links, checks for the social button values and makes their buttons visible and clickable.
        List<Social> socials = venue.getSocial();
        if (socials != null) {
            for (final Social currentSocial: socials) {
                if (currentSocial.getType().trim().equals("facebook")) {
                    facebookView.setVisibility(View.VISIBLE);
                    facebookView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MP.track(mixpanel, "Clicked Facebook");
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(currentSocial.getURL()));
                            startActivity(i);
                        }
                    });
                } else if (currentSocial.getType().trim().equals("twitter")) {
                    twitterView.setVisibility(View.VISIBLE);
                    twitterView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MP.track(mixpanel, "Clicked Twitter");
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(currentSocial.getURL()));
                            startActivity(i);
                        }
                    });

                } else if (currentSocial.getType().trim().equals("yelp")) {
                    yelpView.setVisibility(View.VISIBLE);
                    yelpView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MP.track(mixpanel, "Clicked Yelp");
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(currentSocial.getURL()));
                            startActivity(i);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        map.addMarker(destinationMarker).showInfoWindow();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        onMarkerClick(marker);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
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
}
