package pe.edu.pucp.lab5_20212472;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EventRepository {
    private static final String PREF_NAME = "event_prefs";
    private static final String KEY_EVENTS = "events_list";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public EventRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveEvents(List<Event> events) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String jsonStr = gson.toJson(events);
        editor.putString(KEY_EVENTS, jsonStr);
        editor.apply();
    }

    public List<Event> getEvents() {
        String jsonStr = sharedPreferences.getString(KEY_EVENTS, null);
        if (jsonStr != null) {
            Type type = new TypeToken<ArrayList<Event>>(){}.getType();
            return gson.fromJson(jsonStr, type);
        }
        return new ArrayList<>();
    }
}
