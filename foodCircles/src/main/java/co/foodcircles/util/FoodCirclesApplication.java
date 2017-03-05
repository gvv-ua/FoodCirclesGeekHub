package co.foodcircles.util;

import android.app.Activity;
import android.app.Application;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

import java.util.ArrayList;
import java.util.List;

import co.foodcircles.json.Charity;
import co.foodcircles.json.Offer;
import co.foodcircles.json.Reservation;
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
	public List<Reservation> reservations;
	public List<Voucher> vouchers;
	public Reservation currentReservation;
	public boolean needsRestart = false;
	private int totalKidsFed;

	Permission[] permissions = new Permission[] {
		    
		    Permission.EMAIL,
		    Permission.BASIC_INFO
		};
	
	@Override
	public void onCreate() {
		super.onCreate();
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
		   //.setAppId("526839707387980")
           .setAppId("1508523846087006")
		   .setNamespace("foodcirclesapp")
		   .setPermissions(permissions)
		   .build();
		SimpleFacebook.setConfiguration(configuration);
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
}
