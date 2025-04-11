package com.example.deadline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "";
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        tvUserID = findViewById(R.id.tvUserID);

        // Load user data
        loadUserData();

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        // Set click listeners
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        findViewById(R.id.btnSecurity).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SecurityActivity.class));
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
        });

        findViewById(R.id.btnHelp).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HelpActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String userId = dataSnapshot.child("userId").getValue(String.class);

                    tvUserName.setText(name != null ? name : "No Name");
                    tvUserID.setText(userId != null ? "ID: " + userId : "ID: Unknown");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finishAffinity(); // Close all activities
    }
}