package com.telebroad.teleconsole.controller;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.dashboard.ContactFragment;

public class ChooseContactActivity extends AppCompatActivity {

    public static String EXTRA_CHOOSE_CONTACT_ID = "com.telebroad.teleconsole.ChooseContactActivity.contact.id";
    public static String EXTRA_CHOOSE_CONTACT_TYPE = "com.telebroad.teleconsole.ChooseContactActivity.contact.type";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);
        ContactFragment fragment = (ContactFragment) getSupportFragmentManager().getFragments().get(0);
        fragment.setAddContactFabVisibile(false);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            window.setStatusBarColor(Color.BLACK);
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
            fragment.setContactSelected(contact -> {
           // android.util.Log.d("ChooseContact01", "contact selected " + contact.getWholeName());
            Intent selectedContact = new Intent();
            selectedContact.putExtra(EXTRA_CHOOSE_CONTACT_ID, contact.getID());
            selectedContact.putExtra(EXTRA_CHOOSE_CONTACT_TYPE, contact.getType());
            setResult(RESULT_OK, selectedContact);
            finish();
        });

//        android.util.Log.d("ChooseContact01", fragment.getClass().getName());
    }
}
