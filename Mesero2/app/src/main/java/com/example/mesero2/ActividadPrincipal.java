package com.example.mesero2;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mesero2.databinding.ActividadPrincipalBinding;

import java.util.Objects;

/**
 * Actividad Principal: Sirve como contenedor para todos los fragmentos de la aplicación.
 * Gestiona la barra de herramientas (Toolbar) y la navegación centralizada.
 */
public class ActividadPrincipal extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActividadPrincipalBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Habilita el diseño de borde a borde (Edge-to-Edge)
        EdgeToEdge.enable(this);

        // Uso de View Binding para acceder a los elementos del layout de forma segura
        binding = ActividadPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ajusta los rellenos (paddings) para evitar que el contenido quede bajo las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Configura la Toolbar personalizada como la ActionBar de la actividad
        setSupportActionBar(binding.toolbar);

        // Configuración del componente de Navegación de Jetpack
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_contenido_principal);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Define los fragmentos de nivel superior
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.FragmentoLogin, R.id.FragmentoInicio).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Oculta el título de la ActionBar para mantener un diseño limpio
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            // Invalida el menú según el destino
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> invalidateOptionsMenu());
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_contenido_principal);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            if (navController.getCurrentDestination() != null && 
                navController.getCurrentDestination().getId() == R.id.FragmentoInicio) {
                menu.clear();
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_contenido_principal);
        if (navHostFragment != null) {
            return NavigationUI.navigateUp(navHostFragment.getNavController(), appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}