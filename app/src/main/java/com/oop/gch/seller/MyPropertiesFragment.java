package com.oop.gch.seller;

import static android.app.ProgressDialog.show;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.oop.gch.components.DividerItemDecoration;
import com.oop.gch.list.ListingAdapter;
import com.oop.gch.R;
import com.oop.gch.Utils;
import com.oop.gch.model.ForRent;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPropertiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPropertiesFragment extends Fragment implements ListingAdapter.OnItemClickListener {

    ListenerRegistration registration;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    MaterialToolbar topAppBar;
    RecyclerView recyclerView;

    List<ForRent> listings = new ArrayList<>();

    public MyPropertiesFragment() {
        // Required empty public constructor
    }

    public static MyPropertiesFragment newInstance() {
        return new MyPropertiesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        registration.remove();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_properties, container, false);

        topAppBar = view.findViewById(R.id.topAppBar);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.addItemDecoration(new DividerItemDecoration(Utils.convertDpToPixel(16, requireContext())));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new ListingAdapter(listings, this));

        topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isNew", true);
                Navigation.findNavController(view).navigate(R.id.action_myPropertiesFragment_to_listingEditFragment, bundle);
                return true;
            }
            return false;
        });

        return view;
    }

    private void updateListener() {
        registration = db.collection("listings")
                .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    Log.i(TAG, "updateListener: " + value.getDocuments());
                    List<ForRent> values = Utils.updateListings(recyclerView, value);
                    for (int i = 0; i < values.size(); i++) {
                        int finalI = i;
                        loadImage(values, finalI);
                    }
                });
    }

    private void loadImage(List<ForRent> values, int finalI) {
        storage.getReference().child(values.get(finalI).getImageFilename()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
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

    private static final String TAG = "MyPropertiesFragment";

    @Override
    public void onItemClick(ForRent item) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isNew", false);
        bundle.putString("docId", item.getDocId());
        Navigation.findNavController(requireView()).navigate(R.id.action_myPropertiesFragment_to_listingEditFragment, bundle);
    }

    @Override
    public void onItemLongClick(ForRent item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete listing?")
                .setMessage("Are you sure you want to delete " + item.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("listings").document(item.getDocId()).delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.i(TAG, "onItemLongClick: " + task.getException());
                                    Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                                }
                            });

                })
                .show();
    }
}