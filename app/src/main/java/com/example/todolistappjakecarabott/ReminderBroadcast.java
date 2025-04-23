package com.example.todolistappjakecarabott;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

// BroadcastReceiver that handles scheduled task reminder notifications
public class ReminderBroadcast extends BroadcastReceiver {

    // Flag to determine whether notifications should be shown
    public static boolean canNotif = true; // Can be toggled via settings screen

    // Triggered when the scheduled alarm goes off
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!canNotif) return; // Exit if reminders are disabled

        // Extract task text and notification ID from the Intent
        String taskText = intent.getStringExtra("task");
        int notificationId = intent.getIntExtra("notifId", 0);

        // Prepare intent to open the app's main screen when notification is tapped
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE
        );

        // Build the actual notification with title, message, and tap intent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "todo_notify")
                .setSmallIcon(R.drawable.ic_notification) // App icon used in notification
                .setContentTitle("Task Reminder")
                .setContentText("\"" + taskText + "\" is due")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true); // Dismiss when tapped

        // Get system service to display the notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build()); // Show it using the provided ID
    }
}
