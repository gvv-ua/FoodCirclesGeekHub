package co.foodcircles.util;

import android.app.Application;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.Arrays;
import java.util.List;

import co.foodcircles.json.Voucher;
import io.fabric.sdk.android.Fabric;

public class FoodCirclesApplication extends Application
{
	public Voucher newVoucher;
	public String purchasedOffer;
	public int purchasedCost;
	public int purchasedGroupSize;
	public boolean purchasedVoucher = false;
	public boolean needsRestart = false;

	private List<String> permissions = Arrays.asList("email", "public_profile");

	public List<String> getPermissions() {
		return permissions;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		final TwitterAuthConfig authConfig = new TwitterAuthConfig(Const.CONSUMER_KEY, Const.CONSUMER_SECRET);
		Fabric.with(this, new Twitter(authConfig));
	}

	public Voucher getNewVoucher() {
		return newVoucher;
	}

	public String getPurchasedOffer() {
		return purchasedOffer;
	}

	public int getPurchasedCost() {
		return purchasedCost;
	}

	public int getPurchasedGroupSize() {
		return purchasedGroupSize;
	}

	public boolean isPurchasedVoucher() {
		return purchasedVoucher;
	}

	public boolean isNeedsRestart() {
		return needsRestart;
	}
}
