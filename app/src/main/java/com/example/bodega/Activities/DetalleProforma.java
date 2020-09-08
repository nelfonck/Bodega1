package com.example.bodega.Activities;

import android.content.ContentValues;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.bodega.Adapters.AdapterDetalleProforma;
import com.example.bodega.Adapters.BaseAdapter;
import com.example.bodega.Adapters.FiltroArticuloAdapter;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ModDetalleProforma;
import com.example.bodega.Models.ModFiltroArticulo;
import com.example.bodega.R;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleProforma extends AppCompatActivity {
    private static String consecutivo, cod_cliente, cliente, user;

    private BaseAdapter baseAdapter;
    private TextView tvSubTotalExento;
    private TextView tvSubTotalGravado;
    private TextView tvMontoIv;
    private TextView tvTotal;
    private EditText txtCodigo;
    private EditText txtCantidad;
    private double subTotalExento = 0;
    private double subTotalGravado = 0;
    private double montoImpuesto = 0;
    private double total = 0;
    private List<ModDetalleProforma> detalles;
    private AdapterDetalleProforma adapterDetalleProforma;
    private boolean scanned_from_scan ;
    private Configuracion configuracion ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_proforma);

        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        baseAdapter = new BaseAdapter(this);

        if (getSupportActionBar()!=null)
        getSupportActionBar().setTitle("DETALLES PROFORMA");

        BaseAdapter base = new BaseAdapter(this);

        Bundle extras = getIntent().getExtras();
        RecyclerView rvDetalleProforma = findViewById(R.id.rvDetalleProforma);
        TextView tvCliente = findViewById(R.id.tvCliente);
        txtCodigo = findViewById(R.id.txtCodigo);
        txtCantidad = findViewById(R.id.txtCantidad);
        tvSubTotalExento = findViewById(R.id.tvTotalExento);
        tvSubTotalGravado = findViewById(R.id.tvTotalGravado);
        tvMontoIv = findViewById(R.id.tvMontoIV);
        tvTotal = findViewById(R.id.tvTotal);
        ImageButton btnScan = findViewById(R.id.btnScan);
        ImageButton btnDescripcion = findViewById(R.id.btnBuscarDescripcion);
        ImageButton btnAdd = findViewById(R.id.btnAdd);

        txtCantidad.setSelectAllOnFocus(true);

        assert extras != null;
        cliente = extras.getString("cliente");
        cod_cliente = extras.getString("cod_cliente");
        consecutivo = extras.getString("consecutivo");
        user = extras.getString("user");

        tvCliente.setText(cliente);

        detalles = new ArrayList<>();

        adapterDetalleProforma = new AdapterDetalleProforma(detalles);
        rvDetalleProforma.setHasFixedSize(true);
        rvDetalleProforma.setLayoutManager(new LinearLayoutManager(this));
        rvDetalleProforma.setAdapter(adapterDetalleProforma);

        cargarDetalles(base, detalles, adapterDetalleProforma);

        sumarTotales(detalles);

        adapterDetalleProforma.SetOnItemClick(new AdapterDetalleProforma.OnItemClick() {
            @Override
            public void ItemClick(int pos) {
                cambiarCantidad(pos, detalles, adapterDetalleProforma);
            }
        });

        adapterDetalleProforma.SetOnLongItemClick(new AdapterDetalleProforma.OnLongItemClick() {
            @Override
            public void ItemLongClick(final int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleProforma.this);
                builder.setTitle("Eliminar esta linea").setMessage("Está seguro(a) de eliminar esta linea?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarLineaDB(consecutivo, detalles.get(pos).getCod_articulo());
                        detalles.remove(pos);
                        adapterDetalleProforma.notifyDataSetChanged();
                        sumarTotales(detalles);
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

        txtCodigo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {

                        txtCantidad.requestFocus();
                        scanned_from_scan = false ;
                        showKeyboard(DetalleProforma.this,txtCantidad);
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
                            findArticulo(txtCodigo.getText().toString(), Double.parseDouble(txtCantidad.getText().toString()), detalles, adapterDetalleProforma);
                            txtCodigo.setText("");
                            txtCantidad.setText("");
                            txtCodigo.requestFocus();
                            if (scanned_from_scan){
                                hideKeyboard(DetalleProforma.this,txtCodigo);
                            }else{
                                showKeyboard(DetalleProforma.this,txtCodigo);
                            }
                        } else {
                            Toast.makeText(DetalleProforma.this, "El código y la cantidad no pueden estar vaciós", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return false;
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanNow();
            }
        });

        btnDescripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDescripcion();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (validar(txtCodigo.getText().toString(),txtCantidad.getText().toString())){
                    findArticulo(txtCodigo.getText().toString(), Double.parseDouble(txtCantidad.getText().toString()), detalles, adapterDetalleProforma);
                    txtCodigo.setText("");
                    txtCantidad.setText("");
                    txtCodigo.requestFocus();
                    if (scanned_from_scan){
                        hideKeyboard(DetalleProforma.this,txtCodigo);
                    }else{
                        showKeyboard(DetalleProforma.this,txtCodigo);
                    }
                }

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

    protected void hideKeyboard(Context activityContext,EditText editText) {
        InputMethodManager imm = (InputMethodManager)
                activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm!=null)
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void cambiarCantidad(final int pos, final List<ModDetalleProforma> lista, final AdapterDetalleProforma adaptador) {

        AlertDialog.Builder builder = new AlertDialog.Builder(DetalleProforma.this);
        View view = View.inflate(DetalleProforma.this,R.layout.dialog_editar_linea_proforma, null);
        builder.setView(view);
        final EditText txtCant = view.findViewById(R.id.txtCant);
        final ImageButton btnClearCant = view.findViewById(R.id.btnClearCant);

        txtCant.setText(String.valueOf(lista.get(pos).getCantidad()));
        txtCant.setSelectAllOnFocus(true);
        txtCant.requestFocus();
        showKeyboard(DetalleProforma.this,txtCant);
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
                            double newCant = Float.parseFloat(txtCant.getText().toString());
                            double newTotal = newCant * lista.get(pos).getPrecio();
                            lista.get(pos).setCantidad(newCant);
                            lista.get(pos).setTotal(newTotal);

                            editarLineaDB(consecutivo, lista.get(pos).getCod_articulo(), newCant, newTotal);

                            sumarTotales(lista);

                            adaptador.notifyDataSetChanged();
                            editarEncabezadoDB();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(DetalleProforma.this, "No ha digitado una cantidad", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();

    }

    private void eliminarLineaDB(String consecutivo, String codigo) {
        SQLiteDatabase db = baseAdapter.getWritableDatabase();
        double countRowAffected = db.delete(BaseAdapter.DETALLE_PROFORMA.TABLE_NAME, BaseAdapter.DETALLE_PROFORMA.COD_ARTICULO +
                "=? and " + BaseAdapter.DETALLE_PROFORMA.REF_PROFORMA + "=?", new String[]{codigo, consecutivo});

        if (countRowAffected == 0) {
            msj("Error", "No se ha podido eliminar la linea");
        }

        db.close();

    }

    private void editarLineaDB(String consecutivo, String codigo, double cant, double total) {
        SQLiteDatabase db = baseAdapter.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(BaseAdapter.DETALLE_PROFORMA.CANTIDAD, cant);
        cv.put(BaseAdapter.DETALLE_PROFORMA.TOTAL, total);

        double row = db.update(BaseAdapter.DETALLE_PROFORMA.TABLE_NAME, cv,
                BaseAdapter.DETALLE_PROFORMA.REF_PROFORMA + "=? and " +
                        BaseAdapter.DETALLE_PROFORMA.COD_ARTICULO + "=?", new String[]{consecutivo, codigo});

        if (row == 0) {

            msj("Error", "row can`t be updated");
        }
        db.close();
    }


    private boolean validar(String codigo, String cant) {
        return !codigo.equals("") && !cant.equals("");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void findArticulo(final String codigo, final double cant, final List<ModDetalleProforma> detalles, final AdapterDetalleProforma adapter) {

            com.example.bodega.Models.ContentValues values  = new com.example.bodega.Models.ContentValues();
            values.put("codigo",codigo);
            values.put("api_key",Configuracion.API_KEY);

            StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                    "/articulo/articulo/" + values.toString(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject articulo = new JSONObject(response);
                        if (articulo.length() > 0) {
                            if (articulo.getString("activo").equals("S")) {
                                if (!enLaLista(detalles , adapter, articulo.getString("cod_articulo"), cant)) {
                                    String cod_articulo = articulo.getString("cod_articulo");
                                    String descripcion = articulo.getString("descripcion");
                                    double venta = articulo.getDouble("venta");
                                    int iv = articulo.getInt("porc_impuesto");
                                    double total = venta * cant;
                                    insertarLinea(cod_articulo, descripcion, venta, iv, cant, total);
                                    detalles.add(new ModDetalleProforma(cod_articulo, descripcion, venta, iv, cant, total));
                                    sumarTotales(detalles);
                                    adapter.notifyDataSetChanged();
                                }

                            } else {
                                Toast.makeText(DetalleProforma.this, "El artículo se  encuentra inactivo. : " + articulo.getString("codigo"), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DetalleProforma.this, "El articulo no existe", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        msj("Error", e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                }
            });

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);


    }

    private boolean enLaLista(final List<ModDetalleProforma> lista, final AdapterDetalleProforma adaptador, final String codigo, double cant) {

        for (int i = 0; i <= lista.size() -1; i++){
            if (lista.get(i).getCod_articulo().equals(codigo)){
                final double newCant = lista.get(i).getCantidad() + cant ;
                final double newTotal = lista.get(i).getPrecio() * newCant ;

                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleProforma.this);
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


    private void insertarLinea(String codigo, String articulo, double precio, int iv, double cant, double total) {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(BaseAdapter.DETALLE_PROFORMA.REF_PROFORMA, consecutivo);
            values.put(BaseAdapter.DETALLE_PROFORMA.COD_ARTICULO, codigo);
            values.put(BaseAdapter.DETALLE_PROFORMA.ARTICULO, articulo);
            values.put(BaseAdapter.DETALLE_PROFORMA.PRECIO, precio);
            values.put(BaseAdapter.DETALLE_PROFORMA.IV, iv);
            values.put(BaseAdapter.DETALLE_PROFORMA.CANTIDAD, cant);
            values.put(BaseAdapter.DETALLE_PROFORMA.TOTAL, total);

            long newRowId = db.insert(BaseAdapter.DETALLE_PROFORMA.TABLE_NAME, null, values);

            if (newRowId == -1) {
                Toast.makeText(DetalleProforma.this, "Row can`t be inserted", Toast.LENGTH_SHORT).show();
            }

            db.close();

        } catch (Exception e) {
            msj("Error", e.getMessage());
        }
    }



    private void cargarDetalles(BaseAdapter base, List<ModDetalleProforma> detalles, AdapterDetalleProforma adapter) {
        SQLiteDatabase db = base.getReadableDatabase();

        Cursor c = db.rawQuery("select * from " + BaseAdapter.DETALLE_PROFORMA.TABLE_NAME + " WHERE " + BaseAdapter.DETALLE_PROFORMA.REF_PROFORMA + "=?", new String[]{consecutivo});

        while (c.moveToNext()) {
            detalles.add(new ModDetalleProforma(c.getString(c.getColumnIndex(BaseAdapter.DETALLE_PROFORMA.COD_ARTICULO)),
                    c.getString(c.getColumnIndex(BaseAdapter.DETALLE_PROFORMA.ARTICULO)),
                    c.getDouble(c.getColumnIndex(BaseAdapter.DETALLE_PROFORMA.PRECIO)),
                    c.getInt(c.getColumnIndex(BaseAdapter.DETALLE_PROFORMA.IV)),
                    c.getDouble(c.getColumnIndex(BaseAdapter.DETALLE_PROFORMA.CANTIDAD)),
                    c.getDouble(c.getColumnIndex(BaseAdapter.DETALLE_PROFORMA.TOTAL))));

        }
        c.close();
        db.close();
        adapter.notifyDataSetChanged();
    }


    private void sumarTotales(List<ModDetalleProforma> lista) {
        double tempsubTotalExcento = 0;
        double tempsubTotalGravado = 0;
        double tempMontoIv = 0;
        double tempTotal = 0;

        for (int i = 0; i <= lista.size() - 1; i++) {
            if (lista.get(i).getIv() == 0) {
                tempsubTotalExcento += lista.get(i).getTotal();
            } else if (lista.get(i).getIv() > 0) {
                tempsubTotalGravado += lista.get(i).getTotal();
                tempMontoIv += ((lista.get(i).getTotal() * lista.get(i).getIv()) / 100);
            }
        }
        tempTotal += tempsubTotalExcento + tempsubTotalGravado;

        subTotalExento = tempsubTotalExcento;
        subTotalGravado = tempsubTotalGravado;
        montoImpuesto = tempMontoIv;
        total = tempTotal;

        DecimalFormat formatter = new DecimalFormat("#,###,###");
        tvSubTotalExento.setText(("Total exento ¢ " + formatter.format(subTotalExento)));
        tvSubTotalGravado.setText(("Total gravado ¢ " + formatter.format(subTotalGravado)));
        tvMontoIv.setText(("Monto Iv ¢ " + formatter.format(montoImpuesto)));
        tvTotal.setText(("Total ¢" + formatter.format(total)));

        editarEncabezadoDB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.enviarQpos:
                enviarQpos(detalles);
                break;
            case R.id.clearAll:
                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleProforma.this);
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

    private void enviarQpos(final List<ModDetalleProforma> detallesList) {
        try {
            RequestQueue queue = Volley.newRequestQueue(DetalleProforma.this);
            StringRequest request = new StringRequest(Request.Method.POST, configuracion.getUrl() +
                    "/proforma/guardar", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetalleProforma.this);
                    builder.setTitle("Mensaje");
                    builder.setMessage(response);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            eliminarProformaDB(consecutivo);
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Gson gson = new Gson();
                    String detalles = gson.toJson(detallesList);
                    Map<String, String> params = new HashMap<>();
                    params.put("api_key",Configuracion.API_KEY);
                    params.put("cod_cliente", cod_cliente);
                    params.put("cliente", cliente);
                    params.put("total", String.valueOf(total));
                    params.put("monto_iv_colones", String.valueOf(montoImpuesto));
                    params.put("sub_total_exento", String.valueOf(subTotalExento));
                    params.put("sub_total_gravado", String.valueOf(subTotalGravado));
                    params.put("detalle_proforma", detalles);
                    params.put("user",user);
                    return params;
                }
            };
            queue.add(request);
        } catch (Exception e) {
            msj("Error", e.getMessage());
        }
    }

    private void eliminarProformaDB(String consecutivo) {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();
            db.delete(BaseAdapter.DETALLE_PROFORMA.TABLE_NAME, BaseAdapter.DETALLE_PROFORMA.REF_PROFORMA + "=?", new String[]{consecutivo});
            db.delete(BaseAdapter.PROFORMA.TABLE_NAME, BaseAdapter.PROFORMA.ID + "=?", new String[]{consecutivo});
            db.close();
            detalles.clear();
            adapterDetalleProforma.notifyDataSetChanged();
        } catch (SQLiteException e) {
            msj("Error", e.getMessage());
        }
    }

    private void eliminarDetallesDB() {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();
            double rowAff = db.delete(BaseAdapter.DETALLE_PROFORMA.TABLE_NAME, BaseAdapter.DETALLE_PROFORMA.REF_PROFORMA + "=?", new String[]{consecutivo});
            if (rowAff == 0) {
                Toast.makeText(DetalleProforma.this, "No se limpió la lista en la base de datos", Toast.LENGTH_LONG).show();
            }
        } catch (SQLiteException e) {
            msj("Error", e.getMessage());
        }
    }

    private void limpiarLista() {
        eliminarDetallesDB();
        detalles.clear();
        adapterDetalleProforma.notifyDataSetChanged();
        sumarTotales(detalles);
    }




    private void editarEncabezadoDB() {
        try {
            SQLiteDatabase db = baseAdapter.getWritableDatabase();

            //String date = new SimpleDateFormat("Y-m-d", Locale.getDefault()).format(new Date());
            ContentValues values = new ContentValues();
            values.put(BaseAdapter.PROFORMA.TOTAL_EXENTO, subTotalExento);
            values.put(BaseAdapter.PROFORMA.TOTAL_GRAVADO, subTotalGravado);
            values.put(BaseAdapter.PROFORMA.MONTO_IV, montoImpuesto);
            values.put(BaseAdapter.PROFORMA.TOTAL, total);
            db.update(BaseAdapter.PROFORMA.TABLE_NAME, values, BaseAdapter.PROFORMA.ID + "=?", new String[]{consecutivo});

            db.close();

        } catch (SQLiteException e) {
            msj("Error", e.getMessage());
        }
    }

    private void buscarDescripcion() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = View.inflate(DetalleProforma.this,R.layout.dialog_filtro_descripcion,null);
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
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                        final Gson gson = new Gson();
                        com.example.bodega.Models.ContentValues values  = new com.example.bodega.Models.ContentValues();
                        values.put("descripcion",txtArticulo.getText().toString());
                        values.put("api_key",Configuracion.API_KEY);

                        StringRequest  request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                                "/articulo/articulo/"+ values.toString(), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    articulos.clear();
                                    articulos.addAll(Arrays.asList(gson.fromJson(response, ModFiltroArticulo[].class)));
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Toast.makeText(DetalleProforma.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                            }

                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                               msj("Error",new String(error.networkResponse.data,StandardCharsets.UTF_8));
                            }
                        });

                        RequestQueue queue = Volley.newRequestQueue(DetalleProforma.this);
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
                showKeyboard(DetalleProforma.this,txtCantidad);
                dialog.dismiss();
            }
        });
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
            if (scanResult!=null){
                txtCodigo.setText(scanResult);
                txtCantidad.setText("1");
                txtCantidad.requestFocus();
                scanned_from_scan = true ;
                showKeyboard(DetalleProforma.this,txtCantidad);
            }
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
}


