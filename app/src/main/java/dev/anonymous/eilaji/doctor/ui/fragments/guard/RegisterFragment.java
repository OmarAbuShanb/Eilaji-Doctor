package dev.anonymous.eilaji.doctor.ui.fragments.guard;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

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

import dev.anonymous.eilaji.doctor.utils.constants.Constant;
import dev.anonymous.eilaji.doctor.utils.Utils;
import dev.anonymous.eilaji.doctor.databinding.FragmentRegisterBinding;
import dev.anonymous.eilaji.doctor.ui.activities.BaseActivity;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";

    FragmentRegisterBinding binding;
    //    private RegisterViewModel mViewModel;
    Uri pharmacyImageUri;

    private FirebaseAuth auth;
    private StorageReference messagesImagesRef;
    private CollectionReference pharmaciesCollection;

    private SharedPreferences preferences;

    private String pharmacyName,
            phone,
            address,
            email,
            password,
            token;

    private String userUid;

    double lat = 31.448934162124537,
            lng = 34.39408320427861;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        messagesImagesRef = FirebaseStorage.getInstance()
                .getReference(Constant.PHARMACIES_IMAGES_DOCUMENT);

        pharmaciesCollection = FirebaseFirestore.getInstance()
                .collection(Constant.PHARMACIES_COLLECTION);

        preferences = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE);

        binding.buChooseImage.setOnClickListener(v -> {
            PickVisualMediaRequest mediaRequest = new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build();
            pickMedia.launch(mediaRequest);
        });

        binding.buCreateAnAccount.setOnClickListener(v -> {
            pharmacyName = Objects.requireNonNull(binding.edPharmacyName.getText()).toString().trim();
            phone = Objects.requireNonNull(binding.edPhone.getText()).toString().trim();
            address = Objects.requireNonNull(binding.edAddress.getText()).toString().trim();
            email = Objects.requireNonNull(binding.edEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.edPassword.getText()).toString().trim();

            registerPharmacy(email, password);
        });

        return binding.getRoot();
    }

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri != null) {
                            pharmacyImageUri = uri;
                            binding.ivPharmacy.setImageURI(uri);
                        }
                    });

    private void registerPharmacy(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
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

        pdfRef.putFile(pharmacyImage)
                .addOnProgressListener(taskSnapshot -> {
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
                }).addOnFailureListener(exception ->
                        Log.e(TAG, "uploadPharmacyImage: " + exception.getMessage()));
    }


    private void updateUserProfile(String fullName, Uri imageUrl) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .setPhotoUri(imageUrl)
                .build();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "update Profile Successful", Toast.LENGTH_SHORT).show();

                            SharedPreferences preferences =
                                    requireActivity().getSharedPreferences("user_info", MODE_PRIVATE);
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

        pharmaciesCollection.document(userUid)
                .set(pharmacyData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        preferences.edit().putString("address", address).apply();

                        Toast.makeText(getActivity(), "create pharmacy successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getActivity(), BaseActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                }).addOnFailureListener(e ->
                        Log.e(TAG, "createPharmacy: " + e.getMessage()));
    }
}