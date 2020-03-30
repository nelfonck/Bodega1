package com.example.bodega.Models;

import androidx.annotation.NonNull;

public class ModFamilia {
    private String cod_familia, descripcion ;

    public ModFamilia(String cod_familia, String descripcion) {
        this.cod_familia = cod_familia;
        this.descripcion = descripcion;
    }



    public void setCod(String cod_familia) {
        this.cod_familia = cod_familia;
    }

    public void setFamilia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCod() {
        return cod_familia;
    }

    public String getFamilia() {
        return descripcion;
    }

    @NonNull
    @Override
    public String toString() {
        return descripcion;
    }
}
