package com.example.diufacultyfinder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Total time the splash screen stays visible before moving to Login.
    private static final int SPLASH_DELAY_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.splashLogo);
        TextView title = findViewById(R.id.splashTitle);
        TextView tagline = findViewById(R.id.splashTagline);
        ProgressBar progress = findViewById(R.id.splashProgress);

        // Simple fade + scale-in animation for the logo, then fade in the text below it.
        logo.setScaleX(0.8f);
        logo.setScaleY(0.8f);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .withEndAction(() -> {
                    title.animate().alpha(1f).setDuration(350).start();
                    tagline.animate().alpha(1f).setDuration(350).setStartDelay(100).start();
                    progress.animate().alpha(1f).setDuration(350).setStartDelay(200).start();
                })
                .start();

        // After the delay, go to LoginActivity and remove Splash from the back stack
        // so pressing "back" from Login doesn't return to the splash screen.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DELAY_MS);
    }
}