package lk.avn.irenttechs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private NotificationManager notificationManager;
    private String channel_id = "info";

    @Override
    public void onReceive(Context context, Intent intent) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id, "INFO", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            channel.setDescription("This is Information Channel");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000});
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        String action = intent.getAction();

        if (action != null && action.equals("android.intent.action.BATTERY_LOW")) {
            Toast.makeText(context, "Battery is low", Toast.LENGTH_SHORT).show();
        }

        if (action != null && action.equals("lk.avn.irenttechs.CUSTOM_INTENT")) {
            String orderId = intent.getStringExtra("orderId");
            String email = intent.getStringExtra("email");

            SharedPreferences preferences = context.getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);
            String name = preferences.getString("NAME", null);
            String email2 = preferences.getString("EMAIL", null);
            if (email != null && email.equals(email2)) {
                Notification notification = new NotificationCompat.Builder(context, channel_id)
                        .setSmallIcon(R.drawable.baseline_file_download_done_24)
                        .setContentTitle("Your Order is in shipping process")
                        .setContentText(name+" This is your Order Id : "+orderId)
                        .setColor(Color.RED)
                        .build();

                notificationManager.notify(1, notification);
                Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
            }
        }
    }
}