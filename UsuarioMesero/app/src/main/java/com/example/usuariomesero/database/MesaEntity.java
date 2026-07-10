package com.example.usuariomesero.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.example.usuariomesero.models.ItemOrden;
import java.util.List;

/**
 * Representa la entidad "mesas" en la base de datos Room.
 * Almacena el estado actual, el total acumulado y los productos de la orden.
 */
@Entity(tableName = "mesas")
public class MesaEntity {
    @PrimaryKey
    private int numero;
    
    /**
     * Estado de la mesa: LIBRE, OCUPADA, COBRO.
     */
    private String estado;
    
    /**
     * Monto total acumulado en la mesa (ej. "$ 45.00").
     */
    private String precio;

    /**
     * Lista de productos que componen el pedido actual.
     */
    private List<ItemOrden> itemsPedido;

    /**
     * Información adicional (Nombre del cliente o mesero que atiende).
     */
    private String nombreInformacion;

    public MesaEntity(int numero, String estado, String precio, List<ItemOrden> itemsPedido, String nombreInformacion) {
        this.numero = numero;
        this.estado = estado;
        this.precio = precio;
        this.itemsPedido = itemsPedido;
        this.nombreInformacion = nombreInformacion;
    }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPrecio() { return precio; }
    public void setPrecio(String precio) { this.precio = precio; }

    public List<ItemOrden> getItemsPedido() { return itemsPedido; }
    public void setItemsPedido(List<ItemOrden> itemsPedido) { this.itemsPedido = itemsPedido; }

    public String getNombreInformacion() { return nombreInformacion; }
    public void setNombreInformacion(String nombreInformacion) { this.nombreInformacion = nombreInformacion; }
}
