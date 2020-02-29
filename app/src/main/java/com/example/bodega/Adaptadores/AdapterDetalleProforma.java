package com.example.bodega.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.bodega.Modelos.ModDetalleProforma;
import com.example.bodega.R;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterDetalleProforma extends RecyclerView.Adapter<AdapterDetalleProforma.ViewHolder> {
    private List<ModDetalleProforma> detalles ;
    private OnItemClick onItemClick ;
    private OnLongItemClick onLongItemClick ;

    public void SetOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick ;
    }

    public void SetOnLongItemClick(OnLongItemClick onLongItemClick){
        this.onLongItemClick = onLongItemClick ;
    }

    public AdapterDetalleProforma(List<ModDetalleProforma> detalles) {
        this.detalles = detalles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  v = LayoutInflater.from(parent.getContext()).inflate(R.layout.detalle_proforma_item,parent,false);
        return new ViewHolder(v,onItemClick,onLongItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvArticulo.setText(detalles.get(position).getDescripcion());
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String yourFormattedString = formatter.format(detalles.get(position).getPrecio());
        holder.tvPrecio.setText(("¢" + yourFormattedString));
        holder.tvIv.setText(("Iv: " + detalles.get(position).getIv()));
        holder.tvCantidad.setText(("Cant: " + detalles.get(position).getCantidad()));
        yourFormattedString = formatter.format(detalles.get(position).getTotal());
        holder.tvTotal.setText(("¢ " + yourFormattedString));
    }

    @Override
    public int getItemCount() {
        return detalles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvArticulo  ;
        TextView tvPrecio ;
        TextView tvIv ;
        TextView tvCantidad ;
        TextView tvTotal ;
        OnItemClick onItemClick ;
        OnLongItemClick onLongItemClick ;

        public ViewHolder(@NonNull View itemView, final OnItemClick onItemClick, final OnLongItemClick onLongItemClick) {
            super(itemView);
            this.onItemClick = onItemClick;
            this.onLongItemClick = onLongItemClick ;
            tvArticulo = itemView.findViewById(R.id.tvArticulo);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvIv = itemView.findViewById(R.id.tvIv);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvTotal = itemView.findViewById(R.id.tvTotal);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.ItemClick(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongItemClick.ItemLongClick(getAdapterPosition());
                    return false;
                }
            });
        }


    }

    public interface OnItemClick{
        void ItemClick(int pos);
    }
    public interface OnLongItemClick{
        void ItemLongClick(int pos);
    }
}
