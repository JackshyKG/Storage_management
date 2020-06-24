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

    private TextView tvDocNumber;
    private TextView tvDocItems;
    private TextView tvDocDate;


    public DocRecyclerAdapter(Context context, ArrayList<Document> docList) {
        this.context = context;
        this.docList = docList;
    }

    public class DocViewHolder extends RecyclerView.ViewHolder {

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
        return new DocViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        tvDocNumber.setText(String.valueOf(docList.get(position).getNumber()));
        tvDocItems.setText(docList.get(position).getTableListAsString());
        tvDocDate.setText(docList.get(position).getDateAsStringType());
    }

    @Override
    public int getItemCount() {
        return docList.size();
    }

    public void updateDocListAndNotify(ArrayList<Document> docList) {
        this.docList = docList;
        notifyDataSetChanged();
    }

}
