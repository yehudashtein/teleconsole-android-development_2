package com.telebroad.teleconsole.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.databinding.ActivityNewContactBinding;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.TeleConsoleContact;
import com.telebroad.teleconsole.model.repositories.ContactRepository;
import com.telebroad.teleconsole.viewmodels.NewContactViewModel;

import java.util.HashMap;
import java.util.List;

import static com.telebroad.teleconsole.controller.ViewContactActivity.EXTRA_CONTACT_DELETED;

public class NewContactActivity extends AppCompatActivity {

    public static String EXTRA_CONTACT_ID = "com.telebroad.teleconsole.controller.NewContactActivity.contact.id";
    public static String EXTRA_PHONE_NUMBER = "com.telebroad.teleconsole.controller.NewContactActivity.phone.number";
    private boolean wasEdited = false;
    private NewContactViewModel contactViewModel;
    private PhoneAdapter phoneAdapter;
    private EmailAdapter emailAdapter;
    private ActivityNewContactBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactViewModel = ViewModelProviders.of(this).get(NewContactViewModel.class);
        phoneAdapter = new PhoneAdapter(contactViewModel.getItem());
        emailAdapter = new EmailAdapter(contactViewModel.getItem());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_contact);
        binding.setContact(contactViewModel);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            window.setStatusBarColor(Color.BLACK);
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            window.setStatusBarColor(Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        String contactID = getIntent() == null ? null : getIntent().getStringExtra(EXTRA_CONTACT_ID);
        String telephoneNumber = getIntent() == null ? null : getIntent().getStringExtra(EXTRA_PHONE_NUMBER);
        //android.util.Log.d("ChooseContact01.1", "edit contact save number " + telephoneNumber + " id " +  contactID );
        if (telephoneNumber != null){
            contactViewModel.setNumberToAdd(new PhoneNumber(telephoneNumber, PhoneNumber.PhoneType.MOBILE));
        }
        if (contactID != null){
            TextView titleText = findViewById(R.id.contact_title);
            titleText.setText(R.string.edit_contact);
            binding.delete.setVisibility(View.VISIBLE);
            binding.delete.setOnClickListener(view -> {
                androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.delete_contact_title).setMessage(R.string.delete_contact_message)
                        .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
                            Toast.makeText(this, "Deleting Contact", Toast.LENGTH_SHORT).show();
                            HashMap<String, String> parmas = new HashMap<>();
                            parmas.put(URLHelper.KEY_ID, contactID);
                            URLHelper.request(Request.Method.DELETE, URLHelper.GET_CONTACT_URL,
                                    parmas,
                                    result -> {
                                        //Log.d("Contact13", "Contact deleted");
                                        wasEdited = false;
                                        AsyncTask.execute(() -> ContactRepository.getInstance().deleteContact(contactID));
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra(EXTRA_CONTACT_DELETED, true);
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    },
                                    error -> {
                                        //Log.d("Contact13", "Error is " + error.getFullErrorMessage());
                                        Toast.makeText(this, "Error deleting contact. Error Code " + error.getCode(), Toast.LENGTH_LONG).show();
                                    });

                        }).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
                alert.setOnShowListener(dialog -> {
                    Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                    negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                });alert.show();
            });
            AsyncTask.execute(() ->{
                TeleConsoleContact contact = ContactRepository.getInstance().getContactByID(contactID);
                runOnUiThread(() -> {
                   // android.util.Log.d("ChooseContact01.1", "contact id  " + contact.getID() + " work " +  contact.getWork() );
                    contactViewModel.setItem(contact);
                    binding.executePendingBindings();
                    phoneAdapter.updateContact(contact);
                    emailAdapter.updateContact(contact);
                });
            });
        }else {
            binding.delete.setVisibility(View.GONE);
        }
//        setContentView(R.layout.activity_new_contact);
        setSupportActionBar(findViewById(R.id.new_contact_toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_round_arrow_back);
        }
        binding.phoneRecycler.setAdapter(phoneAdapter);
        binding.emailRecycler.setAdapter(emailAdapter);
        binding.save.setOnClickListener( l -> {
            if (getCurrentFocus() != null) {
                getCurrentFocus().clearFocus();
            }
            Toast.makeText(this, "Saving Contact...", Toast.LENGTH_LONG).show();
            contactViewModel.save(this::getCompletionHandler);
        });
    }

    private void getCompletionHandler(TeleConsoleError error) {
        if (error == null){
            wasEdited = false;
            finish();
        }else{
            androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(this, R.style.DialogStyle).
                    setTitle("Error Saving Contact").
                    setMessage(error.getErrorMessage()).
                    setNeutralButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    }).create();
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();
        }
    }

    @Override
    public void finish() {
        if (wasEdited) {
            AlertDialog alert = new MaterialAlertDialogBuilder(this).
                    setTitle("Discard changes?").
                    setMessage("You made some changes, Do you want to save them before leaving the page?").
                    setPositiveButton("Save", (dialog, which) -> {
                        contactViewModel.save(this::getCompletionHandler);
                        dialog.dismiss();
                    }).
                    setNegativeButton("Discard", (dialog, which) -> super.finish()).
                    create();
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();
        }else{
            super.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.ViewHolder>{
        private TeleConsoleContact contact;
        PhoneAdapter(TeleConsoleContact contact){
            this.contact = contact;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(AppController.getInstance());
            return new ViewHolder(inflater.inflate(R.layout.item_phone_new_contact,parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            //android.util.Log.d("Numbers02", "Binding " + position );
            String[] items = getResources().getStringArray(R.array.phone_types);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewContactActivity.this, android.R.layout.simple_spinner_item, items) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                        ColorStateList colorStateList = ColorStateList.valueOf(Color.WHITE);
                        text.setTextColor(colorStateList);
                    }else {
                        ColorStateList colorStateList = ColorStateList.valueOf(Color.BLACK);
                        text.setTextColor(colorStateList);
                    }
                    // Set your color here
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.type.setAdapter(adapter);

            holder.type.setOnItemSelectedListener(null);
            if (position < contactViewModel.getNumbers().size()) {
                PhoneNumber number = contactViewModel.getNumbers().get(position);
                holder.phone.setText(number.formatted());
                holder.type.setSelection(number.getPhoneType().ordinal());
            }else{
                holder.phone.setText("");
//                holder.type.setSelection(0);
            }
            holder.phone.setOnEditorActionListener((v, actionId, event) -> {
                v.clearFocus();
                return true;
            });
            holder.phone.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus){
                    return;
                }
                List<PhoneNumber> numbers = contactViewModel.getNumbers();
                String str = holder.phone.getText().toString();
                if (str.isEmpty()){
                    if (position < contactViewModel.getNumbers().size()){
                        numbers.remove(position);
                        wasEdited = true;
                        notifyItemRemoved(position);
                    }
                }else{
                    if (position >= contactViewModel.getNumbers().size()){
                        wasEdited = true;
                        numbers.add(new PhoneNumber(str, PhoneNumber.PhoneType.values()[holder.type.getSelectedItemPosition()]));
                        notifyItemInserted(getItemCount() -1 );
//                        binding.phoneRecycler.post(() -> {
////                            View currentFocus = getCurrentFocus();
//
//                            android.util.Log.d("Numbers01", "Inserted" + (getItemCount() - 1));
//                            try {
//                                notifyItemInserted(getItemCount() - 1);
//                            }catch (Exception e){
//                                android.util.Log.e("Numbers01", "caught", e);
//                            }
////                            notifyDataSetChanged();
////                            if (currentFocus != null) currentFocus.findFocus();
//                        });
                    }else if (!contactViewModel.getNumbers().get(position).fixed().equals(PhoneNumber.fix(str))){
                        wasEdited = true;
                        //android.util.Log.d("Contact11", "was edited");
                        PhoneNumber number = contactViewModel.getNumbers().get(position);
                        number.setPhoneNumber(str);
//                        contactViewModel.getNumbers().remove(position);
                        //android.util.Log.d("Contact12", "numbers " + contactViewModel.getNumbers() + " current number " + contactViewModel.getNumbers().get(position) + " number " + number);
                    }
                }
            });
            holder.type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position1, long id) {
                   // android.util.Log.d("Contact11", "was selected");
                    if (position < contactViewModel.getNumbers().size()){
                        if (PhoneNumber.PhoneType.values()[holder.type.getSelectedItemPosition()] != contactViewModel.getNumbers().get(position).getPhoneType()){
                            wasEdited = true;
                            contactViewModel.getNumbers().get(position).setPhoneType(PhoneNumber.PhoneType.values()[holder.type.getSelectedItemPosition()]);
                            //android.util.Log.d("Contact11", "was edited");
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

           // android.util.Log.d("Numbers02", "Binding Finished " + position );
        }

        @Override
        public int getItemCount() {
            if (contact == null){
                return 1;
            }
            int size = contactViewModel.getNumbers().size() + 1;
            //android.util.Log.d("Numbers01", "size is " + size);
            return size;
        }

        private void updateContact(TeleConsoleContact newContact){
            this.contact = newContact;
            notifyDataSetChanged();
        }
        class ViewHolder extends RecyclerView.ViewHolder{
            EditText phone;
            Spinner type;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                phone = itemView.findViewById(R.id.firstPhoneTV);
                type = itemView.findViewById(R.id.firstPhoneSpinner);
            }
        }
    }

    public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.ViewHolder>{
        private TeleConsoleContact contact;
        EmailAdapter(TeleConsoleContact contact){
            this.contact = contact;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(AppController.getInstance());
            return new ViewHolder(inflater.inflate(R.layout.item_email_new,parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (contact != null && position < contact.getEmailAddresses().size() ) {
                String email = contact.getEmailAddresses().get(position);
                holder.email.setText( email);
            }else{
                holder.email.setText("");
            }
            holder.email.setOnEditorActionListener((v, actionId, event) -> {
                v.clearFocus();
                return true;
            });
            holder.email.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus){
                    return;
                }
                String str = holder.email.getText().toString();
                if (str.isEmpty()){
                    if (contact != null && position < contact.getEmailAddresses().size()){
                        contact.getEmailAddresses().remove(position);
                        wasEdited = true;
                        notifyItemRemoved(position);
                    }
                }else{
                    if (contact == null){
                        return;
                    }
                    if (position >= contact.getEmailAddresses().size()){
                        wasEdited = true;
                        contact.getEmailAddresses().add(str);
                        binding.emailRecycler.post(() -> {
                            View currentFocus = getCurrentFocus();
                            notifyItemInserted(getItemCount() - 1);
                            if (currentFocus != null) currentFocus.findFocus();
                        });
                    }else if (!contact.getEmailAddresses().get(position).equals(str)){
                        wasEdited = true;
                       // android.util.Log.d("Contact11", "was editied");
                        contact.getEmailAddresses().set(position, str);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            if (contact == null){
                return 1;
            }
            return contact.getEmailAddresses().size() + 1;
        }

        private void updateContact(TeleConsoleContact newContact){
            this.contact = newContact;
            notifyDataSetChanged();
        }
        class ViewHolder extends RecyclerView.ViewHolder{
            EditText email;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                email = itemView.findViewById(R.id.firstEmailTV);
            }
        }
    }

    public static void editContact(Activity activity, String type, String id, String phoneNumber){
        if (type.equals("mobile")){
            showPhoneEditActivity(activity, id, phoneNumber);
        }else{
            showEditActivity(activity, type, id, phoneNumber);
        }
    }

    public static void showPhoneEditActivity(Activity activity, String id) {
        showPhoneEditActivity(activity, id, null);
    }

    public static void showPhoneEditActivity(Activity activity, String id, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        if (phoneNumber != null ){
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        }
        Uri uri = Uri.parse(ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/" + id);
        intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        // Sets the special extended data for navigation
        intent.putExtra("finishActivityOnSaveCompleted", true);
        activity.startActivity(intent);
    }

    public static void showEditActivity(Activity activity, String type, String id) {
        showEditActivity(activity, type, id, null);
    }

    public static void showEditActivity(Activity activity, String type, String id, String phoneNumber) {
        if (type.equals("corporate")) {
            Toast.makeText(activity, R.string.change_company_contacts_toast, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(activity, NewContactActivity.class);
        intent.putExtra(NewContactActivity.EXTRA_CONTACT_ID, id);
        if(phoneNumber != null){
            intent.putExtra(NewContactActivity.EXTRA_PHONE_NUMBER, phoneNumber);
        }
        activity.startActivity(intent);
    }
}
