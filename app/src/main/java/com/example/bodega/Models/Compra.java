package com.example.bodega.Models;

public class Compra {
    int id ;
    String numero_compra ;
    String cod_proveedor ;
    String razon_social ;
    String razon_comercial ;
    double sub_total ;
    double descuento ;
    double impuestos ;
    double total ;
    String fecha ;
    String nota ;


    public Compra() {
    }

    public Compra(int id, String numero_compra, String cod_proveedor, String razon_social, String razon_comercial, double sub_total, double descuento, double impuestos, double total, String fecha, String nota) {
        this.id = id;
        this.numero_compra = numero_compra;
        this.cod_proveedor = cod_proveedor;
        this.razon_social = razon_social;
        this.razon_comercial = razon_comercial;
        this.sub_total = sub_total;
        this.descuento = descuento;
        this.impuestos = impuestos;
        this.total = total;
        this.fecha = fecha;
        this.nota = nota;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero_compra() {
        return numero_compra;
    }

    public void setNumero_compra(String numero_compra) {
        this.numero_compra = numero_compra;
    }

    public String getCod_proveedor() {
        return cod_proveedor;
    }

    public void setCod_proveedor(String cod_proveedor) {
        this.cod_proveedor = cod_proveedor;
    }

    public String getRazon_social() {
        return razon_social;
    }

    public void setRazon_social(String razon_social) {
        this.razon_social = razon_social;
    }

    public String getRazon_comercial() {
        return razon_comercial;
    }

    public void setRazon_comercial(String razon_comercial) {
        this.razon_comercial = razon_comercial;
    }

    public double getSub_total() {
        return sub_total;
    }

    public void setSub_total(double sub_total) {
        this.sub_total = sub_total;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
