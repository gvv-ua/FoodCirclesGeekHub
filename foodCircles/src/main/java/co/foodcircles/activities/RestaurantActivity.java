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

public class RestaurantActivity extends FragmentActivity {
    public static final String IS_VENUE_ON_RESERVE_KEY = "on_reserved_key";
    public static final String SELECTED_VENUE_KEY = "selected_venue_key";

    private static final String[] CONTENT = new String[]{"OFFER", "INFO"};

    private boolean mIsVenueOnReserve;
    private Venue selectedVenue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.simple_tabs);


        Intent intent = getIntent();
        if (intent != null) {
            mIsVenueOnReserve = intent.getBooleanExtra(IS_VENUE_ON_RESERVE_KEY, false);
            selectedVenue = intent.getParcelableExtra(SELECTED_VENUE_KEY);
        }

        CONTENT[0] = selectedVenue.getName().toUpperCase(Locale.getDefault());
        FragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
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
            switch (position) {
                case 0:
                    return VenueItemFragment.newInstance(mIsVenueOnReserve, selectedVenue);
                case 1:
                    return VenueProfileFragment.newInstance(selectedVenue);
                default:
                    return null;
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
