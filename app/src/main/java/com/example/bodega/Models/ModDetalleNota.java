package com.example.bodega.Models;

public class ModDetalleNota {
    private String codigo ;
    private String descripcion ;
    private double cantidad ;
    private double costo ;
    private double impuesto ;
    private double monto_impuesto ;
    private double total_ivi ;
    private String cod_impuesto ;

    public ModDetalleNota(String codigo, String descripcion, double cantidad, double costo, double impuesto, double monto_impuesto, double total_ivi, String cod_impuesto) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.cantidad = cantidad ;
        this.costo = costo;
        this.impuesto = impuesto;
        this.monto_impuesto = monto_impuesto;
        this.total_ivi = total_ivi;
        this.cod_impuesto = cod_impuesto;
    }

    public String getCodigo() {
        return codigo;
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

    public double getImpuesto() {
        return impuesto;
    }

    public double getMonto_impuesto() {
        return monto_impuesto;
    }

    public double getTotal_ivi() {
        return total_ivi;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public void setTotal_ivi(double total_ivi) {
        this.total_ivi = total_ivi;
    }

    public String getCod_impuesto() {
        return cod_impuesto;
    }
}
