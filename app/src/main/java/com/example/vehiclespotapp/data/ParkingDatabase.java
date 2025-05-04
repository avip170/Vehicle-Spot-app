package com.example.vehiclespotapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ParkingLocation.class}, version = 1, exportSchema = false)
public abstract class ParkingDatabase extends RoomDatabase {
    private static ParkingDatabase instance;
    public abstract ParkingDao parkingDao();

    public static synchronized ParkingDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ParkingDatabase.class,
                    "parking_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
} 