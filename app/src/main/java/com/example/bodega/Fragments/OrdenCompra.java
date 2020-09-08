package com.example.bodega.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import com.android.volley.toolbox.Volley;
import com.example.bodega.Activities.DetalleOrden;
import com.example.bodega.Adapters.AdapterOrdenes;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ModOrden;
import com.example.bodega.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class OrdenCompra extends Fragment {
    private Configuracion configuracion ;
    private List<ModOrden> ordenes ;
    private AdapterOrdenes adapter ;

    public OrdenCompra() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_orden_compra, container, false);
        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        RecyclerView rv = v.findViewById(R.id.rvOrdenes);
        rv.setHasFixedSize(true);
        ordenes = new ArrayList<>();
        adapter = new AdapterOrdenes(ordenes);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);

        getOrders();
        // Inflate the layout for this fragment

        adapter.SetOnAbrirOrden(new AdapterOrdenes.OnAbrirOrden() {
            @Override
            public void abrirOrden(int pos) {
                Intent detalle = new Intent(getActivity(), DetalleOrden.class);
                detalle.putExtra("id",ordenes.get(pos).getId());
                detalle.putExtra("cod_proveedor", ordenes.get(pos).getCod_proveedor());
                detalle.putExtra("razon_social",ordenes.get(pos).getRazon_social());
                detalle.putExtra("razon_comercial",ordenes.get(pos).getRazon_comercial());
                detalle.putExtra("fecha",ordenes.get(pos).getFecha_creacion());
                detalle.putExtra("user",getArguments().getString("user"));
                startActivity(detalle);
            }
        });

        adapter.SetOnEliminarOrden(new AdapterOrdenes.OnEliminarOrden() {
            @Override
            public void eliminarOrden(int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Advertencia").setMessage("Seguro(a) que desea elimiar esta orden?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        FloatingActionButton fab = v.findViewById(R.id.fabNuevaOrden);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //codigo para crear una nueva orden
            }
        });

        return v;
    }

    private void getOrders(){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Cargando");
        progressDialog.setMessage("Espere un momento..");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                "/pedido/pedidos?api_key=" + Configuracion.API_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson() ;
                ordenes.addAll(Arrays.asList(gson.fromJson(response,ModOrden[].class)));
                adapter.notifyDataSetChanged();
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                msj("Error",new String(error.networkResponse.data, StandardCharsets.UTF_8));
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
        queue.add(request);
    }

    @SuppressWarnings("SameParameterValue")
    private void msj(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(message).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}