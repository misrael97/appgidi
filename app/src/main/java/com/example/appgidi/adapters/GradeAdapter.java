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

public class GradeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_GRADE = 0;
    private static final int VIEW_TYPE_AVERAGE = 1;
    
    private List<Grade> gradeList;
    private boolean showAverage = false;

    public GradeAdapter(List<Grade> gradeList) {
        this.gradeList = gradeList;
    }

    public static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnidad, tvCalificacion, tvProfesor;

        public GradeViewHolder(View view) {
            super(view);
            tvUnidad = view.findViewById(R.id.tvUnidad);
            tvCalificacion = view.findViewById(R.id.tvCalificacion);
            tvProfesor = view.findViewById(R.id.tvProfesor);
        }
    }

    public static class AverageViewHolder extends RecyclerView.ViewHolder {
        TextView tvPromedio;

        public AverageViewHolder(View view) {
            super(view);
            tvPromedio = view.findViewById(R.id.tvPromedio);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AVERAGE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calificacion_promedio, parent, false);
            return new AverageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calificacion_scroll, parent, false);
            return new GradeViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showAverage && position == gradeList.size()) {
            return VIEW_TYPE_AVERAGE;
        }
        return VIEW_TYPE_GRADE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GradeViewHolder) {
            GradeViewHolder gradeHolder = (GradeViewHolder) holder;
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
                gradeHolder.tvProfesor.setText(nombre.isEmpty() ? "Sin profesor" : nombre);
                Log.d("GradeAdapter", "Profesor: " + nombre);
            } else {
                gradeHolder.tvProfesor.setText("Sin profesor");
                Log.d("GradeAdapter", "Profesor: NULL");
            }

            // Unidad
            gradeHolder.tvUnidad.setText(String.valueOf(grade.getUnitNumber()));

            // Calificación
            gradeHolder.tvCalificacion.setText(String.valueOf(grade.getGrade()));
            
            Log.d("GradeAdapter", "=== FIN MOSTRANDO CALIFICACIÓN ===");
        } else if (holder instanceof AverageViewHolder) {
            AverageViewHolder averageHolder = (AverageViewHolder) holder;
            double promedio = calcularPromedio();
            averageHolder.tvPromedio.setText(String.format("%.2f", promedio));
            Log.d("GradeAdapter", "=== MOSTRANDO PROMEDIO ===");
            Log.d("GradeAdapter", "Promedio calculado: " + promedio);
        }
    }

    private double calcularPromedio() {
        if (gradeList.isEmpty()) {
            return 0.0;
        }
        
        double suma = 0.0;
        for (Grade grade : gradeList) {
            suma += grade.getGrade();
        }
        return suma / gradeList.size();
    }

    @Override
    public int getItemCount() {
        int count = gradeList != null ? gradeList.size() : 0;
        if (showAverage && count > 0) {
            count += 1; // Agregar una posición extra para el promedio
        }
        return count;
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
        this.showAverage = !nuevas.isEmpty(); // Mostrar promedio solo si hay calificaciones
        notifyDataSetChanged();
        Log.d("GradeAdapter", "=== LISTA ACTUALIZADA ===");
    }
}
