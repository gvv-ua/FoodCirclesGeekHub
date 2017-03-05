package co.foodcircles.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import co.foodcircles.R;

public class ReservedActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserved);

        String venueName = getIntent().getStringExtra("venue_name");
        TextView message = (TextView) findViewById(R.id.tv_reserved_venue_name);
        message.setText(String.format("%s %s", getString(R.string.does_not_reserve_table_at_hop_cat), venueName));
    }

    public void onContinueBrowsingClick(View view) {
        finish();
    }
}
