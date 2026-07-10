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
import com.example.usuariomesero.activities.MesasActivity;
import com.example.usuariomesero.api.ApiClient;
import com.example.usuariomesero.api.ApiService;
import com.example.usuariomesero.utils.TokenManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etUser, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ProgressBar pbLoading;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Asegúrate de que este sea el nombre de tu XML

        // Inicializamos las vistas con tu método
        initView();

        tokenManager = new TokenManager(this);

        // Comprobamos si el mesero ya había iniciado sesión antes
        if (tokenManager.obtenerToken() != null) {
            irAMesas();
        }

        // Acción del botón Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarLogin();
            }
        });
    }

    private void initView() {
        etUser = findViewById(R.id.et_user);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        pbLoading = findViewById(R.id.pb_loading);
    }

    private void realizarLogin() {
        String username = etUser.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🎨 MEJORA VISUAL: Ocultamos el botón y mostramos tu circulito de carga
        btnLogin.setVisibility(View.INVISIBLE);
        pbLoading.setVisibility(View.VISIBLE);

        // Preparamos el JSON que le vamos a enviar a Laravel
        // OJO: Asumo que tu backend de Laravel espera la variable "email"
        JsonObject credenciales = new JsonObject();
        credenciales.addProperty("username", username);
        credenciales.addProperty("password", password);

        // Hacemos la llamada a la API usando Retrofit
        ApiService api = ApiClient.getService(this);
        api.login(credenciales).enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                // Restauramos la vista (ocultamos la carga, mostramos el botón)
                pbLoading.setVisibility(View.GONE);
                btnLogin.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    // ¡Éxito! Laravel nos respondió con el Token
                    String token = response.body().get("access_token").getAsString();

                    // Guardamos el token de forma segura en el dispositivo
                    tokenManager.guardarToken(token);

                    Toast.makeText(MainActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                    irAMesas();
                } else {
                    // Error de credenciales incorrectas (Error 401)
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Error de red
                pbLoading.setVisibility(View.GONE);
                btnLogin.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Error de conexión con el servidor", Toast.LENGTH_LONG).show();
                System.err.println("Error de red: " + t.getMessage());
            }
        });
    }

    private void irAMesas() {
        // Asumo que tu siguiente pantalla se llama MesasActivity. 
        // Cámbialo si le pusiste otro nombre.
        Intent intent = new Intent(this, MesasActivity.class);
        startActivity(intent);
        finish();
    }
}