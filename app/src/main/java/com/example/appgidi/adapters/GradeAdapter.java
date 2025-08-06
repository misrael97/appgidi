package com.example.appgidi.adapters;

import android.util.Log;
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
        
        Log.d("GradeAdapter", "=== MOSTRANDO CALIFICACIÓN ===");
        Log.d("GradeAdapter", "Posición: " + position);
        Log.d("GradeAdapter", "Materia: " + (grade.getSubject() != null ? grade.getSubject().getName() : "NULL"));
        Log.d("GradeAdapter", "Unidad: " + grade.getUnitNumber());
        Log.d("GradeAdapter", "Calificación: " + grade.getGrade());

        // Mostrar nombre del profesor desde Map con manejo de nulos
        if (grade.getTeacher() != null) {
            Map<String, Object> teacher = grade.getTeacher();
            String firstName = teacher.get("first_name") != null ? teacher.get("first_name").toString() : "";
            String lastName = teacher.get("last_name") != null ? teacher.get("last_name").toString() : "";
            String nombre = (firstName + " " + lastName).trim();
            holder.tvProfesor.setText(nombre.isEmpty() ? "Sin profesor" : nombre);
            Log.d("GradeAdapter", "Profesor: " + nombre);
        } else {
            holder.tvProfesor.setText("Sin profesor");
            Log.d("GradeAdapter", "Profesor: NULL");
        }

        // Unidad
        holder.tvUnidad.setText(String.valueOf(grade.getUnitNumber()));

        // Calificación
        holder.tvCalificacion.setText(String.valueOf(grade.getGrade()));
        
        Log.d("GradeAdapter", "=== FIN MOSTRANDO CALIFICACIÓN ===");
    }

    @Override
    public int getItemCount() {
        return gradeList != null ? gradeList.size() : 0;
    }

    public void actualizarLista(List<Grade> nuevas) {
        Log.d("GradeAdapter", "=== ACTUALIZANDO LISTA ===");
        Log.d("GradeAdapter", "Nuevas calificaciones: " + nuevas.size());
        for (int i = 0; i < nuevas.size(); i++) {
            Grade g = nuevas.get(i);
            Log.d("GradeAdapter", "Calificación " + (i+1) + ": " + 
                  (g.getSubject() != null ? g.getSubject().getName() : "NULL") + 
                  " - Unidad " + g.getUnitNumber() + " - Calificación " + g.getGrade());
        }
        
        this.gradeList = nuevas;
        notifyDataSetChanged();
        Log.d("GradeAdapter", "=== LISTA ACTUALIZADA ===");
    }
}
