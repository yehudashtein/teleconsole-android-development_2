package com.telebroad.teleconsole.controller.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.telebroad.teleconsole.BuildConfig;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.DashboardActivity;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.TeleConsoleProfile;


import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.USE_SIP;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.MALFORMED_ERROR;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // android.util.Log.d("startup", "setting content");
        setContentView(com.telebroad.teleconsole.R.layout.activity_main);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        String username = SettingsHelper.getString(SettingsHelper.TELEBROAD_USERNAME);
        if (username != null) {
            FirebaseCrashlytics.getInstance().setCustomKey("username", username);
            TeleConsoleProfile.signIn((error) -> {
               // android.util.Log.d("LOGIN", "error " + (error instanceof TeleConsoleError.ServerError && error != MALFORMED_ERROR));
                if (error instanceof TeleConsoleError.ServerError && error != MALFORMED_ERROR) {

                   // android.util.Log.d("LOGIN", "In error handler");
                    startLoginActivity();
                    return;
                }
                URLHelper.requestJWTAuth(result -> {
                    SettingsHelper.putString(SettingsHelper.JWT_TOKEN, result.getAsString());
                    Utils.logToFile("Updated JWT");
                } , teleConsoleError -> {
                    if (teleConsoleError != null) {
                        Utils.logToFile("error updating JWT " + teleConsoleError.getFullErrorMessage());
                    }
                });
//                finish();
//                else{
//
//                }
            });
            // If the username is not null, don't wait to confirm if the password is correct, just
            // start the dashboard activity and let the default error handler take care of the redirect
            boolean hasPermission = AppController.getInstance().hasPermissions(USE_SIP );
            boolean shouldRequestBTPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? BLUETOOTH : BLUETOOTH_CONNECT);
            ActivityCompat.requestPermissions(this, new String[]{BLUETOOTH}, 0);;
            Intent intent = new Intent(getApplicationContext(), hasPermission && !shouldRequestBTPermission ? DashboardActivity.class : PermissionsActivity.class);
            this.startActivity(intent);
            finish();
        } else {
            startLoginActivity();
        }
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        boolean crashed = SettingsHelper.getBoolean(SettingsHelper.DID_CRASH, false);
        if (crashed){
            URLHelper.uploadLogs(Utils.getLogFile(getApplicationContext()), "Crashlytics_");
        }
    }

    private void startLoginActivity() {
       // android.util.Log.d("LOGIN", "starting login activity");
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        AppController.getInstance().getCurrentActivity().startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
