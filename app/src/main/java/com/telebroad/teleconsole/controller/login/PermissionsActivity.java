package com.telebroad.teleconsole.controller.login;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.USE_SIP;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.dashboard.DashboardActivity;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneContact;
import com.telebroad.teleconsole.model.db.PhoneContactDAO;

public class PermissionsActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (hasPermissions(READ_MEDIA_VIDEO,CAMERA, POST_NOTIFICATIONS,READ_CONTACTS, READ_PHONE_STATE, CALL_PHONE, RECORD_AUDIO, USE_SIP, Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? BLUETOOTH : BLUETOOTH_CONNECT)){
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            Utils.copyShareIntent(getIntent(), intent);
            this.startActivity(intent);
            return;
        }
        setContentView(R.layout.activity_permissions);
        Button askButton = findViewById(R.id.ask_button);
        askButton.setOnClickListener(v -> {
                requestPermissions(new String[]{READ_MEDIA_VIDEO,CAMERA,POST_NOTIFICATIONS,READ_CONTACTS, READ_PHONE_STATE, CALL_PHONE, RECORD_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, USE_SIP, Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? BLUETOOTH : BLUETOOTH_CONNECT}, 1);
            // ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS, READ_PHONE_STATE, CALL_PHONE, RECORD_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, 0);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        Utils.copyShareIntent(getIntent(), intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PhoneContact.loadPhoneContacts();
        // Get instance loads the contacts
        PhoneContactDAO.getInstance();
        this.startActivity(intent);
    }

    private boolean hasPermissions(String... permissions){

        for (String permission : permissions){
            if (!hasPermission(permission)){
                return false;
            }
        }
        return true;
    }
    private boolean hasPermission(String permission){
        return getPackageManager().checkPermission(permission, getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }


}
