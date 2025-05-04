package com.example.vehiclespotapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.vehiclespotapp.MainActivity;
import com.example.vehiclespotapp.R;

public class ParkingNotificationService extends Service {
    private static final String CHANNEL_ID = "parking_reminder_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "ParkingNotificationService";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("location")) {
            Location location = intent.getParcelableExtra("location");
            if (location != null) {
                scheduleNotification(location);
            }
        }
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Parking Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Reminds you where you parked your vehicle");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void scheduleNotification(Location location) {
        // Create an intent to open the app when notification is clicked
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("show_parking_location", true);
        notificationIntent.putExtra("parking_latitude", location.getLatitude());
        notificationIntent.putExtra("parking_longitude", location.getLongitude());
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_car_logo)
            .setContentTitle("Parking Reminder")
            .setContentText("Don't forget where you parked your vehicle!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // Schedule the next notification (e.g., after 1 hour)
        scheduleNextNotification();
    }

    private void scheduleNextNotification() {
        // Here you can implement logic to schedule the next notification
        // For example, using AlarmManager or WorkManager
        // This is a placeholder for the actual scheduling implementation
        Log.d(TAG, "Next notification scheduled");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 