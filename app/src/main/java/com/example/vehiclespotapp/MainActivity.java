package com.example.vehiclespotapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.example.vehiclespotapp.data.ParkingLocation;
import com.example.vehiclespotapp.service.ParkingNotificationService;
import com.example.vehiclespotapp.ui.HistoryFragment;
import com.example.vehiclespotapp.viewmodel.ParkingViewModel;
import android.widget.RelativeLayout;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import android.widget.FrameLayout;
import android.widget.ZoomControls;
import android.view.Gravity;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    private static final float DEFAULT_ZOOM = 17f;
    private static final long LOCATION_UPDATE_INTERVAL = 2000; // 2 seconds
    private static final long FASTEST_UPDATE_INTERVAL = 1000; // 1 second
    private FusedLocationProviderClient fusedLocationClient;
    private MaterialCardView btnSave, btnFind;
    private ParkingViewModel parkingViewModel;
    private boolean locationPermissionGranted;
    private BottomNavigationView bottomNavigation;
    private LocationRequest locationRequest;
    private ParkingNotificationService notificationService;
    private LocationCallback locationCallback;
    private boolean isTrackingLocation = false;
    private Marker parkedLocationMarker;
    private boolean isFirstLocationUpdate = true;
    private MapView osmMapView;
    private org.osmdroid.views.overlay.Marker userMarker;
    private org.osmdroid.views.overlay.Polygon parkingZoneCircle;
    private static final int PARKING_ZONE_RADIUS = 50; // radius in meters
    private static final int PARKING_ZONE_COLOR = 0x33FF0000; // semi-transparent red
    private static final int PARKING_ZONE_STROKE_COLOR = 0xFFFF0000; // solid red
    private static final float PARKING_ZONE_STROKE_WIDTH = 2.0f;
    private CompassOverlay compassOverlay;
    private RotationGestureOverlay rotationGestureOverlay;
    private MyLocationNewOverlay myLocationOverlay;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "app_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_main);
        try {
            setTheme(R.style.Theme_VehicleSpotApp);
            
            // Initialize services
            initializeServices();
            
            // Initialize UI
            initializeUI();
            
            // Setup location callback
            setupLocationCallback();
            
            // Setup click listeners
            setupClickListeners();

            // Check permissions
            checkLocationPermission();
            checkNotificationPermission();

            // Setup bottom navigation
            setupBottomNavigation();

            osmMapView = findViewById(R.id.osm_map);
            osmMapView.setTileSource(TileSourceFactory.MAPNIK);
            osmMapView.setMultiTouchControls(true);
            osmMapView.getController().setZoom(15.0);
            osmMapView.getController().setCenter(new GeoPoint(23.0225, 72.5714)); // Example: Ahmedabad
            osmMapView.setBuiltInZoomControls(false);

            // Add compass overlay
            compassOverlay = new CompassOverlay(this, osmMapView);
            compassOverlay.enableCompass();
            osmMapView.getOverlays().add(compassOverlay);

            // Add rotation gesture overlay
            rotationGestureOverlay = new RotationGestureOverlay(osmMapView);
            rotationGestureOverlay.setEnabled(true);
            osmMapView.getOverlays().add(rotationGestureOverlay);

            findViewById(R.id.btnZoomIn).setOnClickListener(v -> osmMapView.getController().zoomIn());
            findViewById(R.id.btnZoomOut).setOnClickListener(v -> osmMapView.getController().zoomOut());
            findViewById(R.id.btnMyLocation).setOnClickListener(v -> centerMapOnUserLocation());

            // Request permissions if needed
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                startLiveLocationTracking();
            }

            // Initialize SharedPreferences
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        } catch (Exception e) {
            showErrorAndFinish("Error initializing MainActivity: " + e.getMessage());   
        }
    }

    private void initializeServices() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            parkingViewModel = new ViewModelProvider(this).get(ParkingViewModel.class);
            
            // Create location request with high accuracy
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)  // 10 seconds
                    .setFastestInterval(5000);  // 5 seconds
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize services: " + e.getMessage());
        }
    }

    private void initializeUI() {
        try {
            btnSave = findViewById(R.id.btnSaveLocation);
            btnFind = findViewById(R.id.btnFindLocation);

            if (btnSave == null || btnFind == null) {
                throw new NullPointerException("Required MaterialCardViews not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize UI: " + e.getMessage());
        }
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                updateUserLocation(location);
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Create a high-accuracy location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)  // 1 second
                .setFastestInterval(500)  // 0.5 seconds
                .setSmallestDisplacement(1)  // 1 meter minimum movement
                .setMaxWaitTime(2000);  // Maximum wait time for updates

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
        
        // Also get the last known location immediately
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null && osmMapView != null) {
                        updateUserLocation(location);
                    }
                });
    }

    private void updateUserLocation(Location location) {
        if (location == null || osmMapView == null) return;

        GeoPoint currentGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        
        // Remove the old user marker if it exists
        if (userMarker != null) {
            osmMapView.getOverlays().remove(userMarker);
            userMarker = null;
        }
        
        // Center map on first location update
        if (isFirstLocationUpdate) {
            osmMapView.getController().setZoom(DEFAULT_ZOOM);
            osmMapView.getController().setCenter(currentGeoPoint);
            isFirstLocationUpdate = false;
        }
        
        // Redraw the map
        osmMapView.invalidate();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isTrackingLocation = false;
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> {
            // Add animation to the save button
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            btnSave.startAnimation(pulse);
            checkLocationPermission();
        });
        
        btnFind.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
                return;
            }

            // Get current location
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null && osmMapView != null) {
                        // Center map on current location
                        GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        
                        // Set initial zoom level to show more area
                        osmMapView.getController().setZoom(15.0);
                        osmMapView.getController().setCenter(currentLocation);
                        
                        // Enable multi-touch controls only
                        osmMapView.setMultiTouchControls(true);
                        
                        // Remove any existing user marker
                        if (userMarker != null) {
                            osmMapView.getOverlays().remove(userMarker);
                            userMarker = null;
                        }

                        // Show parked vehicle location if available
                        parkingViewModel.getLastLocation().observe(this, parkedLocation -> {
                            if (parkedLocation != null) {
                                GeoPoint parkedPoint = new GeoPoint(parkedLocation.getLatitude(), parkedLocation.getLongitude());
                                
                                // Remove previous parked location marker if exists
                                if (parkedLocationMarker != null) {
                                    osmMapView.getOverlays().remove(parkedLocationMarker);
                                }

                                // Add new marker with car logo icon
                                parkedLocationMarker = new Marker(osmMapView);
                                parkedLocationMarker.setPosition(parkedPoint);
                                parkedLocationMarker.setTitle("Parked Vehicle");
                                parkedLocationMarker.setSnippet("Your vehicle is parked here");
                                parkedLocationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_car_logo));
                                osmMapView.getOverlays().add(parkedLocationMarker);
                                
                                // Add parking zone circle
                                updateParkingZone(parkedPoint);
                                
                                // Calculate center point between user and vehicle
                                double centerLat = (currentLocation.getLatitude() + parkedPoint.getLatitude()) / 2;
                                double centerLon = (currentLocation.getLongitude() + parkedPoint.getLongitude()) / 2;
                                GeoPoint centerPoint = new GeoPoint(centerLat, centerLon);
                                
                                // Set zoom level to show both locations
                                osmMapView.getController().setCenter(centerPoint);
                                osmMapView.getController().setZoom(15.0);
                                
                                osmMapView.invalidate();
                                
                                // Show navigation options
                                showNavigationOptions(parkedPoint);
                            } else {
                                showNoLocationDialog();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }

    private void checkLocationPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                if (!isLocationEnabled()) {
                    promptEnableLocation();
                } else {
                    saveLocation();
                }
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showPermissionRationaleDialog();
                } else {
                    requestLocationPermission();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error checking permissions: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showPermissionRationaleDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.location_permission)
                    .setMessage(R.string.location_permission_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> requestLocationPermission())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing permission dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestLocationPermission() {
        try {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Error requesting permission: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // Check if there's already a saved location
            parkingViewModel.getLastLocation().observe(this, existingLocation -> {
                if (existingLocation != null) {
                    // Show confirmation dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Override Location")
                            .setMessage("You already have a saved location. Do you want to override it?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                getAndSaveCurrentLocation();
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    getAndSaveCurrentLocation();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error saving location: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void getAndSaveCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, R.string.getting_location, Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        ParkingLocation parkingLocation = new ParkingLocation(
                            location.getLatitude(),
                            location.getLongitude()
                        );
                        parkingViewModel.insert(parkingLocation);
                        showSuccessDialog();
                        
                        // Start notification service with the parking location
                        startNotificationService(location);
                        
                        // Update map with parked location
                        if (osmMapView != null) {
                            GeoPoint parkedLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                            
                            // Remove previous parked location marker if exists
                            if (parkedLocationMarker != null) {
                                osmMapView.getOverlays().remove(parkedLocationMarker);
                            }

                            try {
                                // Add new marker with car logo icon
                                parkedLocationMarker = new Marker(osmMapView);
                                parkedLocationMarker.setPosition(parkedLocation);
                                parkedLocationMarker.setTitle("Parked Vehicle");
                                parkedLocationMarker.setSnippet("Your vehicle is parked here");
                                parkedLocationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_car_logo));
                                osmMapView.getOverlays().add(parkedLocationMarker);
                                
                                // Add parking zone circle
                                updateParkingZone(parkedLocation);
                                
                                osmMapView.getController().setCenter(parkedLocation);
                                osmMapView.invalidate();
                            } catch (Exception e) {
                                // Fallback to default marker if custom icon fails
                                parkedLocationMarker = new Marker(osmMapView);
                                parkedLocationMarker.setPosition(parkedLocation);
                                parkedLocationMarker.setTitle("Parked Vehicle");
                                parkedLocationMarker.setSnippet("Your vehicle is parked here");
                                osmMapView.getOverlays().add(parkedLocationMarker);
                                
                                // Add parking zone circle
                                updateParkingZone(parkedLocation);
                                
                                osmMapView.getController().setCenter(parkedLocation);
                                osmMapView.invalidate();
                            }
                        }
                    } else {
                        Toast.makeText(this, R.string.location_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.location_saved)
                    .setMessage(R.string.location_saved_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing success dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showNoLocationDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_location)
                    .setMessage(R.string.no_location_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                if (!isLocationEnabled()) {
                    promptEnableLocation();
                } else {
                    saveLocation();
                }
            } else {
                Toast.makeText(this, R.string.location_permission_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                showHomeFragment();
                return true;
            } else if (itemId == R.id.navigation_history) {
                showHistoryFragment();
                return true;
            } else if (itemId == R.id.navigation_settings) {
                // Launch SettingsActivity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Launch ProfileActivity
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void showHomeFragment() {
        Fragment historyFragment = getSupportFragmentManager().findFragmentByTag("history");
        if (historyFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .hide(historyFragment)
                    .commit();
        }
        View mapView = findViewById(R.id.osm_map);
        if (mapView != null) {
            mapView.setVisibility(View.VISIBLE);
        }
        findViewById(R.id.buttonContainer).setVisibility(View.VISIBLE);
        // Show the My Location button
        View myLocationButton = findViewById(R.id.btnMyLocation);
        if (myLocationButton != null) {
            myLocationButton.setVisibility(View.VISIBLE);
        }
        // Show the zoom controls
        View zoomControls = findViewById(R.id.zoomControls);
        if (zoomControls != null) {
            zoomControls.setVisibility(View.VISIBLE);
        }
    }

    private void showHistoryFragment() {
        findViewById(R.id.buttonContainer).setVisibility(View.GONE);
        View mapView = findViewById(R.id.osm_map);
        if (mapView != null) {
            mapView.setVisibility(View.GONE);
        }
        // Hide the My Location button
        View myLocationButton = findViewById(R.id.btnMyLocation);
        if (myLocationButton != null) {
            myLocationButton.setVisibility(View.GONE);
        }
        // Hide the zoom controls
        View zoomControls = findViewById(R.id.zoomControls);
        if (zoomControls != null) {
            zoomControls.setVisibility(View.GONE);
        }

        Fragment historyFragment = getSupportFragmentManager().findFragmentByTag("history");
        if (historyFragment == null) {
            historyFragment = new HistoryFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, historyFragment, "history")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .show(historyFragment)
                    .commit();
        }
    }

    private void showNavigationOptions(GeoPoint destination) {
        // ... code using osmdroid ...
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void startNotificationService(Location location) {
        Intent serviceIntent = new Intent(this, ParkingNotificationService.class);
        serviceIntent.putExtra("location", location);
        startService(serviceIntent);
    }

    @Override
    public void onBackPressed() {
        Fragment historyFragment = getSupportFragmentManager().findFragmentByTag("history");
        if (historyFragment != null && historyFragment.isVisible()) {
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.navigation_home);
            }
            showHomeFragment();
        } else {
            super.onBackPressed();
            new AlertDialog.Builder(this)
                    .setTitle("Exit App")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
        showHomeFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
        showHomeFragment();
        if (compassOverlay != null) {
            compassOverlay.enableCompass();
        }
        if (locationPermissionGranted) {
            startLiveLocationTracking();
        }
        osmMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (compassOverlay != null) {
            compassOverlay.disableCompass();
        }
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
            myLocationOverlay.disableFollowLocation();
        }
        osmMapView.onPause();
    }

    private void centerMapOnUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && osmMapView != null) {
                GeoPoint userPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                osmMapView.getController().setCenter(userPoint);
                osmMapView.getController().setZoom(17.0);
            }
        });
    }

    private void startLiveLocationTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Initialize MyLocationNewOverlay if not already done
        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), osmMapView);
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
            myLocationOverlay.setDrawAccuracyEnabled(true);
            
            // Convert Drawable to Bitmap
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.person_icon);
            if (drawable != null) {
                Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
                );
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                myLocationOverlay.setPersonIcon(bitmap);
            }
            
            osmMapView.getOverlays().add(myLocationOverlay);
        }

        // Create a high-accuracy location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)  // 1 second
                .setFastestInterval(500)  // 0.5 seconds
                .setSmallestDisplacement(1);  // 1 meter minimum movement

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) return;
                        
                        Location location = locationResult.getLastLocation();
                        if (location == null) return;

                        // Update the map center and zoom
                        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                        osmMapView.getController().setZoom(18.0);
                        osmMapView.getController().animateTo(point);
                        
                        // The MyLocationNewOverlay will automatically update the marker position
                        osmMapView.invalidate();
                    }
                },
                Looper.getMainLooper());
    }

    private void updateParkingZone(GeoPoint center) {
        if (osmMapView == null) return;

        // Remove existing circle if any
        if (parkingZoneCircle != null) {
            osmMapView.getOverlays().remove(parkingZoneCircle);
        }

        // Create new circle
        parkingZoneCircle = new org.osmdroid.views.overlay.Polygon();
        parkingZoneCircle.setPoints(createCirclePoints(center, PARKING_ZONE_RADIUS));
        parkingZoneCircle.setFillColor(PARKING_ZONE_COLOR);
        parkingZoneCircle.setStrokeColor(PARKING_ZONE_STROKE_COLOR);
        parkingZoneCircle.setStrokeWidth(PARKING_ZONE_STROKE_WIDTH);
        parkingZoneCircle.setTitle("Parking Zone");

        // Add circle to map
        osmMapView.getOverlays().add(parkingZoneCircle);
        osmMapView.invalidate();
    }

    private List<GeoPoint> createCirclePoints(GeoPoint center, double radiusInMeters) {
        List<GeoPoint> points = new ArrayList<>();
        int numberOfPoints = 36; // Number of points to create a smooth circle
        
        for (int i = 0; i < numberOfPoints; i++) {
            double angle = (2 * Math.PI * i) / numberOfPoints;
            double lat = center.getLatitude() + (radiusInMeters / 111111.0) * Math.cos(angle);
            double lon = center.getLongitude() + (radiusInMeters / (111111.0 * Math.cos(Math.toRadians(center.getLatitude())))) * Math.sin(angle);
            points.add(new GeoPoint(lat, lon));
        }
        
        // Close the circle
        points.add(points.get(0));
        return points;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void promptEnableLocation() {
        new AlertDialog.Builder(this)
            .setTitle("Enable Location")
            .setMessage("Location services are disabled. Please enable location to use this feature.")
            .setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}