package co.foodcircles.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.foodcircles.util.AndroidUtils;

public class Social
{
	private String URL;
	private String Type;

	public static List<Social> parseSocial(String jsonString) throws JSONException
	{

		JSONArray jsonArray = new JSONArray(jsonString);
		List<Social> social = new ArrayList<Social>();

		for (int i = 0; i < jsonArray.length(); i++)
		{
			social.add(new Social(jsonArray.getString(i)));
		}

		return social;
	}

	public Social(String jsonString) throws JSONException
	{
		jsonString.substring(1, jsonString.length()-1);
		JSONObject json = new JSONObject(jsonString);
		URL = AndroidUtils.safelyGetJsonString(json, "url");
		Type = AndroidUtils.safelyGetJsonString(json, "source");
	}

	public String getURL()
	{
		return URL;
	}

	public void setURL(String url)
	{
		this.URL = url;
	}

	public String getType()
	{
		return Type;
	}

	public void setType(String type)
	{
		this.Type = type;
	}


}
