package com.choosemuse.example.libmuse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private String currentDeviceID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_registration);

        // Get the device ID or generate a new one if it doesn't exist
        currentDeviceID = getDeviceID();
        Log.d("DeviceID:", currentDeviceID);




        // Set up the toolbar (if you have one)

/*        // Navigation setup (for AppBar or BottomNavigation)
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);*/

        // Floating Action Button (FAB) click action


        // Automatically log the user in or prompt registration
        autoLogin();
    }

    /**
     * Helper function to switch activity when a button is clicked
     */
    public void switchActivityButton(Button button, Intent intent) {
        button.setOnClickListener(view -> startActivity(intent));
    }

    /**
     * Retrieve or generate a unique device ID.
     * Saves the ID in SharedPreferences for later use.
     */
    private String getDeviceID() {
        SharedPreferences localID = getSharedPreferences("LocalID", Context.MODE_PRIVATE);
        String ID = localID.getString("ID", "Not Found");
        if (ID.equals("Not Found")) {
            ID = UUID.randomUUID().toString(); // Generate a new UUID
            SharedPreferences.Editor editor = localID.edit();
            editor.putString("ID", ID); // Save the generated ID in SharedPreferences
            editor.apply();
        }
        return ID;
    }

    /**
     * Automatically log in the user if Firebase Authentication exists,
     * otherwise prompt for registration using the device ID.
     */
    private void autoLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the currently logged-in user
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get the Firebase user ID
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            // Load the user's profile from Firestore
                            loadUserProfile(document.getData());
                        } else {
                            // If no user profile exists, create one
                            Log.d("AutoLogin", "User profile not found. Redirecting to registration.");
                            promptUserRegistration();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("AutoLogin", "Failed to retrieve user data.", e));
        } else {
            // If no Firebase user is logged in, check if a user exists based on device ID
            Log.d("AuthActivity", "No user logged in. Redirecting to registration.");
            promptUserRegistration();
        }
    }

    /**
     * Redirect to the registration activity
     */
    private void promptUserRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    /**
     * Load user profile from Firestore
     */
    private void loadUserProfile(Map<String, Object> userData) {
        String username = userData.containsKey("username") ? (String) userData.get("username") : "Unknown User";
        Log.d("Profile", "Welcome back, " + username + "!");
        // Here, you can update the UI with the user's data
    }
}

