package dev.anonymous.eilaji.doctor.ui.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dev.anonymous.eilaji.doctor.databinding.FragmentEditInformationBinding;
import dev.anonymous.eilaji.doctor.firebase.FirebaseController;
import dev.anonymous.eilaji.doctor.utils.Utils;
import dev.anonymous.eilaji.doctor.utils.constants.Constant;

public class EditInformationFragment extends Fragment {
    private FragmentEditInformationBinding binding;

    //    private EditInformationViewModel mViewModel;
    private CollectionReference pharmaciesCollection;
    private Uri pharmacyImageUri;
    private SharedPreferences preferences;
    private FirebaseUser user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pharmaciesCollection = FirebaseFirestore.getInstance()
                .collection(Constant.PHARMACIES_COLLECTION);
        preferences = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE);
        // user
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditInformationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        if (user != null) {

            String pharmacyImageUrl = preferences.getString("pharmacy_image_url", null);
            String pharmacyName = preferences.getString("pharmacy_name", null);
            String address = preferences.getString("address", null);


            Utils.loadImage(getActivity(), pharmacyImageUrl).into(binding.ivPharmacy);
            binding.imageHint.setVisibility(View.INVISIBLE);
            binding.edPharmacyName.setText(pharmacyName);
            binding.edAddress.setText(address);


        }
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickVisualMediaRequest = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    pharmacyImageUri = uri;
                    binding.imageHint.setVisibility(View.INVISIBLE);
                    binding.ivPharmacy.setImageURI(uri);
                }
            });

    private void imagePickingSettings() {
        PickVisualMediaRequest mediaRequest = new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build();
        pickVisualMediaRequest.launch(mediaRequest);
    }

    private void setupListeners() {
        binding.ivPharmacy.setOnClickListener(view -> imagePickingSettings());

        binding.buSaveEdits.setOnClickListener(v -> {
            // get the image Uri
            /*pharmacyImageUri*/
            String name = Objects.requireNonNull(binding.edPharmacyName.getText()).toString();
            String address = Objects.requireNonNull(binding.edAddress.getText()).toString();
            if (!address.isEmpty() && !name.isEmpty() && pharmacyImageUri != null) {
                Map<String, Object> pharmacy = new HashMap<>();
                pharmacy.put("pharmacy_image_url", pharmacyImageUri);
                pharmacy.put("pharmacy_name", name);
                pharmacy.put("address", address);
                var uid = FirebaseController
                        .getInstance().getCurrentUser().getUid();
                pharmaciesCollection
                        .document(uid)
                        .set(pharmacy)
                        .addOnSuccessListener(unused -> {
                            Utils.getInstance()
                                        .showSnackBar(binding.getRoot()
                                                , "Your Changes Got Submitted");
                                    preferences.edit()
                                            .putString("pharmacy_image_url"
                                                    , pharmacyImageUri.toString())
                                            .putString("pharmacy_name"
                                                    , name)
                                            .putString("address"
                                                    , address)
                                            .apply();

                        }

                        );
            } else {
                Utils.getInstance().showSnackBar(binding.getRoot(), "Change Any Field to save ..");
            }
        });
    }

}