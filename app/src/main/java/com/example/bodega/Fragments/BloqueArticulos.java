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
import android.os.Build;
import android.os.Bundle;


import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adapters.AdapterBloqueArticulos;
import com.example.bodega.Adapters.AdapterFamilias;
import com.example.bodega.Adapters.BaseAdapter;
import com.example.bodega.Adapters.FiltroArticuloAdapter;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ModBloqueArticulos;
import com.example.bodega.Models.ModFamilia;
import com.example.bodega.Models.ModFiltroArticulo;
import com.example.bodega.Models.ModHablador;
import com.example.bodega.R;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloqueArticulos extends Fragment {
    Configuracion configuracion ;
    private List<ModBloqueArticulos> lista ;
    private AdapterBloqueArticulos adapter ;
    private BaseAdapter baseAdapter;
    private RecyclerView recyclerView;
    private EditText txtCodigo;
    private String user ;
    private String cod_familia = "", familia = "" ;
    private Button btnFAmilia ;

    public BloqueArticulos() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.enviarQpos:
                enviar(lista);
                break;
            case R.id.clearAll:
                limpiarTodo();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            String scanResult = result.getContents();
            if (scanResult != null){
                doCall(scanResult);
            }

        } else {
            Toast toast = Toast.makeText(getActivity(), "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        baseAdapter = new BaseAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bloque_articulos, container, false);

        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        ImageButton btnScan = view.findViewById(R.id.btnScan);
        ImageButton btnBuscarDescripcion = view.findViewById(R.id.btnBuscarDescripcion);
        btnFAmilia = view.findViewById(R.id.btnFamilia);

        lista = new ArrayList<>();
        user = getArguments().getString("user");

        recyclerView = view.findViewById(R.id.rvBlqueArticulos);
        txtCodigo = view.findViewById(R.id.txtCodigo);
        lista = new ArrayList<>();
        cargarLista();
        adapter = new AdapterBloqueArticulos(lista);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanNow();
            }
        });

        btnBuscarDescripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDescripcion();
            }
        });

        txtCodigo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (KeyEvent.ACTION_DOWN == event.getAction()) {
                        if (!txtCodigo.getText().toString().equals("")){
                            doCall(txtCodigo.getText().toString());
                            txtCodigo.setText("");
                        }else{
                            Toast.makeText(getActivity(),"Campo código vacío", Toast.LENGTH_SHORT).show();
                            txtCodigo.setText("");
                            return true ;
                        }
                    }
                    }
                return false;
            }
        });

        btnFAmilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarFamilia();
            }
        });

        adapter.SetOnEliminarListener(new AdapterBloqueArticulos.OnEliminarListener() {
            @Override
            public void onEliminar(final int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Seguro(a) de quitar esta linea de la lista?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarItemDB(lista.get(pos).getCodigo());
                        lista.remove(pos);
                        adapter.notifyItemRemoved(pos);
                    }
                });
                builder.setNegativeButton("Nó", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create() ;
                dialog.show();
            }
        });

        return view;
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

    private void cargarLista() {
        try {
            SQLiteDatabase db = baseAdapter.getReadableDatabase();

            Cursor c = db.rawQuery("SELECT * FROM " + BaseAdapter.BLOQUE_ARTICULOS.TABLE_NAME, null);

            while (c.moveToNext()) {
                lista.add(new ModBloqueArticulos(c.getString(1), c.getString(2)));
            }

            c.close();
            db.close();

        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void doCall(final String codigo) {

        com.example.bodega.Models.ContentValues values = new com.example.bodega.Models.ContentValues();
        values.put("codigo", codigo);
        values.put("api_key", Configuracion.API_KEY);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, configuracion.getUrl() + "/hablador/articulo" + values.toString() +
                "&api_key=" + Configuracion.API_KEY,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject articulo) {
                try {
                    if (articulo.length() > 0) {
                        if (!existeItem(articulo.getString("cod_articulo"))) {
                            addToList(articulo.getString("cod_articulo"), articulo.getString("descripcion"));
                        } else {

                            Toast.makeText(getActivity(), "El artículo está en la lista", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "El artículo no éxiste : " + codigo, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse != null) {
                        msj("Error", new String(error.networkResponse.data, StandardCharsets.UTF_8));
                    } else {
                        msj("Error", error.getMessage());
                    }

                } catch (Exception e) {
                    msj("Error", e.getMessage());
                }
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(request);
    }
    public boolean existeItem(String codigo) {
        for (ModBloqueArticulos item : lista) {
            if (item.getCodigo().equals(codigo)) return true;
        }
        return false;
    }

    public void addToList(String codigo, String descripcion) {
        lista.add(new ModBloqueArticulos(codigo, descripcion));
        adapter.notifyItemInserted(lista.size() - 1);
        recyclerView.scrollToPosition(lista.size() - 1);
        insertToHabladoresDB(codigo, descripcion);
    }

    private void insertToHabladoresDB(String codigo, String descripcion) {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(BaseAdapter.BLOQUE_ARTICULOS.COD_ARTICULO, codigo);
            v.put(BaseAdapter.BLOQUE_ARTICULOS.ARTICULO, descripcion);
            db.insert(BaseAdapter.BLOQUE_ARTICULOS.TABLE_NAME, null, v);
            db.close();
        } catch (SQLiteException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void buscarFamilia(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(),R.layout.dialog_filtro_familia,null);
        builder.setView(view);
        final EditText txtFamilia = view.findViewById(R.id.txtFiltroFamilia);
        RecyclerView recyclerView = view.findViewById(R.id.rvFiltroFamilia);
        final List<ModFamilia> familias = new ArrayList<>();
        final AdapterFamilias adapterFamilias = new AdapterFamilias(familias);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapterFamilias);

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        txtFamilia.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        com.example.bodega.Models.ContentValues values = new com.example.bodega.Models.ContentValues();
                        values.put("api_key", Configuracion.API_KEY);
                        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                                "/filtrar_familia" + (!txtFamilia.getText().toString().equals("") ? "/" + txtFamilia.getText().toString() : "") + values.toString(),
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Gson gson = new Gson();
                                        familias.clear();
                                        familias.addAll(Arrays.asList(gson.fromJson(response, ModFamilia[].class)));
                                        adapterFamilias.notifyDataSetChanged();

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
                        //Un minuto de timeout porque puede devolver varios registros y puede demorar
                        request.setRetryPolicy(new DefaultRetryPolicy(60000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        RequestQueue queue = Volley.newRequestQueue(getActivity());
                        queue.add(request);

                        return true ;
                    }
                return false;
            }
        });

        adapterFamilias.setOnItemClickListener(new AdapterFamilias.OnItemClickListener() {
            @Override
            public void OnItemClick(int pos) {
                cod_familia = familias.get(pos).getCod();
                familia = familias.get(pos).getFamilia();
                btnFAmilia.setText(familia);
                dialog.dismiss();
            }
        });
    }

    private void buscarDescripcion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(),R.layout.dialog_filtro_descripcion, null);
        builder.setView(view);
        final EditText txtArticulo = view.findViewById(R.id.txtFiltroDescripcion);
        RecyclerView recyclerView = view.findViewById(R.id.rvResultadoFiltroDescripcion);

        final List<ModFiltroArticulo> articulos = new ArrayList<>();
        final FiltroArticuloAdapter adapter = new FiltroArticuloAdapter(articulos);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        txtArticulo.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        articulos.clear();
                        final Gson gson = new Gson();

                        com.example.bodega.Models.ContentValues values = new com.example.bodega.Models.ContentValues();
                        values.put("descripcion",txtArticulo.getText().toString());
                        values.put("api_key", Configuracion.API_KEY);

                        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                                "/hablador/articulo"+ values.toString(), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    articulos.addAll(Arrays.asList(gson.fromJson(response, ModFiltroArticulo[].class)));
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
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

                        //Un minuto de timeout porque puede devolver varios registros y puede demorar
                        request.setRetryPolicy(new DefaultRetryPolicy(60000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        RequestQueue queue = Volley.newRequestQueue(getActivity());
                        queue.add(request);

                        return true;
                    }
                return false;
            }
        });

        adapter.SetOnItemClickListener(new FiltroArticuloAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void OnItemClick(int pos) {
                doCall(articulos.get(pos).getCodigo());
                dialog.dismiss();
            }
        });
    }

    public void enviar(final List<ModBloqueArticulos> lista) {

        if (lista.isEmpty())
            Toast.makeText(getActivity(), "No hay registros aún.", Toast.LENGTH_SHORT).show();
        else {
            Gson gson = new Gson();
            com.example.bodega.Models.ContentValues values = new com.example.bodega.Models.ContentValues();
            values.put("cod_familia",cod_familia);
            values.put("codigos",gson.toJson(lista));
            values.put("api_key",Configuracion.API_KEY);

            StringRequest request = new StringRequest(Request.Method.PUT, configuracion.getUrl() + "/articulo/familia_bloque" + values.toString(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
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
            //Un minuto de timeout porque puede devolver varios registros y puede demorar
            request.setRetryPolicy(new DefaultRetryPolicy(60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(request);
        }
    }


    private void limpiarTodo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eliminar todo.");
        builder.setMessage("Desea eliminar todo de la lista?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lista.clear();
                adapter.notifyDataSetChanged();
                SQLiteDatabase db = baseAdapter.getWritableDatabase();
                db.execSQL("delete from " + BaseAdapter.BLOQUE_ARTICULOS.TABLE_NAME);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void eliminarItemDB(String codigo) {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();
            db.execSQL("DELETE FROM " + BaseAdapter.BLOQUE_ARTICULOS.TABLE_NAME + " WHERE " + BaseAdapter.BLOQUE_ARTICULOS.COD_ARTICULO + "=" + codigo);
            db.close();
        } catch (SQLiteException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void scanNow() {
        IntentIntegrator intentIntegrator = IntentIntegrator.forFragment(BloqueArticulos.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13,IntentIntegrator.EAN_8,IntentIntegrator.UPC_A,IntentIntegrator.UPC_E);
        intentIntegrator.setPrompt("Scan barcode");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
    }

}