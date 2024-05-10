package com.oop.gch;


import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.oop.gch.list.ListingAdapter;
import com.oop.gch.model.Apartment;
import com.oop.gch.model.Bedspace;
import com.oop.gch.model.ForRent;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static int convertDpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static List<ForRent> updateListings(RecyclerView recyclerView, QuerySnapshot value) {
        List<ForRent> listings = new ArrayList<>();
        if (value != null) {
            for (DocumentSnapshot snapshot : value.getDocuments()) {
                if (snapshot.contains("noOfBedrooms")) {
                    listings.add(snapshot.toObject(Apartment.class));
                } else {
                    listings.add(snapshot.toObject(Bedspace.class));
                }
            }
            ListingAdapter adapter = (ListingAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.updateData(listings);
            }
        }
        return listings;
    }
}