package com.telebroad.teleconsole.controller;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pdfview.PDFView;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.IntentHelper;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.Fax;
import com.telebroad.teleconsole.model.repositories.FaxRepository;
import com.telebroad.teleconsole.viewmodels.FaxViewModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY;
import static com.android.volley.Request.Method.GET;
import static com.telebroad.teleconsole.helpers.IntentHelper.MESSAGE_TIME;

public class FaxOpenActivity extends AppCompatActivity {
    private FaxViewModel faxViewModel;
    private TextView mailboxView, nameView, dateView;
    private byte[] faxData;
    private PDFView pdfView;
    private ProgressBar pdfProgress;
    private ImageView errorView;
    private int contactCount = 0;
    private List<? extends Contact> matchedContacts;
    private String downloadError = "Unknown Error";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fax_open);
        String id = getIntent().getExtras() == null ? "" : getIntent().getExtras().getString(IntentHelper.MESSAGE_ID, "");
        long time = getIntent().getExtras().getLong(MESSAGE_TIME, 0);
        if (id.isEmpty() && time  == 0){
            return;
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white, null)));
        }
        mailboxView = findViewById(R.id.mailbox);
        nameView = findViewById(R.id.nameView);
        dateView = findViewById(R.id.dateView);
        pdfView = findViewById(R.id.pdfView);
        pdfProgress = findViewById(R.id.pdfProgress);
        errorView = findViewById(R.id.errorView);
        errorView.setOnClickListener(v -> {
            AlertDialog alert = new MaterialAlertDialogBuilder(FaxOpenActivity.this).setTitle("Unable to download Fax")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).setMessage(downloadError).create();
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();
                }
        );
        if (id.isEmpty()){
            FaxRepository.getInstance(getApplication()).getFaxFromTime(time).observe(this, this::displayFax);
        }else{
            FaxRepository.getInstance(getApplication()).getFax(id).observe(this, this::displayFax);
        }
    }

    private void displayFax(Fax fax) {
        if (fax != null){
            faxViewModel = new FaxViewModel(); // ViewModelProviders.of(this).get(FaxViewModel.class);
            faxViewModel.setItem(fax);
            faxViewModel.getOtherNumber().getMatchedContacts().observe(this, contacts -> {
                contactCount = contacts == null ? 0 : contacts.size();
                matchedContacts = contacts;
                invalidateOptionsMenu();
            });
//            mailboxView.setText(faxViewModel.getFormattedMailbox());
            mailboxView.setText(getResources().
                    getString(faxViewModel.isIncoming() ? R.string.from_with_placeholder : R.string.to_with_placeholder, faxViewModel.getFormattedMailbox()));
            faxViewModel.getOtherName().observe(this, (name) -> {
                if (name == null || name.equals(faxViewModel.getOtherNumber().formatted())){
                    nameView.setText(faxViewModel.getOtherNumber().formatted());
                }else{
                    nameView.setText(name + " - " + faxViewModel.getOtherNumber().formatted());
                }
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null){

                    actionBar.setTitle(name == null ? faxViewModel.getOtherNumber().formatted() : name);
                }else{
                   // android.util.Log.d("FaxOpen", "ActionBar was null");
                }
            });
            dateView.setText(faxViewModel.getFullDate());
            showFaxPDF();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fax_open, menu);
        MenuItem contactMenuItem = menu.findItem(R.id.viewContact);
        if (contactCount > 0){
            contactMenuItem.setTitle(R.string.view_contact);
            contactMenuItem.setIcon(R.drawable.ic_person_white);
        }else{
            contactMenuItem.setTitle(R.string.add_contact);
            contactMenuItem.setIcon(R.drawable.ic_person_add);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putByteArray("faxData", faxData);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        faxData = savedInstanceState.getByteArray("faxData");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share){
            String root = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Faxes";
            File filedirs = new File(root);
            Uri shareUri = FileProvider.getUriForFile(this, "com.telebroad.teleconsole.fileprovider", new File(getPathname(filedirs)));
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setDataAndType(shareUri,"application/pdf");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.putExtra(Intent.EXTRA_STREAM, shareUri);
            if (share.resolveActivity(getPackageManager()) == null){
                Toast.makeText(this, "You don't have any apps installed that can share PDF files. Please install one from your app store", Toast.LENGTH_LONG).show();
            }else{
                startActivity(share);
            }
        }else if (item.getItemId() == R.id.delete){
            faxViewModel.deleteItem();
            finish();
        }else if (item.getItemId() == R.id.fax){
            if (faxViewModel != null) {
                Uri shareUri = FileProvider.getUriForFile(this, "com.telebroad.teleconsole.fileprovider", getLocalFile());
                NewFaxActivity.showNewFaxActivity(this, shareUri);
            }
        }else if (item.getItemId() == R.id.viewContact){
            Utils.viewContact(matchedContacts, faxViewModel.getOtherNumber().fixed(), this);
            return true;
        }else if (item.getItemId() == R.id.download){
            Utils.asyncTask(this::saveFax);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    public void saveFax() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContentValues faxValues = new ContentValues();
            ContentResolver contentResolver = getContentResolver();
            Uri fileURI = MediaStore.Files.getContentUri(VOLUME_EXTERNAL_PRIMARY);
            faxValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf");
            faxValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + File.separator + "Telebroad Faxes");
            faxValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, faxViewModel.generateFileName());
            File localFile = getLocalFile();
            Uri file = contentResolver.insert(fileURI, faxValues);
            if (file == null){
                runOnUiThread(() -> Toast.makeText(this, "Error Saving Fax", Toast.LENGTH_LONG).show());
                return;
            }
            try {
                FileInputStream fileInputStream = new FileInputStream(localFile);
                OutputStream outputStream = contentResolver.openOutputStream(file);
                byte[] buffer = new byte[4096];
                int read = fileInputStream.read(buffer);
                if (outputStream != null) {
                    while (read != -1) {
                        outputStream.write(buffer, 0, read);
                        read = fileInputStream.read(buffer);
                    }
                    outputStream.flush();
                    outputStream.close();
                    fileInputStream.close();
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error Saving Fax", Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
            runOnUiThread(() -> Toast.makeText(this, "Fax Saved To Documents/TelebroadFaxes ", Toast.LENGTH_LONG).show());
        }
           // newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, recordingContactName + "_" + recordingFileName );
    }

    public File getLocalFile() {
        return new File(getPathname(new File(Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Faxes")));
    }

    private  void showFaxPDF(){
        String root = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Faxes";
        File filedirs = new File(root);
        final File filepath = new File(getPathname(filedirs));
        //android.util.Log.d("FAX003", filepath.getAbsolutePath());
        if(filepath.exists()){
            pdfView.fromFile(filepath).show();
//            pdfProgress.setVisibility(View.GONE);
        }else {
            HashMap<String, String> params = new HashMap<>();
            params.put(URLHelper.KEY_MAILBOX, faxViewModel.getItem().getMailbox());
            params.put(URLHelper.KEY_FILE, faxViewModel.getDownloadFilename());
            params.put(URLHelper.KEY_DIR, faxViewModel.getDir());
            //android.util.Log.d("FAX003", "fetching file " + faxViewModel.getDownloadFilename());
            URLHelper.request(GET, URLHelper.GET_FAX_FILE, params, (result) -> {
                String base64encoded = result.getAsJsonObject().get(URLHelper.KEY_DATA).getAsString();
                if (base64encoded != null && !base64encoded.isEmpty()){
                    //android.util.Log.d("Fax03", base64encoded);
                    try {
                        faxData = Base64.decode(base64encoded, 0);
                    }catch (IllegalArgumentException ile){
                        faxData = base64encoded.getBytes();
                    }
                    File file = saveFax(false);
                    if (file != null) {
                        runOnUiThread(() -> {
                            pdfView.fromFile(file).show();
                        });
                        //pdfView.fromFile(file).show();
//                        pdfProgress.setVisibility(View.GONE);
                    }
                }
            }, (error) -> {
                runOnUiThread(() -> {
                    downloadError = error.getErrorMessage();
                    pdfProgress.setVisibility(View.GONE);
                    errorView.setVisibility(View.VISIBLE);
                });

            });
        }
    }

    private String getPathname(File filedirs) {
        return filedirs.getAbsoluteFile() + File.separator + faxViewModel.getItem().getName() + ".pdf";
    }

    private File saveFax(boolean shouldRequest){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || (AppController.getInstance().hasExternalStoragePermissions() && faxData != null)){
            String root = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Faxes";
            File filedirs = new File(root);
            final File filepath = new File(getPathname(filedirs));
            filedirs.mkdirs();
           // android.util.Log.d("Filepath00", "filepath root " + filepath);
            try {
                filepath.createNewFile();
                FileOutputStream fos = new FileOutputStream( filepath, true);
                fos.write(faxData);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
               // android.util.Log.d("Filepath00" ,"File not found");
                e.printStackTrace();
            } catch (IOException e) {
              //  android.util.Log.d("Filepath00" ,"IOExeception");
                e.printStackTrace();
            }
            return filepath;
//            Toast.makeText(FaxOpenActivity.this, "Fax Saved",  Toast.LENGTH_SHORT);
        }else if (shouldRequest){
            ActivityCompat.requestPermissions(this, new String[] {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},0);
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        saveFax(false);
    }
}
