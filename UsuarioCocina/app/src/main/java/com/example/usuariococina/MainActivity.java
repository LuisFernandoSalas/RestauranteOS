package com.example.usuariococina;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;

/**
 * Actividad de inicio de sesión (Login) para el personal de cocina.
 * Gestiona el acceso al sistema RestaurantOS y la recuperación de credenciales.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnIngresar;
    private TextView tvForgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Habilitar diseño de pantalla completa (Edge-to-Edge)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Ajustar el padding para evitar que la UI quede debajo de las barras del sistema (status bar/navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de componentes de la vista
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvForgotPass = findViewById(R.id.tvForgotPass);

        // Listener para procesar el intento de acceso
        btnIngresar.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            
            performLogin(user, pass);
        });

        // Listener para abrir el flujo de recuperación de contraseña
        tvForgotPass.setOnClickListener(v -> showForgotPasswordDialog());
    }

    /**
     * Muestra la ventana emergente para recuperación de contraseña mediante código de verificación.
     */
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        
        // Fondo transparente para permitir que el CardView del XML maneje las sombras y bordes
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etCode = dialogView.findViewById(R.id.etRecoveryCode);
        Button btnVerify = dialogView.findViewById(R.id.btnVerify);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnVerify.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Ingrese el código", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                showSuccessDialog(); // Avanzar al diálogo de éxito
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
    }

    /**
     * Muestra una confirmación visual de que la operación de recuperación fue exitosa.
     */
    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_success, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Estilo: Subrayado programático para el subtítulo de éxito
        TextView tvSubtitle = dialogView.findViewById(R.id.tvSuccessSubtitle);
        tvSubtitle.setPaintFlags(tvSubtitle.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);

        Button btnContinue = dialogView.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Ejecuta la lógica de autenticación. 
     * Actualmente simula un acceso local y redirige al Tablero de Órdenes.
     * @param username Nombre de usuario ingresado.
     * @param password Contraseña ingresada.
     */
    private void performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese todos los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Integrar con servicio de autenticación (Firebase/API REST) en fases posteriores
        Toast.makeText(this, "Sesión iniciada con éxito", Toast.LENGTH_SHORT).show();
        
        // Navegación hacia la actividad principal de la cocina
        Intent intent = new Intent(this, OrdersActivity.class);
        startActivity(intent);
        finish(); // Finaliza la actividad de login para evitar volver atrás tras loguearse
    }
}