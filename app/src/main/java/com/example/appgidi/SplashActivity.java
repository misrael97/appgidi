package com.example.appgidi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvSmartEntry = findViewById(R.id.tvSmartEntry);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvSmartEntry.startAnimation(fadeIn);

        // Redirigir al login después de 2 segundos
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish(); // cierra splash para que no vuelva con "atrás"
        }, SPLASH_DURATION);
    }
}
