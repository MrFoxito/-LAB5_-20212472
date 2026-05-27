package pe.edu.pucp.lab5_20212472;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private TextView tvNoEvents;
    private CalendarView calendarView;
    private FloatingActionButton fabAddEvent;
    private EventRepository repository;
    private List<Event> allEvents;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crear canales de notificación
        NotificationReceiver.createNotificationChannels(this);

        // Solicitar permiso de notificaciones para Android >= 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        repository = new EventRepository(this);

        rvEvents = findViewById(R.id.rvEvents);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        calendarView = findViewById(R.id.calendarView);
        fabAddEvent = findViewById(R.id.fabAddEvent);

        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(new ArrayList<>(), this::confirmDeleteEvent, this::editEvent);
        rvEvents.setAdapter(adapter);

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventFormActivity.class);
            startActivity(intent);
        });

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDay = eventDay.getCalendar();
                filterEventsFromDate(clickedDay);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        allEvents = repository.getEvents();
        updateCalendar();

        if (allEvents.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);

            Calendar today = Calendar.getInstance();
            filterEventsFromDate(today);
        }
    }

    private void filterEventsFromDate(Calendar selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE"));
        android.widget.Toast.makeText(this, "Mostrando desde: " + sdf.format(selectedDate.getTime()), android.widget.Toast.LENGTH_SHORT).show();

        if (allEvents == null || allEvents.isEmpty()) {
            adapter.updateList(new ArrayList<>());
            rvEvents.setVisibility(View.GONE);
            tvNoEvents.setVisibility(View.VISIBLE);
            return;
        }

        List<Event> filtered = new ArrayList<>();

        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        for (Event event : allEvents) {
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTimeInMillis(event.getDateInMillis());
            eventCal.set(Calendar.HOUR_OF_DAY, 0);
            eventCal.set(Calendar.MINUTE, 0);
            eventCal.set(Calendar.SECOND, 0);
            eventCal.set(Calendar.MILLISECOND, 0);

            if (!eventCal.before(selectedDate)) {
                filtered.add(event);
            }
        }

        adapter.updateList(filtered);

        if (filtered.isEmpty()) {
            rvEvents.setVisibility(View.GONE);
            tvNoEvents.setVisibility(View.VISIBLE);
        } else {
            rvEvents.setVisibility(View.VISIBLE);
            tvNoEvents.setVisibility(View.GONE);
        }
    }

    private void updateCalendar() {
        java.util.Map<String, int[]> dateMap = new java.util.HashMap<>();
        java.util.Map<String, Calendar> calMap = new java.util.HashMap<>();
        
        for (Event event : allEvents) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(event.getDateInMillis());
            String key = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
            
            if (!dateMap.containsKey(key)) {
                dateMap.put(key, new int[]{0, 0}); // {hasAnnual, hasSingle}
                calMap.put(key, calendar);
            }
            
            if (event.getPeriodicity().equals("Anual")) {
                dateMap.get(key)[0] = 1;
            } else {
                dateMap.get(key)[1] = 1;
            }
        }
        
        List<EventDay> eventDays = new ArrayList<>();
        for (String key : dateMap.keySet()) {
            int[] types = dateMap.get(key);
            int drawableRes;
            if (types[0] == 1 && types[1] == 1) {
                drawableRes = R.drawable.dot_multiple; // Ambos tipos
            } else if (types[0] == 1) {
                drawableRes = R.drawable.dot_annual; // Solo anual
            } else {
                drawableRes = R.drawable.dot_single; // Solo unico
            }
            eventDays.add(new EventDay(calMap.get(key), drawableRes));
        }
        calendarView.setEvents(eventDays);
    }

    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    deleteEvent(event);
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void deleteEvent(Event event) {
        allEvents.remove(event);
        repository.saveEvents(allEvents);
        cancelNotification(event);
        loadEvents();
    }

    private void editEvent(Event event) {
        Intent intent = new Intent(this, EventFormActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    private void cancelNotification(Event event) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        int requestCode = event.getId().hashCode();

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, flags);
        alarmManager.cancel(pendingIntent);
    }
}
