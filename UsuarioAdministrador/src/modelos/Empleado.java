package modelos;

public class Empleado {
    private Integer id;
    private String nombre;
    private String username;
    private String rol; // "Administrador", "Cocinero", "Mesero", "Cajero"
    private String password;

    public Empleado() {}

    public Empleado(Integer id, String nombre, String username, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.rol = rol;
    }

    public Empleado(String nombre, String username, String rol, String password) {
        this.nombre = nombre;
        this.username = username;
        this.rol = rol;
        this.password = password;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}