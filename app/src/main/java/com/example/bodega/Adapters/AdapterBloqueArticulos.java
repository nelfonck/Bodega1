package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModBloqueArticulos;
import com.example.bodega.R;

import java.util.List;

public class AdapterBloqueArticulos  extends RecyclerView.Adapter<AdapterBloqueArticulos.ViewHolder> {
    private List<ModBloqueArticulos> lista ;
    private  OnEliminarListener onEliminarListener ;


    public AdapterBloqueArticulos(List<ModBloqueArticulos> lista) {
        this.lista = lista;
    }

    public void SetOnEliminarListener(OnEliminarListener onEliminarListener){
        this.onEliminarListener = onEliminarListener ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bloqe_articulo_item,parent,false);
        return new ViewHolder(view,onEliminarListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvCodigo.setText(lista.get(position).getCodigo());
        holder.tvDescripcion.setText(lista.get(position).getDescripcion());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCodigo, tvDescripcion ;
        OnEliminarListener onEliminarListener ;


        public ViewHolder(@NonNull View itemView, final OnEliminarListener onEliminarListener) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            this.onEliminarListener = onEliminarListener ;

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onEliminarListener.onEliminar(getAdapterPosition());
                    return false;
                }
            });

        }
    }

    public interface OnEliminarListener{
        void onEliminar(int pos) ;
    }
}
