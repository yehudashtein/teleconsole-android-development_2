package com.telebroad.teleconsole.controller.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.base.Strings;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import java.util.HashMap;
import java.util.Map;
import static com.android.volley.Request.Method.POST;
import static com.telebroad.teleconsole.controller.login.TelebroadLoginActivity.EXTRA_DEFAULT_PASSWORD;

public class ResetPasswordActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private EditText resetCode;
    private EditText password;
    private EditText confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //android.util.Log.d("ResetPassword", "started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        resetCode = findViewById(R.id.emailEditText);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        password.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                password.setError(null);
            }else if (password.getText().length() < 8 && password.getText().length() > 0){
                password.setError(getString(R.string.password_short_error));
            }
        });
        confirmPassword.setOnFocusChangeListener((v, focus) -> {
            if (focus){
                password.setError(null);
            }else if (!password.getText().toString().equals(confirmPassword.getText().toString())){
                confirmPassword.setError(getString(R.string.password_match_error));
            }
        });
        handleIntentUri(getIntent());
        progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        Button reset = findViewById(R.id.signinButton);
        reset.setOnClickListener(v -> {
            if (password.getText().length() < 8){
                // Display the password to small error
                new AlertDialog.Builder(this,R.style.DialogStyle).setTitle(R.string.password_short_error).
                        setMessage("Passwords should be at least 8 characters long").
                        setPositiveButton(android.R.string.ok, ((dialog, which) -> dialog.dismiss())).create().show();
                return;
            }else if (!confirmPassword.getText().toString().equals(password.getText().toString())){
                new AlertDialog.Builder(this,R.style.DialogStyle).setTitle(R.string.passwords_dont_match_title).
                        setPositiveButton(android.R.string.ok, ((dialog, which) -> dialog.dismiss())).create().show();
                // Display password doesn't match error
                return;
            }
            progressDialog.setMessage("Resetting Password...");
            progressDialog.show();
            Map<String, String> params = new HashMap<>();
            params.put(URLHelper.KEY_CODE, resetCode.getText().toString());
            params.put(URLHelper.KEY_NEW_PASSWORD, password.getText().toString());
            URLHelper.request(POST, URLHelper.POST_RESET_PASSWORD, params, false, false,  result -> handleSuccessfulReset(), this::handleError);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntentUri(intent);
        super.onNewIntent(intent);
    }

    private void handleIntentUri(Intent intent) {
        Uri appLinkData = intent.getData();
        if (appLinkData != null) {
            String code = appLinkData.getFragment();
            code = Strings.nullToEmpty(code);
            code = code.replace("/", "");
            resetCode.setText(code);
            resetCode.clearFocus();
            password.requestFocus();
        }
    }

    private void handleError(TeleConsoleError error){
        runOnUiThread(() -> {
        if (isFinishing()){
            return;
        }
        progressDialog.dismiss();
            AlertDialog alert = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.error_reset_password)
                .setMessage(error.getFullErrorMessage())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .create();
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();
        });
    }
    private void handleSuccessfulReset() {
        progressDialog.dismiss();
        Intent intent = new Intent(this, TelebroadLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_DEFAULT_PASSWORD, password.getText().toString());
        startActivity(intent);
    }
}
