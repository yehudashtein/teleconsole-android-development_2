package com.telebroad.teleconsole.controller.dashboard;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.controller.dashboard.ChatActivity.CURRENT_CHAT_EXTRA;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.DelMessage;
import com.telebroad.teleconsole.chat.client.PubMassage3;
import com.telebroad.teleconsole.chat.client.ReplacePub;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.chat.viewModels.ChatMessageViewModel;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.db.models.Attachments;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ChatImageActivity extends AppCompatActivity {

    public static final String MMS_IMAGE_URL = "com.telebroad.teleconsole.controller.mms.image.view.activity.mms.image.urlString";
    private String urlString = "";
    private URL url;
    private List<String> urls = new ArrayList<>();
    private final List<PubMassage3.Attachments> attachmentsList1 = new ArrayList<>();
    private ImageView imageView;
    private EditText editText;
    private ConstraintLayout constraintLayout;
    private ChatMessageViewModel chat;
    private final Gson gson = new Gson();
    private String attachments;
    private Replies replies;
    private String path;


    private static final String MMS_IMAGE_FOLDER = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "MMS Images";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_view);
        urlString = getIntent().getStringExtra(MMS_IMAGE_URL);
        constraintLayout = findViewById(R.id.relEtLayout);
        editText = findViewById(R.id.editTextImage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            chat= getIntent().getSerializableExtra("originalMassage",ChatMessageViewModel.class);
            String repliesList = getIntent().getStringExtra("repliesCollectionList");
            replies = gson.fromJson(repliesList,new TypeToken<Replies>() {}.getType());
            attachments = getIntent().getStringExtra("replyAttachments");
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("originalMassage")) {
                Object messageObject = extras.get("originalMassage");
                if (messageObject instanceof ChatMessageViewModel) {
                    chat = (ChatMessageViewModel) messageObject;
                    String repliesList = getIntent().getStringExtra("repliesCollectionList");
                    replies = gson.fromJson(repliesList,new TypeToken<Replies>() {}.getType());
                    attachments = getIntent().getStringExtra("replyAttachments");
                }
            }
        }
        ImageView imageViewCancel = findViewById(R.id.cancelEdit);
        ImageView imageViewConfirmEdit = findViewById(R.id.confirmEdit);
        imageViewConfirmEdit.setOnClickListener(v -> {
            if (chat != null){
                String attachments = gson.toJson(chat.getAttachments());
                Type collectionType = new TypeToken<List<ReplacePub.Attachments>>() {}.getType();
                List<ReplacePub.Attachments> enums = gson.fromJson(attachments, collectionType);
                ReplacePub.Head head = new ReplacePub.Head("text/*",chat.getSeq(),enums);
                ReplacePub pubMessage = new ReplacePub("replace",chat.getTopic(),SettingsHelper.getString(SettingsHelper.MY_TOPIC),true,editText.getText().toString(), head);
                ChatWebSocket.getInstance().sendObject("pub",pubMessage);
                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().UpdateContent(editText.getText().toString(),chat.getSeq(),chat.getTopic()));
                constraintLayout.setVisibility(View.GONE);
            }else {
                Type collectionType = new TypeToken<List<ReplacePub.Attachments>>() {}.getType();
                List<ReplacePub.Attachments> enums = gson.fromJson(attachments, collectionType);
                ReplacePub.Head head = new ReplacePub.Head("text/*",replies.getSeq(),enums);
                ReplacePub pubMessage = new ReplacePub("replace",replies.getTopic(),SettingsHelper.getString(SettingsHelper.MY_TOPIC),true,editText.getText().toString(), head);
                ChatWebSocket.getInstance().sendObject("pub",pubMessage);
                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().UpdateContent(editText.getText().toString(),replies.getSeq(),replies.getTopic()));
                constraintLayout.setVisibility(View.GONE);
            }

        });
        imageViewCancel.setOnClickListener(v -> {
            if (constraintLayout.getVisibility() == View.VISIBLE){
                constraintLayout.setVisibility(View.GONE);
            }
        });
        imageView = findViewById(R.id.mmsImage);
        supportPostponeEnterTransition();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        if (chat == null){
            getMenuInflater().inflate(R.menu.chat_video_reply, menu);
            ImageView iv = menu.findItem(R.id.saveIm).getActionView().findViewById(R.id.download_iv);
            if (iv != null){
                downloadAnim = (AnimationDrawable) iv.getDrawable();
            }
            View downloadMenuView = menu.findItem(R.id.saveIm).getActionView();
            downloadMenuView.setOnClickListener(v -> {
                saveImage();
                BitmapDrawable bitmapDrawable =(BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                saveImageToGallery(bitmap);
            });
        }else {
            getMenuInflater().inflate(R.menu.chat_image_menu, menu);
            View downloadMenuView = menu.findItem(R.id.saveIm).getActionView();
            downloadMenuView.setOnClickListener(v -> {
                saveImage();
                BitmapDrawable bitmapDrawable =(BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                saveImageToGallery(bitmap);
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishAfterTransition();
                finish();
                break;
            case R.id.imageEdit:
                if (chat!= null){
                    constraintLayout.setVisibility(View.VISIBLE);
                    editText.setText(SettingsHelper.reformatHTML(chat.getText()));
                }else {
                    constraintLayout.setVisibility(View.VISIBLE);
                    editText.setText(SettingsHelper.reformatHTML(replies.getContent()));
                }
                break;
            case R.id.imageDelete:
                if (chat != null){
                    String attachments = gson.toJson(chat.getAttachments());
                    Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
                    Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
                    if (enums.size() == 1) {
                        List<DelMessage.Delseq> delseqList = new ArrayList<>();
                        DelMessage.Delseq delseq = new DelMessage.Delseq(chat.getSeq());
                        delseqList.add(delseq);
                        DelMessage delMessage = new DelMessage("delete", chat.getTopic(), "msg", true, delseqList);
                        ChatWebSocket.getInstance().sendObject("del", delMessage);
                        for (DelMessage.Delseq d : delseqList) {
                            ListenableFuture<Boolean> hasMessageWithSeq = ChatDatabase.getInstance().chatMessageDao().hasMessageWithSeq(d.getLow(), chat.getTopic());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                Futures.addCallback(hasMessageWithSeq,
                                        new FutureCallback<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean result) {
                                                if (result) {
                                                    Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteMassage(chat.getTopic(), d.getLow()));
                                                    Intent intent = new Intent(ChatImageActivity.this, ChatActivity.class);
                                                    String topic = chat.getTopic();
                                                    intent.putExtra(CURRENT_CHAT_EXTRA, topic);
                                                    startActivity(intent);
                                                    finishAfterTransition();
                                                } else {
                                                    Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteMassage(chat.getTopic(), d.getLow()));
                                                    Intent intent = new Intent(ChatImageActivity.this, ChatActivity.class);
                                                    String topic = chat.getTopic();
                                                    intent.putExtra(CURRENT_CHAT_EXTRA, topic);
                                                    startActivity(intent);
                                                    finishAfterTransition();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {}
                                        }, AppController.getInstance().getMainExecutor());
                            }
                        }
                    }else {
                        for (Attachments a: enums) {
                            if (!a.getPath().equals(path)){
//                                String urlEnd = a.getPath().substring(a.getPath().lastIndexOf(".") + 1);
//                                PubMassage3.Attachments attachmentsReplace = new PubMassage3.Attachments("img_" + SettingsHelper.getDateString() + "." + urlEnd, a.getPath(), "image/" + urlEnd, ctr.getParams().getExpires(), false);
//                                attachmentsList1.add(attachmentsReplace);
//                                urls.add(ctr.getParams().getUrl());
//                                head1 = PubMassage3.setHeadWithAttachments(attachmentsList1);
                            }

                        }
                    }
                }else {
                    List<DelMessage.Delseq> delseqList = new ArrayList<>();
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
                                                Intent intent = new Intent(ChatImageActivity.this,ChatReplyActivity.class);
                                                String topic = replies.getTopic();
                                                intent.putExtra(CURRENT_CHAT_EXTRA,topic);
                                                startActivity(intent);
                                                finishAfterTransition();
                                            } else {
                                                Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteMassage(replies.getTopic(), d.getLow()));
                                                Intent intent = new Intent(ChatImageActivity.this,ChatReplyActivity.class);
                                                String topic = replies.getTopic();
                                                intent.putExtra(CURRENT_CHAT_EXTRA,topic);
                                                startActivity(intent);
                                                finishAfterTransition();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable t) {}
                                    }, AppController.getInstance().getMainExecutor());
                        }
                    }
                }
                break;
            case R.id.imageReply:
               Intent intent = new Intent(this,ChatReplyActivity.class);
               intent.putExtra("originalMassage",chat);
               intent.putExtra(MMS_IMAGE_URL,urlString);
               startActivity(intent);
                break;
            case R.id.imageForward:
                if (chat != null){
                    Intent intent1 = new Intent(this,ChatForwardActivity.class);
                    intent1.putExtra("topic",chat.getTopic());
                    intent1.putExtra("messageObject",chat);
                    startActivity(intent1);
                }else {
                    String replies1 = gson.toJson(replies);
                    Intent intent1 = new Intent(this,ChatForwardActivity.class);
                    intent1.putExtra("topic",replies.getTopic());
                    intent1.putExtra("messageObject",replies1);
                    startActivity(intent1);}
                break;
            case R.id.saveIm:
                BitmapDrawable bitmapDrawable =(BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                try {
                    Bitmap bitmap1 = Glide
                            .with(this)
                            .asBitmap()
                            .load(urlString)
                            .submit()
                            .get();
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
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"image.jpg");
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
        }else{AsyncTask.execute(() -> {
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
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriForFile);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
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
        }else {
            fileName = urlFile.substring(urlFile.lastIndexOf("/"));
        }
        return new File(fileDir.getAbsoluteFile() + File.separator + fileName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(this,ChatActivity.class);
//        intent.putExtra(ChatActivity.CURRENT_CHAT_EXTRA, replies.getTopic());
//        startActivity(intent);
        finishAfterTransition();
        finish();
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
