package com.example.bodega.Modelos;

public class ModProveedores {
    private String cod_proveedor ;
    private String razsocial ;
    private String razon_comercial ;

    public ModProveedores(String cod_proveedor,String razsocial, String raz_comercial) {
        this.cod_proveedor = cod_proveedor ;
        this.razsocial = razsocial;
        this.razon_comercial = raz_comercial;
    }

    public String getRaz_social() {
        return razsocial;
    }


    public String getRaz_comercial() {
        return razon_comercial;
    }

    public String getCod_cliente() {
        return cod_proveedor;
    }
}
