package dev.anonymous.eilaji.doctor.ui.fragments.guard;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import dev.anonymous.eilaji.doctor.R;
import dev.anonymous.eilaji.doctor.databinding.FragmentRegisterBinding;
import dev.anonymous.eilaji.doctor.firebase.FirebaseController;
import dev.anonymous.eilaji.doctor.ui.activities.BaseActivity;
import dev.anonymous.eilaji.doctor.utils.AppController;
import dev.anonymous.eilaji.doctor.utils.Utils;
import dev.anonymous.eilaji.doctor.utils.constants.Constant;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";
    private FragmentRegisterBinding binding;
    private Uri pharmacyImageUri;

    private StorageReference messagesImagesRef;
    private CollectionReference pharmaciesCollection;

    private SharedPreferences preferences;

    private String pharmacyName, phone, address, email, password, token;

    private String userUid;

    private double lat = 31.448934162124537, lng = 34.39408320427861;
    // hesham
    private final FirebaseController firebaseController = FirebaseController.getInstance();
    private final FirebaseAuth firebaseAuth = firebaseController.getAuth();
    private final AppController appController = AppController.getInstance();
    private final FusedLocationProviderClient fusedLocationClient;

    {
        assert appController != null;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(appController);
    }

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messagesImagesRef = FirebaseStorage.getInstance().getReference(Constant.PHARMACIES_IMAGES_DOCUMENT);

        pharmaciesCollection = FirebaseFirestore.getInstance().collection(Constant.PHARMACIES_COLLECTION);

        preferences = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        // setup the runtime permissions launchers
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            // Check if all permissions are granted
            boolean allGranted = true;
            for (boolean isGranted : permissions.values()) {
                if (!isGranted) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // All permissions granted, you can proceed with sending notifications
                Toast.makeText(requireContext(), "Great! Now you are all set to use The Reminder", Toast.LENGTH_LONG).show();
            } else {
                // Permission denied, handle accordingly
                // At least one permission denied, handle accordingly (e.g., show a message or disable certain features)
                Toast.makeText(requireContext(), "Permission denied. Cannot create reminder.", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners() {
        binding.buChooseImage.setOnClickListener(v -> pickImage());

        binding.buCreateAnAccount.setOnClickListener(v -> {
            collectUserInputs();
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)&& !TextUtils.isEmpty(address)) {
                binding.buCreateAnAccount.setEnabled(false);
                registerPharmacy(email, password);
            } else {
                Utils.getInstance().showSnackBar(binding.getRoot(), "Enter All Fields!");
            }
        });

        binding.buLoginScreen.setOnClickListener(view -> {
            var navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.navigation_login);
        });

        binding.edLocation.setOnTouchListener((view, motionEvent) -> {
            getUserLocationIntoEdLocation();
            return false;
        });

    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void getUserLocationIntoEdLocation() {
        if (arePermissionsGranted()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // TODO These are REAL .....
                    lat = latitude;
                    lng = longitude;

                    LatLng currentLatLng = new LatLng(latitude, longitude);
                    binding.edLocation.setText(currentLatLng.toString());
                    binding.edLocation.setEnabled(false);
                } else {
                    binding.edLocation.setText("Not Found....!");
                }
            }).addOnFailureListener(e -> System.out.println("addOnFailureListener " + e.getMessage()));
        } else {
            // Handle the case when location permission is not granted
            requestPermissions();
        }
    }

    private void requestPermissions() {
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
    }

    public boolean arePermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            assert appController != null;
            if (ContextCompat.checkSelfPermission(appController, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void pickImage() {
        PickVisualMediaRequest mediaRequest = new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build();
        pickMedia.launch(mediaRequest);
    }

    private void collectUserInputs() {
        pharmacyName = Objects.requireNonNull(binding.edPharmacyName.getText()).toString().trim();
        phone = Objects.requireNonNull(binding.edPhone.getText()).toString().trim();
        address = Objects.requireNonNull(binding.edAddress.getText()).toString().trim();
        email = Objects.requireNonNull(binding.edEmail.getText()).toString().trim();
        password = Objects.requireNonNull(binding.edPassword.getText()).toString().trim();
    }

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        if (uri != null) {
            pharmacyImageUri = uri;
            binding.imageHint.setVisibility(View.INVISIBLE);
            binding.ivPharmacy.setImageURI(uri);
        }
    });

    private void registerPharmacy(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();
                userUid = Objects.requireNonNull(firebaseUser).getUid();

                Toast.makeText(getActivity(), "register successful", Toast.LENGTH_SHORT).show();
                uploadPharmacyImage(pharmacyImageUri);
            } else {
                if (task.getException() != null) {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadPharmacyImage(Uri pharmacyImage) {
        final String imageName = userUid + "::" + System.currentTimeMillis();
        StorageReference pdfRef = messagesImagesRef.child(imageName + ".jpg");

        pdfRef.putFile(pharmacyImage).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            System.out.println(progress);
        }).continueWithTask(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                throw task.getException();
            }
            return pdfRef.getDownloadUrl();
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getActivity(), "تم رفع الصورة بنجاح", Toast.LENGTH_SHORT).show();
            updateUserProfile(pharmacyName, taskSnapshot);
        }).addOnFailureListener(exception -> Log.e(TAG, "uploadPharmacyImage: " + exception.getMessage()));
    }


    private void updateUserProfile(String fullName, Uri imageUrl) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(fullName).setPhotoUri(imageUrl).build();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "update Profile Successful", Toast.LENGTH_SHORT).show();

                    SharedPreferences preferences = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE);
                    token = preferences.getString("token", null);
                    if (token == null) {
                        Utils.getToken(getActivity(), newToken -> {
                            token = newToken;
                            createPharmacy(userUid, imageUrl);
                        });
                    } else {
                        createPharmacy(userUid, imageUrl);
                    }
                }
            });
        }
    }

    private void createPharmacy(String userUid, Uri pharmacyImageUrl) {
        HashMap<String, Object> pharmacyData = new HashMap<>();
        pharmacyData.put("uid", userUid);
        pharmacyData.put("pharmacy_name", pharmacyName);
        pharmacyData.put("pharmacy_image_url", String.valueOf(pharmacyImageUrl));
        pharmacyData.put("phone", phone);
        pharmacyData.put("address", address);
        pharmacyData.put("lat", lat);
        pharmacyData.put("lng", lng);
        pharmacyData.put("token", token);
        pharmacyData.put("timestamp", FieldValue.serverTimestamp());

        pharmaciesCollection.document(userUid).set(pharmacyData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                preferences.edit().putString("address", address).apply();
                //TODO : Replace
//                        ChatSharedPreferences.getInstance().putAddress(address);

                Toast.makeText(getActivity(), "create pharmacy successful", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), BaseActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        }).addOnFailureListener(e -> Log.e(TAG, "createPharmacy: " + e.getMessage()));
    }
}