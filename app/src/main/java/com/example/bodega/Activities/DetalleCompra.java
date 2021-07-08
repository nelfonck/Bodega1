package com.example.bodega.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adapters.PageAdapter;
import com.example.bodega.Fragments.Compra.Detalle;
import com.example.bodega.Fragments.Compra.Encabezado;
import com.example.bodega.Models.Compra;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ModDetalleCompra;
import com.example.bodega.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetalleCompra extends AppCompatActivity  {
    TabLayout tabLayout;
    ViewPager viewPager;
    TabItem tabEncabezado, tabDetalle, tabNota ;
    PageAdapter pageAdapter ;
    Configuracion configuracion ;
    public Compra compra ;
    public List<ModDetalleCompra> detalleCompra ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_compra);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewpager);
        tabEncabezado = findViewById(R.id.tabEncabezado);
        tabDetalle = findViewById(R.id.tabDetalle);
        tabNota = findViewById(R.id.tabNotas);

        compra = new Compra();
        compra.setRazon_comercial("hello wordl");
        detalleCompra = new ArrayList<>();
        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        pageAdapter = new PageAdapter(getSupportFragmentManager(),tabLayout.getTabCount());

        viewPager.setAdapter(pageAdapter);

        Bundle args = getIntent().getExtras();
        cargarCompra(args.getString("numero_compra"), args.getString("cod_proveedor"));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition()>=0 && tab.getPosition()<=2){
                    pageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setCurrentItem(1);

    }



    public void cargarCompra(String numero_compra, String cod_proveedor){
        StringRequest request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                "/compras/compra/" + numero_compra + "/" + cod_proveedor + "?api_key=" + Configuracion.API_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String r) {
                try {
                    JSONObject response = new JSONObject(r);
                    if (response.getJSONObject("compra").length() > 0){
                        compra.setId(response.getJSONObject("compra").getInt("id"));
                        compra.setNumero_compra(response.getJSONObject("compra").getString("numero_compra"));
                        compra.setCod_proveedor(response.getJSONObject("compra").getString("cod_proveedor"));
                        compra.setRazon_social(response.getJSONObject("compra").getString("razon_social"));
                        compra.setRazon_comercial(response.getJSONObject("compra").getString("razon_comercial"));
                        compra.setSub_total(response.getJSONObject("compra").getDouble("sub_total"));
                        compra.setDescuento(response.getJSONObject("compra").getDouble("descuento"));
                        compra.setImpuestos(response.getJSONObject("compra").getDouble("impuestos"));
                        compra.setTotal(response.getJSONObject("compra").getDouble("total"));
                        compra.setNota(response.getJSONObject("compra").getString("nota"));

                        if (response.getJSONArray("detalle_compra").length()>0)
                        {
                            Gson gson = new Gson();
                            detalleCompra.addAll(Arrays.asList(gson.fromJson(response.getJSONArray("detalle_compra").toString(),ModDetalleCompra[].class)));
                        }

                        ((Encabezado) pageAdapter.instantiateItem(viewPager, 0)).mostrarDatos();
                        ((Detalle) pageAdapter.instantiateItem(viewPager, 1)).mostrarDatos();
                    }
                } catch (JSONException e) {
                    msj("Error", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse!=null){
                    msj("Error",new String(error.networkResponse.data, StandardCharsets.UTF_8));
                }else{
                    msj("Error",error.getMessage());
                }
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
    @SuppressWarnings("SameParameterValue")
    private void msj(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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