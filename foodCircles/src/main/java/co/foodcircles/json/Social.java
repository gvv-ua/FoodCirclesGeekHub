package co.foodcircles.json;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.foodcircles.util.AndroidUtils;

public class Social implements Parcelable {
    private String URL;
    private String Type;

    public static List<Social> parseSocial(String jsonString) throws JSONException {

        JSONArray jsonArray = new JSONArray(jsonString);
        List<Social> social = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            social.add(new Social(jsonArray.getString(i)));
        }

        return social;
    }

    public Social(String jsonString) throws JSONException {
        jsonString.substring(1, jsonString.length() - 1);
        JSONObject json = new JSONObject(jsonString);
        URL = AndroidUtils.safelyGetJsonString(json, "url");
        Type = AndroidUtils.safelyGetJsonString(json, "source");
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String url) {
        this.URL = url;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.URL);
        dest.writeString(this.Type);
    }

    protected Social(Parcel in) {
        this.URL = in.readString();
        this.Type = in.readString();
    }

    public static final Parcelable.Creator<Social> CREATOR = new Parcelable.Creator<Social>() {
        @Override
        public Social createFromParcel(Parcel source) {
            return new Social(source);
        }

        @Override
        public Social[] newArray(int size) {
            return new Social[size];
        }
    };
}
