package com.example.bodega.Modelos;

public class ModNotaCredito {
    private int _id ;
    private String cod_proveedor ;
    private String razsocial ;
    private String razon_comercial ;
    private String estado ;
    private double total ;
    private String fecha ;

    public ModNotaCredito(int _id, String cod_proveedor, String razsocial, String razon_comercial, String estado,  double total, String fecha) {
        this._id = _id;
        this.cod_proveedor = cod_proveedor;
        this.razsocial = razsocial;
        this.razon_comercial = razon_comercial;
        this.estado = estado;
        this.fecha = fecha;
        this.total = total;
    }

    public int get_id() {
        return _id;
    }

    public String getCod_proveedor() {
        return cod_proveedor;
    }

    public String getRazsocial() {
        return razsocial;
    }

    public String getRazon_comercial() {
        return razon_comercial;
    }

    public String getEstado() {
        return estado;
    }

    public String getFecha() {
        return fecha;
    }

    public double getTotal() {
        return total;
    }
}
