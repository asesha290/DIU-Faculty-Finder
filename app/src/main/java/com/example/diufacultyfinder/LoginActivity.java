package com.example.diufacultyfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private AppCompatButton btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private ImageView btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sessionPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sessionPrefs.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnLogin.setOnClickListener(v -> {
            // What the user typed — could be their Student/Employee ID OR their email
            String loginInput = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String savedId = sharedPreferences.getString("email", "");          // Student/Employee ID
            String savedRealEmail = sharedPreferences.getString("real_email", ""); // actual Gmail/email
            String savedPassword = sharedPreferences.getString("password", "");

            if (loginInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your ID/Email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Accept login with EITHER the Student/Employee ID OR the real email
            boolean idMatches = loginInput.equalsIgnoreCase(savedId);
            boolean emailMatches = !savedRealEmail.isEmpty() && loginInput.equalsIgnoreCase(savedRealEmail);

            if ((idMatches || emailMatches) && password.equals(savedPassword)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                sharedPreferences.edit().putBoolean("is_logged_in", true).apply();

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                // Always pass the REAL email to HomeActivity (not the ID),
                // so the owner-only feature check in home.html works correctly
                // no matter which identifier the person used to log in.
                intent.putExtra("userEmail", savedRealEmail);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid ID/Email or password", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show()
        );
    }
}