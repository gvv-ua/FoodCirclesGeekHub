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
import co.foodcircles.adapters.viewitems.TimelineExpiringVoucherViewItem;
import co.foodcircles.util.FoodCirclesUtils;

/**
 * Created by gvv on 22.03.17.
 */

public class TimelineExpiringVoucherAdapter implements DelegateAdapter {
    private final TimelineAdapter.ItemClickListener clickListener;

    public TimelineExpiringVoucherAdapter(TimelineAdapter.ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ItemViewHolder(inflater.inflate(R.layout.timeline_row_expiring, parent, false), clickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, TimelineViewItem viewItem) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.bind((TimelineExpiringVoucherViewItem) viewItem);
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TimelineAdapter.ItemClickListener clickListener;
        private TimelineExpiringVoucherViewItem item;
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

        private void bind(TimelineExpiringVoucherViewItem item) {
            this.item = item;
            date.setText(FoodCirclesUtils.convertLongIntoStringDate(item.getItem().getStartsExpiring().getTime()));
            venue.setText(item.getItem().getVenue().getName());
            int kidsFed = item.getItem().getOffer().getChildrenFed();
            childrenFed.setText(String.format(itemView.getContext().getResources().getQuantityString(R.plurals.children_fed, kidsFed), kidsFed));
        }
    }
}
