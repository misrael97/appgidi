package com.example.appgidi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgidi.models.LoginResponse;
import com.example.appgidi.models.VerifyCodeRequest;
import com.example.appgidi.network.ApiClient;
import com.example.appgidi.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText editCode;
    private Button btnVerify;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        editCode = findViewById(R.id.editCode);
        btnVerify = findViewById(R.id.btnVerify);

        email = getIntent().getStringExtra("email");

        btnVerify.setOnClickListener(v -> {
            String code = editCode.getText().toString().trim();
            if (code.length() != 6) {
                Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            verificarCodigo(email, code);
        });
    }

    private void verificarCodigo(String email, String code) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.verifyCode(new VerifyCodeRequest(email, code));

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    SharedPreferences prefs = getSharedPreferences("AppGidiPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("jwt_token", loginResponse.getData().getToken())
                            .putInt("user_id", loginResponse.getData().getId())
                            .putString("user_email", loginResponse.getData().getEmail())
                            .apply();

                    Toast.makeText(VerifyCodeActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(VerifyCodeActivity.this, HorarioActivity.class));
                    finish();
                } else {
                    Toast.makeText(VerifyCodeActivity.this, "Código incorrecto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(VerifyCodeActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
