package com.example.bodega.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.Models.ModDetalleOrden;
import com.example.bodega.R;
import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleOrden extends AppCompatActivity {
    private static String user ;
    private int id ;
    private List<ModDetalleOrden> detalle ;
    private AdapterDetalleOrden adapter ;
    private Configuracion configuracion ;
    private  JSONObject objArticulo  = null;
    private TextView tvDescripcion ;
    private TextView tvFechaUltimaCompra ;
    private TextView tvPedido , tvSalidas, tvTotalImpuesto, tvTotalOrden , tvSubTotal;
    private String descripcion = "" ;
    private double costo=0, impuesto=0, total_impuesto=0, total=0;
    private EditText txtCodigo, txtCant ;
    private Totales totales ;

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
        tvSubTotal = findViewById(R.id.tvSubTotal);

        ImageButton btnBuscarDescripcion = findViewById(R.id.btnBuscarDescripcion);
        ImageButton btnScan = findViewById(R.id.btnScan);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        RecyclerView rvDetalleOrden = findViewById(R.id.rvDetalleOrden);

        totales = new Totales(0,0);

        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        final Bundle extras = getIntent().getExtras();
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

        txtCodigo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        assert extras != null;
                        obtArticulo(txtCodigo.getText().toString(),extras.getString("cod_proveedor"));

                    }
                return false;
            }
        });

        txtCant.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        if (validarTextos(txtCodigo,txtCant)){
                            if (objArticulo!=null){
                                insertarLinea(txtCodigo.getText().toString(), Float.parseFloat(txtCant.getText().toString()));
                            }else{
                                Toast.makeText(DetalleOrden.this, "Debe filtrar el artículo", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                return false;
            }
        });

        adapter.SetOnEliminarLinea(new AdapterDetalleOrden.OnEliminarLinea() {
            @Override
            public void eliminarLinea(final int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleOrden.this);
                builder.setTitle("Advertencia").setMessage("Seguro(a) de eliminar esta linea?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
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
                                        msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
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
                                                    msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
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

                            }
                        });
                    }
                });
                dialog.show();
            }
        });
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
                          msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
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
                        msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
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

    private void cargarDetalle(){
        ContentValues values = new ContentValues();
        values.put("api_key", Configuracion.API_KEY);
        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                "/pedido/detalle/" + id + "/" + values.toString(), new Response.Listener<String>() {
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
                        msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
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
        double total_impuesto = 0, total = 0 ;
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        for (int x=0; x<=detalle.size()-1;x++)
        {
            total_impuesto+= detalle.get(x).getTotal_impuesto() ;
            total+= detalle.get(x).getTotal();
        }

        totales.setTotal_impuesto(total_impuesto);
        totales.setTotal(total);
        tvSubTotal.setText(("Sub tot: ₡" + formatter.format(totales.getTotal())));
        tvTotalImpuesto.setText(("Total imp: ₡" + formatter.format(totales.getTotal_impuesto())));
        tvTotalOrden.setText(("Total: ₡" + formatter.format(totales.getTotal() + totales.getTotal_impuesto())));

        setTotalesDb(totales.getTotal(),totales.getTotal_impuesto(),(totales.getTotal() + totales.getTotal_impuesto()));
    }

    private void setTotalesDb(double subtotal, double impuesto, double total){
        ContentValues values = new ContentValues();
        values.put("api_key",Configuracion.API_KEY);
        StringRequest request = new StringRequest(Request.Method.PUT, configuracion.getUrl() +
                "/pedido/settotal/" + id + "/" + subtotal + "/" + impuesto + "/" + total   + values.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //msj("Response", response);
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

        request.setRetryPolicy(
                new DefaultRetryPolicy(50000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
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
        double total_impuesto ;
        double total ;

        public Totales(double total_impuesto, double total) {
            this.total_impuesto = total_impuesto;
            this.total = total;
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