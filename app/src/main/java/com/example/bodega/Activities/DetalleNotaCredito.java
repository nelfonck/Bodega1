package com.example.bodega.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adapters.AdapterDetalleNota;
import com.example.bodega.Adapters.BaseAdapter;
import com.example.bodega.Adapters.FiltroArticuloAdapter;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.Models.ModDetalleNota;
import com.example.bodega.Models.ModFiltroArticulo;
import com.example.bodega.R;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetalleNotaCredito extends AppCompatActivity {
    private int id_nota  ;
    String cod_proveedor, razsocial, razon_comercial ;
    private List<ModDetalleNota> detalle ;
    private AdapterDetalleNota adapter ;
    private TextView tvTotal ;
    private double total ;
    private boolean scanned_from_scan ;
    private Configuracion configuracion;
    private BaseAdapter dbHelper ;
    private DecimalFormat formatter ;
    private EditText txtCodigo ;
    private EditText txtCantidad ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_nota_credito);

        total = 0 ;
        getConfiguracion();
        dbHelper = new BaseAdapter(this);
        formatter = new DecimalFormat("#,###,###");

        TextView tvNumeroNota = findViewById(R.id.tvNumeroNota);
        TextView tvCodProveedor = findViewById(R.id.tvCodProveedor);
        TextView tvRazsocial = findViewById(R.id.tvRazsocial);
        TextView tvRazonComercial = findViewById(R.id.tvRazonComercial);
        tvTotal = findViewById(R.id.tvTotal);
        RecyclerView rvDetalleNota = findViewById(R.id.rvDetalleNota);
        ImageButton btnBuscarDescripcion = findViewById(R.id.btnBuscarDescripcion);
        ImageButton btnScan = findViewById(R.id.btnScan);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        txtCodigo = findViewById(R.id.txtCodigo);
        txtCantidad = findViewById(R.id.txtCantidad);

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
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (validar(txtCodigo.getText().toString(), txtCantidad.getText().toString())) {
                            findArticulo(txtCodigo.getText().toString(), Float.valueOf(txtCantidad.getText().toString()));
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

        btnBuscarDescripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDescripcion();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (validar(txtCodigo.getText().toString(), txtCantidad.getText().toString())) {
                    findArticulo(txtCodigo.getText().toString(), Float.valueOf(txtCantidad.getText().toString()));
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
        });

        adapter.setOnLongItemClickListener(new AdapterDetalleNota.OnLongClickListener() {
            @Override
            public void onLongClick(final int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleNotaCredito.this);
                builder.setTitle("Eliminar esta linea").setMessage("Está seguro(a) de eliminar esta linea?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarLineaDB(detalle.get(pos).getCodigo());
                        detalle.remove(pos);
                        adapter.notifyDataSetChanged();
                        tvTotal.setText(("Total ₡" +formatter.format(getTotal())));
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
        });

        adapter.setOnItemClickListener(new AdapterDetalleNota.OnClickListener() {
            @Override
            public void onClick(int pos) {
                cambiarCantidad(pos);
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanNow();
            }
        });

        cargarDetalles();

        tvTotal.setText(("Total ₡" +formatter.format(getTotal())));

    }

    private void cargarDetalles() {

        try{
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.rawQuery("select * from " + BaseAdapter.DETALLE_NOTAS_CREDITO.TABLE_NAME + " WHERE " + BaseAdapter.DETALLE_NOTAS_CREDITO.REF + "=?", new String[]{String.valueOf(id_nota)});

            detalle.clear();
            while (c.moveToNext()) {
                detalle.add(new ModDetalleNota(
                        c.getString(c.getColumnIndex(BaseAdapter.DETALLE_NOTAS_CREDITO.CODIGO)),
                        c.getString(c.getColumnIndex(BaseAdapter.DETALLE_NOTAS_CREDITO.ARTICULO)),
                        c.getDouble(c.getColumnIndex(BaseAdapter.DETALLE_NOTAS_CREDITO.CANTIDAD)),
                        c.getDouble(c.getColumnIndex(BaseAdapter.DETALLE_NOTAS_CREDITO.COSTO)),
                        c.getDouble(c.getColumnIndex(BaseAdapter.DETALLE_NOTAS_CREDITO.IMPUETO)),
                        c.getDouble(c.getColumnIndex(BaseAdapter.DETALLE_NOTAS_CREDITO.MONTO_IMPUESTO)),
                        c.getDouble(c.getColumnIndex(BaseAdapter.DETALLE_NOTAS_CREDITO.TOTAL))));

            }

            c.close();
            db.close();
            adapter.notifyDataSetChanged();
        }catch (SQLiteException e){
            msj("Error",e.getMessage());
        }


    }

    private boolean validar(String codigo, String cant) {
        return !codigo.equals("") && !cant.equals("");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void findArticulo(final String codigo, final double cant) {

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
                                    double monto_impuesto = (costo  *  iv) / 100 ;
                                    double totalIVI = (costo * cant) + (monto_impuesto * cant) ;
                                    insertarLinea(cant,codigo,descripcion,costo,iv,monto_impuesto,totalIVI);
                                    detalle.add(new ModDetalleNota(cod_articulo, descripcion,cant, costo, iv, monto_impuesto, totalIVI));
                                    tvTotal.setText(("Total IVI ₡" + formatter.format(getTotal())));
                                    editarEncabezadoDB(total);
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
    }

    private void eliminarLineaDB(String codigo) {
        try{
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            double countRowAffected = db.delete(BaseAdapter.DETALLE_NOTAS_CREDITO.TABLE_NAME, BaseAdapter.DETALLE_NOTAS_CREDITO.CODIGO +
                    "=? and " + BaseAdapter.DETALLE_NOTAS_CREDITO.REF + "=?", new String[]{codigo, String.valueOf(id_nota)});

            if (countRowAffected == 0) {
                msj("Error", "No se ha podido eliminar la linea");
            }
            db.close();

        }catch (SQLiteException e){
            msj("Error",e.getMessage());
        }

    }

    private void cambiarCantidad(final int pos) {

        AlertDialog.Builder builder = new AlertDialog.Builder(DetalleNotaCredito.this);
        View view = View.inflate(DetalleNotaCredito.this,R.layout.dialog_editar_linea_proforma, null);
        builder.setView(view);
        final EditText txtCant = view.findViewById(R.id.txtCant);
        final ImageButton btnClearCant = view.findViewById(R.id.btnClearCant);

        txtCant.setText(String.valueOf(detalle.get(pos).getCantidad()));
        txtCant.setSelectAllOnFocus(true);
        txtCant.requestFocus();
        showKeyboard(DetalleNotaCredito.this,txtCant);
        builder.setPositiveButton("Aceptar", null);
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        btnClearCant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtCant.setText("");
                txtCant.requestFocus();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnCambiar = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                btnCambiar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!txtCant.getText().toString().equals("")) {
                            double cant = Float.valueOf(txtCant.getText().toString());
                            double costo = detalle.get(pos).getCosto() ;
                            double monto_impuesto = detalle.get(pos).getMonto_impuesto();
                            double totalIVI = (costo * cant) + (monto_impuesto * cant) ;

                            detalle.get(pos).setCantidad(cant);
                            detalle.get(pos).setTotal_ivi(totalIVI);

                            editarLineaDB(detalle.get(pos).getCodigo(), cant, totalIVI);

                            tvTotal.setText(("Total ₡" +formatter.format(getTotal())));

                            editarEncabezadoDB(total);
                            adapter.notifyDataSetChanged();

                            dialog.dismiss();
                        } else {
                            Toast.makeText(DetalleNotaCredito.this, "No ha digitado una cantidad", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();

    }

    private void buscarDescripcion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(DetalleNotaCredito.this,R.layout.dialog_filtro_descripcion,null);
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
        final AlertDialog dialog = builder.create();
        dialog.show();

        txtArticulo.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        final Gson gson = new Gson();
                    ContentValues values  = new ContentValues();
                    values.put("descripcion",txtArticulo.getText().toString());
                    values.put("host_db",configuracion.getHost_db());
                    values.put("port_db",configuracion.getPort_db());
                    values.put("user_name",configuracion.getUser_name());
                    values.put("password",configuracion.getPassword());
                    values.put("db_name", configuracion.getDatabase());
                    values.put("schema",configuracion.getSchema());

                        StringRequest  request = new StringRequest(Request.Method.GET, configuracion.getUrl() + "/articulos/" + values.toString(), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    articulos.clear();
                                    articulos.addAll(Arrays.asList(gson.fromJson(response, ModFiltroArticulo[].class)));
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Toast.makeText(DetalleNotaCredito.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                            }

                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(DetalleNotaCredito.this, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                        RequestQueue queue = Volley.newRequestQueue(DetalleNotaCredito.this);
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
                //txtCantidad.setText("1");
                txtCantidad.requestFocus();
                showKeyboard(DetalleNotaCredito.this,txtCantidad);
                dialog.dismiss();
            }
        });
    }

    private void insertarLinea(double cant,String codigo,String descripcion,double costo, double impuesto, double monto_impuesto, double total){
        try{

            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.REF, id_nota);
            cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.CANTIDAD,cant);
            cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.CODIGO,codigo);
            cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.ARTICULO,descripcion);
            cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.COSTO,costo);
            cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.IMPUETO,impuesto);
            cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.MONTO_IMPUESTO,monto_impuesto);
            cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.TOTAL,total);

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            long insert = db.insert(BaseAdapter.DETALLE_NOTAS_CREDITO.TABLE_NAME,null,cv);

            if (insert == -1) Toast.makeText(DetalleNotaCredito.this, "Row can`t be inserted", Toast.LENGTH_SHORT).show();

        }catch (SQLiteException e){
            msj("Error", e.getMessage());
        }
    }

    private boolean enLaLista(final List<ModDetalleNota> lista, final String codigo, double cant) {

        for (int i = 0; i <= lista.size() -1; i++){
            if (lista.get(i).getCodigo().equals(codigo)){
                final double newCant = lista.get(i).getCantidad() + cant ;
                final double newTotal = (lista.get(i).getCosto() * newCant) + (lista.get(i).getMonto_impuesto() * newCant);

                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleNotaCredito.this);
                builder.setTitle("Advertencia!")
                        .setMessage("Hay " + lista.get(i).getCantidad() + " en la lista \n " +
                                "Desea adjuntar " + cant + " "+ lista.get(i).getDescripcion() + " a la cantidad actual?");
                final int finalI = i;
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lista.get(finalI).setCantidad(newCant);
                        lista.get(finalI).setTotal_ivi(newTotal);

                        editarLineaDB(codigo, newCant, newTotal);

                        tvTotal.setText(formatter.format(getTotal()));

                        adapter.notifyDataSetChanged();

                        editarEncabezadoDB(total);

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

    private void editarEncabezadoDB(double total) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            //String date = new SimpleDateFormat("Y-m-d", Locale.getDefault()).format(new Date());
            android.content.ContentValues cv = new android.content.ContentValues();

            cv.put(BaseAdapter.NOTAS_CREDITO.TOTAL,total);

            db.update(BaseAdapter.NOTAS_CREDITO.TABLE_NAME, cv, BaseAdapter.DETALLE_NOTAS_CREDITO.ID + "=?", new String[]{String.valueOf(id_nota)});

            db.close();

        } catch (SQLiteException e) {
            msj("Error", e.getMessage());
        }
    }

    private void editarLineaDB(String codigo , double cant, double total) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.CANTIDAD, cant);
        cv.put(BaseAdapter.DETALLE_NOTAS_CREDITO.TOTAL, total);

        double row = db.update(BaseAdapter.DETALLE_NOTAS_CREDITO.TABLE_NAME, cv,
                BaseAdapter.DETALLE_NOTAS_CREDITO.REF + "=? and " +
                        BaseAdapter.DETALLE_NOTAS_CREDITO.CODIGO + "=?", new String[]{String.valueOf(id_nota), codigo});

        if (row == 0) {
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

    public void scanNow() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13,IntentIntegrator.EAN_8,IntentIntegrator.UPC_A,IntentIntegrator.UPC_E);
        intentIntegrator.setPrompt("Scan barcode");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            String scanResult = result.getContents();
            txtCodigo.setText(scanResult);
            txtCantidad.setText("1");
            txtCantidad.requestFocus();
            scanned_from_scan = true ;
            showKeyboard(DetalleNotaCredito.this,txtCantidad);
        } else {
            Toast toast = Toast.makeText(this, "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private void limpiarLista() {
        eliminarDetallesDB();
        detalle.clear();
        adapter.notifyDataSetChanged();
        tvTotal.setText(("Total ₡" +formatter.format(getTotal())));
    }

    private void eliminarDetallesDB() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            double rowAff = db.delete(BaseAdapter.DETALLE_NOTAS_CREDITO.TABLE_NAME, BaseAdapter.DETALLE_NOTAS_CREDITO.REF + "=?", new String[]{String.valueOf(id_nota)});
            if (rowAff == 0) {
                Toast.makeText(DetalleNotaCredito.this, "No se limpió la lista en la base de datos", Toast.LENGTH_LONG).show();
            }
        } catch (SQLiteException e) {
            msj("Error", e.getMessage());
        }
    }

    private void enviarQpos(){
        try {

        }catch (Exception e){
            msj("Errro", e.getMessage());
        }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleNotaCredito.this);
                builder.setTitle("Warning").setMessage("Se eliminará toda la lista \n desea continuar?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
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
}
