package co.foodcircles.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import co.foodcircles.R;
import co.foodcircles.exception.NetException2;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FoodCirclesUtils;

public class PasswordDialogFragment extends DialogFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View v = inflater.inflate(R.layout.change_password, container, false);
		final EditText password1 = (EditText) v.findViewById(R.id.editTextPassword);
		final EditText password2 = (EditText) v.findViewById(R.id.editTextConfirm);
		Button button = (Button) v.findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if(password1.getText().toString().length()>0 && password1.getText().toString().equals(password2.getText().toString())==false){
					AndroidUtils.showAlertOk(PasswordDialogFragment.this.getActivity(), "Password does not match");
					return;
				}
				final String email=FoodCirclesUtils.getEmail(getActivity());
				final String password = password1.getText().toString();
				final String name=FoodCirclesUtils.getName(getActivity());
				final String phoneNumber="";
				final String token=FoodCirclesUtils.getToken(getActivity());
				new Thread(){
					public void run() {
						try {
							Net.updateUserInfo(token,email,password, name, phoneNumber);
							PasswordDialogFragment.this.getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									FoodCirclesUtils.savePassword(PasswordDialogFragment.this.getActivity(), password);
									AndroidUtils.alert(PasswordDialogFragment.this.getActivity(), "Email updated");
									PasswordDialogFragment.this.dismiss();
								}
							});
						} catch (final NetException2 e) {
							PasswordDialogFragment.this.getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									AndroidUtils.alert(PasswordDialogFragment.this.getActivity(), "Failed Updating User Info :"+e.getMessage());
								}
							});
							
						}
					};
				}.start();
			}
		});
		return v;
	}
}
