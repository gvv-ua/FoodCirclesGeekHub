package co.foodcircles.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.foodcircles.util.AndroidUtils;

public class Charity {
    public int id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String description;
    private String imageUrl;

    public static List<Charity> parseCharities(String jsonString) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        List<Charity> charities = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            charities.add(new Charity(jsonArray.getString(i)));
        }
        return charities;
    }

    public Charity(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        id = AndroidUtils.safelyGetJsonInt(json, "id");
        name = AndroidUtils.safelyGetJsonString(json, "name");
        address = AndroidUtils.safelyGetJsonString(json, "address");
        city = AndroidUtils.safelyGetJsonString(json, "city");
        state = AndroidUtils.safelyGetJsonString(json, "state");
        description = AndroidUtils.safelyGetJsonString(json, "description");
        imageUrl = AndroidUtils.safelyGetJsonString(json, "image");
    }

    public Charity(int id, String name, String address, String city, String state, String zip, String description, String imageUrl) {
        super();
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Charity(boolean testCharity) {
        this.id = 1;
        this.name = "Local Orgs";
        this.address = "Address";
        this.city = "City";
        this.state = "State";
        this.description = "Description";
        this.imageUrl = "/media/BAhbBlsHOgZmSSIvMjAxMi8wNS8yNC8xM180N181MF81OTRfR29qb19FdGhpb3BpYW4ucG5nBjoGRVQ";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
