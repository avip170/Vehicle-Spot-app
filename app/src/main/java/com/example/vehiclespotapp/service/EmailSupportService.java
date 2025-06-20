package com.example.vehiclespotapp.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmailSupportService {
    
    public static final String SUPPORT_EMAIL = "vehiclespotapp@gmail.com";
    public static final String FEEDBACK_EMAIL = "vehiclespotapp@gmail.com";
    
    /**
     * Send support email
     */
    public static void sendSupportEmail(Context context, String subject, String message) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{SUPPORT_EMAIL});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);
            
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(emailIntent, "Send Support Email"));
            }
        } catch (Exception e) {
            Log.e("EmailSupportService", "Failed to send support email: " + e.getMessage());
        }
    }
    
    /**
     * Send feedback email
     */
    public static void sendFeedbackEmail(Context context, String feedback) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("email", "Unknown");
            String userName = prefs.getString("first_name", "User");
            
            String subject = "Vehicle Spot App Feedback";
            String message = "User: " + userName + " (" + userEmail + ")\n\n" +
                           "Feedback:\n" + feedback + "\n\n" +
                           "App Version: " + getAppVersion(context) + "\n" +
                           "Device: " + android.os.Build.MODEL + "\n" +
                           "Android Version: " + android.os.Build.VERSION.RELEASE;
            
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);
            
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
            } else {
                android.widget.Toast.makeText(context, "No email app found.", android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("EmailSupportService", "Failed to send feedback email: " + e.getMessage());
            android.widget.Toast.makeText(context, "Failed to open email app.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Send password reset email
     */
    public static void sendPasswordResetEmail(Context context, String userEmail) {
        try {
            String subject = "Vehicle Spot App - Password Reset";
            String message = "Dear User,\n\n" +
                           "You have requested a password reset for your Vehicle Spot App account.\n\n" +
                           "If you did not request this reset, please ignore this email.\n\n" +
                           "To reset your password, please contact our support team.\n\n" +
                           "Best regards,\nVehicle Spot App Team";
            
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);
            
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(emailIntent, "Send Password Reset Email"));
            }
        } catch (Exception e) {
            Log.e("EmailSupportService", "Failed to send password reset email: " + e.getMessage());
        }
    }
    
    /**
     * Send account deletion confirmation email
     */
    public static void sendAccountDeletionEmail(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("email", "Unknown");
            String userName = prefs.getString("first_name", "User");
            
            String subject = "Vehicle Spot App - Account Deletion Confirmation";
            String message = "Dear " + userName + ",\n\n" +
                           "Your Vehicle Spot App account has been successfully deleted.\n\n" +
                           "Account Details:\n" +
                           "Email: " + userEmail + "\n" +
                           "Deletion Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
                               Locale.getDefault()).format(new Date()) + "\n\n" +
                           "All your data has been permanently removed from our servers.\n\n" +
                           "If you did not request this deletion, please contact our support team immediately.\n\n" +
                           "Thank you for using Vehicle Spot App.\n\n" +
                           "Best regards,\nVehicle Spot App Team";
            
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);
            
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(emailIntent, "Send Account Deletion Email"));
            }
        } catch (Exception e) {
            Log.e("EmailSupportService", "Failed to send account deletion email: " + e.getMessage());
        }
    }
    
    /**
     * Open email app with support contact
     */
    public static void openSupportEmail(Context context) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{SUPPORT_EMAIL});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Vehicle Spot App Support");
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(emailIntent, "Send Support Email"));
            } else {
                android.widget.Toast.makeText(context, "No email app found.", android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("EmailSupportService", "Failed to open support email: " + e.getMessage());
            android.widget.Toast.makeText(context, "Failed to open email app.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get app version
     */
    private static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }
} 