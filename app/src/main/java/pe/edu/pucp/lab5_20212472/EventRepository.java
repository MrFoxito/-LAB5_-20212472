package pe.edu.pucp.lab5_20212472;

import android.content.Context;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventRepository {
    private static final String FILE_NAME = "eventsJson.json";
    private Context context;

    public EventRepository(Context context) {
        this.context = context;
    }

    public void saveEvents(List<Event> events) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(events);
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             FileWriter fw = new FileWriter(fos.getFD())) {
            fw.write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Event> getEvents() {
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             FileReader fr = new FileReader(fis.getFD());
             BufferedReader br = new BufferedReader(fr)) {
            String jsonStr = br.readLine();
            if (jsonStr != null) {
                Gson gson = new Gson();
                Event[] eventArray = gson.fromJson(jsonStr, Event[].class);
                return new ArrayList<>(Arrays.asList(eventArray));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
