package com.example.bodega.Modelos;

public class Clientes {
    private String cod_cliente ;
    private String razon_social ;

    public Clientes(String cod_cliente, String razon_social) {
        this.cod_cliente = cod_cliente;
        this.razon_social = razon_social;
    }

    public String getCod_cliente() {
        return cod_cliente;
    }

    public String getRazon_social() {
        return razon_social;
    }
}
