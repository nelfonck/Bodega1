package com.example.bodega.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Activities.DetalleCompra;
import com.example.bodega.Adapters.AdapterCompra;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.Models.ModCompra;
import com.example.bodega.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RegistroCompras extends Fragment {
    private List<ModCompra> compras ;
    private AdapterCompra adapter ;
    private Configuracion configuracion ;
    private ProgressDialog progressDialog ;


    public RegistroCompras() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_registro_compras,container,false);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Cargando");
        progressDialog.setMessage("Espere un momento..");

        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        RecyclerView rvCompras = view.findViewById(R.id.rvCompras);
        FloatingActionButton fabNuevaCompra = view.findViewById(R.id.fabNuevaCompra);

        compras = new ArrayList<>();
        adapter = new AdapterCompra(compras);

        rvCompras.setHasFixedSize(true);
        rvCompras.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCompras.setAdapter(adapter);

        cargarCompras();

        fabNuevaCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detalleCompra = new Intent(getActivity(), DetalleCompra.class);
                startActivity(detalleCompra);
            }
        });

        return view;
    }

    private void cargarCompras(){

        progressDialog.show();

        ContentValues values = new ContentValues();
        values.put("api_key", Configuracion.API_KEY);

        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                "/compras/compras" + values.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Gson gson = new Gson();
                compras.clear();
                compras.addAll(Arrays.asList(gson.fromJson(response,ModCompra[].class)));
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                if (error.networkResponse!=null)
                {
                    msj("Error",new String(error.networkResponse.data, StandardCharsets.UTF_8));
                }else{
                    msj("Error",error.getMessage());
                }
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(getActivity());
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