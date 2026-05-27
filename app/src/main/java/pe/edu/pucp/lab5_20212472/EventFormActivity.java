package pe.edu.pucp.lab5_20212472;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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
    private boolean dateSelected = false;
    private boolean isEditMode = false;
    private String editEventId = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

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

        // Por defecto seleccionar '1 día antes'
        spinnerNotification.setSelection(1);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveEvent());

        // Verificar si es modo edición
        if (getIntent().hasExtra("event_id")) {
            editEventId = getIntent().getStringExtra("event_id");
            isEditMode = true;
            setTitle(getString(R.string.edit_event));
            loadEventData();
        }
    }

    private void loadEventData() {
        List<Event> events = repository.getEvents();
        for (Event event : events) {
            if (event.getId().equals(editEventId)) {
                etName.setText(event.getName());
                selectedCalendar.setTimeInMillis(event.getDateInMillis());
                dateSelected = true;

                SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE"));
                btnDate.setText(sdfDate.format(selectedCalendar.getTime()));

                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", new Locale("es", "PE"));
                btnTime.setText(sdfTime.format(selectedCalendar.getTime()));

                // Seleccionar periodicidad
                String[] periodicityOptions = getResources().getStringArray(R.array.periodicity_array);
                for (int i = 0; i < periodicityOptions.length; i++) {
                    if (periodicityOptions[i].equals(event.getPeriodicity())) {
                        spinnerPeriodicity.setSelection(i);
                        break;
                    }
                }

                // Seleccionar plazo de notificación
                String[] notificationOptions = getResources().getStringArray(R.array.notification_array);
                for (int i = 0; i < notificationOptions.length; i++) {
                    if (notificationOptions[i].equals(event.getNotificationDeadline())) {
                        spinnerNotification.setSelection(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            dateSelected = true;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE"));
            btnDate.setText(sdf.format(selectedCalendar.getTime()));
        }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedCalendar.set(Calendar.MINUTE, minute);
            selectedCalendar.set(Calendar.SECOND, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", new Locale("es", "PE"));
            btnTime.setText(sdf.format(selectedCalendar.getTime()));
        }, selectedCalendar.get(Calendar.HOUR_OF_DAY), selectedCalendar.get(Calendar.MINUTE), true).show();
    }

    private void saveEvent() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(this, "Debe ingresar un nombre", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dateSelected && !isEditMode) {
            Toast.makeText(this, "Debe seleccionar una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        String periodicity = spinnerPeriodicity.getSelectedItem().toString();
        String notificationOption = spinnerNotification.getSelectedItem().toString();

        List<Event> events = repository.getEvents();

        if (isEditMode) {
            // Modo edición: buscar y actualizar evento existente
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId().equals(editEventId)) {
                    Event existing = events.get(i);
                    // Cancelar notificación anterior
                    cancelNotification(existing);

                    existing.setName(name);
                    existing.setDateInMillis(selectedCalendar.getTimeInMillis());
                    existing.setPeriodicity(periodicity);
                    existing.setNotificationDeadline(notificationOption);
                    events.set(i, existing);
                    repository.saveEvents(events);
                    // Programar nueva notificación
                    scheduleNotification(existing);
                    Toast.makeText(this, getString(R.string.event_updated), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        } else {
            // Modo creación
            Event newEvent = new Event(name, selectedCalendar.getTimeInMillis(), periodicity, notificationOption);
            events.add(newEvent);
            repository.saveEvents(events);
            scheduleNotification(newEvent);
            Toast.makeText(this, getString(R.string.event_saved), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void scheduleNotification(Event event) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_EVENT_NAME, event.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "PE"));
        intent.putExtra(NotificationReceiver.EXTRA_EVENT_DATE, sdf.format(event.getDateInMillis()));
        intent.putExtra(NotificationReceiver.EXTRA_PERIODICITY, event.getPeriodicity());

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
                break;
        }
        return cal.getTimeInMillis();
    }
}
