package com.telebroad.teleconsole.controller;

import static com.google.common.base.Strings.isNullOrEmpty;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
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

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

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

public class MMSVideoViewActivity extends AppCompatActivity {

    public static String MMS_VIDEO_URL = "com.telebroad.teleconsole.controller.mms.image.view.activity.mms.video.urlString";
    private static final String MMS_VIDEO_FOLDER = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "MMS VIDEOS";
    AnimationDrawable downloadAnim;

    private PlayerView playerView;
    private ExoPlayer player;
    private String urlString = "";
    private String fileName;
    private URL url;

    @OptIn(markerClass = UnstableApi.class) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mms_video_view1);
        playerView = findViewById(R.id.exo_player_view);
        urlString = getIntent().getStringExtra(MMS_VIDEO_URL);
        Uri videoUri = Uri.parse(urlString);
        if(!isNullOrEmpty(urlString)){
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        // Set the media item to be played.
        player.setMediaItem(mediaItem);
        // Prepare the player.
        player.prepare();
        // Start the playback.
        player.play();
    }


    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
    }

    // TODO: 12/6/2023 all methods are copied from mmsImageViewActivity. need to do adoptations
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
            saveVideo();
//            BitmapDrawable bitmapDrawable =(BitmapDrawable) videoView.getDrawable();
//            Bitmap bitmap = bitmapDrawable.getBitmap();
            saveVideoToGallery();
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
                //BitmapDrawable bitmapDrawable =(BitmapDrawable) videoView.getDrawable();
                //Bitmap bitmap = bitmapDrawable.getBitmap();
                try {
                    //Bitmap bitmap1 = Glide.with(this).asBitmap().load(urlString).submit().get();
                    URL url = new URL(urlString);
                    //Bitmap bitmap2 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    saveVideoToGallery();
                    //saveImageToGallery(bitmap);
                    //saveImageToGallery(bitmap1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                saveVideo();
                break;
            case R.id.share:
                share();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveVideoToGallery() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".mp4");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + File.separator + "Telebroad Videos");

                    ContentResolver contentResolver = getContentResolver();
                    Uri uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);

                    try (OutputStream fos = contentResolver.openOutputStream(Objects.requireNonNull(uri));
                         InputStream inputStream = new URL(urlString).openStream()) {
                        byte[] buf = new byte[8192];
                        int length;
                        while ((length = inputStream.read(buf)) > 0) {
                            fos.write(buf, 0, length);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else Toast.makeText(MMSVideoViewActivity.this, "This android version is too old for this feature", Toast.LENGTH_SHORT).show();
            }
        }).start();
    }


    private void share(){
        File file = getFile(new File(MMS_VIDEO_FOLDER));
        if (file.exists()){
            shareFile(file);
        }else{
            AsyncTask.execute(() -> {
                if(!isNullOrEmpty(urlString)){
                    downloadVideo(null  ,() -> shareFile(file));
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

    private void saveVideo() {
        AsyncTask.execute(() -> {
            if(!isNullOrEmpty(urlString)){
                downloadVideo(downloadAnim::start ,this::finishDownloading);
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
    private void downloadVideo(Runnable onStart, Runnable onComplete) {
        if (url != null){
            File fileDir = new File(MMS_VIDEO_FOLDER);
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




}
