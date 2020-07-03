package com.example.management;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ItemDialog extends DialogFragment {

    private int id;
    private String name;
    private boolean deleteItem;

    private TextView tv_id;
    private EditText et_name;
    private Button bSave, bCancel;/*bSave <-> can be as Delete anyway*/

    public interface OnSaveClicked {
        void itemDeleting(boolean deleteItem);
        void sendItemIdName(int id, String name);
    }
    public OnSaveClicked mOnSaveClicked;

    public ItemDialog(int id, String name, boolean deleteItem) {
        this.id = id;
        this.name = name;
        this.deleteItem = deleteItem;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view;

        if (deleteItem) {

            view = inflater.inflate(R.layout.item_delete_dialog, container, false);

            tv_id = view.findViewById(R.id.tv_information);
            tv_id.setText("Item: "+name+" is exists in "+id+" document(s). This item will be deleted from the documents. Delete anyway?");

            bSave = view.findViewById(R.id.b_delete_anyway);
            bCancel = view.findViewById(R.id.b_cancel_del_any);
            bSave.setOnClickListener(v -> {
                mOnSaveClicked.itemDeleting(true);
                getDialog().dismiss();
            });
            bCancel.setOnClickListener(v -> {
                mOnSaveClicked.itemDeleting(false);
                getDialog().dismiss();
            });

        } else {

            view = inflater.inflate(R.layout.item_dialog, container, false);

            tv_id = view.findViewById(R.id.tv_item_id);
            et_name = view.findViewById(R.id.et_item_name);
            tv_id.setText(id == -1 ? "-" : String.valueOf(id));
            et_name.setText(name);

            bSave = view.findViewById(R.id.b_save);
            bCancel = view.findViewById(R.id.b_cancel);
            bSave.setOnClickListener(v -> {
                String itemName = et_name.getText().toString().trim();
                mOnSaveClicked.sendItemIdName(id, itemName);
                getDialog().dismiss();
            });
            bCancel.setOnClickListener(v -> {
                getDialog().dismiss();
            });
        }

        return view;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mOnSaveClicked = (OnSaveClicked) getTargetFragment();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

    }

}
