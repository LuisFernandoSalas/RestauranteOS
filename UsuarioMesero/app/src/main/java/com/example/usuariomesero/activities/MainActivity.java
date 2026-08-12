package com.example.usuariomesero.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usuariomesero.R;
import com.example.usuariomesero.models.LoginRequest;
import com.example.usuariomesero.models.LoginResponse;
import com.example.usuariomesero.network.ApiService;
import com.example.usuariomesero.network.RetrofitClient;
import com.example.usuariomesero.utils.SesionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etUser, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ProgressBar pbLoading;
    private SesionManager sesionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ahora sí, esta línea ya no te marcará error:
        sesionManager = new SesionManager(this);

        // --- AUTO-LOGIN ---
        if (sesionManager.getAuthToken() != null) {
            irAMesas(sesionManager.getAuthToken());
            return;
        }

        initView();
        setupListeners();
    }

    private void initView() {
        etUser = findViewById(R.id.et_user);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        pbLoading = findViewById(R.id.pb_loading);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "Función de recuperación en desarrollo", Toast.LENGTH_SHORT).show()
        );
    }

    private void attemptLogin() {
        String user = etUser.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (user.isEmpty()) {
            etUser.setError("Por favor, ingresa tu usuario");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Por favor, ingresa tu contraseña");
            return;
        }

        performAuthentication(user, password);
    }

    private void performAuthentication(final String user, String password) {
        btnLogin.setEnabled(false);
        pbLoading.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient(null).create(ApiService.class);
        LoginRequest request = new LoginRequest(user, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                pbLoading.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    // 🚨 AQUÍ AGREGAMOS LA VALIDACIÓN DEL ROL PARA MESEROS 🚨
                    String userRole = response.body().getUser().getRole();

                    if (userRole != null && userRole.equalsIgnoreCase("mesero")) {
                        // ✅ EL ROL ES CORRECTO, LO DEJAMOS PASAR
                        String token = response.body().getToken();
                        String nombreUsuario = response.body().getUser().getName();

                        sesionManager.saveAuthToken(token);
                        irAMesas(nombreUsuario);

                    } else {
                        // ⛔ EL ROL NO ES MESERO (Es cocinero, admin, etc.), BLOQUEAMOS EL ACCESO
                        android.util.Log.w("LOGIN_ERROR", "Bloqueado. El usuario intentó entrar con rol: " + userRole);
                        Toast.makeText(MainActivity.this, "⛔ Acceso denegado: Esta app es exclusiva de Meseros", Toast.LENGTH_LONG).show();
                    }

                } else {
                    // AQUÍ CAPTURAMOS EL ERROR DE LARAVEL
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error vacío";
                        android.util.Log.e("LOGIN_ERROR", "Código HTTP: " + response.code() + " - Body: " + errorBody);
                    } catch (Exception e) {
                        android.util.Log.e("LOGIN_ERROR", "Error al leer el body: " + e.getMessage());
                    }

                    // 👈 MANEJO DE ERRORES HTTP DE FORMA AMIGABLE
                    if (response.code() == 401) {
                        Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 422) {
                        Toast.makeText(MainActivity.this, "Formato de datos inválido", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error del servidor (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                // AQUÍ CAPTURAMOS ERRORES DE RED (Timeout, sin internet, etc.)
                android.util.Log.e("LOGIN_ERROR", "Fallo de red o conversión: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irAMesas(String nombreUsuario) {
        Intent intent = new Intent(MainActivity.this, MesasActivity.class);
        intent.putExtra("usuario_nombre", nombreUsuario);
        startActivity(intent);
        finish();
    }
}