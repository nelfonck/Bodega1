package com.example.bodega.Modelos;

public class ModDocumentos {
    private String consecutivo_hacienda ;
    private String nombre_comercial_vendedor ;
    private String fecha_emision ;
    private String cod_proveedor ;

    public ModDocumentos(String consecutivo_hacienda, String nombre_comercial_vendedor, String fecha_emision, String cod_proveedor) {
        this.consecutivo_hacienda = consecutivo_hacienda;
        this.nombre_comercial_vendedor = nombre_comercial_vendedor;
        this.fecha_emision = fecha_emision;
        this.cod_proveedor = cod_proveedor ;
    }

    public String getConsecutivo_hacienda() {
        return consecutivo_hacienda;
    }

    public String getCod_proveedor() {
        return cod_proveedor;
    }

    public String getNombre_comercial_vendedor() {
        return nombre_comercial_vendedor;
    }

    public String getFecha_emision() {
        return fecha_emision;
    }
}
