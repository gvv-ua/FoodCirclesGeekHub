package co.foodcircles.util;

import java.util.ArrayList;
import java.util.List;

import co.foodcircles.adapters.base.ViewItem;
import co.foodcircles.adapters.viewitems.TimelineExpiringVoucherViewItem;
import co.foodcircles.adapters.viewitems.TimelineFriendViewItem;
import co.foodcircles.adapters.viewitems.TimelineHeaderViewItem;
import co.foodcircles.adapters.viewitems.TimelineMonthViewItem;
import co.foodcircles.adapters.viewitems.TimelineUsedVoucherViewItem;
import co.foodcircles.adapters.viewitems.TimelineVoucherViewItem;
import co.foodcircles.json.Offer;
import co.foodcircles.json.Reservation;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.net.NetException;

/**
 * Created by gvv on 22.03.17.
 */

public class TimelineHelper {

    public static List<ViewItem> fillItems(List<ViewItem> items) {
        List<Venue> venues = new ArrayList<>();

        try {
            venues.addAll(Net.getVenues(-85.632823, 42.955202, null));

            Reservation reservation = new Reservation("1", "gvv", venues.get(0), null, null, 10000);
            reservation.setState(0);
            reservation.setVenue(venues.get(0));
            reservation.setOffer(new Offer(true));
            items.add(new TimelineVoucherViewItem(reservation));

            reservation = new Reservation("2", "gvv1", venues.get(1), null, null, 10000);
            reservation.setState(0);
            reservation.setVenue(venues.get(1));
            reservation.setOffer(new Offer(true));
            items.add(new TimelineVoucherViewItem(reservation));

            reservation = new Reservation("3", "gvv3", venues.get(1), null, null, 10000);
            reservation.setState(1);
            reservation.setVenue(venues.get(1));
            items.add(new TimelineFriendViewItem(reservation));

            reservation = new Reservation("4", "gvv4", venues.get(0), null, null, 10000);
            reservation.setState(2);
            reservation.setVenue(venues.get(0));
            items.add(new TimelineMonthViewItem(reservation));

            reservation = new Reservation("4", "gvv4", venues.get(0), null, null, 10000);
            reservation.setState(3);
            reservation.setVenue(venues.get(0));
            reservation.setOffer(new Offer(true));
            items.add(new TimelineUsedVoucherViewItem(reservation));

            reservation = new Reservation("5", "gvv5", venues.get(1), null, null, 10000);
            reservation.setState(4);
            reservation.setVenue(venues.get(0));
            reservation.setOffer(new Offer(true));
            items.add(new TimelineExpiringVoucherViewItem(reservation));

            reservation = new Reservation("6", "gvv6", venues.get(1), null, null, 10000);
            reservation.setState(5);
            reservation.setVenue(venues.get(0));
            reservation.setOffer(new Offer(true));
            items.add(new TimelineHeaderViewItem(reservation));

        } catch (NetException e) {

        }

        return items;
    }

}
