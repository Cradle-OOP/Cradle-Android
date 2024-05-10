package com.oop.gch.seller;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oop.gch.components.CustomProgressDialog;
import com.oop.gch.R;
import com.oop.gch.model.Apartment;
import com.oop.gch.model.Bedspace;
import com.oop.gch.model.ForRent;
import com.squareup.picasso.Picasso;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingAddEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressLint("NewApi")
public class ListingAddEditFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private boolean isNew;
    private String docId;

    private MaterialToolbar topAppBar;
    private MaterialButtonToggleGroup toggleButtonType;
    private LinearLayout linearLayoutApartmentDetails;
    private LinearLayout linearLayoutBedspaceDetails;
    private ImageView imageView;
    private Button buttonAddImage;
    private TextInputLayout textFieldName;
    private TextInputLayout textFieldContactPerson;
    private TextInputLayout textFieldContactNumber;
    private TextInputLayout textFieldAddress;
    private TextInputLayout textFieldPrice;
    private MaterialButtonToggleGroup toggleButtonBillsIncluded;
    private MaterialCardView cardViewBillsIncluded;
    private ChipGroup chipGroupBillsIncluded;
    private MaterialButtonToggleGroup toggleButtonCurfew;
    private Button buttonCurfewTime;
    private MaterialButtonToggleGroup toggleButtonContract;
    private TextInputLayout textFieldContractYears;
    private TextInputLayout textFieldOtherDetails;

    // For apartment
    private TextInputLayout textFieldBathrooms;
    private TextInputLayout textFieldBedrooms;
    private TextInputLayout textFieldCapacity;

    // For bedspace
    private TextInputLayout textFieldRoommateCount;
    private TextInputLayout textFieldBathroomShareCount;

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    CustomProgressDialog customProgressDialog;

    String imageFilename;

    MaterialTimePicker fromPicker;
    MaterialTimePicker toPicker;

    LocalTime curfewFromTime = LocalTime.of(22, 0);
    LocalTime curfewToTime = LocalTime.of(4, 0);

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    public ListingAddEditFragment() {
        // Required empty public constructor
    }

    public static ListingAddEditFragment newInstance() {
        return new ListingAddEditFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        customProgressDialog = new CustomProgressDialog(requireContext(), "Uploading...");

        fromPicker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(22)
                        .setMinute(0)
                        .setTitleText("Set curfew start time")
                        .build();
        toPicker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(4)
                        .setMinute(0)
                        .setTitleText("Set curfew end time")
                        .build();

        fromPicker.addOnPositiveButtonClickListener(v -> {
            toPicker.show(getChildFragmentManager(), "curfew to time");
            curfewFromTime = LocalTime.of(fromPicker.getHour(), fromPicker.getMinute());
            buttonCurfewTime.setText(curfewFromTime.format(timeFormatter) + " - " + curfewToTime.format(timeFormatter));
        });

        toPicker.addOnPositiveButtonClickListener(v -> {
            curfewToTime = LocalTime.of(toPicker.getHour(), toPicker.getMinute());
            buttonCurfewTime.setText(curfewFromTime.format(timeFormatter) + " - " + curfewToTime.format(timeFormatter));
        });

        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        StorageReference imageRef = storage.getReference().child(UUID.randomUUID() + ".jpg");
                        imageRef.putFile(uri)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        imageRef.getDownloadUrl().addOnCompleteListener(downloadUrlTask -> {
                                            if (downloadUrlTask.isSuccessful()) {
                                                Picasso.get().load(downloadUrlTask.getResult()).into(imageView);
                                                imageFilename = imageRef.getName();
                                            }
                                            customProgressDialog.dismiss();
                                        });
                                    }
                                    customProgressDialog.dismiss();
                                });

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                        customProgressDialog.dismiss();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = requireArguments();
        isNew = arguments.getBoolean("isNew");
        docId = arguments.getString("docId");

        assert isNew || docId != null;

        if (docId == null) {
            docId = db.collection("listings").document().getId();
        } else {
            fetchInitialData();
        }

        View view = inflater.inflate(R.layout.fragment_listing_add_edit, container, false);

        topAppBar = view.findViewById(R.id.topAppBar);
        toggleButtonType = view.findViewById(R.id.toggleButtonType);
        linearLayoutApartmentDetails = view.findViewById(R.id.linearLayoutApartmentDetails);
        linearLayoutBedspaceDetails = view.findViewById(R.id.linearLayoutBedspaceDetails);
        textFieldName = view.findViewById(R.id.textFieldName);
        textFieldContactPerson = view.findViewById(R.id.textFieldContactPerson);
        imageView = view.findViewById(R.id.imageView);
        buttonAddImage = view.findViewById(R.id.buttonAddImage);
        textFieldContactNumber = view.findViewById(R.id.textFieldContactNumber);
        textFieldAddress = view.findViewById(R.id.textFieldAddress);
        textFieldPrice = view.findViewById(R.id.textFieldPrice);
        toggleButtonBillsIncluded = view.findViewById(R.id.toggleButtonBillsIncluded);
        cardViewBillsIncluded = view.findViewById(R.id.cardViewBillsIncluded);
        chipGroupBillsIncluded = view.findViewById(R.id.chipGroupBillsIncluded);
        toggleButtonCurfew = view.findViewById(R.id.toggleButtonCurfew);
        buttonCurfewTime = view.findViewById(R.id.buttonCurfewTime);
        toggleButtonContract = view.findViewById(R.id.toggleButtonContract);
        textFieldContractYears = view.findViewById(R.id.textFieldContractYears);
        textFieldOtherDetails = view.findViewById(R.id.textFieldOtherDetails);

        textFieldBedrooms = view.findViewById(R.id.textFieldBedrooms);
        textFieldBathrooms = view.findViewById(R.id.textFieldBathrooms);
        textFieldCapacity = view.findViewById(R.id.textFieldCapacity);
        textFieldRoommateCount = view.findViewById(R.id.textFieldRoommateCount);
        textFieldBathroomShareCount = view.findViewById(R.id.textFieldBathroomShareCount);

        topAppBar.setTitle(isNew ? "New Property" : "Edit Property");
        topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        buttonCurfewTime.setText(curfewFromTime.format(timeFormatter) + " - " + curfewToTime.format(timeFormatter));

        toggleButtonType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            onTypeSelected(checkedId, isChecked);
        });

        buttonAddImage.setOnClickListener(v -> {
            customProgressDialog.show();
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        toggleButtonBillsIncluded.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            cardViewBillsIncluded.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        toggleButtonCurfew.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            buttonCurfewTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        toggleButtonContract.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            textFieldContractYears.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        buttonCurfewTime.setOnClickListener(v -> {
            fromPicker.show(getChildFragmentManager(), "curfew from time");
        });

        topAppBar.setOnMenuItemClickListener(this::handleAppBarMenuClick);

        return view;
    }

    private void fetchInitialData() {
        CustomProgressDialog initialProgressDialog = new CustomProgressDialog(requireContext(), "Loading...");
        initialProgressDialog.show();
        db.collection("listings").document(docId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ForRent forRent;
                if (task.getResult().contains("noOfBedrooms")) {
                    Apartment apartment = task.getResult().toObject(Apartment.class);
                    forRent = apartment;
                    toggleButtonType.check(R.id.buttonApartment);
                    textFieldBedrooms.getEditText().setText(String.valueOf(apartment.getNoOfBedrooms()));
                    textFieldBathrooms.getEditText().setText(String.valueOf(apartment.getNoOfBathrooms()));
                    textFieldCapacity.getEditText().setText(String.valueOf(apartment.getCapacity()));
                } else {
                    Bedspace bedspace = task.getResult().toObject(Bedspace.class);
                    forRent = bedspace;
                    toggleButtonType.check(R.id.buttonBedspace);
                    textFieldRoommateCount.getEditText().setText(String.valueOf(bedspace.getRoommateCount()));
                    textFieldBathroomShareCount.getEditText().setText(String.valueOf(bedspace.getBathroomShareCount()));
                }
                imageFilename = forRent.getImageFilename();
                storage.getReference().child(imageFilename).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Picasso.get().load(task.getResult()).into(imageView);
                        }
                    }
                });

                textFieldName.getEditText().setText(forRent.getName());
                textFieldContactPerson.getEditText().setText(forRent.getContactPerson());
                textFieldContactNumber.getEditText().setText(forRent.getContactNumber());
                textFieldAddress.getEditText().setText(forRent.getAddress());
                textFieldPrice.getEditText().setText(String.valueOf(forRent.getPrice()));
                textFieldOtherDetails.getEditText().setText(String.valueOf(forRent.getOtherDetails()));

                if (!forRent.getBillsIncluded().isEmpty()) {
                    toggleButtonBillsIncluded.check(R.id.buttonBillsIncluded);
                    for (String included : forRent.getBillsIncluded()) {
                        if (Objects.equals(included, "water")) {
                            chipGroupBillsIncluded.check(R.id.chipWater);
                        } else if (Objects.equals(included, "electricity")) {
                            chipGroupBillsIncluded.check(R.id.chipElectricity);
                        } else if (Objects.equals(included, "internet")) {
                            chipGroupBillsIncluded.check(R.id.chipInternet);
                        } else if (Objects.equals(included, "lpg")) {
                            chipGroupBillsIncluded.check(R.id.chipLpg);
                        }
                    }
                }
                if (forRent.getCurfew() != null && forRent.getCurfew().isEmpty()) {
                    toggleButtonCurfew.check(R.id.buttonCurfew);
                    buttonCurfewTime.setText(forRent.getCurfew());
                    String[] curfew = forRent.getCurfew().split("-", 2);
                    curfewFromTime = LocalTime.parse(curfew[0], timeFormatter);
                    curfewToTime = LocalTime.parse(curfew[1], timeFormatter);
                }
                if (forRent.getContract() > 0) {
                    toggleButtonContract.check(R.id.buttonContract);
                    textFieldContractYears.getEditText().setText(String.valueOf(forRent.getContract()));
                }
            }
            initialProgressDialog.dismiss();
        });
    }

    private boolean handleAppBarMenuClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.check) {
            saveData();
            return true;
        }
        return false;
    }

    private void saveData() {
        CustomProgressDialog progressDialog = new CustomProgressDialog(requireActivity(), "Saving...");
        progressDialog.show();

        // Get inputs from user
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String name = textFieldName.getEditText().getText().toString();
        String contactPerson = textFieldContactPerson.getEditText().getText().toString();
        String contactNumber = textFieldContactNumber.getEditText().getText().toString();
        float price = Float.parseFloat(textFieldPrice.getEditText().getText().toString());

        List<String> billsIncluded = new ArrayList<>();
        for (int chipId : chipGroupBillsIncluded.getCheckedChipIds()) {
            if (chipId == R.id.chipWater) {
                billsIncluded.add("water");
            } else if (chipId == R.id.chipElectricity) {
                billsIncluded.add("electricity");
            } else if (chipId == R.id.chipInternet) {
                billsIncluded.add("internet");
            } else if (chipId == R.id.chipLpg) {
                billsIncluded.add("lpg");
            }
        }

        String address = textFieldAddress.getEditText().getText().toString();
        String curfew = curfewFromTime.format(timeFormatter) + " - " + curfewToTime.format(timeFormatter);

        int contract = 0;
        if (toggleButtonContract.getCheckedButtonId() == R.id.buttonContract) {
            contract = Integer.parseInt(textFieldContractYears.getEditText().getText().toString());
        }

        double latitude = 13.785176;
        double longitude = 121.073863;
        String otherDetails = textFieldOtherDetails.getEditText().getText().toString();

        int checkedTypeId = toggleButtonType.getCheckedButtonId();

        Task<Void> setTask = null;

        if (checkedTypeId == R.id.buttonApartment) {
            int noOfBedrooms = Integer.parseInt(textFieldBedrooms.getEditText().getText().toString());
            int noOfBathrooms = Integer.parseInt(textFieldBathrooms.getEditText().getText().toString());
            int capacity = Integer.parseInt(textFieldCapacity.getEditText().getText().toString());

            Apartment apartment = new Apartment(
                    uid,
                    imageFilename,
                    name,
                    contactPerson,
                    contactNumber,
                    price,
                    billsIncluded,
                    address,
                    curfew,
                    contract,
                    latitude,
                    longitude,
                    otherDetails,
                    noOfBedrooms,
                    noOfBathrooms,
                    capacity
            );
            setTask = db.collection("listings").document(docId).set(apartment);
        } else if (checkedTypeId == R.id.buttonBedspace) {
            int roommateCount = Integer.parseInt(textFieldRoommateCount.getEditText().getText().toString());
            int bathroomShareCount = Integer.parseInt(textFieldBathroomShareCount.getEditText().getText().toString());
            Bedspace bedspace = new Bedspace(
                    uid,
                    imageFilename,
                    name,
                    contactPerson,
                    contactNumber,
                    price,
                    billsIncluded,
                    address,
                    curfew,
                    contract,
                    latitude,
                    longitude,
                    otherDetails,
                    roommateCount,
                    bathroomShareCount
            );
            setTask = db.collection("listings").document(docId).set(bedspace);
        } else {
            progressDialog.dismiss();
        }

        if (setTask != null) {
            setTask.addOnSuccessListener(unused -> {
                Log.i(TAG, "handleAppBarMenuClick: Success");
                Navigation.findNavController(requireView()).popBackStack(R.id.myPropertiesFragment, false);
                progressDialog.dismiss();
            }).addOnFailureListener(e -> {
                Log.i(TAG, "handleAppBarMenuClick: " + e);
                progressDialog.dismiss();
            });
        }
    }

    private void onTypeSelected(int checkedId, boolean isChecked) {
        if (checkedId == R.id.buttonApartment) {
            linearLayoutApartmentDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            linearLayoutBedspaceDetails.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        } else if (checkedId == R.id.buttonBedspace) {
            linearLayoutApartmentDetails.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            linearLayoutBedspaceDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
    }

    private static final String TAG = "ListingAddEditFragment";
}