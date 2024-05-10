package com.oop.gch;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.oop.gch.components.CustomProgressDialog;
import com.oop.gch.components.DividerItemDecoration;
import com.oop.gch.list.ListingAdapter;
import com.oop.gch.model.Filters;
import com.oop.gch.model.ForRent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements ListingAdapter.OnItemClickListener {

    ListenerRegistration registration;

    DrawerLayout drawerLayout;
    MaterialToolbar topAppBar;
    NavigationView navigationView;
    RecyclerView recyclerView;

    Filters filters = new Filters(null, null, null);
    Filters filtersTemp = filters.clone();

    List<ForRent> listings = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private SharedPreferences sharedPref;
    String accountType;

    private SideSheetDialog sideSheetDialog;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateSnapshotListener();

        accountType = sharedPref.getString("accountType", null);

        View view = getView();
        if (view != null) {
            NavigationView navigationView = view.findViewById(R.id.navigationView);
            updateNavigationViewMenu(navigationView);
        }
    }

    private void updateSnapshotListener() {
        updateSnapshotListener(false);
    }

    private void updateSnapshotListener(boolean showProgress) {
        CustomProgressDialog progressDialog = new CustomProgressDialog(requireContext(), "Please wait...");
        if (showProgress) {
            progressDialog.show();
        }

        if (registration != null) {
            registration.remove();
        }
        Log.i(TAG, "updateSnapshotListener: " + filtersTemp);

        CollectionReference ref = db.collection("listings");
        Query query = buildQuery(ref);

        registration = query
                .addSnapshotListener((value, error) -> {
                    progressDialog.dismiss();
                    if (error != null)
                        return;
                    List<ForRent> values = Utils.updateListings(recyclerView, value);
                    for (int i = 0; i < values.size(); i++) {
                        int finalI = i;
                        storage.getReference().child(values.get(i).getImageFilename()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    ForRent temp = values.get(finalI);
                                    temp.imageDownloadUrl = task.getResult();
                                    values.set(values.indexOf(temp), temp);
                                    recyclerView.getAdapter().notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });
    }

    private Query buildQuery(CollectionReference ref) {
        String type = filtersTemp.getType();
        Boolean contract = filtersTemp.getContract();
        Boolean curfew = filtersTemp.getCurfew();

        Query query = null;
        if (type != null) {
            if (type.equals("APARTMENT"))
                query = ref.whereGreaterThan("noOfBedrooms", -1);
            else
                query = ref.whereGreaterThan("roommateCount", -1);
        }
        if (contract != null) {
            if (contract)
                query = (query != null ? query : ref).whereGreaterThan("contract", 0);
            else
                query = (query != null ? query : ref).whereEqualTo("contract", 0);
        }
        if (curfew != null) {
            if (curfew)
                query = (query != null ? query : ref).whereNotEqualTo("curfew", "");
            else
                query = (query != null ? query : ref).whereEqualTo("curfew", "");
        }
        query = (query != null ? query : ref).orderBy("dateCreated", Query.Direction.DESCENDING);
        return query;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Get UI elements from the XML layout
        drawerLayout = view.findViewById(R.id.drawerLayout);
        topAppBar = view.findViewById(R.id.topAppBar);
        navigationView = view.findViewById(R.id.navigationView);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.addItemDecoration(new DividerItemDecoration(Utils.convertDpToPixel(16, requireContext())));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new ListingAdapter(listings, this));

        // Needed to create the collapsible side sheet
        sideSheetDialog = new SideSheetDialog(requireContext());
        sideSheetDialog.setContentView(R.layout.side_sheet_content_layout);
        setupSideSheetDialog();

        updateNavigationViewMenu(navigationView);

        // Listener for the menu button. Open the drawer when clicked
        topAppBar.setNavigationOnClickListener(v -> drawerLayout.open());

        // Listener for the filter button which opens the side sheet
        topAppBar.setOnMenuItemClickListener(this::handleMenuClick);

        // Listener for the drawer menu buttons.
        navigationView.setNavigationItemSelectedListener(this::handleNavigationItemClick);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        registration.remove();
    }

    @Override
    public void onItemClick(ForRent item) {
        Bundle bundle = new Bundle();
        bundle.putString("id", item.getDocId());
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_listingDetailFragment, bundle);
    }

    @Override
    public void onItemLongClick(ForRent item) {

    }

    private void setupSideSheetDialog() {
        MaterialCheckBox checkBoxApartment = sideSheetDialog.findViewById(R.id.checkBoxApartment);
        MaterialCheckBox checkBoxBedspace = sideSheetDialog.findViewById(R.id.checkBoxBedspace);

        MaterialCheckBox checkBoxWithCurfew = sideSheetDialog.findViewById(R.id.checkBoxWithCurfew);
        MaterialCheckBox checkBoxNoCurfew = sideSheetDialog.findViewById(R.id.checkBoxNoCurfew);

        MaterialCheckBox checkBoxWithContract = sideSheetDialog.findViewById(R.id.checkBoxWithContract);
        MaterialCheckBox checkBoxNoContract = sideSheetDialog.findViewById(R.id.checkBoxNoContract);

        assert checkBoxApartment != null && checkBoxBedspace != null && checkBoxWithCurfew != null && checkBoxNoCurfew != null && checkBoxWithContract != null && checkBoxNoContract != null;

        checkBoxApartment.setOnClickListener(v -> handleTypeFilterUpdate(checkBoxApartment, checkBoxBedspace));
        checkBoxBedspace.setOnClickListener(v -> handleTypeFilterUpdate(checkBoxApartment, checkBoxBedspace));

        checkBoxWithCurfew.setOnClickListener(v -> handleCurfewFilterUpdate(checkBoxWithCurfew, checkBoxNoCurfew));
        checkBoxNoCurfew.setOnClickListener(v -> handleCurfewFilterUpdate(checkBoxWithCurfew, checkBoxNoCurfew));

        checkBoxWithContract.setOnClickListener(v -> handleContractFilterUpdate(checkBoxWithContract, checkBoxNoContract));
        checkBoxNoContract.setOnClickListener(v -> handleContractFilterUpdate(checkBoxWithContract, checkBoxNoContract));

        sideSheetDialog.findViewById(R.id.buttonSave).setOnClickListener(v -> {
            filters = filtersTemp.clone();
            sideSheetDialog.dismiss();
            updateSnapshotListener(true);
        });
        sideSheetDialog.findViewById(R.id.buttonClear).setOnClickListener(v -> {
            filters = new Filters(null, null, null);
            sideSheetDialog.dismiss();
            updateSnapshotListener(true);
        });
    }

    private void handleContractFilterUpdate(MaterialCheckBox checkBoxWithContract, MaterialCheckBox checkBoxNoContract) {
        boolean withContract = checkBoxWithContract.isChecked();
        boolean noContract = checkBoxNoContract.isChecked();

        Boolean contract;
        if (withContract == noContract) {
            contract = null;
        } else {
            contract = withContract;
        }

        filtersTemp.setContract(contract);
    }

    private void handleCurfewFilterUpdate(MaterialCheckBox checkBoxWithCurfew, MaterialCheckBox checkBoxNoCurfew) {
        boolean withCurfew = checkBoxWithCurfew.isChecked();
        boolean noCurfew = checkBoxNoCurfew.isChecked();

        Boolean curfew;
        if (withCurfew == noCurfew) {
            curfew = null;
        } else {
            curfew = withCurfew;
        }

        filtersTemp.setCurfew(curfew);
    }

    private void handleTypeFilterUpdate(MaterialCheckBox checkBoxApartment, MaterialCheckBox checkBoxBedspace) {
        boolean apartment = checkBoxApartment.isChecked();
        boolean bedspace = checkBoxBedspace.isChecked();

        String type;
        if (apartment == bedspace) {
            type = null;
        } else {
            type = apartment ? "APARTMENT" : "BEDSPACE";
        }

        filtersTemp.setType(type);
    }

    /**
     * Updates drawer content. Sets options inside the navigation drawer
     * (Home, Login, etc.) depending on user authentication state.
     *
     * @param navigationView NavigationView to use
     */
    private void updateNavigationViewMenu(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView textViewDrawerTitle = headerView.findViewById(R.id.textViewDrawerTitle);
        TextView textViewDrawerSubtitle = headerView.findViewById(R.id.textViewDrawerSubtitle);

        textViewDrawerTitle.setText(sharedPref.getString("fullName", "GCH Dorm"));
        textViewDrawerSubtitle.setText(sharedPref.getString("accountType", "Not signed in"));

        navigationView.getMenu().clear();
        if (accountType == null) {
            navigationView.inflateMenu(R.menu.default_navigation_drawer);
            return;
        }
        switch (accountType) {
            case "SELLER": {
                navigationView.inflateMenu(R.menu.seller_navigation_drawer);
                break;
            }
            case "BUYER": {
                navigationView.inflateMenu(R.menu.buyer_navigation_drawer);
                break;
            }
            default: {
                navigationView.inflateMenu(R.menu.default_navigation_drawer);
                break;
            }
        }
    }

    private boolean handleNavigationItemClick(MenuItem menuItem) {
        // Check item id to determine which button is clicked
        if (menuItem.getItemId() == R.id.homeItem) {
            // Nothing yet, just shows text to user
            Toast.makeText(getActivity(), "Home clicked", Toast.LENGTH_SHORT).show();
        } else if (menuItem.getItemId() == R.id.propertiesItem) {
            // Navigate to MyPropertiesFragment
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_myPropertiesFragment);
        } else if (menuItem.getItemId() == R.id.loginItem) {
            // Navigate to PickAccountTypeFragment
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_pickAccountTypeFragment);
        } else if (menuItem.getItemId() == R.id.logoutItem) {
            // Set account type to null
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("accountType", null);
            editor.putString("fullName", null);
            editor.apply();
            accountType = null;

            // Logout user
            mAuth.signOut();

            // Update drawer menu
            updateNavigationViewMenu(navigationView);
        }
        drawerLayout.close();
        return true;
    }

    private boolean handleMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.filters) {
            filtersTemp = filters.clone();
            setupSideSheetFilters(filters);
            sideSheetDialog.show();
            return true;
        }
        return false;
    }

    private void setupSideSheetFilters(Filters filters) {
        MaterialCheckBox checkBoxApartment = sideSheetDialog.findViewById(R.id.checkBoxApartment);
        MaterialCheckBox checkBoxBedspace = sideSheetDialog.findViewById(R.id.checkBoxBedspace);

        MaterialCheckBox checkBoxWithCurfew = sideSheetDialog.findViewById(R.id.checkBoxWithCurfew);
        MaterialCheckBox checkBoxNoCurfew = sideSheetDialog.findViewById(R.id.checkBoxNoCurfew);

        MaterialCheckBox checkBoxWithContract = sideSheetDialog.findViewById(R.id.checkBoxWithContract);
        MaterialCheckBox checkBoxNoContract = sideSheetDialog.findViewById(R.id.checkBoxNoContract);

        assert checkBoxApartment != null && checkBoxBedspace != null && checkBoxWithCurfew != null && checkBoxNoCurfew != null && checkBoxWithContract != null && checkBoxNoContract != null;

        checkBoxApartment.setChecked(Objects.equals(filters.getType(), "APARTMENT"));
        checkBoxBedspace.setChecked(Objects.equals(filters.getType(), "BEDSPACE"));
        checkBoxWithCurfew.setChecked(Boolean.TRUE.equals(filters.getCurfew()));
        checkBoxNoCurfew.setChecked(Boolean.FALSE.equals(filters.getCurfew()));
        checkBoxWithContract.setChecked(Boolean.TRUE.equals(filters.getContract()));
        checkBoxNoContract.setChecked(Boolean.FALSE.equals(filters.getContract()));
    }

    private static final String TAG = "HomeFragment";
}