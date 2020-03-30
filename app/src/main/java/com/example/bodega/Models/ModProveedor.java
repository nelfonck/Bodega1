package com.example.bodega.Models;

public class ModProveedor {
    private String cod_proveedor ;
    private String razsocial ;
    private String razon_comercial ;

    public ModProveedor(String cod_proveedor, String razocial, String razon_comercial) {
        this.cod_proveedor = cod_proveedor;
        this.razsocial = razocial;
        this.razon_comercial = razon_comercial;
    }

    public String getCod_proveedor() {
        return cod_proveedor;
    }

    public String getRazocial() {
        return razsocial;
    }

    public String getRazon_comercial() {
        return razon_comercial;
    }
}
