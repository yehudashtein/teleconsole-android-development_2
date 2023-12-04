package com.telebroad.teleconsole.controller;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.TeleConsoleContact;

import java.util.List;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.ViewHolder> {
    private List<? extends Contact> contacts;
    private OnBottomReached onBottomReached;
    private OnContactSelected onContactSelected;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
       // android.util.Log.d("Contacts Holder", "Creating ");
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (contacts != null) {
            Contact current = contacts.get(position);
            holder.nameTextView.setText(current.getWholeName());
            holder.numberTextView.setText(current.getAllLines().isEmpty() ? "" : current.getAllLines().get(0).formatted());
            holder.typeTextView.setText(current.getType());
            if (onContactSelected != null) {
                holder.itemView.setOnClickListener(ev -> onContactSelected.selected(current));
            }
            String initial = current.getWholeName().replaceAll("[^a-zA-Z0-9א-ת]", "").trim();
            if (initial.isEmpty()) {
                initial = "#";
            } else {
                initial = initial.substring(0, 1).toUpperCase();
                String a = initial;
            }
            if (initial.isEmpty() || initial.equals("+") || Character.isDigit(initial.charAt(0))) {
                initial = "#";
            }
            int color = R.color.phoneContact;
            switch (current.getType()) {
                case "corporate":
                    TeleConsoleContact teleConsoleContact = (TeleConsoleContact) current;
                    String ext = teleConsoleContact.getExtension();
                    holder.typeTextView.setText(R.string.company);
                    holder.numberTextView.setText((ext == null || ext.isEmpty()) ? teleConsoleContact.getPbxLine() : ext);
                    color = R.color.corporateContact;
                    break;
                case "personal":
                    holder.typeTextView.setText(R.string.teleconsole);
                    color = R.color.personalContact;
                    break;
                case "mobile":
                    color = R.color.phoneContact;
                    holder.typeTextView.setText(R.string.mobile);
                    break;
                default:
                    color = R.color.personalContact;
                    holder.typeTextView.setText("");
                    break;

            }
            color = ActivityCompat.getColor(holder.itemView.getContext().getApplicationContext(), color);
            TextDrawable textDrawable = TextDrawable.builder().buildRound(initial, color);
            holder.initialImageView.setImageDrawable(textDrawable);
            holder.initialImageView.setImageTintList(ColorStateList.valueOf(color));
//            holder.initialImageView.setImageTintList(new ColorStateList());
            if (onBottomReached != null) {
                onBottomReached.onBottomReached(position == contacts.size() - 1);
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.nameTextView.setText(R.string.no_contact);
        }
    }

    @Override
    public int getItemCount() {
        if (contacts != null) {
            return contacts.size();
        }
        return 0;
    }

    public void setContacts(List<? extends Contact> contacts) {
        //android.util.Log.d("Contacts88", "contacts set is null " + (contacts == null));
        if (contacts != null) {
            //android.util.Log.d("Contacts Frag", "Setting " + contacts.size() + " Contacts of size ");
            this.contacts = contacts;
//            android.util.Log.d("NTCON01", "Starting " + contacts.size());
//            AsyncTask.execute(() -> {
//                //Collections.sort(contacts);
//                //notifyDataSetChanged();
//            });
//            Collections.sort(contacts);
            //android.util.Log.d("NTCON01", "End");
            notifyDataSetChanged();
        }
    }

    public void setOnBottomReached(OnBottomReached onBottomReached) {
        this.onBottomReached = onBottomReached;
    }

    public void setOnContactSelected(OnContactSelected onContactSelected) {
        this.onContactSelected = onContactSelected;
    }

    public static class ContactRecyclerFragment extends Fragment {
        OnContactSelected onContactSelected;
        public static ContactRecyclerFragment newInstance(OnContactSelected onContactSelected) {
            ContactRecyclerFragment fragment = new ContactRecyclerFragment();
            fragment.onContactSelected = onContactSelected;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_item_list, container, false);
            // Set the adapter
            if (view instanceof RecyclerView) {
                Context context = view.getContext();
                RecyclerView recyclerView = (RecyclerView) view;
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                ContactRecyclerAdapter adapter = new ContactRecyclerAdapter();
                adapter.setOnContactSelected(onContactSelected);
                recyclerView.setAdapter(adapter);
            }
            return view;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView numberTextView;
        private final ImageView initialImageView;
        private final TextView typeTextView;
        private final View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contactName);
            numberTextView = itemView.findViewById(R.id.contactNumber);
            initialImageView = itemView.findViewById(R.id.contactInitial);
            typeTextView = itemView.findViewById(R.id.contactType);
            this.itemView = itemView;
        }
    }

    @FunctionalInterface
    public interface OnBottomReached {
        void onBottomReached(boolean bottomReached);
    }

    @FunctionalInterface
    public interface OnContactSelected {
        void selected(Contact contact);
    }
}
