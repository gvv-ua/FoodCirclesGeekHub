package co.foodcircles.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.foodcircles.R;
import co.foodcircles.adapters.base.DelegateAdapter;
import co.foodcircles.adapters.base.TimelineViewItem;
import co.foodcircles.adapters.viewitems.TimelineUsedVoucherViewItem;
import co.foodcircles.util.FoodCirclesUtils;

/**
 * Created by gvv on 22.03.17.
 */

public class TimelineUsedVoucherAdapter implements DelegateAdapter {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ItemViewHolder(inflater.inflate(R.layout.timeline_row_used, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, TimelineViewItem viewItem) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.bind((TimelineUsedVoucherViewItem) viewItem);
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TimelineUsedVoucherViewItem item;
        TextView date;
        TextView venue;
        TextView childrenFed;

        public ItemViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.textViewDate);
            venue = (TextView) itemView.findViewById(R.id.textViewVenue);
            childrenFed = (TextView) itemView.findViewById(R.id.textViewChildrenFed);
        }

        private void bind(TimelineUsedVoucherViewItem item) {
            this.item = item;
            date.setText(FoodCirclesUtils.convertLongIntoStringDate(item.getItem().getDatePurchased()));
            venue.setText(item.getItem().getVenue().getName());
            int kidsFed = item.getItem().getOffer().getChildrenFed();
            if (kidsFed == 1) {
                childrenFed.setText(String.format(itemView.getContext().getString(R.string.child_fed), kidsFed));
            } else {
                childrenFed.setText(String.format(itemView.getContext().getString(R.string.children_fed), kidsFed));
            }
        }
    }
}
