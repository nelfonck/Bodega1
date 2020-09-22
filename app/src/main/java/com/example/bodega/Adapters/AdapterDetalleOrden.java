package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModDetalleOrden;
import com.example.bodega.R;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterDetalleOrden extends RecyclerView.Adapter<AdapterDetalleOrden.ViewHorlder> {
    private List<ModDetalleOrden> detalle ;
    private OnCambiarCantidad onCambiarCantidad ;
    private OnEliminarLinea onEliminarLinea ;

    public AdapterDetalleOrden(List<ModDetalleOrden> detalle) {
        this.detalle = detalle;
    }

    public void SetOnCambiarCantidad(OnCambiarCantidad onCambiarCantidad){
        this.onCambiarCantidad = onCambiarCantidad ;
    }

    public void SetOnEliminarLinea(OnEliminarLinea onEliminarLinea){
        this.onEliminarLinea = onEliminarLinea ;
    }

    @NonNull
    @Override
    public ViewHorlder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.detalle_orden_item,parent,false);
        return new ViewHorlder(v,onCambiarCantidad, onEliminarLinea);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHorlder holder, int position) {
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        holder.tvDescripcion.setText(detalle.get(position).getDescripcion());
        holder.tvCantidad.setText(("Cant:" +detalle.get(position).getCantidad()));
        holder.tvCosto.setText(("Costo: " + formatter.format(detalle.get(position).getCosto())));
        holder.tvImpuesto.setText(("Imp: ₡" + detalle.get(position).getPorc_impuesto()));
        holder.tvTotalImpuesto.setText(("Total imp: ₡" + formatter.format(detalle.get(position).getTotal_impuesto())));
        holder.tvTotal.setText(("total: ₡" + formatter.format(detalle.get(position).getTotal())));
    }

    @Override
    public int getItemCount() {
        return detalle.size();
    }

    public class ViewHorlder extends RecyclerView.ViewHolder {
        TextView  tvDescripcion, tvCantidad, tvCosto, tvImpuesto,tvTotalImpuesto, tvTotal;
        OnCambiarCantidad onCambiarCantidad ;
        OnEliminarLinea onEliminarLinea ;
        public ViewHorlder(@NonNull View itemView, final OnCambiarCantidad onCambiarCantidad, final OnEliminarLinea onEliminarLinea) {
            super(itemView);
            this.onCambiarCantidad = onCambiarCantidad ;
            this.onEliminarLinea = onEliminarLinea ;

            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCantidad = itemView.findViewById(R.id.tvCambiarCantidad);
            tvCosto = itemView.findViewById(R.id.tvCosto);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvImpuesto = itemView.findViewById(R.id.tvPorcImpuesto);
            tvTotalImpuesto = itemView.findViewById(R.id.tvTotalImpuesto);
            tvTotal = itemView.findViewById(R.id.tvTotal);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCambiarCantidad.cambiarCantiad(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onEliminarLinea.eliminarLinea(getAdapterPosition());
                    return false;
                }
            });
        }
    }

    public interface OnCambiarCantidad{
        void cambiarCantiad(int pos);
    }
    public interface OnEliminarLinea{
        void eliminarLinea(int pos);
    }
}
