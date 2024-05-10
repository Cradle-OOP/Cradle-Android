package com.oop.gch.model;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class ForRent {

    @DocumentId
    private String docId;
    private String uid;

    private String imageFilename;
    private String name;
    private String contactPerson;
    private String contactNumber;
    private float price;
    private List<String> billsIncluded;
    private String address;
    @Nullable
    private String curfew;
    private int contract;
    private double latitude;
    private double longitude;
    private String otherDetails;

    @ServerTimestamp
    private Date dateCreated;

    @Exclude
    public Uri imageDownloadUrl;

    public ForRent() {
    }

    public ForRent(String uid, String imageFilename, String name, String contactPerson, String contactNumber, float price, List<String> billsIncluded, String address, @Nullable String curfew, int contract, double latitude, double longitude, String otherDetails) {
        this.uid = uid;
        this.imageFilename = imageFilename;
        this.name = name;
        this.contactPerson = contactPerson;
        this.contactNumber = contactNumber;
        this.price = price;
        this.billsIncluded = billsIncluded;
        this.address = address;
        this.curfew = curfew;
        this.contract = contract;
        this.latitude = latitude;
        this.longitude = longitude;
        this.otherDetails = otherDetails;
    }

    public String getDocId() {
        return docId;
    }
    public String getUid() {
        return uid;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public String getName() {
        return name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public List<String> getBillsIncluded() {
        return billsIncluded;
    }

    public float getPrice() {
        return price;
    }

    public String getAddress() {
        return address;
    }

    @Nullable
    public String getCurfew() {
        return curfew;
    }

    public int getContract() {
        return contract;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getOtherDetails() {
        return otherDetails;
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}
