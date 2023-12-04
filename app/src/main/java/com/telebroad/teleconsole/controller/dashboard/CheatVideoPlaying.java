package com.telebroad.teleconsole.controller.dashboard;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.controller.dashboard.ChatActivity.CURRENT_CHAT_EXTRA;
import static com.telebroad.teleconsole.controller.dashboard.ChatImageActivity.MMS_IMAGE_URL;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import android.app.PictureInPictureParams;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.DelMessage;
import com.telebroad.teleconsole.chat.client.ReplacePub;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.chat.viewModels.ChatMessageViewModel;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.databinding.ActivityCheatVideoPlayingBinding;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CheatVideoPlaying extends AppCompatActivity implements MenuProvider {
    private static final String MMS_IMAGE_FOLDER = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Chat Videos";
    private ActivityCheatVideoPlayingBinding binding;
    private URL Url;
    private Replies replies;
    //private String url;
    private PictureInPictureParams.Builder picInPicParams;
    private ActionBar actionBar;
    private PackageManager packageManager;
    private ChatMessageViewModel chatMessageViewModel;
    String attachments;
    String url;
    boolean isBackPressed;
    private final Gson gson = new Gson();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheatVideoPlayingBinding.inflate(getLayoutInflater());
//        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
//        getWindow().setEnterTransition(new Explode());
        setContentView(binding.getRoot());
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.black)));
        Rect rect = binding.chatVideo.getClipBounds();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            picInPicParams = new PictureInPictureParams.Builder();
            picInPicParams.setSourceRectHint(rect);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                picInPicParams.setAutoEnterEnabled(true);
            }
        }
        packageManager = this.getPackageManager();
        url = getIntent().getStringExtra("videoURL");
        //this.url = url;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            chatMessageViewModel = getIntent().getSerializableExtra("ChatMessageViewModel",ChatMessageViewModel.class);
            String repliesCollectionList = getIntent().getStringExtra("repliesCollectionList");
            replies = gson.fromJson(repliesCollectionList,new TypeToken<Replies>() {}.getType());
            attachments = getIntent().getStringExtra("replyAttachments");
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("originalMassage")) {
                Object messageObject = extras.get("originalMassage");
                if (messageObject instanceof ChatMessageViewModel) {
                    chatMessageViewModel = (ChatMessageViewModel) messageObject;
                    String repliesList = getIntent().getStringExtra("repliesCollectionList");
                    replies = gson.fromJson(repliesList, new TypeToken<Replies>() {
                    }.getType());
                    attachments = getIntent().getStringExtra("replyAttachments");
                }
            }
        }
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {@Override public void handleOnBackPressed() {
            if (!isInPictureInPictureMode() && HasSupport() && binding.chatVideo.isPlaying()){
                picInPicMode();
            }
            else {
                finish();
            }
        }};
        getOnBackPressedDispatcher().addCallback(this, callback);
        binding.chatVideo.setTransitionName(url);
        binding.chatVideo.setVideoPath(url);
        MediaController mediaControllerCompat = new MediaController(this);
        binding.chatVideo.setMediaController(mediaControllerCompat);
        mediaControllerCompat.setAnchorView(binding.chatVideo);
        binding.chatVideo.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            binding.chatVideo.start();
            supportPostponeEnterTransition();
        });
        binding.cancelEdit.setOnClickListener(v -> {
            if (binding.relEtLayout.getVisibility() == View.VISIBLE){
                binding.relEtLayout.setVisibility(View.GONE);
            }
        });
        binding.confirmEdit.setOnClickListener(v -> {
            if (chatMessageViewModel != null){
                String attachments = gson.toJson(chatMessageViewModel.getAttachments());
                Type collectionType = new TypeToken<List<ReplacePub.Attachments>>() {}.getType();
                List<ReplacePub.Attachments> enums = gson.fromJson(attachments, collectionType);
                ReplacePub.Head head = new ReplacePub.Head("text/*",chatMessageViewModel.getSeq(),enums);
                ReplacePub pubMessage = new ReplacePub("replace",chatMessageViewModel.getTopic(),SettingsHelper.getString(SettingsHelper.MY_TOPIC),true,binding.editTextImage.getText().toString(), head);
                ChatWebSocket.getInstance().sendObject("pub",pubMessage);
                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().UpdateContent(binding.editTextImage.getText().toString(),chatMessageViewModel.getSeq(),chatMessageViewModel.getTopic()));
                binding.relEtLayout.setVisibility(View.GONE);
            }else {
                Type collectionType = new TypeToken<List<ReplacePub.Attachments>>() {}.getType();
                List<ReplacePub.Attachments> enums = gson.fromJson(attachments, collectionType);
                ReplacePub.Head head = new ReplacePub.Head("text/*",replies.getSeq(),enums);
                ReplacePub pubMessage = new ReplacePub("replace",replies.getTopic(),SettingsHelper.getString(SettingsHelper.MY_TOPIC),true,binding.editTextImage.getText().toString(), head);
                ChatWebSocket.getInstance().sendObject("pub",pubMessage);
                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().UpdateContent(binding.editTextImage.getText().toString(),replies.getSeq(),replies.getTopic()));
                binding.relEtLayout.setVisibility(View.GONE);
            }
        });
        binding.chatVideo.setOnCompletionListener(
                MediaPlayer::stop);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if(!isNullOrEmpty(url)){
            try {
                Url = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (chatMessageViewModel == null){
            getMenuInflater().inflate(R.menu.chat_video_reply, menu);
            View downloadMenuView = menu.findItem(R.id.saveIm).getActionView();
            downloadMenuView.setOnClickListener(v -> {
                downloadImage();
            });
        }else {
            getMenuInflater().inflate(R.menu.chat_image_menu, menu);
            View downloadMenuView = menu.findItem(R.id.saveIm).getActionView();
            downloadMenuView.setOnClickListener(v -> {
                downloadImage();
            });
        }

        return true;
    }
    private void downloadImage(){
        File file = getFile(new File(MMS_IMAGE_FOLDER));
        if (file.exists()){
            saveToGallery(file);
        }else{
            AsyncTask.execute(() -> {
                if(!isNullOrEmpty(url)){
                    downloadImage(null  ,() -> saveToGallery(file));
                }
            });
        }
    }


    private void saveToGallery(File file) {
        Uri uri1 = FileProvider.getUriForFile(CheatVideoPlaying.this, "com.telebroad.teleconsole.fileprovider", file);
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver contentResolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"video.mp4");
                contentValues.put(MediaStore.MediaColumns.DATA, uri1.toString());
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + File.separator + "TestFolder");
                Uri uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = contentResolver.openOutputStream(uri);
                ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "w");
                fos = new FileOutputStream(pfd.getFileDescriptor());
                fos.write(getVideoData(uri1)); // Replace `videoData` with your video data
                fos.close();
                pfd.close();
            }
            Toast.makeText(this, "Downloaded to gallery", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private byte[] getVideoData(Uri videoUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(videoUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byteArrayOutputStream.flush();
            inputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (chatMessageViewModel != null){
                finish();
            }else {
                finish();
            }

        }
        switch (item.getItemId()) {
            case R.id.share:
                try {
                    share();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.saveIm:
                break;
            case R.id.imageForward:
                if (chatMessageViewModel != null){
                    Intent intent = new Intent(CheatVideoPlaying.this,ChatForwardActivity.class);
                    intent.putExtra("messageObject",chatMessageViewModel);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(CheatVideoPlaying.this,ChatForwardActivity.class);
                    String replies1 = gson.toJson(replies);
                    intent.putExtra("messageObject",replies1);
                    startActivity(intent);
                }
                break;
            case R.id.imageReply:
                Intent intent1 = new Intent(this,ChatReplyActivity.class);
                intent1.putExtra("originalMassage",chatMessageViewModel);
                intent1.putExtra(MMS_IMAGE_URL,url);
                startActivity(intent1);
                break;
            case R.id.imageDelete:
                List<DelMessage.Delseq> delseqList = new ArrayList<>();
                if (chatMessageViewModel != null) {
                    DelMessage.Delseq delseq = new DelMessage.Delseq(chatMessageViewModel.getSeq());
                    delseqList.add(delseq);
                    DelMessage delMessage = new DelMessage("delete", chatMessageViewModel.getTopic(), "msg", true, delseqList);
                    ChatWebSocket.getInstance().sendObject("del", delMessage);
                    for (DelMessage.Delseq d : delseqList) {
                        ListenableFuture<Boolean> hasMessageWithSeq = ChatDatabase.getInstance().chatMessageDao().hasMessageWithSeq(d.getLow(), chatMessageViewModel.getTopic());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            Futures.addCallback(hasMessageWithSeq,
                                    new FutureCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            if (result) {
                                                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteMassage(chatMessageViewModel.getTopic(), d.getLow()));
                                                Intent intent = new Intent(CheatVideoPlaying.this, ChatActivity.class);
                                                String topic = chatMessageViewModel.getTopic();
                                                intent.putExtra(CURRENT_CHAT_EXTRA, topic);
                                                startActivity(intent);
                                                finishAfterTransition();
                                            } else {
                                                Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteMassage(chatMessageViewModel.getTopic(), d.getLow()));
                                                Intent intent = new Intent(CheatVideoPlaying.this, ChatActivity.class);
                                                intent.putExtra(CURRENT_CHAT_EXTRA, chatMessageViewModel);
                                                startActivity(intent);
                                                finishAfterTransition();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable t) {
                                        }
                                    }, AppController.getInstance().getMainExecutor());
                        }
                    }
                }else {
                    DelMessage.Delseq delseq = new DelMessage.Delseq(replies.getSeq());
                    delseqList.add(delseq);
                    DelMessage delMessage = new DelMessage("delete", replies.getTopic(), "msg", true, delseqList);
                    ChatWebSocket.getInstance().sendObject("del", delMessage);
                    for (DelMessage.Delseq d : delseqList) {
                        ListenableFuture<Boolean> hasMessageWithSeq = ChatDatabase.getInstance().chatMessageDao().hasMessageWithSeq(d.getLow(), replies.getTopic());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            Futures.addCallback(hasMessageWithSeq,
                                    new FutureCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            if (result) {
                                                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteMassage(replies.getTopic(), d.getLow()));
                                                Intent intent = new Intent(CheatVideoPlaying.this, ChatActivity.class);
                                                String topic = replies.getTopic();
                                                intent.putExtra(CURRENT_CHAT_EXTRA, topic);
                                                startActivity(intent);
                                                finishAfterTransition();
                                                finish();
                                            } else {
                                                Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteMassage(replies.getTopic(), d.getLow()));
                                                Intent intent = new Intent(CheatVideoPlaying.this, ChatActivity.class);
                                                intent.putExtra(CURRENT_CHAT_EXTRA, replies.getTopic());
                                                startActivity(intent);
                                                finishAfterTransition();
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable t) {
                                        }
                                    }, AppController.getInstance().getMainExecutor());
                        }
                    }
                }
                break;
            case R.id.imageEdit:
                if (chatMessageViewModel != null){
                    binding.relEtLayout.setVisibility(View.VISIBLE);
                    binding.editTextImage.setText(SettingsHelper.reformatHTML(chatMessageViewModel.getText()));
                }else {
                    binding.relEtLayout.setVisibility(View.VISIBLE);
                    binding.editTextImage.setText(SettingsHelper.reformatHTML(replies.getContent()));
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void share() throws URISyntaxException {
        File file = getFile(new File(MMS_IMAGE_FOLDER));
        if (file.exists()){
            shareFile(file);
        }else{
            AsyncTask.execute(() -> {
                if(!isNullOrEmpty(url)){
                    downloadImage(null  ,() -> shareFile(file));
                }
            });
        }
    }
    private void downloadImage(Runnable onStart, Runnable onComplete) {
        if (url != null){
            File fileDir = new File(MMS_IMAGE_FOLDER);
            fileDir.mkdirs();
            final File file = getFile(fileDir);
            try {
                if (Url != null){
                    if (onStart != null){
                        runOnUiThread(onStart);
                    }
                    InputStream is = Url.openStream();
                    OutputStream os = new FileOutputStream( file);
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
    private File getFile(File fileDir) {
        String urlFile = Url.getFile();
        String fileName;
        if (urlFile.contains("?")){
            fileName = urlFile.substring(urlFile.lastIndexOf("/"), urlFile.indexOf('?'));
        }else {
            fileName = urlFile.substring(urlFile.lastIndexOf("/"));
        }
        fileName = fileName.substring(0,fileName.lastIndexOf("."));
        return new File(fileDir.getAbsoluteFile() + File.separator + fileName+".mp4");
    }

    private void shareFile(File file) {
        String ff = getMimeType();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri uriForFile = FileProvider.getUriForFile(AppController.getInstance(), "com.telebroad.teleconsole.fileprovider", file);
        shareIntent.setDataAndType(uriForFile, getMimeType());
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriForFile);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }
    private String getMimeType(){
        String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(url);
        if("jfif".equals(fileExtensionFromUrl)){
            return "image/jpeg";
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        //return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionFromUrl);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.mms_open, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
    private void picInPicMode(){
        Rational aspectRation = new Rational(binding.chatVideo.getWidth(),binding.chatVideo.getHeight());
        picInPicParams.setAspectRatio(aspectRation);
        enterPictureInPictureMode(picInPicParams.build());
    }

    @Override
    protected void onUserLeaveHint() {
        picInPicMode();
        super.onUserLeaveHint();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
            finishAndRemoveTask();
        }
        if (isInPictureInPictureMode){
            actionBar.hide();
        }else {
            actionBar.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (binding.chatVideo.isPlaying()){
            binding.chatVideo.stopPlayback();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        url = null;
        if (binding.chatVideo.isPlaying()){
            if (isInPictureInPictureMode()) {
                //binding.chatVideo.
                //overridePendingTransition(0,R.anim.slide_out_bottom);
            } else {
                binding.chatVideo.pause();
            }
        }
    }
    public boolean HasSupport(){
        if ( packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (!isInPictureInPictureMode() && HasSupport() && binding.chatVideo.isPlaying()){
            picInPicMode();
        }
        else {
            super.onBackPressed();
            finish();
        }
    }

}