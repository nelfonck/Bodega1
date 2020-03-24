package com.example.bodega.Modelos;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ContentValues {
    StringBuilder parametros;

    public ContentValues() {
        parametros = new StringBuilder();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void put(String key, String value) {


        try {
            parametros.append(parametros.length() == 0 ? "?" : "&").append(key).append("=").append(URLEncoder.encode(value, "utf-8"));
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
