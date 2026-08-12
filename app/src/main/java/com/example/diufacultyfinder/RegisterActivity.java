package com.example.diufacultyfinder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class RegisterActivity extends AppCompatActivity {

    private EditText regName, regEmail, regRealEmail, regPassword;
    private AppCompatButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regName = findViewById(R.id.regName);
        regEmail = findViewById(R.id.regEmail);
        regRealEmail = findViewById(R.id.regRealEmail);
        regPassword = findViewById(R.id.regPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name = regName.getText().toString().trim();
            String email = regEmail.getText().toString().trim(); // this is the Student/Employee ID used to log in
            String realEmail = regRealEmail.getText().toString().trim(); // actual email, used only for Forgot Password
            String password = regPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || realEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "সবগুলো ঘর পূরণ করুন", Toast.LENGTH_SHORT).show();
            } else {
                // SharedPreferences দিয়ে ডাটা সেভ করা
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("name", name);
                editor.putString("email", email);           // login ID, unchanged — same as before
                editor.putString("real_email", realEmail);  // NEW — used by Forgot Password
                editor.putString("password", password);
                editor.apply(); // ডাটা সেভ হলো

                Toast.makeText(this, "Registration Done Successfully!", Toast.LENGTH_SHORT).show();
                finish(); // রেজিস্ট্রেশন পেজ বন্ধ
            }
        });
    }
}