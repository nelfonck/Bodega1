package com.example.bodega.Modelos;

import androidx.annotation.NonNull;

public class ContentValues {
    StringBuilder parametros;

    public ContentValues() {
        parametros = new StringBuilder();
    }

    public void put(String key, String value) {
        parametros.append(parametros.length() == 0 ? "?" : "&").append(key).append("=").append(value);
    }

    @NonNull
    @Override
    public String toString() {
        return parametros.toString();
    }
}
