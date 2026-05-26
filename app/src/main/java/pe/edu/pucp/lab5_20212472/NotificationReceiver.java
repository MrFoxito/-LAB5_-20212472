package pe.edu.pucp.lab5_20212472;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ANNUAL = "annual_events";
    public static final String CHANNEL_SINGLE = "single_events";
    public static final String EXTRA_EVENT_NAME = "extra_event_name";
    public static final String EXTRA_EVENT_DATE = "extra_event_date";
    public static final String EXTRA_PERIODICITY = "extra_periodicity";

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventName = intent.getStringExtra(EXTRA_EVENT_NAME);
        String eventDate = intent.getStringExtra(EXTRA_EVENT_DATE);
        String periodicity = intent.getStringExtra(EXTRA_PERIODICITY);

        createNotificationChannels(context);

        String channelId = periodicity.equals("Anual") ? CHANNEL_ANNUAL : CHANNEL_SINGLE;
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Recordatorio: " + eventName)
                .setContentText("Fecha programada: " + eventDate)
                .setPriority(periodicity.equals("Anual") ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return; // Sin permisos
            }
        }
        
        // Use a unique ID based on time so multiple notifications can show
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if(manager.getNotificationChannel(CHANNEL_ANNUAL) == null) {
                NotificationChannel channelAnnual = new NotificationChannel(
                        CHANNEL_ANNUAL, "Eventos Anuales", NotificationManager.IMPORTANCE_HIGH);
                channelAnnual.setDescription("Notificaciones para eventos anuales (alta prioridad)");
                manager.createNotificationChannel(channelAnnual);
            }
            if(manager.getNotificationChannel(CHANNEL_SINGLE) == null) {
                NotificationChannel channelSingle = new NotificationChannel(
                        CHANNEL_SINGLE, "Eventos Únicos", NotificationManager.IMPORTANCE_DEFAULT);
                channelSingle.setDescription("Notificaciones para eventos de única vez");
                manager.createNotificationChannel(channelSingle);
            }
        }
    }
}
