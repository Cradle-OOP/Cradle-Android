package com.oop.gch.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.oop.gch.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PickAccountTypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PickAccountTypeFragment extends Fragment {

    public PickAccountTypeFragment() {
        // Required empty public constructor
    }

    public static PickAccountTypeFragment newInstance() {
        return new PickAccountTypeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_account_type, container, false);

        Button buttonBuyer = view.findViewById(R.id.buttonBuyer);
        Button buttonSeller = view.findViewById(R.id.buttonSeller);

        buttonBuyer.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_pickAccountTypeFragment_to_buyerSignInFragment));
        buttonSeller.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_pickAccountTypeFragment_to_sellerSignInFragment));

        return view;
    }
}