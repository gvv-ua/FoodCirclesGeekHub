package co.foodcircles.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import co.foodcircles.R;
import co.foodcircles.adapters.base.DelegateAdapter;
import co.foodcircles.adapters.base.TimelineViewItem;
import co.foodcircles.adapters.viewitems.TimelineHeaderViewItem;

/**
 * Created by gvv on 22.03.17.
 */

public class TimelineHeaderAdapter implements DelegateAdapter {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ItemViewHolder(inflater.inflate(R.layout.timeline_top_row, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, TimelineViewItem viewItem) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.bind((TimelineHeaderViewItem) viewItem);
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TimelineHeaderViewItem item;
        TextView me;
        TextView friends;
        TextView childrenFed;
        ImageView settingsButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            me = (TextView) itemView.findViewById(R.id.textViewMe);
            friends = (TextView) itemView.findViewById(R.id.textViewFriends);
            childrenFed = (TextView) itemView.findViewById(R.id.textViewKidsFed);
            settingsButton = (ImageView) itemView.findViewById(R.id.button);
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(getActivity(), AccountOptionsActivity.class);
//                    startActivity(intent);
                }
            });
        }

        private void bind(TimelineHeaderViewItem item) {
            this.item = item;
            int totalKidsFed = 0;
            childrenFed.setText(String.format(itemView.getContext().getResources().getQuantityString(R.plurals.children_fed, totalKidsFed), totalKidsFed));
        }
    }
}
