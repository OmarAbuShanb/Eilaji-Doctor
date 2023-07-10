package dev.anonymous.eilaji.doctor.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import dev.anonymous.eilaji.doctor.R;
import dev.anonymous.eilaji.doctor.databinding.ActivityMainBinding;
import dev.anonymous.eilaji.doctor.firebase.FirebaseController;

public class MainActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup the navController for the main
        setupNavController();
        // to set the fragmentContainer
        decideWhichScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // check if the user active
        checkUserStatus();
    }

    // if user in or out
    private void checkUserStatus() {
        if (FirebaseController.getInstance().getCurrentUser() != null) {
            navigateToBaseScreen();
        }
    }

    private void navigateToBaseScreen() {
        var intent = new Intent(this, BaseActivity.class);
        startActivity(intent);
        finish();
    }

    // setup NavController
    private void setupNavController() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
    }

    private void decideWhichScreen() {
        var intent = new Intent();
        var screenSender = intent.getStringExtra("fragmentType");
        if (screenSender != null){
            switch (screenSender) {
                case "Login" -> toLogin();
                case "SingUp" -> toRegister();
                case "ChangePassword" -> toForgotPassword();
            }
        }
    }

    private void toLogin() {
        navController.navigate(R.id.navigation_login);
    }

    private void toRegister() {
        navController.navigate(R.id.navigation_register);
    }

    private void toForgotPassword() {
        navController.navigate(R.id.navigation_forgotPassword);
    }
}