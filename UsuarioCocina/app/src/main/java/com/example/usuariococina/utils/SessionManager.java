package com.example.usuariococina.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "RestaurantOS_Prefs";
    private static final String KEY_TOKEN = "auth_token";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    // Guardar el token cuando el login sea exitoso
    public void guardarToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Obtener el token para pegarlo en las llamadas a la API
    public String obtenerToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Borrar el token cuando se haga Logout
    public void borrarSesion() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}