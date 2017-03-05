package co.foodcircles.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmSetter extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
//		Log.i("AlarmSetter","onReceive");
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.DAY_OF_WEEK, 5);
//		calendar.set(Calendar.HOUR_OF_DAY, 16);
//		calendar.set(Calendar.MINUTE, 00);
//		calendar.set(Calendar.SECOND, 00);
//
//		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
//		alarmIntent.setAction("co.foodcircles.geonotification");
//		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		alarm.cancel(pendingIntent);
//		alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
	}
}