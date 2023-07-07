package dev.anonymous.eilaji.doctor.ui.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import dev.anonymous.eilaji.doctor.utils.constants.Constant;
import dev.anonymous.eilaji.doctor.databinding.FragmentEditInformationBinding;

public class EditInformationFragment extends Fragment {
    private static final String TAG = "EditInformationFragment";

    FragmentEditInformationBinding binding;
    //    private EditInformationViewModel mViewModel;
    private CollectionReference pharmaciesCollection;

    private String userUid,
            fullName,
            urlImage,
            address;

    private SharedPreferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditInformationBinding.inflate(inflater, container, false);

        pharmaciesCollection = FirebaseFirestore.getInstance()
                .collection(Constant.PHARMACIES_COLLECTION);
        preferences = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userUid = user.getUid();
            fullName = user.getDisplayName();
            urlImage = String.valueOf(user.getPhotoUrl());
            address = preferences.getString("address", null);

            binding.edPharmacyName.setText(fullName);
            binding.edAddress.setText(address);

            binding.buSaveEdits.setOnClickListener(v -> {

            });
        }
        return binding.getRoot();
    }

    void getAddress(String userUid) {
        pharmaciesCollection.document(userUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> data = task.getResult().getData();
                        if (data != null) {
                            String address = String.valueOf(data.get("address"));
                            preferences.edit().putString("address", address).apply();
                        }
                    } else {
                        if (task.getException() != null) {
                            Log.e(TAG, "getAddress:task.getException(): "
                                    + task.getException().getMessage());
                        }
                    }
                }).addOnFailureListener(e ->
                        Log.e(TAG, "getAddress:addOnFailureListener: " + e.getMessage()));
    }
}