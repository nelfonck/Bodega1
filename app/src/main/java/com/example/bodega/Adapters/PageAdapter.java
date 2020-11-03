package com.example.bodega.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.bodega.Fragments.Compra.Detalle;
import com.example.bodega.Fragments.Compra.Encabezado;
import com.example.bodega.Fragments.Compra.Notas;

public class PageAdapter extends FragmentPagerAdapter {

    int numTabs ;

    public PageAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.numTabs = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Encabezado();
            case 1:
                return new Detalle();
            case 2:
                return new Notas();
            default:
                return null ;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
