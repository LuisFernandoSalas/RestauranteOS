package com.example.mesero2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mesero2.databinding.FragmentoLoginBinding;

/**
 * Fragmento Login: Gestiona el acceso de los usuarios a la aplicación.
 * Incluye validación de credenciales y un sistema de recuperación de contraseña.
 */
public class FragmentoLogin extends Fragment {

    private FragmentoLoginBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Infla el diseño del fragmento usando View Binding
        binding = FragmentoLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configura el evento de clic para el botón Ingresar
        binding.btnIngresar.setOnClickListener(v -> {
            String usuario = binding.txtUsuario.getText().toString().trim();
            String contrasena = binding.txtContrasena.getText().toString().trim();

            // Validación simple de credenciales (admin / 1234)
            if (usuario.equals("admin") && contrasena.equals("1234")) {
                // Navega hacia la pantalla de Inicio si los datos son correctos
                NavHostFragment.findNavController(FragmentoLogin.this)
                        .navigate(R.id.action_FragmentoLogin_to_FragmentoInicio);
            } else {
                // Muestra un mensaje de error si los datos son incorrectos
                Toast.makeText(getContext(), getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
            }
        });

        // Configura el evento para el texto de "¿Olvidaste tu contraseña?"
        binding.txtOlvideContrasena.setOnClickListener(v -> mostrarDialogoRecuperacion());
    }

    /**
     * Muestra una ventana emergente (AlertDialog) para que el usuario introduzca 
     * su código de recuperación.
     */
    private void mostrarDialogoRecuperacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.recovery_dialog_title);
        builder.setMessage(R.string.recovery_dialog_msg);

        // Crea dinámicamente un campo de texto para el código
        final EditText entrada = new EditText(requireContext());
        entrada.setInputType(InputType.TYPE_CLASS_NUMBER); // Solo números
        entrada.setHint(R.string.recovery_code_hint);
        
        // Contenedor para aplicar márgenes/padding al campo de texto dentro del diálogo
        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        int paddingDp = 20;
        float densidad = getResources().getDisplayMetrics().density;
        int paddingPixel = (int)(paddingDp * densidad);
        contenedor.setPadding(paddingPixel, 0, paddingPixel, 0);
        contenedor.addView(entrada);

        builder.setView(contenedor);

        // Botón para verificar el código
        builder.setPositiveButton(R.string.btn_verify, (dialog, which) -> {
            String codigo = entrada.getText().toString();
            if (!codigo.isEmpty()) {
                Toast.makeText(getContext(), R.string.code_verified, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Botón para cancelar
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Libera la referencia del binding para evitar fugas de memoria
        binding = null;
    }

}