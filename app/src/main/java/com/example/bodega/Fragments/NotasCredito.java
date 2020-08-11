package com.example.bodega.Fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adapters.AdapterNotaCredito;
import com.example.bodega.Adapters.BaseAdapter;
import com.example.bodega.Adapters.ProveedorAdapter;
import com.example.bodega.Activities.DetalleNotaCredito;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.Models.ModNotaCredito;
import com.example.bodega.Models.ModProveedor;
import com.example.bodega.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NotasCredito extends Fragment {
    private Configuracion configuracion ;
    private ProgressDialog progressDialog ;
    private BaseAdapter dbHelper ;
    private AdapterNotaCredito adapter ;
    private List<ModNotaCredito> notas ;
    private String user ;

    public NotasCredito() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notas_credito, container, false);
        FloatingActionButton fabNuevo = v.findViewById(R.id.fabNuevaNota);

        user = getArguments().getString("user") ;

        getConfiguracion();

        fabNuevo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                nuevaNota();
            }
        });

        progressDialog = new ProgressDialog(getActivity());

        dbHelper = new BaseAdapter(getActivity());

        RecyclerView rvNotasCredito = v.findViewById(R.id.rvNotasCredito);
        rvNotasCredito.setHasFixedSize(true);
        rvNotasCredito.setLayoutManager(new LinearLayoutManager(getActivity()));

        notas = new ArrayList<>();
        adapter = new AdapterNotaCredito(notas);

        rvNotasCredito.setAdapter(adapter);



        adapter.SetOnClickListener(new AdapterNotaCredito.OnClickListener() {
            @Override
            public void onClick(int pos) {
                //lanzar activity detalles
                lanzarDetalles(
                        notas.get(pos).get_id(),
                        notas.get(pos).getCod_proveedor(),
                        notas.get(pos).getRazsocial(),
                        notas.get(pos).getRazon_comercial()
                        );
            }
        });

        adapter.SetOnLongClickListener(new AdapterNotaCredito.OnLongClickListener() {
            @Override
            public void onLongClick(final int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Advertencia").setMessage("Está seguro(a) de eliminar la nota?") ;
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarNota(notas.get(pos).get_id(),pos);
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

    private void cargarNotas(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + BaseAdapter.NOTAS_CREDITO.TABLE_NAME, null);

        notas.clear();

        if (c.moveToFirst())
            do{
                notas.add(new ModNotaCredito(c.getInt(c.getColumnIndex(BaseAdapter.NOTAS_CREDITO.ID)),
                                             c.getString(c.getColumnIndex(BaseAdapter.NOTAS_CREDITO.COD_PROVEEDOR)),
                                             c.getString(c.getColumnIndex(BaseAdapter.NOTAS_CREDITO.RAZSOCIAL)),
                                             c.getString(c.getColumnIndex(BaseAdapter.NOTAS_CREDITO.RAZON_COMERCIAL)),
                                             c.getString(c.getColumnIndex(BaseAdapter.NOTAS_CREDITO.ESTADO)),
                                             c.getDouble(c.getColumnIndex(BaseAdapter.NOTAS_CREDITO.TOTAL)),
                                             c.getString(c.getColumnIndex(BaseAdapter.NOTAS_CREDITO.FECHA))));
            }while (c.moveToNext());

            adapter.notifyDataSetChanged();
            db.close();
            c.close();
    }

    private void eliminarNota(int id, int pos){
        try{
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(BaseAdapter.DETALLE_NOTAS_CREDITO.TABLE_NAME,BaseAdapter.DETALLE_NOTAS_CREDITO.REF+"=?",new String[]{String.valueOf(id)});
            db.delete(BaseAdapter.NOTAS_CREDITO.TABLE_NAME,BaseAdapter.NOTAS_CREDITO.ID+"=?",new String[]{String.valueOf(id)});
            db.close();
            notas.remove(pos);
            adapter.notifyDataSetChanged();
        }catch (SQLiteException e){
            msj("Error",e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void nuevaNota(){
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

                String cod_proveedor = proveedorList.get(pos).getCod_proveedor();
                String razsocial = proveedorList.get(pos).getRazocial();
                String razon_comercial = proveedorList.get(pos).getRazon_comercial();
                insertarNota(cod_proveedor,razsocial,razon_comercial);

                cargarNotas();
                int lastId = getLastId();
                lanzarDetalles(lastId,cod_proveedor,razsocial,razon_comercial);
                dialog.dismiss();
            }
        });

    }

    private void insertarNota(String cod_proveedor, String razsocial,String razon_comercial){
        double total = 0 ;
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        try{
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put(BaseAdapter.NOTAS_CREDITO.COD_PROVEEDOR,cod_proveedor);
            cv.put(BaseAdapter.NOTAS_CREDITO.RAZSOCIAL,razsocial);
            cv.put(BaseAdapter.NOTAS_CREDITO.RAZON_COMERCIAL,razon_comercial);
            cv.put(BaseAdapter.NOTAS_CREDITO.ESTADO,"Generada");
            cv.put(BaseAdapter.NOTAS_CREDITO.TOTAL,total);
            cv.put(BaseAdapter.NOTAS_CREDITO.FECHA,currentDate);

            db.insert(BaseAdapter.NOTAS_CREDITO.TABLE_NAME,null,cv);

            db.close();

        }catch (SQLiteException e){
            msj("Error",e.getMessage());
        }
    }

    private int getLastId(){
        try{
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT "+BaseAdapter.NOTAS_CREDITO.ID+" FROM " + BaseAdapter.NOTAS_CREDITO.TABLE_NAME +
                    " WHERE " + BaseAdapter.NOTAS_CREDITO.ID + "=(SELECT MAX("+BaseAdapter.NOTAS_CREDITO.ID+") FROM "+BaseAdapter.NOTAS_CREDITO.TABLE_NAME+")",null);

            int id = -1 ;

            if (c.moveToFirst()){

                id = c.getInt(0);
            }

            c.close();
            db.close();

            return id ;

        }catch (SQLiteException e){
            msj("Error",e.getMessage());
        }
        return -1 ;
    }

    private void lanzarDetalles(int id ,String cod_proveedor, String razsocial,String razon_comercial){

        Intent detallesNota = new Intent(getActivity(), DetalleNotaCredito.class);
        detallesNota.putExtra("id_nota",id);//
        detallesNota.putExtra("cod_proveedor",cod_proveedor);
        detallesNota.putExtra("razsocial",razsocial);
        detallesNota.putExtra("razon_comercial",razon_comercial);
        detallesNota.putExtra("user",user);
        startActivity(detallesNota);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void loadProveedores(final ProveedorAdapter proveedorAdapter, final List<ModProveedor>proveedorList, final String proveedor){
        try{
            progressDialog.setTitle("Cargando");
            progressDialog.setMessage("Obteniendo proveedores, Porfavor espere...");
            progressDialog.show();

            ContentValues values = new ContentValues();
            values.put("api_key",Configuracion.API_KEY);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            JsonArrayRequest arrProv = new JsonArrayRequest(Request.Method.GET, Configuracion.URL_APIBODEGA
                    + "/proveedor/proveedores" + ((proveedor!=null) ? "/" + proveedor : "") +
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
                    String msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    msj("Error", msg);
                }
            });
            queue.add(arrProv);
        }catch (Exception e){
            msj("Error",e.getMessage());
            if (progressDialog.isShowing()) progressDialog.dismiss();
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

    @Override
    public void onResume() {
        notas.clear();
        cargarNotas();
        super.onResume();
    }

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
