package com.example.diufacultyfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etFpEmail, etFpNewPassword, etFpConfirmPassword;
    private AppCompatButton btnResetPassword;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etFpEmail = findViewById(R.id.etFpEmail);
        etFpNewPassword = findViewById(R.id.etFpNewPassword);
        etFpConfirmPassword = findViewById(R.id.etFpConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnResetPassword.setOnClickListener(v -> handleReset());

        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleReset() {
        String email = etFpEmail.getText().toString().trim();
        String newPassword = etFpNewPassword.getText().toString().trim();
        String confirmPassword = etFpConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "সবগুলো ঘর পূরণ করুন", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Same SharedPreferences file as LoginActivity.java & RegisterActivity.java.
        // Checks against "real_email" (the genuine email field on Register),
        // NOT "email" (which is actually the Student/Employee login ID).
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedRealEmail = prefs.getString("real_email", "");

        if (savedRealEmail.isEmpty()) {
            Toast.makeText(this, "No registered account found. Please register first.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!email.equalsIgnoreCase(savedRealEmail)) {
            Toast.makeText(this, "This email doesn't match our records", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit().putString("password", newPassword).apply();
        Toast.makeText(this, "Password reset successful! Please log in.", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
        finish();
    }
}