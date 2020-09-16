package com.example.bodega.Models;

public class ModOrden {
    private int id ;
    private String cod_proveedor ;
    private String razon_social ;
    private String razon_comercial ;
    private String fecha_creacion ;
    private String estado ;
    private double total_exento ;
    private double total_gravado ;
    private double total_impuesto ;
    private double total_iva ;


    public ModOrden(int id, String cod_proveedor, String razon_social, String razon_comercial, String fecha_creacion, String estado, double total_exento,double total_gravado, double total_impuesto, double total_iva) {
        this.id = id ;
        this.cod_proveedor = cod_proveedor;
        this.razon_social = razon_social;
        this.razon_comercial = razon_comercial;
        this.fecha_creacion = fecha_creacion;
        this.estado = estado;
        this.total_exento = total_exento ;
        this.total_gravado = total_gravado ;
        this.total_impuesto = total_impuesto;
        this.total_iva = total_iva;
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

    public double getTotal_exento() {
        return total_exento;
    }

    public void setTotal_exento(double total_exento) {
        this.total_exento = total_exento;
    }

    public double getTotal_gravado() {
        return total_gravado;
    }

    public void setTotal_gravado(double total_gravado) {
        this.total_gravado = total_gravado;
    }

    public double getTotal_impuesto() {
        return total_impuesto;
    }

    public void setTotal_impuesto(double total_impuesto) {
        this.total_impuesto = total_impuesto;
    }

    public double getTotal_iva() {
        return total_iva;
    }

    public void setTotal_iva(double total_iva) {
        this.total_iva = total_iva;
    }
}
