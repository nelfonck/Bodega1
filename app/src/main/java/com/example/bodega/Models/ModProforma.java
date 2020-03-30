package com.example.bodega.Models;

public class ModProforma {
    private String id, cod_cliente, cliente , fecha;
    private double totalExento, totalGravado, montoIv, total, credito_disponible, tope_credito, monto_deuda ;

    public ModProforma(String id, String cod_cliente, String cliente, String fecha,
                       double totalExento, double totalGravado, double montoIv,  double total,double credito_disponible,
                       double tope_credito, double monto_deuda) {
        this.id = id;
        this.cod_cliente = cod_cliente;
        this.cliente = cliente;
        this.fecha = fecha;
        this.totalExento = totalExento ;
        this.totalGravado = totalGravado ;
        this.montoIv = montoIv ;
        this.total = total ;
        this.credito_disponible = credito_disponible ;
        this.tope_credito = tope_credito ;
        this.monto_deuda = monto_deuda;
    }

    public void setCredito_disponible(double credito_disponible) {
        this.credito_disponible = credito_disponible;
    }

    public void setTope_credito(double tope_credito) {
        this.tope_credito = tope_credito;
    }

    public void setMonto_deuda(double monto_deuda) {
        this.monto_deuda = monto_deuda;
    }

    public String getId() {
        return id;
    }

    public String getCod_cliente() {
        return cod_cliente;
    }

    public String getCliente() {
        return cliente;
    }

    public String getFecha() {
        return fecha;
    }

    public double getTotal() {
        return total;
    }

    public double getTotalExento() {
        return totalExento;
    }

    public double getTotalGravado() {
        return totalGravado;
    }

    public double getMontoIv() {
        return montoIv;
    }

    public double getCredito_disponible() {
        return credito_disponible;
    }

    public double getTope_credito() {
        return tope_credito;
    }

    public double getMonto_deuda() {
        return monto_deuda;
    }
}
