package co.foodcircles.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import co.foodcircles.R;

public class AccountOptionsActivity extends FragmentActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.account_options_activity);

		AccountOptionsFragment accountOptionsFragment = new AccountOptionsFragment();
		accountOptionsFragment.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction().add(R.id.fragment1, accountOptionsFragment).commit();
	}
}