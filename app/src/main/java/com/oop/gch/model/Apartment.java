package com.oop.gch.model;

import androidx.annotation.Nullable;

import java.util.List;

public class Apartment extends ForRent {
    private int noOfBedrooms;
    private int noOfBathrooms;
    private int capacity;

    public Apartment() {
    }

    public Apartment(
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
            int noOfBedrooms,
            int noOfBathrooms,
            int capacity) {
        super(uid, imageFilename, name, contactPerson, contactNumber, price, billsIncluded, address, curfew, contract, latitude, longitude, otherDetails);

        this.noOfBedrooms = noOfBedrooms;
        this.noOfBathrooms = noOfBathrooms;
        this.capacity = capacity;
    }

    public int getNoOfBedrooms() {
        return noOfBedrooms;
    }

    public int getNoOfBathrooms() {
        return noOfBathrooms;
    }

    public int getCapacity() {
        return capacity;
    }

}
