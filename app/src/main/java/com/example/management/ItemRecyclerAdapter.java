package com.example.management;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder> {

    private Context context;
    private ArrayList<String[]> itemList;/*0 - id, 1 - name*/

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public ItemRecyclerAdapter(Context context, ArrayList<String[]> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView tvId, tvName;

        public ItemViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            tvId = itemView.findViewById(R.id.tv_item_id);
            tvName = itemView.findViewById(R.id.tv_item_name);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_recycler_items, parent, false);
        return new ItemViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.tvId.setText(itemList.get(position)[0]);
        holder.tvName.setText(itemList.get(position)[1]);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
