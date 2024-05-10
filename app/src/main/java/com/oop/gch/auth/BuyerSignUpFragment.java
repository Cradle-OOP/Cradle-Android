package com.oop.gch.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
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
 * Use the {@link BuyerSignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuyerSignUpFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPref;

    // Declare UI elements
    private TextInputLayout menu;
    private Button buttonCreateAccount;
    private TextInputLayout textFieldEmail;
    private TextInputLayout textFieldPassword;
    private TextInputLayout textFieldName;
    private TextInputLayout textFieldAge;
    private TextInputLayout textFieldPhone;
    private TextInputLayout textFieldGuardianName;
    private TextInputLayout textFieldGuardianPhone;
    private TextInputLayout textFieldYearLevel;
    private TextInputLayout textFieldProgram;


    public BuyerSignUpFragment() {
        // Required empty public constructor
    }

    public static BuyerSignUpFragment newInstance(String param1, String param2) {
        return new BuyerSignUpFragment();
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
        View view = inflater.inflate(R.layout.fragment_buyer_sign_up, container, false);

        menu = view.findViewById(R.id.menu);
        buttonCreateAccount = view.findViewById(R.id.buttonCreateAccount);
        textFieldEmail = view.findViewById(R.id.textFieldEmail);
        textFieldPassword = view.findViewById(R.id.textFieldPassword);
        textFieldName = view.findViewById(R.id.textFieldName);
        textFieldAge = view.findViewById(R.id.textFieldAge);
        textFieldPhone = view.findViewById(R.id.textFieldPhone);
        textFieldGuardianName = view.findViewById(R.id.textFieldGuardianName);
        textFieldGuardianPhone = view.findViewById(R.id.textFieldGuardianPhone);
        textFieldYearLevel = view.findViewById(R.id.textFieldYearLevel);
        textFieldProgram = view.findViewById(R.id.textFieldProgram);


        // Set options for sex field
        String items[] = {"Male", "Female"};
        ((MaterialAutoCompleteTextView) menu.getEditText()).setSimpleItems(items);

        buttonCreateAccount.setOnClickListener(this::onCreateAccountClicked);

        return view;
    }

    private void onCreateAccountClicked(View view) {
        String email = textFieldEmail.getEditText().getText().toString();
        String password = textFieldPassword.getEditText().getText().toString();

        if (email.trim().isEmpty() || password.trim().isEmpty()) return;

        ProgressDialog progressDialog = new CustomProgressDialog(getActivity(), "Creating account...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user, progressDialog);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(requireActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser user, ProgressDialog progressDialog) {
        String name = textFieldName.getEditText().getText().toString();
        int age =Integer.parseInt(textFieldAge.getEditText().getText().toString());
        String phone = textFieldPhone.getEditText().getText().toString();
        String Guardian_name = textFieldGuardianName.getEditText().getText().toString();
        String Guardian_phone = textFieldGuardianPhone.getEditText().getText().toString();
        int Year_Level =Integer.parseInt(textFieldYearLevel.getEditText().getText().toString());
        String program = textFieldProgram.getEditText().getText().toString();

        String uid = user.getUid();

        String accountType = "BUYER";

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("accountType", accountType);
        userInfo.put("fullName", name);
        userInfo.put("age", age);
        userInfo.put("sex", "Male");
        userInfo.put("phone", phone);
        userInfo.put("guardianName", Guardian_name);
        userInfo.put("guardianPhone", Guardian_phone);
        userInfo.put("type", "9012345678");
        userInfo.put("yearLevel", Year_Level);
        userInfo.put("program", program);

        db.collection("users")
                .document(uid)
                .set(userInfo)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + uid);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("accountType", accountType);
                    editor.putString("fullName", (String) userInfo.get("fullName"));
                    editor.apply();
                    progressDialog.dismiss();
                    Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }

    private static final String TAG = "BuyerSignUpFragment";
}