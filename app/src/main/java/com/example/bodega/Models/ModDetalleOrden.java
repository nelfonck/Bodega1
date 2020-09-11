package com.example.bodega.Models;

public class ModDetalleOrden {
    private int id ;
    private int id_pedido ;
    private int linea ;
    private String codigo ;
    private String descripcion ;
    private double cantidad ;
    private double costo ;
    private double impuesto ;
    private double total_impuesto;
    private double total ;

    public ModDetalleOrden(int id, int id_pedido, int linea, String codigo, String descripcion, double cantidad, double costo, double impuesto, double total_impuesto, double total) {
        this.id = id;
        this.id_pedido = id_pedido;
        this.linea = linea;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.costo = costo ;
        this.impuesto = impuesto ;
        this.total_impuesto = total_impuesto ;
        this.total  = total ;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getCosto() {
        return costo;
    }

    public double getPorc_impuesto() {
        return impuesto;
    }

    public double getTotal_impuesto() {
        return total_impuesto;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal_impuesto(double total_impuesto) {
        this.total_impuesto = total_impuesto;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
