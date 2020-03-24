package com.example.bodega;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class DetalleNotaCredito extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_nota_credito);

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

    }
}
