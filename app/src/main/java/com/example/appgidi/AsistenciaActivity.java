package com.example.appgidi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgidi.adapters.AttendanceAdapter;
import com.example.appgidi.models.Attendance;
import com.example.appgidi.models.AttendanceResponse;
import com.example.appgidi.models.Grade;
import com.example.appgidi.models.GradesResponse;
import com.example.appgidi.models.GroupDataResponse;
import com.example.appgidi.models.Subject;
import com.example.appgidi.models.TeacherSubjectGroup;
import com.example.appgidi.network.ApiClient;
import com.example.appgidi.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AsistenciaActivity extends AppCompatActivity {

    private Spinner spinnerMes, spinnerMateria;
    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;
    private List<Subject> materias = new ArrayList<>();
    private int selectedMes = 0, selectedSubjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistencia);
        ImageView iconAjustes = findViewById(R.id.iconAjustesAsis);
        spinnerMes = findViewById(R.id.spinnerMes);
        spinnerMateria = findViewById(R.id.spinnerMateria);
        recyclerView = findViewById(R.id.recyclerAsistencia);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        cargarMeses();
        cargarMaterias();
        setupBottomNav(R.id.nav_asistencias);

        iconAjustes.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(AsistenciaActivity.this, view);
            popup.getMenuInflater().inflate(R.menu.menu_ajustes, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_cerrar_sesion) {
                    cerrarSesion();
                    return true;
                }
                return false;
            });

            popup.show();
        });


    }
    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void cargarMeses() {
        List<String> meses = Arrays.asList("Mes", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        ArrayAdapter<String> adapterMes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, meses);
        spinnerMes.setAdapter(adapterMes);

        spinnerMes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMes = position;
                if (selectedMes > 0 && selectedSubjectId > 0) {
                    obtenerAsistencias();
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarMaterias() {
        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String token = prefs.getString("jwt_token", null);
        String authHeader = "Bearer " + token;

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getGradesForStudent(userId, authHeader).enqueue(new Callback<GradesResponse>() {
            @Override
            public void onResponse(Call<GradesResponse> call, Response<GradesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subject> materiasUnicas = new ArrayList<>();
                    Set<String> nombres = new HashSet<>();

                    for (Grade g : response.body().getData().getGrades()) {
                        Subject s = g.getSubject();
                        if (!nombres.contains(s.getName())) {
                            materiasUnicas.add(s);
                            nombres.add(s.getName());
                        }
                    }

                    materias.clear();
                    materias.addAll(materiasUnicas);

                    List<String> opciones = new ArrayList<>();
                    opciones.add("Materia");
                    for (Subject s : materias) opciones.add(s.getName());

                    ArrayAdapter<String> adapterMateria = new ArrayAdapter<>(AsistenciaActivity.this, android.R.layout.simple_spinner_dropdown_item, opciones);
                    spinnerMateria.setAdapter(adapterMateria);
                    spinnerMateria.setSelection(0);

                    spinnerMateria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                adapter.actualizarLista(new ArrayList<>());
                                return;
                            }
                            selectedSubjectId = materias.get(position - 1).getId();
                            if (selectedMes > 0) {
                                obtenerAsistencias();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    Toast.makeText(AsistenciaActivity.this, "No se pudieron cargar materias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GradesResponse> call, Throwable t) {
                Toast.makeText(AsistenciaActivity.this, "Error al obtener materias", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void obtenerAsistencias() {
        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String token = prefs.getString("jwt_token", null);
        String authHeader = "Bearer " + token;
        int year = Calendar.getInstance().get(Calendar.YEAR);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAttendanceFiltered(userId, selectedMes, year, selectedSubjectId, authHeader).enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Attendance> asistencias = response.body().getData();
                    adapter.actualizarLista(asistencias);
                } else {
                    adapter.actualizarLista(new ArrayList<>());
                    Toast.makeText(AsistenciaActivity.this, "Sin asistencias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                Toast.makeText(AsistenciaActivity.this, "Error al obtener asistencias: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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

            finish();
            return true;
        });
    }
}
