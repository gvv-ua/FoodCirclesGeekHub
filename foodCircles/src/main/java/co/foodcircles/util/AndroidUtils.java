package co.foodcircles.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AndroidUtils {
    public static void showAlertOk(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void alert(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private static final int TEXT_ID = 0;

    public static void confirm(Context context, String title, String message, final ConfirmListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        final EditText input = new EditText(context);
        input.setId(TEXT_ID);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                listener.onOk(value);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onCancel();
            }
        });
        builder.create().show();
    }

    public interface ConfirmListener {
        void onOk(String text);

        void onCancel();
    }

    private static ProgressDialog pd;

    public static void showProgress(Context context) {
        showProgress(context, "Processing...", "Please wait.");
    }

    public static void showProgress(Context context, String title, String message) {
        pd = new ProgressDialog(context);
        pd.setTitle(title);
        pd.setMessage(message);
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
    }

    public static void dismissProgress() {
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    public static String getSharePreferences(Context me, String name, String fieldName) {
        return getSharedPreferences(me, name, fieldName, "");
    }

    @SuppressWarnings("static-access")
    public static String getSharedPreferences(Context me, String name, String fieldName, String defaultValue) {
        SharedPreferences myPrefs = me.getSharedPreferences(name, me.MODE_PRIVATE);
        return myPrefs.getString(fieldName, defaultValue);
    }

    public static void saveSharedPreferences(Context me, String name, String fieldName, String value) {
        if (name != "") {

            Log.i("Saving", name + " & " + value);
            SharedPreferences myPrefs = me.getSharedPreferences(name, Context.MODE_PRIVATE);
            SharedPreferences.Editor e = myPrefs.edit();
            e.putString(fieldName, value).apply();
        }
    }

    public static void clearSharePreferences(Context me, String name, String fieldName) {
        @SuppressWarnings("static-access")
        SharedPreferences myPrefs = me.getSharedPreferences(name, me.MODE_PRIVATE);
        SharedPreferences.Editor e = myPrefs.edit();
        e.remove(fieldName).apply();
    }

    public static String safelyGetJsonString(JSONObject json, String string) {
        try {
            return json.getString(string);
        } catch (JSONException e) {
            return "";
        }
    }

    public static double safelyGetJsonDouble(JSONObject json, String string) {
        try {
            return json.getDouble(string);
        } catch (JSONException e) {
            return 0;
        }
    }

    public static JSONArray safelyGetJsonArray(JSONObject json, String string) {
        try {
            return json.getJSONArray(string);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public static int safelyGetJsonInt(JSONObject json, String string) {
        try {
            return json.getInt(string);
        } catch (JSONException e) {
            return 0;
        }
    }

    @SuppressWarnings("unused")
    private void openLinkByDefaultBrowser(Activity activity, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(i);
    }

    public static void loadActivity(Activity activity,
                                    Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);
    }
}
