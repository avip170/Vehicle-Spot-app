package com.example.vehiclespotapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ParkingDao {
    @Insert
    long insert(ParkingLocation location);

    @Update
    void update(ParkingLocation location);

    @Delete
    void delete(ParkingLocation location);

    @Query("SELECT * FROM parking_locations ORDER BY timestamp DESC")
    LiveData<List<ParkingLocation>> getAllLocations();

    @Query("SELECT * FROM parking_locations ORDER BY timestamp DESC LIMIT 1")
    LiveData<ParkingLocation> getLastLocation();

    @Query("DELETE FROM parking_locations")
    void deleteAll();
} 