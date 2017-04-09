package co.foodcircles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import co.foodcircles.R;
import co.foodcircles.json.Venue;

import static co.foodcircles.activities.RestaurantActivity.SELECTED_VENUE_KEY;

public class VenueProfileActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.account_options_activity);

        Venue venue = null;
        Intent intent = getIntent();
        if (intent != null) {
            venue = intent.getParcelableExtra(SELECTED_VENUE_KEY);
        }

        VenueProfileFragment venueProfileFragment = VenueProfileFragment.newInstance(venue);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment1, venueProfileFragment).commit();
    }
}