package com.example.management;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ItemFragment extends Fragment {

    private RecyclerView recyclerView;

    public ArrayList<String[]> itemList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fillItemList();
        View rootView = inflater.inflate(R.layout.doc_recycler_view, container, false);
        recyclerView = rootView.findViewById(R.id.rv_documents);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        DocRecyclerAdapter docAdapter = new DocRecyclerAdapter(getActivity(), docList);
        recyclerView.setAdapter(docAdapter);
        recViewOnClickCommands();

        return rootView;
    }

    private void fillItemList() {

        itemList = new ArrayList<>();
        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        String tableQuery = "documents as DOC inner join items as IT on DOC.item = IT._id";
        String[] columns = {"number as Number", "date as Date", "IT.item as Item", "count as Count", "time as Time"};
        Cursor cursor = database.query(tableQuery, columns, null, null, null, null, "Number");

        if (cursor.moveToFirst()) {

            ArrayList<String[]> tableList = new ArrayList<>();

            int nIndex = cursor.getColumnIndex("Number");
            int dIndex = cursor.getColumnIndex("Date");
            int tIndex = cursor.getColumnIndex("Time");
            int iIndex = cursor.getColumnIndex("Item");
            int cIndex = cursor.getColumnIndex("Count");

            int number = cursor.getInt(nIndex);
            int date = cursor.getInt(dIndex);
            int time = cursor.getInt(tIndex);

            /* To identify the type of document*/
            int count = cursor.getInt(cIndex);
            boolean documentIn = (count > 0);

            do {
                if (number != cursor.getInt(nIndex)) {

                    docList.add(new Document(number, date, time, documentIn, tableList));

                    number = cursor.getInt(nIndex);
                    date = cursor.getInt(dIndex);
                    time = cursor.getInt(tIndex);

                    /* To identify the type of document*/
                    count = cursor.getInt(cIndex);
                    documentIn = (count > 0);

                    tableList = new ArrayList<>();
                }

                String[] tableItem = new String[3];
                tableItem[0] = cursor.getString(iIndex);
                tableItem[1] = String.valueOf(documentIn ? cursor.getInt(cIndex) : -1 * cursor.getInt(cIndex));
                tableItem[2] = "";/* for id to fill on DocItemActivity -> onClickSaveDocument*/
                tableList.add(tableItem);

            } while (cursor.moveToNext());

            docList.add(new Document(number, date, time, documentIn, tableList));// last one

        }

        cursor.close();
        sqLiteDB.close();

    }

    private void recViewOnClickCommands() {

        recyclerView.addOnItemTouchListener(
                new DocRecyclerItemClickListener(getActivity(), new DocRecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                        Intent intent = new Intent(getContext(), DocItemActivity.class);

                        intent.putExtra(getString(R.string.intent_document), docList.get(position));
                        intent.putExtra(getString(R.string.intent_document_in), docList.get(position).getDocumentIn());
                        startActivity(intent);

                    }

                })
        );

    }

}
