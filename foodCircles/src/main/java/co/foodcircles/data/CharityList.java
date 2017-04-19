package co.foodcircles.data;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.foodcircles.json.Charity;
import co.foodcircles.net.Net;

/**
 * Created by gvv on 09.04.17.
 */

public class CharityList {
    private final static String TAG = "CharityList";
    private static CharityList charityList;
    private final List<Charity> charities;

    public static CharityList getInstance() {
        if (charityList == null) {
            charityList = new CharityList();
        }
        return charityList;
    }

    private CharityList() {
        charities = new ArrayList<>();
    }

    public List<Charity> getCharities() {
        return charities;
    }

    public Charity getById(long id) {
        for (Charity charity: charities) {
            if (charity.getId() == id) {
                return charity;
            }
        }
        return null;
    }

    public void updateData() {
        new AsyncTask<Object, Void, Void>() {

            protected Void doInBackground(Object... param) {
                try {
                    charities.clear();
                    charities.addAll(Net.getCharities());
                } catch (Exception e) {
                    Log.v(TAG, "Error loading charities", e);
                }
                return null;
            }
        }.execute();
    }
}
