package com.example.bodega.Models;

public class Configuracion {
    private String host ;
    private String port ;
    private String host_doc ;
    private String port_doc ;

    public static final String WEBAPI = "/apibodega/public" ;
    public static final String API_KEY = "$2y$10$ww4b.izY6lDO/.YgQGu4VeIeN5f8YlIgjNDXsZZmDsHBfJCdiyKXC";

    public Configuracion() {
    }

    public Configuracion(String host, String port, String host_doc, String port_doc) {
        this.host = host;
        this.port = port;
        this.host_doc = host_doc ;
        this.port_doc = port_doc ;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    private String getHost_doc() {
        return host_doc;
    }

    public void setHost_doc(String host_doc) {
        this.host_doc = host_doc;
    }

    private String getPort_doc() {
        return port_doc;
    }

    public void setPort_doc(String port_doc) {
        this.port_doc = port_doc;
    }

    private String getHost() {
        return host;
    }

    private String getPort() {
        return port;
    }


    public String getUrl(){
        return "http://" + getHost() + ":" + getPort() + "/" + WEBAPI ;
    }

    public String getUrlDoc(){
        return "http://" + getHost_doc() + ":" + getPort_doc() + "/" + WEBAPI ;
    }


}
