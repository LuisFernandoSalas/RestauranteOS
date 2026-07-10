package api;

public class SessionManager {
    // Variable estática: significa que es GLOBAL para toda la app
    private static String token = null;

    // Guardar el token (Se usa en el Login)
    public static void setToken(String nuevoToken) {
        token = nuevoToken;
    }

    // Obtener el token (Se usará en el ApiClient)
    public static String getToken() {
        return token;
    }

    // Saber si ya estamos logueados
    public static boolean hasToken() {
        return token != null && !token.isEmpty();
    }

    // Cerrar sesión
    public static void logout() {
        token = null;
    }
}