package co.foodcircles.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import co.foodcircles.R;
import co.foodcircles.exception.NetException2;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FoodCirclesUtils;

public class EmailDialogFragment extends DialogFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View v = inflater.inflate(R.layout.change_email, container, false);
		final EditText editText = (EditText) v.findViewById(R.id.editTextEmail);
		editText.setText(FoodCirclesUtils.getEmail(getActivity()));
		// Watch for button clicks.
		Button button = (Button) v.findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				final String email=editText.getText().toString();
				final String password = FoodCirclesUtils.getPassword(getActivity());
				final String name=FoodCirclesUtils.getName(getActivity());
				final String phoneNumber="";
				final String token=FoodCirclesUtils.getToken(getActivity());
				new Thread(){
					public void run() {
						try {
							Net.updateUserInfo(token,email,password, name, phoneNumber);
							EmailDialogFragment.this.getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									FoodCirclesUtils.saveEmail(EmailDialogFragment.this.getActivity(), email);
									AndroidUtils.alert(EmailDialogFragment.this.getActivity(), "Email updated");
									EmailDialogFragment.this.dismiss();
								}
							});
						} catch (final NetException2 e) {
							EmailDialogFragment.this.getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									AndroidUtils.alert(EmailDialogFragment.this.getActivity(), "Failed Updating User Info : "+e.getMessage());
								}
							});

						}
					}
				}.start();
			}
		});

		return v;
	}
}
