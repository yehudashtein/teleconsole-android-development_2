package com.telebroad.teleconsole.controller;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.telebroad.teleconsole.model.PhoneNumber;
import java.util.List;

public class ContactPhoneChooserListDialog extends ListBottomSheetDialog<PhoneNumber> {

    private ResultHandler onClickListener;

    public static ContactPhoneChooserListDialog getInstance(List<PhoneNumber> items, ResultHandler onClickListener){
       // android.util.Log.d("0020", "getInstance items " + items);
        ContactPhoneChooserListDialog instance = new ContactPhoneChooserListDialog();
        instance.items = items;
        instance.onClickListener = onClickListener;
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return new ContactPhoneChooserAdapter();
    }

    class ContactPhoneChooserAdapter extends RecyclerView.Adapter<ContactPhoneChooserAdapter.ContactPhoneNumberViewHolder> {

        @NonNull
        @Override
        public ContactPhoneNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ContactPhoneNumberViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false), onClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactPhoneNumberViewHolder holder, int position) {
            PhoneNumber number = items.get(position);
            holder.textView.setText(number.formatted() + " - " + number.getPhoneType().formattedName());
            holder.phoneNumber = number;
        }

        @Override
        public int getItemCount() {
            //android.util.Log.d("0020", "getItemCount " + items.size());
            return items.size();
        }

        class ContactPhoneNumberViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;
            private PhoneNumber phoneNumber;
            public ContactPhoneNumberViewHolder(View itemView, ResultHandler onClickListener) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
                itemView.setOnClickListener(v -> {
                    dismiss();
                    if (onClickListener != null){
                        onClickListener.handleResult(phoneNumber);
                    }
                });
            }
        }
    }

    @FunctionalInterface
    public interface ResultHandler{
        void handleResult(PhoneNumber pn) ;
    }
}
