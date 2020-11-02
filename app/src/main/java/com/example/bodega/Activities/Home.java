package com.example.bodega.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adapters.BaseAdapter;
import com.example.bodega.Adapters.InputStreamVolleyRequest;
import com.example.bodega.Fragments.Articulos;
import com.example.bodega.Fragments.BloqueArticulos;
import com.example.bodega.Fragments.Habladores;
import com.example.bodega.Fragments.NotasCredito;
import com.example.bodega.Fragments.OrdenCompra;
import com.example.bodega.Fragments.Preferencias;
import com.example.bodega.Fragments.Proformas;
import com.example.bodega.Fragments.RecepcionDocumentos;
import com.example.bodega.Fragments.RegistroCompras;
import com.example.bodega.Fragments.Salidas;

import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.R;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;


public class Home extends AppCompatActivity {
    private DrawerLayout drawer ;
    private NavigationView nav ;
    private static String user ;
    private Configuracion configuracion ;
    private static final int REQUEST_CODE = 1;
    private static final String[] PERMISOS = {
            Manifest.permission.INSTALL_PACKAGES,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.naview);

        int readStorage = ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStorage = ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera = ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA);

        if (readStorage != PackageManager.PERMISSION_GRANTED ||
                writeStorage != PackageManager.PERMISSION_GRANTED ||
                camera != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,PERMISOS,REQUEST_CODE);
        }

        configuracion = new Configuracion();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        configuracion.setHost(sp.getString("host",""));
        configuracion.setPort(sp.getString("port",""));

        TextView tvUser = nav.getHeaderView(0).findViewById(R.id.tvUser);
        user = (getIntent().getExtras()!=null) ? getIntent().getExtras().getString("user") : "undefined" ;

        tvUser.setText(("Usuario: " + user));

        boolean configurar = getIntent().getExtras().getBoolean("configurar");
        if (configurar) {
            getFragmentManager().beginTransaction().replace(R.id.contenedor,new Preferencias()).commit();
            if (getSupportActionBar()!= null)
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
                        Bundle arg = new Bundle();
                        arg.putString("user",user);
                        fr.setArguments(arg);
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
                        Bundle bnc = new Bundle();
                        bnc.putString("user",user);
                        fr.setArguments(bnc);
                        titulo = "Nótas de crédito";
                        break;

                    case R.id.orden_compra :
                        fr = new OrdenCompra();
                        fragmentTransaction = true ;
                        Bundle boc = new Bundle();
                        boc.putString("user",user);
                        fr.setArguments(boc);
                        titulo = "Orden de compra";
                        break;

                    case R.id.bloque_articulos:
                        fr = new BloqueArticulos();
                        fragmentTransaction =true ;
                        Bundle bba = new Bundle();
                        bba.putString("user",user);
                        fr.setArguments(bba);
                        titulo = "Asignar familia a bloque de artículos";
                        break;
                    case R.id.registro_compras:
                        fr = new RegistroCompras();
                        fragmentTransaction =true ;
                        Bundle brc = new Bundle();
                        brc.putString("user",user);
                        fr.setArguments(brc);
                        titulo = "Registro de compras";
                        break;
                    case R.id.salir :
                        finish();
                        break;

                }

                if (fragmentTransaction){
                    getFragmentManager().beginTransaction().replace(R.id.contenedor,fr).commit();
                    if (getSupportActionBar()!=null)
                    getSupportActionBar().setTitle(titulo);
                    nav.setCheckedItem(item.getItemId());
                    drawer.closeDrawer(GravityCompat.START);
                }
                return false;
            }
        });

        setActionBar();

        //checkUpdates();

    }





    private void setActionBar(){
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!drawer.isDrawerOpen(GravityCompat.START))
                drawer.openDrawer(GravityCompat.START);
            else drawer.closeDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }
 /*
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkUpdates(){

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int version = pInfo.versionCode ;
            String version_name = pInfo.versionName ;
            ContentValues cv = new ContentValues() ;

            cv.put("app_name","bodega");
            cv.put("version",String.valueOf(version));
            cv.put("version_name",version_name);

            //final String url = configuracion.getUrlUpdates()  + cv.toString() ;

            StringRequest objectRequest = new StringRequest(Request.Method.GET, configuracion.getUrl(), new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    try {
                        final JSONObject obj = new JSONObject(response);
                        if (obj.length()>0){

                            AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                            builder.setTitle("Actualización requerida");
                            builder.setMessage("\nv "+obj.getString("new_version")+
                                    "\n\nDesea acutalizar ahora?");
                            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        String urlFile = obj.getString("urlFile") ;
                                        updateWebApi(urlFile);

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

    private void updateWebApi(final String uriFile){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Actualizando servicio web");
        progressDialog.setMessage("Porfavor espere..");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, configuracion.getUrl() + "/updateWebApi.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (progressDialog.isShowing()) progressDialog.dismiss();

                if (response.equals("")){
                    //proceder a descargar la app

                    actualizar(uriFile);
                }else{
                    //mostrar el error obtenido
                    msj("",response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                msj("Error","Ha ocurrido un error al actualizar el servicio web \n "+ error.networkResponse.statusCode);
            }
        });
        //timeout 1 minute
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void actualizar(String urlFile){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Descargando app");
        progressDialog.setMessage("Porfavor espere..");
        progressDialog.show();

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, urlFile,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        try {
                            if (response!=null) {

                                String name= "bodega.apk";

                                String filePath =  "/storage/emulated/0/Download/" + name ;

                                FileOutputStream outputStream  = new FileOutputStream(new File("/storage/emulated/0/Download/",name));

                                outputStream.write(response);
                                outputStream.close();


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

        request.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
        mRequestQueue.add(request);
    }

    private void lauchApp(String path){

        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);

        File file = new File(path) ;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            i.setDataAndType(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file), "application/vnd.android.package-archive" );
        }else{
            i.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        }

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(i);


        }
*/
    @SuppressWarnings("SameParameterValue")
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
