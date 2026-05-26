package pe.edu.pucp.lab5_20212472;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventFormActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private Button btnDate, btnTime, btnSave;
    private Spinner spinnerPeriodicity, spinnerNotification;
    private Calendar selectedCalendar;
    private EventRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        repository = new EventRepository(this);
        selectedCalendar = Calendar.getInstance();

        etName = findViewById(R.id.etName);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        spinnerPeriodicity = findViewById(R.id.spinnerPeriodicity);
        spinnerNotification = findViewById(R.id.spinnerNotification);
        btnSave = findViewById(R.id.btnSave);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            btnDate.setText(sdf.format(selectedCalendar.getTime()));
        }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedCalendar.set(Calendar.MINUTE, minute);
            selectedCalendar.set(Calendar.SECOND, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            btnTime.setText(sdf.format(selectedCalendar.getTime()));
        }, selectedCalendar.get(Calendar.HOUR_OF_DAY), selectedCalendar.get(Calendar.MINUTE), true).show();
    }

    private void saveEvent() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(this, "Debe ingresar un nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        String periodicity = spinnerPeriodicity.getSelectedItem().toString();
        String notificationOption = spinnerNotification.getSelectedItem().toString();

        Event newEvent = new Event(name, selectedCalendar.getTimeInMillis(), periodicity, notificationOption);
        
        List<Event> events = repository.getEvents();
        events.add(newEvent);
        repository.saveEvents(events);

        scheduleNotification(newEvent);

        Toast.makeText(this, "Evento guardado", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scheduleNotification(Event event) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_EVENT_NAME, event.getName());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        intent.putExtra(NotificationReceiver.EXTRA_EVENT_DATE, sdf.format(event.getDateInMillis()));
        intent.putExtra(NotificationReceiver.EXTRA_PERIODICITY, event.getPeriodicity());

        // Use event ID hash as request code to allow multiple distinct alarms
        int requestCode = event.getId().hashCode();
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, flags);

        long timeToNotify = calculateNotificationTime(event.getDateInMillis(), event.getNotificationDeadline());
        
        if (timeToNotify > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeToNotify, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeToNotify, pendingIntent);
            }
        }
    }

    private long calculateNotificationTime(long eventTime, String deadlineOption) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(eventTime);
        switch (deadlineOption) {
            case "1 día antes":
                cal.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "3 días antes":
                cal.add(Calendar.DAY_OF_YEAR, -3);
                break;
            case "1 semana antes":
                cal.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case "Mismo día":
            default:
                // For "Mismo día", let's notify at exactly the event time or default morning if no time provided.
                // Assuming the time was set or defaults to midnight. We'll just use the event time.
                break;
        }
        return cal.getTimeInMillis();
    }
}
