package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModNotaCredito;
import com.example.bodega.R;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterNotaCredito extends RecyclerView.Adapter<AdapterNotaCredito.ViewHolder> {
    private List<ModNotaCredito> notas ;
    private OnLongClickListener onLongClickListener ;
    private OnClickListener onClickListener ;

    public AdapterNotaCredito(List<ModNotaCredito> notas) {
        this.notas = notas;
    }

    public void SetOnLongClickListener(OnLongClickListener onLongClickListener){
        this.onLongClickListener = onLongClickListener;
    }

    public void SetOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nota_credito_item,null);
        return new ViewHolder(view,onClickListener,onLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvId.setText(("# " + notas.get(position).get_id()));
        holder.tvCodProveedor.setText(("Proveedor:"+notas.get(position).getCod_proveedor()));
        holder.tvRazsocial.setText(notas.get(position).getRazsocial());
        holder.tvRazonComercial.setText(notas.get(position).getRazon_comercial());
        holder.tvEstado.setText(notas.get(position).getEstado());
        holder.tvFecha.setText(notas.get(position).getFecha());
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        holder.tvTotal.setText(("Total ¢ " + formatter.format(notas.get(position).getTotal())));
    }

    @Override
    public int getItemCount() {
        return notas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvId, tvCodProveedor, tvRazsocial, tvRazonComercial, tvEstado, tvTotal,tvFecha;
        OnLongClickListener onLongClickListener ;
        OnClickListener onClickListener ;

         ViewHolder(@NonNull View itemView, final OnClickListener onClickListener, final OnLongClickListener onLongClickListener) {
            super(itemView);
            this.onLongClickListener = onLongClickListener;
            this.onClickListener = onClickListener ;
            tvId = itemView.findViewById(R.id.tvId);
            tvCodProveedor = itemView.findViewById(R.id.tvCodProveedor);
            tvRazsocial = itemView.findViewById(R.id.tvRazsocial);
            tvRazonComercial = itemView.findViewById(R.id.tvRazonComercial);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvFecha = itemView.findViewById(R.id.tvFecha);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickListener.onLongClick(getAdapterPosition());
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(getAdapterPosition());
                }
            });
        }

    }

    public interface OnLongClickListener{
        void onLongClick(int pos);
    }

    public interface OnClickListener{
        void onClick(int pos);
    }
}
