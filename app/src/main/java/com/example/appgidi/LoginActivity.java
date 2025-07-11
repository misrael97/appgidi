package com.example.appgidi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgidi.HorarioActivity;
import com.example.appgidi.R;
import com.example.appgidi.models.User;
import com.example.appgidi.models.LoginResponse;
import com.example.appgidi.network.ApiClient;
import com.example.appgidi.network.ApiService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);

        apiService = ApiClient.getClient().create(ApiService.class);

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        User user = new User(email, password, "mobile");

        apiService.loginUser(user).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if ("success".equals(loginResponse.getStatus())) {
                        // Guardar token JWT
                        String jwtToken = loginResponse.getData().getToken();
                        SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putString("jwt_token", jwtToken)
                                .putInt("user_id", loginResponse.getData().getId())
                                .putString("user_email", loginResponse.getData().getEmail())
                                .apply();

                        // Ir a siguiente pantalla
                        Toast.makeText(LoginActivity.this, loginResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HorarioActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, loginResponse.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(LoginActivity.this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("API_ERROR", errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("NETWORK_ERROR", "Error de conexión", t);
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
