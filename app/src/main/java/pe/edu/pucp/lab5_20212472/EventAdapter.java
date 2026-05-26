package pe.edu.pucp.lab5_20212472;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventDeleteListener deleteListener;

    public interface OnEventDeleteListener {
        void onDelete(Event event);
    }

    public EventAdapter(List<Event> eventList, OnEventDeleteListener deleteListener) {
        this.eventList = eventList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvName.setText(event.getName());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(event.getDateInMillis())));

        long diff = event.getDateInMillis() - System.currentTimeMillis();
        if(diff < 0) {
            holder.tvTimeRemaining.setText("El evento ya ocurrió");
        } else {
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff / (1000 * 60 * 60)) % 24;
            holder.tvTimeRemaining.setText("Faltan: " + days + " días y " + hours + " horas");
        }

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
    
    public void updateList(List<Event> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvTimeRemaining;
        ImageButton btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvTimeRemaining = itemView.findViewById(R.id.tvTimeRemaining);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
