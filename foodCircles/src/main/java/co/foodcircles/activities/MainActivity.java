package co.foodcircles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;

import com.viewpagerindicator.TabPageIndicator;

import co.foodcircles.R;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;

//import com.sromku.simple.fb.SimpleFacebook;

public class MainActivity extends FragmentActivity {
    public static final String CURRENT_TAB = "tab";
    private static final String TAG = "MainActivity";
    public static final int TAB_NEWS = 0;
    public static final int TAB_RESTAURANTS = 1;
    public static final int TAB_TIMELINE = 2;

    private static final String[] CONTENT = new String[]{"NEWS", "FOOD", "YOU"};
    private FoodCirclesApplication app;
    //	SimpleFacebook mSimpleFacebook;

    private TabPageIndicator indicator;
    private int currentTab = TAB_NEWS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.simple_tabs);
        app = (FoodCirclesApplication) getApplicationContext();
        indicator = (TabPageIndicator) findViewById(R.id.indicator);
        FragmentPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        pager.setCurrentItem(getIntent().getIntExtra(CURRENT_TAB, currentTab));
        FontSetter.overrideFonts(this, findViewById(R.id.root));

        if (app.purchasedVoucher) {
            FragmentManager fm = getSupportFragmentManager();
            ReceiptDialogFragment receiptDialog = new ReceiptDialogFragment();
            receiptDialog.show(fm, "receipt_dialog");
            pager.setCurrentItem(TAB_TIMELINE);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //This launches the receipt fragment and reloads the application
        if (app.needsRestart) {
            app.needsRestart = false;
            MainActivity.this.finish();
            Intent intent = getIntent();
            intent.putExtra(MainActivity.CURRENT_TAB, currentTab);
            startActivity(intent);
        }
    }

    class MainPagerAdapter extends FragmentPagerAdapter {
        public MainPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
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
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //SimpleFacebook.getInstance(this).onActivityResult(this, requestCode, resultCode, data);
    }

}
