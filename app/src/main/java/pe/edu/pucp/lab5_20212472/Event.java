package pe.edu.pucp.lab5_20212472;

import java.io.Serializable;
import java.util.UUID;

public class Event implements Serializable {
    private String id;
    private String name;
    private long dateInMillis;
    private String periodicity;
    private String notificationDeadline;
    
    public Event() {
        this.id = UUID.randomUUID().toString();
    }

    public Event(String name, long dateInMillis, String periodicity, String notificationDeadline) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.dateInMillis = dateInMillis;
        this.periodicity = periodicity;
        this.notificationDeadline = notificationDeadline;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public long getDateInMillis() { return dateInMillis; }
    public void setDateInMillis(long dateInMillis) { this.dateInMillis = dateInMillis; }
    
    public String getPeriodicity() { return periodicity; }
    public void setPeriodicity(String periodicity) { this.periodicity = periodicity; }
    
    public String getNotificationDeadline() { return notificationDeadline; }
    public void setNotificationDeadline(String notificationDeadline) { this.notificationDeadline = notificationDeadline; }
}
