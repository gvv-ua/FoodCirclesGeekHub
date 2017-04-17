package co.foodcircles.net;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.foodcircles.exception.NetException2;
import co.foodcircles.json.Charity;
import co.foodcircles.json.Reservation;
import co.foodcircles.json.Venue;
import co.foodcircles.json.Voucher;
import co.foodcircles.util.LocationCoordinate;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Net {
    private static final String TAG = Net.class.getSimpleName();

    public static final String HOST = "http://joinfoodcircles.org";
    //public static final String HOST = "https://foodcircles.gq";
    private static final String API_URL = "/api";
    private static final String GET_VENUES = "/venues/%f/%f";
    //private static final String GET_RESERVATION = "/reservations/[reservationId]";
    private static final String GET_CHARITY_1 = "/charities";
    //private static final String GET_NEWS = "/news";
    private static final String MARK_REDEEMED = "/payment/used?code=";
    private static final String GET_TIMELINE = "/timeline?auth_token=%s";
    private static final String VENUES_SUBSCRIBE = "/venues/%s/subscribe?auth_token=%s";
    private static final String VENUES_UNSUBSCRIBE = "/venues/%s/unsubscribe?auth_token=%s";
    private static final String VENUES_IS_SUBSCRIBED = "/venues/%s/subscribed.json?auth_token=%s";
    //public static String logo = "https://foodcircles.org/media/BAhbBlsHOgZmSSIkMjAxMy8wOC8yMC8xNl81Nl8xMV84MzNfRkFRLnBuZwY6BkVU";
    public static String logo = "http://joinfoodcircles.org/media/W1siZiIsIjIwMTYvMTIvMTMvMDJfNTdfMzBfNjY3X2dlbmVyb3NpdHlfd2ViLnBuZyJdXQ";

    public static String FACEBOOK_URL = "https://www.facebook.com/FoodCircles";
    public static String FACEBOOK_PAGE_ID = "179227878796116";

    private static final OkHttpClient client = new OkHttpClient();

    private static String postOk(String path, RequestBody params) {
        Log.d(TAG, "post request url = " + HOST + path);
        Request request = new Request.Builder()
                .url(HOST + path)
                .post(params)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getOk(String path) {
        Log.d(TAG, "get request url = " + HOST + path);
        Request request = new Request.Builder()
                .url(HOST + path)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String putOk(String path, RequestBody params) {
        Log.d(TAG, "post request url = " + HOST + path);
        Request request = new Request.Builder()
                //.url(HOST + path)
                .url(HOST + API_URL + path)
                .put(params)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @SuppressLint("DefaultLocale")
    public static List<Venue> getVenues(LocationCoordinate locationCoordinate)
            throws NetException {
        try {
            String url = String.format(GET_VENUES, locationCoordinate.getLatitude(), locationCoordinate.getLongitude());
            String response = getOk(API_URL + url);
            return Venue.parseVenues(response);
        } catch (JSONException j) {
            throw new NetException();
        }
    }

    public static List<Reservation> getReservationsList(String token) throws NetException {
        try {
            String url = String.format(GET_TIMELINE, token);
            String response = getOk(API_URL + url);
            return Reservation.parseReservations(response);
        } catch (JSONException j) {
            throw new NetException();
        }
    }

//    public static Reservation getReservation(String reservationId)
//            throws NetException {
//        try {
//            String response = getOk(API_URL + GET_RESERVATION.replace("[reservationId]", reservationId));
//            return new Reservation(response);
//        } catch (JSONException j) {
//            throw new NetException();
//        }
//    }

    public static List<Charity> getCharities() throws NetException {
        List<Charity> charities = new ArrayList<>();
        String response = getOk(API_URL + GET_CHARITY_1);
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("content");
            for (int ctr = 0; ctr < jsonArray.length(); ctr++) {
                charities.add(new Charity(jsonArray.getJSONObject(ctr).toString()));
            }
        } catch (JSONException e) {
            throw new NetException();
        }
        return charities;
    }

    public static String signUp(String userEmail, String userPassword)
            throws NetException2 {
        RequestBody requestBody = new FormBody.Builder()
                .add("user_email", userEmail)
                .add("user_password", userPassword)
                .build();

        String html = postOk(API_URL + "/sessions/sign_up", requestBody);
        NetException2 n = new NetException2();
        try {
            JSONObject json = new JSONObject(html);
            if (json.getBoolean("error")) {
                n.setMessage(getSignUpErrorMsg(json));
                throw n;
            }
            return json.getString("auth_token");
        } catch (JSONException e) {
            n.setMessage(e.getMessage());
            throw n;
        }
    }

    private static String getSignUpErrorMsg(JSONObject json) throws JSONException {
        if (json.has("errors")) {
            JSONObject errorsJson = json.getJSONObject("errors");
            JSONArray emailErrors = errorsJson.getJSONArray("email");
            String emailErrMsg = emailErrors.getString(0);

            JSONArray passwordErrors = errorsJson.getJSONArray("password");
            String passwordErrMsg = passwordErrors.getString(0);

            String errMsgDescription = json.getString("description");

            return String.format("%s\nEmail %s\nPassword %s", errMsgDescription, emailErrMsg, passwordErrMsg);
        } else {
            return json.getString("description");
        }
    }

    public static String twitterSignUp(String userEmail, String UID)
            throws NetException2 {
        RequestBody requestBody = new FormBody.Builder()
                .add("user_email", userEmail)
                .add("uid", UID)
                .build();
        String html = postOk(API_URL + "/sessions/sign_up", requestBody);
        NetException2 n = new NetException2();
        try {
            JSONObject json = new JSONObject(html);
            if (json.getBoolean("error")) {
                n.setMessage(json.getString("description"));
                throw n;
            }
            String token = json.getString("auth_token");
            System.out.println("TOKEN ::" + token);
            return token;
        } catch (JSONException e) {
            n.setMessage(e.getMessage());
            throw n;
        }
    }

    public static String twittersignIn(String uid)
            throws NetException2 {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", uid)
                .build();
        String html = postOk(API_URL + "/sessions/sign_in", requestBody);
        JSONObject json;
        NetException2 n = new NetException2();
        try {
            json = new JSONObject(html);
            if (json.getBoolean("error")) {
                n.setMessage(json.getString("description"));
                return json.getString("description");
            }
            return json.getString("auth_token");
        } catch (JSONException e) {
            n.setMessage(e.getMessage());
            throw n;
        }
    }

    public static String facebookSignUp(String userID, String emailid)
            throws NetException2 {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", userID)
                .add("user_email", emailid)
                .build();
        String html = postOk(API_URL + "/sessions/sign_up", requestBody);

        NetException2 n = new NetException2();
        try {
            JSONObject json = new JSONObject(html);
            if (json.getBoolean("error")) {
                n.setMessage(json.getString("description"));
                Log.i("Error1", n.getMessage());
                throw n;
            }
            return json.getString("auth_token");
        } catch (JSONException e) {
            n.setMessage(e.getMessage());
            Log.i("Error2", n.getMessage());
            throw n;
        }
    }

    public static String signIn(String userEmail, String userPassword)
            throws NetException2 {
        RequestBody requestBody = new FormBody.Builder()
                .add("user_email", userEmail)
                .add("user_password", userPassword)
                .build();
        String html = postOk(API_URL + "/sessions/sign_in", requestBody);
        JSONObject json;
        NetException2 n = new NetException2();
        try {
            json = new JSONObject(html);
            if (json.getBoolean("error")) {
                n.setMessage(getSignUpErrorMsg(json));
                throw n;
            }
            return json.getString("auth_token");
        } catch (JSONException e) {
            n.setMessage(e.getMessage());
            throw n;
        }
    }

    public static void updateUserInfo(String authToken, String userEmail, String userPassword,
                                      String name, String phone) throws NetException2 {
        RequestBody requestBody = new FormBody.Builder()
                .add("user_email", userEmail)
                .add("user_password", userPassword)
                .add("name", name)
                .add("phone", phone)
                .build();
        String html = putOk("/sessions/update?auth_token=" + authToken, requestBody);

        NetException2 n = new NetException2();
        JSONObject json;
        try {
            json = new JSONObject(html);
            if (json.getBoolean("error")) {
                n.setMessage(json.getString("description"));
                throw n;
            }
        } catch (JSONException e) {
            n.setMessage(e.getMessage());
            throw n;
        }
    }

//    public static void getNews() throws NetException2 {
//        String html = getOk(API_URL + GET_NEWS);
//        NetException2 n = new NetException2();
//        JSONObject json;
//        try {
//            json = new JSONObject(html);
//            if (json.getBoolean("error") == true) {
//                n.setMessage(json.getString("description"));
//                throw n;
//            }
//        } catch (JSONException e) {
//            n.setMessage(e.getMessage());
//            throw n;
//        }
//    }
//
//    public static void getTimeLine() throws NetException2 {
//        String html = getOk(API_URL + "/timeline/");
//        NetException2 n = new NetException2();
//        try {
//            JSONObject json = new JSONObject(html);
//            if (json.getBoolean("error") == true) {
//                n.setMessage(json.getString("description"));
//                throw n;
//            }
//        } catch (JSONException e) {
//            n.setMessage(e.getMessage());
//            throw n;
//        }
//
//    }
//
//    public static void getTimeLineVoucher(int id) throws NetException2 {
//        RequestBody requestBody = new FormBody.Builder()
//                .build();
//        String html = postOk(API_URL + "/timeline/voucher/" + id, requestBody);
//        NetException2 n = new NetException2();
//        try {
//            JSONObject json = new JSONObject(html);
//            if (json.getBoolean("error") == true) {
//                n.setMessage(json.getString("description"));
//                throw n;
//            }
//        } catch (JSONException e) {
//            n.setMessage(e.getMessage());
//            throw n;
//        }
//    }

    public static boolean markReservationAsUsed(String id) {
        String response = getOk(MARK_REDEEMED + id.trim());
        return response.equals("success");
    }

    // Attempts to verify the payment, and if successful, returns the certificate information
    public static Voucher verifyPayment(String token, int priceValue, String offerId, String payKey, int charity) throws NetException2 {
        RequestBody requestBody = new FormBody.Builder()
                .add("auth_token", token)
                .add("payment[amount]", priceValue + "")
                .add("payment[offer_id]", offerId)
                .add("payment[paypal_charge_token]", payKey)
                .add("payment[charity_id]", "" + charity)
                .build();

        String html = postOk(API_URL + "/payments", requestBody);
        NetException2 n = new NetException2();
        try {
            return Voucher.parseVoucher(html);
        } catch (JSONException e) {
            n.setMessage(e.getMessage());
            throw n;
        }

    }

    public static String getMailChimp() {
        String html = getOk(API_URL + "/general/users");
        try {
            JSONObject json = new JSONObject(html);
            if (json.getBoolean("error")) {
                return "Many";
            }
            return (json.getInt("content") + "");
        } catch (JSONException e) {
            return "Many";
        }
    }

    public static String subscribeVenue(String slug, String authToken) {
        //Without API in URL
        //Server return javascript code (HTTP 406)
        RequestBody requestBody = new FormBody.Builder()
                .build();
        return postOk(String.format(VENUES_SUBSCRIBE, slug, authToken), requestBody);
    }

    public static String unsubscribeVenue(String slug, String authToken) {
        //Without API in URL
        //Server return javascript code (HTTP 406)
        RequestBody requestBody = new FormBody.Builder()
                .build();
        return postOk(String.format(VENUES_UNSUBSCRIBE, slug, authToken), requestBody);
    }

    public static boolean isSubscribed(String slug, String authToken) {
        String html = getOk(String.format(VENUES_IS_SUBSCRIBED, slug, authToken));
        boolean isSubscribed;
        try {
            JSONObject jsonObject = new JSONObject(html);
            isSubscribed = jsonObject.optBoolean("subscribed", false);
        } catch (JSONException e) {
            isSubscribed = false;
        }
        return isSubscribed;
    }
}
