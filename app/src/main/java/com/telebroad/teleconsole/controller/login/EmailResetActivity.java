package com.telebroad.teleconsole.controller.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import java.util.HashMap;
import java.util.Map;
import static com.android.volley.Request.Method.POST;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.INVALID_EMAIL_ERROR;
public class EmailResetActivity extends AppCompatActivity {
    private EditText emailEditText;
    private ProgressDialog progressDialog;
    private boolean shouldContinue = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_reset);
        Button resetButton = findViewById(R.id.resetButtton);
        emailEditText = findViewById(R.id.emailEditText);
        resetButton.setOnClickListener(v -> {
            sendEmail();
        });
        progressDialog = new ProgressDialog(this, R.style.DialogStyle);
    }

    private void sendEmail() {
        progressDialog.setMessage("Sending Email...");
        progressDialog.show();
        if (Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
            Map<String, String> params = new HashMap<>();
            params.put(URLHelper.KEY_EMAIL, emailEditText.getText().toString());
            params.put(URLHelper.KEY_TYPE, URLHelper.KEY_PASSWORD);
            URLHelper.request(POST, URLHelper.POST_FORGET_PASSWORD, params, false, false, result -> {
                runOnUiThread(this::handleSuccessfulEmail);
            }, this::handleError);
        } else {
            handleError(INVALID_EMAIL_ERROR);
        }
    }

    private void handleError(TeleConsoleError error) {
        // If the activity is in the middle of finishing, don't display the alert
        if (isFinishing()){
            return;
        }
        progressDialog.dismiss();
        AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.error_sending_email).setMessage(error.getFullErrorMessage()).setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
        }).setNeutralButton("Retry", (dialog, which) -> {
            sendEmail();
        }).create();
        alert.setOnShowListener(dialog -> {
            Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.black,null));
            negativeButton.setTextColor(getResources().getColor(R.color.black,null));
        });alert.show();
    }

    private void handleSuccessfulEmail() {
        // If the activity is in the middle of finishing, don't display the alert
        if (isFinishing()){
            return;
        }
        progressDialog.dismiss();
        AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.sent_email_title).setMessage(R.string.sent_email_message).setPositiveButton(R.string.sent_email_button, (dialog, which) -> {
            shouldContinue = false;
            Intent emailIntent = new Intent(Intent.ACTION_MAIN);
            emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(new Intent(this, ResetPasswordActivity.class));
            emailIntent = Intent.createChooser(emailIntent, "");
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            }else{
                AlertDialog alert1 = new MaterialAlertDialogBuilder(this).setTitle("No Email App")
                        .setMessage("No Email app available, please install one or allow one in your filter")
                        .setPositiveButton(android.R.string.ok, (dialog2, which2) -> {
                            try {
                                dialog.dismiss();
                            }catch (IllegalArgumentException iae){
                                Utils.logToFile(this, "on email dialog crash " + iae.getMessage());
                            }
                }).create();
                alert1.show();
            }
        }).setNeutralButton("Continue", (dialog, which) -> {
            dialog.dismiss();
        }).setOnDismissListener(dialog -> {
            if (shouldContinue) {
                startActivity(new Intent(this, ResetPasswordActivity.class));
            }
        }).show();
    }
}
