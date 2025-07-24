package com.example.appgidi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appgidi.adapters.TeacherGroupAdapter;
import com.example.appgidi.models.GroupDataResponse;
import com.example.appgidi.models.TeacherSubjectGroup;
import com.example.appgidi.network.ApiClient;
import com.example.appgidi.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorarioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherGroupAdapter adapter;
    private List<TeacherSubjectGroup> listaCompleta = new ArrayList<>();
    private String diaActual = "Monday"; // Día por defecto
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_horario);
        ImageView iconAjustes = findViewById(R.id.iconAjustes);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshHorario);
        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        if (token == null) {
            // Redirigir a login si no está autenticado
            startActivity(new Intent(HorarioActivity.this, LoginActivity.class));
            finish();
        }
        iconAjustes.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(HorarioActivity.this, view);
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

        swipeRefreshLayout = findViewById(R.id.swipeRefreshHorario);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            obtenerDatosDesdeAPI();
            swipeRefreshLayout.setRefreshing(false);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHorario), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerMaterias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeacherGroupAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        setupDayButtons();
        setupBottomNav(R.id.nav_horarios);
        obtenerDatosDesdeAPI();
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void obtenerDatosDesdeAPI() {
        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        if (token == null) {
            Toast.makeText(this, "Token no encontrado, por favor inicia sesión", Toast.LENGTH_SHORT).show();
            return;
        }
        String authHeader = "Bearer " + token;

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getTeacherGroups(1, authHeader).enqueue(new Callback<GroupDataResponse>() {
            @Override
            public void onResponse(Call<GroupDataResponse> call, Response<GroupDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta = response.body().getTeacherSubjectGroups();

                    Log.d("API_DEBUG", "Cantidad de asignaciones recibidas: " + listaCompleta.size());

                    for (TeacherSubjectGroup item : listaCompleta) {
                        String materia = item.getSubjects() != null ? item.getSubjects().getName() : "null";
                        String profe = item.getUsers() != null ? item.getUsers().getFullName() : "null";
                        String dia = item.getSchedules() != null ? item.getSchedules().getWeekday() : "null";

                        Log.d("API_DEBUG", "Materia: " + materia + " | Profesor: " + profe + " | Día: " + dia);
                    }

                    filtrarPorDia(diaActual);
                } else {
                    Log.e("API_DEBUG", "Error en la respuesta: " + response.code());
                    Toast.makeText(HorarioActivity.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupDataResponse> call, Throwable t) {
                Log.e("API_DEBUG", "Fallo en la conexión", t);
                Toast.makeText(HorarioActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarPorDia(String dia) {
        List<TeacherSubjectGroup> filtrados = new ArrayList<>();
        for (TeacherSubjectGroup item : listaCompleta) {
            if (item.getSchedules().getWeekday().equalsIgnoreCase(dia)) {
                filtrados.add(item);
            }
        }
        adapter.setLista(filtrados);
    }

    private void setupDayButtons() {
        Button[] dayButtons = {
                findViewById(R.id.btnLunes),
                findViewById(R.id.btnMartes),
                findViewById(R.id.btnMiercoles),
                findViewById(R.id.btnJueves),
                findViewById(R.id.btnViernes)
        };

        String[] diasIngles = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for (int i = 0; i < dayButtons.length; i++) {
            int finalI = i;
            Button btn = dayButtons[i];

            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.vista_horario)));
            btn.setTextColor(getResources().getColor(R.color.dark_gray_2));
            btn.setOnClickListener(v -> {
                // Resetear todos los botones
                for (Button b : dayButtons) {
                    b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.vista_horario)));
                    b.setTextColor(getResources().getColor(R.color.dark_gray_2));
                }

                // Seleccionar actual
                btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark_gray_2)));
                btn.setTextColor(getResources().getColor(R.color.white));

                // Cambiar día y filtrar
                diaActual = diasIngles[finalI];
                filtrarPorDia(diaActual);

            });
        }

        // Establecer Lunes como día por defecto
        dayButtons[0].performClick();
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
