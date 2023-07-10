package dev.anonymous.eilaji.doctor.ui.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
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

import dev.anonymous.eilaji.doctor.databinding.FragmentEditInformationBinding;
import dev.anonymous.eilaji.doctor.utils.Utils;
import dev.anonymous.eilaji.doctor.utils.constants.Constant;

public class EditInformationFragment extends Fragment {
    private static final String TAG = "EditInformationFragment";

    FragmentEditInformationBinding binding;

    //    private EditInformationViewModel mViewModel;
    private CollectionReference pharmaciesCollection;

    private String userUid,
            pharmacyName,
            pharmacyImageUrl,
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

            pharmacyImageUrl = preferences.getString("pharmacy_image_url", null);
            pharmacyName = preferences.getString("pharmacy_name", null);
            address = preferences.getString("address", null);

            Utils.loadImage(getActivity(), pharmacyImageUrl).into(binding.ivPharmacy);
            binding.edPharmacyName.setText(pharmacyName);
            binding.edAddress.setText(address);

            binding.buSaveEdits.setOnClickListener(v -> {

            });
        }
        return binding.getRoot();
    }
}