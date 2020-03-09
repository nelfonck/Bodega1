package com.example.bodega;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.bodega.Adaptadores.AdapterDetalleProforma;
import com.example.bodega.Adaptadores.BaseAdapter;
import com.example.bodega.Adaptadores.FiltroArticuloAdapter;
import com.example.bodega.Modelos.Configuracion;
import com.example.bodega.Modelos.ModDetalleProforma;
import com.example.bodega.Modelos.ModFiltroArticulo;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

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
    private Configuracion configuracion;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_proforma);

        baseAdapter = new BaseAdapter(this);


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

        getConfiguracion();

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
                        txtCantidad.setText("1");
                        txtCantidad.requestFocus();
                        showKeyboard(DetalleProforma.this,txtCantidad);
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
                        if (validar(txtCodigo, txtCantidad)) {
                            findArticulo(txtCodigo.getText().toString(), Integer.valueOf(txtCantidad.getText().toString()), detalles, adapterDetalleProforma);
                            txtCodigo.setText("");
                            txtCantidad.setText("");
                            txtCodigo.requestFocus();
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
            @Override
            public void onClick(View v) {
                findArticulo(txtCodigo.getText().toString(), Integer.valueOf(txtCantidad.getText().toString()), detalles, adapterDetalleProforma);
                txtCodigo.setText("");
                txtCantidad.setText("");
                txtCodigo.requestFocus();
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
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });
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

    private void cambiarCantidad(final int pos, final List<ModDetalleProforma> lista, final AdapterDetalleProforma adaptador) {

        AlertDialog.Builder builder = new AlertDialog.Builder(DetalleProforma.this);
        View view = LayoutInflater.from(DetalleProforma.this).inflate(R.layout.dialog_editar_linea_proforma, null);
        builder.setView(view);
        final EditText txtCant = view.findViewById(R.id.txtCant);
        final ImageButton btnClearCant = view.findViewById(R.id.btnClearCant);

        txtCant.setText(String.valueOf(lista.get(pos).getCantidad()));
        txtCant.setSelectAllOnFocus(true);

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
                            double newCant = Float.valueOf(txtCant.getText().toString());
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

        if (row > 0) {
            // Toast.makeText(DetalleProforma.this,"Row updated",Toast.LENGTH_SHORT).show();
        } else {
            msj("Error", "row can`t be updated");
        }
        db.close();
    }


    private boolean validar(EditText txtCodigo, EditText txtCant) {
        if (txtCodigo.getText().toString().isEmpty() || txtCant.getText().toString().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    private void findArticulo(final String codigo, final double cant, final List<ModDetalleProforma> detalles, final AdapterDetalleProforma adapter) {
        RequestQueue queue = Volley.newRequestQueue(this);
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
                        if (articulo.getString("activo").equals("S")) {
                            if (!enLaLista(detalles , adapter, articulo.getString("codigo"), cant)) {
                                String cod_articulo = articulo.getString("codigo");
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
                msj("Error", error.getMessage());
            }
        });

        queue.add(request);
    }
    //Si el código se encuentra en la lista entonces cambiamos ajustamos la cantidad y el total de dicha linea
    private boolean enLaLista(List<ModDetalleProforma> lista,AdapterDetalleProforma adaptador, String codigo, double cant) {

        for (int i = 0; i <= lista.size() -1; i++){
            if (lista.get(i).getCod_articulo().equals(codigo)){
                double newCant = lista.get(i).getCantidad() + cant ;
                double newTotal = lista.get(i).getPrecio() * newCant ;

                lista.get(i).setCantidad(newCant);
                lista.get(i).setTotal(newTotal);

                editarLineaDB(consecutivo, codigo, newCant, newTotal);

                sumarTotales(lista);

                adaptador.notifyDataSetChanged();
                editarEncabezadoDB();
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

            if (newRowId != -1) {
                // Toast.makeText(DetalleProforma.this, "Row inserted!", Toast.LENGTH_SHORT).show();
            } else {
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
            StringRequest request = new StringRequest(Request.Method.POST, configuracion.getUrl() + "/proformas/", new Response.Listener<String>() {
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
                    msj("Error", error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Gson gson = new Gson();
                    String detalles = gson.toJson(detallesList);
                    Map<String, String> params = new HashMap<>();
                    params.put("host_db", configuracion.getHost_db());
                    params.put("port_db", configuracion.getPort_db());
                    params.put("user_name", configuracion.getUser_name());
                    params.put("password", configuracion.getPassword());
                    params.put("db_name", configuracion.getDatabase());
                    params.put("schema", configuracion.getSchema());
                    params.put("cod_cliente", cod_cliente);
                    params.put("cliente", cliente);
                    params.put("total", String.valueOf(total));
                    params.put("ocacional", "false");
                    params.put("monto_iv_colones", String.valueOf(montoImpuesto));
                    params.put("total_exento", String.valueOf(subTotalExento));
                    params.put("total_gravado", String.valueOf(subTotalGravado));
                    params.put("detalles_proforma", detalles);
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
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    final Gson gson = new Gson();
                    RequestQueue queue = Volley.newRequestQueue(DetalleProforma.this);
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
                            Toast.makeText(DetalleProforma.this, error.getMessage(), Toast.LENGTH_LONG).show();
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
                txtCodigo.setText(articulos.get(pos).getCodigo());
                txtCantidad.setText("1");
                txtCantidad.requestFocus();
                dialog.dismiss();
            }
        });
    }

    public void scanNow() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
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
        } else {
            Toast toast = Toast.makeText(this, "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}


