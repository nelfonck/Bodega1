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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.bodega.Adapters.BaseAdapter;
import com.example.bodega.Adapters.FiltroArticuloAdapter;
import com.example.bodega.Adapters.HabladoresAdapter;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.InformeErrores;
import com.example.bodega.Models.ModFiltroArticulo;
import com.example.bodega.Models.ModHablador;
import com.example.bodega.Models.ModSalidas;
import com.example.bodega.R;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Habladores extends Fragment {
    private RecyclerView recyclerView;
    private List<ModHablador> lista;
    private String user ;
    private HabladoresAdapter adapter;
    private EditText txtCodigo;
    private BaseAdapter baseAdapter;
    public static final String TAG = "Tag";
    InformeErrores informeErrores ;
    private static String url = "http://201.192.158.233:82/apibodega/public/hablador";
    private static String api_key = "$2y$10$ww4b.izY6lDO/.YgQGu4VeIeN5f8YlIgjNDXsZZmDsHBfJCdiyKXC";

    public Habladores() {
        // Required empty public constructor
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
        View view = inflater.inflate(R.layout.fragment_habladores, container, false);

        ImageButton btnScan = view.findViewById(R.id.btnScan);
        ImageButton btnBuscarDescripcion = view.findViewById(R.id.btnBuscarDescripcion);

        recyclerView = view.findViewById(R.id.recyclerView);
        txtCodigo = view.findViewById(R.id.txtCodigo);
        lista = new ArrayList<>();

        informeErrores = new InformeErrores(getActivity());


        user = getArguments().getString("user");

        cargarLista();

        adapter = new HabladoresAdapter(lista);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanNow();
            }
        });

        txtCodigo.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (KeyEvent.ACTION_DOWN == event.getAction()) {
                        doCall(txtCodigo.getText().toString());
                        txtCodigo.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        txtCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnBuscarDescripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDescripcion();
            }
        });

        adapter.setEliminarItem(new HabladoresAdapter.EliminarItem() {
            @Override
            public void OnItemClick(int pos) {
                eliminarItemDB(lista.get(pos).getcodigo());
                lista.remove(pos);
                adapter.notifyItemRemoved(pos);
            }
        });

        return view;
    }


    private void buscarDescripcion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_filtro_descripcion, (ViewGroup)null);
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
                        values.put("api_key",txtArticulo.getText().toString());

                        StringRequest request = new StringRequest(Request.Method.GET, url +
                                "/descripcion/"+ values.toString(), new Response.Listener<String>() {
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
                                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            String scanResult = result.getContents();
            doCall(scanResult);
        } else {
            Toast toast = Toast.makeText(getActivity(), "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void scanNow() {
        IntentIntegrator intentIntegrator = IntentIntegrator.forFragment(Habladores.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13,IntentIntegrator.EAN_8,IntentIntegrator.UPC_A,IntentIntegrator.UPC_E);
        intentIntegrator.setPrompt("Scan barcode");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
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
                db.execSQL("delete from " + BaseAdapter.HABLADORES.TABLE_NAME);
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

    private void cargarLista() {
        try {
            SQLiteDatabase db = baseAdapter.getReadableDatabase();

            Cursor c = db.rawQuery("SELECT * FROM " + BaseAdapter.HABLADORES.TABLE_NAME, null);

            while (c.moveToNext()) {
                lista.add(new ModHablador(c.getString(0), c.getString(1), c.getDouble(2)));
            }

            c.close();
            db.close();

        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void doCall(final String codigo) {

                com.example.bodega.Models.ContentValues values = new com.example.bodega.Models.ContentValues();
                values.put("codigo",codigo);
                values.put("api_key", api_key);

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url + "/articulo/"+values.toString(),
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject articulo) {
                        try {
                         if (!existeItem(articulo.getString("cod_articulo")))
                         {
                             if (articulo.length()>0){

                                 addToList(articulo.getString("cod_articulo"), articulo.getString("descripcion"), articulo.getDouble("venta"));

                             }else{
                                 Toast.makeText(getActivity(), "El artículo no éxiste : "+ codigo, Toast.LENGTH_SHORT).show();
                             }
                         }else{
                             Toast.makeText(getActivity(), "El artículo está en la lista", Toast.LENGTH_SHORT).show();
                         }
                        } catch (JSONException e) {
                            msj("Error",e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        msj("Error",error.getMessage());
                    }
                });

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                queue.add(request);
    }

    public void activarArticulo(final String codigo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Activar");
        builder.setMessage("Artículo inactivo\nDesea activar este artículo?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    com.example.bodega.Models.ContentValues values = new com.example.bodega.Models.ContentValues();
                    values.put("codigo",codigo);
                    values.put("api_key", api_key);

                    StringRequest stringRequest = new StringRequest(Request.Method.PUT, url+
                            "/activar/"+ values.toString(), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            doCall(codigo);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            msj("Error", error.getMessage());
                        }
                    });

                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    queue.add(stringRequest);

                } catch (Exception e) {
                    msj("Error",e.getMessage());
                }
                dialog.dismiss();
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

    public void enviar(final List<ModHablador> lista) {
        try{
            if (lista.isEmpty())
                Toast.makeText(getActivity(), "No hay registros aún.", Toast.LENGTH_SHORT).show();
            else {
                StringRequest request = new StringRequest(Request.Method.POST, url + "/guardar/", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                            Toast.makeText(getActivity(),response, Toast.LENGTH_SHORT).show();
                            lista.clear();
                            adapter.notifyDataSetChanged();
                            SQLiteDatabase db = baseAdapter.getWritableDatabase();
                            db.execSQL("delete from " + BaseAdapter.HABLADORES.TABLE_NAME);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        String err ;
                        if (error.getMessage() != null)
                        {
                            err = error.getMessage();
                        }else{
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            err = new String(htmlBodyBytes) ;
                        }
                        informeErrores.enviar("Ha ocurrido un error",err);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Gson gson = new Gson();
                        final String jsonList = gson.toJson(lista);
                        Map<String, String> params = new HashMap<>();
                        params.put("api_key",api_key);
                        params.put("user",user);
                        params.put("lista", jsonList);
                        return params;
                    }
                };
                request.setRetryPolicy(new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                queue.add(request);
            }
        }catch (Exception e){
            informeErrores.enviar("Error",e.getMessage());
        }
    }


    public boolean existeItem(String codigo) {
        for (ModHablador item : lista) {
            if (item.getcodigo().toString().equals(codigo)) return true;
        }
        return false;
    }

    public void addToList(String codigo, String descripcion, double precio) {
        lista.add(new ModHablador(codigo, descripcion, precio));
        adapter.notifyItemInserted(lista.size() - 1);
        recyclerView.scrollToPosition(lista.size() - 1);
        insertToHabladoresDB(codigo, descripcion, precio);
    }

    private void insertToHabladoresDB(String codigo, String descripcion, double precio) {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(BaseAdapter.HABLADORES.CODIGO, codigo);
            v.put(BaseAdapter.HABLADORES.DESCRIPCION, descripcion);
            v.put(BaseAdapter.HABLADORES.PRECIO, precio);
            db.insert(BaseAdapter.HABLADORES.TABLE_NAME, null, v);
            db.close();
        } catch (SQLiteException e) {
            msj("Error", e.getMessage());
        }
    }

    private void eliminarItemDB(String codigo) {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();
            db.execSQL("DELETE FROM " + BaseAdapter.HABLADORES.TABLE_NAME + " WHERE " + BaseAdapter.HABLADORES.CODIGO + "=" + codigo);
            db.close();
        } catch (SQLiteException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
