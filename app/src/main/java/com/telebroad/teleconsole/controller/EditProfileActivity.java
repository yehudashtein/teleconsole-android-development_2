package com.telebroad.teleconsole.controller;

import com.bumptech.glide.Glide;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.databinding.ActivityEditProfileBinding;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Settings;
import com.telebroad.teleconsole.model.TeleConsoleProfile;
import java.util.HashMap;
import static com.android.volley.Request.Method.PUT;
import static com.google.android.gms.common.util.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
public class EditProfileActivity extends AppCompatActivity {
    private boolean profileChanged;
    private boolean credentialsChanged;
    private Bitmap updatedProfile;
    private ActivityEditProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Drawable myFabSrc = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_add_photo,null);
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
        willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        binding.uploadButton.setImageDrawable(willBeWhite);
        TeleConsoleProfile.getLiveInstance().observe(this, profile -> {
            if (profile != null) {
                binding.firstName.setText(profile.getFirstname());
                binding.lastName.setText(profile.getLastname());
                binding.email.setText(profile.getEmail());
                binding.username.setText(profile.getUsername());
                String extension = "";
                if (Settings.getInstance() != null) {
                    extension = nullToEmpty(Settings.getInstance().getPhoneLine().getFcode());
                }
                if (extension.trim().isEmpty()) {
                    extension = "";
                } else {
                    extension = " Ext. " + extension;
                }
                if (profile.getPhoto() != null && !profile.getPhoto().isEmpty()){
                    Glide.with(this).load(profile.getPhoto()).circleCrop().into(binding.profileImage);
                }
                String fullName = profile.getFullName() + extension;
//                if (fullName.isEmpty()){
//                    fullName = getString(R.string.anonymous);
//                }
                binding.fullName.setText(fullName);
            }
        });

        binding.firstName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newFirstName = binding.firstName.getText() != null ? binding.firstName.getText().toString() : "";
                if (TeleConsoleProfile.getInstance() == null || !newFirstName.equals(TeleConsoleProfile.getInstance().getFirstname())) {
                    profileChanged = true;
                }
            }
        });
        binding.lastName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newLastName = binding.lastName.getText().toString();
                if (TeleConsoleProfile.getInstance() == null || !newLastName.equals(TeleConsoleProfile.getInstance().getLastname())) {
                    profileChanged = true;
                }
            }
        });
        binding.email.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newEmail = binding.email.getText().toString();
                if (TeleConsoleProfile.getInstance() == null || !newEmail.equals(TeleConsoleProfile.getInstance().getEmail())) {
                    profileChanged = true;
                }
            }
        });
        binding.username.setOnFocusChangeListener((v, hasFocus) -> {
            // If the user changed his username
            if (TeleConsoleProfile.getInstance() == null || !binding.username.getText().toString().equals(TeleConsoleProfile.getInstance().getUsername())) {
                credentialsChanged = true;
            }
        });
        binding.password.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.passwordLayout.setHint(getString(R.string.password_req));
                binding.passwordLayout.setErrorEnabled(false);
            } else {
                binding.passwordLayout.setHint(getString(R.string.password));
                if (binding.password.getText().length() < 8 && binding.password.getText().length() > 0) {
                    binding.passwordLayout.setErrorEnabled(true);
                    binding.passwordLayout.setError(getString(R.string.password_short_error));
                }
            }
        });
        binding.confirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            binding.confirmPassword.clearFocus();
            //android.util.Log.d("EditProfile", "gained focus? " + binding.fullName.requestFocus());
//            confirmPassword.clearFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(binding.confirmPassword.getRootView().getWindowToken(), 0);
            }
            return false;
        });
        binding.confirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.confirmPasswordLayout.setErrorEnabled(false);
            } else {
                if (!binding.password.getText().toString().equals(binding.confirmPassword.getText().toString())) {
                    binding.confirmPasswordLayout.setErrorEnabled(true);
                    binding.confirmPasswordLayout.setError(getString(R.string.password_match_error));
                } else {
                    credentialsChanged = true;
                }
            }
        });
        binding.uploadButton.setOnClickListener(v -> {
            PhotoSourceDialog dialog = new PhotoSourceDialog(uri -> {
                Glide.with(this).load(uri).circleCrop().into(binding.profileImage);
                updatedProfile = Utils.getBitmapFromURI(getContentResolver(), uri);
            });
            dialog.show(getSupportFragmentManager(), "editProfilePictureChooser");
        });
        binding.save.setOnClickListener(v -> {
            save();
        });
    }

    @Override
    public void onBackPressed() {
        if (credentialsChanged || profileChanged || updatedProfile != null){
            AlertDialog alert = new MaterialAlertDialogBuilder(this).
                    setTitle("Discard changes?").
                    setMessage("You made some changes, Do you want to save them before leaving the page?").
                    setPositiveButton("Save", (dialog, which) -> {
                        save();
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
        }else {
            super.finish();
        }
    }

    private void save() {
        if (credentialsChanged) {
            TeleConsoleProfile.getInstance().updateCredentials(binding.username.getText().toString(), emptyToNull(binding.password.getText().toString()), result -> {
                //TODO display confirmation alert
               // android.util.Log.d("EditProfile", "Success updating credentials");
                SettingsHelper.updateCredentials(TeleConsoleProfile.getInstance().getUsername(), binding.password.getText().toString());
                super.finish();
            }, error -> {
                // TODO display error alert
               // android.util.Log.d("EditProfile", "Error updating credentials " + error.getFullErrorMessage());
            });
        }
        if (profileChanged) {
            TeleConsoleProfile.getInstance().updateProfile(binding.firstName.getText().toString(), binding.lastName.getText().toString(), binding.email.getText().toString(), result -> {
                //TODO display confirmation alert
                //android.util.Log.d("EditProfile", "Success updating profile");
                super.finish();
            }, error -> {
                // TODO display error alert
                //android.util.Log.d("EditProfile", "Error updating profile " + error.getFullErrorMessage());
            });
        }
        if (updatedProfile != null){
            HashMap<String, String> params = new HashMap<>();
            Toast.makeText(this, "Uploading profile picture", Toast.LENGTH_LONG).show();
            params.put("image", Utils.getBase64FromBitmap(updatedProfile));
            URLHelper.request(PUT, URLHelper.GET_MYPHOTO_URL, params, result -> {
                //android.util.Log.d("MYPHOTO_RESPONSE", result.toString());
                TeleConsoleProfile.fetchProfile((profile) -> {});
                } , error -> Utils.logToFile("Error uploading profile pic " + error.getFullErrorMessage())) ;
            super.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
