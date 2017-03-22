package co.foodcircles.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.foodcircles.json.Charity;
import co.foodcircles.json.Offer;
import co.foodcircles.json.Venue;
import co.foodcircles.json.Voucher;

public class FoodCirclesApplication extends Application
{
	public List<Activity> activities;
	public Venue selectedVenue;
	public Offer selectedOffer;
	public Voucher newVoucher;
	public String purchasedOffer;
	public int purchasedCost;
	public int purchasedGroupSize;
	public boolean purchasedVoucher = false;
	public List<Venue> venues;
	public List<Charity> charities;
	public List<Voucher> vouchers;
	public boolean needsRestart = false;
	private int totalKidsFed;

	private List permissions = Arrays.asList("email", "public_profile");
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	public void addPoppableActivity(Activity activity) {
		if(activities == null) activities = new ArrayList<Activity>();
		activities.add(activity);
	}

	public void newTop() {
		try
		{
			for (Activity activity : activities)
			{
				activity.finish();
			}
		}
		catch (Exception e){ }
		activities = new ArrayList<Activity>();
	}

	public int getTotalKidsFed() {
		return totalKidsFed;
	}

	public void setTotalKidsFed(int totalKidsFed) {
		this.totalKidsFed = totalKidsFed;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	public List getPermissions() {
		return permissions;
	}
}
