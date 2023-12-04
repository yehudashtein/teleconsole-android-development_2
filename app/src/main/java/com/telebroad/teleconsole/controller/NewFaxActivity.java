package com.telebroad.teleconsole.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.MutableLiveData;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pdfview.PDFView;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.Fax;
import com.telebroad.teleconsole.model.FullPhone;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.TeleConsoleProfile;
import com.telebroad.teleconsole.model.repositories.ContactRepository;
import com.telebroad.teleconsole.model.repositories.FaxRepository;
import com.telebroad.teleconsole.notification.NotificationBuilder;
import com.telebroad.teleconsole.viewmodels.ContactViewModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import crl.android.pdfwriter.PDFWriter;
import team.clevel.documentscanner.ImageCropActivity;
import team.clevel.documentscanner.helpers.ScannerConstants;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.telebroad.teleconsole.helpers.Utils.getRotation;
import static com.telebroad.teleconsole.helpers.Utils.showNumberChooser;

public class NewFaxActivity extends AppCompatActivity {
    private TextView callerID;
    private EditText receiverText;
    private Group controls;
    private ContactViewModel contactViewModel;
    private FaxDocument document;
    private Group fileGroup;
    private FrameLayout loading;
    private TextView fileName;
    private final MutableLiveData<String> myNumber = new MutableLiveData<>();
    private static final String RECEIVER_EXTRA = "com.telebroad.teleconsole.new.fax.receiver";
    private static final String DATA_EXTRA = "com.telebroad.teleconsole.new.fax.has_data";
    private static final int DOCUMENT_CHOOSER = 0;
    private static final int PICTURE_TAKER = 1;
    private static final int ASK_PERMISSION = 2;
    private static final int PICTURE_CHOOSER = 3;
    private static final int PICTURE_CROPPED = 4;
    private Button sendFaxButton;
    private ConstraintLayout takePhotoLayout;
    public static void showNewFaxActivity(Activity activity) {
        showNewFaxActivity(activity, null, null, null);
    }
    public static void showNewFaxActivity(Activity activity, Bundle transition) {
        showNewFaxActivity(activity, null, null, transition);
    }

    public static void showNewFaxActivity(Activity activity, String receiver) {
        showNewFaxActivity(activity, receiver, null, null);
    }
    public static void showNewFaxActivity(Activity activity, Uri data) {
       // android.util.Log.d("FAX02", "redirecting");
        showNewFaxActivity(activity, null, data, null);
    }

    public static void showNewFaxActivity(Activity activity, String receiver, Uri data, Bundle transition) {
        Intent faxIntent = new Intent(activity, NewFaxActivity.class);
        if (receiver != null) {
            faxIntent.putExtra(RECEIVER_EXTRA, receiver);
        }
        if(data != null){
            faxIntent.setData(data);
            faxIntent.putExtra(DATA_EXTRA, true);
            //android.util.Log.d("FAX02", "data put");
        }
        activity.startActivity(faxIntent, transition);
    }
    private void handleIntent(Intent intent){
        if (intent == null){
            return;
        }
        if (intent.getBooleanExtra(DATA_EXTRA, false)){
            if (nullToEmpty(intent.getType()).contains("image")) {
                handleImage(intent);
            }else /*if ("application/pdf".equals(intent.getType()))*/{
                handleChosenDocument(intent.getData());
            }
            intent.putExtra(DATA_EXTRA, false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_fax);
        //<editor-fold desc="View finding">
        // Finding your views, please wait one moment
        callerID = findViewById(R.id.callerID);
        receiverText = findViewById(R.id.receiverText);
        controls = findViewById(R.id.controls);
        ConstraintLayout attachDocumentLayout = findViewById(R.id.attach_document_layout);
        takePhotoLayout = findViewById(R.id.take_photo_layout);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        ConstraintLayout fileLayout = findViewById(R.id.file_layout);
        ImageView deleteButton = findViewById(R.id.delete_button);
        sendFaxButton = findViewById(R.id.send_fax_button);
        fileGroup = findViewById(R.id.fileGroup);
        loading = findViewById(R.id.loadingPage);
        PDFView pdfviewer = findViewById(R.id.pdfViewer);
        loading.setOnClickListener(v -> {});
        findViewById(R.id.attach_photo_layout).setOnClickListener( v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICTURE_CHOOSER);
        });
        findViewById(R.id.attachmentLayout).setClipToOutline(true);
        fileName = findViewById(R.id.fileName);
        //</editor-fold>
        attachDocumentLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            String[] mimetypes = {"application/pdf"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            startActivityForResult(intent, DOCUMENT_CHOOSER);
        });
        RecyclerView matchedContacts = findViewById(R.id.matchedContacts);
        receiverText.setOnFocusChangeListener((view, focus) -> {
           // android.util.Log.d("Fax04", "Focus changed " + focus);
            if (focus) {
                controls.setVisibility(View.GONE);
                matchedContacts.setVisibility(View.VISIBLE);
            } else {
                setReceiver(PhoneNumber.format(receiverText.getText().toString()));
                InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(receiverText.getWindowToken(), 0);
                controls.setVisibility(View.VISIBLE);
                fileGroup.setVisibility(document == null ? View.GONE : View.VISIBLE);
                matchedContacts.setVisibility(View.GONE);
                checkFaxReady();
            }
        });
        receiverText.setOnEditorActionListener((v, actionId, event) -> {
           // android.util.Log.d("Fax04", "Action! " );
            setReceiver(PhoneNumber.format(receiverText.toString()));
            receiverText.clearFocus();
            return true;
        });
        ContactRecyclerAdapter adapter = new ContactRecyclerAdapter();
        adapter.setOnContactSelected(contact -> {
            if (contact.getFullLines().isEmpty()) {
                return;
            }
            if (contact.getFullLines().size() == 1) {
                setReceiver(contact.getTelephoneLines().get(0).formatted());
            } else {
                ContactPhoneChooserListDialog.getInstance(contact.getFullLines(),
                        v -> setReceiver(v.formatted()))
                        .show(getSupportFragmentManager(), "choose email");
            }
        });
        FullPhone.getLiveInstance().observe(this, phone -> {
            if (phone != null) {
                callerID.setText(getString(R.string.from_with_placeholder, PhoneNumber.format(phone.getCalleridExternal())));
            }
        });
        ArrayAdapter<String> myNumberAdapter = new ArrayAdapter<>(this, R.layout.item_choose_sms, new ArrayList<>());
        callerID.setOnClickListener(v -> showNumberChooser(this, myNumber, myNumberAdapter, R.string.chose_fax_num));
        myNumber.observe(this, number -> {
            if (!isNullOrEmpty(number)) {
                callerID.setText(getString(R.string.from_with_placeholder, PhoneNumber.format(number)));
            }
        });
        TeleConsoleProfile.getLiveInstance().observe(this, profile -> {
            if (profile == null) {
                return;
            }
            if (profile.getFaxBoxes().isEmpty()) {
                AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.no_fax_line_title).setMessage(R.string.no_fax_lines_message).setPositiveButton(R.string.contact, ((dialog, which) -> {
                    SipManager.getInstance().call(getString(R.string.telebroad_support_number), this);
                    finish();
                })).setCancelable(false).setNegativeButton(android.R.string.cancel, ((dialog, which) -> finish())).create();
                alert.setOnShowListener(dialog -> {
                    Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                    negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                });alert.show();
            }else {
                List<String> faxList = PhoneNumber.convertNumberListToFormatted(profile.getOwnedPhoneNumber());
                myNumberAdapter.clear();
                myNumberAdapter.addAll(faxList);
            }
//            android.util.Log.d("SMS01", "email = " + settings.getDefaultSMSLine());
//            updateLiveData(myNumber, PhoneNumber.format(settings.getDefaultSMSLine()));
        });
        deleteButton.setOnClickListener(v -> {
            document = null;
            fileGroup.setVisibility(View.GONE);
            checkFaxReady();
        });
        sendFaxButton.setOnClickListener(v -> {
            Map<String, String> params = new HashMap<>();
            params.put(URLHelper.KEY_SNUMBER, PhoneNumber.fix(callerID.getText().toString()));
            params.put(URLHelper.KEY_CNUMBER, PhoneNumber.fix(receiverText.getText().toString()));
            params.put(URLHelper.KEY_EMAIL, TeleConsoleProfile.getInstance().getEmail());
            params.put(URLHelper.KEY_DATA, document.data);
            params.put(URLHelper.KEY_FILE_EXTENSION, document.extension);
            NotificationBuilder builder = NotificationBuilder.getInstance();
            builder.showSendingFaxNotification(true, false);
            URLHelper.request(Request.Method.POST, URLHelper.SEND_FAX_URL, params, r -> {
                Fax fax = new Fax();
                fax.setDirection(Message.Direction.OUT);
                fax.setCallerid(PhoneNumber.fix(callerID.getText().toString()));
                fax.setCalled(PhoneNumber.fix(receiverText.getText().toString()));
                long time = System.currentTimeMillis();
                fax.setTimestamp(time);
                fax.setId(time + "");
                Utils.asyncTask(() -> FaxRepository.getInstance().saveFaxes(fax));
                runOnUiThread(() -> {
                    Toast.makeText(this, "Sending fax...", Toast.LENGTH_SHORT).show();
                });
                if (r == null) {
                   // android.util.Log.d("FAX02", "no result");
                    builder.showSendingFaxNotification(false, true);
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.fax_sent_toast, Toast.LENGTH_SHORT).show();
                    });
                    finish();
                    return;
                }
                builder.showSendingFaxNotification(false, false);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.fax_sent_toast, Toast.LENGTH_SHORT).show();
                });
                finish();
               // android.util.Log.d("FAX02", "result is " + r);
            }, e -> {
                builder.showSendingFaxNotification(false, true);
               // android.util.Log.d("FAX02", "error is " + e.getFullErrorMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Sending Fax Failed " + e.getErrorMessage(), Toast.LENGTH_LONG).show();
                });
            });
        });
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            takePhotoLayout.setVisibility(View.GONE);
        }
        takePhotoLayout.setOnClickListener(v -> {
           if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
               ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ASK_PERMISSION);
           }else{
               startPictureTakingActivity();
           }
        });
        matchedContacts.setLayoutManager(new LinearLayoutManager(this));
        matchedContacts.setAdapter(adapter);
        ContactRepository contactRepository = ContactRepository.getInstance();
        receiverText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 1) {
                    adapter.setContacts(new ArrayList<>());
                } else {
                    ContactRepository.getInstance().findContact(s.toString());

                }
            }
        });
        receiverText.setOnEditorActionListener((v, actionId, event) -> {
            if (adapter.getItemCount() == 0) {
                receiverText.clearFocus();
            }
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(receiverText.getWindowToken(), 0);
            return false;
        });
        contactRepository.getMatchedContacts().observe(this, contacts -> {
            List<Contact> contactList = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                contactList = contacts.stream().filter(Contact::hasFullLines).collect(Collectors.toList());
            }else{
                for ( Contact contact: contacts) {
                    if (contact.hasFullLines()) {
                        contactList.add(contact);
                    }
                }
            }
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
    private void setReceiver(String receiver) {
        receiverText.setText(receiver);
        receiverText.clearFocus();
    }


    private void startPictureTakingActivity() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                Uri photoURI = FileProvider.getUriForFile(this,"com.telebroad.teleconsole.fileprovider",  photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, PICTURE_TAKER);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            startActivityForResult(cameraIntent, PICTURE_TAKER);
        }else{
           // Log.d("Picture", "No camera");
            //TODO show error
        }
    }

    @Override
    protected void onResume() {
        handleIntent(getIntent());
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (receiverText.isFocused()) {
            receiverText.clearFocus();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ASK_PERMISSION) {
            if (grantResults[0] == PERMISSION_DENIED) {
                takePhotoLayout.setVisibility(View.GONE);
            } else {
                startPictureTakingActivity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
       // Log.d("Scan02", "requestcode = " + requestCode + " result code " + resultCode);
        // Using switch instead of 2 ifs, to make it easier to add requests in the future.
        switch (requestCode) {
            case DOCUMENT_CHOOSER:
                handleChosenDocument(data.getData());
                break;
            case PICTURE_CHOOSER:
                handleImage(data);
                break;
            case PICTURE_TAKER:
                if (resultCode == RESULT_OK) {
//                    if (data.getExtras() == null) {
//                        return;
//                    }
                    try {
                        ExifInterface exifInterface = new ExifInterface(currentPhotoPath);
                        ScannerConstants.selectedImageBitmap = Utils.rotateBitmap(getRotation(exifInterface), BitmapFactory.decodeFile(currentPhotoPath));//BitmapFactory.decodeFile(currentPhotoPath);
//                    (Bitmap) data.getExtras().get("data");
                        Intent intent = new Intent(NewFaxActivity.this, ImageCropActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivityForResult(intent, PICTURE_CROPPED);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PICTURE_CROPPED:
                //Log.d("SCAN02", "Picture cropped");
                loading.setVisibility(View.VISIBLE);
                AsyncTask.execute(() -> {
                    document = new FaxDocument(ScannerConstants.selectedImageBitmap);
                    runOnUiThread(() -> {
                        updateDocumentStatus();
                        loading.setVisibility(View.GONE);
                    });
                });
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleImage(Intent data) {
        if (data == null || data.getData() == null){
            return;
        }
        //            InputStream inputStream = getContentResolver().openInputStream(data.getData());
        ScannerConstants.selectedImageBitmap = Utils.getBitmapFromURI(getContentResolver(), data.getData());//BitmapFactory.decodeStream(inputStream);
        Intent intent = new Intent(NewFaxActivity.this, ImageCropActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, PICTURE_CROPPED);
    }

    private void checkFaxReady() {
        sendFaxButton.setEnabled(document != null && !isNullOrEmpty(receiverText.getText().toString()));
    }

    private void setupFileLayout() {
        //android.util.Log.d("Fax04", "setting up file");
        fileGroup.setVisibility(View.VISIBLE);
        fileName.setText(document.displayName);
    }

    private void handleChosenDocument(Uri documentUri) {
        if (documentUri != null) {
            loading.setVisibility(View.VISIBLE);
            AsyncTask.execute(() -> {
                document = new FaxDocument(documentUri);
                runOnUiThread(this::updateDocumentStatus);
            });
        }
    }

    private void updateDocumentStatus() {
        setupFileLayout();
        checkFaxReady();
        loading.setVisibility(View.GONE);
    }

//    private void showNumberChooser(MutableLiveData<String> myNumber, ArrayAdapter<String> myNumberAdapter) {
//        new AlertDialog.Builder(this, R.style.SMSChooserDialog).setTitle(R.string.chose_fax_num).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
//                .setSingleChoiceItems(myNumberAdapter, myNumberAdapter.getPosition(myNumber.getValue()), (dialog, which) -> {
//                    updateLiveData(myNumber, myNumberAdapter.getItem(which));
//                    dialog.dismiss();
//                }).show();
//    }
    String currentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getFilesDir();
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private class FaxDocument {
        private final Uri uri;
        String displayName;
        // Base64 encoded fax data
        String data;
        // File extension
        String extension;

        private FaxDocument(Uri uri) {
            this.uri = uri;
            fetchMetadata();
            getDataFromUri();
        }

        private FaxDocument(Bitmap bitmap){
            if (bitmap.getByteCount() > 850  * 1000){
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                float aspectRatio = (float) height / width;
                float scale = aspectRatio < 1.78f ? 1080.0f / width : 1920.0f / height;
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            }
            uri = null;
            displayName = "Picture from camera";
            PDFWriter pdfWriter = new PDFWriter(bitmap.getWidth(), bitmap.getHeight());
            pdfWriter.addImage(0,0, bitmap);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                pdfWriter.writeTo(byteArrayOutputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                data = encode(inputStream);
                extension = "pdf";
                //android.util.Log.d("Scan02", "PDF error");
            } catch (IOException e) {
               // android.util.Log.d("Scan02", "Error ", e);
                e.printStackTrace();
            }
        }

        private String getPathname(File filedirs) {
            return filedirs.getAbsolutePath() + File.separator + android.text.format.DateFormat.format("M\\dd\\yyyy hh:mm a (ss)", new java.util.Date()).toString() + ".pdf";
        }

        private FaxDocument(Uri uri, String data, String displayName, String extension) {
            this.uri = uri;
            this.displayName = displayName;
            this.data = data;
            this.extension = extension;
        }

        private void getDataFromUri() {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                data = encode(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("Range")
        private void fetchMetadata() {
            Cursor documentMetadataCursor = getContentResolver().query(uri, null, null, null, null, null);
            if (documentMetadataCursor != null && documentMetadataCursor.moveToFirst()) {
                this.displayName = documentMetadataCursor.getString(documentMetadataCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                String[] parts = this.displayName.split("\\.");
                if (parts.length > 0){
                    this.extension = parts[parts.length - 1];
                    //android.util.Log.d("EXTENSION01", "Extension is " + this.extension);
                }
//                this.extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(documentMetadataCursor.getString(documentMetadataCursor.getColumnIndex(OpenableColumns.M)));
                String[] columnNames = documentMetadataCursor.getColumnNames();
                String allNames = "";
                for (String column : columnNames) {
                    allNames = allNames.concat(column + "\n ");
                }
               // Log.d("FAX01", "column Names " + allNames);
            }
            if (documentMetadataCursor != null) {
                documentMetadataCursor.close();
            }
        }

        private String encode(InputStream inputStream) {
            byte[] bytes;
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                byte[] buffer = new byte[8192];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            bytes = output.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        private Bitmap getBitmapFromUri(Uri uri) throws IOException {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        }
    }
}
