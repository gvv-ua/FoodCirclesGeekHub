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

public class NameDialogFragment extends DialogFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View v = inflater.inflate(R.layout.change_name, container, false);
		final EditText editText = (EditText) v.findViewById(R.id.editTextName);
		editText.setText(FoodCirclesUtils.getName(getActivity()));
		Button button = (Button) v.findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				final String email=FoodCirclesUtils.getEmail(getActivity());
				final String password = FoodCirclesUtils.getPassword(getActivity());
				final String name=editText.getText().toString();
				final String phoneNumber="";
				final String token=FoodCirclesUtils.getToken(getActivity());
				new Thread(){
					public void run() {
						try {
							Net.updateUserInfo(token,email,password, name, phoneNumber);
							NameDialogFragment.this.getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									FoodCirclesUtils.saveName(NameDialogFragment.this.getActivity(), email);
									AndroidUtils.alert(NameDialogFragment.this.getActivity(), "Name updated");
									NameDialogFragment.this.dismiss();
								}
							});
						} catch (final NetException2 e) {
							NameDialogFragment.this.getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									AndroidUtils.alert(NameDialogFragment.this.getActivity(), "Failed Updating User Info : "+e.getMessage());
								}
							});

						}
					}
				}.start();
				NameDialogFragment.this.dismiss();
			}
		});

		return v;
	}
}
