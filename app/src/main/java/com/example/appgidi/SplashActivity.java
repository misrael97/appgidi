package com.example.appgidi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgidi.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 segundos
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);
        
        // Verificar si la app fue actualizada y limpiar sesión si es necesario
        sessionManager.checkAndClearSessionIfUpdated();

        TextView tvSmartEntry = findViewById(R.id.tvSmartEntry);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvSmartEntry.startAnimation(fadeIn);

        // Redirigir después de 2 segundos
        new Handler().postDelayed(() -> {
            Intent intent;
            
            // Verificar si el usuario ya está logueado
            if (sessionManager.isLoggedIn()) {
                // Usuario ya logueado, ir directamente a la actividad principal (Horario)
                intent = new Intent(SplashActivity.this, HorarioActivity.class);
            } else {
                // Usuario no logueado, ir al login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            
            startActivity(intent);
            finish(); // cierra splash para que no vuelva con "atrás"
        }, SPLASH_DURATION);
    }
}
