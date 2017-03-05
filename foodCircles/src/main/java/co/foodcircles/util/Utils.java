package co.foodcircles.util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utils {

	public static Date addDaysToDate(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days); 
		return c.getTime();
	}
	
	public static int getDaysDistanceBetweenDates(Date date1, Date date2)
	{       
	    return (int)((date2.getTime() - date1.getTime()) / (1000*60*60*24l));
	}
	
	public static Map<String, String> convertQueryToMap(String query) {  
	    String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params)  
	    {  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
	        map.put(name, value);  
	    }  
	    return map;  
	}  
}
