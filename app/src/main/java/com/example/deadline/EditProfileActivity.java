package com.example.deadline;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail;
    private Button btnSave;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

            // Load current data
            userRef.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = currentUser.getEmail();

                    etName.setText(name);
                    etEmail.setText(email);
                }
            });
        }

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            // Cập nhật email trên Firebase Auth
            currentUser.updateEmail(newEmail).addOnSuccessListener(unused -> {
                // Cập nhật tên và email trong Realtime Database
                userRef.child("name").setValue(newName);
                userRef.child("email").setValue(newEmail);
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi cập nhật email: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }
}
