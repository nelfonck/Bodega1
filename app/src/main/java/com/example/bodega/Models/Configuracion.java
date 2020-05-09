package com.example.bodega.Models;

public class Configuracion {
    private String host ;
    private String port ;
    private String host_db ;
    private String port_db ;
    private String user_name ;
    private String password ;
    private String database;
    private String schema ;
    private String host_update ;
    private String port_update ;


    public static final String WEBAPI = "KiwiUT" ;
    public static final String WEBAPIUPDATES = "actualizador" ;
    public static final String URL_APIBODEGA = "http://201.192.158.233:82/apibodega/public" ;
    public static final String API_KEY = "$2y$10$ww4b.izY6lDO/.YgQGu4VeIeN5f8YlIgjNDXsZZmDsHBfJCdiyKXC";

    public Configuracion() {
    }

    public Configuracion(String host, String port, String host_db, String port_db, String user_name, String password, String database, String schema,String host_update, String port_update) {
        this.host = host;
        this.port = port;
        this.host_db = host_db;
        this.port_db = port_db;
        this.user_name = user_name;
        this.password = password;
        this.database = database;
        this.schema = schema;
        this.host_update = host_update;
        this.port_update = port_update ;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setHost_db(String host_db) {
        this.host_db = host_db;
    }

    public void setPort_db(String port_db) {
        this.port_db = port_db;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setHost_update(String host_update) {
        this.host_update = host_update;
    }

    public void setPort_update(String port_update) {
        this.port_update = port_update;
    }


    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getHost_db() {
        return host_db;
    }

    public String getPort_db() {
        return port_db;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getSchema() {
        return schema;
    }

    public String getUrl(){
        return "http://" + host + ":" + port + "/" + WEBAPI ;
    }


    public String getUrlUpdates(){
        return "http://" + host_update + ":" + port_update + "/" + WEBAPIUPDATES + "/" ;
    }

}
