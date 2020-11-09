package com.example.bodega.Activities;

import android.content.ContentValues;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.bodega.Adapters.PageAdapter;
import com.example.bodega.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class DetalleCompra extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    TabItem tabEncabezado, tabDetalle, tabNota ;
    PageAdapter pageAdapter ;

    private String myString = "hello";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_compra);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewpager);
        tabEncabezado = findViewById(R.id.tabEncabezado);
        tabDetalle = findViewById(R.id.tabDetalle);
        tabNota = findViewById(R.id.tabNotas);

        Bundle args = getIntent().getExtras();

        pageAdapter = new PageAdapter(getSupportFragmentManager(),tabLayout.getTabCount(),args);

        viewPager.setAdapter(pageAdapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition()>=0 && tab.getPosition()<=2){
                    pageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


    }

    public String getMyData() {
        return myString;
    }

    public void setByData(String str){
        this.myString = str ;
    }
}