package com.example.bodega.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.bodega.Modelos.ModProveedores;
import com.example.bodega.R;

import java.util.List;

public class ProveedoresAdapter extends RecyclerView.Adapter<ProveedoresAdapter.ViewHolder> {
    private List<ModProveedores> proveedores ;
    private OnProveedorListener onProveedorListener;

    public ProveedoresAdapter(List<ModProveedores> proveedores,OnProveedorListener onProveedorListener) {
        this.proveedores = proveedores;
        this.onProveedorListener = onProveedorListener ;
    }

    @NonNull
    @Override
    public ProveedoresAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.proveedores_item,parent,false);
        return new ViewHolder(view,onProveedorListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProveedoresAdapter.ViewHolder holder, int position) {
        holder.tvRazSocial.setText(proveedores.get(position).getRaz_social());
        holder.tvRazComercial.setText(proveedores.get(position).getRaz_comercial());


    }

    @Override
    public int getItemCount() {
        return proveedores.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvRazSocial ;
        private TextView tvRazComercial ;
        private OnProveedorListener onProveedorListener;

        private ViewHolder(@NonNull View itemView, OnProveedorListener onProveedorListener) {
            super(itemView);
            tvRazSocial = itemView.findViewById(R.id.tv_raz_social);
            tvRazComercial = itemView.findViewById(R.id.tv_raz_comercial);
            this.onProveedorListener = onProveedorListener ;
            itemView.setOnClickListener(this);

        }

         @Override
         public void onClick(View v) {
            onProveedorListener.OnProveedorClick(getAdapterPosition());
         }
     }
    public  interface OnProveedorListener{
        void OnProveedorClick(int position);
    }
}
