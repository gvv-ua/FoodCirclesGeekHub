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
import co.foodcircles.adapters.viewitems.TimelineVoucherViewItem;
import co.foodcircles.util.FoodCirclesUtils;

/**
 * Created by gvv on 22.03.17.
 */

public class TimelineVoucherAdapter implements DelegateAdapter {
    private final TimelineAdapter.ItemClickListener clickListener;

    public TimelineVoucherAdapter(TimelineAdapter.ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ItemViewHolder(inflater.inflate(R.layout.timeline_row, parent, false), clickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, TimelineViewItem viewItem) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.bind((TimelineVoucherViewItem) viewItem);
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TimelineAdapter.ItemClickListener clickListener;
        private TimelineVoucherViewItem item;
        TextView date;
        TextView venue;
        TextView childrenFed;

        public ItemViewHolder(View itemView, TimelineAdapter.ItemClickListener clickListener) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.textViewDate);
            venue = (TextView) itemView.findViewById(R.id.textViewVenue);
            childrenFed = (TextView) itemView.findViewById(R.id.textViewChildrenFed);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(item.getItem());
        }

        private void bind(TimelineVoucherViewItem item) {
            this.item = item;
            date.setText(FoodCirclesUtils.convertLongIntoStringDate(item.getItem().getDatePurchased()));
            venue.setText(item.getItem().getVenue().getName());
            int kidsFed = item.getItem().getKidsFed();
            if (kidsFed == 1) {
                childrenFed.setText(String.format(itemView.getContext().getString(R.string.child_fed), kidsFed));
            } else {
                childrenFed.setText(String.format(itemView.getContext().getString(R.string.children_fed), kidsFed));
            }
        }
    }
}
