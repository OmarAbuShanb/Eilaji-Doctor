package dev.anonymous.eilaji.doctor.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
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

    private ImageView imageView;
    private TextView textView;
    private NavigationView navigationView;
    private NavController navController;

    private final FirebaseController firebaseController = FirebaseController.getInstance();
    private final FirebaseUser user = firebaseController.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialing the binding
        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        // set the view root
        setContentView(binding.getRoot());
        // set up the ActionBar
        setSupportActionBar(binding.includeAppBarLayoutBase.toolbarApp);

        // init
        init();

        // set the user photo
        setUserPhoto();
        // set the Ph Name
        setPharmacyName();
    }

    private void listenerToExit() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menuItemLogout) {// Perform logout action here
                // to sig-out
                firebaseController.signOut();
                // to do and clear the user session
                //.. appShearedPreference = clear..
                closeDrawer();
                // at the end show the login
                var intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("fragmentType", "Login");
                startActivity(intent);
                finish();
                return true; // Return true to indicate that the item has been handled
            }else {
                // Handle other menu items
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        });
    }

    private void init(){
        preferences = getSharedPreferences("user_info", MODE_PRIVATE);

        findViews();
        // set the nav
        setupNavController();
        // if logout
        listenerToExit();
        // setup the Drawer Layout
        setUpDrawerLayout();
    }
    private void findViews(){
        navigationView = binding.navView;
        View header = navigationView.getHeaderView(0);
        imageView = header.findViewById(R.id.ivPharmacyNav);
        textView = header.findViewById(R.id.tvPharmacyName);
    }
    private void setUserPhoto() {
        if (user != null) {
            String pharmacyImageUrl = preferences.getString("pharmacy_image_url", null);
            Utils.loadImage(this, pharmacyImageUrl).into(imageView);
        }
    }

    private void setPharmacyName() {
        if (user != null) {
            String pharmacyName = preferences.getString("pharmacy_name", null);
            textView.setText(pharmacyName);
        }
    }

    // setup the NavController
    private void setupNavController() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_base);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    // helper methods
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