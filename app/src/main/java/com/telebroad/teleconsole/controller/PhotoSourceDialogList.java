package com.telebroad.teleconsole.controller;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.databinding.ChatChoseBinding;
import com.telebroad.teleconsole.databinding.FragmentSelectImageAndVideoBinding;
import com.telebroad.teleconsole.databinding.FragmentSelectImageSourceBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoSourceDialogList extends BottomSheetDialogFragment {
    //ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ActivityResultLauncher<Intent> videoLauncher;
    int takeFlags;
    private static final int PICTURE_CHOOSER = 0;
    private static final int TAKE_PHOTO = 1;
    private static final int PERMISSIONS = 2;
    private final OnDocumentChosen documentChosen;
    private ChatChoseBinding binding;
    List<Uri> uris;
    private Uri photoURI;

    public PhotoSourceDialogList(OnDocumentChosen documentChosen) {
        this.documentChosen = documentChosen;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //ActivityResultLauncher<Intent> mGetImg;
        binding = ChatChoseBinding.inflate(getLayoutInflater());
        uris = new ArrayList<>();
        videoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        //Uri videoUri = data.getData();
                        try {
                            ClipData clipData =  data.getClipData();
                            if (clipData != null) {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    ClipData.Item item = clipData.getItemAt(i);
                                    Uri uri = item.getUri();
                                    getActivity(). getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                    uris.add(uri);
                                }
                                this.documentChosen.handleDocument(uris);
                                dismiss();
                            }
                            //Uri[] uri = (Uri[]) data.getData();
                            //this.documentChosen.handleDocument(Arrays.asList(convert(data.getData())));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        try {
//                            this.documentChosen.handleDocument(Arrays.asList(convert(videoUri)));
//                            dismiss();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                });
//        mGetImg = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
//                    Bundle bundle = result.getData().getExtras();
//                    //assert bundle != null;
//                    //Uri[] uris = (Uri[]) bundle.get("data");
//                    Bitmap bitmap = (Bitmap) bundle.get("data");
//                    try {
//                        //documentChosen.handleDocument(Arrays.asList(uris));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        });
        binding.takeVideoView.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            videoLauncher.launch(intent);
        });
        binding.ChoseVideo.setOnClickListener(v -> {
             Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            videoLauncher.launch(intent);
        });

        binding.attachPhotoView.setOnClickListener(v -> {
            String deviceName = "Samsung";
            if(deviceName.equals(android.os.Build.MODEL)){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICTURE_CHOOSER);
            }else{
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICTURE_CHOOSER);
            }
            //mGetImg.launch(intent);
            //startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICTURE_CHOOSER);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
//            startActivityForResult(cameraIntent, PICTURE_TAKER);
        }else{
            //Log.d("Picture", "No camera");
            //TODO show error
        }
       // Log.d("PhotoSource", "take photo");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       // Log.d("PErmission", " Permission returned");
        if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED){
            startPictureTakingIntent();
        }else{
//            Toast.makeText(AppController.getInstance(), "We can't take a photo, because we don't have access to the camera", Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(getActivity(), R.style.DialogStyle).
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
                    create().show();
            dismiss();
        }
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case PICTURE_CHOOSER:
                if (data == null ){
                    Toast.makeText(getContext(), "Error choosing Picture", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    ClipData clipData =  data.getClipData();
                    if (clipData != null) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            Uri uri = item.getUri();
                            uris.add(uri);
                        }
                        this.documentChosen.handleDocument(uris);
                    }
                    //Uri[] uri = (Uri[]) data.getData();
                    //this.documentChosen.handleDocument(Arrays.asList(convert(data.getData())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        this.documentChosen.handleDocument(Arrays.asList(convert(photoURI)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
        dismiss();
        super.onActivityResult(requestCode, resultCode, data);
    }
    private static Uri[] convert(Uri... array) {return array;}


    @FunctionalInterface
    public interface OnDocumentChosen{
        void handleDocument(List<Uri> uri) throws IOException;
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = AppController.getInstance().getFilesDir();

        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",/* suffix */
                storageDir      /* directory */
        );
    }
}
