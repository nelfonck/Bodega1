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
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adapters.BaseAdapter;
import com.example.bodega.Adapters.FiltroArticuloAdapter;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.Models.InformeErrores;
import com.example.bodega.Models.ModFamilia;
import com.example.bodega.Models.ModFiltroArticulo;
import com.example.bodega.Models.ModImpuesto;
import com.example.bodega.Models.ModMarca;
import com.example.bodega.Models.UnidadMedida;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.example.bodega.R;
import com.google.gson.Gson;

import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
    private TextView tvProveedor ;
    private EditText txtDesc1, txtDesc2, txtDesc3 ;
    private CheckBox articulo_granel;
    private CheckBox articulo_romana;
    private Switch activo ;
    private String cod_articulo;
    private double costo = 0, utilidad = 0, venta = 0;
    private BaseAdapter baseAdapter;

    private String user;
    private ProgressDialog progress;
    private boolean block;
    private String cod_proveedor, razsocial ;
    private InformeErrores informeErrores ;

    public Articulos() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_articulos, container, false);

        final DecimalFormat formatter = new DecimalFormat("#");
        formatter.setMaximumFractionDigits(2);

        informeErrores = new InformeErrores(getActivity());

        user = getArguments().getString("user");

        progress = new ProgressDialog(getActivity());

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

        tvProveedor = view.findViewById(R.id.tvProveedor);

        txtDesc1 = view.findViewById(R.id.txtDesc1);
        txtDesc2 = view.findViewById(R.id.txtDesc2);
        txtDesc3 = view.findViewById(R.id.txtDesc3);

        articulo_granel = view.findViewById(R.id.check_granel);
        articulo_romana = view.findViewById(R.id.check_romana);

        activo = view.findViewById(R.id.activo);

        txtCosto.setSelectAllOnFocus(true);
        txtUtilidad.setSelectAllOnFocus(true);
        txtVenta.setSelectAllOnFocus(true);
        txtDesc1.setSelectAllOnFocus(true);
        txtDesc2.setSelectAllOnFocus(true);
        txtDesc3.setSelectAllOnFocus(true);

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
                    costo = (!txtCosto.getText().toString().equals("") ? Double.valueOf(txtCosto.getText().toString()) : 0);
                    txtVenta.setText(formatter.format(setVenta(costo, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), utilidad)));
                    return true;
                }
                return false;
            }
        });

        txtUtilidad.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    utilidad = (!txtUtilidad.getText().toString().equals("") ? Double.valueOf(txtUtilidad.getText().toString()) : 0);
                    txtVenta.setText(formatter.format(setVenta(costo, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), utilidad)));
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
                    venta = (!txtVenta.getText().toString().equals("") ? Double.valueOf(txtVenta.getText().toString()) : 0);
                    txtUtilidad.setText(formatter.format(setUtilidad(venta, impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto(), costo)));
                    return true;
                }
                return false;
            }
        });

        spImpuestos.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                block = false;
                return false;
            }
        });

        spImpuestos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!block) {
                    txtVenta.setText(formatter.format(setVenta(costo, impuestos.get(position).getImpuesto(), utilidad)));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txtCodigo.requestFocus();

        return view;
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
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        ContentValues values = new ContentValues() ;
                        values.put("api_key",Configuracion.API_KEY);
                        values.put("descripcion",txtArticulo.getText().toString());

                        final Gson gson = new Gson();
                        StringRequest request = new StringRequest(Request.Method.GET, Configuracion.URL_APIBODEGA +
                                 "/articulo/articulo" + values.toString(), new Response.Listener<String>() {
                             @Override
                             public void onResponse(String response) {
                                 try {
                                     articulos.clear();
                                     articulos.addAll(Arrays.asList(gson.fromJson(response, ModFiltroArticulo[].class)));
                                     adapter.notifyDataSetChanged();
                                 } catch (Exception e) {
                                     Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                 }

                             }

                         }, new Response.ErrorListener() {
                             @Override
                             public void onErrorResponse(VolleyError error) {
                                 informeErrores.enviar("Error",new String(error.networkResponse.data, StandardCharsets.UTF_8));
                             }
                         });

                        request.setRetryPolicy(
                                new DefaultRetryPolicy(50000,
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
            @Override
            public void OnItemClick(int pos) {
                obtArticulo(articulos.get(pos).getCodigo());
                dialog.dismiss();
            }
        });
    }

    private void populateFamilias() {

            final Gson gson = new Gson();
            ContentValues values = new ContentValues();
            values.put("api_key",Configuracion.API_KEY);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Configuracion.URL_APIBODEGA + "/familia/" +
                    values.toString(), null, new Response.Listener<JSONArray>() {
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
                    informeErrores.enviar("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                }
            });

            jsonArrayRequest.setRetryPolicy(
                    new DefaultRetryPolicy(50000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(jsonArrayRequest);
    }

    private void populateImpuestos(final ArrayAdapter<ModImpuesto> adapterImpuestos) {

            final Gson gson = new Gson();
            ContentValues values = new ContentValues();
            values.put("api_key",Configuracion.API_KEY);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Configuracion.URL_APIBODEGA + "/impuesto/" +
                    values.toString(), null, new Response.Listener<JSONArray>() {
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
                    informeErrores.enviar("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                }
            });

            jsonArrayRequest.setRetryPolicy(
                    new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(jsonArrayRequest);

    }

    private void populateMarcas(final ArrayAdapter<ModMarca> adapterMarcas) {

            final Gson gson = new Gson();
            ContentValues values = new ContentValues();
            values.put("api_key",Configuracion.API_KEY);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Configuracion.URL_APIBODEGA + "/marca/" +
                 values.toString(), null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    marcas.clear();
                    marcas.addAll(Arrays.asList(gson.fromJson(response.toString(), ModMarca[].class)));
                    adapterMarcas.notifyDataSetChanged();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    informeErrores.enviar("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                }
            });

            jsonArrayRequest.setRetryPolicy(
                new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(jsonArrayRequest);
    }

    private void populateUnidadMedida() {

            final Gson gson = new Gson();
            ContentValues values = new ContentValues();
            values.put("api_key",Configuracion.API_KEY);

            StringRequest request = new StringRequest(Request.Method.GET, Configuracion.URL_APIBODEGA + "/unidad_medida/" +
            values.toString(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    unidadMedidas.clear();
                    unidadMedidas.addAll(Arrays.asList(gson.fromJson(response, UnidadMedida[].class)));
                    adapterUnidadMedida.notifyDataSetChanged();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    informeErrores.enviar("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                }
            });

            request.setRetryPolicy(
                new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(request);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_articulos, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                    if (existeItem(cod_articulo)){
                        Toast.makeText(getActivity(), "El artículo ya está en la lista", Toast.LENGTH_SHORT).show();
                    }else{
                        addToList(cod_articulo,txtDescripcion.getText().toString(),venta);
                    }
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
            informeErrores.enviar("Error",e.getMessage());
        }
    }

    private boolean existeItem(String codigo) {
        SQLiteDatabase db = baseAdapter.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + BaseAdapter.HABLADORES.TABLE_NAME + " WHERE " + BaseAdapter.HABLADORES.CODIGO + "=?", new String[]{codigo});

        if (c.getCount() > 0) {
            c.close();
            db.close();
            return true;
        } else {
            c.close();
            db.close();
            return false;
        }
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            String scanResult = result.getContents();
            if (scanResult!=null){
                obtArticulo(scanResult);
            }

        } else {
            Toast toast = Toast.makeText(getActivity(), "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void obtArticulo(final String codigo) {
        block = true ;
        progress.setTitle("Obteniendo datos");
        progress.setMessage("Porfavor espere...");
        if (!progress.isShowing()) progress.show();

        ContentValues values = new ContentValues();
        values.put("api_key",Configuracion.API_KEY);
        values.put("codigo",codigo);
        StringRequest request = new StringRequest(Request.Method.GET, Configuracion.URL_APIBODEGA + "/articulo/articulo/"  +
            values.toString(), new Response.Listener<String>() {
             @Override
             public void onResponse(String response) {
                 try {
                     JSONObject articulo = new JSONObject(response);
                     if (articulo.length() > 0) {
                         DecimalFormat formatter = new DecimalFormat("#");
                         formatter.setMaximumFractionDigits(2);

                         txtDescripcion.setText(articulo.getString("descripcion"));
                         int pos = indexOfFamilias(familias, articulo.getString("cod_familia"));
                         spFamilias.setSelection(pos);
                         pos = indexOfMarcas(marcas, articulo.getString("cod_marca"));
                         spMarcas.setSelection(pos);

                         cod_articulo = articulo.getString("cod_articulo");
                         costo = articulo.getDouble("costo");
                         pos = indexOfImpuestos(impuestos, articulo.getString("cod_impuesto"));
                         spImpuestos.setSelection(pos);
                         pos = indexOfUnidadMedida(unidadMedidas, articulo.getString("unidad_medida"));
                         spUnidadMedida.setSelection(pos);
                         txtFactorMedida.setText(articulo.getString("factor_medida"));
                         utilidad = articulo.getDouble("porcentaje_utilidad");
                         venta = articulo.getDouble("venta");
                         articulo_granel.setChecked(articulo.getString("art_granel").equals("S"));
                         articulo_romana.setChecked(articulo.getString("articulo_romana").equals("S"));

                         txtCosto.setText(formatter.format(costo));
                         txtUtilidad.setText(formatter.format(utilidad));
                         txtVenta.setText(formatter.format(venta));
                         txtDesc1.setText(String.valueOf(articulo.getInt("porc_tope_descuento_1")));
                         txtDesc2.setText(String.valueOf(articulo.getInt("porc_tope_descuento_2")));
                         txtDesc3.setText(String.valueOf(articulo.getInt("porc_tope_descuento_3")));
                         activo.setChecked(articulo.getString("activo").equals("S"));

                         if (!articulo.isNull("ultimo_proveedor"))
                         {
                             cod_proveedor = articulo.getJSONObject("ultimo_proveedor").getString("cod_proveedor");
                             razsocial = articulo.getJSONObject("ultimo_proveedor").getString("razsocial");
                             tvProveedor.setText(razsocial);
                         }

                         txtCodigo.setText("");
                         txtCodigo.requestFocus();

                         if (progress.isShowing()) progress.dismiss();
                     } else {

                         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                         builder.setTitle("No registrado").setMessage("El artículo no está registrado \nDesea registrarlo ahora?");
                         builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 if (progress.isShowing()) progress.dismiss();
                                 nuevoArticulo(codigo);
                                 dialog.dismiss();
                             }
                         });
                         builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 if (progress.isShowing()) progress.dismiss();
                                 dialog.dismiss();
                             }
                         });
                         AlertDialog dialog = builder.create();
                         dialog.show();

                         tvProveedor.setText("");
                     }
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         }, new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError error) {

                 if (progress.isShowing()) progress.dismiss();
                 String msj = (error.getMessage() != null && !error.getMessage().isEmpty()) ? error.getMessage()
                         : new String(error.networkResponse.data,StandardCharsets.UTF_8) ;
                 informeErrores.enviar(String.valueOf(error.networkResponse.statusCode),msj);
             }
         });
        RequestQueue queue = Volley.newRequestQueue(getActivity());
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
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13,IntentIntegrator.EAN_8,IntentIntegrator.UPC_A,IntentIntegrator.UPC_E);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void aplicarCambios() {

            ContentValues params = new ContentValues();
            params.put("api_key", Configuracion.API_KEY);

            params.put("codigo", cod_articulo);
            params.put("descripcion", txtDescripcion.getText().toString());
            params.put("cod_familia", familias.get(spFamilias.getSelectedItemPosition()).getCod());
            params.put("cod_marca", marcas.get(spMarcas.getSelectedItemPosition()).getCod_marca());
            params.put("utilidad", String.valueOf(utilidad));
            params.put("cod_impuesto", impuestos.get(spImpuestos.getSelectedItemPosition()).getCodigo());
            params.put("impuesto", String.valueOf(impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto()));
            params.put("venta", String.valueOf(venta));
            params.put("activo",activo.isChecked() ? "S" : "N");
            params.put("unidad_medida", unidadMedidas.get(spUnidadMedida.getSelectedItemPosition()).getUnidad_medida());
            params.put("factor_medida", txtFactorMedida.getText().toString());
            params.put("porc_tope_descuento_1",txtDesc1.getText().toString());
            params.put("porc_tope_descuento_2",txtDesc2.getText().toString());
            params.put("porc_tope_descuento_3",txtDesc3.getText().toString());
            params.put("art_granel", (articulo_granel.isChecked() ? "S" : "N"));
            params.put("articulo_romana", (articulo_romana.isChecked() ? "S" : "N"));

            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            StringRequest request = new StringRequest(Request.Method.PUT, Configuracion.URL_APIBODEGA + "/articulo/actualizar" + params.toString(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                   informeErrores.enviar("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                }
            });

            request.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);

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

                                StringRequest requestGuardar = new StringRequest(Request.Method.POST, Configuracion.URL_APIBODEGA + "/articulo/nuevo", new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
                                    }}, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        informeErrores.enviar("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                                    }
                                }){
                                    @Override
                                    protected Map<String, String> getParams() {
                                        SimpleDateFormat df = new SimpleDateFormat("Y-m-d");
                                        Map<String, String> params = new HashMap<>();
                                        params.put("cod_articulo",codigo) ;
                                        params.put("cod_familia",familias.get(spFamilias.getSelectedItemPosition()).getCod()) ;
                                        params.put("cod_marca",marcas.get(spMarcas.getSelectedItemPosition()).getCod_marca()) ;
                                        params.put("descripcion",txtDescripcion.getText().toString()) ;
                                        params.put("precio_default",String.valueOf((venta * 100) / (100 + impuestos.get(spImpuestos.getSelectedItemPosition()).getImpuesto()))) ;
                                        params.put("creado_por",user) ;
                                        params.put("modificado_por", user) ;
                                        params.put("porcentaje_utilidad",String.valueOf(utilidad)) ;
                                        params.put("cod_impuesto",impuestos.get(spImpuestos.getSelectedItemPosition()).getCodigo()) ;
                                        params.put("articulo_romana",articulo_romana.isChecked() ? "S" : "N") ;
                                        params.put("art_granel",articulo_granel.isChecked() ? "S" : "N");
                                        params.put("unidad_medida",unidadMedidas.get(spUnidadMedida.getSelectedItemPosition()).getUnidad_medida());
                                        params.put("factor_medida",txtFactorMedida.getText().toString());
                                        params.put("api_key",Configuracion.API_KEY);
                                        return params;
                                    }
                                };

                            requestGuardar.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                RequestQueue queue = Volley.newRequestQueue(getActivity());
                                queue.add(requestGuardar);

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




}
