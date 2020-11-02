package com.example.bodega.Models;

public class ModCompra {
    private int  id ;
    private String numero_compra ;
    private String cod_proveedor ;
    private String razon_social ;
    private String estado ;
    private String fecha ;
    private double total ;

    public ModCompra() {
    }

    public ModCompra(int id,String numero_compra, String cod_proveedor, String razon_social, String estado, String fecha, double total) {
        this.id = id;
        this.numero_compra = numero_compra ;
        this.cod_proveedor = cod_proveedor;
        this.razon_social = razon_social;
        this.estado = estado;
        this.fecha = fecha;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero_compra() {
        return numero_compra;
    }

    public void setNumero_compra(String numero_compra) {
        this.numero_compra = numero_compra;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
