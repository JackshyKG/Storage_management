package com.example.management;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder> {

    private Context context;
    private ArrayList<String[]> itemList;

    private String[] arrItems;
    private ArrayAdapter<String> adapter;
    private boolean onCreateAdapter = true;


    public ItemRecyclerAdapter(Context context, ArrayList<String[]> itemList) {
        this.context = context;
        this.itemList = itemList;
        getItemsFromDB();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView tvEnum;
        public AutoCompleteTextView acItem;
        public EditText etCount;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            tvEnum = itemView.findViewById(R.id.tv_item_enum);
            acItem = itemView.findViewById(R.id.ac_tv_item);
            etCount = itemView.findViewById(R.id.et_count);

            adapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1, arrItems);
            acItem.setAdapter(adapter);
            acItem.setThreshold(2);

            acItem.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {

                    if (onCreateAdapter) {
                        return;
                    }
                    itemList.get(getAdapterPosition())[0] = s.toString();

                }

            });
            acItem.setOnFocusChangeListener((v, hasFocus) -> {
                if (onCreateAdapter) {
                    onCreateAdapter = false;
                }
            });

            etCount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {

                    if (onCreateAdapter) {
                        return;
                    }
                    itemList.get(getAdapterPosition())[1] = s.toString();

                }
            });
            etCount.setOnFocusChangeListener((v, hasFocus) -> {
                if (onCreateAdapter) {
                    onCreateAdapter = false;
                }
            });

        }
    }

    private void getItemsFromDB() {

        SQLiteDB sqLiteDB = new SQLiteDB(context);
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        String table = "items";
        String[] columns = {"item"};
        Cursor cursor = database.query(table, columns, null, null, null, null, null);

        arrItems = new String[cursor.getCount()];

        if (cursor.moveToFirst()) {
            int iIndex = cursor.getColumnIndex("item");
            do {
                arrItems[cursor.getPosition()] = cursor.getString(iIndex);
            } while (cursor.moveToNext());
        }

        return;

    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.open_table_items, parent, false);
        return new ItemViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.tvEnum.setText(position + 1 + ".");
        holder.acItem.setText(itemList.get(position)[0]);
        holder.etCount.setText(itemList.get(position)[1]);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
