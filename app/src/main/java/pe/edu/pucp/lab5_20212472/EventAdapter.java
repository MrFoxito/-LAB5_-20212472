package pe.edu.pucp.lab5_20212472;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventDeleteListener deleteListener;
    private OnEventEditListener editListener;

    public interface OnEventDeleteListener {
        void onDelete(Event event);
    }

    public interface OnEventEditListener {
        void onEdit(Event event);
    }

    public EventAdapter(List<Event> eventList, OnEventDeleteListener deleteListener, OnEventEditListener editListener) {
        this.eventList = eventList;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
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

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "PE"));
        holder.tvDate.setText(sdf.format(new Date(event.getDateInMillis())));

        // Mostrar periodicidad
        holder.tvPeriodicity.setText(event.getPeriodicity());

        // Tiempo restante
        long diff = event.getDateInMillis() - System.currentTimeMillis();
        if (diff < 0) {
            holder.tvTimeRemaining.setText("El evento ya ocurrió");
        } else {
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff / (1000 * 60 * 60)) % 24;
            holder.tvTimeRemaining.setText("Faltan: " + days + " días y " + hours + " horas");
        }

        // Color indicator: verde para anual, azul para unico
        boolean isAnnual = event.getPeriodicity().equals("Anual");
        int color = ContextCompat.getColor(holder.itemView.getContext(),
                isAnnual ? R.color.annualEventColor : R.color.singleEventColor);
        holder.colorIndicator.setBackgroundColor(color);

        // Colorear también el nombre del evento según periodicidad
        holder.tvName.setTextColor(color);

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(event));
        holder.btnEdit.setOnClickListener(v -> editListener.onEdit(event));
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
        TextView tvName, tvDate, tvPeriodicity, tvTimeRemaining;
        ImageButton btnDelete, btnEdit;
        View colorIndicator;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvPeriodicity = itemView.findViewById(R.id.tvPeriodicity);
            tvTimeRemaining = itemView.findViewById(R.id.tvTimeRemaining);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }
    }
}
