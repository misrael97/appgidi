package com.example.appgidi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.appgidi.R;
import com.example.appgidi.models.TeacherSubjectGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TeacherGroupAdapter extends RecyclerView.Adapter<TeacherGroupAdapter.ViewHolder> {
    private List<TeacherSubjectGroup> lista;

    public TeacherGroupAdapter(List<TeacherSubjectGroup> lista) {
        this.lista = lista;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMateria, tvHorario, tvProfesor;

        public ViewHolder(View view) {
            super(view);
            tvMateria = view.findViewById(R.id.tvMateria);
            tvHorario = view.findViewById(R.id.tvHorario);
            tvProfesor = view.findViewById(R.id.tvProfesor);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.materias_scroll, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TeacherSubjectGroup item = lista.get(position);

        // Materia
        if (item.getSubjects() != null && item.getSubjects().getName() != null) {
            holder.tvMateria.setText(item.getSubjects().getName());
        } else {
            holder.tvMateria.setText("Sin materia");
        }

        // Horario - conversión de fecha ISO a solo hora en formato 12h
        if (item.getSchedules() != null && item.getSchedules().getStart_time() != null && item.getSchedules().getEnd_time() != null) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));  // 👈 importante: mantener UTC para que no ajuste

                Date startDate = isoFormat.parse(item.getSchedules().getStart_time());
                Date endDate = isoFormat.parse(item.getSchedules().getEnd_time());

                SimpleDateFormat hourFormat = new SimpleDateFormat("h:mma", Locale.getDefault());
                hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));  // 👈 mostrar hora en UTC (igual a la que viene en JSON)


                assert startDate != null;
                String horario = hourFormat.format(startDate).toLowerCase() + " - " + hourFormat.format(endDate).toLowerCase();
                holder.tvHorario.setText(horario);
            } catch (ParseException e) {
                holder.tvHorario.setText("Horario no disponible");
                e.printStackTrace();
            }
        } else {
            holder.tvHorario.setText("Horario no disponible");
        }

        // Profesor
        if (item.getUsers() != null) {
            String nombre = item.getUsers().getFullName();
            holder.tvProfesor.setText(nombre != null ? nombre : "Sin nombre");
        } else {
            holder.tvProfesor.setText("Sin profesor");
        }
    }


    @Override
    public int getItemCount() {
        return lista.size();
    }
    public void setLista(List<TeacherSubjectGroup> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged(); // Recarga el RecyclerView
    }
}
