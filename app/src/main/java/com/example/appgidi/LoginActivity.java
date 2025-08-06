package com.example.appgidi;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgidi.models.LoginInitResponse;
import com.example.appgidi.models.User;
import com.example.appgidi.network.ApiClient;
import com.example.appgidi.network.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button loginButton;
    private ImageView passwordToggle;
    private ApiService apiService;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        passwordToggle = findViewById(R.id.passwordToggle);

        apiService = ApiClient.getClient().create(ApiService.class);

        loginButton.setOnClickListener(v -> loginUser());
        
        // Configurar el toggle de contraseña
        setupPasswordToggle();
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

        apiService.loginUser(user).enqueue(new Callback<LoginInitResponse>() {
            @Override
            public void onResponse(Call<LoginInitResponse> call, Response<LoginInitResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    LoginInitResponse.LoginInitData data = response.body().getData();
                    String email = data.getEmail();
                    int userId = data.getId();

                    Log.d("LoginActivity", "ID del usuario: " + userId + " Email: " + email);

                    Toast.makeText(LoginActivity.this, "Código 2FA enviado al correo", Toast.LENGTH_SHORT).show();

                    // Redirigir a VerifyCodeActivity con email e ID
                    Intent intent = new Intent(LoginActivity.this, VerifyCodeActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                    finish();
                } else {
                    showError(response);
                }
            }

            @Override
            public void onFailure(Call<LoginInitResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LoginActivity", "Fallo en la petición", t);
            }

            private void showError(Response<LoginInitResponse> response) {
                try {
                    String errorBody = response.errorBody().string();
                    JSONObject errorObj = new JSONObject(errorBody);
                    String msg = errorObj.optString("msg", "Error al iniciar sesión");
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                } catch (IOException | JSONException e) {
                    Toast.makeText(LoginActivity.this, "Error desconocido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * Configura la funcionalidad de mostrar/ocultar contraseña
     */
    private void setupPasswordToggle() {
        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Ocultar contraseña
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordToggle.setImageResource(R.drawable.ic_eye_closed);
                    isPasswordVisible = false;
                } else {
                    // Mostrar contraseña
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordToggle.setImageResource(R.drawable.ic_eye_open);
                    isPasswordVisible = true;
                }
                
                // Mover el cursor al final del texto
                edtPassword.setSelection(edtPassword.getText().length());
            }
        });
    }
}
