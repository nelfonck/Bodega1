package com.example.bodega.Models;

public class ModOrden {
    private int id ;
    private String cod_proveedor ;
    private String razon_social ;
    private String razon_comercial ;
    private String fecha_creacion ;
    private String estado ;

    public ModOrden(int id, String cod_proveedor, String razon_social, String razon_comercial, String fecha_creacion, String estado) {
        this.id = id ;
        this.cod_proveedor = cod_proveedor;
        this.razon_social = razon_social;
        this.razon_comercial = razon_comercial;
        this.fecha_creacion = fecha_creacion;
        this.estado = estado;
    }

    public String getCod_proveedor() {
        return cod_proveedor;
    }

    public void setCod_proveedor(String cod_proveedor) {
        this.cod_proveedor = cod_proveedor;
    }

    public String getRazon_social() {
        return razon_social;
    }

    public void setRazon_social(String razon_social) {
        this.razon_social = razon_social;
    }

    public String getRazon_comercial() {
        return razon_comercial;
    }

    public void setRazon_comercial(String razon_comercial) {
        this.razon_comercial = razon_comercial;
    }

    public String getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(String fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
