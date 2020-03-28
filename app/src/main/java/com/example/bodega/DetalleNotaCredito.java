package com.example.bodega;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adaptadores.AdapterDetalleNota;
import com.example.bodega.Adaptadores.AdapterDetalleProforma;
import com.example.bodega.Adaptadores.BaseAdapter;
import com.example.bodega.Modelos.Configuracion;
import com.example.bodega.Modelos.ContentValues;
import com.example.bodega.Modelos.ModDetalleNota;
import com.example.bodega.Modelos.ModDetalleProforma;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DetalleNotaCredito extends AppCompatActivity {
    private int id_nota  ;
    private String cod_proveedor, razsocial, razon_comercial ;
    private List<ModDetalleNota> detalle ;
    private AdapterDetalleNota adapter ;
    private TextView tvTotal ;
    private double total ;
    private boolean scanned_from_scan ;
    private Configuracion configuracion;
    private BaseAdapter dbHelper ;
    private DecimalFormat formatter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_nota_credito);

        total = 0 ;
        getConfiguracion();
        dbHelper = new BaseAdapter(this);

        TextView tvNumeroNota = findViewById(R.id.tvNumeroNota);
        TextView tvCodProveedor = findViewById(R.id.tvCodProveedor);
        TextView tvRazsocial = findViewById(R.id.tvRazsocial);
        TextView tvRazonComercial = findViewById(R.id.tvRazonComercial);
        tvTotal = findViewById(R.id.tvTotal);
        RecyclerView rvDetalleNota = findViewById(R.id.rvDetalleNota);
        ImageButton btnBuscarDescripcion = findViewById(R.id.btnBuscarDescripcion);
        ImageButton btnScan = findViewById(R.id.btnScan);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        final EditText txtCodigo = findViewById(R.id.txtCodigo);
        final EditText txtCantidad = findViewById(R.id.txtCantidad);

        Bundle extras = getIntent().getExtras();

        if (extras != null){
            id_nota = extras.getInt("id_nota");
            cod_proveedor = extras.getString("cod_proveedor");
            razsocial = extras.getString("razsocial");
            razon_comercial = extras.getString("razon_comercial");

            tvNumeroNota.setText(("Nota:" + id_nota));
            tvCodProveedor.setText(("Proveedor:" + cod_proveedor));
            tvRazsocial.setText(razsocial);
            tvRazonComercial.setText(razon_comercial);
        }

        detalle = new ArrayList<>();
        adapter = new AdapterDetalleNota(detalle);

        rvDetalleNota.setHasFixedSize(true);
        rvDetalleNota.setLayoutManager(new LinearLayoutManager(this));
        rvDetalleNota.setAdapter(adapter);

        formatter = new DecimalFormat("#,###,###");
        tvTotal.setText(("Total ₡" +formatter.format(getTotal())));

        txtCodigo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        txtCantidad.requestFocus();
                        scanned_from_scan = false ;
                        showKeyboard(DetalleNotaCredito.this,txtCantidad);
                        return true;
                    }
                return false;
            }
        });

        txtCantidad.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (validar(txtCodigo.getText().toString(), txtCantidad.getText().toString())) {
                            findArticulo(txtCodigo.getText().toString(), Integer.valueOf(txtCantidad.getText().toString()));
                            txtCodigo.setText("");
                            txtCantidad.setText("");
                            txtCodigo.requestFocus();
                            if (scanned_from_scan){
                                hideKeyboard(DetalleNotaCredito.this,txtCodigo);
                            }else{
                                showKeyboard(DetalleNotaCredito.this,txtCodigo);
                            }
                        } else {
                            Toast.makeText(DetalleNotaCredito.this, "El código y la cantidad no pueden estar vaciós", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return false;
            }
        });

    }

    private boolean validar(String codigo, String cant) {
        if (codigo.equals("") || cant.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void findArticulo(final String codigo, final double cant) {

        try {
            ContentValues cv = new ContentValues();
            cv.put("codigo",codigo);
            cv.put("host_db",configuracion.getHost_db());
            cv.put("port_db",configuracion.getPort_db());
            cv.put("user_name",configuracion.getUser_name());
            cv.put("password",configuracion.getPassword());
            cv.put("db_name",configuracion.getDatabase());
            cv.put("schema",configuracion.getSchema());

             StringRequest  request = new StringRequest(Request.Method.GET, configuracion.getUrl() + "/articulos/" + cv.toString(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject articulo = new JSONObject(response);
                        if (articulo.length() > 0) {
                            if (articulo.getString("activo").equals("S")) {
                                if (!enLaLista(detalle , articulo.getString("codigo"), cant)) {
                                    String cod_articulo = articulo.getString("codigo");
                                    String descripcion = articulo.getString("descripcion");
                                    double costo = articulo.getDouble("venta");
                                    int iv = articulo.getInt("porc_impuesto");
                                    double monto_impuesto = (((costo * cant) * (100 + iv)) / 100) - 100;
                                    double totalIVI = ((costo * cant) *(100 + iv)) / 100 ;
                                    insertarLinea(cod_articulo, descripcion, venta, iv, cant, total);
                                    detalle.add(new ModDetalleNota(cod_articulo, descripcion,cant, costo, iv, monto_impuesto, totalIVI));
                                    tvTotal.setText(("Total IVI ₡" + formatter.format(total)));
                                    adapter.notifyDataSetChanged();
                                }

                            } else {
                                Toast.makeText(DetalleNotaCredito.this, "El artículo se  encuentra inactivo. : " + articulo.getString("codigo"), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DetalleNotaCredito.this, "El articulo no existe", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        msj("Error", e.getMessage());
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    msj("Error", error.getMessage());
                }
            });

            RequestQueue queue = Volley.newRequestQueue(DetalleNotaCredito.this);
            queue.add(request) ;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    //Si el código se encuentra en la lista entonces cambiamos ajustamos la cantidad y el total de dicha linea
    private boolean enLaLista(final List<ModDetalleNota> lista, final String codigo, double cant) {

        for (int i = 0; i <= lista.size() -1; i++){
            if (lista.get(i).getCodigo().equals(codigo)){
                final double newCant = lista.get(i).getCantidad() + cant ;
                final double newTotal = lista.get(i).getCosto() * newCant ;

                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleNotaCredito.this);
                builder.setTitle("Advertencia!")
                        .setMessage("Hay " + lista.get(i).getCantidad() + " en la lista \n " +
                                "Desea adjuntar " + cant + " "+ lista.get(i).getDescripcion() + " a la cantidad actual?");
                final int finalI = i;
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lista.get(finalI).setCantidad(newCant);
                        lista.get(finalI).setTotal(newTotal);

                        editarLineaDB(consecutivo, codigo, newCant, newTotal);

                        sumarTotales(lista);

                        adaptador.notifyDataSetChanged();
                        editarEncabezadoDB();

                        dialog.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true ;
            }
        }
        return false ;
    }

    private void editarLineaDB(String consecutivo, String codigo, double cant, double total) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.CANTIDAD, cant);
        cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.TOTAL, total);

        double row = db.update(BaseAdapter.DETALLE_NOTAS_CREDITO.TABLE_NAME, cv,
                BaseAdapter.DETALLE_NOTAS_CREDITO.REF + "=? and " +
                        BaseAdapter.DETALLE_NOTAS_CREDITO.CODIGO + "=?", new String[]{consecutivo, codigo});

        if (row > 0) {
            // Toast.makeText(DetalleProforma.this,"Row updated",Toast.LENGTH_SHORT).show();
        } else {
            msj("Error", "row can`t be updated");
        }
        db.close();
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
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    protected void hideKeyboard(Context activityContext,EditText editText) {
        InputMethodManager imm = (InputMethodManager)
                activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    private double getTotal(){
        total = 0 ;
        for(int i = 0; i<= detalle.size()-1;i++)
        {
            total+= detalle.get(i).getTotal_ivi();
        }
        return total ;
    }

    private void getConfiguracion() {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);

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
}
