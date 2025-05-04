package com.example.vehiclespotapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "parking_locations")
public class ParkingLocation {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double latitude;
    private double longitude;
    private long timestamp;
    private String notes;
    private String address;
    private String parkingLevel;
    private String parkingSpot;
    private String vehicleDetails;
    private String photoPath;

    public ParkingLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getParkingLevel() { return parkingLevel; }
    public void setParkingLevel(String parkingLevel) { this.parkingLevel = parkingLevel; }
    
    public String getParkingSpot() { return parkingSpot; }
    public void setParkingSpot(String parkingSpot) { this.parkingSpot = parkingSpot; }
    
    public String getVehicleDetails() { return vehicleDetails; }
    public void setVehicleDetails(String vehicleDetails) { this.vehicleDetails = vehicleDetails; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
} 