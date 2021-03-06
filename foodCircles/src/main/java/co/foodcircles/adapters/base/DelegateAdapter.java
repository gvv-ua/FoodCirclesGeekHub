package co.foodcircles.adapters.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by gvv on 21.03.17.
 */

public interface DelegateAdapter {
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent);
    void onBindViewHolder(RecyclerView.ViewHolder holder, TimelineViewItem viewItem);
}
