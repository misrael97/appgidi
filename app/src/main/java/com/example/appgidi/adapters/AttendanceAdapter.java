package com.example.appgidi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgidi.R;
import com.example.appgidi.models.Attendance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
    private List<Attendance> lista;

    public AttendanceAdapter(List<Attendance> lista) {
        this.lista = lista;
    }

    public void actualizarLista(List<Attendance> nuevasAsistencias) {
        this.lista = nuevasAsistencias;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.asistencia_scroll, parent, false);
        return new AttendanceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        Attendance a = lista.get(position);

        String fechaOriginal = a.getDate(); // formato: 2025-07-15T00:00:00.000Z
        String fechaFormateada = "Sin fecha";

        if (fechaOriginal != null) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date fecha = isoFormat.parse(fechaOriginal);

                SimpleDateFormat nuevoFormato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                fechaFormateada = nuevoFormato.format(fecha);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        holder.tvFecha.setText(fechaFormateada);

        String estadoOriginal = a.getStatus() != null ? a.getStatus() : "Sin estado";
        String estadoTraducido;

        switch (estadoOriginal.toLowerCase()) {
            case "present":
                estadoTraducido = "Presente";
                holder.tvEstado.setTextColor(0xFF2E7D32); // verde
                break;
            case "late":
                estadoTraducido = "Tarde";
                holder.tvEstado.setTextColor(0xFFFF9800); // naranja
                break;
            case "absent":
                estadoTraducido = "Ausente";
                holder.tvEstado.setTextColor(0xFFD32F2F); // rojo
                break;
            default:
                estadoTraducido = "Sin estado";
                holder.tvEstado.setTextColor(0xFF000000); // negro
        }

        holder.tvEstado.setText(estadoTraducido);
    }


    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvEstado;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
