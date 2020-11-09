package com.example.bodega.Fragments.Compra;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bodega.Activities.DetalleCompra;
import com.example.bodega.Adapters.PageAdapter;
import com.example.bodega.R;


public class Encabezado extends Fragment {

    EditText txtCompra;
    TextView tvCodProveedor, tvRazonComercial, tvRazonSocial ;

    public Encabezado() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_encabezado,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtCompra = view.findViewById(R.id.txtCompra);
        tvCodProveedor = view.findViewById(R.id.tvCodProveedor);
        tvRazonSocial = view.findViewById(R.id.tvRazonSocial);
        tvRazonComercial = view.findViewById(R.id.tvRazonComercial);

        tvCodProveedor.setText(getArguments().getString("cod_proveedor"));
        tvRazonSocial.setText(getArguments().getString("razon_social"));
        tvRazonComercial.setText(getArguments().getString("razon_comercial"));
        Button btnParametro  = view.findViewById(R.id.btnParam);


        btnParametro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DetalleCompra)getActivity()).setByData(txtCompra.getText().toString());
            }
        });

    }
}