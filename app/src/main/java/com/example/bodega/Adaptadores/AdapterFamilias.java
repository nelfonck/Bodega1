package com.example.bodega.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.bodega.Modelos.ModFamilia;
import com.example.bodega.R;

import java.util.List;

public class AdapterFamilias extends RecyclerView.Adapter<AdapterFamilias.ViewHolder> {
    private List<ModFamilia> familias ;
    public OnItemClickListener onItemClickListener ;

    public interface OnItemClickListener{
        void OnItemClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener ;
    }

    public AdapterFamilias(List<ModFamilia> familias) {
        this.familias = familias;
    }

    @NonNull
    @Override
    public AdapterFamilias.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.familias_item,parent,false);
        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFamilias.ViewHolder holder, int position) {
        holder.tvFamilia.setText(familias.get(position).getFamilia());
    }

    @Override
    public int getItemCount() {
        return familias.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvFamilia;
        OnItemClickListener onItemClickListener ;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            tvFamilia = itemView.findViewById(R.id.tvFamilia);
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.OnItemClick(getAdapterPosition());
        }
    }

}
