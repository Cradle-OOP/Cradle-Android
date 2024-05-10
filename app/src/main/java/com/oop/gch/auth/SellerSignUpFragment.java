package com.oop.gch.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oop.gch.components.CustomProgressDialog;
import com.oop.gch.R;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SellerSignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SellerSignUpFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPref;

    // Declare UI elements
    private TextInputLayout textFieldName;
    private TextInputLayout textFieldEmail;
    private TextInputLayout textFieldPassword;
    private Button buttonSignUp;

    public static SellerSignUpFragment newInstance(String param1, String param2) {
        return new SellerSignUpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_sign_up, container, false);

        textFieldName = view.findViewById(R.id.textFieldName);
        textFieldEmail = view.findViewById(R.id.textFieldEmail);
        textFieldPassword = view.findViewById(R.id.textFieldPassword);

        buttonSignUp = view.findViewById(R.id.buttonSignUp);

        buttonSignUp.setOnClickListener(this::onSignUpClicked);

        return view;
    }

    private void onSignUpClicked(View view) {
        String email = textFieldEmail.getEditText().getText().toString();
        String password = textFieldPassword.getEditText().getText().toString();
        if (email.trim().isEmpty() || password.trim().isEmpty()) return;

        ProgressDialog progressDialog = new CustomProgressDialog(getActivity(), "Creating account...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user, progressDialog);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(requireActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser user, ProgressDialog progressDialog) {
        String uid = user.getUid();

        String name = textFieldName.getEditText().getText().toString();

        String accountType = "SELLER";

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("accountType", accountType);
        userInfo.put("fullName", name);

        db.collection("users")
                .document(uid)
                .set(userInfo)
                .addOnSuccessListener(unused -> {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("accountType", accountType);
                    editor.putString("fullName", (String) userInfo.get("fullName"));
                    editor.apply();
                    progressDialog.dismiss();
                    Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }
}