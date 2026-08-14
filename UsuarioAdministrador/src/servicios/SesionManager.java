package servicios;

public class SesionManager {
    private static String bearerToken = "";

    public static void setToken(String token) {
        bearerToken = token;
    }

    public static String getToken() {
        return bearerToken;
    }

    public static boolean haySesion() {
        return bearerToken != null && !bearerToken.trim().isEmpty();
    }

    public static void logout() {
        bearerToken = "";
    }
}