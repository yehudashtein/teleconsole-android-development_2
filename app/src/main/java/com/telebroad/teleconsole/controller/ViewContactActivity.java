package com.telebroad.teleconsole.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.databinding.ActivityViewContactBinding;
import com.telebroad.teleconsole.databinding.ItemMessageBinding;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.PhoneContact;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.TeleConsoleContact;
import com.telebroad.teleconsole.model.db.PhoneContactDAO;
import com.telebroad.teleconsole.model.repositories.CallHistoryRepository;
import com.telebroad.teleconsole.model.repositories.ContactRepository;
import com.telebroad.teleconsole.viewmodels.CallHistoryViewModel;
import com.telebroad.teleconsole.viewmodels.ViewContactViewModel;

import java.util.List;

public class ViewContactActivity extends AppCompatActivity {
    private   ViewContactViewModel contactViewModel;
    private ViewPhoneAdapter phoneAdapter;
    private ViewEmailAdapter emailAdapter;
    private CallHistoryAdapter callHistoryAdapter;
    public static final String EXTRA_VIEW_CONTACT_ID = "com.telebroad.teleconsole.ViewContactActivity.contact.id";
    public static final String EXTRA_VIEW_CONTACT_TYPE = "com.telebroad.teleconsole.ViewContactActivity.contact.type";
    public static final String EXTRA_CONTACT_DELETED = "com.telebroad.teleconsole.ViewContactActivity.contact.status";
    private static final int EDIT_PHONE_CONTACT = 701;
    private static final int EDIT_TELEBROAD_CONTACT = 702;
    private String lastPhoneContactLookup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactViewModel = ViewModelProviders.of(this).get(ViewContactViewModel.class);
        ActivityViewContactBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_view_contact);
        phoneAdapter = new ViewPhoneAdapter(contactViewModel.getItem());
        emailAdapter = new ViewEmailAdapter(contactViewModel.getItem());
        callHistoryAdapter = new CallHistoryAdapter(contactViewModel.getItem());
        binding.viewPhoneRecycler.setAdapter(phoneAdapter);
        binding.viewEmailRecycler.setAdapter(emailAdapter);
        binding.callLogRecycler.setAdapter(callHistoryAdapter);
        if (getIntent() != null){
            String id = getIntent().getStringExtra(EXTRA_VIEW_CONTACT_ID);
            String type = getIntent().getStringExtra(EXTRA_VIEW_CONTACT_TYPE);
           // android.util.Log.d("ContactType01", type + " id " + id);
            if (Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(type)) {
                finish();
                return;
            }
            switch (type){
                // TODO refactor to make use of new PhoneContactDAO. YAY!!!
                case "mobile":
                    LiveData<PhoneContact> liveContact = PhoneContactDAO.getInstance().getContactByLookup(id, this);
                    liveContact.observe(this, phoneContact -> {
                       // android.util.Log.d("PhoneContactsDAO01", "contact updated " + phoneContact);
                        if (phoneContact != null){
                            updateContact(binding, phoneContact);
                            binding.edit.setOnClickListener(v -> {
                                Intent intent = new Intent(Intent.ACTION_EDIT);
                                Uri uri = Uri.parse(ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/" + phoneContact.getID());
                                lastPhoneContactLookup = phoneContact.getID();
                                intent.setData(uri);
                                startActivityForResult(intent, EDIT_PHONE_CONTACT);
                            });
                        }
                    });
//                    Contact contact = ContactRepository.getInstance().getPhoneContactByID(id);
//                    android.util.Log.d("ViewContact02", "mobile contact id " + contact.getID());
//                    android.util.Log.d("ViewContact01", "contact " + contact);
//                    if (contact == null){
//                        Toast.makeText(this, "Unable to load contact", Toast.LENGTH_SHORT).show();
//                        finish();
//                        return;
//                    }
//                    updateContact(binding,contact);
//                    binding.edit.setOnClickListener(v -> {
//                        Intent intent = new Intent(Intent.ACTION_EDIT);
//                        Uri uri = Uri.parse(ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/" + contact.getID());
//                        lastPhoneContactLookup = contact.getID();
//                        intent.setData(uri);
//                        startActivityForResult(intent, EDIT_PHONE_CONTACT);
//                    });
                    break;
                case "corporate":
                case "personal":
                    if (type.equals("corporate")){
                        binding.edit.setAlpha(0.2f);
                    }
                    AsyncTask.execute(() -> {
                        String contactID = getIntent().getStringExtra(EXTRA_VIEW_CONTACT_ID);
                        runOnUiThread(() -> {
                            ContactRepository.getInstance().getLiveContactByID(contactID).observe(this, (contact2 -> {
                                runOnUiThread(() -> {
                                    if (contact2 == null) {
                                        //finish();
                                    }
                                    binding.edit.setOnClickListener(v -> {
                                        if (type.equals("corporate")) {
                                            Toast.makeText(this, R.string.change_company_contacts_toast, Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        Intent intent = new Intent(this, NewContactActivity.class);
                                        intent.putExtra(NewContactActivity.EXTRA_CONTACT_ID, id);
                                        startActivityForResult(intent, EDIT_TELEBROAD_CONTACT);
                                    });
                                    updateContact(binding, contact2);
                                });
                            }));
                        });
                    });
                    break;
                default:
                    break;
            }
        }
        setSupportActionBar(findViewById(R.id.new_contact_toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }
//        initializeViews();
//        emailRecycler.setAdapter(phoneAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case EDIT_PHONE_CONTACT:
                if (resultCode == 3){
                    PhoneContact.deleteContact(lastPhoneContactLookup);
                    finish();
                }
                if (data != null && data.getExtras() != null){
                    for (String key : data.getExtras().keySet()){
                        //android.util.Log.d("EditContact02", "data get extras key " + key + " value: " + (data.getExtras().get(key) == null ? "null" : data.getExtras().get(key)));
                    }
                }
                //android.util.Log.d("EditContact01", "data " + data + " result code " + resultCode  + " extras " + (data == null ? null : data.getExtras() == null ? null : data.getExtras().keySet()));
                break;
            case EDIT_TELEBROAD_CONTACT:
                boolean deleted = data != null && data.getBooleanExtra(EXTRA_CONTACT_DELETED, false);
                if (deleted){
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void updateContact(ActivityViewContactBinding binding, Contact contact) {
        contactViewModel.setItem(contact);
        binding.setContact(contactViewModel);
        binding.executePendingBindings();
        phoneAdapter.updateContact(contact);
        emailAdapter.updateContact(contact);
        callHistoryAdapter.updateContact(contact);
    }

    class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.ViewHolder>{
        Contact contact;
        List<CallHistory> callLogs;
        CallHistoryAdapter(Contact contact){
            this.contact = contact;
            if (contact != null){
                AsyncTask.execute(() -> callLogs = CallHistoryRepository.getInstance().getCallLogsForContact(contact));
            }
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMessageBinding binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);
            return new ViewHolder(binding);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (callLogs != null) {
                CallHistory current;
                current = callLogs.get(position);
                CallHistoryViewModel currentViewModel = new CallHistoryViewModel();
                currentViewModel.setItem(current);
                holder.bind(currentViewModel);
            }
        }

        @Override
        public int getItemCount() {
            return callLogs == null ? 0 : callLogs.size();
        }

        void updateContact(Contact contact) {
            this.contact = contact;
            if (contact == null){
                return;
            }
            AsyncTask.execute(() -> {
                callLogs = CallHistoryRepository.getInstance().getCallLogsForContact(contact);
                runOnUiThread(this::notifyDataSetChanged);
            });
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            private final ItemMessageBinding binding;
            private CallHistoryViewModel item;

            ViewHolder(ItemMessageBinding binding) {
                super(binding.getRoot());
//                binding.setLifecycleOwner(getViewLifecycleOwner());
                this.binding = binding;
            }

            void bind(CallHistoryViewModel model){
                this.item = model;
                binding.setViewmodel(model);
                binding.executePendingBindings();
            }
        }
    }

    class ViewPhoneAdapter extends RecyclerView.Adapter<ViewPhoneAdapter.ViewHolder>{
        private Contact contact;

        ViewPhoneAdapter(Contact contact) {
            this.contact = contact;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(AppController.getInstance());
            return new ViewHolder(inflater.inflate(R.layout.item_view_contact_phone, parent, false));
        }

        void updateContact(Contact contact){
            this.contact = contact;
            notifyDataSetChanged();
        }
        @Override
        public int getItemCount() {
            return contact == null ? 0 : contact.getAllLines().size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.setNumber(contact.getAllLines().get(position));
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView phoneView;
            private final TextView typeView;
            private final ImageView textButton;
            private  PhoneNumber number;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                phoneView = itemView.findViewById(R.id.phoneNumberView);
                typeView = itemView.findViewById(R.id.typeView);
                ImageView callButton = itemView.findViewById(R.id.callButton);
                callButton.setOnClickListener(this);
                textButton = itemView.findViewById(R.id.textButton);
                textButton.setOnClickListener(this);
            }

            void setNumber(PhoneNumber number){
                if (number == null){
                    return;
                }
                if (number.isShort()){
                    textButton.setVisibility(View.GONE);
                }else{
                    textButton.setVisibility(View.VISIBLE);
                }
                this.number = number;
                phoneView.setText(number.formatted());
                if (number.getPhoneType() != null) {
//                    android.util.Log.d("ViewContact02", "Number is " + number.toString());
                    typeView.setText(number.getPhoneType().formattedName());
                }else{
                    typeView.setText(R.string.other);
//                    android.util.Log.d("ViewContact02", "Type for email " + number + " is null");
                }
            }

            @Override
            public void onClick(View v) {
                //android.util.Log.d("Contact14", "clicked view clicked " + v.getId() + " text button id " + R.id.textButton);
                switch (v.getId()){
                    case R.id.callButton:
                        SipManager.getInstance().call(number.fixed());
                        break;
                    case R.id.textButton:
                        SmsConversationActivity.show(ViewContactActivity.this, number);
                        break;
                }
            }
        }
    }

    class ViewEmailAdapter extends RecyclerView.Adapter<ViewEmailAdapter.ViewHolder>{

        private Contact contact;

        ViewEmailAdapter(Contact contact) {
            this.contact = contact;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(AppController.getInstance());
            return new ViewHolder(inflater.inflate(R.layout.item_view_contact_email, parent, false));
        }
        void updateContact(Contact contact){
            this.contact = contact;
            notifyDataSetChanged();
        }
        @Override
        public int getItemCount() {
            return contact == null ? 0 : contact.getEmailAddresses().size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.setEmail(contact.getEmailAddresses().get(position));
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView emailView;
            String email;
            int position;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                emailView = itemView.findViewById(R.id.emailView);
                itemView.findViewById(R.id.emailButton).setOnClickListener(view -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + email));
                    if (emailIntent.resolveActivity(getPackageManager()) != null){
                        startActivity(emailIntent);
                    }else{
                        Toast.makeText(ViewContactActivity.this, "You don't have an email app, Please install a Email app", Toast.LENGTH_LONG).show();
                    }
                });
            }

            void setEmail(String email){
                this.email = email;
                emailView.setText(email);
            }
        }
    }

    public static void getContactByID(String id, String type, Consumer<Contact> contactConsumer){
        if (type.equals("mobile")){
            contactConsumer.accept(PhoneContact.getContactByID(id));
        }else{
            getTBContactByID(id, contactConsumer);
        }
    }
    public static void getTBContactByID(String id, Consumer<Contact> contactConsumer){
        AsyncTask.execute(() -> {
            TeleConsoleContact contact = ContactRepository.getInstance().getContactByID(id);
            contactConsumer.accept(contact);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}



