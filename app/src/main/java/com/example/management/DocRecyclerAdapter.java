package com.example.management;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DocRecyclerAdapter extends RecyclerView.Adapter<DocRecyclerAdapter.DocViewHolder> {

    private Context context;
    private ArrayList<Document> docList;

    public DocRecyclerAdapter(Context context, ArrayList<Document> docList) {
        this.context = context;
        this.docList = docList;
    }

    public static class DocViewHolder extends RecyclerView.ViewHolder {

        public TextView tvDocNumber;
        public TextView tvDocItems;
        public TextView tvDocDate;

        public DocViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDocNumber = itemView.findViewById(R.id.tv_doc_number);
            tvDocItems = itemView.findViewById(R.id.tv_doc_items);
            tvDocDate = itemView.findViewById(R.id.tv_doc_date);

        }

    }

    @NonNull
    @Override
    public DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.doc_recycler_items, parent, false);
        return new DocRecyclerAdapter.DocViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        holder.tvDocNumber.setText(String.valueOf(docList.get(position).getNumber()));
        holder.tvDocItems.setText(docList.get(position).getTableListAsString());
        holder.tvDocItems.setTextColor(docList.get(position).getDocumentIn() ? context.getResources().getColor(R.color.DarkGreen) : context.getResources().getColor(R.color.DarkRed));
        holder.tvDocDate.setText(docList.get(position).getDateAsStringType());
    }

    @Override
    public int getItemCount() { return docList.size(); }

}
