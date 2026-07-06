package com.example.usuariococina.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usuariococina.R;
import com.example.usuariococina.api.ApiClient;
import com.example.usuariococina.models.LoginRequest;
import com.example.usuariococina.models.LoginResponse;
import com.example.usuariococina.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Asegúrate de que este sea el nombre de tu XML de login

        // 1. Inicializar componentes visuales (Ajusta los IDs si tus XML usan otros nombres)
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnIngresar);

        sessionManager = new SessionManager(this);

        // 2. Evento del botón de Login
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                ejecutarLogin(username, password);
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ejecutarLogin(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);

        ApiClient.getApiService(this).login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    String rol = response.body().getUser().getRole();

                    sessionManager.guardarToken(token);
                    Toast.makeText(MainActivity.this, "¡Bienvenido " + response.body().getUser().getName() + "!", Toast.LENGTH_SHORT).show();

                    // 📍 Redirección real según el rol de Laravel
                    if (rol.equalsIgnoreCase("admin") || rol.equalsIgnoreCase("cocinero")) {
                        Toast.makeText(MainActivity.this, "Abriendo panel de Cocina...", Toast.LENGTH_SHORT).show();

                        // 🔥 AQUÍ ARRANCAMOS TU MONITOR DE COCINA
                        Intent intent = new Intent(MainActivity.this, com.example.usuariococina.ui.OrdersActivity.class);
                        startActivity(intent);
                        finish(); // Cierra el Login para que no puedan regresar con el botón físico de atrás

                    } else if (rol.equalsIgnoreCase("mesero")) {
                        Toast.makeText(MainActivity.this, "Abriendo panel de Mesero...", Toast.LENGTH_SHORT).show();
                        // Aquí irá el intent de los meseros cuando programen esa app
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}