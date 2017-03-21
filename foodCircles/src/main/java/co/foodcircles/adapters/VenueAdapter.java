package co.foodcircles.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import co.foodcircles.R;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;

/**
 * Created by gvv on 21.03.17.
 */

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {
    private LayoutInflater inflater;
    private final List<Venue> items;
    private final ItemClickListener clickListener;
    Context context;

    public VenueAdapter(Context context, List<Venue> items, ItemClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
        this.context = context;
    }

    @Override
    public VenueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.getContext());
        }
        return new VenueViewHolder(inflater.inflate(R.layout.polaroid, parent, false), clickListener);
    }

    @Override
    public void onBindViewHolder(VenueViewHolder holder, int position) {
        Venue venue = items.get(position);
        holder.bind(venue);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VenueViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ItemClickListener clickListener;
        private Venue item;
        private ImageView logo;
        private TextView name;
        private TextView cuisine;
        private TextView soldOut;
        private TextView distance;
        private TextView left;

        public VenueViewHolder(View itemView, ItemClickListener clickListener) {
            super(itemView);
            logo = (ImageView) itemView.findViewById(R.id.imageViewLogo);
            name = (TextView) itemView.findViewById(R.id.textViewName);
            cuisine = (TextView) itemView.findViewById(R.id.textViewCuisine);
            soldOut = (TextView) itemView.findViewById(R.id.SoldOutText);
            distance = (TextView) itemView.findViewById(R.id.textViewDistance);
            left = (TextView) itemView.findViewById(R.id.textViewLeft);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(item);
        }

        void bind(Venue item) {
            this.item = item;
            Glide.with(VenueAdapter.this.context).load(Net.HOST + item.getImageUrl()).into(logo);
            name.setText(item.getName());
            cuisine.setText(item.getFirstTag());
            distance.setText(item.getDistance());
            left.setText("" + item.getVouchersAvailable());
            soldOut.setVisibility(item.checkEmpty());
        }
    }

    public interface ItemClickListener {
        void onItemClick(Venue item);
    }
}
