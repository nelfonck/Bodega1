package com.example.bodega.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;


import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Activities.DetalleCompra;
import com.example.bodega.Activities.DetalleOrden;
import com.example.bodega.Adapters.AdapterCompra;
import com.example.bodega.Adapters.ProveedorAdapter;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.Models.ModCompra;
import com.example.bodega.Models.ModProveedor;
import com.example.bodega.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View vista = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_nueva_nota,null);
                builder.setView(vista);
                final EditText txtFiltroProveedor = vista.findViewById(R.id.txtFiltroProveedor);
                RecyclerView rvProveedores = vista.findViewById(R.id.rvProveedores);
                rvProveedores.setHasFixedSize(true);
                rvProveedores.setLayoutManager(new LinearLayoutManager(getActivity()));
                final List<ModProveedor> proveedorList = new ArrayList<>();
                final ProveedorAdapter adapter = new ProveedorAdapter(proveedorList);
                rvProveedores.setAdapter(adapter);

                txtFiltroProveedor.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER)
                            if (event.getAction() == KeyEvent.ACTION_DOWN)
                            {
                                loadProveedores(adapter,proveedorList,txtFiltroProveedor.getText().toString());
                            }
                        return false;
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();

                loadProveedores(adapter,proveedorList,"");

                adapter.SetOnItemClick(new ProveedorAdapter.OnItemClick() {
                    @Override
                    public void onClick(int pos) {

                     /*  final String cod_proveedor = proveedorList.get(pos).getCod_proveedor();
                        final String razsocial = proveedorList.get(pos).getRazocial();
                        final String razon_comercial = proveedorList.get(pos).getRazon_comercial();

                        StringRequest request = new StringRequest(Request.Method.POST, configuracion.getUrl() +
                                "/compra/guardar", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.length() > 0){
                                        int id =jsonObject.getInt("id_pedido");
                                        String fecha = jsonObject.getString("fecha");

                                        Intent detalle = new Intent(getActivity(), DetalleOrden.class);
                                        detalle.putExtra("id",id);
                                        detalle.putExtra("cod_proveedor", cod_proveedor);
                                        detalle.putExtra("razon_social",razsocial);
                                        detalle.putExtra("razon_comercial",razon_comercial);
                                        detalle.putExtra("fecha",fecha);
                                        detalle.putExtra("user",getArguments().getString("user"));
                                        startActivity(detalle);
                                    }
                                } catch (JSONException e) {
                                    msj("Error",e.getMessage());
                                }

                                //getOrders();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                try{
                                    if (error.networkResponse!=null){
                                        msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                                    }else{
                                        msj("Error",error.getMessage());
                                    }

                                }catch (Exception e){
                                    msj("Error",e.getMessage());
                                }
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("api_key",Configuracion.API_KEY);
                                params.put("cod_proveedor",cod_proveedor);
                                params.put("razon_social", razsocial);
                                params.put("razon_comercial", razon_comercial);
                                return params;
                            }
                        };
                        request.setRetryPolicy(new DefaultRetryPolicy(60000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
                        queue.add(request); */
                        dialog.dismiss();
                    }
                });


        // Intent detalleCompra = new Intent(getActivity(), DetalleCompra.class);
               // startActivity(detalleCompra);
            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void loadProveedores(final ProveedorAdapter proveedorAdapter, final List<ModProveedor>proveedorList, final String proveedor){
        try{
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Cargando");
            progressDialog.setMessage("Obteniendo proveedores, Porfavor espere...");
            progressDialog.show();

            ContentValues values = new ContentValues();
            values.put("api_key",Configuracion.API_KEY);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            JsonArrayRequest arrProv = new JsonArrayRequest(Request.Method.GET, configuracion.getUrl()
                    + "/proveedor/proveedores" +
                    ((!proveedor.equals("")) ? "/" + proveedor : "") +
                    values.toString() , null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Gson gson = new Gson();
                    proveedorList.clear();
                    proveedorList.addAll(Arrays.asList(gson.fromJson(response.toString(),ModProveedor[].class)));
                    proveedorAdapter.notifyDataSetChanged();
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try{
                        if (error.networkResponse!=null){
                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                        }else{
                            msj("Error",error.getMessage());
                        }

                    }catch (Exception e){
                        msj("Error",e.getMessage());
                    }
                }
            });
            queue.add(arrProv);
        }catch (Exception e){
            msj("Error",e.getMessage());
            if (progressDialog.isShowing()) progressDialog.dismiss();
        }

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