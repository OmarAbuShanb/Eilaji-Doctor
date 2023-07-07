package dev.anonymous.eilaji.doctor.ui.fragments.guard;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

import dev.anonymous.eilaji.doctor.utils.constants.Constant;
import dev.anonymous.eilaji.doctor.databinding.FragmentLoginBinding;
import dev.anonymous.eilaji.doctor.ui.activities.BaseActivity;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    FragmentLoginBinding binding;
    //    private LoginViewModel mViewModel;
    private FirebaseAuth auth;
    private CollectionReference pharmaciesCollection;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        Intent intent = new Intent(getActivity(), BaseActivity.class);
        startActivity(intent);
        requireActivity().finish();

        auth = FirebaseAuth.getInstance();
        pharmaciesCollection = FirebaseFirestore.getInstance()
                .collection(Constant.PHARMACIES_COLLECTION);
        preferences = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE);

        binding.buLogin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.edEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.edPassword.getText()).toString().trim();
            signIn(email, password);
        });

        return binding.getRoot();
    }

    private void signIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Authentication isSuccessful!", Toast.LENGTH_SHORT).show();
                        String userUid = Objects.requireNonNull(task.getResult().getUser()).getUid();
                        getAddress(userUid);

                        Intent intent = new Intent(getActivity(), BaseActivity.class);
                        startActivity(intent);
                        requireActivity().finish();

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