package com.example.bodega.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.bodega.Modelos.Clientes;
import com.example.bodega.R;

import java.util.List;

public class AdapterClientes  extends RecyclerView.Adapter<AdapterClientes.ViewHolder> {
    private List<Clientes> clientes ;
    private OnClickListener onClickListener ;

    public AdapterClientes(List<Clientes> clientes) {
        this.clientes = clientes;
    }

    public interface OnClickListener{
        void onClick(int pos);
    }

    public void SetOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.filtro_cliente_item,parent,false);
        return new ViewHolder(v,onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvCodCliente.setText(clientes.get(position).getCod_cliente());
        holder.tvCliente.setText(clientes.get(position).getRazon_social());
    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnClickListener onClickListener ;
        private TextView tvCodCliente  ;
        private TextView tvCliente ;

        public ViewHolder(@NonNull View itemView, OnClickListener onClickListener){
            super(itemView);
            tvCodCliente = itemView.findViewById(R.id.tvCodCliente);
            tvCliente = itemView.findViewById(R.id.tvCliente);
            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(getAdapterPosition());
        }
    }
}
