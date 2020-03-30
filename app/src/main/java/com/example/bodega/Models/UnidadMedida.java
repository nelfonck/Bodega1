package com.example.bodega.Models;

import androidx.annotation.NonNull;

public class UnidadMedida {
    private String unidad_medida ;
    private String descripcion ;
    private String abreviatura ;

    public UnidadMedida(String unidad_medida, String descripcion, String abreviatura) {
        this.unidad_medida = unidad_medida;
        this.descripcion = descripcion;
        this.abreviatura = abreviatura;
    }

    public String getUnidad_medida() {
        return unidad_medida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    @NonNull
    @Override
    public String toString() {
        return descripcion;
    }
}
