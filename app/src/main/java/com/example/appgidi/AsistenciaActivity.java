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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.example.appgidi.utils.NetworkUtils;
import com.example.appgidi.utils.SessionManager;
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
    private SwipeRefreshLayout swipeRefreshLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_asistencia);
        
        Log.d("AsistenciaActivity", "=== ACTIVIDAD INICIADA ===");
        
        // Inicializar SessionManager
        sessionManager = new SessionManager(this);
        
        ImageView iconAjustes = findViewById(R.id.iconAjustesAsis);
        spinnerMes = findViewById(R.id.spinnerMes);
        spinnerMateria = findViewById(R.id.spinnerMateria);
        recyclerView = findViewById(R.id.recyclerAsistencia);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshAsistencia);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        Log.d("AsistenciaActivity", "Iniciando carga de meses y materias");
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

        swipeRefreshLayout.setOnRefreshListener(() -> {
            cargarMaterias();
            swipeRefreshLayout.setRefreshing(false);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutAsistencia), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cerrarSesion() {
        // Usar SessionManager para cerrar sesión
        sessionManager.logout();

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
        // Verificar conectividad de red
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: Sin conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int userId = sessionManager.getUserId();
        String token = sessionManager.getToken();
        String authHeader = "Bearer " + token;

        if (token == null || userId == -1) {
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("AsistenciaActivity", "=== INICIANDO CARGA DE MATERIAS ===");
        Log.d("AsistenciaActivity", "UserID: " + userId);
        Log.d("AsistenciaActivity", "Token: " + (token != null ? "Presente" : "Ausente"));

        // Obtener materias desde las calificaciones del estudiante
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAllStudentGrades(userId, authHeader).enqueue(new Callback<GradesResponse>() {
            @Override
            public void onResponse(Call<GradesResponse> call, Response<GradesResponse> response) {
                Log.d("AsistenciaActivity", "=== RESPUESTA API ASISTENCIA ===");
                Log.d("AsistenciaActivity", "Código: " + response.code());
                Log.d("AsistenciaActivity", "¿Exitoso?: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Grade> grades = response.body().getData().getGrades();
                    Log.d("AsistenciaActivity", "Total de calificaciones recibidas: " + grades.size());
                    
                    for (int i = 0; i < grades.size(); i++) {
                        Grade g = grades.get(i);
                        Log.d("AsistenciaActivity", "Calificación " + (i+1) + ": " + 
                              (g.getSubject() != null ? g.getSubject().getName() : "NULL") + 
                              " (ID: " + (g.getSubject() != null ? g.getSubject().getId() : "NULL") + ")");
                    }
                    
                    List<Subject> materiasUnicas = new ArrayList<>();
                    Set<String> nombres = new HashSet<>();

                    for (Grade g : grades) {
                        Subject s = g.getSubject();
                        if (!nombres.contains(s.getName())) {
                            materiasUnicas.add(s);
                            nombres.add(s.getName());
                        }
                    }

                    materias.clear();
                    materias.addAll(materiasUnicas);

                    Log.d("AsistenciaActivity", "=== MATERIAS ÚNICAS ENCONTRADAS ===");
                    Log.d("AsistenciaActivity", "Total de materias únicas: " + materias.size());
                    for (Subject materia : materias) {
                        Log.d("AsistenciaActivity", "Materia: " + materia.getName() + " (ID: " + materia.getId() + ")");
                    }

                    List<String> opciones = new ArrayList<>();
                    opciones.add("Materia");
                    for (Subject s : materias) opciones.add(s.getName());

                    ArrayAdapter<String> adapterMateria = new ArrayAdapter<>(AsistenciaActivity.this, android.R.layout.simple_spinner_dropdown_item, opciones);
                    spinnerMateria.setAdapter(adapterMateria);
                    spinnerMateria.setSelection(0);

                    // Log para debugging
                    Log.d("AsistenciaActivity", "Materias cargadas: " + materias.size());
                    for (Subject materia : materias) {
                        Log.d("AsistenciaActivity", "Materia: " + materia.getName() + " (ID: " + materia.getId() + ")");
                    }

                    spinnerMateria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("AsistenciaActivity", "=== SPINNER MATERIA SELECCIONADO ===");
                            Log.d("AsistenciaActivity", "Posición: " + position);
                            
                            if (position == 0) {
                                Log.d("AsistenciaActivity", "Seleccionado placeholder - Limpiando lista");
                                adapter.actualizarLista(new ArrayList<>());
                                return;
                            }
                            selectedSubjectId = materias.get(position - 1).getId();
                            Log.d("AsistenciaActivity", "Materia seleccionada: " + materias.get(position - 1).getName() + " (ID: " + selectedSubjectId + ")");
                            
                            if (selectedMes > 0) {
                                Log.d("AsistenciaActivity", "Mes y materia seleccionados - Obteniendo asistencias");
                                obtenerAsistencias();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    Log.e("AsistenciaActivity", "Error en respuesta: " + response.code());
                    Toast.makeText(AsistenciaActivity.this, "No se pudieron cargar materias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GradesResponse> call, Throwable t) {
                Log.e("AsistenciaActivity", "Error de red: " + t.getMessage());
                Toast.makeText(AsistenciaActivity.this, "Error al obtener materias", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void obtenerAsistencias() {
        // Verificar conectividad de red
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: Sin conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int userId = sessionManager.getUserId();
        String token = sessionManager.getToken();
        String authHeader = "Bearer " + token;
        int year = Calendar.getInstance().get(Calendar.YEAR);

        if (token == null || userId == -1) {
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("AsistenciaActivity", "=== OBTENIENDO ASISTENCIAS ===");
        Log.d("AsistenciaActivity", "UserID: " + userId);
        Log.d("AsistenciaActivity", "Mes: " + selectedMes);
        Log.d("AsistenciaActivity", "Año: " + year);
        Log.d("AsistenciaActivity", "SubjectID: " + selectedSubjectId);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAttendanceFiltered(userId, selectedMes, year, selectedSubjectId, authHeader).enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                Log.d("AsistenciaActivity", "=== RESPUESTA ASISTENCIA ===");
                Log.d("AsistenciaActivity", "Código: " + response.code());
                Log.d("AsistenciaActivity", "¿Exitoso?: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Attendance> asistencias = response.body().getData();
                    Log.d("AsistenciaActivity", "Total de asistencias recibidas: " + asistencias.size());
                    
                    for (int i = 0; i < asistencias.size(); i++) {
                        Attendance a = asistencias.get(i);
                        Log.d("AsistenciaActivity", "Asistencia " + (i+1) + ": " + 
                              "SubjectID: " + a.getSubject_id() + 
                              " - Fecha: " + a.getDate() + " - Estado: " + a.getStatus());
                    }
                    
                    adapter.actualizarLista(asistencias);
                    
                    // Log para debugging
                    Log.d("AsistenciaActivity", "Asistencias cargadas: " + asistencias.size());
                } else {
                    Log.e("AsistenciaActivity", "Error en respuesta: " + response.code());
                    adapter.actualizarLista(new ArrayList<>());
                    Toast.makeText(AsistenciaActivity.this, "Sin asistencias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                Log.e("AsistenciaActivity", "Error de red: " + t.getMessage());
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
