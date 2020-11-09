package com.example.bodega.Adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.bodega.Fragments.Compra.Detalle;
import com.example.bodega.Fragments.Compra.Encabezado;
import com.example.bodega.Fragments.Compra.Notas;

public class PageAdapter extends FragmentPagerAdapter {

    int numTabs ;
    Bundle bundle ;

    public PageAdapter(@NonNull FragmentManager fm, int behavior,Bundle bundle) {
        super(fm, behavior);
        this.numTabs = behavior;
        this.bundle = bundle ;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                Encabezado encabezado = new Encabezado();
                encabezado.setArguments(bundle);
                return encabezado;
            case 1:
                Detalle detalle = new Detalle();
                detalle.setArguments(bundle);
                return detalle;
            case 2:
                Notas notas = new Notas();
                notas.setArguments(bundle);
                return notas;
            default:
                return null ;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }

}
