package com.choosemuse.example.libmuse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration); // Ensure the layout file is correct

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // UI components
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etName = findViewById(R.id.etName);
        Button btnRegister = findViewById(R.id.btnRegister);

        // Register button click listener
        btnRegister.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String name = etName.getText().toString().trim();

            // Input validation
            if (email.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(db, email, name); // Register the user
            }
        });
    }

    private void registerUser(FirebaseFirestore db, String email, String name) {
        // Generate a unique ID for the user
        String userId = java.util.UUID.randomUUID().toString();

        // Create a map to hold user details
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("email", email);
        userDetails.put("name", name);

        db.collection("users").document(userId)
                .set(userDetails)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RegistrationActivity", "User registered successfully.");
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    // Redirect to MainActivity after successful registration
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    finish(); // Close RegistrationActivity
                })
                .addOnFailureListener(e -> {
                    Log.e("RegistrationActivity", "Error saving user details: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to save user details. Check your permissions.", Toast.LENGTH_SHORT).show();
                });
    }

}
