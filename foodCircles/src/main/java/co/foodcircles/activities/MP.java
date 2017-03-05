package co.foodcircles.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class MP
{
	public static void track(MixpanelAPI mp, String... strings)
	{
		if (strings.length == 0) return;
		try
		{
			JSONObject props = new JSONObject();
			for (int i = 1; (i + 1) < strings.length; i += 2)
			{
				props.put(strings[i], strings[i + 1]);
			}
			mp.track(strings[0], props);
		}
		catch (JSONException e)
		{ Log.e("MixPanel", "JSONException", e); }
	}
}
