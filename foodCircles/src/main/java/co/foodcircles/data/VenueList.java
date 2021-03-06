package co.foodcircles.data;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.util.LocationCoordinate;
import co.foodcircles.util.SortListByDistance;

/**
 * Created by gvv on 02.04.17.
 */

public class VenueList {
    private final static String TAG = "VenueList";
    private static VenueList venueList;
    private final List<Venue> venues;

    public static VenueList getInstance() {
        if (venueList == null) {
            venueList = new VenueList();
        }
        return venueList;
    }

    private VenueList() {
        venues = new ArrayList<>();
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public Venue getById(long id) {
        for (Venue venue: venues) {
            if (venue.getId() == id) {
                return venue;
            }
        }
        return null;
    }

    public void updateData(final LocationCoordinate locationCoordinate, final OnDataUpdateSuccessCallback successCallback, final OnDataUpdateFailCallback failCallback) {
        new AsyncTask<Object, Void, Boolean>() {

            protected Boolean doInBackground(Object... param) {
                try {
                    venues.clear();
                    venues.addAll(Net.getVenues(locationCoordinate));
                    return true;
                } catch (Exception e) {
                    Log.v(TAG, "Error loading venues", e);
                    return false;
                }
            }

            protected void onPostExecute(Boolean success) {
                if (success) {
                    Collections.sort(venues, new SortListByDistance());
                    successCallback.onUpdateVenuesSuccess();
                } else {
                    failCallback.onUpdateVenuesFailed();
                }
            }
        }.execute();
    }

    public interface OnDataUpdateSuccessCallback {
        void onUpdateVenuesSuccess();
    }

    public interface OnDataUpdateFailCallback {
        void onUpdateVenuesFailed();
    }
}
