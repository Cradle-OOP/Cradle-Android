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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BuyerSignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuyerSignInFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPref;

    private TextInputLayout textFieldEmail;
    private TextInputLayout textFieldPassword;
    private Button buttonLogin;
    private Button buttonSignUp;

    public BuyerSignInFragment() {
        // Required empty public constructor
    }

    public static BuyerSignInFragment newInstance(String param1, String param2) {
        return new BuyerSignInFragment();
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
        View view = inflater.inflate(R.layout.fragment_buyer_sign_in, container, false);

        textFieldEmail = view.findViewById(R.id.textFieldEmail);
        textFieldPassword = view.findViewById(R.id.textFieldPassword);

        buttonLogin = view.findViewById(R.id.buttonLogin);
        buttonSignUp = view.findViewById(R.id.buttonSignUp);

        buttonLogin.setOnClickListener(this::onLoginClicked);
        buttonSignUp.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_buyerSignInFragment_to_buyerSignUpFragment));

        return view;
    }

    private void onLoginClicked(View view) {
        String email = textFieldEmail.getEditText().getText().toString();
        String password = textFieldPassword.getEditText().getText().toString();
        if (email.trim().isEmpty() || password.trim().isEmpty()) return;

        ProgressDialog progressDialog = new CustomProgressDialog(getActivity(), "Logging in...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        fetchUserData(user, progressDialog);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }

    private void fetchUserData(FirebaseUser user, ProgressDialog progressDialog) {
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("accountType", documentSnapshot.get("accountType", String.class));
                    editor.putString("fullName", documentSnapshot.get("fullName", String.class));
                    editor.apply();
                    progressDialog.dismiss();
                    Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }
}