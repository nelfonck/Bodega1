package com.example.bodega;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bodega.Adaptadores.AdapterDetalleNota;
import com.example.bodega.Modelos.ModDetalleNota;

import java.util.ArrayList;
import java.util.List;

public class DetalleNotaCredito extends AppCompatActivity {
    private int id_nota  ;
    private String cod_proveedor, razsocial, razon_comercial ;
    private List<ModDetalleNota> detalle ;
    private AdapterDetalleNota adapter ;
    private double total ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_nota_credito);

        total = 0 ;

        TextView tvNumeroNota = findViewById(R.id.tvNumeroNota);
        TextView tvCodProveedor = findViewById(R.id.tvCodProveedor);
        TextView tvRazsocial = findViewById(R.id.tvRazsocial);
        TextView tvRazonComercial = findViewById(R.id.tvRazonComercial);
        TextView tvTotal = findViewById(R.id.tvTotal);
        RecyclerView rvDetalleNota = findViewById(R.id.rvDetalleNota);
        ImageButton btnBuscarDescripcion = findViewById(R.id.btnBuscarDescripcion);
        ImageButton btnScan = findViewById(R.id.btnScan);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        EditText txtCodigo = findViewById(R.id.txtCodigo);

        Bundle extras = getIntent().getExtras();

        if (extras != null){
            id_nota = extras.getInt("id_nota");
            cod_proveedor = extras.getString("cod_proveedor");
            razsocial = extras.getString("razsocial");
            razon_comercial = extras.getString("razon_comercial");

            tvNumeroNota.setText(("Nota:" + id_nota));
            tvCodProveedor.setText(("Proveedor:" + cod_proveedor));
            tvRazsocial.setText(razsocial);
            tvRazonComercial.setText(razon_comercial);
        }

        detalle = new ArrayList<>();
        adapter = new AdapterDetalleNota(detalle);

        rvDetalleNota.setHasFixedSize(true);
        rvDetalleNota.setLayoutManager(new LinearLayoutManager(this));
        rvDetalleNota.setAdapter(adapter);

    }
}
