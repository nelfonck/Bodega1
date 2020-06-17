package com.example.bodega.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.InformeErrores;
import com.example.bodega.R;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Login extends AppCompatActivity {
    private Configuracion configuracion;
    private InformeErrores informeErrores;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText txtUser = findViewById(R.id.txtUser);
        final EditText txtPass = findViewById(R.id.txtPass);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        getConfiguracion();

        informeErrores = new InformeErrores(Login.this);

        txtUser.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        txtPass.setText("");
                        txtPass.requestFocus();
                        showKeyboard(Login.this,txtPass);
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


    private void validar(final String user, String pass) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, Configuracion.URL_APIBODEGA +
                "/usuario/login/" + user + "/" + pass + "?api_key=" + Configuracion.API_KEY, null, new Response.Listener<JSONObject>() {
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
                String msj = (error.getMessage() != null && error.getMessage().isEmpty()) ?
                        error.getMessage() : new String(error.networkResponse.data, StandardCharsets.UTF_8);
                informeErrores.enviar("Error",msj);
            }
        });
        queue.add(objectRequest);
    }

    private void getConfiguracion() {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Login.this);

        if (!p.contains("host")) {
            abrirConfiguracion();
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

    @SuppressWarnings("SameParameterValue")
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_login,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.configurar) {
            abrirConfiguracion();
        }
        return super.onOptionsItemSelected(item);
    }

    private void abrirConfiguracion(){
        Intent home = new Intent(Login.this, Home.class);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        home.putExtra("user", "No definido");
        home.putExtra("configurar", true);
        startActivity(home);
    }
}
