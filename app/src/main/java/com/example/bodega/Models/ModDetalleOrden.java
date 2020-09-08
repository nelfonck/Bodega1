package com.example.bodega.Models;

public class ModDetalleOrden {
    private int id ;
    private int id_pedido ;
    private int linea ;
    private String codigo ;
    private String descripcion ;
    private int cantidad ;
    private float costo ;
    private float porc_impuesto ;
    private float total_impuesto;
    private float total ;

    public ModDetalleOrden(int id, int id_pedido, int linea, String codigo, String descripcion, int cantidad, float costo, float porc_impuesto, float total_impuesto, float total) {
        this.id = id;
        this.id_pedido = id_pedido;
        this.linea = linea;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.costo = costo ;
        this.porc_impuesto = porc_impuesto ;
        this.total_impuesto = total_impuesto ;
        this.total  = total ;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public float getCosto() {
        return costo;
    }

    public float getPorc_impuesto() {
        return porc_impuesto;
    }

    public float getTotal_impuesto() {
        return total_impuesto;
    }

    public float getTotal() {
        return total;
    }
}
