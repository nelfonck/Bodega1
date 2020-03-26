package com.example.bodega;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Fragments.Articulos;
import com.example.bodega.Fragments.Habladores;
import com.example.bodega.Fragments.NotasCredito;
import com.example.bodega.Fragments.Preferencias;
import com.example.bodega.Fragments.Proformas;
import com.example.bodega.Fragments.RecepcionDocumentos;
import com.example.bodega.Fragments.Salidas;
import com.example.bodega.Modelos.Configuracion;
import com.example.bodega.Modelos.ContentValues;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class Home extends AppCompatActivity {
    private DrawerLayout drawer ;
    private NavigationView nav ;
    private static String user = "undefined";
    private Configuracion configuracion ;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.naview);

        getConfiguracion();

        TextView tvUser = nav.getHeaderView(0).findViewById(R.id.tvUser);
        user = getIntent().getExtras().getString("user");
        tvUser.setText("Usuario: " + user);

        boolean configurar = getIntent().getExtras().getBoolean("configurar");
        if (configurar) {
            getFragmentManager().beginTransaction().replace(R.id.contenedor,new Preferencias()).commit();
            getSupportActionBar().setTitle("Configurar");
        }

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean fragmentTransaction = false;

                Fragment fr = null ;
                String titulo = "Compras movil" ;
                switch (item.getItemId()){
                    case R.id.habladores:
                        fr = new Habladores();
                        fragmentTransaction = true ;
                        titulo = "Etiquetas";
                        break;
                    case R.id.proforma :
                        fr = new Proformas();
                        Bundle param = new Bundle();
                        param.putString("user",user);
                        fr.setArguments(param);
                        fragmentTransaction = true ;
                        titulo = "Proformas";
                        break;
                    case R.id.preferencias:
                        fr = new Preferencias();
                        fragmentTransaction = true ;
                        titulo = "Preferencias";
                        break;
                    case R.id.gestionArticulos:
                        fr = new Articulos();
                        Bundle p = new Bundle();
                        p.putString("user",user);
                        fr.setArguments(p);
                        fragmentTransaction = true ;
                        titulo = "Gestión de artículos";
                        break;

                    case R.id.rotacion:
                        fr = new Salidas();
                        fragmentTransaction = true ;
                        titulo = "Salidas";
                        break;
                    case R.id.recepcion_documentos :
                        fr = new RecepcionDocumentos();
                        fragmentTransaction = true ;
                        titulo = "Verificar recepción de documentos";
                        break;
                    case R.id.notas_credito:
                        fr = new NotasCredito();
                        fragmentTransaction = true ;
                        titulo = "Nótas de crédito";
                        break;
                    case R.id.salir :
                        finish();
                        break;
                }

                if (fragmentTransaction){
                    getFragmentManager().beginTransaction().replace(R.id.contenedor,fr).commit();
                    getSupportActionBar().setTitle(titulo);
                    nav.setCheckedItem(item.getItemId());
                    drawer.closeDrawer(Gravity.LEFT);
                }
                return false;
            }
        });

        setActionBar();


                checkUpdates();

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

    private void setActionBar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (!drawer.isDrawerOpen(GravityCompat.START))
                    drawer.openDrawer(Gravity.LEFT);
                else drawer.closeDrawer(Gravity.LEFT);
            default:return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkUpdates(){
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int version = pInfo.versionCode ;

            ContentValues cv = new ContentValues() ;
            cv.put("app_name","bodega");
            cv.put("version",String.valueOf(version));

            final String url = configuracion.getUrlUpdates()  + cv.toString() ;

            StringRequest objectRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    try {
                        final JSONObject obj = new JSONObject(response);
                        if (obj.length()>0){

                            AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                            builder.setTitle("Actualización requerida");
                            builder.setMessage("Desea acutalizar ahora?");
                            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        String urlFile = obj.getString("urlFile") ;
                                        actualizar(urlFile);
                                    } catch (JSONException e) {
                                       msj("Error",e.getMessage());
                                   }
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
                        msj("Error",e.getMessage());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    msj("Error",error.getMessage());
                }
            });

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(objectRequest);
        } catch (PackageManager.NameNotFoundException e) {
            msj("Error",e.getMessage());
        }
    }

    private void actualizar(String urlFile){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Descargando");
        progressDialog.setMessage("Porfavor espere..");
        progressDialog.show();

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, urlFile,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        try {
                            if (response!=null) {

                                FileOutputStream outputStream;
                                String name= "bodega.apk";
                                outputStream = openFileOutput(name, Context.MODE_PRIVATE);
                                outputStream.write(response);
                                outputStream.close();
                                String filePath = getFilesDir() + "/" + name ;
                                if (progressDialog.isShowing())progressDialog.dismiss();
                                lauchApp(filePath);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                           msj("Error", e.getMessage());
                        }
                    }
                } ,new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                msj("Error",error.getMessage());
            }
        }, null);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
        mRequestQueue.add(request);
    }

    private void lauchApp(String path){
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setDataAndType(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(path)), "application/vnd.android.package-archive" );
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(i);
    }


    private void msj(String title, String msj){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msj);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

}
