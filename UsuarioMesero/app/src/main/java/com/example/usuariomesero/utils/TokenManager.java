package com.example.usuariomesero.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private SharedPreferences prefs;

    public TokenManager(Context context) {
        // Crea un archivo secreto llamado "MisPreferencias"
        prefs = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
    }

    public void guardarToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TOKEN_MESERO", token);
        editor.apply();
    }

    public String obtenerToken() {
        return prefs.getString("TOKEN_MESERO", null);
    }

    public void borrarToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("TOKEN_MESERO");
        editor.apply();
    }
}