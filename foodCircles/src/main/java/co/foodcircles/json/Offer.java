package co.foodcircles.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.foodcircles.util.AndroidUtils;

public class Offer
{
	private String id;
	private String title;
	private String details;
	private String name;
	private int minDiners;
	private int discountPrice;
	private int fullPrice;
	private String imageUrl;
	private String venue;

	public static List<Offer> parseOffers(String jsonString) throws JSONException
	{
		JSONArray jsonArray = new JSONArray(jsonString);
		List<Offer> offers = new ArrayList<Offer>();

		for (int i = 0, ii = jsonArray.length(); i < ii; i++)
		{
			offers.add(new Offer(jsonArray.getString(i)));
		}
		return offers;
	}

	public Offer(String jsonString) throws JSONException
	{
		JSONObject json = new JSONObject(jsonString);
		id = AndroidUtils.safelyGetJsonString(json,"id");
		title = AndroidUtils.safelyGetJsonString(json,"title");
		details = AndroidUtils.safelyGetJsonString(json,"details");
		minDiners = AndroidUtils.safelyGetJsonInt(json,"minimum_diners");
		discountPrice = AndroidUtils.safelyGetJsonInt(json,"price");
		name = AndroidUtils.safelyGetJsonString(json, "name");
		fullPrice = AndroidUtils.safelyGetJsonInt(json,"original_price");
		imageUrl = AndroidUtils.safelyGetJsonString(json, "image_url");
		try {
			venue = AndroidUtils.safelyGetJsonArray(json, "venue").getString(0);
		} catch (Exception e) {}
	}

	public Offer(String id, String title, String details, int minDiners, int discountPrice, int fullPrice, String imageUrl)
	{
		super();
		this.id = id;
		this.title = title;
		this.details = details;
		this.minDiners = minDiners;
		this.discountPrice = discountPrice;
		this.fullPrice = fullPrice;
		this.imageUrl = imageUrl;

	}
	
	public Offer(boolean testOffer)
	{
		super();
		this.id = "1";
		this.title = "Loaded Crack Fries";
		this.details = "Delicious!";
		this.minDiners = 2;
		this.discountPrice = 2;
		this.fullPrice = 4;
		this.imageUrl = "/media/BAhbBlsHOgZmSSIvMjAxMi8wNS8yNC8xM180N181MF81OTRfR29qb19FdGhpb3BpYW4ucG5nBjoGRVQ";
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDetails()
	{
		return details;
	}

	public void setDetails(String details)
	{
		this.details = details;
	}

	public int getMinDiners()
	{
		return minDiners;
	}

	public void setMinDiners(int minDiners)
	{
		this.minDiners = minDiners;
	}

	public int getDiscountPrice()
	{
		return discountPrice;
	}

	public void setDiscountPrice(int discountPrice)
	{
		this.discountPrice = discountPrice;
	}

	public String getVenue()
	{
		return venue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOffer(String venue)
	{
		this.venue = venue;
	}

	public int getFullPrice()
	{
		return fullPrice;
	}

	public void setFullPrice(int fullPrice)
	{
		this.fullPrice = fullPrice;
	}

	public String getImageUrl()
	{
		return imageUrl;
	}

	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}

	public int getChildrenFed() {
		int childrenFed=0;
		if(minDiners>=6){
			childrenFed=3;
		}else if(minDiners>=4){
			childrenFed=2;
		}else if(minDiners>=2){
			childrenFed=1;
		}else{
			childrenFed=0;
		}
		return childrenFed;
	}

}
