package dev.anonymous.eilaji.doctor.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import dev.anonymous.eilaji.doctor.R;
import dev.anonymous.eilaji.doctor.databinding.ActivityBaseBinding;
import dev.anonymous.eilaji.doctor.firebase.FirebaseController;
import dev.anonymous.eilaji.doctor.utils.Utils;

public class BaseActivity extends AppCompatActivity {
    private ActivityBaseBinding binding;

    private SharedPreferences preferences;

    public ActionBarDrawerToggle actionBarDrawerToggle;

    private final FirebaseController firebaseController = FirebaseController.getInstance();
    private final FirebaseUser user = firebaseController.getCurrentUser();

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialing the binding
        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        // set the view root
        setContentView(binding.getRoot());
        // set up the ActionBar
        setSupportActionBar(binding.includeAppBarLayoutBase.toolbarApp);

        preferences = getSharedPreferences("user_info", MODE_PRIVATE);

        // setup the Drawer Layout
        setUpDrawerLayout();
        // setup the NavController
        setupNavController();

        // set the user photo
        setUserPhoto();

        // LunchListeners
        setupListeners();
    }

    private void setUserPhoto() {
        if (user != null) {
            String pharmacyImageUrl = preferences.getString("pharmacy_image_url", null);
            Utils.loadImage(this, pharmacyImageUrl).into(binding.ivPharmacyNav);
        }
    }

    // setup the NavController
    private void setupNavController() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_base);
    }

    // helper methods
    private void setupListeners() {
        binding.buChats.setOnClickListener(view -> {
            // to be moving to the chat
            closeDrawer();
            navController.navigate(R.id.navigation_chats);
        });
        binding.buEditUserInfo.setOnClickListener(view -> {
            // to be moving to the editScreen
            closeDrawer();
            navController.navigate(R.id.navigation_editInformation);
        });
        binding.buLogout.setOnClickListener(view -> {
            // to sig-out
            firebaseController.signOut();

            // to do and clear the user session
            //.. appShearedPreference = clear..
            closeDrawer();
            // at the end show the login
            var intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragmentType", "Login");
            startActivity(intent);
            finish();
        });
    }

    private void closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // some of the drawer settings
    private void setUpDrawerLayout() {
        actionBarDrawerToggle =
                new ActionBarDrawerToggle
                        (this,
                                binding.drawerLayout,
                                R.string.nav_open,
                                R.string.nav_close);

        actionBarDrawerToggle.getDrawerArrowDrawable()
                .setColor(getResources().getColor(R.color.white));

        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}