package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModDetalleCompra;
import com.example.bodega.R;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterDetalleCompra extends RecyclerView.Adapter<AdapterDetalleCompra.ViewHolder> {
    private List<ModDetalleCompra> detalle ;
    private OnEliminarListener onEliminarListener ;
    private OnModificarListener onModificarListener ;

    public AdapterDetalleCompra(List<ModDetalleCompra> detalle) {
        this.detalle = detalle;
    }

    public void SetOnEliminarListener(OnEliminarListener onEliminarListener){
        this.onEliminarListener = onEliminarListener ;
    }

    public void SetOnModificarListener(OnModificarListener onModificarListener){
        this.onModificarListener = onModificarListener ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detalle_compra_item,parent,false);
        return new ViewHolder(view,onEliminarListener,onModificarListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        holder.tvCodArticulo.setText(("Cod art: " + detalle.get(position).getCod_articulo()));
        holder.tvDescripcion.setText(detalle.get(position).getDescripcion());
        holder.tvCantidad.setText(("Cant: " + detalle.get(position).getCantidad()));
        holder.tvCosto.setText(("Costo: ₡" + formatter.format(detalle.get(position).getCosto())));
        holder.tvPorcDes.setText(("%Desc: " + detalle.get(position).getDescuento()));
        holder.tvPorIv.setText(("%IV: " + detalle.get(position).getIv()));
        holder.tvSubTotal.setText(("Subtotal: ₡" + formatter.format(detalle.get(position).getSub_total())));
        holder.tvTotalIvi.setText(("Total IVI: ₡" + formatter.format(detalle.get(position).getTotal_ivi())));
    }

    @Override
    public int getItemCount() {
        return detalle.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvCodArticulo,
                    tvDescripcion,
                    tvCantidad,
                    tvCosto,
                    tvSubTotal,
                    tvPorcDes,
                    tvPorIv,
                    tvTotalIvi;
        OnEliminarListener onEliminarListener ;
        OnModificarListener onModificarListener ;

        public ViewHolder(@NonNull View itemView, final OnEliminarListener onEliminarListener, final OnModificarListener onModificarListener) {
            super(itemView);
            this.onEliminarListener = onEliminarListener ;
            this.onModificarListener = onModificarListener ;

            tvCodArticulo = itemView.findViewById(R.id.tvCodArticulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvCosto = itemView.findViewById(R.id.tvCosto);
            tvSubTotal = itemView.findViewById(R.id.tvSubTotal);
            tvPorcDes = itemView.findViewById(R.id.tvPorcDes);
            tvPorIv = itemView.findViewById(R.id.tvPorIv);
            tvTotalIvi = itemView.findViewById(R.id.tvTotalIvi);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onEliminarListener.onEliminar(getAdapterPosition());
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onModificarListener.onModificar(getAdapterPosition());
                }
            });
        }
    }

    public interface OnEliminarListener{
        void onEliminar(int pos);
    }

    public interface OnModificarListener{
        void onModificar(int pos);
    }
}
