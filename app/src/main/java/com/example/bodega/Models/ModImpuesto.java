package com.example.bodega.Models;

import androidx.annotation.NonNull;

public class ModImpuesto {
    private String cod_impuesto,descripcion ;
    private double porcentaje ;


    public ModImpuesto() {
    }

    public void setCodigo(String cod_impuesto) {
        this.cod_impuesto = cod_impuesto;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setImpuesto(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getCodigo() {
        return cod_impuesto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getImpuesto() {
        return porcentaje;
    }

    @NonNull
    @Override
    public String toString() {
        return descripcion ;
    }
}
