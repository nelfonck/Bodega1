package com.example.bodega.Models;

public class ModDetalleProforma {

    private String cod_articulo ;
    private String descripcion ;
    private double precio ;
    private int iv ;
    private double cantidad ;
    private double total ;


    public ModDetalleProforma(String cod_articulo, String descripcion, double precio,int iv, double cantidad, double total) {
        this.cod_articulo = cod_articulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.iv = iv ;
        this.cantidad = cantidad;
        this.total = total;
    }

    public String getCod_articulo() {
        return cod_articulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public double getCantidad() {
        return cantidad;
    }

    public double getTotal() {
        return total;
    }

    public int getIv() {
        return iv;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
