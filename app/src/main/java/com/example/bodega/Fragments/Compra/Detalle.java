package com.example.bodega.Fragments.Compra;

import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bodega.Activities.DetalleCompra;
import com.example.bodega.Adapters.AdapterDetalleCompra;
import com.example.bodega.Models.ModDetalleCompra;
import com.example.bodega.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;


public class Detalle extends Fragment {

    private AdapterDetalleCompra adapter ;
    private List<ModDetalleCompra> detalle ;
    public Detalle() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detalle,container,false);

        RecyclerView rvDetalleCompra = v.findViewById(R.id.rvDetalleCompra);
        rvDetalleCompra.setHasFixedSize(true);
        rvDetalleCompra.setLayoutManager(new LinearLayoutManager(getActivity()));
        detalle = ((DetalleCompra)getActivity()).detalleCompra;
        adapter = new AdapterDetalleCompra(detalle);
        rvDetalleCompra.setAdapter(adapter);

        FloatingActionButton fab = v.findViewById(R.id.fabNuevoArticulo);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        return v;
    }

    public void mostrarDatos()
    {
        adapter.notifyDataSetChanged();
    }
}