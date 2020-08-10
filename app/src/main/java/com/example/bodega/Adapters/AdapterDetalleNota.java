package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModDetalleNota;
import com.example.bodega.R;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterDetalleNota extends RecyclerView.Adapter<AdapterDetalleNota.ViewHolder>{

    private List<ModDetalleNota> detalle ;
    private  OnClickListener onClickListener ;
    private OnLongClickListener onLongClickListener ;

    public void setOnItemClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener ;
    }

    public void setOnLongItemClickListener(OnLongClickListener onLongClickListener){
        this.onLongClickListener = onLongClickListener ;
    }

    public AdapterDetalleNota(List<ModDetalleNota> detalle) {
        this.detalle = detalle;
    }

    public interface OnClickListener{
        void onClick(int pos);
    }

    public interface OnLongClickListener{
        void onLongClick(int pos);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detalle_nota_item,parent,false);
        return new ViewHolder(view,onClickListener,onLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        holder.tvCodigo.setText(("Cod. Art."+ detalle.get(pos).getCodigo()));
        holder.tvDescripcion.setText(detalle.get(pos).getDescripcion());
        holder.tvCantidad.setText(("Cant:" + detalle.get(pos).getCantidad()));
        holder.tvCosto.setText(("Costo: ₡"+ formatter.format(detalle.get(pos).getCosto())));
        holder.tvImpuesto.setText(("%Imp: " + detalle.get(pos).getImpuesto()));
        holder.tvMontoImpuesto.setText(("%IV ₡"+formatter.format(detalle.get(pos).getMonto_impuesto())));
        holder.tvTotalIvi.setText(("Total IVI \n₡"+formatter.format(detalle.get(pos).getTotal_ivi())));
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
        OnClickListener onClickListener ;
        OnLongClickListener onLongClickListener ;

        public ViewHolder(@NonNull View itemView, OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvCosto = itemView.findViewById(R.id.tvCosto);
            tvImpuesto = itemView.findViewById(R.id.tvPorcImpuesto);
            tvMontoImpuesto = itemView.findViewById(R.id.tvMontoImpuesto);
            tvTotalIvi = itemView.findViewById(R.id.tvTotalLinea);
            this.onClickListener = onClickListener ;
            this.onLongClickListener = onLongClickListener ;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onLongClickListener.onLongClick(getAdapterPosition());
            return false;
        }
    }
}
