package com.example.bodega.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Modelos.ModFiltroArticulo;
import com.example.bodega.R;


import java.util.List;

public class FiltroArticuloAdapter extends RecyclerView.Adapter<FiltroArticuloAdapter.ViewHolder> {
    private List<ModFiltroArticulo> articulos ;
    public OnItemClickListener onItemClickListener;

    public void SetOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener ;
    }

    public FiltroArticuloAdapter(List<ModFiltroArticulo> articulos) {
        this.articulos = articulos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filtro_articulo_item,parent,false);
        return new ViewHolder(view,onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvArticulo.setText(articulos.get(position).getDescripcion());
    }

    @Override
    public int getItemCount() {
        return articulos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvArticulo ;
        OnItemClickListener onItemClickListener ;
        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            tvArticulo = itemView.findViewById(R.id.tvArticulo);
            this.onItemClickListener = onItemClickListener ;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.OnItemClick(getAdapterPosition());
        }
    }

    public interface OnItemClickListener{
        void OnItemClick(int pos);
    }
}
