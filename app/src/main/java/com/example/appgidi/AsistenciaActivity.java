package com.example.appgidi;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AsistenciaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistencia);

        setupBottomNav(R.id.nav_asistencias);
    }

    private void setupBottomNav(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == selectedItemId) return true;

            if (id == R.id.nav_horarios) {
                startActivity(new Intent(this, HorarioActivity.class));
            } else if (id == R.id.nav_asistencias) {
                startActivity(new Intent(this, AsistenciaActivity.class));
            } else if (id == R.id.nav_calificaciones) {
                startActivity(new Intent(this, CalificacionesActivity.class));
            }

            finish(); // Finaliza la actual para evitar stack de Activities
            return true;
        });
    }

}
