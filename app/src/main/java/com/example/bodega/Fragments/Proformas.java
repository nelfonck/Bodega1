package com.example.bodega.Fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.bodega.Adapters.AdapterClientes;
import com.example.bodega.Adapters.BaseAdapter;
import com.example.bodega.Adapters.ProformasAdapter;
import com.example.bodega.Activities.DetalleProforma;
import com.example.bodega.Models.Clientes;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.InformeErrores;
import com.example.bodega.Models.ModProforma;
import com.example.bodega.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Proformas extends Fragment {
   private List<ModProforma> proformaList ;
   private ProformasAdapter adapter ;
    private  List<Clientes> clientes ;
    private AdapterClientes adapterClientes;
    private BaseAdapter baseAdapter ;
    private Configuracion configuracion ;
    private InformeErrores informeErrores ;

    public Proformas() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_proformas, container, false);

        RecyclerView rvProformas = v.findViewById(R.id.rvProformas);

        FloatingActionButton fabNueva = v.findViewById(R.id.fabNuevaProforma);

        baseAdapter = new BaseAdapter(getActivity());

        proformaList = new ArrayList<>();

        adapter = new ProformasAdapter(proformaList) ;

        informeErrores = new InformeErrores(getActivity());

        getConfiguracion();

        rvProformas.setHasFixedSize(true);
        rvProformas.setLayoutManager(new LinearLayoutManager(getActivity()));

        rvProformas.setAdapter(adapter);

        fabNueva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    nuevaProforma(baseAdapter);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        adapter.SetOnItemClick(new ProformasAdapter.OnItemClick() {
            @Override
            public void onClick(int pos) {
                Intent detalleProforma = new Intent(getActivity(), DetalleProforma.class);
                detalleProforma.putExtra("consecutivo",proformaList.get(pos).getId());
                detalleProforma.putExtra("cod_cliente",proformaList.get(pos).getCod_cliente());
                detalleProforma.putExtra("cliente",proformaList.get(pos).getCliente());
                detalleProforma.putExtra("fecha",proformaList.get(pos).getFecha());
                detalleProforma.putExtra("user",getArguments().getString("user"));
                startActivity(detalleProforma);
            }
        });

        adapter.SetOnEliminarItemClick(new ProformasAdapter.OnEliminarItemClick() {
            @Override
            public void onEliminarClick(final int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Advertencia").setMessage("Está seguro(a) de eliminar la proforma?") ;
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarProformaDB(proformaList.get(pos).getId(),pos) ;
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create() ;
                dialog.show();

            }
        });

        return v ;
    }

    private void eliminarProformaDB(String consecutivo,int pos){
        try{
            SQLiteDatabase db = baseAdapter.getWritableDatabase();
            db.delete(BaseAdapter.DETALLE_PROFORMA.TABLE_NAME, BaseAdapter.DETALLE_PROFORMA.REF_PROFORMA+"=?",new String[]{consecutivo});
            db.delete(BaseAdapter.PROFORMA.TABLE_NAME, BaseAdapter.PROFORMA.ID+"=?",new String[]{consecutivo});
            db.close();
            proformaList.remove(pos);
            adapter.notifyDataSetChanged();
        }catch (SQLiteException e){
            msj("Error",e.getMessage());
        }
    }

    private void getConfiguracion() {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());

        configuracion = new Configuracion();

        configuracion.setHost(p.getString("host", ""));
        configuracion.setPort(p.getString("port", ""));
        configuracion.setHost_db(p.getString("host_db", ""));
        configuracion.setPort_db(p.getString("port_db", ""));
        configuracion.setUser_name(p.getString("user_name", ""));
        configuracion.setPassword(p.getString("password", ""));
        configuracion.setDatabase(p.getString("db_name", ""));
        configuracion.setSchema(p.getString("schema", ""));

    }


    private void nuevaProforma(final BaseAdapter baseAdapter) throws UnsupportedEncodingException {
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_nueva_proforma,null);
        builder.setView(v);

        final EditText txtFiltroCliente = v.findViewById(R.id.txtFiltroCliente);
        final EditText txtClienteOcacional = v.findViewById(R.id.txtClienteOcacional);
        ImageButton btnClienteOcacional = v.findViewById(R.id.btnClienteOcacional);
        RecyclerView rvClientes = v.findViewById(R.id.rvClientes);


        clientes = new ArrayList<>();
        adapterClientes = new AdapterClientes(clientes);

        rvClientes.setHasFixedSize(true);
        rvClientes.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvClientes.setAdapter(adapterClientes);

        filtrarCliente("");

        txtFiltroCliente.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER){
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        try {
                            filtrarCliente(txtFiltroCliente.getText().toString());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        return true ;
                    }
                }
                return false;
            }
        });

        final AlertDialog dialog = builder.create();

        adapterClientes.SetOnClickListener(new AdapterClientes.OnClickListener() {
            @Override
            public void onClick(int pos) {
               // Toast.makeText(getActivity(),clientes.get(pos).getRazon_social(),Toast.LENGTH_SHORT).show();
                String cod_cliente = clientes.get(pos).getCod_cliente();
                String cliente = clientes.get(pos).getRazon_social();

                incluirProforma(baseAdapter,cod_cliente,cliente);
                String consecutivo = String.valueOf(getLastId(baseAdapter));
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                proformaList.add(new ModProforma(consecutivo,cod_cliente,cliente,date,0,0,0,0,0,0,0));
                getCreditoDisponible(cod_cliente,proformaList.size()-1);

                Intent detalleProforma = new Intent(getActivity(), DetalleProforma.class);
                detalleProforma.putExtra("consecutivo",consecutivo);
                detalleProforma.putExtra("cod_cliente",cod_cliente);
                detalleProforma.putExtra("cliente",cliente);
                detalleProforma.putExtra("user",getArguments().getString("user"));
                startActivity(detalleProforma);
                dialog.dismiss();
            }
        });
        btnClienteOcacional.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtClienteOcacional.getText().equals("")){
                    String cod_cliente = "";
                    String cliente = txtClienteOcacional.getText().toString();

                    incluirProforma(baseAdapter,cod_cliente,cliente);
                    String consecutivo = String.valueOf(getLastId(baseAdapter));
                    String date = new SimpleDateFormat("Y-m-d", Locale.getDefault()).format(new Date());
                    proformaList.add(new ModProforma(consecutivo,cod_cliente,cliente,date,0,0,0,0,0,0,0));
                    Intent detalleProforma = new Intent(getActivity(), DetalleProforma.class);
                    detalleProforma.putExtra("consecutivo",consecutivo);
                    detalleProforma.putExtra("cod_cliente",cod_cliente);
                    detalleProforma.putExtra("cliente",cliente);
                    detalleProforma.putExtra("user",getArguments().getString("user"));
                    startActivity(detalleProforma);
                    dialog.dismiss();
                }  else {
                    Toast.makeText(getActivity(),"Digíte el cliente ocacional",Toast.LENGTH_SHORT).show();
                }
            }
        });


        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        dialog.show();

    }


    private void incluirProforma(BaseAdapter baseAdapter,String cod_cliente, String cliente){
        try{
            SQLiteDatabase db = baseAdapter.getWritableDatabase();

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            ContentValues values = new ContentValues();
            values.put(BaseAdapter.PROFORMA.FECHA,date);
            values.put(BaseAdapter.PROFORMA.COD_CLIENTE,cod_cliente);
            values.put(BaseAdapter.PROFORMA.NOMBRE_CLIENTE,cliente);
            values.put(BaseAdapter.PROFORMA.TOTAL_EXENTO,0);
            values.put(BaseAdapter.PROFORMA.TOTAL_GRAVADO,0);
            values.put(BaseAdapter.PROFORMA.MONTO_IV,0);
            values.put(BaseAdapter.PROFORMA.TOTAL, 0);
            db.insert(BaseAdapter.PROFORMA.TABLE_NAME,null,values);

            db.close();

        }catch (SQLiteException e){
            errorMessage("Error",e.getMessage());
        }
    }

    private int getLastId(BaseAdapter baseAdapter){
        try{
            SQLiteDatabase db = baseAdapter.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT "+BaseAdapter.PROFORMA.ID+" FROM " + BaseAdapter.PROFORMA.TABLE_NAME +
                                            " WHERE " + BaseAdapter.PROFORMA.ID + "=(SELECT MAX("+BaseAdapter.PROFORMA.ID+") FROM "+BaseAdapter.PROFORMA.TABLE_NAME+")",null);
            if (c.moveToFirst()){
                return c.getInt(0);
            }else {
                return -1;
            }

        }catch (Exception e){
            errorMessage("Error",e.getMessage());
        }
        return -1 ;
    }

    private void filtrarCliente(final String cliente) throws UnsupportedEncodingException {
        final Gson gson = new Gson();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final StringRequest request = new StringRequest(Request.Method.GET, Configuracion.URL_APIBODEGA+
                "/cliente/cliente/?cliente="+cliente+"&api_key="+Configuracion.API_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                  clientes.clear();

                        clientes.addAll(Arrays.asList(gson.fromJson(response,Clientes[].class)));

                    adapterClientes.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msj = (error.getMessage() != null && error.getMessage().isEmpty()) ?
                        error.getMessage() : new String(error.networkResponse.data, StandardCharsets.UTF_8);
                informeErrores.enviar("Error",msj);
            }
        });
        queue.add(request);
    }

    private void cargarProformas(BaseAdapter baseAdapter){
        try{
            SQLiteDatabase db =  baseAdapter.getReadableDatabase() ;

            Cursor c = db.rawQuery("SELECT * FROM "+ BaseAdapter.PROFORMA.TABLE_NAME,null);
            int pos = 0 ;
            if (c.moveToFirst()){
                do{
                    proformaList.add(new ModProforma(c.getString(c.getColumnIndex(BaseAdapter.PROFORMA.ID)),
                            c.getString(c.getColumnIndex(BaseAdapter.PROFORMA.COD_CLIENTE)),
                            c.getString(c.getColumnIndex(BaseAdapter.PROFORMA.NOMBRE_CLIENTE)),
                            c.getString(c.getColumnIndex(BaseAdapter.PROFORMA.FECHA)),
                            c.getDouble(c.getColumnIndex(BaseAdapter.PROFORMA.TOTAL_EXENTO)),
                            c.getDouble(c.getColumnIndex(BaseAdapter.PROFORMA.TOTAL_GRAVADO)),
                            c.getDouble(c.getColumnIndex(BaseAdapter.PROFORMA.MONTO_IV)),
                            c.getDouble(c.getColumnIndex(BaseAdapter.PROFORMA.TOTAL)),
                            0,0,0));
                    if (!c.getString(c.getColumnIndex(BaseAdapter.PROFORMA.COD_CLIENTE)).equals("")){
                        getCreditoDisponible(c.getString(c.getColumnIndex(BaseAdapter.PROFORMA.COD_CLIENTE)),pos) ;
                    }
                    pos+= 1 ;
                }while (c.moveToNext());
            }
            adapter.notifyDataSetChanged();
            c.close();
            db.close();
        }catch (Exception e){
           errorMessage("Error fatal",e.getMessage());
        }
    }

    private void getCreditoDisponible(String cod_cliente,final int pos){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, configuracion.getUrl() + "/clientes/" +
                "?cod_cliente=" + cod_cliente +
                "&host_db=" + configuracion.getHost_db() +
                "&port_db=" + configuracion.getPort_db() +
                "&user_name=" + configuracion.getUser_name() +
                "&password=" + configuracion.getPassword() +
                "&db_name=" + configuracion.getDatabase() +
                "&schema=" + configuracion.getSchema(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.length()>0){
                    try {
                        proformaList.get(pos).setCredito_disponible(response.getDouble("credito_disponible"));
                        proformaList.get(pos).setTope_credito(response.getDouble("tope_credito"));
                        proformaList.get(pos).setMonto_deuda(response.getDouble("monto_deuda_cliente"));
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        msj("Error",e.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                msj("Error",error.getMessage());
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void errorMessage(String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onResume() {
        proformaList.clear();
        cargarProformas(baseAdapter);
        super.onResume();
    }

    private void msj(String title, String msj){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(msj);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
