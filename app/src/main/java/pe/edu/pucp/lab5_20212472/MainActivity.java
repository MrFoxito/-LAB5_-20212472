package pe.edu.pucp.lab5_20212472;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private TextView tvNoEvents;
    private CalendarView calendarView;
    private FloatingActionButton fabAddEvent;
    private EventRepository repository;
    private List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        NotificationReceiver.createNotificationChannels(this);

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
        
        adapter = new EventAdapter(new ArrayList<>(), this::deleteEvent);
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
            
            // Default filter to today
            Calendar today = Calendar.getInstance();
            filterEventsFromDate(today);
        }
    }

    private void filterEventsFromDate(Calendar selectedDate) {
        if(allEvents == null || allEvents.isEmpty()) return;
        
        List<Event> filtered = new ArrayList<>();
        
        // Reset selectedDate to start of day
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
    }

    private void updateCalendar() {
        List<EventDay> eventDays = new ArrayList<>();
        for (Event event : allEvents) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(event.getDateInMillis());
            
            int colorResource = event.getPeriodicity().equals("Anual") ? 
                    pe.edu.pucp.lab5_20212472.R.color.annualEventColor : 
                    pe.edu.pucp.lab5_20212472.R.color.singleEventColor;
                    
            int colorInt = androidx.core.content.ContextCompat.getColor(this, colorResource);
            eventDays.add(new EventDay(calendar, pe.edu.pucp.lab5_20212472.R.drawable.baseline_circle_24, colorInt));
        }
        calendarView.setEvents(eventDays);
    }

    private void deleteEvent(Event event) {
        allEvents.remove(event);
        repository.saveEvents(allEvents);
        cancelNotification(event);
        loadEvents();
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
