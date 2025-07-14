package com.example.appgidi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgidi.R;
import com.example.appgidi.models.Grade;

import java.util.List;
import java.util.Map;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.ViewHolder> {

    private List<Grade> gradeList;

    public GradeAdapter(List<Grade> gradeList) {
        this.gradeList = gradeList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnidad, tvCalificacion, tvProfesor;

        public ViewHolder(View view) {
            super(view);
            tvUnidad = view.findViewById(R.id.tvUnidad);
            tvCalificacion = view.findViewById(R.id.tvCalificacion);
            tvProfesor = view.findViewById(R.id.tvProfesor);
        }
    }

    @Override
    public GradeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calificacion_scroll, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Grade grade = gradeList.get(position);

        // Mostrar nombre del profesor desde Map
        if (grade.getTeacher() != null) {
            Map<String, Object> teacher = grade.getTeacher();
            String nombre = teacher.get("first_name") + " " + teacher.get("last_name");
            holder.tvProfesor.setText(nombre);
        } else {
            holder.tvProfesor.setText("Sin profesor");
        }

        // Unidad
        holder.tvUnidad.setText(String.valueOf(grade.getUnitNumber()));

        // Calificación
        holder.tvCalificacion.setText(String.valueOf(grade.getGrade()));
    }

    @Override
    public int getItemCount() {
        return gradeList.size();
    }

    public void actualizarLista(List<Grade> nuevas) {
        this.gradeList = nuevas;
        notifyDataSetChanged();
    }
}
