package com.example.bodega.Modelos;

public class ModDocumentos {
    private String consecutivo_hacienda ;
    private String nombre_comercial_vendedor ;
    private String fecha_emision ;
    private int  id ;
    private String cliente ;
    private String total ;

    public ModDocumentos(String consecutivo_hacienda, String nombre_comercial_vendedor, String fecha_emision, int id,String cliente, String total) {
        this.consecutivo_hacienda = consecutivo_hacienda;
        this.nombre_comercial_vendedor = nombre_comercial_vendedor;
        this.fecha_emision = fecha_emision;
        this.id = id ;
        this.cliente = cliente ;
        this.total = total ;
    }

    public String getConsecutivo_hacienda() {
        return consecutivo_hacienda;
    }

    public int getId() {
        return id;
    }

    public String getNombre_comercial_vendedor() {
        return nombre_comercial_vendedor;
    }

    public String getFecha_emision() {
        return fecha_emision;
    }

    public String getCliente() {
        return cliente;
    }

    public String getTotal() {
        return total;
    }
}
