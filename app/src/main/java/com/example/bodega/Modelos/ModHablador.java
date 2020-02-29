package com.example.bodega.Modelos;

import java.io.Serializable;

public class ModHablador implements Serializable {
    private String codigo, descripcion ;
    private double precio ;

    public ModHablador(String codigo, String descripcion, double precio)  {
        this.codigo = codigo ;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getcodigo(){
        return  this.codigo ;
    }

    public String getDescripcion() {
        return this.descripcion;
    }



    public double getPrecio() {
        return this.precio;
    }


}
