package com.example.bodega.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bodega.Models.ModHablador;
import com.example.bodega.R;


import java.text.DecimalFormat;
import java.util.List;

public class HabladoresAdapter extends RecyclerView.Adapter<HabladoresAdapter.ViewHolder> {
    private List<ModHablador> lista ;
    private EliminarItem eliminarItem ;

    public HabladoresAdapter(List<ModHablador> lista) {
        this.lista = lista;
    }

    public interface EliminarItem{
        void OnItemClick(int pos);
    }

    public void setEliminarItem(EliminarItem eliminarItem){
        this.eliminarItem = eliminarItem ;
    }

    @NonNull
    @Override
    public HabladoresAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.habladores_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem,eliminarItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HabladoresAdapter.ViewHolder holder, final int position) {
        holder.tvDescripcion.setText(lista.get(position).getDescripcion());
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String yourFormattedString = formatter.format(lista.get(position).getPrecio());
        holder.tvPrecio.setText("¢" + yourFormattedString);

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvDescripcion, tvPrecio ;
        private ImageView btnDelete ;
        private EliminarItem eliminarItem ;

        public ViewHolder(@NonNull View itemView, EliminarItem eliminarItem) {
            super(itemView);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            btnDelete = itemView.findViewById(R.id.delete);

           this.eliminarItem = eliminarItem ;
           btnDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            eliminarItem.OnItemClick(getAdapterPosition());
        }
    }

}


