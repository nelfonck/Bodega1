package com.example.bodega.Fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.bodega.Adapters.FiltroArticuloAdapter;
import com.example.bodega.Models.Configuracion;
import com.example.bodega.Models.ModFiltroArticulo;
import com.example.bodega.Models.ModSalidas;
import com.example.bodega.R;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Salidas extends Fragment {

    private TextView tvSalidas ;
    private TextView tvArticulo ;
    private Configuracion configuracion;
    private RadarChart chartSalidas ;
    private EditText txtfi ;
    private EditText txtff ;
    private RadioButton rbDia ;
    private RadioButton rbSemana ;
    private RadioButton rbMes ;

    public Salidas() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_salidas, container, false);

        getConfiguracion();

        chartSalidas = view.findViewById(R.id.chartSalidas);


        txtfi = view.findViewById(R.id.txtfi);
        txtff = view.findViewById(R.id.txtff);
        final EditText txtCodigo = view.findViewById(R.id.txtCodigo);
        tvSalidas = view.findViewById(R.id.tvASalidas);
        tvArticulo = view.findViewById(R.id.tvArticulo);
        rbDia = view.findViewById(R.id.rbDia);
        rbSemana = view.findViewById(R.id.rbSemana);
        rbMes = view.findViewById(R.id.rbMes);

        ImageButton btnScan = view.findViewById(R.id.btnScan);
        ImageButton btnBuscarDescripcion = view.findViewById(R.id.btnBuscarDescripcion);

        txtfi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(txtfi);
            }
        });

        txtff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(txtff);
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                scanNow();
            }
        });

        txtCodigo.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (KeyEvent.ACTION_DOWN == event.getAction()) {
                        traerContenido(txtCodigo.getText().toString(),txtfi.getText().toString(),txtff.getText().toString());
                        txtCodigo.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        btnBuscarDescripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  buscarDescripcion(txtCodigo);

            }
        });


        return view ;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            String scanResult = result.getContents();
            traerContenido(scanResult,txtfi.getText().toString(),txtff.getText().toString());
        } else {
            Toast toast = Toast.makeText(getActivity(), "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void llenarGrafica(JSONArray salidas) {

        XAxis xAxis = chartSalidas.getXAxis();
        YAxis yAxis = chartSalidas.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawLabels(false);

        List<RadarEntry> data = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i<= salidas.length()-1;i++){

            try {
                String fi  = salidas.getJSONObject(i).getString("fi");
                fi = fi.substring(0,fi.length()-2);
                String ff = salidas.getJSONObject(i).getString("ff") ;
                ff = ff.substring(0,ff.length()-2);
                int vendidas = salidas.getJSONObject(i).getInt("salidas") ;
                data.add(new RadarEntry(vendidas));
                labels.add(fi.equals(ff)? fi : fi + " - " + ff);
            } catch (JSONException e) {
                msj("Error",e.getMessage());
            }
        }
        RadarDataSet dataSet = new RadarDataSet(data,"Salidas");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(0.5f);
        RadarData radarData = new RadarData();
        radarData.addDataSet(dataSet);

        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        chartSalidas.getLegend().setEnabled(false);
        chartSalidas.getDescription().setEnabled(false);
        chartSalidas.setData(radarData);
        chartSalidas.invalidate();

    }

    private void getConfiguracion() {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void traerContenido(String codigo, String fi, String ff){
        try{
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Obteniendo datos..");
            progressDialog.setMessage("Esto puede tardar dependiendo del rango entre fechas y el historial de ventas");
            progressDialog.show();
            Date date1 = new SimpleDateFormat("dd-MM-yyyy").parse(fi);
            Date date2 = new SimpleDateFormat("dd-MM-yyyy").parse(ff);
            final List<ModSalidas> intervalos = new ArrayList<>();

            int tf = 0 ;
            if (rbDia.isChecked()){
                tf = 1 ;
            }else if (rbSemana.isChecked()){
                tf = 2 ;
            } else if (rbMes.isChecked()){
                tf = 3 ;
            }

            pushFechas(intervalos,date1,date2,tf);

            final Gson gson = new Gson();

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            StringRequest stringRequest = new StringRequest(Request.Method.GET, configuracion.getUrl() + "/salidas/" +
                    "?codigo=" + URLEncoder.encode(codigo,"utf-8") +
                    "&host_db=" + configuracion.getHost_db() +
                    "&port_db=" + configuracion.getPort_db() +
                    "&user_name=" + configuracion.getUser_name() +
                    "&password=" + configuracion.getPassword() +
                    "&db_name=" + configuracion.getDatabase() +
                    "&schema=" + configuracion.getSchema()+
                    "&dias="+gson.toJson(intervalos), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        if (progressDialog.isShowing()){
                            JSONObject jsonObject = new JSONObject(response);

                            tvArticulo.setText(jsonObject.getString("descripcion"));
                            tvSalidas.setText(("Salidas: " + jsonObject.getDouble("salidas")));
                            llenarGrafica(jsonObject.getJSONArray("detallado"));
                            progressDialog.dismiss();
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        }catch (Exception e){
            msj("Error",e.getMessage());
        }
    }

    private void pushFechas(List<ModSalidas> fechas, Date fi, Date ff, int tf){
        try{
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy") ;
            Calendar c = Calendar.getInstance();
            c.setTime(fi);
            Calendar tempCalendar = (Calendar)c.clone();
            Calendar cff =  Calendar.getInstance();
            cff.setTime(ff);

            switch (tf){
                case 1 :
                    while(c.getTime().before(ff) || c.getTime().equals(ff)){
                        fechas.add(new ModSalidas(df.format(c.getTime()),df.format(c.getTime())));
                        c.add(Calendar.DAY_OF_YEAR,1);
                    }
                    break;
                case 2:
                    if (c.get(Calendar.WEEK_OF_YEAR) == cff.get(Calendar.WEEK_OF_YEAR)){
                        fechas.add(new ModSalidas(df.format(c.getTime()),df.format(cff.getTime())));
                    }else{
                        c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
                        c.add(Calendar.WEEK_OF_YEAR,1);

                        while(c.getTime().before(ff)){

                            fechas.add(new ModSalidas(df.format(tempCalendar.getTime()),df.format(c.getTime())));

                            tempCalendar = (Calendar)c.clone();
                            tempCalendar.add(Calendar.DAY_OF_YEAR,1);
                            c.add(Calendar.WEEK_OF_YEAR,1);

                            if (c.getTime().after(ff) || c.getTime().equals(ff)){
                                fechas.add(new ModSalidas(df.format(tempCalendar.getTime()),df.format(ff)));
                            }
                        }
                    }

                    break;
                case 3 :
                    if (c.get(Calendar.MONTH) == cff.get(Calendar.MONTH)){
                        fechas.add(new ModSalidas(df.format(c.getTime()),df.format(cff.getTime())));
                    }else{
                        c.set(Calendar.DAY_OF_MONTH,1);
                        c.add(Calendar.MONTH,1);
                        c.add(Calendar.DAY_OF_YEAR,-1);
                        while(c.getTime().before(ff)){

                            fechas.add(new ModSalidas(df.format(tempCalendar.getTime()),df.format(c.getTime())));

                            tempCalendar = (Calendar)c.clone();
                            tempCalendar.add(Calendar.DAY_OF_YEAR,1);
                            c.add(Calendar.MONTH,1);
                            c.add(Calendar.DAY_OF_YEAR,-1);

                            if (c.getTime().after(ff) || c.getTime().equals(ff)){
                                fechas.add(new ModSalidas(df.format(tempCalendar.getTime()),df.format(ff)));
                            }
                        }
                    }

                    break;
            }
        }catch (Exception e){
            msj("Error", e.getMessage());
        }
    }

    private void showDatePickerDialog(final EditText txt) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(),"Select date");

        newFragment.SetOnSelectedDate(new DatePickerFragment.OnSelectedDate() {
            @Override
            public void OnSelected(int year, int month, int dayOfMonth) {
                String fecha = dayOfMonth + "-"+  month + "-" + year ;
                txt.setText(fecha);
            }
        });

    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private OnSelectedDate onSelectedDate ;

        public void SetOnSelectedDate(OnSelectedDate onSelectedDate){
            this.onSelectedDate = onSelectedDate;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            onSelectedDate.OnSelected(year,month+1,dayOfMonth);
        }

        public interface OnSelectedDate{
            void OnSelected(int year, int month, int dayOfMonth);
        }
    }

    private boolean validarTexto(String texto, String fi, String ff){
        if (texto.equals("")||(fi.equals("")||(ff.equals("")))){
            Toast.makeText(getActivity(),"No pueden haber campos vacíos",Toast.LENGTH_SHORT).show();
            return false ;
        }else{
            return true ;
        }
    }

    private void buscarDescripcion(final EditText txtCodigo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_filtro_descripcion, null);
        builder.setView(view);
        final EditText txtArticulo = view.findViewById(R.id.txtFiltroDescripcion);
        RecyclerView recyclerView = view.findViewById(R.id.rvResultadoFiltroDescripcion);

        final List<ModFiltroArticulo> articulos = new ArrayList<>();
        final FiltroArticuloAdapter adapter = new FiltroArticuloAdapter(articulos);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        txtArticulo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    articulos.clear();
                    final Gson gson = new Gson();
                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    StringRequest request = null;
                    try {
                        request = new StringRequest(Request.Method.GET, configuracion.getUrl() +
                                "/articulos/?descripcion=" + URLEncoder.encode(txtArticulo.getText().toString(),"utf-8") +
                                "&host_db=" + configuracion.getHost_db() +
                                "&port_db=" + configuracion.getPort_db() +
                                "&user_name=" + configuracion.getUser_name() +
                                "&password=" + configuracion.getPassword() +
                                "&db_name=" + configuracion.getDatabase() +
                                "&schema=" + configuracion.getSchema(), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    articulos.addAll(Arrays.asList(gson.fromJson(response, ModFiltroArticulo[].class)));
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                            }

                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    queue.add(request);
                    return true;
                }
                return false;
            }
        });

        adapter.SetOnItemClickListener(new FiltroArticuloAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void OnItemClick(int pos) {
                txtCodigo.setText(articulos.get(pos).getCodigo());
                traerContenido(txtCodigo.getText().toString(),txtfi.getText().toString(),txtff.getText().toString());
                txtCodigo.setText("");
                dialog.dismiss();
            }
        });
    }

    private void msj(String title, String msj){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(msj).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void scanNow() {
        IntentIntegrator intentIntegrator = IntentIntegrator.forFragment(Salidas.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13,IntentIntegrator.EAN_8,IntentIntegrator.UPC_A,IntentIntegrator.UPC_E);
        intentIntegrator.setPrompt("Scan barcode");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
    }

}
