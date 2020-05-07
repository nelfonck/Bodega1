package com.example.bodega.Models;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;

public class InformeErrores {

   private static final String email = "nelfonck@gmail.com";
   private static final String pass = "NHisoka0571";
   private static final String mailTo  = "nelfonck@gmail.com";
   private Context context ;

    public InformeErrores(Context context) {
        this.context = context ;
    }

    public void enviar(final String subject, final String body)
    {
        new AlertDialog.Builder(context)
                .setTitle(subject)
                .setMessage(body)
                .setPositiveButton("Enviar informe de errores", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        BackgroundMail.newBuilder(context)
                                .withUsername(email)
                                .withPassword(pass)
                                .withMailto(mailTo)
                                .withSubject(subject)
                                .withBody(body)
                                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                                    @Override
                                    public void onSuccess() {
                                        //do some magic
                                        Toast.makeText(context, "Los errores fueron enviados para su corrección", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                                    @Override
                                    public void onFail() {
                                        //do some magic
                                        Toast.makeText(context, "Ocurrió un error al enviar el informe de errores", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .send();
                    }
                }).show();



    }
}
