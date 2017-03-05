package co.foodcircles.util;

import java.util.Comparator;

import co.foodcircles.json.Venue;

public class SortListByDistance implements Comparator<Venue> {

	@Override
	public int compare(Venue v1, Venue v2) {
		return v1.getDistance().compareTo(v2.getDistance()); 
	}

}
