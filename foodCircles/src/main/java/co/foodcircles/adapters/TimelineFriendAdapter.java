package co.foodcircles.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import co.foodcircles.R;
import co.foodcircles.adapters.base.DelegateAdapter;
import co.foodcircles.adapters.base.TimelineViewItem;
import co.foodcircles.adapters.viewitems.TimelineFriendViewItem;
import co.foodcircles.net.Net;
import co.foodcircles.util.FoodCirclesUtils;

/**
 * Created by gvv on 22.03.17.
 */

public class TimelineFriendAdapter implements DelegateAdapter {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ItemViewHolder(inflater.inflate(R.layout.timeline_row_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, TimelineViewItem viewItem) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.bind((TimelineFriendViewItem) viewItem);
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TimelineFriendViewItem item;
        TextView date;
        TextView venue;
        TextView name;
        TextView childrenFed;
        ImageView image;

        public ItemViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.textViewDate);
            venue = (TextView) itemView.findViewById(R.id.textViewVenue);
            name = (TextView) itemView.findViewById(R.id.textViewName);
            childrenFed = (TextView) itemView.findViewById(R.id.textViewChildrenFed);
            image = (ImageView) itemView.findViewById(R.id.imageViewFriend);
        }

        private void bind(TimelineFriendViewItem item) {
            this.item = item;
            date.setText(FoodCirclesUtils.convertLongIntoStringDate(item.getItem().getDatePurchased()));
            venue.setText(item.getItem().getVenue().getName());
            Glide.with(itemView.getContext()).load(Net.HOST + item.getItem().getVenue().getImageUrl()).into(image);
        }
    }
}
