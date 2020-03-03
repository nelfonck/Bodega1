package com.example.bodega;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Modelos.Configuracion;

import org.json.JSONObject;

public class Login extends AppCompatActivity {
    private Configuracion configuracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText txtUser = findViewById(R.id.txtUser);
        final EditText txtPass = findViewById(R.id.txtPass);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        getConfiguracion();

        txtUser.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        txtPass.setText("");
                        txtPass.requestFocus();
                        return true;
                    }
                return false;
            }
        });

        txtPass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        validar(txtUser.getText().toString(), txtPass.getText().toString());
                        return true;
                    }
                return false;
            }
        });

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validar(txtUser.getText().toString(), txtPass.getText().toString());
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void validar(final String user, String pass) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, configuracion.getUrl() + "/login/" +
                "?host_db=" + configuracion.getHost_db() +
                "&port_db=" + configuracion.getPort_db() +
                "&user_name=" + configuracion.getUser_name() +
                "&password=" + configuracion.getPassword() +
                "&db_name=" + configuracion.getDatabase() +
                "&schema=" + configuracion.getSchema() +
                "&user=" + user +
                "&pass=" + pass, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.length() > 0) {
                    Intent home = new Intent(Login.this, Home.class);
                    home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    home.putExtra("user", user);
                    startActivity(home);
                } else {
                    Toast.makeText(Login.this, "Usuario o clave incorrecta", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                msj("Error", error.getMessage());
            }
        });
        queue.add(objectRequest);
    }

    private void getConfiguracion() {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Login.this);

        if (!p.contains("host")) {
            Intent home = new Intent(Login.this, Home.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            home.putExtra("user", "No definido");
            home.putExtra("configurar", true);
            startActivity(home);


        }

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

    private void msj(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
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
