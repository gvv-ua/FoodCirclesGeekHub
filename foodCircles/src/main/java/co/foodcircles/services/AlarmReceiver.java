package co.foodcircles.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import co.foodcircles.R;
import co.foodcircles.activities.MP;
import co.foodcircles.activities.SignInActivity;
import co.foodcircles.json.Offer;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.net.NetException;
import co.foodcircles.util.LocationCoordinate;
import co.foodcircles.util.SortListByDistance;

public class AlarmReceiver extends BroadcastReceiver {
    private final static String SOMEACTION = "co.foodcircles.geonotification";
    private final static String TAG = "AlarmReceiver";
    private final static long MS_BEFORE_NOTIFY = AlarmManager.INTERVAL_DAY * 7;
    private MixpanelAPI mixpanel;
    private GoogleApiClient googleApiClient;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (SOMEACTION.equals(action)) {
            mixpanel = MixpanelAPI.getInstance(context, context.getResources().getString(R.string.mixpanel_token));
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            tryGeoNotify(context, new LocationCoordinate(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
                            googleApiClient.disconnect();
                        }

                        @Override
                        public void onConnectionSuspended(int i) { }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            tryGeoNotify(context, new LocationCoordinate(null));
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    private void tryGeoNotify(final Context context, final LocationCoordinate locationCoordinate) {
        Log.d(TAG, String.format("tryGeoNotify: %f, %f", locationCoordinate.getLatitude(), locationCoordinate.getLongitude()));
        if (timeSinceLastNotification(context) > MS_BEFORE_NOTIFY && isOnline(context))
        {
            try {
                MP.track(mixpanel, "Notification", "Attempting");
                if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                    MP.track(mixpanel, "Notification", "Acquired location");
                    new AsyncTask<Void, Void, Venue>() {
                        @Override
                        protected Venue doInBackground(Void... params) {
                            try {
                                List<Venue> venues = new ArrayList<>();
                                venues.addAll(Net.getVenues(locationCoordinate));
                                Collections.sort(venues, new SortListByDistance());
                                if (venues.size() > 0) {
                                    Venue venue = venues.get(0);
                                    String loc = venue.getDistance();
                                    String distance = loc.replaceAll("[^\\d.]", "");
                                    return (Double.parseDouble(distance) < 10) ? venue : null;
                                } else {
                                    return null;
                                }
                            } catch (NetException e) {
                                MP.track(mixpanel, "Notification", "Failed to get venues");
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(Venue venue) {
                            if (venue != null) {
                                String deal = "a deal";
                                for (Offer offer : venue.getOffers()) {
                                    if (offer.getMinDiners() == 2) deal = offer.getTitle();
                                }
                                MP.track(mixpanel, "Notification", "Geo notification displayed");
                                makeNotification(context, venue.getName() + " is close by!", deal + " for just $1!");
                                setNotifiedTime(context);
                            } else {
                                MP.track(mixpanel, "Notification", "Generic notification displayed");
                                makeNotification(context, "Your hunger is powerful", "Feed a child for $1.");
                                setNotifiedTime(context);
                            }
                        }
                    }.execute();
                } else if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                    MP.track(mixpanel, "Notification", "Generic notification displayed");
                    makeNotification(context, "Your hunger is powerful", "Feed a child for $1.");
                    setNotifiedTime(context);
                }
            } catch (Exception e) {
                makeNotification(context, "Hungry?", "Buy one feed one with FoodCircles!");
            }
        }
    }

    private void makeNotification(Context context, String title, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.logo).setContentTitle(title).setContentText(message);
        mBuilder.setSmallIcon(R.drawable.ic_stat_android_notification);
        Intent rateIntent = new Intent(context, SignInActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, rateIntent, 0);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

    }

    private void setNotifiedTime(Context context) {
        SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.preferences), Context.MODE_PRIVATE);
        Editor edit = pref.edit();
        edit.putLong(context.getResources().getString(R.string.last_notification), Calendar.getInstance().getTimeInMillis());
        edit.apply();
    }

    private long timeSinceLastNotification(Context context) {
        SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.preferences), Context.MODE_PRIVATE);
        long lastNotification = pref.getLong(context.getResources().getString(R.string.last_notification), 0);
        long now = Calendar.getInstance().getTimeInMillis();
        return now - lastNotification;
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }
}