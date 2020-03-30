package com.example.bodega.Models;

public class ModMarca {
    private String cod_marca;
    private String descripcion;

    public ModMarca(String cod_marca, String descripcion) {
        this.cod_marca = cod_marca;
        this.descripcion = descripcion;
    }

    public String getCod_marca() {
        return cod_marca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return  descripcion ;

    }
}
