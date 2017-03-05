package co.foodcircles.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import co.foodcircles.R;

public class SimpleDialogFragment extends DialogFragment
{
	private String titleText, messageText;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View v = inflater.inflate(R.layout.simple_dialog, container, false);
		TextView titleTextView = (TextView) v.findViewById(R.id.textViewTitle);
		titleTextView.setText(titleText);
		TextView messageTextView = (TextView) v.findViewById(R.id.textViewMessage);
		messageTextView.setText(messageText);
		return v;
	}
	
	public void setText(String titleText, String messageText)
	{
		this.titleText = titleText;
		this.messageText = messageText;
	}
}
