package com.example.vehiclespotapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {
    private EditText firstNameEdit, lastNameEdit, emailEdit, vehicleNumberEdit, bioEdit;
    private Button saveButton;
    private ImageView profileIcon, editProfilePic;
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    private static final int REQUEST_PERMISSIONS = 1003;
    private Uri cameraImageUri;
    private String savedImagePath = null;
    private Bitmap currentProfileBitmap = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firstNameEdit = findViewById(R.id.editTextFirstName);
        lastNameEdit = findViewById(R.id.editTextLastName);
        emailEdit = findViewById(R.id.editTextEmail);
        vehicleNumberEdit = findViewById(R.id.editTextVehicleNumber);
        bioEdit = findViewById(R.id.editTextBio);
        saveButton = findViewById(R.id.buttonSaveProfile);
        profileIcon = findViewById(R.id.profileIcon);
        editProfilePic = findViewById(R.id.editProfilePic);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        // Load saved values
        firstNameEdit.setText(prefs.getString("first_name", ""));
        lastNameEdit.setText(prefs.getString("last_name", ""));
        emailEdit.setText(prefs.getString("email", ""));
        vehicleNumberEdit.setText(prefs.getString("vehicle_number", ""));
        bioEdit.setText(prefs.getString("bio", ""));

        // Check if user signed in with Google
        boolean isGoogleSignIn = prefs.getBoolean("google_sign_in", false);
        if (isGoogleSignIn) {
            // Make email field read-only for Google users
            emailEdit.setEnabled(false);
            emailEdit.setFocusable(false);
            emailEdit.setHint("Email (Google Account)");
        }

        // Load saved profile image if exists
        savedImagePath = prefs.getString("profile_image_path", null);
        if (savedImagePath != null) {
            File imgFile = new File(savedImagePath);
            if (imgFile.exists()) {
                profileIcon.setImageBitmap(BitmapFactory.decodeFile(savedImagePath));
            }
        }

        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("first_name", firstNameEdit.getText().toString().trim());
            editor.putString("last_name", lastNameEdit.getText().toString().trim());
            // Email is not editable
            editor.putString("vehicle_number", vehicleNumberEdit.getText().toString().trim());
            editor.putString("bio", bioEdit.getText().toString().trim());
            // Save profile image if changed
            if (currentProfileBitmap != null) {
                try {
                    File file = new File(getFilesDir(), "profile_image.jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    currentProfileBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                    editor.putString("profile_image_path", file.getAbsolutePath());
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to save profile image", Toast.LENGTH_SHORT).show();
                }
            }
            editor.apply();
            Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
        });

        profileIcon.setOnClickListener(v -> showImagePickerDialog());
        editProfilePic.setOnClickListener(v -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {
        String[] options = {"Choose from Gallery", "Take Photo"};
        new AlertDialog.Builder(this)
                .setTitle("Set Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImageFromGallery();
                    } else if (which == 1) {
                        takePhotoFromCamera();
                    }
                })
                .show();
    }

    private void pickImageFromGallery() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = File.createTempFile("profile_pic", ".jpg", getExternalFilesDir(null));
                cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(intent, REQUEST_CAMERA);
            } catch (IOException e) {
                Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Try again after permission granted
                showImagePickerDialog();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                try {
                    currentProfileBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    profileIcon.setImageBitmap(currentProfileBitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CAMERA && cameraImageUri != null) {
                try {
                    currentProfileBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraImageUri);
                    profileIcon.setImageBitmap(currentProfileBitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
} 