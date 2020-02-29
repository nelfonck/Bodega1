package com.example.bodega.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.bodega.Modelos.ModDocumentos;
import com.example.bodega.R;

import java.util.List;

public class AdapterDocumentos extends RecyclerView.Adapter<AdapterDocumentos.ViewHolder> {
    private List<ModDocumentos> documentos ;
    private OnItemClick onItemClick ;

    public AdapterDocumentos(List<ModDocumentos> documentos) {
        this.documentos = documentos;
    }

    public void SetOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick ;
    }


    @NonNull
    @Override
    public AdapterDocumentos.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.documentos_item,parent,false);
        return new ViewHolder(view,onItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDocumentos.ViewHolder holder, int position) {
        holder.tvConsecutivo.setText(documentos.get(position).getConsecutivo_hacienda());
        holder.tvNombreComercialVendedor.setText(documentos.get(position).getNombre_comercial_vendedor());
        holder.tvFechaCreacion.setText(("Fecha de emisión: " + documentos.get(position).getFecha_emision()));

    }

    @Override
    public int getItemCount() {
        return documentos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConsecutivo,tvNombreComercialVendedor, tvFechaCreacion ;
        OnItemClick onItemClick ;

        public ViewHolder(@NonNull View item, final OnItemClick onItemClick) {
            super(item);
            this.onItemClick = onItemClick ;
            tvConsecutivo = item.findViewById(R.id.tvConsecutivoItem);
            tvNombreComercialVendedor = item.findViewById(R.id.tvNombreComercialVendedor);
            tvFechaCreacion = item.findViewById(R.id.tvFechaCreacion);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.itemClick(getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClick{
        void itemClick(int pos);
    }


}
