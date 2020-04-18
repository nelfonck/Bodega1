package com.example.bodega.Models;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class InformeErrores {

    private Context context ;

    public InformeErrores(Context context) {
        this.context = context;
    }

    public void enviarInformeErrores(String subject, String error) {
        Log.i("Send email", "");
        String[] TO = {"nelfonck@gmail.com"};
        String[] CC = {"informe de errores"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, error);

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
