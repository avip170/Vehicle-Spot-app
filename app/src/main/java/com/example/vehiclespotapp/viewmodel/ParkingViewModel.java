package com.example.vehiclespotapp.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.vehiclespotapp.data.ParkingLocation;
import com.example.vehiclespotapp.data.ParkingRepository;

import java.util.List;

public class ParkingViewModel extends AndroidViewModel {
    private ParkingRepository repository;
    private LiveData<List<ParkingLocation>> allLocations;
    private LiveData<ParkingLocation> lastLocation;

    public ParkingViewModel(Application application) {
        super(application);
        repository = new ParkingRepository(application);
        allLocations = repository.getAllLocations();
        lastLocation = repository.getLastLocation();
    }

    public void insert(ParkingLocation location) {
        repository.insert(location);
    }

    public void update(ParkingLocation location) {
        repository.update(location);
    }

    public void delete(ParkingLocation location) {
        repository.delete(location);
    }

    public LiveData<List<ParkingLocation>> getAllLocations() {
        return allLocations;
    }

    public LiveData<ParkingLocation> getLastLocation() {
        return lastLocation;
    }

    public void deleteAll() {
        repository.deleteAll();
    }
} 