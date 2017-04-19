package co.foodcircles.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import co.foodcircles.R;

public class FoodCirclesUtils {
	public static final String TAG = "foodcircles";

	public static void saveToken(Context me, String token) {
		AndroidUtils.saveSharedPreferences(me, TAG, "token", token);
	}

	public static String getToken(Context me) {
		return AndroidUtils.getSharedPreferences(me, TAG, "token", "");
	}

	public static void saveEmail(Context me, String email) {
		AndroidUtils.saveSharedPreferences(me, TAG, "email", email);
	}

	public static String getEmail(Context me) {
		return AndroidUtils.getSharedPreferences(me, TAG, "email", "");
	}

	public static void savePassword(Context me, String password) {
		AndroidUtils.saveSharedPreferences(me, TAG, "password", password);
	}

	public static void clearPassword(Context me) {
		AndroidUtils.clearSharePreferences(me, TAG, "password");
	}

	public static String getPassword(Context me) {
		return AndroidUtils.getSharedPreferences(me, TAG, "password", "");
	}

	public static void saveName(Context me, String name) {
		AndroidUtils.saveSharedPreferences(me, TAG, "name", name);
	}

	public static void saveFBUserId(Context me, String name) {
		AndroidUtils.saveSharedPreferences(me, TAG, "fbuserid", name);
	}
	public static String getFBUserId(Context me) {
	return	AndroidUtils.getSharedPreferences(me, TAG, "fbuserid","");
	}
	public static String getName(Context me) {
		return AndroidUtils.getSharedPreferences(me, TAG, "name", "");
	}

	public static boolean isConnectedToSocialAccounts(Context me) {
		return isTwitterConnected(me) || isFacebookConnected(me);
	}

	public static void saveIsFacebookConnected(Context me, boolean isConnected) {
		AndroidUtils.saveSharedPreferences(me, TAG, "isfacebookconnected", ""
				+ isConnected);
	}

	public static void saveIsTwitterConnected(Context me, boolean isConnected) {
		AndroidUtils.saveSharedPreferences(me, TAG, "istwitterconnected", ""
				+ isConnected);
	}
	

	public static boolean isFacebookConnected(Context me) {
		String isConected = AndroidUtils.getSharePreferences(me, TAG,
				"isfacebookconnected");
		return (isConected.equalsIgnoreCase("true"));
	}

	public static boolean isTwitterConnected(Context me) {
		String isConected = AndroidUtils.getSharePreferences(me, TAG,
				"istwitterconnected");
		return (isConected.equalsIgnoreCase("true"));
	}

	public static String convertLongIntoStringDate(long datePurchased) {
		DateFormat formatter = new SimpleDateFormat("MM/dd");
		String dt = formatter.format(convertLongToDate(datePurchased).getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(sdf.parse(dt));
		} catch (java.text.ParseException e) {
			Log.d(TAG, e.getMessage());
		}
		SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd");
		return sdf1.format(c.getTime());
	}

	public static String convertLongToFormattedDateString(long date,
			String format) {
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(convertLongToDate(date).getTime());
	}

	public static Date convertLongToDate(long date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		return calendar.getTime();
	}

	public static Uri getLogoUri(Context context) {
        File dir = new File(context.getFilesDir().getPath() + "/images");

        if (dir.exists() || dir.mkdir()) {
            File file = new File(dir.getPath(), "logo.png");
            if (!file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
                try {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    } finally {
                        if (fos != null) fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return FileProvider.getUriForFile(context, "co.foodcircles.fileprovider", file);
        }
        return null;
	}
}
