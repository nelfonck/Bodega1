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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
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
import com.example.bodega.Models.ModFiltroArticulo;
import com.example.bodega.Models.ModHablador;
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


public class Habladores extends Fragment {
    private RecyclerView recyclerView;
    private List<ModHablador> lista;

    private HabladoresAdapter adapter;
    private EditText txtCodigo;
    private BaseAdapter baseAdapter;
    public static final String TAG = "Tag";
    RequestQueue queue;
    Configuracion configuracion;

    public Habladores() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        baseAdapter = new BaseAdapter(getActivity());
        queue = Volley.newRequestQueue(getActivity());
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

        getConfiguracion();

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

    private void buscarDescripcion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_filtro_descripcion, null);
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
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    articulos.clear();
                    final Gson gson = new Gson();
                    StringRequest request = null;
                    try {
                        request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                                "/articulos/?descripcion=" + URLEncoder.encode(txtArticulo.getText().toString(),"utf-8")+
                                "&host_db=" + configuracion.getHost_db() +
                                "&port_db=" + configuracion.getPort_db() +
                                "&user_name=" + configuracion.getUser_name() +
                                "&password=" + configuracion.getPassword() +
                                "&db_name=" + configuracion.getDatabase() +
                                "&schema=" + configuracion.getSchema(), new Response.Listener<String>() {
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
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    request.setTag(TAG);
                    queue.add(request);
                    queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                        @Override
                        public void onRequestFinished(Request<Object> request) {
                            queue.getCache().clear();
                        }
                    });
                    return true;
                }
                return false;
            }
        });

        adapter.SetOnItemClickListener(new FiltroArticuloAdapter.OnItemClickListener() {
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


    public void doCall(final String codigo) {
        if (!existeItem(codigo)) {

            JsonObjectRequest request = null;
            try {
                request = new JsonObjectRequest(Request.Method.GET, configuracion.getUrl() +
                        "/habladores/?codigo=" + URLEncoder.encode(codigo,"utf-8") +
                        "&host_db=" + configuracion.getHost_db() +
                        "&port_db=" + configuracion.getPort_db() +
                        "&user_name=" + configuracion.getUser_name() +
                        "&password=" + configuracion.getPassword() +
                        "&db_name=" + configuracion.getDatabase() +
                        "&schema=" + configuracion.getSchema(),null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject articulo) {
                        try {

                            if (articulo.length()>0){
                                if (articulo.getBoolean("en_cola")) {
                                    Toast.makeText(getActivity(), "El artículo está en proceso de impresión en QPOS...", Toast.LENGTH_LONG).show();
                                }else
                                    if (articulo.getString("activo").equals("S")) {
                                    addToList(articulo.getString("codigo"), articulo.getString("descripcion"), articulo.getDouble("venta"));
                                } else {
                                    activarArticulo(articulo.getString("codigo"));
                                }
                            }else{
                                Toast.makeText(getActivity(), "El artículo no éxiste : "+ codigo, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            request.setTag(TAG);
            queue.add(request);
            queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                @Override
                public void onRequestFinished(Request<Object> request) {
                    queue.getCache().clear();
                }
            });
        } else
            Toast.makeText(getActivity(), "El artículo ya está en la lista.", Toast.LENGTH_SHORT).show();
    }

    public void activarArticulo(final String codigo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Activar");
        builder.setMessage("Artículo inactivo\nDesea activar este artículo?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    StringRequest stringRequest = new StringRequest(Request.Method.PUT, configuracion.getUrl() +
                            "/habladores/?codigo=" + URLEncoder.encode(codigo,"utf-8") +
                            "&host_db=" + configuracion.getHost_db() +
                            "&port_db=" + configuracion.getPort_db() +
                            "&user_name=" + configuracion.getUser_name() +
                            "&password=" + configuracion.getPassword() +
                            "&db_name=" + configuracion.getDatabase() +
                            "&schema=" + configuracion.getSchema(), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            doCall(codigo);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    queue.add(stringRequest);
                    queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                        @Override
                        public void onRequestFinished(Request<Object> request) {
                            queue.getCache().clear();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        if (lista.isEmpty())
            Toast.makeText(getActivity(), "No hay registros aún.", Toast.LENGTH_SHORT).show();
        else {
            StringRequest request = new StringRequest(Request.Method.POST, configuracion.getUrl() + "/habladores/", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
                    lista.clear();
                    adapter.notifyDataSetChanged();
                    SQLiteDatabase db = baseAdapter.getWritableDatabase();
                    db.execSQL("delete from " + BaseAdapter.HABLADORES.TABLE_NAME);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Gson gson = new Gson();
                    final String jsonList = gson.toJson(lista);

                    Map<String, String> params = new HashMap<>();
                    params.put("host_db",configuracion.getHost_db());
                    params.put("port_db",configuracion.getPort_db());
                    params.put("user_name",configuracion.getUser_name());
                    params.put("password",configuracion.getPassword());
                    params.put("db_name",configuracion.getDatabase());
                    params.put("schema",configuracion.getSchema());
                    params.put("lista", jsonList);
                    return params;
                }
            };
            queue.add(request);
            queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                @Override
                public void onRequestFinished(Request<Object> request) {
                    queue.getCache().clear();
                }
            });
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
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
