package com.oop.gch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.oop.gch.components.CustomProgressDialog;
import com.oop.gch.model.Apartment;
import com.oop.gch.model.Bedspace;
import com.oop.gch.model.ForRent;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingDetailFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private String id;

    private ImageView imageView;
    private TextView textViewName;
    private TextView textViewAddress;
    private LinearLayout linearLayoutCapacity;
    private TextView textViewCapacity;
    private TextView textViewContactPerson;
    private Button buttonCall;
    private TextView textViewBillsIncluded;
    private TextView textViewCurfew;
    private LinearLayout linearLayoutApartment;
    private TextView textViewBedrooms;
    private TextView textViewBathrooms;
    private LinearLayout linearLayoutBedspace;
    private TextView textViewRoommates;
    private TextView textViewBathroomShareCount;
    private TextView textViewPrice;
    private TextView textViewContract;

    private String phoneNumber;

    public ListingDetailFragment() {
        // Required empty public constructor
    }

    public static ListingDetailFragment newInstance() {
        return new ListingDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = requireArguments();
        id = arguments.getString("id");

        View view = inflater.inflate(R.layout.fragment_listing_detail, container, false);

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        imageView = view.findViewById(R.id.imageView);
        textViewName = view.findViewById(R.id.textViewName);
        textViewAddress = view.findViewById(R.id.textViewAddress);
        linearLayoutCapacity = view.findViewById(R.id.linearLayoutCapacity);
        textViewCapacity = view.findViewById(R.id.textViewCapacity);
        textViewContactPerson = view.findViewById(R.id.textViewContactPerson);
        buttonCall = view.findViewById(R.id.buttonCall);
        textViewBillsIncluded = view.findViewById(R.id.textViewBillsIncluded);
        textViewCurfew = view.findViewById(R.id.textViewCurfew);
        linearLayoutApartment = view.findViewById(R.id.linearLayoutApartment);
        textViewBedrooms = view.findViewById(R.id.textViewBedrooms);
        textViewBathrooms = view.findViewById(R.id.textViewBathrooms);
        linearLayoutBedspace = view.findViewById(R.id.linearLayoutBedspace);
        textViewRoommates = view.findViewById(R.id.textViewRoommates);
        textViewBathroomShareCount = view.findViewById(R.id.textViewBathroomShareCount);
        textViewPrice = view.findViewById(R.id.textViewPrice);
        textViewContract = view.findViewById(R.id.textViewContract);

        buttonCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        });

        topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        CustomProgressDialog progressDialog = new CustomProgressDialog(requireContext(), "Loading...");
        progressDialog.show();
        db.collection("listings").document(id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        DocumentSnapshot result = task.getResult();
                        if (result.exists()) {
                            ForRent forRent;
                            if (snapshot.contains("noOfBedrooms")) {
                                Apartment apartment = result.toObject(Apartment.class);
                                forRent = apartment;
                                linearLayoutCapacity.setVisibility(View.VISIBLE);
                                linearLayoutApartment.setVisibility(View.VISIBLE);
                                linearLayoutBedspace.setVisibility(View.GONE);
                                textViewCapacity.setText(String.valueOf(apartment.getCapacity()));
                                textViewBedrooms.setText(apartment.getNoOfBedrooms() + " bedroom/s");
                                textViewBathrooms.setText(apartment.getNoOfBathrooms() + " bathroom/s");
                            } else {
                                Bedspace bedspace = result.toObject(Bedspace.class);
                                linearLayoutCapacity.setVisibility(View.GONE);
                                linearLayoutApartment.setVisibility(View.GONE);
                                linearLayoutBedspace.setVisibility(View.VISIBLE);
                                textViewRoommates.setText(bedspace.getRoommateCount() + " roommate/s");
                                textViewBathroomShareCount.setText(String.valueOf(bedspace.getBathroomShareCount()));
                                forRent = bedspace;
                            }
                            String imageFilename = forRent.getImageFilename();
                            storage.getReference().child(imageFilename).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Picasso.get().load(task.getResult()).into(imageView);
                                    }
                                }
                            });

                            String billsIncluded = String.join(", ", forRent.getBillsIncluded());
                            phoneNumber = forRent.getContactNumber();

                            textViewName.setText(forRent.getName());
                            textViewAddress.setText(forRent.getAddress());
                            textViewContactPerson.setText("CONTACT PERSON (" + forRent.getContactPerson() + ")");
                            buttonCall.setText("Call " + phoneNumber);
                            textViewBillsIncluded.setText(billsIncluded.isEmpty() ? "None" : billsIncluded);
                            textViewCurfew.setText(forRent.getCurfew());
                            textViewPrice.setText("₱" + String.format("%.2f", forRent.getPrice()));
                            textViewContract.setText(forRent.getContract() + " year/s");
                        }
                    }
                    progressDialog.dismiss();
                });

        return view;
    }
}