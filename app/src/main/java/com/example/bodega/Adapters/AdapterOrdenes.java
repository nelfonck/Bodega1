package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModOrden;
import com.example.bodega.R;

import java.util.List;

public class AdapterOrdenes extends RecyclerView.Adapter<AdapterOrdenes.ViewHolder> {
    private List<ModOrden> ordenes ;
    private OnEliminarOrden onEliminarOrden ;
    private OnAbrirOrden onAbrirOrden ;

    public void SetOnEliminarOrden(OnEliminarOrden onEliminarOrden){
        this.onEliminarOrden = onEliminarOrden ;
    }

    public void SetOnAbrirOrden(OnAbrirOrden onAbrirOrden){
        this.onAbrirOrden = onAbrirOrden ;
    }

    public AdapterOrdenes(List<ModOrden> ordenes) {
        this.ordenes = ordenes;
    }

    @NonNull
    @Override
    public AdapterOrdenes.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.orden_item,parent,false);
        return new ViewHolder(v,onEliminarOrden, onAbrirOrden);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOrdenes.ViewHolder holder, int position) {
        holder.tvCodProveedor.setText(ordenes.get(position).getCod_proveedor());
        holder.tvRazonSocial.setText(ordenes.get(position).getRazon_social());
        holder.tvRazonComercial.setText(ordenes.get(position).getRazon_comercial());
        holder.tvFecha.setText(ordenes.get(position).getFecha_creacion());
    }

    @Override
    public int getItemCount() {
        return ordenes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCodProveedor , tvRazonSocial, tvRazonComercial, tvFecha ;
        OnEliminarOrden onEliminarOrden ;
        OnAbrirOrden onAbrirOrden ;
        public ViewHolder(@NonNull View itemView, final OnEliminarOrden onEliminarOrden, final OnAbrirOrden onAbrirOrden) {
            super(itemView);
            this.onEliminarOrden = onEliminarOrden ;
            this.onAbrirOrden = onAbrirOrden;
            tvCodProveedor = itemView.findViewById(R.id.tvCodProveedor);
            tvRazonSocial = itemView.findViewById(R.id.tvRazonSocial);
            tvRazonComercial = itemView.findViewById(R.id.tvRazonComercial);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onEliminarOrden.eliminarOrden(getAdapterPosition());
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAbrirOrden.abrirOrden(getAdapterPosition());
                }
            });
        }
    }

    public interface OnEliminarOrden{
        void eliminarOrden(int pos);
    }
    public interface OnAbrirOrden{
        void abrirOrden(int pos);
    }
}
