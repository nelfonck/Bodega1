package com.example.bodega.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.bodega.Adapters.AdapterDetalleOrden;
import com.example.bodega.Adapters.FiltroArticuloAdapter;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.Models.ModDetalleOrden;
import com.example.bodega.Models.ModFiltroArticulo;
import com.example.bodega.R;
import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Preconditions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DetalleOrden extends AppCompatActivity {
    private static String user ;
    private int id ;
    private List<ModDetalleOrden> detalle ;
    private AdapterDetalleOrden adapter ;
    private Configuracion configuracion ;
    private  JSONObject objArticulo  = null;
    private TextView tvDescripcion ;
    private TextView tvFechaUltimaCompra ;
    private TextView tvPedido , tvSalidas, tvTotalImpuesto, tvTotalOrden , tvTotalExento, tvTotalGravado;
    private String descripcion = "" ;
    private double costo=0, impuesto=0, total_impuesto=0, total=0;
    private EditText txtCodigo, txtCant ;
    private Totales totales ;
    private  Bundle extras ;
    private boolean scanned_from_scan ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_orden);
        TextView tvNumeroOrden = findViewById(R.id.tvNumeroOrden);
        TextView tvCodProveedor = findViewById(R.id.tvCodProveedor);
        TextView tvRazonSocial = findViewById(R.id.tvRazonSocial);

        tvFechaUltimaCompra = findViewById(R.id.tvFechaUltimaCompra);
        tvPedido = findViewById(R.id.tvPedido);
        tvSalidas = findViewById(R.id.tvSalidas);
        txtCodigo = findViewById(R.id.txtCodigo);
        txtCant = findViewById(R.id.txtCantidad);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvTotalImpuesto = findViewById(R.id.tvTotalImpuesto);
        tvTotalOrden = findViewById(R.id.tvTotalOrden);
        tvTotalExento = findViewById(R.id.tvTotalExento);
        tvTotalGravado = findViewById(R.id.tvTotalGravado);

        ImageButton btnBuscarDescripcion = findViewById(R.id.btnBuscarDescripcion);
        ImageButton btnScan = findViewById(R.id.btnScan);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        RecyclerView rvDetalleOrden = findViewById(R.id.rvDetalleOrden);

        totales = new Totales(0,0,0,0);

        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("user","");
            id = extras.getInt("id");
            tvNumeroOrden.setText(("Orden # " + id));
            tvCodProveedor.setText(("Cod. proveedor: " + extras.getString("cod_proveedor")));
            tvRazonSocial.setText(extras.getString("razon_social"));
            //tvRazonComercial.setText(extras.getString("razon_comercial"));
        }

        detalle = new ArrayList<>();
        adapter = new AdapterDetalleOrden(detalle);

        rvDetalleOrden.setHasFixedSize(true);
        rvDetalleOrden.setLayoutManager(new LinearLayoutManager(this));
        rvDetalleOrden.setAdapter(adapter);

        cargarDetalle();

        btnBuscarDescripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDescripcion();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanNow();
            }
        });

        txtCodigo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        assert extras != null;
                        obtArticulo(txtCodigo.getText().toString(),extras.getString("cod_proveedor"));
                        showKeyboard(DetalleOrden.this,txtCant);
                    }
                return false;
            }
        });

        txtCant.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        if (validarTextos(txtCodigo,txtCant)){
                            if (objArticulo!=null){
                                insertarLinea(txtCodigo.getText().toString(), Float.parseFloat(txtCant.getText().toString()));
                                hideKeyboard(DetalleOrden.this,txtCodigo);
                            }else{
                                Toast.makeText(DetalleOrden.this, "Debe filtrar el artículo", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                return false;
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (validarTextos(txtCodigo,txtCant)){
                    if (objArticulo!=null){
                        insertarLinea(txtCodigo.getText().toString(), Float.parseFloat(txtCant.getText().toString()));
                        hideKeyboard(DetalleOrden.this,txtCodigo);
                    }else{
                        Toast.makeText(DetalleOrden.this, "Debe filtrar el artículo", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        adapter.SetOnEliminarLinea(new AdapterDetalleOrden.OnEliminarLinea() {
            @Override
            public void eliminarLinea(final int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleOrden.this);
                builder.setTitle("Advertencia").setMessage("Seguro(a) de eliminar esta linea?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues values = new ContentValues();
                        values.put("api_key",Configuracion.API_KEY);
                        StringRequest request = new StringRequest(Request.Method.DELETE, configuracion.getUrl() +
                                "/pedido/eliminar_linea_detalle/" + id + "/" + detalle.get(pos).getCodigo() + values.toString(), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                detalle.remove(pos);
                                adapter.notifyDataSetChanged();
                                setTotales();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                try{
                                    if (error.networkResponse!=null){
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                                        }else{
                                            msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                                        }
                                    }else{
                                        msj("Error",error.getMessage());
                                    }

                                }catch (Exception e){
                                    msj("Error",e.getMessage());
                                }
                            }
                        });
                        request.setRetryPolicy(
                                new DefaultRetryPolicy(50000,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        RequestQueue queue = Volley.newRequestQueue(DetalleOrden.this);
                        queue.add(request);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Nó", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                }

        });
        adapter.SetOnCambiarCantidad(new AdapterDetalleOrden.OnCambiarCantidad() {
            @Override
            public void cambiarCantiad(final int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleOrden.this);
                View view = LayoutInflater.from(DetalleOrden.this).inflate(R.layout.dialog_editar_linea_proforma,null);
                builder.setView(view);
                final EditText txtCant = view.findViewById(R.id.txtCant);
                ImageButton btnClear = view.findViewById(R.id.btnClearCant);
                txtCant.setText(String.valueOf(detalle.get(pos).getCantidad()));
                txtCant.setSelectAllOnFocus(true);
                txtCant.requestFocus();
                showKeyboard(DetalleOrden.this,txtCant);
                btnClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        txtCant.setText("");
                        txtCant.requestFocus();
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("Aplicar", null);
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button btnCambiar = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        btnCambiar.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onClick(View v) {
                                if (txtCant.getText().toString().equals("")){
                                    Toast.makeText(DetalleOrden.this, "La cantidad no puede estar vacía", Toast.LENGTH_SHORT).show();
                                }else{
                                    ContentValues values = new ContentValues();
                                    values.put("api_key",Configuracion.API_KEY);
                                    StringRequest request = new StringRequest(Request.Method.PUT, configuracion.getUrl() +
                                            "/pedido/actualizar_cantidad/" + id + "/" + detalle.get(pos).getCodigo() + "/" + txtCant.getText().toString() + values.toString(), new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject objRes = new JSONObject(response);
                                                detalle.get(pos).setCantidad(objRes.getDouble("cantidad"));
                                                detalle.get(pos).setTotal_impuesto(objRes.getDouble("total_impuesto"));
                                                detalle.get(pos).setTotal(objRes.getDouble("total"));
                                                adapter.notifyDataSetChanged();
                                                setTotales();
                                            } catch (JSONException e) {
                                                msj("Error",e.getMessage());
                                            }

                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            try{
                                                if (error.networkResponse!=null){
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                        msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                                                    }else{
                                                        msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                                                    }                                                }else{
                                                    msj("Error",error.getMessage());
                                                }

                                            }catch (Exception e){
                                                msj("Error",e.getMessage());
                                            }
                                        }
                                    });
                                    request.setRetryPolicy(
                                            new DefaultRetryPolicy(50000,
                                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    RequestQueue queue = Volley.newRequestQueue(DetalleOrden.this);
                                    queue.add(request);
                                    dialog.dismiss();
                                }

                            }
                        });
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.enviarQpos:
                enviarQpos();
                break;
            case R.id.clearAll:
                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleOrden.this);
                builder.setTitle("Warning").setMessage("Se eliminará toda la lista \n desea continuar?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        limpiarLista();
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

                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            String scanResult = result.getContents();
            if (scanResult!=null){
                txtCodigo.setText(scanResult);
                txtCant.setText("1");
                txtCant.requestFocus();
                scanned_from_scan = true ;
                showKeyboard(DetalleOrden.this,txtCant);
            }
        } else {
            Toast toast = Toast.makeText(this, "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void enviarQpos(){
        StringRequest request = new StringRequest(Request.Method.POST, configuracion.getUrl() +
                "/pedido/enviar_qupos/" + id + "/" + user + "?api_key=" + Configuracion.API_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject objResponse = new JSONObject(response);
                    if (objResponse.getBoolean("guardado")){
                        limpiarLista();
                        eliminarPedido();
                        finish();
                    }
                } catch (JSONException e) {
                    msj("Error", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    if (error.networkResponse!=null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                        }else{
                            msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                        }

                    }else{
                        msj("Error",error.getMessage());
                    }

                }catch (Exception e){
                    msj("Error",e.getMessage());
                }
            }
        });

        request.setRetryPolicy(
                new DefaultRetryPolicy(50000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void buscarDescripcion() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filtro_descripcion, null);
        builder.setView(view);
        final EditText txtArticulo = view.findViewById(R.id.txtFiltroDescripcion);
        RecyclerView recyclerView = view.findViewById(R.id.rvResultadoFiltroDescripcion);

        final List<ModFiltroArticulo> articulos = new ArrayList<>();
        final FiltroArticuloAdapter adapter = new FiltroArticuloAdapter(articulos);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final android.app.AlertDialog dialog = builder.create();
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
                        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                                "/articulo/articulo" + values.toString(), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    articulos.clear();
                                    articulos.addAll(Arrays.asList(gson.fromJson(response, ModFiltroArticulo[].class)));
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Toast.makeText(DetalleOrden.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                            }

                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                try{
                                    if (error.networkResponse!=null){
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                                        }else{
                                            msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                                        }                                    }else{
                                        msj("Error",error.getMessage());
                                    }

                                }catch (Exception e){
                                    msj("Error",e.getMessage());
                                }
                            }
                        });

                        request.setRetryPolicy(
                                new DefaultRetryPolicy(50000,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        RequestQueue queue = Volley.newRequestQueue(DetalleOrden.this);
                        queue.add(request);


                        return true;
                    }
                return false;
            }
        });

        adapter.SetOnItemClickListener(new FiltroArticuloAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int pos) {
                txtCodigo.setText(articulos.get(pos).getCodigo());
                obtArticulo(txtCodigo.getText().toString(),extras.getString("cod_proveedor"));
                dialog.dismiss();
            }
        });
    }

    public void limpiarLista(){
        detalle.clear();
        adapter.notifyDataSetChanged();
        objArticulo = null ;
        tvFechaUltimaCompra.setText("");
        tvDescripcion.setText("");
        tvPedido.setText("");
        tvSalidas.setText("");
        txtCodigo.setText("");
        txtCant.setText("");
        txtCodigo.requestFocus();

        setTotales();

        eliminarDetalle();

    }

    public void eliminarPedido(){
        StringRequest request = new StringRequest(Request.Method.DELETE, configuracion.getUrl() +
                "/pedido/eliminar_pedido/" + id+ "?api_key=" + Configuracion.API_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    if (error.networkResponse!=null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                        }else{
                            msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                        }                    }else{
                        msj("Error",error.getMessage());
                    }

                }catch (Exception e){
                    msj("Error",e.getMessage());
                }
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public void eliminarDetalle(){
        StringRequest request = new StringRequest(Request.Method.DELETE, configuracion.getUrl() +
                "/pedido/eliminar_detalle/" + id + "?api_key=" + Configuracion.API_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    if (error.networkResponse!=null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                        }else{
                            msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                        }                    }else{
                        msj("Error",error.getMessage());
                    }

                }catch (Exception e){
                    msj("Error",e.getMessage());
                }
            }
        });
        request.setRetryPolicy(
                new DefaultRetryPolicy(50000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public void showKeyboard(Context activityContext, final EditText editText){

        final InputMethodManager imm = (InputMethodManager)
                activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (!editText.hasFocus()) {
            editText.requestFocus();
        }

        editText.post(new Runnable() {
            @Override
            public void run() {
                if (imm!=null)
                    imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    protected void hideKeyboard(Context activityContext,EditText editText) {
        InputMethodManager imm = (InputMethodManager)
                activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm!=null)
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    private void insertarLinea(final String codigo, final double cant){
        boolean existe = false ;
        int pos = -1 ;
      for (int i=0; i <= detalle.size()-1; i++)
      {
            if (detalle.get(i).getCodigo().equals(codigo)){
                existe = true;
                pos = i ;
                break;
            }
      }
      if (!existe){
          try {
              descripcion = objArticulo.getString("descripcion") ;
              costo = objArticulo.getDouble("costo");
              impuesto = objArticulo.getJSONObject("impuesto").getDouble("porcentaje");
              total_impuesto = (objArticulo.getDouble("costo") * impuesto) / 100 ;
              total = objArticulo.getDouble("costo") * cant ;
          } catch (JSONException e) {
              msj("Error",e.getMessage());
          }
          //insertar nueva linea
          StringRequest request = new StringRequest(Request.Method.POST, configuracion.getUrl() +
                  "/pedido/insertar_linea_detalle", new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                 try {

                      JSONObject objResonse = new JSONObject(response);
                      detalle.add(new ModDetalleOrden(
                                    id,objResonse.getInt("id"),
                              detalle.size()+1,
                                    codigo,
                                    descripcion,
                                    cant,
                                    costo,
                                    impuesto,
                                    total_impuesto,
                                    total
                              ));
                      adapter.notifyDataSetChanged();

                      setTotales();

                      objArticulo = null ;
                      tvFechaUltimaCompra.setText("");
                      tvDescripcion.setText("");
                      tvPedido.setText("");
                      tvSalidas.setText("");
                      txtCodigo.setText("");
                      txtCant.setText("");
                      txtCodigo.requestFocus();
                  } catch (JSONException e) {
                      msj("Error",e.getMessage());
                  }
              }
          }, new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                  try{
                      if (error.networkResponse!=null){
                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                              msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                          }else{
                              msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                          }                      }else{
                          msj("Error",error.getMessage());
                      }

                  }catch (Exception e){
                      msj("Error",e.getMessage());
                  }
              }
          }){
              @Override
              protected Map<String, String> getParams() throws AuthFailureError {
                  Map<String,String> params = new HashMap<>();
                  params.put("api_key",Configuracion.API_KEY);
                  params.put("id_pedido",String.valueOf(id));
                  params.put("linea", String.valueOf((detalle.size()+1)));
                  params.put("codigo",codigo);
                  params.put("cantidad", String.valueOf(cant));
                  params.put("descripcion", descripcion);
                  params.put("costo", String.valueOf(costo));
                  params.put("impuesto", String.valueOf(impuesto));
                  params.put("total_impuesto",String.valueOf(total_impuesto));
                  params.put("total", String.valueOf(total));

                  return params ;
              }
          };
          request.setRetryPolicy(
                  new DefaultRetryPolicy(50000,
                          DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                          DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
          RequestQueue queue = Volley.newRequestQueue(this);
          queue.add(request);
      }else{
          //cambiar cantidad linea
          ContentValues values = new ContentValues() ;
          values.put("api_key",Configuracion.API_KEY);
          StringRequest request = new StringRequest(Request.Method.PUT, configuracion.getUrl() +
                  "/pedido/actualizar_cantidad/" + id + "/" + codigo + "/"  + (cant + detalle.get(pos).getCantidad()) +values.toString(), new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                  try {
                      JSONObject objResponse = new JSONObject(response);
                      for (int i = 0; i<= detalle.size()-1;i++){
                          if (detalle.get(i).getCodigo().equals(codigo)){
                              detalle.get(i).setCantidad(objResponse.getDouble("cantidad"));
                              detalle.get(i).setTotal_impuesto(objResponse.getDouble("total_impuesto"));
                              detalle.get(i).setTotal(objResponse.getDouble("total"));
                              adapter.notifyDataSetChanged();

                              setTotales();

                              objArticulo = null ;
                              tvFechaUltimaCompra.setText("");
                              tvDescripcion.setText("");
                              tvPedido.setText("");
                              tvSalidas.setText("");
                              txtCodigo.setText("");
                              txtCant.setText("");
                              txtCodigo.requestFocus();
                          }
                      }
                  } catch (JSONException e) {
                      msj("Error",e.getMessage());
                  }
              }
          }, new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                  try{
                      if (error.networkResponse!=null){
                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                              msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                          }else{
                              msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                          }                      }else{
                          msj("Error",error.getMessage());
                      }

                  }catch (Exception e){
                      msj("Error",e.getMessage());
                  }
              }
          });
          request.setRetryPolicy(
                  new DefaultRetryPolicy(50000,
                          DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                          DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
          RequestQueue queue = Volley.newRequestQueue(this);
          queue.add(request);
      }
    }



    private void obtArticulo(String codigo, String cod_proveedor){
        ContentValues values = new ContentValues();
        values.put("api_key",Configuracion.API_KEY);

        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                "/pedido/articulo/" + codigo + "/" + cod_proveedor + values.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.length() > 0){
                        objArticulo = jsonObject ;
                        txtCodigo.setText(objArticulo.getString("cod_articulo"));
                        tvDescripcion.setText(objArticulo.getString("descripcion"));
                        tvFechaUltimaCompra.setText(("Fecha: " + objArticulo.getJSONObject("movimiento").getString("fecha_ultimo_pedido")));
                        tvPedido.setText(("Se pidieron: " + objArticulo.getJSONObject("movimiento").getString("pedido")));
                        tvSalidas.setText(("Salidas: " + objArticulo.getJSONObject("movimiento").getString("vendido")));
                        txtCant.requestFocus();
                    }else{
                        tvDescripcion.setText("");
                        tvFechaUltimaCompra.setText("");
                        tvPedido.setText("");
                        tvSalidas.setText("");
                        txtCodigo.setText("");
                        txtCodigo.requestFocus();
                        objArticulo = null ;
                        Toast.makeText(DetalleOrden.this, "Artículo no encontrado", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    msj("Error", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    if (error.networkResponse!=null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                        }else{
                            msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                        }                    }else{
                        msj("Error",error.getMessage());
                    }

                }catch (Exception e){
                    msj("Error",e.getMessage());
                }
            }
        });
        request.setRetryPolicy(
                new DefaultRetryPolicy(50000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void cargarDetalle(){
        ContentValues values = new ContentValues();
        values.put("api_key", Configuracion.API_KEY);
        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                "/pedido/detalle/" + id +  values.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    Gson gson = new Gson();
                    detalle.clear();
                    detalle.addAll(Arrays.asList(gson.fromJson(response,ModDetalleOrden[].class)));
                    adapter.notifyDataSetChanged();

                    setTotales();
                }catch (Exception e){
                    msj("Error",e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    if (error.networkResponse!=null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                        }else{
                            msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                        }                    }else{
                        msj("Error",error.getMessage());
                    }

                }catch (Exception e){
                    msj("Error",e.getMessage());
                }
            }
        });

        request.setRetryPolicy(
                new DefaultRetryPolicy(50000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private boolean validarTextos (EditText txtCodigo, EditText txtCant){
        if (txtCant.getText().toString().equals("") || txtCodigo.getText().toString().equals("")){
            Toast.makeText(this, "hay campos vacíos", Toast.LENGTH_SHORT).show();
            return false ;
        }else if (user.equals("")){
            Toast.makeText(this, "Debes estar logeado para continuar", Toast.LENGTH_SHORT).show();
        }
        return true ;
    }

    private void setTotales(){
        double total_exento =0, total_gravado =0, total_impuesto = 0, total = 0 ;
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        for (int x=0; x<=detalle.size()-1;x++)
        {
            if (detalle.get(x).getPorc_impuesto()>0){
                total_gravado+= detalle.get(x).getCosto();
                total_impuesto+= detalle.get(x).getTotal_impuesto() ;
            }else{
                total_exento+= detalle.get(x).getCosto();
            }

            total+= detalle.get(x).getTotal();
        }
        totales.setTotal_exento(total_exento);
        totales.setTotal_gravado(total_gravado);
        totales.setTotal_impuesto(total_impuesto);
        totales.setTotal(total + total_impuesto);
        tvTotalExento.setText(("Total exento: ₡" + formatter.format(totales.getTotal_exento())));
        tvTotalGravado.setText(("Total gravado: ₡" + formatter.format(totales.getTotal_gravado())));
        tvTotalImpuesto.setText(("Impuesto: ₡" + formatter.format(totales.getTotal_impuesto())));
        tvTotalOrden.setText(("Total iva ₡" + formatter.format(totales.getTotal())));
        setTotalesDb(totales.getTotal_exento(), totales.getTotal_gravado(),totales.getTotal_impuesto(),totales.getTotal());
    }

    private void setTotalesDb(double total_exento, double total_gravado, double impuesto, double total){
        ContentValues values = new ContentValues();
        values.put("api_key",Configuracion.API_KEY);
        StringRequest request = new StringRequest(Request.Method.PUT, configuracion.getUrl() +
                "/pedido/settotal/" + id + "/" + total_exento + "/" + total_gravado + "/" + impuesto + "/" + total   + values.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //msj("Response", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    if (error.networkResponse!=null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                        }else{
                            msj("Error",new String(error.networkResponse.data, Charset.forName("UTF-8")));
                        }                    }else{
                        msj("Error",error.getMessage());
                    }

                }catch (Exception e){
                    msj("Error",e.getMessage());
                }
            }
        });

        request.setRetryPolicy(
                new DefaultRetryPolicy(50000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public void scanNow() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13,IntentIntegrator.EAN_8,IntentIntegrator.UPC_A,IntentIntegrator.UPC_E);
        intentIntegrator.setPrompt("Scan barcode");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
    }


    @SuppressWarnings("SameParameterValue")
    private void msj(String title, String msj) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msj);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    class Totales{
        double total_exento ;
        double total_gravado ;
        double total_impuesto ;
        double total ;

        public Totales(double total_exento, double total_gravado , double total_impuesto, double total) {
            this.total_exento = total_exento ;
            this.total_gravado = total_gravado ;
            this.total_impuesto = total_impuesto;
            this.total = total;
        }

        public double getTotal_exento() {
            return total_exento;
        }

        public void setTotal_exento(double total_exento) {
            this.total_exento = total_exento;
        }

        public double getTotal_gravado() {
            return total_gravado;
        }

        public void setTotal_gravado(double total_gravado) {
            this.total_gravado = total_gravado;
        }

        public double getTotal_impuesto() {
            return total_impuesto;
        }

        public void setTotal_impuesto(double total_impuesto) {
            this.total_impuesto = total_impuesto;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }
    }
}