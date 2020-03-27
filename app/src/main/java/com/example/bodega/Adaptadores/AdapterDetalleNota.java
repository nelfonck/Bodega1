package com.example.bodega.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Modelos.ModDetalleNota;
import com.example.bodega.R;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterDetalleNota extends RecyclerView.Adapter<AdapterDetalleNota.ViewHolder>{

    private List<ModDetalleNota> detalle ;
    private OnItemClickListener onItemClickListener ;
    private OnLongItemClickListener onLongItemClickListener ;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener ;
    }

    public void setOnLongItemClickListener(OnLongItemClickListener onLongItemClickListener){
        this.onLongItemClickListener = onLongItemClickListener ;
    }

    public AdapterDetalleNota(List<ModDetalleNota> detalle) {
        this.detalle = detalle;
    }

    private interface OnItemClickListener{
        void onItemClick(int pos);
    }

    private interface OnLongItemClickListener{
        void onLongItemClick(int pos);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detalle_nota_item,parent,false);
        return new ViewHolder(view,onItemClickListener,onLongItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        holder.tvCodigo.setText(("Cod. Art."+ detalle.get(pos).getCodigo()));
        holder.tvDescripcion.setText(detalle.get(pos).getDescripcion());
        holder.tvCantidad.setText(("Cant:" + detalle.get(pos).getCantidad()));
        holder.tvCosto.setText(("Costo: ₡"+ formatter.format(detalle.get(pos).getCosto())));
        holder.tvImpuesto.setText(("%Imp: " + detalle.get(pos).getImpuesto()));
        holder.tvMontoImpuesto.setText(("%IV ₡"+formatter.format(detalle.get(pos).getMonto_impuesto())));
        holder.tvTotalIvi.setText(("Total IVI ₡ \n"+formatter.format(detalle.get(pos).getTotal_ivi())));
    }

    @Override
    public int getItemCount() {
        return detalle.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView tvCodigo;
        TextView tvDescripcion ;
        TextView tvCantidad ;
        TextView tvCosto;
        TextView tvImpuesto;
        TextView tvMontoImpuesto ;
        TextView tvTotalIvi;
        OnItemClickListener onItemClickListener ;
        OnLongItemClickListener onLongItemClickListener ;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener, OnLongItemClickListener onLongItemClickListener) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvCosto = itemView.findViewById(R.id.tvCosto);
            tvImpuesto = itemView.findViewById(R.id.tvPorcImpuesto);
            tvMontoImpuesto = itemView.findViewById(R.id.tvMontoImpuesto);
            tvTotalIvi = itemView.findViewById(R.id.tvTotalLinea);
            this.onItemClickListener = onItemClickListener ;
            this.onLongItemClickListener = onLongItemClickListener ;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onLongItemClickListener.onLongItemClick(getAdapterPosition());
            return false;
        }
    }
}
