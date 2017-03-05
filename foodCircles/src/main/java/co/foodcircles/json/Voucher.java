package co.foodcircles.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FoodCirclesUtils;
import co.foodcircles.util.Utils;

public class Voucher
{
	public static final String USED = "Used";
	public static final String ACTIVE = "Active";
	public static final String EXPIRED = "Expired";
	private String id;
	private String user;
	private String code;
	private String venue;
	@SuppressWarnings("unused")
	private String reward;
	private long datePurchased;
	private String state;

	public static List<Voucher> parseVouchers(String jsonString) throws JSONException
	{
		JSONObject jsonObject = new JSONObject(jsonString);
		JSONArray jsonArray = jsonObject.getJSONArray("content");

		List<Voucher> vouchers = new ArrayList<Voucher>();
		for (int i = 0; i < jsonArray.length(); i++)
		{
			try {
				vouchers.add(new Voucher(jsonArray.getString(i)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return vouchers;
	}

	public static Voucher parseVoucher(String jsonString) throws JSONException
	{
		JSONObject jsonObject = new JSONObject(jsonString);
		try {
		Voucher voucher = new Voucher(jsonObject.toString());
		return voucher;
			} catch (JSONException e) {

				e.printStackTrace();
			}
		return null;
		
	}

	
	public Voucher(String jsonString) throws JSONException
	{
		JSONObject json = (new JSONObject(jsonString)).getJSONObject("content");
		id = AndroidUtils.safelyGetJsonString(json,"id");
		user = AndroidUtils.safelyGetJsonString(json,"user");
		code = AndroidUtils.safelyGetJsonString(json,"code");
		venue = AndroidUtils.safelyGetJsonString(json,"venue");
		reward = AndroidUtils.safelyGetJsonString(json,"reward");
			//it's either null, "Active", "Expired" or "Used"
		state=AndroidUtils.safelyGetJsonString(json,"state");
		String dateString=AndroidUtils.safelyGetJsonString(json,"created_at").replace("T", " ").replace("Z", "");
		SimpleDateFormat formatter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			datePurchased=formatter.parse(dateString).getTime();
		} catch (ParseException e) {
			datePurchased=0;
		}

	}

	public Voucher(String id, String user, Venue venue, Offer offer, Charity charity, long datePurchased)
	{
		super();
		this.id = id;
		this.user = user;
		this.datePurchased = datePurchased;
	}
	
	public Voucher(boolean testvoucher)
	{
		super();
		this.id = "1";
		this.user = "Jonathan Kumar";
		this.datePurchased = Calendar.getInstance().getTimeInMillis();
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getVenue()
	{
		return venue;
	}

	public void setVenue(String venue)
	{
		this.venue = venue;
	}

	public long getDatePurchased()
	{
		return datePurchased;
	}

	public void setDatePurchased(long datePurchased)
	{
		this.datePurchased = datePurchased;
	}
	
	public void setState(String state){
		this.state=state;
	}
	
	public boolean isExpiring(){
		Date date = getExpirationDate();
		int days=Utils.getDaysDistanceBetweenDates(date,new Date());
		if(days<=7 && days>=0){
			return true;
		}else{
			return false;
		}
	}
	
	public Date getExpirationDate(){
		Date date=FoodCirclesUtils.convertLongToDate(datePurchased);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY,23);
		c.set(Calendar.MINUTE,59);
		c.set(Calendar.SECOND,59);
		date=c.getTime();
		date=Utils.addDaysToDate(date,31);
		return date;
	}
	
	public Date getStartsExpiring(){
		return Utils.addDaysToDate(getExpirationDate(),-7);
	}
	
	public boolean isExpired(){
		if(state.equalsIgnoreCase(EXPIRED)){
			return true;
		}else{
			return false;
		}
	}
	public boolean isActive(){
		if(state.equalsIgnoreCase(ACTIVE)){
			return true;
		}else{
			return false;
		}
	}
	public boolean isUsed(){
		if(state.equalsIgnoreCase(USED)){
			return true;
		}else{
			return false;
		}
	}

}
