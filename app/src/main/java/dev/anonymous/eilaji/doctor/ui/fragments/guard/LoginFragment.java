package dev.anonymous.eilaji.doctor.ui.fragments.guard;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

import dev.anonymous.eilaji.doctor.R;
import dev.anonymous.eilaji.doctor.databinding.FragmentLoginBinding;
import dev.anonymous.eilaji.doctor.firebase.FirebaseController;
import dev.anonymous.eilaji.doctor.ui.activities.BaseActivity;
import dev.anonymous.eilaji.doctor.utils.Utils;
import dev.anonymous.eilaji.doctor.utils.constants.Constant;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;

    private CollectionReference pharmaciesCollection;
    private SharedPreferences preferences;

    // hesham changes
    private final FirebaseController firebaseController = FirebaseController.getInstance();

    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialing the collection
        pharmaciesCollection = FirebaseFirestore.getInstance()
                .collection(Constant.PHARMACIES_COLLECTION);

        // here initialing the preferences
        preferences = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE);

        // navController
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    private void moveToBaseActivity() {
        Intent intent = new Intent(getActivity(), BaseActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // get the listeners ready
        setupListeners();
    }

    // helper methods
    private void setupListeners() {
        binding.buLogin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.edEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.edPassword.getText()).toString().trim();
            if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(password)) {
                signIn(email, password);
            } else {
                Utils.getInstance().showSnackBar(binding.getRoot(), "Check Empty Fields!");
            }
        });

        binding.buSignUp.setOnClickListener(v -> navController.navigate(R.id.navigation_register));
    }

    private void signIn(String email, String password) {
        firebaseController.getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Authentication isSuccessful!", Toast.LENGTH_SHORT).show();
                        String userUid = Objects.requireNonNull(task.getResult().getUser()).getUid();

                        getAddress(userUid);
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void getAddress(String userUid) {
        pharmaciesCollection.document(userUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean pharmaciesExists = task.getResult().exists();
                        if (pharmaciesExists) {
                            Map<String, Object> data = task.getResult().getData();
                            if (data != null) {
                                String pharmacyName = String.valueOf(data.get("pharmacy_name"));
                                String pharmacyImageUrl = String.valueOf(data.get("pharmacy_image_url"));
                                String address = String.valueOf(data.get("address"));

                                preferences.edit()
                                        .putString("pharmacy_name", pharmacyName)
                                        .putString("pharmacy_image_url", pharmacyImageUrl)
                                        .putString("address", address)
                                        .apply();

                                moveToBaseActivity();
                            }
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    "لم يتم قبول صيدليتك\nسيصلك اشعار عند مراجعة الطلب",
                                    Toast.LENGTH_SHORT
                            ).show();
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