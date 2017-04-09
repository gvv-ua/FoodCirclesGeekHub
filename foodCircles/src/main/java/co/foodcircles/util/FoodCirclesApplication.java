package co.foodcircles.util;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import java.util.Arrays;
import java.util.List;

import co.foodcircles.json.Voucher;

public class FoodCirclesApplication extends Application
{
	public Voucher newVoucher;
	public String purchasedOffer;
	public int purchasedCost;
	public int purchasedGroupSize;
	public boolean purchasedVoucher = false;
	public boolean needsRestart = false;

	private List<String> permissions = Arrays.asList("email", "public_profile");
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	public List<String> getPermissions() {
		return permissions;
	}
}
