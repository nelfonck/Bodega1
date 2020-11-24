package com.example.bodega.Fragments.Compra;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bodega.Activities.DetalleCompra;
import com.example.bodega.Models.Compra;
import com.example.bodega.R;

import java.text.DecimalFormat;


public class Encabezado extends Fragment {

    TextView tvNumCompra, tvCodProveedor, tvRazonComercial, tvRazonSocial,
    tvSubtotal, tvImpuestos, tvDescuento, tvTotal;
    private Compra compra ;

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
        compra = ((DetalleCompra)getActivity()).compra ;
        return inflater.inflate(R.layout.fragment_encabezado,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvNumCompra = view.findViewById(R.id.tvNumCompra);
        tvCodProveedor = view.findViewById(R.id.tvCodProveedor);
        tvRazonSocial = view.findViewById(R.id.tvRazonSocial);
        tvRazonComercial = view.findViewById(R.id.tvRazonComercial);
        tvSubtotal = view.findViewById(R.id.tvSubTotal);
        tvImpuestos = view.findViewById(R.id.tvImpuestos);
        tvDescuento = view.findViewById(R.id.tvDescuento);
        tvTotal = view.findViewById(R.id.tvTotal);

    }

    public void mostrarDatos(){
        tvNumCompra.setText(("Número de compra: " + compra.getNumero_compra()));
        tvCodProveedor.setText(("Código proveedor: " + compra.getCod_proveedor()));
        tvRazonSocial.setText(compra.getRazon_social());
        tvRazonComercial.setText(compra.getRazon_comercial());
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        tvSubtotal.setText(("₡" + formatter.format(compra.getSub_total())));
        tvImpuestos.setText(("₡" + formatter.format(compra.getImpuestos())));
        tvDescuento.setText(("₡" + formatter.format(compra.getDescuento())));
        tvTotal.setText(("₡" + formatter.format(compra.getTotal())));
    }



}