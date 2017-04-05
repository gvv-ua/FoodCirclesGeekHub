package co.foodcircles.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.foodcircles.R;
import co.foodcircles.adapters.base.DelegateAdapter;
import co.foodcircles.adapters.base.TimelineViewItem;
import co.foodcircles.json.Reservation;

/**
 * Created by gvv on 21.03.17.
 */

public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = "TimelineAdapter";

    private final SparseArray<DelegateAdapter> adapters = new SparseArray<>();
    private final List<TimelineViewItem> items = new ArrayList<>();

    public TimelineAdapter(List<TimelineViewItem> items, ItemClickListener clickListener) {
        adapters.put(R.layout.timeline_top_row, new TimelineHeaderAdapter());
        adapters.put(R.layout.timeline_row, new TimelineVoucherAdapter(clickListener));
        adapters.put(R.layout.timeline_row_friend, new TimelineFriendAdapter());
        adapters.put(R.layout.timeline_row_used, new TimelineUsedVoucherAdapter());
        adapters.put(R.layout.timeline_row_expiring, new TimelineExpiringVoucherAdapter(clickListener));
        adapters.put(R.layout.timeline_row_month, new TimelineMonthAdapter());

        setHasStableIds(true);
        updateAdapter(items);
    }

    public void updateAdapter(@Nullable List<TimelineViewItem> items) {
        this.items.clear();
        if (items != null) {
            this.items.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return adapters.get(viewType)
                .onCreateViewHolder(LayoutInflater.from(parent.getContext()), parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        adapters.get(getItemViewType(position))
                .onBindViewHolder(holder, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        try {
            return items.get(position).viewType();
        } catch (Exception e) {
            Log.d(TAG, "getItemViewType failed!");
            return 1;
        }
    }

    public interface ItemClickListener {
        void onItemClick(Reservation item);
    }
}
