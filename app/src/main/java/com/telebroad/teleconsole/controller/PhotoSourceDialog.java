package com.telebroad.teleconsole.controller;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.databinding.FragmentSelectImageSourceBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PhotoSourceDialog extends BottomSheetDialogFragment {

    private static final int PICTURE_CHOOSER = 0;
    private static final int TAKE_PHOTO = 1;
    private static final int PERMISSIONS = 2;
    private OnDocumentChosen documentChosen;
    private FragmentSelectImageSourceBinding binding;
    private Uri photoURI;
    public PhotoSourceDialog() {}
    public PhotoSourceDialog(OnDocumentChosen documentChosen) {
        this.documentChosen = documentChosen;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        binding = FragmentSelectImageSourceBinding.inflate(getLayoutInflater());
        binding.attachPhotoView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICTURE_CHOOSER);
        });
        binding.takePhotoView.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(AppController.getInstance(), CAMERA ) == PERMISSION_GRANTED){
                startPictureTakingIntent();
            }else {
                requestPermissions(new String[]{CAMERA}, PERMISSIONS);
            }
        });
        dialog.setContentView(binding.getRoot());
        return dialog;
    }

    public void startPictureTakingIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(AppController.getInstance().getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                photoURI = FileProvider.getUriForFile(AppController.getInstance(),"com.telebroad.teleconsole.fileprovider",  photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, TAKE_PHOTO);
            } catch (IOException e) {e.printStackTrace();}
//            startActivityForResult(cameraIntent, PICTURE_TAKER);
        }else{
           // Log.d("Picture", "No camera");
            //TODO show error
        }
        //Log.d("PhotoSource", "take photo");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       // android.util.Log.d("PErmission", " Permission returned");
        if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED){
            startPictureTakingIntent();
        }else{
//            Toast.makeText(AppController.getInstance(), "We can't take a photo, because we don't have access to the camera", Toast.LENGTH_LONG).show();
            AlertDialog alert = new MaterialAlertDialogBuilder(getActivity()).
                    setTitle("Can't take photo").
                    setMessage("We can't take a photo, because we don't have access to the camera").
//                    setPositiveButton("Give Permission", (dialog, which) -> {
//                                Intent intent = new Intent();
//                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                intent.setData(Uri.parse("package:com.telebroad.teleconsole"));
//                                startActivity(intent);
//                                dialog.dismiss();
//                            }).
                    setNeutralButton("OK", (dialog, which) -> dialog.dismiss()).
                    create();
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();
            dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case PICTURE_CHOOSER:
                if (data == null || data.getData() == null){
                    Toast.makeText(getContext(), "Error choosing Picture", Toast.LENGTH_LONG).show();
                    return;
                }
                this.documentChosen.handleDocument(data.getData());
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    this.documentChosen.handleDocument(photoURI);
                }
        }
        dismiss();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @FunctionalInterface
    public interface OnDocumentChosen{
        void handleDocument(Uri uri);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = AppController.getInstance().getFilesDir();
        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
