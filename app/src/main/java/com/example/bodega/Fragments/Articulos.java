package com.example.bodega.Fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adaptadores.BaseAdapter;
import com.example.bodega.Adaptadores.FiltroArticuloAdapter;
import com.example.bodega.Modelos.Configuracion;
import com.example.bodega.Modelos.ContentValues;
import com.example.bodega.Modelos.ModFamilia;
import com.example.bodega.Modelos.ModFiltroArticulo;
import com.example.bodega.Modelos.ModImpuesto;
import com.example.bodega.Modelos.ModMarca;
import com.example.bodega.Modelos.UnidadMedida;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.example.bodega.R;
import com.google.gson.Gson;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class Articulos extends Fragment {

    private List<ModFamilia> familias;
    private List<ModImpuesto> impuestos;
    private List<ModMarca> marcas;
    private List<UnidadMedida> unidadMedidas;
    private ArrayAdapter<ModFamilia> adapterFamilias;
    private ArrayAdapter<ModImpuesto> adapterImpuestos;
    private ArrayAdapter<ModMarca> adapterMarcas;
    private ArrayAdapter<UnidadMedida> adapterUnidadMedida;
    private Spinner spImpuestos;
    private SearchableSpinner spFamilias, spMarcas;
    private SearchableSpinner spUnidadMedida;
    private EditText txtDescripcion;
    private EditText txtCosto;
    private EditText txtUtilidad;
    private EditText txtVenta;
    private EditText txtFactorMedida;
    private EditText txtCodigo;
    private CheckBox articulo_granel;
    private CheckBox articulo_romana;
    private String cod_articulo;
    private double costo = 0, utilidad = 0, venta = 0;
    private BaseAdapter baseAdapter;
    private Configuracion configuracion;
    private String user;

    public Articulos() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_articulos, container, false);


        user = getArguments().getString("user");


        getConfiguracion();
        baseAdapter = new BaseAdapter(getActivity());

        ImageButton btnBuscarDescripcion = view.findViewById(R.id.btnBuscarDescripcion);

        spFamilias = view.findViewById(R.id.spFamilias);
        spMarcas = view.findViewById(R.id.spMarcas);
        spImpuestos = view.findViewById(R.id.spImpuestos);
        spUnidadMedida = view.findViewById(R.id.spUnidadMedida);

        familias = new ArrayList<>();
        impuestos = new ArrayList<>();
        marcas = new ArrayList<>();
        unidadMedidas = new ArrayList<>();

        adapterFamilias = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, familias);
        adapterImpuestos = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, impuestos);
        adapterMarcas = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, marcas);
        adapterUnidadMedida = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, unidadMedidas);

        spFamilias.setAdapter(adapterFamilias);
        spImpuestos.setAdapter(adapterImpuestos);
        spMarcas.setAdapter(adapterMarcas);
        spUnidadMedida.setAdapter(adapterUnidadMedida);

        populateFamilias();
        populateImpuestos(adapterImpuestos);
        populateMarcas(adapterMarcas);
        populateUnidadMedida();

        txtCodigo = view.findViewById(R.id.txtCodigo);
        txtDescripcion = view.findViewById(R.id.txtDescripcion);
        txtCosto = view.findViewById(R.id.txtCosto);
        txtUtilidad = view.findViewById(R.id.txtUtilidad);
        txtVenta = view.findViewById(R.id.txtVenta);
        txtFactorMedida = view.findViewById(R.id.txtFactorMedida);

        articulo_granel = view.findViewById(R.id.check_granel);
        articulo_romana = view.findViewById(R.id.check_romana);


        txtCosto.setSelectAllOnFocus(true);
        txtUtilidad.setSelectAllOnFocus(true);
        txtVenta.setSelectAllOnFocus(true);

        ImageButton btnScan = view.findViewById(R.id.btnScan);


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
                        if (txtCodigo.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), "No ha digitado ningún código", Toast.LENGTH_SHORT).show();
                        } else {
                            obtArticulo(txtCodigo.getText().toString());
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        btnBuscarDescripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDescripcion();
            }
        });

        txtCosto.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    costo = !txtCosto.getText().toString().equals("") ? Float.valueOf(txtCosto.getText().toString()) : 0;
                    txtVenta.setText(String.valueOf(setVenta(costo, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), utilidad)));
                    //txtUtilidad.requestFocus();
                    return true;
                }
                return false;
            }
        });

        txtUtilidad.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    utilidad = !txtUtilidad.getText().toString().equals("") ? Float.valueOf(txtUtilidad.getText().toString()) : 0;
                    txtVenta.setText(String.valueOf(setVenta(costo, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), utilidad)));
                    spImpuestos.requestFocus();
                    return true;
                }
                return false;
            }
        });

        txtVenta.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    venta = !txtVenta.getText().toString().equals("") ? Float.valueOf(txtVenta.getText().toString()) : 0;
                    txtUtilidad.setText(String.valueOf(setUtilidad(venta, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), costo)));
                    return true;
                }
                return false;
            }
        });

        spImpuestos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                utilidad = !txtUtilidad.getText().toString().equals("") ? Float.valueOf(txtUtilidad.getText().toString()) : 0;
                txtVenta.setText(String.valueOf(setVenta(costo, impuestos.get(position).getImpuesto(), utilidad)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txtCodigo.requestFocus();

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

    private boolean validarTextos(EditText txtDescripcion, EditText txtCosto, EditText txtUtilidad,
                                  EditText txtVenta, EditText txtFactorMedida) {
        if (txtDescripcion.getText().toString().equals("") ||
                txtCosto.getText().toString().equals("") ||
                txtUtilidad.getText().toString().equals("") ||
                txtVenta.getText().toString().equals("") ||
                txtFactorMedida.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Hay campos vacíos", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
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
                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                            "/articulos/?descripcion=" + txtArticulo.getText().toString() +
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

                    queue.add(request);
                    return true;
                }
                return false;
            }
        });

        adapter.SetOnItemClickListener(new FiltroArticuloAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int pos) {
                obtArticulo(articulos.get(pos).getCodigo());
                dialog.dismiss();
            }
        });
    }

    private void populateFamilias() {
        try {
            final Gson gson = new Gson();
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, configuracion.getUrl() + "/familias/" +
                    "?host_db=" + configuracion.getHost_db() +
                    "&port_db=" + configuracion.getPort_db() +
                    "&user_name=" + configuracion.getUser_name() +
                    "&password=" + configuracion.getPassword() +
                    "&db_name=" + configuracion.getDatabase() +
                    "&schema=" + configuracion.getSchema(), null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response.length() > 0) {
                        familias.clear();
                        familias.addAll(Arrays.asList(gson.fromJson(response.toString(), ModFamilia[].class)));
                        adapterFamilias.notifyDataSetChanged();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    msj("Error", error.getMessage());
                }
            });
            queue.add(jsonArrayRequest);
        } catch (Exception e) {
            msj("Error", e.getMessage());
        }

    }

    private void populateImpuestos(final ArrayAdapter<ModImpuesto> adapterImpuestos) {
        try {
            final Gson gson = new Gson();
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, configuracion.getUrl() + "/impuestos/" +
                    "?host_db=" + configuracion.getHost_db() +
                    "&port_db=" + configuracion.getPort_db() +
                    "&user_name=" + configuracion.getUser_name() +
                    "&password=" + configuracion.getPassword() +
                    "&db_name=" + configuracion.getDatabase() +
                    "&schema=" + configuracion.getSchema(), null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response.length() > 0) {
                        impuestos.clear();
                        impuestos.addAll(Arrays.asList(gson.fromJson(response.toString(), ModImpuesto[].class)));
                        adapterImpuestos.notifyDataSetChanged();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    msj("Error", error.getMessage());
                }
            });
            queue.add(jsonArrayRequest);
        } catch (Exception e) {
            msj("Error", e.getMessage());
        }
    }

    private void populateMarcas(final ArrayAdapter<ModMarca> adapterMarcas) {
        try {
            final Gson gson = new Gson();
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, configuracion.getUrl() + "/marcas/" +
                    "?host_db=" + configuracion.getHost_db() +
                    "&port_db=" + configuracion.getPort_db() +
                    "&user_name=" + configuracion.getUser_name() +
                    "&password=" + configuracion.getPassword() +
                    "&db_name=" + configuracion.getDatabase() +
                    "&schema=" + configuracion.getSchema(), null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    marcas.clear();
                    marcas.addAll(Arrays.asList(gson.fromJson(response.toString(), ModMarca[].class)));
                    adapterMarcas.notifyDataSetChanged();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    msj("Error", error.getMessage());
                }
            });
            queue.add(jsonArrayRequest);
        } catch (Exception e) {
            msj("Error", e.getMessage());
        }
    }

    private void populateUnidadMedida() {
        try {
            final Gson gson = new Gson();
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() + "/unidad_medida/" +
                    "?host_db=" + configuracion.getHost_db() +
                    "&port_db=" + configuracion.getPort_db() +
                    "&user_name=" + configuracion.getUser_name() +
                    "&password=" + configuracion.getPassword() +
                    "&db_name=" + configuracion.getDatabase() +
                    "&schema=" + configuracion.getSchema(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    unidadMedidas.clear();
                    unidadMedidas.addAll(Arrays.asList(gson.fromJson(response, UnidadMedida[].class)));
                    adapterUnidadMedida.notifyDataSetChanged();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    msj("Error", error.getMessage());
                }
            });
            queue.add(request);
        } catch (Exception e) {
            msj("Error", e.getMessage());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_articulos, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aplicar:
                if (validarTextos(txtDescripcion, txtCosto, txtUtilidad, txtVenta, txtFactorMedida)) {
                    aplicarCambios();
                }
                break;
            case R.id.agregar_a_cola:
                if (validarTextos(txtDescripcion, txtCosto, txtUtilidad, txtVenta, txtFactorMedida)) {
                    addToList(cod_articulo, txtDescripcion.getText().toString(), Double.valueOf(txtVenta.getText().toString()));
                    Toast.makeText(getActivity(), "Agregando a la cola de habladores", Toast.LENGTH_LONG).show();
                }
                break;
        }
        return false;
    }

    public void addToList(String codigo, String descripcion, double precio) {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();
            android.content.ContentValues v = new android.content.ContentValues();
            v.put(BaseAdapter.HABLADORES.CODIGO, codigo);
            v.put(BaseAdapter.HABLADORES.DESCRIPCION, descripcion);
            v.put(BaseAdapter.HABLADORES.PRECIO, precio);
            db.insert(BaseAdapter.HABLADORES.TABLE_NAME, null, v);
            db.close();
        } catch (SQLiteException e) {
            msj("Error", e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            String scanResult = result.getContents();
            obtArticulo(scanResult);
        } else {
            Toast toast = Toast.makeText(getActivity(), "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void obtArticulo(final String codigo) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() + "/articulos/" +
                "?codigo=" + codigo +
                "&host_db=" + configuracion.getHost_db() +
                "&port_db=" + configuracion.getPort_db() +
                "&user_name=" + configuracion.getUser_name() +
                "&password=" + configuracion.getPassword() +
                "&db_name=" + configuracion.getDatabase() +
                "&schema=" + configuracion.getSchema(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject articulo = new JSONObject(response);
                    if (articulo.length() > 0) {

                        txtDescripcion.setText(articulo.getString("descripcion"));
                        int pos = indexOfFamilias(familias, articulo.getString("cod_familia"));
                        spFamilias.setSelection(pos);
                        pos = indexOfMarcas(marcas, articulo.getString("cod_marca"));
                        spMarcas.setSelection(pos);
                        txtCosto.setText(articulo.getString("costo"));
                        txtUtilidad.setText(articulo.getString("utilidad"));
                        txtVenta.setText(articulo.getString("venta"));
                        cod_articulo = codigo;
                        costo = articulo.getDouble("costo");
                        pos = indexOfImpuestos(impuestos, articulo.getString("cod_impuesto"));
                        spImpuestos.setSelection(pos);
                        pos = indexOfUnidadMedida(unidadMedidas, articulo.getString("unidad_medida"));
                        spUnidadMedida.setSelection(pos);
                        txtFactorMedida.setText(articulo.getString("factor_medida"));
                        utilidad = articulo.getDouble("utilidad");
                        venta = articulo.getDouble("venta");
                        articulo_granel.setChecked(articulo.getString("art_granel").equals("S"));
                        articulo_romana.setChecked(articulo.getString("articulo_romana").equals("S"));
                        txtCodigo.setText("");
                        txtCodigo.requestFocus();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("No registrado").setMessage("El artículo no está registrado \nDesea registrarlo ahora?");
                        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                nuevoArticulo(codigo);
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
        queue.add(request);
    }

    private int indexOfUnidadMedida(final List<UnidadMedida> list, String value) {
        for (int index = 0; index <= list.size() - 1; index++) {
            if (list.get(index).getUnidad_medida().equals(value)) {
                return index;
            }
        }
        return -1;
    }

    private int indexOfFamilias(final List<ModFamilia> list, String value) {
        for (int index = 0; index <= list.size() - 1; index++) {
            if (list.get(index).getCod().equals(value)) {
                return index;
            }
        }
        return -1;
    }

    private int indexOfMarcas(final List<ModMarca> list, String value) {
        for (int index = 0; index <= list.size() - 1; index++) {
            if (list.get(index).getCod_marca().equals(value)) {
                return index;
            }
        }
        return -1;
    }

    private int indexOfImpuestos(final List<ModImpuesto> list, String value) {
        for (int index = 0; index <= list.size() - 1; index++) {
            if (list.get(index).getCodigo().equals(value)) {
                return index;
            }
        }
        return -1;
    }


    public void scanNow() {
        IntentIntegrator intentIntegrator = IntentIntegrator.forFragment(Articulos.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt("Scan barcode");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
        txtCodigo.setText("");
        txtCodigo.requestFocus();
    }

    private double setUtilidad(double venta, double impuesto, double costo) {
        try {
            double temp = (venta * 100) / (100 + impuesto);
            return (((temp * 100) / costo) - 100);
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return 0;
    }

    private double setVenta(double costo, double impuesto, double utilidad) {
        try {
            double temp = costo * (100 + impuesto) / 100;
            temp *= (100 + utilidad) / 100;
            return temp;
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return 0;
    }

    private void aplicarCambios() {
        try {

            ContentValues params = new ContentValues();

            params.put("host_db" , configuracion.getHost_db()) ;
            params.put("port_db" , configuracion.getPort_db()) ;
            params.put("user_name" , configuracion.getUser_name()) ;
            params.put("password" , configuracion.getPassword());
            params.put("db_name" , configuracion.getDatabase());
            params.put("schema" , configuracion.getSchema());
            params.put("codigo" , cod_articulo);
            params.put("descripcion", txtDescripcion.getText().toString());
            params.put("cod_familia", familias.get(spFamilias.getSelectedItemPosition()).getCod());
            params.put("cod_marca" , marcas.get(spMarcas.getSelectedItemPosition()).getCod_marca());
            params.put("costo" , String.valueOf(costo));
            params.put("utilidad" , String.valueOf(utilidad));
            params.put("cod_impuesto" , impuestos.get(spImpuestos.getSelectedItemPosition()).getCodigo());
            params.put("impuesto", String.valueOf(impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto()));
            params.put("venta", String.valueOf(venta));
            params.put("unidad_medida" ,unidadMedidas.get(spUnidadMedida.getSelectedItemPosition()).getUnidad_medida());
            params.put("factor_medida", txtFactorMedida.getText().toString());
            params.put("art_granel" , (articulo_granel.isChecked() ? "S" : "N"));
            params.put("articulo_romana" , (articulo_romana.isChecked() ? "S" : "N"));

            AsyncHttpClient cliente = new AsyncHttpClient();
            String urla = configuracion.getUrl() + "/articulos/" + params.toString() ;
            String urlb = URLEncoder.encode(urla,"UTF-8");

            cliente.put(urlb, null, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    msj("Error: " + statusCode, responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
                }
            });

            /*
            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            StringRequest request = new StringRequest(Request.Method.PUT, configuracion.getUrl() + "/articulos/" + params.toString() , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse statusCode = error.networkResponse;
                    Toast.makeText(getActivity(), "Código error: " + statusCode + " \n " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            request.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request); */
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void nuevoArticulo(final String codigo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.nuevo_articulo, null);
        builder.setView(view);
        final SearchableSpinner spFamilias = view.findViewById(R.id.spFamilias);
        spFamilias.setAdapter(adapterFamilias);
        final SearchableSpinner spMarcas = view.findViewById(R.id.spMarcas);
        spMarcas.setAdapter(adapterMarcas);
        final Spinner spImpuestos = view.findViewById(R.id.spImpuestos);
        spImpuestos.setAdapter(adapterImpuestos);
        spUnidadMedida = view.findViewById(R.id.spUnidadMedida);
        spUnidadMedida.setAdapter(adapterUnidadMedida);
        final EditText txtDescripcion = view.findViewById(R.id.txtDescripcion);
        final EditText txtCosto = view.findViewById(R.id.txtCosto);
        final EditText txtUtilidad = view.findViewById(R.id.txtUtilidad);
        final EditText txtVenta = view.findViewById(R.id.txtVenta);
        final EditText txtFactorMedida = view.findViewById(R.id.txtFactorMedida);
        final CheckBox articulo_granel = view.findViewById(R.id.check_granel);
        final CheckBox articulo_romana = view.findViewById(R.id.check_romana);

        txtFactorMedida.setText("1");

        txtCosto.setSelectAllOnFocus(true);
        txtUtilidad.setSelectAllOnFocus(true);
        txtVenta.setSelectAllOnFocus(true);
        txtFactorMedida.setSelectAllOnFocus(true);

        builder.setPositiveButton("GUARDAR", null);

        builder.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnGuardar = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                btnGuardar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (validarTextos(txtDescripcion, txtCosto, txtUtilidad, txtVenta, txtFactorMedida)) {
                            guardarArticulo(codigo, txtDescripcion.getText().toString(),
                                    impuestos.get(spImpuestos.getSelectedItemPosition()).getCodigo(),
                                    familias.get(spFamilias.getSelectedItemPosition()).getCod(),
                                    marcas.get(spMarcas.getSelectedItemPosition()).getCod_marca(),
                                    unidadMedidas.get(spUnidadMedida.getSelectedItemPosition()).getUnidad_medida(),
                                    txtFactorMedida.getText().toString(), (articulo_granel.isChecked() ? "S" : "N"),
                                    (articulo_romana.isChecked() ? "S" : "N"), Double.valueOf(txtCosto.getText().toString()),
                                    Double.valueOf(txtUtilidad.getText().toString()), Double.valueOf(txtVenta.getText().toString()),
                                    impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto());
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        txtCosto.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    costo = !txtCosto.getText().toString().equals("") ? Float.valueOf(txtCosto.getText().toString()) : 0;
                    txtVenta.setText(String.valueOf(setVenta(costo, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), utilidad)));
                    //txtUtilidad.requestFocus();
                    return true;
                }
                return false;
            }
        });

        txtUtilidad.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    utilidad = !txtUtilidad.getText().toString().equals("") ? Float.valueOf(txtUtilidad.getText().toString()) : 0;
                    txtVenta.setText(String.valueOf(setVenta(costo, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), utilidad)));
                    spImpuestos.requestFocus();
                    return true;
                }
                return false;
            }
        });

        txtVenta.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    venta = !txtVenta.getText().toString().equals("") ? Float.valueOf(txtVenta.getText().toString()) : 0;
                    txtUtilidad.setText(String.valueOf(setUtilidad(venta, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), costo)));
                    return true;
                }
                return false;
            }
        });

        spImpuestos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                utilidad = !txtUtilidad.getText().toString().equals("") ? Float.valueOf(txtUtilidad.getText().toString()) : 0;
                txtVenta.setText(String.valueOf(setVenta(costo, impuestos.get(position).getImpuesto(), utilidad)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dialog.show();

    }

    private void guardarArticulo(final String codigo, final String descripcion,
                                 final String cod_impuesto, final String cod_familia,
                                 final String cod_marca, final String unidad_medida,
                                 final String factor_medida, final String articulo_granel,
                                 final String articulo_romana, final double costo,
                                 final double utilidad, final double venta, final double impuesto) {


        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest request = new StringRequest(Request.Method.POST, configuracion.getUrl() + "/articulos/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                msj("Error", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("host_db", configuracion.getHost_db());
                parametros.put("port_db", configuracion.getPort_db());
                parametros.put("user_name", configuracion.getUser_name());
                parametros.put("password", configuracion.getPassword());
                parametros.put("db_name", configuracion.getDatabase());
                parametros.put("schema", configuracion.getSchema());
                parametros.put("codigo", codigo);
                parametros.put("descripcion", descripcion);
                parametros.put("cod_impuesto", cod_impuesto);
                parametros.put("cod_familia", cod_familia);
                parametros.put("cod_marca", cod_marca);
                parametros.put("unidad_medida", unidad_medida);
                parametros.put("factor_medida", factor_medida);
                parametros.put("art_granel", articulo_granel);
                parametros.put("articulo_romana", articulo_romana);
                parametros.put("costo", String.valueOf(costo));
                parametros.put("utilidad", String.valueOf(utilidad));
                parametros.put("venta", String.valueOf(venta));
                parametros.put("impuesto", String.valueOf(impuesto));
                parametros.put("user", user);
                return parametros;
            }
        };

        queue.add(request);

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
