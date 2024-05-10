package com.oop.gch.list;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oop.gch.R;
import com.oop.gch.model.Apartment;
import com.oop.gch.model.ForRent;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ForRent item);
        void onItemLongClick(ForRent item);
    }

    private final List<ForRent> dataset;
    private final OnItemClickListener listener;

    public static class ListingViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewType;
        private final ImageView imageView;
        private final TextView textViewPrice;
        private final TextView textViewContract;
        private final TextView textViewDetails;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewType = itemView.findViewById(R.id.textViewType);
            imageView = itemView.findViewById(R.id.imageView);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewContract = itemView.findViewById(R.id.textViewContract);
            textViewDetails = itemView.findViewById(R.id.textViewDetails);
        }

        public TextView getTextViewName() {
            return textViewName;
        }

        public TextView getTextViewType() {
            return textViewType;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getTextViewPrice() {
            return textViewPrice;
        }

        public TextView getTextViewContract() {
            return textViewContract;
        }

        public TextView getTextViewDetails() {
            return textViewDetails;
        }
    }

    public ListingAdapter(List<ForRent> dataset, OnItemClickListener listener) {
        this.dataset = dataset;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listing_item, parent, false);
        return new ListingViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        holder.getTextViewName().setText(dataset.get(position).getName());
        holder.getTextViewType().setText(dataset.get(position) instanceof Apartment ? "Apartment" : "Bedspace");
        holder.getTextViewPrice().setText("₱" + String.format("%.2f", dataset.get(position).getPrice()) + " / month");
        holder.getTextViewContract().setText(dataset.get(position).getContract() + "-year contract");
        holder.getTextViewDetails().setText(dataset.get(position).getOtherDetails());

        Picasso.get().load(dataset.get(position).imageDownloadUrl).into(holder.getImageView());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(dataset.get(position)));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(dataset.get(position));
            return true;
        });
    }

    @Override
    public int getItemCount() {
        Log.i(TAG, "getItemCount: " + this.dataset.size());
        return this.dataset.size();
    }

    public void updateData(List<ForRent> listings) {
        dataset.clear();
        dataset.addAll(listings);
        notifyDataSetChanged();
    }

    private static final String TAG = "ListingAdapter";

}
