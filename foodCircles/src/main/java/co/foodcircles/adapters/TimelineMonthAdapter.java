package co.foodcircles.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.foodcircles.R;
import co.foodcircles.adapters.base.DelegateAdapter;
import co.foodcircles.adapters.base.ViewItem;
import co.foodcircles.adapters.viewitems.TimelineMonthViewItem;
import co.foodcircles.util.FoodCirclesUtils;

/**
 * Created by gvv on 22.03.17.
 */

public class TimelineMonthAdapter implements DelegateAdapter {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ItemViewHolder(inflater.inflate(R.layout.timeline_row_month, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, ViewItem viewItem) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.bind((TimelineMonthViewItem) viewItem);
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TimelineMonthViewItem item;
        TextView month;
        TextView year;

        public ItemViewHolder(View itemView) {
            super(itemView);
            month = (TextView) itemView.findViewById(R.id.textViewMonth);
            year = (TextView) itemView.findViewById(R.id.textViewYear);
        }

        private void bind(TimelineMonthViewItem item) {
            this.item = item;
            month.setText(FoodCirclesUtils.convertLongToFormattedDateString(item.getItem().getDatePurchased(), "MMMMM"));
            year.setText(FoodCirclesUtils.convertLongToFormattedDateString(item.getItem().getDatePurchased(), "yyyy"));
        }
    }
}
