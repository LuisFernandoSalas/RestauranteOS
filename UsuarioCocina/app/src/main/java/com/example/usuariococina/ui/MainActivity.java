package com.example.usuariococina.ui;

import com.example.usuariococina.R;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.usuariococina.api.apiClient;
import com.example.usuariococina.api.apiService;
import com.example.usuariococina.models.loginRequest;
import com.example.usuariococina.models.loginResponse;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Cocina_MainActivity";

    private EditText etUsername;
    private EditText etPassword;
    private Button btnIngresar;
    private TextView tvForgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Iniciando pantalla de Login de Cocina");

        // Si tu activity_main.xml no tiene android:id="@+id/main", comenta este bloque de ViewCompat
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvForgotPass = findViewById(R.id.tvForgotPass);

        btnIngresar.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            Log.d(TAG, "btnIngresar: Clic detectado. Usuario ingresado: " + user);
            performLogin(user, pass);
        });

        tvForgotPass.setOnClickListener(v -> {
            Log.d(TAG, "tvForgotPass: Abriendo diálogo de recuperación");
            showForgotPasswordDialog();
        });
    }

    private void showForgotPasswordDialog() {
        // ... (Tu código de diálogo) ...
    }

    private void performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Log.w(TAG, "performLogin: Error de validación, campos vacíos");
            Toast.makeText(this, "Por favor ingrese todos los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "performLogin: Iniciando petición al servidor para el usuario: " + username);

        btnIngresar.setEnabled(false);
        btnIngresar.setText("Iniciando...");

        loginRequest request = new loginRequest(username, password);
        apiService apiService = apiClient.getApiService();

        apiService.login(request).enqueue(new retrofit2.Callback<loginResponse>() {

            @Override
            public void onResponse(retrofit2.Call<loginResponse> call, retrofit2.Response<loginResponse> response) {
                btnIngresar.setEnabled(true);
                btnIngresar.setText("Ingresar");

                if (response.isSuccessful() && response.body() != null) {
                    loginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {

                        // 🚨 AQUÍ AGREGAMOS LA VALIDACIÓN DEL ROL 🚨
                        String userRole = loginResponse.getUser().getRole();

                        if (userRole.equalsIgnoreCase("cocinero")) {
                            // ✅ EL ROL ES CORRECTO, LO DEJAMOS PASAR
                            String nombreCompleto = loginResponse.getUser().getName(); // Obtiene el nombre devuelto por la API
                            Log.d(TAG, "onResponse: ¡Login exitoso! Token: " + loginResponse.getAccessToken());
                            Toast.makeText(MainActivity.this, "Bienvenido " + nombreCompleto, Toast.LENGTH_SHORT).show();

                            // Guardamos el token Y el nombre del usuario
                            getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("AUTH_TOKEN", loginResponse.getAccessToken())
                                    .putString("USER_NAME", "Chef " + nombreCompleto) // <-- ¡AQUÍ GUARDAMOS EL NOMBRE!
                                    .apply();

                            // Saltamos a la pantalla de pedidos
                            Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // ⛔ EL ROL NO ES DE COCINA, BLOQUEAMOS EL ACCESO
                            Log.w(TAG, "onResponse: Bloqueado. El usuario intentó entrar con rol: " + userRole);
                            Toast.makeText(MainActivity.this, "⛔ Acceso denegado: Esta app es exclusiva de Cocina", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 👈 MANEJO DE ERRORES HTTP DE FORMA AMIGABLE
                    Log.e(TAG, "onResponse: Error HTTP " + response.code());

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
            public void onFailure(retrofit2.Call<loginResponse> call, Throwable t) {
                btnIngresar.setEnabled(true);
                btnIngresar.setText("Ingresar");

                Log.e(TAG, "onFailure: Fallo en la red o conversión. Detalle: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}