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
import com.example.bodega.Adapters.AdapterDocumentos;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ContentValues;
import com.example.bodega.Models.ModDocumentos;
import com.example.bodega.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.nio.charset.StandardCharsets;
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
    private TextView tvCliente ;
    private TextView tvTotal ;
    private EditText txtTotalFactura ;
    private ImageView imgEstado ;
    private CheckBox igualar_ultimos_digitos ;
    private ImageButton imgClear ;

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
        imgClear = view.findViewById(R.id.imgClear);
        tvNombreComercialVendedor = view.findViewById(R.id.tvNombreComercialVendedor);
        tvFechaEmision = view.findViewById(R.id.tvFechaEmision);
        tvCliente = view.findViewById(R.id.tvCliente);
        tvTotal = view.findViewById(R.id.tvTotal);
        imgEstado.getBackground().setAlpha(120);
        txtTotalFactura = view.findViewById(R.id.txtTotalFactura);


        txtConsecutivo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                if (KeyEvent.ACTION_DOWN == event.getAction()){
                    verificar(txtConsecutivo.getText().toString(),txtTotalFactura.getText().toString());
                    return true ;
                }
                return false;
            }
        });

        txtTotalFactura.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    if (KeyEvent.ACTION_DOWN == event.getAction()){
                        verificar(txtConsecutivo.getText().toString(),txtTotalFactura.getText().toString());
                        return true ;
                    }
                return false;
            }
        });

        txtTotalFactura.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                txtConsecutivo.setText("");
            }
        });

        txtConsecutivo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                txtTotalFactura.setText("");
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
                tvCliente.setText("");
                tvTotal.setText("");
                txtTotalFactura.setText("");
                txtConsecutivo.requestFocus();
            }
        });
                return view;
    }
    //OVERLOAD METHODS

    private void verificar(String consecutivo,String totalFactura){
        ContentValues values = new ContentValues();
        values.put("consecutivo", (consecutivo.length() >0) ? consecutivo : "");
        values.put("total", (totalFactura.length() >0) ? totalFactura : "");
        values.put("iud",(igualar_ultimos_digitos.isChecked() ? "true" :"false"));
        values.put("api_key",Configuracion.API_KEY);

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, Configuracion.URL_APIBODEGA +
                "/recepcion/estado" +
           values.toString(),null, new Response.Listener<JSONArray>() {
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
                            tvNombreComercialVendedor.setText(documento.getJSONObject(0).getString("nombre_vendedor"));
                            tvFechaEmision.setText("Fecha de emisión: "+documento.getJSONObject(0).getString("fecha_emision_documento"));
                            tvCliente.setText("Cliente: "+documento.getJSONObject(0).getString("cliente") );
                            tvTotal.setText("Total ¢ "+ documento.getJSONObject(0).getString("total"));

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
                String msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                msj("Error",msg);
            }
        });
        queue.add(stringRequest);
    }

    private void verificar(int id, String consecutivo){
        ContentValues values = new ContentValues();
        values.put("consecutivo", consecutivo);
        values.put("iud",(igualar_ultimos_digitos.isChecked() ? "true" :"false"));
        values.put("id",String.valueOf(id));
        values.put("api_key",Configuracion.API_KEY);

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, Configuracion.URL_APIBODEGA +
                "/recepcion/estado" +
                values.toString(),null, new Response.Listener<JSONArray>() {
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
                            tvNombreComercialVendedor.setText(documento.getJSONObject(0).getString("nombre_vendedor"));
                            tvFechaEmision.setText("Fecha de emisión: "+documento.getJSONObject(0).getString("fecha_emision_documento"));
                            tvCliente.setText("Cliente: "+documento.getJSONObject(0).getString("cliente") );
                            tvTotal.setText("Total ¢ "+ documento.getJSONObject(0).getString("total"));

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
                String msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                msj("Error",msg);
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
                        documentos.getJSONObject(i).getString("nombre_vendedor"),
                        documentos.getJSONObject(i).getString("fecha_emision_documento"),
                        documentos.getJSONObject(i).getInt("id"),
                        documentos.getJSONObject(i).getString("cliente"),
                        documentos.getJSONObject(i).getString("total")));
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
                verificar(listDocumentos.get(pos).getId(),listDocumentos.get(pos).getConsecutivo_hacienda());
                dialog.dismiss();
            }
        });

        dialog.show();

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
