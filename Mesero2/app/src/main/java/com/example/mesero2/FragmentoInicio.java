package com.example.mesero2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mesero2.databinding.FragmentoInicioBinding;

/**
 * Fragmento Inicio: Pantalla de bienvenida que aparece tras un inicio de sesión exitoso.
 * Permite al usuario cerrar su sesión con una confirmación previa.
 */
public class FragmentoInicio extends Fragment {

    private FragmentoInicioBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Infla el diseño usando View Binding
        binding = FragmentoInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configura el clic en el botón de cerrar sesión
        binding.btnCerrarSesion.setOnClickListener(v -> mostrarConfirmacionCerrarSesion());
    }

    /**
     * Muestra un diálogo de confirmación antes de volver a la pantalla de Login.
     */
    private void mostrarConfirmacionCerrarSesion() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // Si confirma, navega de vuelta al Login
                    NavHostFragment.findNavController(FragmentoInicio.this)
                            .navigate(R.id.action_FragmentoInicio_to_FragmentoLogin);
                })
                .setNegativeButton(R.string.no, null) // Si cancela, no hace nada
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpieza del binding
        binding = null;
    }

}