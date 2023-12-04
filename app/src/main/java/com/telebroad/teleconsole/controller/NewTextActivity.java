package com.telebroad.teleconsole.controller;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.Settings;
import com.telebroad.teleconsole.model.TeleConsoleProfile;
import com.telebroad.teleconsole.model.repositories.ContactRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_STREAM;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.helpers.Utils.showNumberChooser;
import static com.telebroad.teleconsole.helpers.Utils.updateLiveData;
import static com.telebroad.teleconsole.model.Line.convertLineListToStringList;

public class NewTextActivity extends AppCompatActivity {
    private EditText receiverText;
    private Uri imageUri = null;
    private MutableLiveData<String> myNumber;

    private void setReceiver(String receiver) {
        if (isNullOrEmpty(myNumber.getValue())) {
            receiverText.setText(receiver);
        } else {
            SmsConversationActivity.show(this, myNumber.getValue(), receiver, imageUri);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_text);
        if (getSupportActionBar() != null) {
           // getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.white)));
        }
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Intent intent = getIntent();
        if (intent != null && ACTION_SEND.equals(intent.getAction()) ){
//            android.util.Log.d("ShareIMG", " sharing image " + intent.getAction() + " type " + intent.getType() + " data " + intent.getParcelableExtra(EXTRA_STREAM));
            imageUri = intent.getParcelableExtra(EXTRA_STREAM);
            setTitle("Share image with...");
        }else{
           // android.util.Log.d("ShareIMG", " intent was null" );
        }
        receiverText = findViewById(R.id.receiverText);
        myNumber = new MutableLiveData<>();
        Settings.getLiveInstance().observe(this, settings -> {
            if (settings != null) {
                Utils.updateLiveData(myNumber, settings.getDefaultSMSLine());
            }
        });
        RecyclerView recyclerView = findViewById(R.id.matchedContacts);
        ContactRecyclerAdapter adapter = new ContactRecyclerAdapter();
        adapter.setOnContactSelected(contact -> {
            if (contact.getFullLines().isEmpty()) {
                return;
            }
            if (contact.getFullLines().size() == 1) {
                setReceiver(contact.getFullLines().get(0).formatted());
            } else {
                ContactPhoneChooserListDialog.getInstance(contact.getFullLines(), v -> setReceiver(v.formatted()))
                        .show(getSupportFragmentManager(), "choose email");
            }
        });
        ArrayAdapter<String> myNumberAdapter = new ArrayAdapter<>(this, R.layout.item_choose_sms, new ArrayList<>());
        TextView callerID = findViewById(R.id.callerID);
        callerID.setOnClickListener(v -> showNumberChooser(this, myNumber, myNumberAdapter,R.string.sms_callerid_chooser_title));
        myNumber.observe(this, number -> {
            if (!isNullOrEmpty(number)) {
                callerID.setText(getString(R.string.choose_outgoing_sms, PhoneNumber.format(number)));
            }
        });
        TeleConsoleProfile.getLiveInstance().observe(this, teleConsoleProfile -> {
            if (teleConsoleProfile == null){
                return;
            }
            if (teleConsoleProfile.getSmsLines().isEmpty()){
                showNoSMSDialog(this, true);
                return;
            }
            List<String> smsList = convertLineListToStringList(teleConsoleProfile.getSmsLines());
            myNumberAdapter.clear();
            myNumberAdapter.addAll(smsList);
            if (Settings.getInstance() == null){
                updateLiveData(myNumber, PhoneNumber.format(teleConsoleProfile.getSmsLines().get(0).getName()));
            }
        });
        Settings.getLiveInstance().observe(this, settings -> {
            updateLiveData(myNumber, PhoneNumber.format(settings.getDefaultSMSLine()));
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        ContactRepository contactRepository = ContactRepository.getInstance();
        receiverText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                //android.util.Log.d("NTCON01", "query " + s.toString());
                contactRepository.findContact(s.toString());
            }
        });
        receiverText.setOnEditorActionListener((v, actionId, event) -> {
            //android.util.Log.d("NTCON01", "done");
            PhoneNumber rec = PhoneNumber.getPhoneNumber(receiverText.getText().toString());
            if (rec.isText()){
                setReceiver(rec.formatted());
            }else {
                AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.short_number_error_title).setMessage(R.string.short_number_error_message)
                        .setNeutralButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).create();
                alert.setOnShowListener(dialog -> {
                    Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                    negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                });alert.show();
            }
            return false;
        });
        contactRepository.getMatchedContacts().observe(this, contacts -> {
            List<Contact> contactList = contacts.stream().filter(Contact::hasFullLines).collect(Collectors.toList());
            adapter.setContacts(contactList);
        });
        contactRepository.findContact("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
    public static void showNoSMSDialog(Activity activity){
        showNoSMSDialog(activity, false);
    }
    public static void showNoSMSDialog(Activity activity, boolean finishActivity) {
         new MaterialAlertDialogBuilder(activity).setTitle(R.string.no_sms_lines_title).setMessage(R.string.no_sms_lines_message).setPositiveButton(R.string.contact, ((dialog, which) -> {
            SipManager.getInstance().call(activity.getString(R.string.telebroad_support_number), activity);
            if (finishActivity) {
                activity.finish();
            }
        })).setCancelable(false).setNegativeButton(android.R.string.cancel, ((dialog, which) -> {
            if (finishActivity) {
                activity.finish();
            }
        })).show();
    }
}

