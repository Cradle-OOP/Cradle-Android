package com.oop.gch.model;

import androidx.annotation.Nullable;

import java.util.List;

public class Bedspace extends ForRent {

    private int roommateCount;
    private int bathroomShareCount;

    public Bedspace() {
    }

    public Bedspace(
            String uid,
            String imageFilename,
            String name,
            String contactPerson,
            String contactNumber,
            float price,
            List<String> billsIncluded,
            String address,
            @Nullable String curfew,
            int contract,
            double latitude,
            double longitude,
            String otherDetails,
            int roommateCount,
            int bathroomShareCount) {
        super(uid, imageFilename, name, contactPerson, contactNumber, price, billsIncluded, address, curfew, contract, latitude, longitude, otherDetails);

        this.roommateCount = roommateCount;
        this.bathroomShareCount = bathroomShareCount;
    }

    public int getRoommateCount() {
        return roommateCount;
    }

    public int getBathroomShareCount() {
        return bathroomShareCount;
    }
}
