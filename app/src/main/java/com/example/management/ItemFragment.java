package com.example.management;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ItemFragment extends Fragment implements ItemDialog.OnSaveClicked {


    public static final int REQUEST_CODE_OPEN_DIALOG = 14;

    private ImageButton ibAddItem;
    private RecyclerView recyclerView;
    private ItemRecyclerAdapter itemAdapter;
    private EditText etSearch;

    private int currentPosition = -1;
    public ArrayList<String[]> itemList;/*0 - id, 1 - name*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        itemList = new ArrayList<>();
        fillItemList("");

        View rootView = inflater.inflate(R.layout.item_recycler_view, container, false);
        ibAddItem = rootView.findViewById(R.id.ib_add_item);
        recyclerView = rootView.findViewById(R.id.rv_items);
        etSearch = rootView.findViewById(R.id.et_search);

        init();

        return rootView;
    }

    private void fillItemList(String selectedString) {

        itemList.clear();

        SQLiteDB sqLiteDB = new SQLiteDB(getActivity());
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;
        if (!selectedString.isEmpty()) {
            selection = SQLiteDB.KEY_ITEM + " LIKE ?";
            selectionArgs = new String[]{"%"+selectedString+"%"};
        }

        Cursor cursor = database.query(SQLiteDB.TABLE_ITEM, null, selection, selectionArgs, null, null, SQLiteDB.KEY_ITEM);
        if (cursor.moveToFirst()) {

            int idIndex = cursor.getColumnIndex(SQLiteDB.KEY_ID);
            int nameIndex = cursor.getColumnIndex(SQLiteDB.KEY_ITEM);

            String id, name;

            do {
                id = cursor.getString(idIndex);
                name = cursor.getString(nameIndex);
                itemList.add(new String[]{id, name});

            } while (cursor.moveToNext());

        }

    }

    private void init() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        itemAdapter = new ItemRecyclerAdapter(getActivity(), itemList);
        recyclerView.setAdapter(itemAdapter);
        itemAdapter.setOnItemClickListener(position -> {
            currentPosition = position;
            openDialog(Integer.parseInt(itemList.get(position)[0]), itemList.get(position)[1], false);
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                currentPosition = viewHolder.getAdapterPosition();

                int rows = ItemInDocsOrDelete(itemList.get(currentPosition)[0], false);
                if (rows < 1) {
                    deleteItemFromList(currentPosition);
                } else {
                    openDialog(rows, itemList.get(currentPosition)[1], true);
                }

            }

        }).attachToRecyclerView(recyclerView);/*DELETE ROW ON SWIPE (to right)*/

        ibAddItem.setOnClickListener(v -> {
            currentPosition = -1;
            openDialog(-1, "", false);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before > count && s.length() < 2) {
                    fillItemList("");
                    itemAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().trim().isEmpty()) {
                    s.clear();
                }

                String str = s.toString();
                if (str.trim().length() > 1) {
                    fillItemList(str);
                    itemAdapter.notifyDataSetChanged();
                }

            }
        });

    }

    private int ItemInDocsOrDelete(String idDelete, boolean deleteRows) {

        int rows = 0;

        SQLiteDB sqLiteDB = new SQLiteDB(getActivity());
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        if (!deleteRows) {/*just to show dialog*/
            Cursor cursor = database.query(SQLiteDB.TABLE_DOC, null, SQLiteDB.KEY_ITEM_ID+"="+idDelete,null, null, null, null, null);
            rows = cursor.getCount();

        } else {/*delete if clicked DELETE ANYWAY*/

            rows = database.delete(SQLiteDB.TABLE_DOC, SQLiteDB.KEY_ITEM_ID+"="+idDelete, null);
            if (rows < 1) {
                Toast.makeText(getActivity(), "Couldn't save your changes, please try again", Toast.LENGTH_SHORT).show();
            }

        }

        sqLiteDB.close();

        return rows;

    }

    public void openDialog(int id, String name, boolean deleteItem) {
        ItemDialog itemDialog = new ItemDialog(id, name, deleteItem);
        itemDialog.setTargetFragment(ItemFragment.this, REQUEST_CODE_OPEN_DIALOG);
        itemDialog.show(getFragmentManager(), "item dialog");
    }

    @Override
    public void itemDeleting(boolean deleteItem) {

        if (deleteItem) {

            int rowsDeleted = ItemInDocsOrDelete(itemList.get(currentPosition)[0], true);
            if (rowsDeleted > 0) {
                deleteItemFromList(currentPosition);
            }

        } else {
            itemAdapter.notifyItemChanged(currentPosition);
        }
    }

    @Override
    public void sendItemIdName(int id, String name) {

        /*empty name*/
        if (name == null || name.isEmpty()) {
            Toast.makeText(getActivity(), "Item name cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (id != -1 && name.toLowerCase().equals(itemList.get(currentPosition)[1].toLowerCase())) {
            return;
        }

        /*check for the same name*/
        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase database = sqLiteDB.getWritableDatabase();
        String selection = "item LIKE ?";
        String[] selectionArgs = new String[]{name};
        Cursor cursor = database.query(SQLiteDB.TABLE_ITEM, null, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            sqLiteDB.close();
            Toast.makeText(getActivity(), "Item with the same name is already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        /*insert or update*/
        ContentValues cv = new ContentValues();
        cv.put(SQLiteDB.KEY_ITEM, name);

        if (id == -1) {/*new one*/

            long insertedId = database.insert(SQLiteDB.TABLE_ITEM, null, cv);

            if (insertedId != -1) {
                itemList.add(new String[] {String.valueOf(insertedId), name});
                itemAdapter.notifyItemInserted(itemList.size() - 1);
            } else {
                Toast.makeText(getActivity(), "Something went wrong, item is not added!", Toast.LENGTH_SHORT).show();
            }

        } else {/*edited*/

            int updatedRows = database.update(SQLiteDB.TABLE_ITEM, cv, "_id = " + id, null);

            if (updatedRows != 0) {
                itemList.set(currentPosition, new String[]{String.valueOf(id), name});
                itemAdapter.notifyItemChanged(currentPosition);
            } else {
                Toast.makeText(getActivity(), "Something went wrong, item is not updated!", Toast.LENGTH_SHORT).show();
            }

        }
        sqLiteDB.close();

    }

    private void deleteItemFromList(int position) {

        SQLiteDB sqLiteDB = new SQLiteDB(getActivity());
        SQLiteDatabase database = sqLiteDB.getWritableDatabase();
        database.delete(SQLiteDB.TABLE_ITEM, "_id = " + itemList.get(position)[0], null);

        itemList.remove(position);
        itemAdapter.notifyItemRemoved(position);
    }

}
