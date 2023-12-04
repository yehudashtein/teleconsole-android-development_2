package com.telebroad.teleconsole.controller.login;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.DashboardActivity;
import com.telebroad.teleconsole.databinding.ActivityLoginBinding;
import com.telebroad.teleconsole.helpers.AlertsHelper;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.TeleConsoleProfile;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.USE_SIP;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.BLOCKED_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.DISABLED_USER_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.LOGIN_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.NO_PHONES_ERROR;

public class TelebroadLoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    static final String EXTRA_DEFAULT_PASSWORD = "com.telebroad.teleconsole.controller.login.LoginActivity.extra.default_password";
    private @Deprecated
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.forgotPassword.setOnClickListener((event) -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            startActivity(new Intent(this, EmailResetActivity.class));
        });
//        binding.signupButton.setOnClickListener( event -> {
//            Intent newAccountIntent = new Intent(ACTION_SENDTO);
//            newAccountIntent.setData(Uri.parse("mailto:"));
//            newAccountIntent.putExtra(EXTRA_EMAIL, new String[]{"sales@telebroad.com"});
//            newAccountIntent.putExtra(EXTRA_SUBJECT, "New Account");
//            newAccountIntent.putExtra(EXTRA_TEXT, " I would like to create a new Telebroad account for my office. Please send me a proposal, here are the details. \n \n " +
//                    "Internet Provider: \n" +
//                    "Internet Speed: \n" +
//                    "Workstations/Extensions: \n" +
//                    "Contact Number: \n");
//            if (newAccountIntent.resolveActivity(getPackageManager()) == null){
//                Toast.makeText(this, "You don't have any apps installed that can send emails. Please install one from your app store", Toast.LENGTH_LONG).show();
//            }else {
//                startActivity(newAccountIntent);
//            }
//        });
        String defaultUsername = AppController.getInstance().getString(R.string.default_username);
        String defaultPassword = AppController.getInstance().getString(R.string.default_password);
        binding.usernameText.setText(defaultUsername);
        binding.passwordText.setText(defaultPassword);
        binding.passwordText.setOnEditorActionListener((v, actionId, event) -> {
            //android.util.Log.d("ACTIONID", actionId + "");
            signIn();
            return false;
        });
        String newPassword = getIntent().getStringExtra(EXTRA_DEFAULT_PASSWORD);
        binding.passwordText.setText(newPassword);
        binding.signinButton.setOnClickListener((event) -> signIn());
    }

    private void signIn() {
        SettingsHelper.updateCredentials(binding.usernameText.getText().toString(), binding.passwordText.getText().toString());
        progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        progressDialog.setMessage("Logging In");
        progressDialog.show();
        URLHelper.requestJWTAuth(result -> {
            SettingsHelper.putString(SettingsHelper.JWT_TOKEN, result.getAsString());
            //android.util.Log.d("JWT", "return is " + SettingsHelper.getString(SettingsHelper.JWT_TOKEN));
            TeleConsoleProfile.signIn((error -> {
               // android.util.Log.d("JWT", "profile signin error is " + error);
                if (error != null) {
                    progressDialog.dismiss();
                    // Uh-oh there is an error
                    if (error == LOGIN_ERROR) {
                        Utils.logToFile(error.getFullErrorMessage());
                        runOnUiThread(() -> {
                            Snackbar snackbar = AlertsHelper.getLongSnack(this, R.string.login_error);
                            binding.passwordText.setText("");
                            snackbar.setAction(android.R.string.ok, (snackEvent) -> {});
                            snackbar.show();
                        });
                    } else if (error == BLOCKED_ERROR) {
                        Utils.logToFile(error.getFullErrorMessage());
                        runOnUiThread(() -> URLHelper.showBlockedError(this));
                    } else if (error == NO_PHONES_ERROR) {
                        Utils.logToFile(error.getFullErrorMessage());
                        showErrorDialogWithContactBtn(R.string.no_phones, error);
                    } else if (error == DISABLED_USER_ERROR) {
                        Utils.logToFile(error.getFullErrorMessage());
                        showErrorDialogWithContactBtn(R.string.disabled_user, error);
                    } else {
                        runOnUiThread(() -> {
                            if (isFinishing()){
                                return;
                            }
                            AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.loginError)
                                    .setMessage(error.getFullErrorMessage()).setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                try {
                                    dialog.dismiss();
                                } catch (IllegalArgumentException iae) {
                                    Utils.logToFile(this, "Detached window error " + iae.getMessage());
                                }
                            }).create();
                            alert.setOnShowListener(dialog -> {
                                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                            });alert.show();
                        });
                    }
                } else {
                    // There is no error, continue to next activity
                    Intent intent;
                    // Decide which activity to proceed to, if we have permissions, we can continue to the dashboard immediately.
                    if (hasPermissions(READ_CONTACTS, READ_PHONE_STATE, CALL_PHONE, RECORD_AUDIO, READ_EXTERNAL_STORAGE, USE_SIP)) {
                        intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    } else {
                        intent = new Intent(this, PermissionsActivity.class);
                    }
                    intent.setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                    Utils.copyShareIntent(getIntent(), intent);
                    if (progressDialog != null && progressDialog.isShowing()) {
                        try{
                            progressDialog.dismiss();
                        }catch (IllegalArgumentException iae){
                            Utils.logToFile(this, "Detached window error " + iae.getMessage());
                        }
                    }
                    startActivity(intent);
                }
            }));
        }, error -> {
            if (error != null) {
                progressDialog.dismiss();
                if (error == LOGIN_ERROR) {
                    runOnUiThread(() -> {
                        Snackbar snackbar = AlertsHelper.getLongSnack(this, R.string.login_error);
                        binding.passwordText.setText("");
                        snackbar.setAction(android.R.string.ok, (snackEvent) -> {
                        });
                        snackbar.show();
                    });
                } else if (error == BLOCKED_ERROR) {
                    runOnUiThread(() -> URLHelper.showBlockedError(this));
                } else {
                    runOnUiThread(() -> {
                        if (isFinishing()) {
                            return;
                        }
                        AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.loginError)
                                .setMessage(error.getFullErrorMessage()).setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            try {
                                dialog.dismiss();
                            } catch (IllegalArgumentException iae) {
                                Utils.logToFile(this, "Detached window error " + iae.getMessage());
                            }
                        }).create();
                        alert.setOnShowListener(dialog -> {
                            Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                            negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                        });alert.show();
                    });
                }
            }
        });
    }

    private void showErrorDialogWithContactBtn(@StringRes int title, TeleConsoleError error) {
        runOnUiThread(() -> {
            if(isFinishing()){
                return;
            }
            AlertDialog.Builder alertDialogBuilder = new MaterialAlertDialogBuilder(this).setTitle(title).setMessage(error.getFullErrorMessage())
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                alertDialogBuilder.setPositiveButton(R.string.contact_button_title, ((dialog, which) -> {
                    Utils.callSupport(this);
                    dialog.dismiss();
                }));
            }
            alertDialogBuilder.create().setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alertDialogBuilder.show();
        });
    }

    private boolean hasPermissions(String... permissions) {

        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission(String permission) {
        return getPackageManager().checkPermission(permission, getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

}
