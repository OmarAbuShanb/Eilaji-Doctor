package dev.anonymous.eilaji.doctor.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import dev.anonymous.eilaji.doctor.R;
import dev.anonymous.eilaji.doctor.utils.Utils;
import dev.anonymous.eilaji.doctor.databinding.ActivityBaseBinding;

public class BaseActivity extends AppCompatActivity {
    private ActivityBaseBinding binding;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.includeAppBarLayoutBase.toolbarApp);

        setUpDrawerLayout();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Utils.loadImage(this, String.valueOf(user.getPhotoUrl()))
                    .into(binding.ivPharmacyNav);
        }

    }

    void setUpDrawerLayout() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
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