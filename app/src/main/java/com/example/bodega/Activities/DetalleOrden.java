package com.example.bodega.Activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import org.json.JSONArray;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetalleOrden extends AppCompatActivity {
    private static String user ;
    private int id ;
    private List<ModDetalleOrden> detalle ;
    private AdapterDetalleOrden adapter ;
    private Configuracion configuracion ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_orden);
        TextView tvNumeroOrden = findViewById(R.id.tvNumeroOrden);
        TextView tvCodProveedor = findViewById(R.id.tvCodProveedor);
        TextView tvRazonSocial = findViewById(R.id.tvRazonSocial);
        TextView tvRazonComercial = findViewById(R.id.tvRazonComercial);
        EditText txtCodigo = findViewById(R.id.txtCodigo);
        EditText txtCant = findViewById(R.id.txtCantidad);
        ImageButton btnBuscarDescripcion = findViewById(R.id.btnBuscarDescripcion);
        ImageButton btnScan = findViewById(R.id.btnScan);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        RecyclerView rvDetalleOrden = findViewById(R.id.rvDetalleOrden);

        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("user","");
            id = extras.getInt("id");
            tvNumeroOrden.setText(("Orden # " + id));
            tvCodProveedor.setText(("Cod. proveedor: " + extras.getString("cod_proveedor")));
            tvRazonSocial.setText(extras.getString("razon_social"));
            tvRazonComercial.setText(extras.getString("razon_comercial"));
        }

        detalle = new ArrayList<>();
        adapter = new AdapterDetalleOrden(detalle);

        rvDetalleOrden.setHasFixedSize(true);
        rvDetalleOrden.setLayoutManager(new LinearLayoutManager(this));
        rvDetalleOrden.setAdapter(adapter);

        cargarDetalle();

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
                }catch (Exception e){
                    msj("Error",e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                msj("Error",new String(error.networkResponse.data, StandardCharsets.UTF_8));
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