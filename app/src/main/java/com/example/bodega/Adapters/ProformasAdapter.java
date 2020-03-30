package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.bodega.Models.ModProforma;
import com.example.bodega.R;

import java.text.DecimalFormat;
import java.util.List;

public class ProformasAdapter extends RecyclerView.Adapter<ProformasAdapter.ViewHolder> {
    private List<ModProforma> proformas ;
    private OnItemClick onItemClick ;
    private OnEliminarItemClick onEliminarItemClick ;

    public ProformasAdapter(List<ModProforma> proformas) {
        this.proformas = proformas;
    }

    public void SetOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick ;
    }

    public void SetOnEliminarItemClick(OnEliminarItemClick onEliminarItemClick){
        this.onEliminarItemClick = onEliminarItemClick ;
    }

    @NonNull
    @Override
    public ProformasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.proformas_item,parent,false);
        return new ViewHolder(v,onItemClick,onEliminarItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ProformasAdapter.ViewHolder holder, int position) {
        holder.tvCodCliente.setText(proformas.get(position).getCod_cliente());
        holder.tvCliente.setText(proformas.get(position).getCliente());
        holder.tvFecha.setText(proformas.get(position).getFecha());
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        holder.tvTotalExento.setText(("Total exento ¢ " +  formatter.format(proformas.get(position).getTotalExento())));
        holder.tvTotalGravado.setText(("Total gravado ¢ " +  formatter.format(proformas.get(position).getTotalGravado())));
        holder.tvMontoGravado.setText(("Monto gravado ¢ " +  formatter.format(proformas.get(position).getMontoIv())));
        holder.tvTotal.setText(("Total ¢ " +  formatter.format(proformas.get(position).getTotal())));
        holder.tvCreditoDisponible.setText(("Crédito disponible ¢"+ formatter.format(proformas.get(position).getCredito_disponible())));
        holder.tvTopeCredito.setText(("Tope crédito ¢"+ formatter.format(proformas.get(position).getTope_credito())));
        holder.tvMontoDeuda.setText(("Monto deuda ¢"+ formatter.format(proformas.get(position).getMonto_deuda())));
    }

    @Override
    public int getItemCount() {
        return proformas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView tvCodCliente , tvCliente, tvFecha,tvTotalExento, tvTotalGravado,
                tvMontoGravado,tvTotal, tvCreditoDisponible,tvTopeCredito, tvMontoDeuda ;
        OnItemClick onItemClick ;
        OnEliminarItemClick onEliminarItemClick ;
        public ViewHolder(@NonNull View itemView, OnItemClick onItemClick, OnEliminarItemClick onEliminarItemClick) {
            super(itemView);
            this.onItemClick = onItemClick ;
            this.onEliminarItemClick = onEliminarItemClick ;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            tvCodCliente = itemView.findViewById(R.id.tvCodCliente) ;
            tvCliente = itemView.findViewById(R.id.tvCliente) ;
            tvFecha = itemView.findViewById(R.id.tvFecha) ;
            tvTotalExento = itemView.findViewById(R.id.tvTotalExento);
            tvTotalGravado = itemView.findViewById(R.id.tvTotalGravado);
            tvMontoGravado = itemView.findViewById(R.id.tvMontoGravado);
            tvTotal = itemView.findViewById(R.id.tvTotalProforma);
            tvCreditoDisponible = itemView.findViewById(R.id.tvCreditoDisponible);
            tvTopeCredito = itemView.findViewById(R.id.tvTopeCredito);
            tvMontoDeuda = itemView.findViewById(R.id.tvMontoDeuda);
        }


        @Override
        public void onClick(View v) {
            onItemClick.onClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onEliminarItemClick.onEliminarClick(getAdapterPosition());
            return true ;
        }
    }

    public interface OnItemClick{
        void onClick(int pos);
    }

    public interface OnEliminarItemClick{
        void onEliminarClick(int pos);
    }
}
