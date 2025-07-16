package com.example.appgidi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appgidi.adapters.GradeAdapter;
import com.example.appgidi.models.Grade;
import com.example.appgidi.models.GradesResponse;
import com.example.appgidi.models.Subject;
import com.example.appgidi.network.ApiClient;
import com.example.appgidi.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class CalificacionesActivity extends AppCompatActivity {

    private Spinner spinnerMateria;
    private RecyclerView recyclerView;
    private GradeAdapter adapter;
    private List<Grade> todasLasCalificaciones = new ArrayList<>();
    private List<Subject> materias = new ArrayList<>();
    private ArrayAdapter<Subject> spinnerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificaciones);
        ImageView iconAjustes = findViewById(R.id.iconAjustesCal);
        spinnerMateria = findViewById(R.id.spinnerMateria);
        recyclerView = findViewById(R.id.recyclerCalificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GradeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshCalificaciones);
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

    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void obtenerCalificacionesDesdeAPI() {
        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String token = prefs.getString("jwt_token", null);
        if (token == null || userId == -1) return;



        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAllStudentGrades(userId, "Bearer " + token).enqueue(new Callback<GradesResponse>() {
            @Override
            public void onResponse(Call<GradesResponse> call, Response<GradesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todasLasCalificaciones = response.body().getData().getGrades();
                    materias = obtenerMateriasUnicas(todasLasCalificaciones);
                    configurarSpinner(materias);
                }
            }

            @Override
            public void onFailure(Call<GradesResponse> call, Throwable t) {

                Toast.makeText(CalificacionesActivity.this, "Error al cargar calificaciones", Toast.LENGTH_SHORT).show();
            }
        });
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

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaConPlaceholder);
        spinnerMateria.setAdapter(spinnerAdapter);

        spinnerMateria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    adapter.actualizarLista(new ArrayList<>());
                    return;
                }
                Subject materiaSeleccionada = (Subject) parent.getItemAtPosition(position);
                List<Grade> filtradas = new ArrayList<>();
                for (Grade g : todasLasCalificaciones) {
                    if (g.getSubject().getId() == materiaSeleccionada.getId()) {
                        filtradas.add(g);
                    }
                }
                adapter.actualizarLista(filtradas);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
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

