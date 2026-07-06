package api;

public class SessionManager {
    // La variable global que se queda grabada a fuego en la memoria de la app
    private static String token = "";

    public static void setToken(String nuevoToken) {
        token = nuevoToken;
    }

    public static String getToken() {
        return token;
    }

    public static boolean hasToken() {
        return token != null && !token.isEmpty();
    }
}