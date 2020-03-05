package com.example.bodega.Modelos;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ContentValues {
    StringBuilder parametros;

    public ContentValues() {
        parametros = new StringBuilder();
    }

    public void put(String key, String value) {


        try {
            parametros.append(parametros.length() == 0 ? "?" : "&").append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return parametros.toString();
    }
}
