package com.example.bodega;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.bodega.Fragments.Articulos;
import com.example.bodega.Fragments.Habladores;
import com.example.bodega.Fragments.NotasCredito;
import com.example.bodega.Fragments.Preferencias;
import com.example.bodega.Fragments.Proformas;
import com.example.bodega.Fragments.RecepcionDocumentos;
import com.example.bodega.Fragments.Salidas;
import com.google.android.material.navigation.NavigationView;

public class Home extends AppCompatActivity {
    private DrawerLayout drawer ;
    private NavigationView nav ;
    private static String user = "undefined";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.naview);

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

}
