package co.foodcircles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;

import com.viewpagerindicator.TabPageIndicator;

import java.util.Locale;

import co.foodcircles.R;
import co.foodcircles.json.Venue;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;

public class RestaurantActivity extends FragmentActivity {
    public static final String IS_VENUE_ON_RESERVE_KEY = "on_reserved_key";

    private static String[] CONTENT = new String[] { "OFFER", "INFO" };

    private boolean mIsVenueOnReserve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.simple_tabs);

        Intent intent = getIntent();
        if (intent != null) {
            boolean isReserve = intent.getBooleanExtra(IS_VENUE_ON_RESERVE_KEY, false);
            mIsVenueOnReserve = isReserve;
        }

        FoodCirclesApplication app = (FoodCirclesApplication) getApplicationContext();
        Venue selectedVenue = app.selectedVenue;
        CONTENT[0] = selectedVenue.getName().toUpperCase(Locale.getDefault());
        FragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);
        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
		FontSetter.overrideFonts(this, findViewById(R.id.root));
    }

    public void onContinueBrowsingClick(View view) {
        finish();
    }

    class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	switch(position)
        	{
        		case 0: return VenueItemFragment.newInstance(mIsVenueOnReserve);
        		case 1: return new VenueProfileFragment();
        		default: return null;
        	}
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase(Locale.getDefault());
        }

        @Override
        public int getCount() {
          return CONTENT.length;
        }
    }
}
