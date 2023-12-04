package com.telebroad.teleconsole.controller.dashboard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.ChooseContactActivity;
import com.telebroad.teleconsole.controller.NewContactActivity;


public class ContactSaveLocationDialog extends BottomSheetDialogFragment {

    private String defaultPhoneNumber;
    private boolean showExisting = false;
    private static final int CHOOSE_CONTACT = 800;
    private static final int ADD_PHONE_CONTACT = 801;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_contact_save_locations, null);
        View androidView = view.findViewById(R.id.androidLocationView);
        View telebroadView = view.findViewById(R.id.telebroadLocationView);
        Group existingGroup = view.findViewById(R.id.addToExisting);
        androidView.setOnClickListener( l -> {
            Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
            contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            if(defaultPhoneNumber != null){
                contactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, defaultPhoneNumber);
            }
            Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
            Activity activity = getActivity();
            if (activity != null){
                activity.startActivityForResult(contactIntent, ADD_PHONE_CONTACT, transitionBundle);
            }
            dismiss();
        });
        telebroadView.setOnClickListener( l -> {
            if (getContext() != null && getActivity() != null) {
                Intent newContactIntent = new Intent(getActivity(), NewContactActivity.class);
                if(defaultPhoneNumber != null){
                    newContactIntent.putExtra(NewContactActivity.EXTRA_PHONE_NUMBER, defaultPhoneNumber);
                }
                Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
                getActivity().startActivity(newContactIntent,  transitionBundle);
            }
            dismiss();
        });
        if (showExisting){
            existingGroup.setVisibility(View.VISIBLE);
        }else{
            existingGroup.setVisibility(View.GONE);
        }
        existingGroup.setOnClickListener(l -> {
            if (getActivity() != null){
                Intent addToExisting = new Intent(getActivity(), ChooseContactActivity.class);
                if(defaultPhoneNumber != null){
                    addToExisting.putExtra(NewContactActivity.EXTRA_PHONE_NUMBER, defaultPhoneNumber);
                }
//                getActivity().startActivity(addToExisting);
                this.startActivityForResult(addToExisting, CHOOSE_CONTACT);
//                dismiss();
            }
        });
        d.setContentView(view);
        return d;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //android.util.Log.d("ChooseContact01", "result in dialog!!");
        if (requestCode == CHOOSE_CONTACT) {
            if (data != null) {
                String type = data.getStringExtra(ChooseContactActivity.EXTRA_CHOOSE_CONTACT_TYPE);
                String id = data.getStringExtra(ChooseContactActivity.EXTRA_CHOOSE_CONTACT_ID);
                //android.util.Log.d("ChooseContact01.1", "save number " + defaultPhoneNumber + " id " + id + " type " + type);
                NewContactActivity.editContact(getActivity(), type, id, defaultPhoneNumber);
                //android.util.Log.d("ChooseContact01", "Type = " + type);
                if (type.equals("corporate")) {
                   // android.util.Log.d("ChooseContact01", "Retrying");
                    Intent addToExisting = new Intent(getActivity(), ChooseContactActivity.class);
                    startActivityForResult(addToExisting, 0);
                } else {
                    dismiss();
                }
            } else {
                dismiss();
            }
        }else if (requestCode == ADD_PHONE_CONTACT){
            if (data != null && data.getExtras() != null){
                for (String key : data.getExtras().keySet()){
                   // android.util.Log.d("EditContact02", "data get extras key " + key + " value: " + (data.getExtras().get(key) == null ? "null" : data.getExtras().get(key)));
                }
            }else{
                //android.util.Log.d("EditContact02.1", "data is null? " + (data == null) );
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getDefaultPhoneNumber() {
        return defaultPhoneNumber;
    }

    public void setDefaultPhoneNumber(String defaultPhoneNumber) {
        this.defaultPhoneNumber = defaultPhoneNumber;
    }
    public void setShowExisting(boolean showExisting) {
        this.showExisting = showExisting;
    }
}
