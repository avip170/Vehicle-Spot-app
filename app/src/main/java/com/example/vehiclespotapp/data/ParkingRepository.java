package com.example.vehiclespotapp.data;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParkingRepository {
    private ParkingDao parkingDao;
    private LiveData<List<ParkingLocation>> allLocations;
    private LiveData<ParkingLocation> lastLocation;
    private ExecutorService executorService;
    private Geocoder geocoder;

    public ParkingRepository(Application application) {
        ParkingDatabase database = ParkingDatabase.getInstance(application);
        parkingDao = database.parkingDao();
        allLocations = parkingDao.getAllLocations();
        lastLocation = parkingDao.getLastLocation();
        executorService = Executors.newSingleThreadExecutor();
        geocoder = new Geocoder(application, Locale.getDefault());
    }

    public void insert(ParkingLocation location) {
        executorService.execute(() -> {
            try {
                // Get address from coordinates
                List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), 
                    location.getLongitude(), 
                    1
                );
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder addressText = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressText.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) {
                            addressText.append(", ");
                        }
                    }
                    location.setAddress(addressText.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            parkingDao.insert(location);
        });
    }

    public void update(ParkingLocation location) {
        executorService.execute(() -> parkingDao.update(location));
    }

    public void delete(ParkingLocation location) {
        executorService.execute(() -> parkingDao.delete(location));
    }

    public LiveData<List<ParkingLocation>> getAllLocations() {
        return allLocations;
    }

    public LiveData<ParkingLocation> getLastLocation() {
        return lastLocation;
    }

    public void deleteAll() {
        executorService.execute(() -> parkingDao.deleteAll());
    }
} 