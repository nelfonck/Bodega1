package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModProveedor;
import com.example.bodega.R;

import java.util.List;

public class ProveedorAdapter extends RecyclerView.Adapter<ProveedorAdapter.ViewHolder> {
    private List<ModProveedor> proveedores ;
    private OnItemClick onItemClick ;

    public interface OnItemClick{
        void onClick(int pos);
    }

    public void SetOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick ;
    }

    public ProveedorAdapter(List<ModProveedor> proveedores) {
        this.proveedores = proveedores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.proveedor_item,parent,false);
        return new ViewHolder(v,onItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvCodProveedor.setText(proveedores.get(position).getCod_proveedor());
        holder.tvRazsocial.setText(proveedores.get(position).getRazocial());
        holder.tvRazonComercial.setText(proveedores.get(position).getRazon_comercial());
    }

    @Override
    public int getItemCount() {
        return proveedores.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        private TextView tvCodProveedor, tvRazsocial, tvRazonComercial ;
        private OnItemClick onItemClick ;

        public ViewHolder(@NonNull View itemView, final OnItemClick onItemClick) {
            super(itemView);
            tvCodProveedor = itemView.findViewById(R.id.tvCodProveedor);
            tvRazsocial = itemView.findViewById(R.id.tvRazocial);
            tvRazonComercial = itemView.findViewById(R.id.tvRazonComercial);
            this.onItemClick = onItemClick;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onItemClick.onClick(getAdapterPosition());
        }
    }
}
