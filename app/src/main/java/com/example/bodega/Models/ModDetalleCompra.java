package com.example.bodega.Models;

public class ModDetalleCompra {
    private int id;
    private String cod_articulo ;
    private String cod_proveedor ;
    private String descripcion ;
    private double cantidad ;
    private double costo ;
    private double descuento;
    private double iv ;
    private double sub_total ;
    private double total_ivi;

    public ModDetalleCompra(int id, String cod_articulo, String cod_proveedor, String descripcion, double cantidad, double costo, double descuento, double iv, double sub_total, double total_ivi) {
        this.id = id;
        this.cod_articulo = cod_articulo;
        this.cod_proveedor = cod_proveedor;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.costo = costo;
        this.descuento = descuento;
        this.iv = iv;
        this.sub_total = sub_total;
        this.total_ivi = total_ivi;
    }

    public int getId() {
        return id;
    }

    public String getCod_articulo() {
        return cod_articulo;
    }

    public String getCod_proveedor() {
        return cod_proveedor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getCantidad() {
        return cantidad;
    }

    public double getCosto() {
        return costo;
    }

    public double getDescuento() {
        return descuento;
    }

    public double getIv() {
        return iv;
    }

    public double getSub_total() {
        return sub_total;
    }

    public double getTotal_ivi() {
        return total_ivi;
    }
}
