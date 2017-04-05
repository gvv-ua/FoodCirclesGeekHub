package co.foodcircles.adapters.viewitems;

import android.support.annotation.NonNull;

import co.foodcircles.R;
import co.foodcircles.adapters.base.TimelineViewItem;
import co.foodcircles.json.Reservation;

/**
 * Created by gvv on 22.03.17.
 */

public class TimelineUsedVoucherViewItem implements TimelineViewItem {
    @NonNull
    private final Reservation item;

    public TimelineUsedVoucherViewItem(@NonNull Reservation item) { this.item = item; }

    @NonNull
    public Reservation getItem() { return item; }

    @Override
    public int viewType() { return R.layout.timeline_row_used; }

    @Override
    public long getId() { return item.getId(); }
}
