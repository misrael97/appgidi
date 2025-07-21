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

import org.json.JSONException;
import org.json.JSONObject;

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
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();


        if (email.isEmpty()) {
            edtEmail.setError("El correo es obligatorio");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Correo inválido");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("La contraseña es obligatoria");
            edtPassword.requestFocus();
            return;
        }

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
                } else { // Este 'else' es para respuestas NO exitosas (códigos 4xx, 5xx, etc.)
                    String errorMessage = "Error desconocido"; // Mensaje por defecto
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e("API_ERROR_RAW", errorBodyString); // Loguea el JSON crudo para depuración

                            // Parsear el JSON para obtener el mensaje específico
                            JSONObject errorObj = new JSONObject(errorBodyString);
                            if (errorObj.has("msg")) {
                                errorMessage = errorObj.getString("msg");
                            } else {
                                // Si no hay "msg", podrías intentar obtener otro campo o usar el mensaje por defecto
                                errorMessage = "Error en la respuesta del servidor";
                            }
                        } catch (IOException e) {
                            Log.e("API_ERROR_IO", "Error al leer errorBody", e);
                            errorMessage = "Error al procesar respuesta del servidor";
                        } catch (JSONException e) {
                            Log.e("API_ERROR_JSON", "Error al parsear JSON de error", e);
                            errorMessage = "Formato de error inesperado del servidor";
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
