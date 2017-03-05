package co.foodcircles.net;

import android.annotation.SuppressLint;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import co.foodcircles.exception.NetException2;
import co.foodcircles.json.Charity;
import co.foodcircles.json.Reservation;
import co.foodcircles.json.Venue;
import co.foodcircles.json.Voucher;
import co.foodcircles.util.MySSLSocketFactory;


public class Net {
    private static final String TAG = Net.class.getSimpleName();

	//public static final String HOST = "http://staging.foodcircles.net";
    public static final String HOST = "https://joinfoodcircles.org";
	private static final String API_URL = "/api";
	private static final String GET_VENUES = "/venues/%f/%f";
	private static final String GET_RESERVATION = "/reservations/[reservationId]";
	private static final String GET_CHARITY_1 = "/charities";
	private static final String GET_NEWS = "/news";
	private static final String MARK_REDEEMED = "/payment/used?code=";
	private static final String GET_TIMELINE="/timeline?auth_token=%s";
    private static final String VENUES_SUBSCRIBE = "/venues/%s/subscribe";
	//public static String logo="http://staging.foodcircles.net/media/BAhbBlsHOgZmSSIkMjAxMy8wOC8yMC8xNl81Nl8xMV84MzNfRkFRLnBuZwY6BkVU";
    public static String logo="https://joinfoodcircles.org/media/BAhbBlsHOgZmSSIkMjAxMy8wOC8yMC8xNl81Nl8xMV84MzNfRkFRLnBuZwY6BkVU";
	
	private static String post(String path, List<BasicNameValuePair> postValues) {
		HttpContext httpContext = new BasicHttpContext();
		HttpClient httpclient = createHttpClient();
		HttpPost httppost = new HttpPost(HOST + path);
		String response = "";
        Log.d(TAG, "post request url = " + HOST + path);
		try {
            if (postValues != null)
			    httppost.setEntity(new UrlEncodedFormEntity(postValues, HTTP.UTF_8));
			httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			HttpResponse httpResp = httpclient.execute(httppost, httpContext);
			response = EntityUtils.toString(httpResp.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}
        Log.d(TAG, "get response = " + response);
		return response;
	}

	private static String postRedeemed(String id) {
		String responseString = null;
		HttpClient httpclient = createHttpClient();
	    HttpResponse response = null;
		try {
			response = httpclient.execute(new HttpGet(HOST + MARK_REDEEMED + id));
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
	    	response.getEntity().writeTo(out);
	    	out.close();
	    	responseString = out.toString();
	    } catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		return responseString;
	}

    private static String get(String path, List<BasicNameValuePair> urlParams) {
        StringBuilder builder = new StringBuilder();
        HttpClient httpclient = createHttpClient();
        HttpGet httpGet = new HttpGet(HOST + path);
        try {
            HttpResponse response = httpclient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String response = builder.toString();
        return response;
    }

    private static String put(String path, List<BasicNameValuePair> urlParams) {
		HttpContext httpContext = new BasicHttpContext();
		HttpClient httpclient = createHttpClient();
		HttpPut httpput = new HttpPut(HOST + API_URL + path);
		String response = "";


		try {
			String paramString = URLEncodedUtils.format(urlParams, "utf-8");
			path += paramString;
			HttpResponse httpResp = httpclient.execute(httpput, httpContext);
			response = EntityUtils.toString(httpResp.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}
        Log.d(TAG, "post response = " + response);
		return response;
	}

    public static HttpClient createHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    @SuppressLint("DefaultLocale")
	public static List<Venue> getVenues(double longitude,double latitude,List<BasicNameValuePair> filters)
			throws NetException {
		try {
			String url=String.format(GET_VENUES,latitude,longitude);
			String response = get(API_URL + url, filters);
			return Venue.parseVenues(response);
		} catch (JSONException j) {
			throw new NetException();
		}
	}

	public static List<Reservation> getReservationsList(String token) throws NetException {
		try {
			String url=String.format(GET_TIMELINE,token);
			String response = get(API_URL + url, null);
			return Reservation.parseReservations(response);
		} catch (JSONException j) {
			throw new NetException();
		}
	}

	public static Reservation getReservation(String reservationId)
			throws NetException {
		try {
			String response = get(
                    API_URL + GET_RESERVATION.replace("[reservationId]", reservationId),
					null);
			return new Reservation(response);
		} catch (JSONException j) {
			throw new NetException();
		}
	}

	public static List<Charity> getCharities() throws NetException {
		List<Charity> charities = new ArrayList<Charity>();
		String response = get(API_URL + GET_CHARITY_1, null);
		try {
			JSONObject jsonObject=new JSONObject(response);
			JSONArray jsonArray=jsonObject.getJSONArray("content");
			for(int ctr=0;ctr<jsonArray.length();ctr++){
			charities.add(new Charity(jsonArray.getJSONObject(ctr).toString()));
			}
		} catch (JSONException e) {
			throw new NetException();
		}
		return charities;
	}

	public static String signUp(String userEmail, String userPassword)
			throws NetException2 {
		List<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
		postValues.add(new BasicNameValuePair("user_email", userEmail));
		postValues.add(new BasicNameValuePair("user_password", userPassword));
		String html = post(API_URL + "/sessions/sign_up", postValues);
		NetException2 n = new NetException2();
		try {
			JSONObject json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(getSignUpErrorMsg(json));
				throw n;
			}
			String token=json.getString("auth_token");
			return token;
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}
	}
	
	private static String getSignUpErrorMsg(JSONObject json) throws JSONException {
		JSONObject errorsJson = json.getJSONObject("errors");
		
		JSONArray emailErrors = errorsJson.getJSONArray("email");
		String emailErrMsg = emailErrors.getString(0);
		
		JSONArray passwordErrors = errorsJson.getJSONArray("password");
		String passwordErrMsg = passwordErrors.getString(0);
		
		String errMsgDescription = json.getString("description");
		
		String errMsg = String.format("%s\nEmail %s\nPassword %s", errMsgDescription, emailErrMsg, passwordErrMsg);
		return errMsg;
		
	}

	public static String twitterSignUp(String userEmail, String UID)
			throws NetException2 {
		List<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
		postValues.add(new BasicNameValuePair("user_email", userEmail));
		postValues.add(new BasicNameValuePair("uid", UID));
		String html = post(API_URL + "/sessions/sign_up", postValues);
		NetException2 n = new NetException2();
		try {
			JSONObject json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(json.getString("description"));
				throw n;
			}
			String token=json.getString("auth_token");
			System.out.println("TOKEN ::"+token);
			return token;
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}
	}
	
	public static String twittersignIn(String uid)
			throws NetException2 {
		List<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
		postValues.add(new BasicNameValuePair("uid", uid));
		String html = post(API_URL + "/sessions/sign_in", postValues);
		JSONObject json;
		NetException2 n = new NetException2();
		try {
			json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(json.getString("description"));
				return json.getString("description");
			}
			return json.getString("auth_token");
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}
	}

	public static String facebookSignUp(String userID,String emailid)
			throws NetException2 {
		List<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
		Log.i("ID",userID);
		Log.i("Email",emailid);
		postValues.add(new BasicNameValuePair("uid", userID));
		postValues.add(new BasicNameValuePair("user_email", emailid));
		String html = post(API_URL + "/sessions/sign_up", postValues);
		NetException2 n = new NetException2();
		try {
			JSONObject json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(json.getString("description"));
				Log.i("Error1",n.getMessage());
				throw n;
			}
			String token=json.getString("auth_token");
			return token;
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			Log.i("Error2",n.getMessage());
			throw n;
		}
	}
	
	public static String signIn(String userEmail, String userPassword)
			throws NetException2 {
		List<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
		postValues.add(new BasicNameValuePair("user_email", userEmail));
		postValues.add(new BasicNameValuePair("user_password", userPassword));
		String html = post(API_URL + "/sessions/sign_in", postValues);
		JSONObject json;
		NetException2 n = new NetException2();
		try {
			json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(getSignUpErrorMsg(json));
				throw n;
			}
			return json.getString("auth_token");
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}
	}

	public static void updateUserInfo(String authToken,String userEmail, String userPassword,
			String name, String phone) throws NetException2 {
		List<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
		
		postValues.add(new BasicNameValuePair("user_email", userEmail));
		postValues.add(new BasicNameValuePair("user_password", userPassword));
		postValues.add(new BasicNameValuePair("name", name));
		postValues.add(new BasicNameValuePair("phone", phone));
		String html = put("/sessions/update?auth_token="+authToken, postValues);

		NetException2 n = new NetException2();
		JSONObject json;
		try {
			json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(json.getString("description"));
				throw n;
			}
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}
	}

	public static void getNews() throws NetException2 {
		List<BasicNameValuePair> getValues = new ArrayList<BasicNameValuePair>();
		String html = get(API_URL + GET_NEWS, getValues);
		NetException2 n = new NetException2();
		JSONObject json;
		try {
			json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(json.getString("description"));
				throw n;
			}
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}
	}

	public static void getTimeLine() throws NetException2 {
		List<BasicNameValuePair> getValues = new ArrayList<BasicNameValuePair>();
		String html = get(API_URL + "/timeline/", getValues);
		NetException2 n = new NetException2();
		try {
			JSONObject json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(json.getString("description"));
				throw n;
			}
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}

	}

	public static void getTimeLineVoucher(int id) throws NetException2 {
		List<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
		String html = post(API_URL + "/timeline/voucher/" + id, postValues);
		NetException2 n = new NetException2();
		try {
			JSONObject json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				n.setMessage(json.getString("description"));
				throw n;
			}
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}
	}

	public static boolean markReservationAsUsed(String id) {
		String response = postRedeemed(id.trim());
		return response.equals("success");
	}

	// Attempts to verify the payment, and if successful, returns the certificate information
	public static Voucher verifyPayment(String token,int priceValue, String offerId, String payKey, int charity) throws NetException2 {
		List<BasicNameValuePair> putValues = new ArrayList<BasicNameValuePair>();
		putValues.add(new BasicNameValuePair("auth_token", token));
		putValues.add(new BasicNameValuePair("payment[amount]", priceValue + ""));
		putValues.add(new BasicNameValuePair("payment[offer_id]", offerId));
		putValues.add(new BasicNameValuePair("payment[paypal_charge_token]", payKey));
		putValues.add(new BasicNameValuePair("payment[charity_id]",""+ charity));
		String html = post(API_URL + "/payments", putValues);
		NetException2 n = new NetException2();
		try {
			return Voucher.parseVoucher(html);
		} catch (JSONException e) {
			n.setMessage(e.getMessage());
			throw n;
		}

	}

	public static String getMailChimp() throws NetException2 {
		List<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
		String html = get(API_URL + "/general/users", postValues);
		try {
			JSONObject json = new JSONObject(html);
			if (json.getBoolean("error") == true) {
				return "Many";
			}
			return (json.getInt("content") + "");
		} catch (JSONException e) {
			return "Many";
		}
	}

    public static String subscribeVenue(String slug, String authToken) {
        String html = post(String.format("/venues/%s/subscribe?auth_token=%s", slug, authToken), null);
        return html;
    }

    public static String unsubscribeVenue(String slug, String authToken) {
        String html = post(String.format("/venues/%s/unsubscribe?auth_token=%s", slug, authToken), null);
        return html;
    }

    public static boolean isSubscribed(String slug, String authToken) {
        String html = get(String.format("/venues/%s/subscribed.json?auth_token=%s", slug, authToken), null);
        boolean isSubscribed = false;
        try {
            JSONObject jsonObject=new JSONObject(html);
            isSubscribed = jsonObject.optBoolean("subscribed", false);
        } catch (JSONException e) {
            isSubscribed = false;
        }
        return isSubscribed;
    }
}
