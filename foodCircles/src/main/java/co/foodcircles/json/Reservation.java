package co.foodcircles.json;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import co.foodcircles.data.VenueList;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FoodCirclesUtils;
import co.foodcircles.util.Utils;

import static co.foodcircles.util.AndroidUtils.safelyGetJsonInt;

public class Reservation implements Parcelable {
    private final static String TAG = "Reservation";
    public static final int USED = 2;
    public static final int ACTIVE = 1;
    public static final int EXPIRED = 3;
    private long id;
    private String user;
    private String code;
    private Venue venue;
    private Offer offer;
    private long datePurchased;
    private int state;
    private int amount;

    public static List<Reservation> parseReservations(String jsonString)
            throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject jsonContent = jsonObject.getJSONObject("content");
        JSONArray jsonArray = AndroidUtils.safelyGetJsonArray(jsonContent, "payments");
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0, ii = jsonArray.length(); i < ii; i++) {
            try {
                reservations.add(new Reservation(jsonArray.getString(i)));
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        return reservations;
    }

    public static Reservation parseReservation(String jsonString)
            throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        try {
            return new Reservation(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public Reservation(String jsonString) throws JSONException {

        JSONObject json = new JSONObject(jsonString);
        id = safelyGetJsonInt(json, "id");
        String stateHolder = AndroidUtils.safelyGetJsonString(json, "state");
        user = AndroidUtils.safelyGetJsonString(json, "user");
        code = AndroidUtils.safelyGetJsonString(json, "code");
        amount = safelyGetJsonInt(json, "amount");
        if (stateHolder.equals("Active")) {
            state = 0;
        } else if (stateHolder.equals("Expiring")) {
            state = 4;
        } else if (stateHolder.equals("Expired")) {
            state = 3;
        } else if (stateHolder.equals("Used")) {
            state = 3;
        } else {
            state = 0;
        }

        try {
            offer = new Offer(AndroidUtils.safelyGetJsonArray(json, "offer").getString(0));
            try {
                venue = new Venue(offer.getVenue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // it's either null, "Active", "Expired" or "Used"
        String dateString = AndroidUtils
                .safelyGetJsonString(json, "date_purchased").replace("T", " ")
                .replace("Z", "");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            datePurchased = formatter.parse(dateString).getTime();
        } catch (ParseException e) {
            datePurchased = 0;
        }
    }

    public Reservation(long id, String user, Venue venue, Offer offer,
                       Charity charity, long datePurchased) {
        super();
        this.id = id;
        this.user = user;
        this.datePurchased = datePurchased;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public long getDatePurchased() {
        return datePurchased;
    }

    public void setDatePurchased(long datePurchased) {
        this.datePurchased = datePurchased;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isExpiring() {
        Date date = getExpirationDate();
        int days = Utils.getDaysDistanceBetweenDates(date, new Date());
        return ((days <= 7) && (days >= 0));
    }

    public Date getExpirationDate() {
        Date date = FoodCirclesUtils.convertLongToDate(datePurchased);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);

        date = c.getTime();
        date = Utils.addDaysToDate(date, 31);
        return date;
    }

    public Date getStartsExpiring() {
        return Utils.addDaysToDate(getExpirationDate(), -7);
    }

    public boolean isExpired() {
        return (state == EXPIRED);
    }

    public boolean isActive() {
        return (state == ACTIVE);
    }

    public boolean isUsed() {
        return (state == USED);
    }

    public int getKidsFed() {
        return amount;
    }

    public void setKidsFed(int kidsFed) {
        this.amount = kidsFed;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.user);
        dest.writeString(this.code);
        dest.writeParcelable(this.venue, flags);
        dest.writeParcelable(this.offer, flags);
        dest.writeLong(this.datePurchased);
        dest.writeInt(this.state);
        dest.writeInt(this.amount);
    }

    protected Reservation(Parcel in) {
        this.id = in.readLong();
        this.user = in.readString();
        this.code = in.readString();
        this.venue = in.readParcelable(Venue.class.getClassLoader());
        this.offer = in.readParcelable(Offer.class.getClassLoader());
        this.datePurchased = in.readLong();
        this.state = in.readInt();
        this.amount = in.readInt();
    }

    public static final Parcelable.Creator<Reservation> CREATOR = new Parcelable.Creator<Reservation>() {
        @Override
        public Reservation createFromParcel(Parcel source) {
            return new Reservation(source);
        }

        @Override
        public Reservation[] newArray(int size) {
            return new Reservation[size];
        }
    };
}
