package com.example.appgidi;

import android.content.Intent;
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

import com.example.appgidi.adapters.GradeAdapter;
import com.example.appgidi.models.Grade;
import com.example.appgidi.models.GradesResponse;
import com.example.appgidi.models.Subject;
import com.example.appgidi.network.ApiClient;
import com.example.appgidi.network.ApiService;
import com.example.appgidi.utils.NetworkUtils;
import com.example.appgidi.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.appgidi.models.GroupDataResponse;
import com.example.appgidi.models.TeacherSubjectGroup;


public class CalificacionesActivity extends AppCompatActivity {

    private Spinner spinnerMateria;
    private RecyclerView recyclerView;
    private GradeAdapter adapter;
    private List<Grade> todasLasCalificaciones = new ArrayList<>();
    private List<Subject> materias = new ArrayList<>();
    private ArrayAdapter<Subject> spinnerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calificaciones);
        
        Log.d("CalificacionesActivity", "=== ACTIVIDAD INICIADA ===");
        
        // Inicializar SessionManager
        sessionManager = new SessionManager(this);
        
        ImageView iconAjustes = findViewById(R.id.iconAjustesCal);
        spinnerMateria = findViewById(R.id.spinnerMateria);
        recyclerView = findViewById(R.id.recyclerCalificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GradeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshCalificaciones);
        
        Log.d("CalificacionesActivity", "Iniciando llamada a obtenerCalificacionesDesdeAPI()");
        obtenerCalificacionesDesdeAPI();
        setupBottomNav(R.id.nav_calificaciones);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            obtenerCalificacionesDesdeAPI();
            swipeRefreshLayout.setRefreshing(false);
        });

        iconAjustes.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(CalificacionesActivity.this, view);
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutCalificaciones), (v, insets) -> {
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


    private void obtenerCalificacionesDesdeAPI() {
        // Verificar conectividad de red
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: Sin conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int userId = sessionManager.getUserId();
        String token = sessionManager.getToken();
        
        if (token == null || userId == -1) {
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CalificacionesActivity", "Iniciando llamada a API - UserID: " + userId);
        Log.d("CalificacionesActivity", "Token: " + (token != null ? "Presente" : "Ausente"));

        // Obtener calificaciones del estudiante
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAllStudentGrades(userId, "Bearer " + token).enqueue(new Callback<GradesResponse>() {
            @Override
            public void onResponse(Call<GradesResponse> call, Response<GradesResponse> response) {
                Log.d("CalificacionesActivity", "Respuesta recibida - Código: " + response.code());
                
                // Verificar respuesta completa
                verificarRespuestaAPI(response);
                
                if (response.isSuccessful() && response.body() != null) {
                    todasLasCalificaciones = response.body().getData().getGrades();
                    materias = obtenerMateriasUnicas(todasLasCalificaciones);
                    
                    // Log detallado de los datos recibidos
                    Log.d("CalificacionesActivity", "=== DATOS RECIBIDOS DE LA API ===");
                    Log.d("CalificacionesActivity", "Total de calificaciones: " + todasLasCalificaciones.size());
                    
                    for (int i = 0; i < todasLasCalificaciones.size(); i++) {
                        Grade grade = todasLasCalificaciones.get(i);
                        Log.d("CalificacionesActivity", "Calificación " + (i+1) + ":");
                        Log.d("CalificacionesActivity", "  - Materia: " + (grade.getSubject() != null ? grade.getSubject().getName() : "NULL"));
                        Log.d("CalificacionesActivity", "  - Unidad: " + grade.getUnitNumber());
                        Log.d("CalificacionesActivity", "  - Calificación: " + grade.getGrade());
                        if (grade.getTeacher() != null) {
                            Log.d("CalificacionesActivity", "  - Profesor: " + grade.getTeacher().toString());
                        }
                    }
                    
                    Log.d("CalificacionesActivity", "=== MATERIAS ENCONTRADAS ===");
                    for (Subject materia : materias) {
                        Log.d("CalificacionesActivity", "Materia: " + materia.getName() + " (ID: " + materia.getId() + ")");
                    }
                    
                    configurarSpinner(materias);
                    
                    // Log para debugging
                    Log.d("CalificacionesActivity", "Calificaciones cargadas: " + todasLasCalificaciones.size());
                    Log.d("CalificacionesActivity", "Materias encontradas: " + materias.size());
                } else {
                    Log.e("CalificacionesActivity", "Error en respuesta: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("CalificacionesActivity", "Error body: " + errorBody);
                        } catch (IOException e) {
                            Log.e("CalificacionesActivity", "Error leyendo error body", e);
                        }
                    }
                    Toast.makeText(CalificacionesActivity.this, "Error al cargar calificaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GradesResponse> call, Throwable t) {
                Log.e("CalificacionesActivity", "Error de red: " + t.getMessage());
                Toast.makeText(CalificacionesActivity.this, "Error al cargar calificaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void verificarRespuestaAPI(Response<GradesResponse> response) {
        Log.d("CalificacionesActivity", "=== VERIFICACIÓN DE RESPUESTA API ===");
        Log.d("CalificacionesActivity", "Código de respuesta: " + response.code());
        Log.d("CalificacionesActivity", "¿Es exitosa?: " + response.isSuccessful());
        Log.d("CalificacionesActivity", "¿Tiene body?: " + (response.body() != null));
        
        if (response.body() != null) {
            GradesResponse gradesResponse = response.body();
            Log.d("CalificacionesActivity", "Status de respuesta: " + gradesResponse.getStatus());
            Log.d("CalificacionesActivity", "¿Tiene data?: " + (gradesResponse.getData() != null));
            
            if (gradesResponse.getData() != null) {
                Log.d("CalificacionesActivity", "Total de calificaciones en data: " + gradesResponse.getData().getGrades().size());
            }
        }
        
        // Verificar headers
        Log.d("CalificacionesActivity", "Headers de respuesta:");
        for (String headerName : response.headers().names()) {
            Log.d("CalificacionesActivity", headerName + ": " + response.headers().get(headerName));
        }
        
        Log.d("CalificacionesActivity", "=== FIN VERIFICACIÓN ===");
    }

    private List<Subject> obtenerMateriasUnicas(List<Grade> grades) {
        Map<Integer, Subject> mapa = new LinkedHashMap<>();
        for (Grade g : grades) {
            mapa.put(g.getSubject().getId(), g.getSubject());
        }
        return new ArrayList<>(mapa.values());
    }

    private void configurarSpinner(List<Subject> materias) {
        List<Subject> listaConPlaceholder = new ArrayList<>();
        Subject placeholder = new Subject() {
            @Override public String getName() { return "Materias"; }

            @Override public String toString() { return "Materias"; } // << MOSTRAR EN SPINNER
        };

        listaConPlaceholder.add(placeholder);
        listaConPlaceholder.addAll(materias);

        Log.d("CalificacionesActivity", "=== CONFIGURANDO SPINNER ===");
        Log.d("CalificacionesActivity", "Total de materias en spinner: " + listaConPlaceholder.size());
        for (int i = 0; i < listaConPlaceholder.size(); i++) {
            Subject s = listaConPlaceholder.get(i);
            Log.d("CalificacionesActivity", "Posición " + i + ": " + s.getName() + " (ID: " + s.getId() + ")");
        }

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaConPlaceholder);
        spinnerMateria.setAdapter(spinnerAdapter);

        spinnerMateria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("CalificacionesActivity", "=== SPINNER SELECCIONADO ===");
                Log.d("CalificacionesActivity", "Posición seleccionada: " + position);
                
                if (position == 0) {
                    Log.d("CalificacionesActivity", "Seleccionado placeholder - Limpiando lista");
                    adapter.actualizarLista(new ArrayList<>());
                    return;
                }
                
                Subject materiaSeleccionada = (Subject) parent.getItemAtPosition(position);
                Log.d("CalificacionesActivity", "Materia seleccionada: " + materiaSeleccionada.getName() + " (ID: " + materiaSeleccionada.getId() + ")");
                
                List<Grade> filtradas = new ArrayList<>();
                Log.d("CalificacionesActivity", "Filtrando calificaciones para materia: " + materiaSeleccionada.getName());
                
                for (Grade g : todasLasCalificaciones) {
                    Log.d("CalificacionesActivity", "Comparando: " + (g.getSubject() != null ? g.getSubject().getName() : "NULL") + 
                          " (ID: " + (g.getSubject() != null ? g.getSubject().getId() : "NULL") + ") vs " + 
                          materiaSeleccionada.getName() + " (ID: " + materiaSeleccionada.getId() + ")");
                    
                    if (g.getSubject() != null && g.getSubject().getId() == materiaSeleccionada.getId()) {
                        filtradas.add(g);
                        Log.d("CalificacionesActivity", "✓ Coincidencia encontrada - Unidad: " + g.getUnitNumber() + ", Calificación: " + g.getGrade());
                    }
                }
                
                // Ordenar calificaciones por unidad de manera ascendente
                filtradas.sort((g1, g2) -> Integer.compare(g1.getUnitNumber(), g2.getUnitNumber()));
                
                Log.d("CalificacionesActivity", "=== CALIFICACIONES ORDENADAS ===");
                for (int i = 0; i < filtradas.size(); i++) {
                    Grade g = filtradas.get(i);
                    Log.d("CalificacionesActivity", "Posición " + (i+1) + ": Unidad " + g.getUnitNumber() + " - Calificación " + g.getGrade());
                }
                
                Log.d("CalificacionesActivity", "Total de calificaciones filtradas y ordenadas: " + filtradas.size());
                adapter.actualizarLista(filtradas);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                Log.d("CalificacionesActivity", "Nada seleccionado en spinner");
            }
        });
    }

    private void setupBottomNav(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;
            if (id == R.id.nav_horarios) startActivity(new Intent(this, HorarioActivity.class));
            else if (id == R.id.nav_asistencias) startActivity(new Intent(this, AsistenciaActivity.class));
            else if (id == R.id.nav_calificaciones) startActivity(new Intent(this, CalificacionesActivity.class));
            finish();
            return true;
        });
    }
}

