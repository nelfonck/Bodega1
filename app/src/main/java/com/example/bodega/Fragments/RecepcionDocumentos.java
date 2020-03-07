package com.example.bodega.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.bodega.Adaptadores.AdapterDocumentos;
import com.example.bodega.Modelos.Configuracion;
import com.example.bodega.Modelos.ModDocumentos;
import com.example.bodega.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecepcionDocumentos extends Fragment {

    private Configuracion configuracion; ;
    private EditText txtConsecutivo ;
    private TextView tvNombreComercialVendedor ;
    private TextView tvFechaEmision ;
    private ImageView imgEstado ;
    private CheckBox igualar_ultimos_digitos ;

    public RecepcionDocumentos() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recepcion_documentos, container, false);

        imgEstado = view.findViewById(R.id.imgEstado);
        txtConsecutivo = view.findViewById(R.id.txtConsecutivo);
        igualar_ultimos_digitos = view.findViewById(R.id.igualar_ultimos_digitos);
        ImageButton imgClear = view.findViewById(R.id.imgClear);
        tvNombreComercialVendedor = view.findViewById(R.id.tvNombreComercialVendedor);
        tvFechaEmision = view.findViewById(R.id.tvFechaEmision);

        imgEstado.getBackground().setAlpha(120);

        getConfiguracion();


        txtConsecutivo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                if (KeyEvent.ACTION_DOWN == event.getAction()){
                    verificar(txtConsecutivo.getText().toString());
                    return true ;
                }
                return false;
            }
        });

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtConsecutivo.setText("");
                imgEstado.setImageResource(0);
                imgEstado.setBackgroundResource(R.drawable.documentos);
                tvNombreComercialVendedor.setText("");
                tvFechaEmision.setText("");
            }
        });
                return view;
    }
    //OVERLOAD METHODS

    private void verificar(String consecutivo){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, configuracion.getUrl() + "/recepcion_documentos" +
                "?host_db=" + configuracion.getHost_db() +
                "&port_db=" + configuracion.getPort_db() +
                "&user_name=" + configuracion.getUser_name() +
                "&password=" + configuracion.getPassword() +
                "&db_name=" + configuracion.getDatabase() +
                "&schema=" + configuracion.getSchema() +
                "&consecutivo=" + txtConsecutivo.getText().toString() +
                "&iud=" + (igualar_ultimos_digitos.isChecked() ? true :false),null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray documento) {
                try {
                    if (documento.length()>0){
                        if (documento.length()==1){
                            if (documento.getJSONObject(0).getString("estado_recepcion").equals("01")){
                                imgEstado.setImageResource(R.drawable.aprovado);
                            }else if (documento.getJSONObject(0).getString("estado_recepcion").equals("03")){
                                imgEstado.setImageResource(R.drawable.rejected);
                            }else if (documento.getJSONObject(0).getString("estado_recepcion").equals("05")){
                                imgEstado.setImageResource(R.drawable.fileerror);
                                imgEstado.setBackgroundResource(0);
                            }
                            txtConsecutivo.setText(documento.getJSONObject(0).getString("consecutivo_documento"));
                            tvNombreComercialVendedor.setText(documento.getJSONObject(0).getString("nombre_comercial_vendedor"));
                            tvFechaEmision.setText("Fecha de emisión: "+documento.getJSONObject(0).getString("fecha_emision_documento"));
                        }else if (documento.length()>1){
                            mostrarResultados(documento);
                        }

                    }else{
                        imgEstado.setImageResource(R.drawable.noresults);
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
        queue.add(stringRequest);
    }

    private void verificar(String consecutivo,String cod_proveedor){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, configuracion.getUrl() + "/recepcion_documentos" +
                "?host_db=" + configuracion.getHost_db() +
                "&port_db=" + configuracion.getPort_db() +
                "&user_name=" + configuracion.getUser_name() +
                "&password=" + configuracion.getPassword() +
                "&db_name=" + configuracion.getDatabase() +
                "&schema=" + configuracion.getSchema() +
                "&consecutivo=" + txtConsecutivo.getText().toString() +
                "&iud=" + (igualar_ultimos_digitos.isChecked() ? true :false)+
                "&cod_proveedor="+cod_proveedor,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray documento) {
                try {
                    if (documento.length()>0){
                        if (documento.length()==1){
                            if (documento.getJSONObject(0).getString("estado_recepcion").equals("01")){
                                imgEstado.setImageResource(R.drawable.aprovado);
                            }else if (documento.getJSONObject(0).getString("estado_recepcion").equals("03")){
                                imgEstado.setImageResource(R.drawable.rejected);
                            }else if (documento.getJSONObject(0).getString("estado_recepcion").equals("05")){
                                imgEstado.setImageResource(R.drawable.fileerror);
                                imgEstado.setBackgroundResource(0);
                            }
                            txtConsecutivo.setText(documento.getJSONObject(0).getString("consecutivo_documento"));
                            tvNombreComercialVendedor.setText(documento.getJSONObject(0).getString("nombre_comercial_vendedor"));
                            tvFechaEmision.setText("Fecha de emisión: "+documento.getJSONObject(0).getString("fecha_emision_documento"));
                        }else if (documento.length()>1){
                            mostrarResultados(documento);
                        }

                    }else{
                        imgEstado.setImageResource(R.drawable.noresults);
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
        queue.add(stringRequest);
    }

    private void mostrarResultados(final JSONArray documentos){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_documentos_electronicos,null);
        builder.setView(view);
        RecyclerView rvDocumentos = view.findViewById(R.id.rvDocumentosElectronicos);
        final List<ModDocumentos> listDocumentos = new ArrayList<>();

        for (int i = 0; i<= documentos.length()-1;i++){
            try {
                listDocumentos.add(new ModDocumentos(documentos.getJSONObject(i).getString("consecutivo_documento"),
                        documentos.getJSONObject(i).getString("nombre_comercial_vendedor"),
                        documentos.getJSONObject(i).getString("fecha_emision_documento"),documentos.getJSONObject(i).getString("cod_proveedor")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        AdapterDocumentos adapterDocumentos = new AdapterDocumentos(listDocumentos);
        rvDocumentos.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDocumentos.setAdapter(adapterDocumentos);
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();

        adapterDocumentos.SetOnItemClick(new AdapterDocumentos.OnItemClick() {
            @Override
            public void itemClick(int pos) {
                txtConsecutivo.setText(listDocumentos.get(pos).getConsecutivo_hacienda());
                verificar(txtConsecutivo.getText().toString(),listDocumentos.get(pos).getCod_proveedor());
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void getConfiguracion() {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());

        configuracion = new Configuracion();

        configuracion.setHost(p.getString("host_d", ""));
        configuracion.setPort(p.getString("port_d", ""));
        configuracion.setHost_db(p.getString("host_db_d", ""));
        configuracion.setPort_db(p.getString("port_db_d", ""));
        configuracion.setUser_name(p.getString("user_name_d", ""));
        configuracion.setPassword(p.getString("password_d", ""));
        configuracion.setDatabase(p.getString("db_name_d", ""));
        configuracion.setSchema(p.getString("schema_d", ""));

    }

    private void msj(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
