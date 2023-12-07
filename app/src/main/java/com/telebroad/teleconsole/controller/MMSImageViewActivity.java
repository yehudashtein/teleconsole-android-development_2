package com.telebroad.teleconsole.controller;

import static com.google.common.base.Strings.isNullOrEmpty;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MMSImageViewActivity extends AppCompatActivity {
    public static final String MMS_IMAGE_URL = "com.telebroad.teleconsole.controller.mms.image.view.activity.mms.image.urlString";
    private String urlString = "";
    private URL url;
    private ImageView imageView;
    private static final String MMS_IMAGE_FOLDER = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "MMS Images";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mms_image_view);
        urlString = getIntent().getStringExtra(MMS_IMAGE_URL);
        imageView = findViewById(R.id.mmsImage);
        supportPostponeEnterTransition();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAfterTransition();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        Glide.with(imageView.getContext()).load(urlString).apply(RequestOptions.placeholderOf(R.drawable.ic_image_color_primary)).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                supportStartPostponedEnterTransition();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                supportStartPostponedEnterTransition();
                return false;
            }
        }).into(imageView);
        if(!isNullOrEmpty(urlString)){
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    AnimationDrawable downloadAnim;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mms_open, menu);
        View downloadMenuView = menu.findItem(R.id.saveIm).getActionView();
        ImageView iv = menu.findItem(R.id.saveIm).getActionView().findViewById(R.id.download_iv);
        if (iv != null){
            downloadAnim = (AnimationDrawable) iv.getDrawable();
        }
        downloadMenuView.setOnClickListener(v -> {
            saveImage();
            BitmapDrawable bitmapDrawable =(BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            saveImageToGallery(bitmap);
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishAfterTransition();
                break;
            case R.id.saveIm:
                BitmapDrawable bitmapDrawable =(BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                try {
                    Bitmap bitmap1 = Glide.with(this).asBitmap().load(urlString).submit().get();
                    URL url = new URL(urlString);
                    Bitmap bitmap2 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    saveImageToGallery(bitmap2);
                    //saveImageToGallery(bitmap);
                    //saveImageToGallery(bitmap1);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveImage();
                break;
            case R.id.share:
                share();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImageToGallery(Bitmap bitmap) {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                ContentResolver contentResolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,System.currentTimeMillis()+".jpg");
                contentValues.put(MediaStore.MediaColumns.DATA,urlString);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/*");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+ File.separator+"TestFolder");
                Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                fos = contentResolver.openOutputStream(Objects.requireNonNull(uri));
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                Objects.requireNonNull(fos);
            }
        }catch (Exception e){}
    }

    private void share(){
        File file = getFile(new File(MMS_IMAGE_FOLDER));
        if (file.exists()){
            shareFile(file);
        }else{
            AsyncTask.execute(() -> {
                if(!isNullOrEmpty(urlString)){
                 downloadImage(null  ,() -> shareFile(file));
              }
           });
        }
    }

    private void shareFile(File file) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri uriForFile = FileProvider.getUriForFile(AppController.getInstance(), "com.telebroad.teleconsole.fileprovider", file);
        shareIntent.setDataAndType(uriForFile, getMimeType());
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //android.util.Log.e("Test", "Test1");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriForFile);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
        //android.util.Log.e("Test", "Test2");
    }

    private void saveImage() {
        AsyncTask.execute(() -> {
            if(!isNullOrEmpty(urlString)){
                downloadImage(downloadAnim::start ,this::finishDownloading);
            }
        });
    }

    private String getMimeType(){
        String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(urlString);
        if("jfif".equals(fileExtensionFromUrl)){
            return "image/jpeg";
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionFromUrl);
    }
    private void downloadImage(Runnable onStart, Runnable onComplete) {
        if (url != null){
            File fileDir = new File(MMS_IMAGE_FOLDER);
            //noinspection ResultOfMethodCallIgnored
            fileDir.mkdirs();
            final File file = getFile(fileDir);
            try {
                if (url != null){
                    if (onStart != null){
                        runOnUiThread(onStart);
                    }
                    InputStream is = url.openStream();
                    OutputStream os = new FileOutputStream(file);
                    byte[] b = new byte[2048];
                    int length;
                    while((length = is.read(b)) != -1){
                        os.write(b,0, length);
                    }
                    is.close();
                    os.close();
                }
            } catch (IOException e) {
                // TODO Display error message
                e.printStackTrace();
            } finally {
                // TODO Display error message
                if (onComplete != null){
                    runOnUiThread(onComplete);
                }
            }
        }
    }

    private void finishDownloading() {
        downloadAnim.stop();
        downloadAnim.selectDrawable(0);
        Toast.makeText(this, R.string.image_downloaded, Toast.LENGTH_SHORT).show();
    }

    private File getFile(File fileDir) {
        String urlFile = url.getFile();
        String fileName;
        if (urlFile.contains("?")){
             fileName = urlFile.substring(urlFile.lastIndexOf("/"), urlFile.indexOf('?'));
            //Log.v("fileName",fileName);
        }else {
            fileName = urlFile.substring(urlFile.lastIndexOf("/"));
        }
        return new File(fileDir.getAbsoluteFile() + File.separator + fileName);
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
    }
}
