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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.volley.toolbox.JsonObjectRequest;
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
    private String user ;

    public RegistroCompras() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
        Bundle bundle = getArguments();

        user = (bundle!=null) ? bundle.getString("user") : "undefined" ;

        RecyclerView rvCompras = view.findViewById(R.id.rvCompras);
        FloatingActionButton fabNuevaCompra = view.findViewById(R.id.fabNuevaCompra);

        compras = new ArrayList<>();
        adapter = new AdapterCompra(compras);

        rvCompras.setHasFixedSize(true);
        rvCompras.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCompras.setAdapter(adapter);

        adapter.SetOnModificarListener(new AdapterCompra.OnModificar() {
            @Override
            public void modificar(int pos) {
                Intent detalle = new Intent(getActivity(),DetalleCompra.class);
                detalle.putExtra("numero_compra", compras.get(pos).getNumero_compra());
                detalle.putExtra("cod_proveedor",compras.get(pos).getCod_proveedor());
                detalle.putExtra("user",user);
                startActivity(detalle);
            }
        });

        cargarCompras();

        fabNuevaCompra.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

                    final String cod_proveedor = proveedorList.get(pos).getCod_proveedor();
                    final String razsocial = proveedorList.get(pos).getRazocial();
                    final String razon_comercial = proveedorList.get(pos).getRazon_comercial();

                    AlertDialog.Builder builNumCompra = new AlertDialog.Builder(getActivity()) ;
                        View vista = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_numero_compra,null);
                        builNumCompra.setView(vista);
                        final AlertDialog dialogNumeroCompra = builNumCompra.create();

                        final EditText txtNumeroCompra = vista.findViewById(R.id.txtNumeroCompra);
                        TextView tvRazonSocial = vista.findViewById(R.id.tvRazonSocial);
                        TextView tvRazonComercial = vista.findViewById(R.id.tvRazonComercial);
                        tvRazonSocial.setText(razsocial);
                        tvRazonComercial.setText(razon_comercial);
                        Button btnContinuar = vista.findViewById(R.id.btnContinuar);
                        Button btnCancelar = vista.findViewById(R.id.btnCancelar);

                        btnCancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogNumeroCompra.dismiss();
                            }
                        });

                        btnContinuar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            //Si la compra no existe continuar
                                StringRequest request = new StringRequest(Request.Method.POST, configuracion.getUrl() +
                                        "/compras/guardar_compra", new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String r) {

                                        try {
                                            JSONObject response = new JSONObject(r);
                                            if (response.getBoolean("continuar")){
                                                Intent detalle = new Intent(getActivity(),DetalleCompra.class);
                                                detalle.putExtra("numero_compra", response.getString("numero_compra"));
                                                detalle.putExtra("cod_proveedor",cod_proveedor);
                                                detalle.putExtra("user",user);
                                                startActivity(detalle);
                                                dialogNumeroCompra.dismiss();
                                            }else{
                                                Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_LONG).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (error.networkResponse!=null){
                                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                                        }else{
                                            msj("Error",error.getMessage());
                                        }
                                    }
                                }){
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {

                                        Map<String, String> params = new HashMap<>();
                                        params.put("api_key",Configuracion.API_KEY);
                                        params.put("numero_compra",txtNumeroCompra.getText().toString());
                                        params.put("cod_proveedor",cod_proveedor);
                                        params.put("razon_social",razsocial);
                                        params.put("razon_comercial",razon_comercial);
                                        params.put("user",user);
                                        return params;
                                    }
                                };
                                request.setRetryPolicy(new DefaultRetryPolicy(60000,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                RequestQueue queue = Volley.newRequestQueue(getActivity());
                                queue.add(request);
                            }
                        });

                        dialogNumeroCompra.show();

                        dialog.dismiss(); //cerrar le dialog de proveedores

                    }
                });

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


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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