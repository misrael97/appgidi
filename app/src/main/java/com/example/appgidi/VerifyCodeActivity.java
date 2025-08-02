package com.example.appgidi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgidi.models.LoginResponse;
import com.example.appgidi.models.ResendCodeRequest;
import com.example.appgidi.models.VerifyCodeRequest;
import com.example.appgidi.network.ApiClient;
import com.example.appgidi.network.ApiService;
import com.example.appgidi.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText editCode;
    private Button btnVerify, btnResend;
    private ApiService apiService;
    private SessionManager sessionManager;
    private String email;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        editCode = findViewById(R.id.editCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnResend = findViewById(R.id.btnResend);

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Obtener email y userId desde el intent
        email = getIntent().getStringExtra("email");
        userId = getIntent().getIntExtra("user_id", -1);

        if (email == null || userId == -1) {
            Toast.makeText(this, "Error: Faltan datos", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnVerify.setOnClickListener(v -> verificarCodigo());
        btnResend.setOnClickListener(v -> reenviarCodigo());
    }

    private void verificarCodigo() {
        String code = editCode.getText().toString().trim();
        if (code.length() != 6) {
            editCode.setError("El código debe tener 6 dígitos");
            return;
        }

        VerifyCodeRequest request = new VerifyCodeRequest(email, code);
        apiService.verifyCode(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getData().getToken();

                    // Guardar sesión usando SessionManager
                    sessionManager.saveSession(token, userId);

                    Log.d("LOGIN_DEBUG", "Guardado user_id=" + userId + " token=" + token);

                    Toast.makeText(VerifyCodeActivity.this, "Verificación exitosa", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(VerifyCodeActivity.this, HorarioActivity.class));
                    finish();
                } else {
                    mostrarError(response);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("VerifyCodeActivity", "Error de conexión", t);
                Toast.makeText(VerifyCodeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void reenviarCodigo() {
        ResendCodeRequest request = new ResendCodeRequest(email);
        apiService.resendCode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(VerifyCodeActivity.this, "Código reenviado al correo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerifyCodeActivity.this, "No se pudo reenviar el código", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("VerifyCodeActivity", "Fallo al reenviar", t);
                Toast.makeText(VerifyCodeActivity.this, "Error al reenviar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarError(Response<LoginResponse> response) {
        try {
            String errorBody = response.errorBody().string();
            JSONObject errorObj = new JSONObject(errorBody);
            String msg = errorObj.optString("msg", "Código inválido");
            Toast.makeText(VerifyCodeActivity.this, msg, Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            Toast.makeText(VerifyCodeActivity.this, "Error desconocido", Toast.LENGTH_SHORT).show();
        }
    }
}
