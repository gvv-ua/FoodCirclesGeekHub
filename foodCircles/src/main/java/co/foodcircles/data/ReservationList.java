package co.foodcircles.data;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.foodcircles.adapters.base.TimelineViewItem;
import co.foodcircles.adapters.viewitems.TimelineExpiringVoucherViewItem;
import co.foodcircles.adapters.viewitems.TimelineFriendViewItem;
import co.foodcircles.adapters.viewitems.TimelineHeaderViewItem;
import co.foodcircles.adapters.viewitems.TimelineMonthViewItem;
import co.foodcircles.adapters.viewitems.TimelineUsedVoucherViewItem;
import co.foodcircles.adapters.viewitems.TimelineVoucherViewItem;
import co.foodcircles.json.Reservation;
import co.foodcircles.net.Net;

/**
 * Created by gvv on 04.04.17.
 */

public class ReservationList {
    private final static String TAG = "ReservationList";
    private static final int TIMELINE_YOU_AND_FRIENDS_TYPE = 5;
    private static final int TIMELINE_VOUCHER_TYPE = 0;
    private static final int TIMELINE_FRIEND_TYPE = 1;
    private static final int TIMELINE_MONTH_TYPE = 2;
    private static final int TIMELINE_USED_VOUCHER_TYPE = 3;
    private static final int TIMELINE_EXPIRING_VOUCHER_TYPE = 4;

    private static ReservationList reservationList;
    private final List<TimelineViewItem> reservations;
    private int totalKidsFed = 0;

    public static ReservationList getInstance() {
        if (reservationList == null) {
            reservationList = new ReservationList();
        }
        return reservationList;
    }

    private ReservationList() {
        reservations = new ArrayList<>();
    }

    public List<TimelineViewItem> getReservations() {
        return reservations;
    }

    public int getTotalKidsFed() {
        return totalKidsFed;
    }

    public void updateData(final String token, final OnDataUpdateSuccessCallback successCallback, final OnDataUpdateFailCallback failCallback) {
        new AsyncTask<Object, Void, Boolean>() {

            protected Boolean doInBackground(Object... param) {
                try {
                    reservations.clear();
                    totalKidsFed = 0;
                    List<Reservation> list = Net.getReservationsList(token);
                    for (Reservation reservation : list) {
                        reservations.add(getViewItem(reservation));
                        totalKidsFed += reservation.getKidsFed();
                    }

                    //Fill fake data
                    //TimelineHelper.fillItems(reservations);

                    return true;
                } catch (Exception e) {
                    Log.v(TAG, "Error loading reservations", e);
                    return false;
                }
            }

            protected void onPostExecute(Boolean success) {
                if (success) {
                    successCallback.onUpdateVenuesSuccess();
                } else {
                    failCallback.onUpdateVenuesFailed();
                }
            }
        }.execute();
    }

    private TimelineViewItem getViewItem(Reservation reservation) {
        int itemViewType = reservation.getState();
        switch (itemViewType) {
            case TIMELINE_YOU_AND_FRIENDS_TYPE:
                return new TimelineHeaderViewItem(reservation);
            case TIMELINE_VOUCHER_TYPE:
                return new TimelineVoucherViewItem(reservation);
            case TIMELINE_FRIEND_TYPE:
                return new TimelineFriendViewItem(reservation);
            case TIMELINE_MONTH_TYPE:
                return new TimelineMonthViewItem(reservation);
            case TIMELINE_USED_VOUCHER_TYPE:
                return new TimelineUsedVoucherViewItem(reservation);
            case TIMELINE_EXPIRING_VOUCHER_TYPE:
                return new TimelineExpiringVoucherViewItem(reservation);
            default:
                return null;
        }
    }

    public interface OnDataUpdateSuccessCallback {
        void onUpdateVenuesSuccess();
    }

    public interface OnDataUpdateFailCallback {
        void onUpdateVenuesFailed();
    }
}
