package com.example.deadline;

import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchNotifications;
    private SwitchCompat switchDarkMode;
    private FirebaseAuth mAuth;
    private DatabaseReference userSettingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Initialize DatabaseReference
        userSettingsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("settings");

        // Initialize the switches
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Load current settings
        loadSettings();

        // Set listeners for the switches
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle notifications setting
            if (isChecked) {
                Toast.makeText(SettingsActivity.this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
            saveSettings();  // Save the settings when they change
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle dark mode setting
            if (isChecked) {
                Toast.makeText(SettingsActivity.this, "Dark mode enabled", Toast.LENGTH_SHORT).show();
                // You can implement dark mode theme change here if needed
            } else {
                Toast.makeText(SettingsActivity.this, "Dark mode disabled", Toast.LENGTH_SHORT).show();
                // You can implement light mode theme change here if needed
            }
            saveSettings();  // Save the settings when they change
        });
    }

    // Method to load settings from Firebase
    private void loadSettings() {
        // Load settings from Firebase (add necessary listeners for real-time updates if needed)
        // For now, we'll assume the settings are already present in Firebase.
    }

    // Save settings to Firebase
    private void saveSettings() {
        // Save the settings to Firebase Realtime Database
        boolean notificationsEnabled = switchNotifications.isChecked();
        boolean darkModeEnabled = switchDarkMode.isChecked();

        userSettingsRef.child("notifications").setValue(notificationsEnabled);
        userSettingsRef.child("darkMode").setValue(darkModeEnabled);

        // Provide feedback to the user
        Toast.makeText(SettingsActivity.this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }
}
