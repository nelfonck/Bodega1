package com.example.bodega.Modelos;

public class ModCompra {
    private String numero  ;
    private String cod_proveedor ;
    private String proveedor ;
    private String estado ;
    private double total ;
    private String fecha ;

    public ModCompra(String numero, String cod_proveedor, String proveedor, String estado, double total, String fecha) {
        this.numero = numero;
        this.cod_proveedor = cod_proveedor;
        this.proveedor = proveedor;
        this.estado = estado;
        this.total = total;
        this.fecha = fecha;
    }

    public ModCompra() {
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setCod_proveedor(String cod_proveedor) {
        this.cod_proveedor = cod_proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNumero() {
        return numero;
    }

    public String getCod_proveedor() {
        return cod_proveedor;
    }

    public String getProveedor() {
        return proveedor;
    }

    public String getEstado() {
        return estado;
    }

    public double getTotal() {
        return total;
    }

    public String getFecha() {
        return fecha;
    }
}
