package co.foodcircles.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import co.foodcircles.R;
import co.foodcircles.activities.MP;
import co.foodcircles.activities.SignInActivity;
import co.foodcircles.json.Offer;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.net.NetException;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.SortListByDistance;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AlarmReceiver extends BroadcastReceiver
{
	List<Venue> venues=new ArrayList<Venue>();
	private final String SOMEACTION = "co.foodcircles.geonotification";
	private final static long MS_BEFORE_NOTIFY = AlarmManager.INTERVAL_DAY * 7;
	private MixpanelAPI mixpanel;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if (SOMEACTION.equals(action))
		{
			mixpanel = MixpanelAPI.getInstance(context, context.getResources().getString(R.string.mixpanel_token));
			tryGeoNotify(context);
		}
	}

	public void tryGeoNotify(final Context context)
	{
		if (timeSinceLastNotification(context) > MS_BEFORE_NOTIFY && isOnline(context))
		{
			try {
				MP.track(mixpanel, "Notification", "Attempting");
				if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY)
				{
					MP.track(mixpanel, "Notification", "Acquired location");
					new AsyncTask<Void, Void, String>()
					{
						@Override
						protected String doInBackground(Void... params)
						{
							try
							{

								//Checks for venues near the user's location
								LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
								Location locationGPS = null;
								if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
									locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
								}
								Location locationNet = null;
								if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
									locationNet = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
								}
								Location location = AndroidUtils.getLastBestLocation(locationGPS, locationNet);
								
								
								if(location==null){
									venues.addAll(Net.getVenues(-85.632823,42.955202,null));
								}else{
									venues.addAll(Net.getVenues(location.getLongitude(),location.getLatitude(),null));								
								}
								Collections.sort(venues,new SortListByDistance());
								String loc = venues.get(0).getDistance();
								String nums = loc.replaceAll("[^\\d.]", "");
								if (venues.size() > 0) {
									return (Double.parseDouble(nums) < 10 ) ? venues.get(0).getName() : "";
								} else {
									return "";
								}
							}
							catch (NetException e)
							{
								MP.track(mixpanel, "Notification", "Failed to get venues");
								return "";
							}
						}
	
						@Override
						protected void onPostExecute(String name)
						{
							if (!name.equals(""))
							{
								String deal="a deal";
								for(Offer offer :venues.get(0).getOffers())
								{
									if(offer.getMinDiners()==2) deal=offer.getTitle();
								}
								MP.track(mixpanel, "Notification", "Geo notification displayed");
								makeNotification(context, name + " is close by!", deal +" for just $1!");
								setNotifiedTime(context);
								
							}
							else 
							{
								MP.track(mixpanel, "Notification", "Generic notification displayed");
								makeNotification(context, "Your hunger is powerful", "Feed a child for $1.");
								setNotifiedTime(context);
							}
						}
					}.execute();
				}
				else if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
				{
					MP.track(mixpanel, "Notification", "Generic notification displayed");
					makeNotification(context, "Your hunger is powerful", "Feed a child for $1.");
					setNotifiedTime(context);
				}
			} catch (Exception e) {
				makeNotification(context, "Hungry?", "Buy one feed one with FoodCircles!");
			}
		}
	}

	public void makeNotification(Context context, String title, String message)
	{
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.logo).setContentTitle(title).setContentText(message);
		mBuilder.setSmallIcon(R.drawable.ic_stat_android_notification);
		Intent rateIntent = new Intent(context, SignInActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, rateIntent, 0);
		mBuilder.setAutoCancel(true);
		mBuilder.setContentIntent(pendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());
		
	}

	public void setNotifiedTime(Context context)
	{
		SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.preferences), Context.MODE_PRIVATE);
		Editor edit = pref.edit();
		edit.putLong(context.getResources().getString(R.string.last_notification), Calendar.getInstance().getTimeInMillis());
		edit.commit();
	}

	public long timeSinceLastNotification(Context context)
	{
		SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.preferences), Context.MODE_PRIVATE);
		long lastNotification = pref.getLong(context.getResources().getString(R.string.last_notification), 0);
		long now = Calendar.getInstance().getTimeInMillis();
		return now - lastNotification;
	}

	public boolean isOnline(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnectedOrConnecting()) ? true : false;
	}

	public Location getPassiveLocation(Context context)
	{
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
	}
}