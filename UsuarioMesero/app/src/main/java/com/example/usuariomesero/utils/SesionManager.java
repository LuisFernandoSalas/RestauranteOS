package com.example.usuariomesero.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SesionManager {

    // Nombre del archivo de preferencias y la clave para el token
    private static final String PREF_NAME = "MeseroAppSession";
    private static final String KEY_TOKEN = "auth_token";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // 👇 ESTE ES EL CONSTRUCTOR QUE TE MARCABA ERROR 👇
    // Ahora sí está preparado para recibir el "(this)" que le mandas desde MainActivity
    public SesionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Guarda el token cuando el login es exitoso.
     */
    public void saveAuthToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    /**
     * Recupera el token. Devuelve null si el usuario no está logueado.
     */
    public String getAuthToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Borra el token (Ideal para cuando el mesero cierra sesión).
     */
    public void clearSession() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}