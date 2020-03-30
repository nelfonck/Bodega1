package com.example.bodega.Models;

public class ModFiltroArticulo {
    private String cod_articulo, descripcion ;

    public ModFiltroArticulo(String cod_articulo, String descripcion) {
        this.cod_articulo = cod_articulo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return cod_articulo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
