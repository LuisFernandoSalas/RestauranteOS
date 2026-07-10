package com.example.usuariomesero.models;

import java.util.List;
import java.util.UUID; // 🚀 Importamos esto para generar el ID único

public class OrdenRequest {

    // 🌟 Cambiamos los nombres para que coincidan con Laravel
    private String client_uuid;
    private int mesa_id;
    private List<ItemOrdenRequest> productos;

    public OrdenRequest(int mesa_id, List<ItemOrdenRequest> productos) {
        // Generamos un identificador único aleatorio para este pedido
        this.client_uuid = UUID.randomUUID().toString();

        this.mesa_id = mesa_id;
        this.productos = productos;
    }
}