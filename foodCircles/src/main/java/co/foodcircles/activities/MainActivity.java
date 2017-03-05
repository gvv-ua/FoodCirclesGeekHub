package co.foodcircles.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import co.foodcircles.R;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;

import com.sromku.simple.fb.SimpleFacebook;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends FragmentActivity
{
	private static final String[] CONTENT = new String[] { "NEWS", "FOOD", "YOU" };
	ViewPager pager;
	FoodCirclesApplication app;
	public static Activity mActivity;
	SimpleFacebook mSimpleFacebook;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.simple_tabs);
		mActivity=this;
		app = (FoodCirclesApplication) getApplicationContext();
		FragmentPagerAdapter adapter = new GoogleMusicAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		pager.setCurrentItem(getIntent().getIntExtra("tab", 0));
		FontSetter.overrideFonts(this, findViewById(R.id.root));
		if (app.purchasedVoucher)
		{
			FragmentManager fm = getSupportFragmentManager();
			ReceiptDialogFragment receiptDialog = new ReceiptDialogFragment();
			receiptDialog.show(fm, "receipt_dialog");
			pager.setCurrentItem(2);
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		//This launches the receipt fragment and reloads the application
		if (app.needsRestart)
		{
			app.needsRestart = false;
			MainActivity.this.finish();
			startActivity(getIntent());
		}
	}

	class GoogleMusicAdapter extends FragmentPagerAdapter
	{
		public GoogleMusicAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			switch (position)
			{
				case 0:
					return new CarouselFragment();
				case 1:
					return new RestaurantListFragment();
				case 2:
					return new TimelineFragment();
				default:
					return null;
			}
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return CONTENT[position % CONTENT.length].toUpperCase();
		}

		@Override
		public int getCount()
		{
			return CONTENT.length;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SimpleFacebook.getInstance(this).onActivityResult(this, requestCode, resultCode, data);
	}

}
