package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModCompra;
import com.example.bodega.R;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterCompra extends RecyclerView.Adapter<AdapterCompra.ViewHolder> {
    private List<ModCompra> compras ;
    private OnEliminar onEliminar ;
    private OnModificar onModificar ;

    public AdapterCompra (List<ModCompra> compras){
        this.compras = compras ;
    }
    public void SetOnEliminarListener(OnEliminar onEliminar){
        this.onEliminar = onEliminar ;
    }

    public void SetOnModificarListener(OnModificar onModificar){
        this.onModificar = onModificar;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.compra_item,parent,false);
        return new ViewHolder(view,onModificar,onEliminar);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        holder.tvCompra.setText(("# " + compras.get(position).getNumero_compra()));
        holder.tvCodProveedor.setText(("Proveedor: " + compras.get(position).getCod_proveedor()));
        holder.tvRazonScocial.setText(compras.get(position).getRazon_social());
        holder.tvEstado.setText(compras.get(position).getEstado());
        holder.tvTotal.setText(("Total ₡ " + formatter.format(compras.get(position).getTotal())));
        holder.tvFecha.setText(compras.get(position).getFecha());
    }

    @Override
    public int getItemCount() {
        return compras.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompra ;
        TextView tvCodProveedor ;
        TextView tvRazonScocial ;
        TextView tvEstado ;
        TextView tvTotal ;
        TextView tvFecha ;
        OnModificar onModificar ;
        OnEliminar onEliminar ;

        public ViewHolder(@NonNull View itemView, final OnModificar onModificar, final OnEliminar onEliminar) {
            super(itemView);
            tvCompra = itemView.findViewById(R.id.tvCompra);
            tvCodProveedor = itemView.findViewById(R.id.tvCodProveedor);
            tvRazonScocial = itemView.findViewById(R.id.tvRazonSocial);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            this.onModificar = onModificar ;
            this.onEliminar = onEliminar ;

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onEliminar.eliminar(getAdapterPosition());
                    return false;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onModificar.modificar(getAdapterPosition());
                }
            });
        }
    }

    public interface OnEliminar{
        void eliminar(int pos);
    }

    public interface OnModificar{
        void modificar(int pos);
    }
}
