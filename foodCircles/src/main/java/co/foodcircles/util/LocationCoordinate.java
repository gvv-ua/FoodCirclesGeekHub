package co.foodcircles.util;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gvv on 31.03.17.
 */

public class LocationCoordinate implements Parcelable {
    private static final double DEFAULT_LATITUDE = 42.955202;
    private static final double DEFAULT_LONGITUDE = -85.632823;
    private double latitude;
    private double longitude;

    public LocationCoordinate(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else {
            latitude = DEFAULT_LATITUDE;
            longitude = DEFAULT_LONGITUDE;
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    protected LocationCoordinate(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Parcelable.Creator<LocationCoordinate> CREATOR = new Parcelable.Creator<LocationCoordinate>() {
        @Override
        public LocationCoordinate createFromParcel(Parcel source) {
            return new LocationCoordinate(source);
        }

        @Override
        public LocationCoordinate[] newArray(int size) {
            return new LocationCoordinate[size];
        }
    };
}
