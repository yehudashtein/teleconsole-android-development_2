package com.telebroad.teleconsole.controller;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.USE_SIP;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.androidnetworking.utils.Utils.getMimeType;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.model.Message.Direction.IN;
import static com.telebroad.teleconsole.model.Message.Direction.OUT;
import static com.telebroad.teleconsole.model.SMS.MMSMedia.compressImage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.fxn.pix.Pix;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.databinding.ActivitySmsConversationBinding;
import com.telebroad.teleconsole.databinding.ItemIncomingSmsBinding;
import com.telebroad.teleconsole.databinding.ItemOutgoingMmsBinding;
import com.telebroad.teleconsole.databinding.LayoutSendText1Binding;
import com.telebroad.teleconsole.databinding.MmsImagesBinding;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.DlrUpdate;
import com.telebroad.teleconsole.model.Line;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.PubnubInfo;
import com.telebroad.teleconsole.model.SMS;
import com.telebroad.teleconsole.model.Settings;
import com.telebroad.teleconsole.model.TeleConsoleProfile;
import com.telebroad.teleconsole.model.repositories.SMSRepository;
import com.telebroad.teleconsole.notification.NotificationBuilder;
import com.telebroad.teleconsole.notification.VoicemailPlayingService;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.viewmodels.ConversationListViewModel;
import com.telebroad.teleconsole.viewmodels.ConversationListViewModelFactory;
import com.telebroad.teleconsole.viewmodels.ConversationViewModel;
import com.telebroad.teleconsole.viewmodels.MMSLinksViewModels;
import com.telebroad.teleconsole.viewmodels.VoicemailViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import team.clevel.documentscanner.helpers.ScannerConstants;


public class SmsConversationActivity extends AppCompatActivity {
    private boolean isPlaying, audioHasStarted = false, audioHasFinished = false;
    private static final String EXTRA_MY_NUMBER = "com.telebroad.teleconsole.controller.SmsConversationActivity.extra.my.email";
    private static final String EXTRA_OTHER_NUMBER = "com.telebroad.teleconsole.controller.SmsConversationActivity.extra.other.email";
    private static final String EXTRA_NEW_SMS = "com.telebroad.teleconsole.controller.SmsConversationActivity.extra.new.sms";
    private static final String EXTRA_IMAGE_URI = "com.telebroad.teleconsole.controller.SmsConversationActivity.extra.image.uri";
    private static final int PICK_PICTURE_REQUEST = 100;
    private SMS.MMSMedia media;
    private static final String MMS_IMAGE_FOLDER = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "MMS VoiceNotes";
    private Handler seekBarHandler;
    private ArrayList<String> SpeedT;
    private Float SpeedState;
    private int i = 0;
    private int fromOffSet = 0;
    private SizeLimitedRecorder sizeLimitedRecorder;
    private Runnable updateSeekbar;
    private String url = "";
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private VoicemailViewModel voicemailViewModel;
    private MediaBrowserCompat mediaBrowser;
    private RecyclerView recyclerView;
    private EditText newText;
    private FloatingActionButton sendButton;
    private ImageView previewImage, cancelImage;
    private String myNumber;
    private String otherNumber;
    private PhoneNumber otherPhoneNumber;
    private ConversationListViewModel listViewModel;
    private final boolean isFiltering = false;
    private String imageToSend;
    private int contactCount = 0;
    private ImageView currentPlay;
    private List<? extends Contact> matchedContacts;
    private boolean isBlocked = false;
    private SeekBar seekBar;
    private TextView textView;
    private LayoutSendText1Binding sendTextBinding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Bitmap bitmap;
    private ConversationAdapter adapter;
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (currentPlay != null) {
                if (state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
                } else if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    handler.post(updateProgress);
                    currentPlay.setImageResource(R.drawable.ic_outline_pause_blue);
                } else if (state.getState() != PlaybackStateCompat.STATE_PAUSED) {
                    handler.removeCallbacks(updateProgress);
                    currentPlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_blue);
                    changePlayback(false);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {super.onMetadataChanged(metadata);handleMetaData(metadata);}
    };
    private final Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 200);
            long longProgress = MediaControllerCompat.getMediaController(SmsConversationActivity.this).getPlaybackState().getPosition();
            seekBar.setProgress((int) longProgress);
            textView.setText((Utils.formatLongMilliSeconds(longProgress)));
        }
    };
    public final Consumer<JsonObject> DLR_LISTENER = object -> {
        DlrUpdate dlrupdate = new Gson().fromJson(object, DlrUpdate.class);
        SMSRepository.getInstance().updateDLR(dlrupdate);
        SMSRepository.getInstance().loadConversationFromServer1(myNumber, otherNumber, null,fromOffSet);
        //android.util.Log.d("DLR_Running", "DLR IS RUNNING");
        adapter.updateDLR(dlrupdate);
    };
    public static void show(Activity activity, PhoneNumber myNumber, PhoneNumber otherNumber, SMS newSms) {
        show(activity, myNumber, otherNumber, newSms, null);
    }
    public static void show(Activity activity, PhoneNumber myNumber, PhoneNumber otherNumber, SMS newSms, Uri imageURI) {
        activity = activity == null ? AppController.getInstance().getActiveActivity() : activity;
        if (activity == null){
            Toast.makeText(AppController.getInstance(), "No current activity", Toast.LENGTH_LONG).show();
            return;
        }
        //android.util.Log.d("SMS09", "myNumber " + myNumber + " sms lines " + TeleConsoleProfile.getInstance().getSmsLines());
        if (myNumber == null){
            if (TeleConsoleProfile.getInstance().getSmsLines() == null || TeleConsoleProfile.getInstance().getSmsLines().isEmpty()) {
                NewTextActivity.showNoSMSDialog(activity);
//                Toast.makeText(AppController.getInstance(), "Can't find a valid SMS line", Toast.LENGTH_LONG).show();
                return;
            }
            myNumber = PhoneNumber.getPhoneNumber(TeleConsoleProfile.getInstance().getSmsLines().get(0).getName());
        }
        Intent smsConversationIntent = getIntent(activity, myNumber, otherNumber);
        if (newSms != null) {
            String smsJson = new Gson().toJson(newSms);
            smsConversationIntent.putExtra(EXTRA_NEW_SMS, smsJson);
        }
        if (imageURI != null){
            smsConversationIntent.putExtra(EXTRA_IMAGE_URI, imageURI);
        }
        Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
        activity.startActivity(smsConversationIntent, transitionBundle);
        NotificationBuilder.getInstance().dismissNotification(myNumber.fixed().hashCode() + otherNumber.fixed().hashCode());
    }

    public static void show(Activity activity, PhoneNumber myNumber, PhoneNumber otherNumber) {
        show(activity, myNumber, otherNumber, null, null);
    }

    @NonNull
    public static Intent getIntent(Context activity, PhoneNumber myNumber, PhoneNumber otherNumber) {
        Intent intent = new Intent(activity, SmsConversationActivity.class);
        intent.putExtra(EXTRA_MY_NUMBER, myNumber.fixed());
        intent.putExtra(EXTRA_OTHER_NUMBER, otherNumber.fixed());
        return intent;
    }

    public static void show(Activity activity, PhoneNumber otherNumber) {
        if (TeleConsoleProfile.getInstance().getSmsLines() == null || TeleConsoleProfile.getInstance().getSmsLines().isEmpty()) {
            NewTextActivity.showNoSMSDialog(activity);
//                Toast.makeText(AppController.getInstance(), "Can't find a valid SMS line", Toast.LENGTH_LONG).show();
            return;
        }
        show(activity, Settings.getInstance() == null ? null : PhoneNumber.getPhoneNumber( Settings.getInstance().getDefaultSMSLine()), otherNumber);
    }

    public static void show(Activity activity, String otherNumber) {
        show(activity, PhoneNumber.getPhoneNumber(otherNumber));
    }

    public static void show(Activity activity, String myNumber, String otherNumber) {
        show(activity, PhoneNumber.getPhoneNumber(myNumber), PhoneNumber.getPhoneNumber(otherNumber));
    }
    public static void show(Activity activity, String myNumber, String otherNumber, Uri imageURI) {
        show(activity, new PhoneNumber(myNumber), new PhoneNumber(otherNumber), null, imageURI);
    }
    public static void show(Activity activity, String myNumber, String otherNumber, SMS newSms) {
        show(activity, PhoneNumber.getPhoneNumber(myNumber), PhoneNumber.getPhoneNumber(otherNumber), newSms, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.telebroad.teleconsole.databinding.ActivitySmsConversationBinding activityBinding = ActivitySmsConversationBinding.inflate(getLayoutInflater());
        sendTextBinding = LayoutSendText1Binding.bind(activityBinding.getRoot());
        setContentView(activityBinding.getRoot());
        SpeedT = new ArrayList<>();
        SpeedT.add("1");SpeedT.add("1.5");SpeedT.add("2");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.white)));
        }
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0);
           // getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
        }else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        voicemailViewModel = new VoicemailViewModel();
        myNumber = getIntent().getStringExtra(EXTRA_MY_NUMBER);
        otherNumber = getIntent().getStringExtra(EXTRA_OTHER_NUMBER);
        if (isNullOrEmpty(myNumber) || isNullOrEmpty(otherNumber)) {
            finish();
            return;
        }
        Uri extraPhotoURI = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        handleImageURI(extraPhotoURI);
        //android.util.Log.d("ShareIMG", "extraPhotoURI = " + extraPhotoURI);
        otherPhoneNumber = new PhoneNumber(otherNumber, null);
        otherPhoneNumber.getName(this).observe(this, this::setTitle);
        otherPhoneNumber.getMatchedContacts().observe(this, contacts -> {
            contactCount = contacts == null ? 0 : contacts.size();
            matchedContacts = contacts;
            invalidateOptionsMenu();
        });
        TextView callerID = activityBinding.textCallerID;
        callerID.setText(getResources().getString(R.string.with, PhoneNumber.format(myNumber)));
        String[] availableLines = Line.convertLineListToStringList(TeleConsoleProfile.getInstance().getSmsLines()).toArray(new String[]{});
        sendTextBinding.addImage.setOnClickListener(v -> {
                new PhotoSourceDialog(SmsConversationActivity.this::handleImageURI).show(getSupportFragmentManager(), "chooseSource");
        });
        // If there is only 1 line available, it makes no sense to allow the user to switch lines
        if (availableLines.length > 1) {
            callerID.setOnClickListener(view -> {
                AlertDialog alert= new MaterialAlertDialogBuilder(this).setTitle("Pick a SMS line to send from").
                        setItems(availableLines, ((dialog, which) -> {
                            String newSender = availableLines[which];
                            if (!PhoneNumber.stringEqualsString(newSender, myNumber)) {
                                show(this, availableLines[which], otherNumber, extraPhotoURI);
                            }
                        })).create();
                alert.setOnShowListener(dialog -> {
                    Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                    negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                });alert.show();
            });
        }
        recyclerView = activityBinding.conversationRecycler;//findViewById(R.id.conversationRecycler);
        newText = sendTextBinding.newText; //findViewById(R.id.newText);
        sendButton = sendTextBinding.sendButton; //findViewById(R.id.sendButton);
        int imageResourceIdMic = R.drawable.ic_mic_blue;
        sendButton.setTag(imageResourceIdMic);
        //ImageView smsVoiceNote = sendTextBinding.VoiceNote;
        previewImage = sendTextBinding.imagePreview; //findViewById(R.id.image_preview);
        cancelImage = sendTextBinding.imageCancel;//findViewById(R.id.image_cancel);
        cancelImage.setOnClickListener(v -> {
            previewImage.animate().translationY(previewImage.getHeight()).alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    removePreviewPicture();
                }
            });
        });
        newText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int imageResourceIdSend = R.drawable.ic_send;
                sendTextBinding.sendButton.setImageResource(R.drawable.ic_send);
                sendTextBinding.sendButton.setTag(imageResourceIdSend);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (newText.getText().toString().isEmpty()) {
                    int imageResourceIdMic = R.drawable.ic_mic_blue;
                    sendTextBinding.sendButton.setImageResource(R.drawable.ic_mic_blue);
                    sendTextBinding.sendButton.setTag(imageResourceIdMic);
                }
            }
        });
        sendTextBinding.sendButton.setOnClickListener(v -> {
            int imageResourceId = R.drawable.ic_baseline_stop_circle_blue;
            int imageResourceIdSend = R.drawable.ic_send;
            if (!isRecording && (int)sendButton.getTag()==imageResourceIdMic ){
                sendTextBinding.VisualizerViewSMS.setVisibility(View.VISIBLE);
                sendTextBinding.newText.setHint("");
                sendTextBinding.newText.setCursorVisible(false);
                //sendTextBinding.delete.setVisibility(View.VISIBLE);
                sendTextBinding.recordTimer.setVisibility(View.VISIBLE);
                //sendTextBinding.VoiceNote.setVisibility(View.VISIBLE);
                sendTextBinding.sendButton.setImageResource(R.drawable.ic_baseline_stop_circle_blue);
                sendTextBinding.sendButton.setTag(imageResourceId);
                sendTextBinding.addImage.setVisibility(View.GONE);
                sendTextBinding.recordTimer.setBase(SystemClock.elapsedRealtime());
                sendTextBinding.recordTimer.start();
                //sendTextBinding.VoiceNote.setImageResource(R.drawable.ic_baseline_stop_circle_blue);
                isRecording = true;
                //Utils.asyncTask(() -> {
//                    mediaRecorder.setAudioEncodingBitRate(24_000);
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSamplingRate(16_000);
                    //mediaRecorder.setAudioSamplingRate(22050);
                    mediaRecorder.setAudioChannels(1);
                    mediaRecorder.setAudioEncodingBitRate(32000);
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                    mediaRecorder.setOutputFile(SettingsHelper.getRecordFilePathSMS(getApplicationContext()));
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    handler.post(updateVisualizer);
                    sizeLimitedRecorder = new SizeLimitedRecorder(mediaRecorder,new File(SettingsHelper.getRecordFilePathSMS(getApplicationContext())));
                    sizeLimitedRecorder.startRecording();
//                    try {mediaRecorder.prepare();} catch (IOException e) {e.printStackTrace();}
//                    mediaRecorder.start();
                //});
            }else if (isRecording && (int)sendTextBinding.sendButton.getTag() == imageResourceId){
                new Handler().postDelayed(() -> {
                    if (sizeLimitedRecorder != null) sizeLimitedRecorder.stopRecording();
                    else{
                        sizeLimitedRecorder = new SizeLimitedRecorder(mediaRecorder,new File(SettingsHelper.getRecordFilePathSMS(getApplicationContext())));
                        sizeLimitedRecorder.stopRecording();
                    }
                    }, 200);
                sendTextBinding.sendButton.setImageResource(R.drawable.ic_send);
                sendTextBinding.sendButton.setTag(imageResourceIdSend);
            }else if (!isRecording && (int)sendTextBinding.sendButton.getTag() == imageResourceIdSend){
                if (!isNullOrEmpty(imageToSend)) {
                    sendTextBinding.newTextView.setBackgroundResource(R.drawable.bg_new_text_text);
                    Drawable d = getDrawable(R.drawable.bg_new_text_text);
                    sendTextBinding.newTextView.setBackground(d);
                    String msgData = newText.getText().toString();
                    if (isNullOrEmpty(msgData) && media == null) {
                        return;
                    }
                    long time = System.currentTimeMillis() / SECOND_IN_MILLIS;
                    SMS newSms = new SMS(time, otherNumber, myNumber, newText.getText().toString(), OUT);
                    //android.util.Log.d("NEWTEXT01", "sent " + time);
                    newSms.setIsNew(0);
                    ArrayList<SMS.MMSMedia> media = new ArrayList<>();
                    if (this.media != null) {
                        media.add(this.media);
                    }
                    if (!isNullOrEmpty(imageToSend)) {
                        ArrayList<String> mediaURLs = new ArrayList<>();
                        mediaURLs.add(Uri.fromFile(new File(imageToSend)).toString());
                        newSms.setMedia(mediaURLs);
                    }
                    removePreviewPicture();
                    newSms.setSending(true);
                    if (PubnubInfo.getInstance() != null){
                        Line line = PubnubInfo.getInstance().getLine(PhoneNumber.fix(myNumber));
                        if (line != null && line.getPubnub_channel() != null && !line.getPubnub_channel().isEmpty()) {
                            PubnubInfo.getInstance().addListener(line.getPubnub_channel(), DLR_LISTENER);
                        }
                    }
                    newSms.send(media, result -> handleResult(newSms, result), this::handleError);
                    bitmap = null;
                    Intent intent = getIntent();
                    cancelImage.setVisibility(View.GONE);
                    imageToSend = null;
                    compressed = false;
                    if (intent != null) {
                        intent.putExtra(EXTRA_IMAGE_URI, (Parcelable) null);
                    }
                    mediaRecorder = null;
                    newText.setText("");
                    //sendTextBinding.newTextView.setBackgroundColor(getResources().getColor(R.color.transp));
                    //.setColorFilter(ContextCompat.getColor(this,R.color.transp));
                }else if (mediaRecorder != null){
                    File file = new File(SettingsHelper.getRecordFilePathSMS(getApplicationContext()));
                    //Log.d("FileSize", file.length()/1024+"");
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] mp3Bytes = new byte[(int) file.length()];
                        fileInputStream.read(mp3Bytes);
                        fileInputStream.close();
                        byte[] base64Bytes = Base64.encode(mp3Bytes, Base64.DEFAULT);
                        //Log.d("FileSize", new String(base64Bytes));
                        ArrayList<SMS.MMSMedia> media = new ArrayList<>();
                        media.add(new SMS.MMSMedia(file.getName(), MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath())), new String(base64Bytes)));
                        long time = System.currentTimeMillis() / SECOND_IN_MILLIS;
                        SMS newSms = new SMS(time, otherNumber, myNumber, newText.getText().toString(), OUT);
                        newSms.setIsNew(0);
                        if (PubnubInfo.getInstance() != null){
                            Line line = PubnubInfo.getInstance().getLine(PhoneNumber.fix(myNumber));
                            if (line != null && line.getPubnub_channel() != null && !line.getPubnub_channel().isEmpty()) {
                                PubnubInfo.getInstance().addListener(line.getPubnub_channel(), DLR_LISTENER);
                            }
                        }
                        if (!isNullOrEmpty(file.getAbsolutePath())) {
                            ArrayList<String> mediaURLs = new ArrayList<>();
                            mediaURLs.add(Uri.fromFile(new File(file.getAbsolutePath())).toString());
                            newSms.setMedia(mediaURLs);
                        }
                        newSms.send(media, result -> handleResult(newSms, result), this::handleError);
                        media.clear();
                        newText.setText("");
                        sendTextBinding.constraintLayout3.setVisibility(View.GONE);
                        ClearViews();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    sendTextBinding.newTextView.setBackgroundResource(R.drawable.bg_new_text_text);
                    Drawable d = AppCompatResources.getDrawable(this,R.drawable.bg_new_text_text);
                    sendTextBinding.newTextView.setBackground(d);
                    String msgData = newText.getText().toString();
                    if (isNullOrEmpty(msgData) && media == null) {
                        return;
                    }
                    long time = System.currentTimeMillis() / SECOND_IN_MILLIS;
                    SMS newSms = new SMS(time, otherNumber, myNumber, newText.getText().toString(), OUT);
                    //android.util.Log.d("NEWTEXT01", "sent " + time);
                    newSms.setIsNew(0);
                    ArrayList<SMS.MMSMedia> media = new ArrayList<>();
                    if (this.media != null) {
                        media.add(this.media);
                    }
                    if (!isNullOrEmpty(imageToSend)) {
                        ArrayList<String> mediaURLs = new ArrayList<>();
                        mediaURLs.add(Uri.fromFile(new File(imageToSend)).toString());
                        newSms.setMedia(mediaURLs);
                    }
                    removePreviewPicture();
                    newSms.setSending(true);
                    if (PubnubInfo.getInstance() != null){
                        Line line = PubnubInfo.getInstance().getLine(PhoneNumber.fix(myNumber));
                        if (line != null && line.getPubnub_channel() != null && !line.getPubnub_channel().isEmpty()) {
                            PubnubInfo.getInstance().addListener(line.getPubnub_channel(), DLR_LISTENER);
                        }
                    }
                    newSms.send(media, result -> handleResult(newSms, result), this::handleError);
                    bitmap = null;
                    Intent intent = getIntent();
                    cancelImage.setVisibility(View.GONE);
                    imageToSend = null;
                    compressed = false;
                    if (intent != null) {
                        intent.putExtra(EXTRA_IMAGE_URI, (Parcelable) null);
                    }
                    newText.setText("");
                    ClearViews();
                    mediaRecorder = null;
                }
            }
        });
        sendTextBinding.playChat.setOnClickListener(v -> {
            if (isPlaying) pauseAudio();
            else {
                try {
                    if (audioHasStarted && !audioHasFinished) resumeAudio();
                    else {
                        playAudio();
                        audioHasFinished = false;
                    }
                } catch (IOException e) {e.printStackTrace();}
            }
        });
        sendTextBinding.cancelImg.setOnClickListener(v -> {
            sendTextBinding.constraintLayout3.animate().translationY(sendTextBinding.constraintLayout3.getHeight()).alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);
                    ClearViews();
                    sendTextBinding.constraintLayout3.setAlpha(1.0f);
                    sendTextBinding.constraintLayout3.animate().translationY(0);

                }
            });
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setReverseLayout(true);
        adapter = new ConversationAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(-1)) {
                   fromOffSet+=20;
                    SMSRepository.getInstance(getApplication()).loadConversationFromServer1(myNumber, otherNumber, new Gson().fromJson(getIntent().getStringExtra(EXTRA_NEW_SMS), SMS.class),fromOffSet);
                }
            }
        });
        SMSRepository smsRepository = SMSRepository.getInstance(getApplication());
//        Log.d("letsSee1",myNumber);
//        Log.d("letsSee1",otherNumber);
        smsRepository.loadConversationFromServer1(myNumber, otherNumber, new Gson().fromJson(getIntent().getStringExtra(EXTRA_NEW_SMS), SMS.class),fromOffSet);
//        smsRepository.getConversation(myNumber, otherNumber).observe(this, adapter::setConversation);
        listViewModel = ViewModelProviders.of(this, new ConversationListViewModelFactory(myNumber, otherNumber)).get(ConversationListViewModel.class);
        listViewModel.conversationList.observe(this, adapter::setConversation);
        listViewModel.isBlocked.observe(this, this::updateBlockStatus);
//        listViewModel.conversationList.observe(this, results -> {
//            if (results == null) {
//                android.util.Log.d("Test0001", "result not yet ready");
//
//                return;
//            }
//            android.util.Log.d("Test0001", "Successful reload " + results.size());
//        });
    }
    private void updateBlockStatus(Boolean blocked) {
      //  android.util.Log.d("SMS_BLOCK", "is sms blocked? " + blocked);
        if (blocked == null){
            this.isBlocked = false;
        }else {
            this.isBlocked = blocked;
        }
        runOnUiThread(() ->{
                invalidateOptionsMenu();
                updateSendTextUI();
        });
    }


    private void updateSendTextUI() {
        if (sendTextBinding == null){
            return;
        }
        if (isBlocked){
            //sendTextBinding.imageGroup.setVisibility(View.GONE);
            //sendTextBinding.sendTextGroup.setVisibility(View.GONE);
        }else{
            //sendTextBinding.sendTextGroup.setVisibility(View.VISIBLE);
            if (imageToSend != null){
                //sendTextBinding.imageGroup.setVisibility(View.VISIBLE);
            }
        }
    }

    private void handleImageURI(Uri extraPhotoURI) {
        if (extraPhotoURI != null){
            try {
                InputStream inputStream = getContentResolver().openInputStream(extraPhotoURI);
                BitmapFactory.decodeStream(inputStream);
                bitmap = Utils.getBitmapFromURI(getContentResolver(), extraPhotoURI);
                File file = File.createTempFile( "data", ".jpg", getFilesDir());
                imageToSend = file.getAbsolutePath();
                AsyncTask.execute(() -> {
                    try {
                        FileOutputStream stream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        stream.close();
                        inputStream.close();
                        compressed = true;
                        updateImageURI();
                    } catch (IOException e) {e.printStackTrace();}
                });
            } catch (IOException e) {e.printStackTrace();}
        }
    }

    private void handleResult(SMS newSMS, JsonElement result){
        newSMS.setSending(false);
       // android.util.Log.d("NewSMSresult", result.getAsString());
        this.adapter.updateLast();
    }
    private void handleError(TeleConsoleError error){
       // android.util.Log.e("Sending", error.getFullErrorMessage());
    }
    private void removePreviewPicture() {
        sendTextBinding.newTextView.setBackgroundResource(R.drawable.bg_new_text_text);
        Drawable d = AppCompatResources.getDrawable(this,R.drawable.bg_new_text_text);
        sendTextBinding.newTextView.setBackground(d);
        previewImage.clearAnimation();
        previewImage.setImageDrawable(null);
        previewImage.setVisibility(View.GONE);
        cancelImage.animate().alpha(0.0f);
        imageToSend = null;
        cancelImage.setVisibility(View.GONE);
        sendTextBinding.previewSpace.setVisibility(View.GONE);
        media = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sms_conversation, menu);
        MenuItem contactMenuItem = menu.findItem(R.id.viewContact);
        //contactMenuItem.setb
        if (contactCount > 0){
            contactMenuItem.setTitle(R.string.view_contact);
            contactMenuItem.setIcon(R.drawable.ic_person_white);
        }else{
            contactMenuItem.setTitle(R.string.add_contact);
            contactMenuItem.setIcon(R.drawable.ic_person_add);
        }
        MenuItem blockItem = menu.findItem(R.id.blockContact);
        if (isBlocked){
            blockItem.setTitle(R.string.unblock_number);
        }else {
            blockItem.setTitle(R.string.block_number);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_PICTURE_REQUEST) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            if(returnValue == null){
                return;
            }
            if (!returnValue.isEmpty()) {
                imageToSend = returnValue.get(0);
                Glide.with(this).load(Uri.fromFile(new File(imageToSend))).into(previewImage);
                updateImage();
            }
        }
    }

    private void updateImageURI(){
        if (resumed && compressed){
            runOnUiThread(this::updateImage);
        }
    }
    boolean resumed = false;
    boolean compressed = false;

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        updateImageURI();
    }

    private void updateImage() {
        if (!isNullOrEmpty(imageToSend)) {
            cancelImage.setVisibility(View.VISIBLE);
            cancelImage.setAlpha(1.0f);
            sendTextBinding.previewSpace.setVisibility(View.VISIBLE);
            previewImage.setVisibility(View.VISIBLE);
            previewImage.setAlpha(1.0f);
            previewImage.setTranslationY(0);
            previewImage.setImageBitmap(bitmap);
            int imageResourceIdSend = R.drawable.ic_send;
            sendTextBinding.sendButton.setImageResource(R.drawable.ic_send);
            sendTextBinding.sendButton.setTag(imageResourceIdSend);
            //sendTextBinding.newTextView.getBackground().setAlpha(45);
            @SuppressLint("ShowToast")
            Toast compToast = Toast.makeText(this, "Preparing Image", Toast.LENGTH_LONG);
            Timer compTimer = new Timer();
            compTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(compToast::show);
                }
            }, 0, 1000);
            //compSnack.show();
            sendButton.setEnabled(false);
            AsyncTask.execute(() -> {
                try {
                    media = compressImage(imageToSend, error -> {
                        runOnUiThread(() -> {
                            compTimer.cancel();
                            compToast.cancel();
                            sendButton.setEnabled(true);
                        });
                        if (isNullOrEmpty(error)) {
                            //android.util.Log.d("Compress01", "Success");
                        } else {
                            //android.util.Log.e("Compress01", "Error compressing " + error);
                        }
                    }, bitmap -> {
                        runOnUiThread(() -> {
                            sendButton.setEnabled(true);
                            compTimer.cancel();
                            compToast.cancel();
                            previewImage.setImageBitmap(bitmap);
                        });
                    });
                } catch (FileNotFoundException e) {e.printStackTrace();}
            });
        }
    }

    private File createImageFile(Uri uri) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ScannerConstants.selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
        // Save a file: path for use with ACTION_VIEW intents
        //currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.delete_convo_title).setMessage(R.string.delete_convo_message)
                    .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
                        //repository.deleteVoicemail(voicemailViewModel.getItem());
                       // Log.d("Delete01", "Runnning");
                        listViewModel.delete();
                        finish();
                    }).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();

        } else if (item.getItemId() == R.id.call) {
            if (AppController.getInstance().hasPermissions(RECORD_AUDIO, USE_SIP)) {
                SipManager.getInstance(this).call(PhoneNumber.fix(otherNumber), this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, USE_SIP}, 0);
            }
        }else if (item.getItemId() == R.id.viewContact){
            if (otherPhoneNumber == null){
                return false;
            }
            Utils.viewContact(matchedContacts, otherPhoneNumber.fixed(), this);
            return true;
        }else if (item.getItemId() == R.id.mark_unread){
                HashMap<String, String> params = new HashMap<>();
                params.put(URLHelper.KEY_TYPE, "sms");
                params.put(URLHelper.KEY_LINE, PhoneNumber.fix(myNumber));
                SMS lastSMS = adapter.getLastSMS();
                params.put("ids", lastSMS.getId() + "," + otherPhoneNumber.fixed());
               // android.util.Log.d("UNREAD", params.toString());
                URLHelper.request(Request.Method.PUT, URLHelper.UNREAD_URL, params, result -> {
                    Utils.asyncTask(() -> {
                        SMSRepository.getInstance().setSMSUnread(lastSMS.getTimestamp());
                    });
                   // android.util.Log.d("UNREAD", "result is " + result.toString());
                }, error -> {
                    //android.util.Log.d("UNREAD", "error is " + error.getFullErrorMessage());
                });
        }else if (item.getItemId() == R.id.blockContact){
            if (isBlocked){
                URLHelper.request(Request.Method.DELETE, URLHelper.UNBLOCK_SMS_URL, getBlockListParams(), result -> {
                    Toast.makeText(this, "Number Unblocked Successfully", Toast.LENGTH_LONG).show();
                    listViewModel.setBlocked(false);
                          //  android.util.Log.d("SMSBLOCK", result.toString());
                    }, error -> {
                            Toast.makeText(this, "Failed to block number " + error.getErrorMessage(), Toast.LENGTH_LONG).show();
                            //android.util.Log.d("SMSBLOCK", error.getFullErrorMessage());
                        });
                // TODO call unblock api
                listViewModel.setBlocked(false);
            }else{
                AlertDialog alert = new MaterialAlertDialogBuilder(this)
                        .setTitle("Block Number?")
                        .setMessage("You will no longer receive messages from the number " + otherPhoneNumber.formatted() + " to the SMS number " + PhoneNumber.format(myNumber) + ".")
                        .setPositiveButton("Block", (dialog, which) -> {
                            // TODO call block API
                            HashMap<String, String> params = getBlockListParams();
                            params.put(URLHelper.KEY_REASON, "Blocked by Android app");
                            //recipient
                            URLHelper.request(Request.Method.POST, URLHelper.BLOCK_SMS_URL, params, result -> {
                                Toast.makeText(this, "Number Blocked Successfully", Toast.LENGTH_LONG).show();
                                listViewModel.setBlocked(true);
                               // android.util.Log.d("SMSBLOCK", result.toString());
                            }, error -> {
                                Toast.makeText(this, "Failed to block number " + error.getErrorMessage(), Toast.LENGTH_LONG).show();
                                //android.util.Log.d("SMSBLOCK", error.getFullErrorMessage());
                            });
                        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
                alert.setOnShowListener(dialog -> {
                    Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                    negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                });alert.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private HashMap<String, String> getBlockListParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_SMSLINE, PhoneNumber.fix(myNumber));
        params.put(URLHelper.KEY_RECIPIENT, otherPhoneNumber.fixed());
        return params;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0){
            SipManager.getInstance(this).call(PhoneNumber.fix(otherNumber));
        }
//        for (int result : grantResults) {
//            if (result != PERMISSION_GRANTED) {
//                // TODO alert user of permission error
////                return;
//            }
//        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public String getMyNumber() {return myNumber;}

    public String getOtherNumber() {return otherNumber;}

    class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> implements SmsConversationActivity.OnChildItemClickListener {
        private final static int OUTGOING_VIEW = 1;
        private final static int INCOMING_VIEW = 2;
        private List<SMS> messages = new ArrayList<>();
        private final List<ConversationViewModel> messageModels = new ArrayList<>();
        private final List<ConversationViewModel> filteredMessageModels = new ArrayList<>();
        private final List<SMS> filteredMessages = new ArrayList<>();
        private final List<ConversationViewModel> selectedMessageModels = new ArrayList<>();
        private boolean isSelectedMode = false;
        private ActionMode selectedMode;
        private MenuItem copyItem;
        private MenuItem share;
        private MenuItem download;
        private final ActionMode.Callback selectedModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.sms_selected, menu);
                copyItem = menu.findItem(R.id.copy);
                share = menu.findItem(R.id.share);
                download = menu.findItem(R.id.download);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete) {
                    String suffix = selectedMessageModels.size() == 1 ? "" : "s";
                    AlertDialog alert = new MaterialAlertDialogBuilder(SmsConversationActivity.this)
                            .setTitle("Delete " + selectedMessageModels.size() + " Message" + suffix + "?")
                            .setMessage("Are you sure you want to delete " + selectedMessageModels.size() + " message " + suffix + ".")
                            .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                                for (ConversationViewModel model : selectedMessageModels) {
                                    //notifyItemRemoved(isFiltering ? filteredMessages.indexOf(model) : messages.indexOf(model));
                                    model.deleteItem();
                                    notifyDataSetChanged();
                                }
                                selectedMode.finish();
                                //notifyDataSetChanged();
                                dialog.dismiss();
                            })).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
                    alert.setOnShowListener(dialog -> {
                        Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                        negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                    });alert.show();
                } else if (item.getItemId() == R.id.copy) {
                    String toCopy = "";
                    if (selectedMessageModels.size() == 1) {
                        toCopy = selectedMessageModels.get(0).getMsgData().toString();
                    } else {
                        // Future support for multi-message copy
                    }
                    if (!toCopy.isEmpty()) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(null, toCopy);
                        clipboard.setPrimaryClip(clip);
                    }
                } else if (item.getItemId() == R.id.share) {
                    if (selectedMessageModels.stream().allMatch(model -> model.isWav())) {
                        for (ConversationViewModel conversationViewModel : selectedMessageModels) {
                            String urlString = conversationViewModel.getMMSImage();
                            try {
                                share(urlString);
                            } catch (MalformedURLException e) {e.printStackTrace();}
                        }
                    }
                } else if (item.getItemId() == R.id.download) {
                    if (selectedMessageModels.stream().allMatch(model -> model.isWav())) {
                        for (ConversationViewModel conversationViewModel : selectedMessageModels) {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(conversationViewModel.getMMSImage()));
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setTitle("Downloading File"); // Set the title of the download notification
                            request.setDescription("Downloading"); // Set the description of the download notification
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, System.currentTimeMillis() + ".mp3"); // Set the destination path and file name
                            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            long id = downloadManager.enqueue(request);
                            BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                                    if (referenceId == id) {
                                        DownloadManager.Query query = new DownloadManager.Query();
                                        query.setFilterById(id);
                                        Cursor cursor = downloadManager.query(query);
                                        if (cursor.moveToFirst()) {
                                            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                            if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                                                String uriString = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                                                File file = null;
                                                try {
                                                    file = getFile(new File(MMS_IMAGE_FOLDER),conversationViewModel.getMMSImage());
                                                } catch (MalformedURLException e) {e.printStackTrace();}
                                                Uri downloadFileUri = FileProvider.getUriForFile(AppController.getInstance(), "com.telebroad.teleconsole.fileprovider", file);
                                                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                                                openIntent.setDataAndType(downloadFileUri, getMimeType(uriString));
                                                openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "my_channel_id")
                                                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                                        .setContentTitle("Download Complete")
                                                        .setContentText("Tap to open")
                                                        .setContentIntent(contentIntent)
                                                        .setAutoCancel(true);
                                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                notificationManager.notify(0, builder.build());
                                            }
                                        }
                                        cursor.close();
                                    }
                                }
                            };
                            registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        }
                    }
                }
                return false;
            };

            private void share(String urlString) throws MalformedURLException {
                File file = getFile(new File(MMS_IMAGE_FOLDER),urlString);
                if (file.exists()){
                    shareFile(file,urlString);
                }else{
                    AsyncTask.execute(() -> {
                        if(!isNullOrEmpty(urlString)){
                            try {
                                downloadImage(null  ,() -> shareFile(file,urlString),urlString);
                            } catch (MalformedURLException e) {e.printStackTrace();}
                        }
                    });
                }
            }
            private void downloadImage(Runnable onStart, Runnable onComplete,String urlString) throws MalformedURLException {
                URL url = new URL(urlString);
                File fileDir = new File(MMS_IMAGE_FOLDER);
                //noinspection ResultOfMethodCallIgnored
                fileDir.mkdirs();
                final File file = getFile(fileDir,urlString);
                try {
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
            private void shareFile(File file,String urlString) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                Uri uriForFile = FileProvider.getUriForFile(AppController.getInstance(), "com.telebroad.teleconsole.fileprovider", file);
                shareIntent.setDataAndType(uriForFile,MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(urlString)));
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //android.util.Log.e("Test", "Test1");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriForFile);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
               // android.util.Log.e("Test", "Test2");
            }
            private File getFile(File fileDir,String urlString) throws MalformedURLException {
                URL url = new URL(urlString);
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
            public void onDestroyActionMode(ActionMode mode) {
                setSelectedMode(false);
                updateItemBackground();
                selectedMessageModels.clear();
                }
        };
        public void updateItemBackground() {
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = recyclerView.getChildAt(i);
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(childView);
                viewHolder.itemView.setBackgroundResource(R.color.clear);
            }
        }

        private int reloadtimes = 0;

        private void update(ConversationViewModel update) {
            if (selectedMessageModels.contains(update)) {
                selectedMessageModels.remove(update);
                if (selectedMessageModels.isEmpty() && selectedMode != null) {
                    setSelectedMode(false);
                }
            } else {
                if (selectedMessageModels.isEmpty()) {
                    setSelectedMode(true);
                }
                selectedMessageModels.add(update);
            }
            download.setVisible(selectedMessageModels.stream().allMatch(model -> model.isWav()));
            share.setVisible(selectedMessageModels.stream().allMatch(model -> model.isWav()));
            copyItem.setVisible(selectedMessageModels.size() == 1 && selectedMessageModels.get(0).isCopyable());
            if (selectedMode != null) {
                selectedMode.setTitle(selectedMessageModels.size() + " Selected");
            }
        }

        private void updateDLR(DlrUpdate update){
            if ((isFiltering && filteredMessageModels.isEmpty()) || (!isFiltering && messageModels.isEmpty())){
                return;
            }
            int position = isFiltering ? filteredMessageModels.size() - 1 : messageModels.size() - 1;
            ConversationViewModel model = isFiltering ? filteredMessageModels.get(0): messageModels.get(0);
            model.updateDLR(update);
            //android.util.Log.d("DLR_PN_SMS", "updating DLR for msgData " + model.getMsgData() + " pos " + position);
            runOnUiThread(() -> notifyItemChanged(position));
        }

        private void updateLast(){
            if ((isFiltering && filteredMessageModels.isEmpty()) || (!isFiltering && messageModels.isEmpty())){
                return;
            }
            int position = isFiltering ? filteredMessageModels.size() - 1 : messageModels.size() - 1;
            ConversationViewModel model = isFiltering ? filteredMessageModels.get(0): messageModels.get(0);
            model.setSent();
            //android.util.Log.d("Sending", model.getID() + " sending " + model.getItem().isSending());
            runOnUiThread(() -> notifyItemChanged(position));
//            notifyItemChanged(position);
//            notifyDataSetChanged();
        }

        private SMS getLastSMS(){
            return messages.get(0);
        }
        private void setSelectedMode(boolean on) {
            if (isSelectedMode == on) {
                return;
            }
            isSelectedMode = on;
            if (isSelectedMode) {
                selectedMode = (SmsConversationActivity.this.startSupportActionMode(selectedModeCallback));
            } else {
                selectedMode.finish();
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewHolder viewHolder = viewType == INCOMING_VIEW ?
                    new ViewHolder(ItemIncomingSmsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)):
                    new ViewHolder(ItemOutgoingMmsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            return viewHolder;
//            Zoomy.Builder builder = new Zoomy.Builder(SmsConversationActivity.this).target(viewHolder.binding.getRoot().findViewById(R.id.pictureView));
//            builder.enableImmersiveMode(false).register();

        }

        @Override
        public int getItemViewType(int position) {
            if (getCurrent(position).getItem().getDirection() == IN) {
                return INCOMING_VIEW;
            } else {
                return OUTGOING_VIEW;
            }
        }

        private ConversationViewModel getCurrent(int position) {
            return isFiltering ? filteredMessageModels.get(position) : messageModels.get(position);
        }

        private static final String TAG = "yehuda";

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (messages != null) {
                ConversationViewModel viewModel = getCurrent(position);

                holder.setMessageViewModel(viewModel);
                ArrayList<String> media = viewModel.getItem().getMedia();
                if (media != null && media.size() >0 && (!viewModel.areAllNotImages(media)||!viewModel.areAllNotVideos(media))){
                    List<MMSLinksViewModels> mmsLinksViewModels = viewModel.getItem().getMedia().stream().map(MMSLinksViewModels::createInstance).collect(Collectors.toCollection(ArrayList::new));
                    ChildAdapter childAdapter = new ChildAdapter(mmsLinksViewModels,this,position);
                    Log.d(TAG, "1");
                    RecyclerView recyclerView = holder.itemView.findViewById(R.id.pictureView);
                    Log.d(TAG, "2");
                    recyclerView.setLayoutManager(new LinearLayoutManager(SmsConversationActivity.this));
                    Log.d(TAG, "3");
                    recyclerView.setAdapter(childAdapter);
                    Log.d(TAG, "4");

                }

            }

        }

        @Override
        public int getItemCount() {
            if (messages != null) {
                return isFiltering ? filteredMessages.size() : messages.size();
            }
            return 0;
        }

        private void filter(String query) {
//            filteredMessages.clear();
//            for (Message message: m){
//                if (history.getSnumber().contains(query)){
//                    filteredMessages.add(history);
//                }
//            }
//            notifyDataSetChanged();
        }

        public void setConversation(List<SMS> newMessages) {
//            android.util.Log.d("CONVO01", "times reloaded " + ++reloadtimes);
            if (newMessages != null && !newMessages.isEmpty()) {
                // We only want to reload if the new messages are different then the exisiting messages
                // Check if there are different amount of messages now and before, if yes obviously we need to reload, there are either new messages, or messages got deleted (or both in different amounts).
                if (newMessages.size() == this.messages.size()
                        // new messages and existing messages are the same size. This can only happen in two ways,
                        // 1. equal amount of messages get deleted and inserted
                        // 2. nothing changed.
                        // If 1 we need to reload, if 2 we can skip the reloading.
                        // How can we find out if it is 1 or 2?
                        // A new message will always be the newest message, as we cannot (yet) go back in time and send a message
                        // Messages are sorted by time, with the newest message being the first
                        // We can now check if the newest message from the existing messages and the newest message from the new are the same time
                        // If yes it must be that there are no new messages in the new messages and since the email of messages is the same as before there cannot be any deleted messages
                        // Ergo we don't need to reload, so we can just return at this point
                        // WARNING: THIS CODE WILL BREAK IN THE EVENT THAT SOMEONE INVENTS A TIME MACHINE! IF THIS HAPPENS GO BACK IN TIME AND WARN ME TO FIX THIS CODE!!!
                        && newMessages.get(0).getTimeStamp() == this.messages.get(0).getTimeStamp()) {
                    return;
                }
                this.messages = newMessages;
                if (newMessages.isEmpty()) {
                    recyclerView.setBackgroundResource(R.drawable.bg_no_messages);
                } else {
                    //android.util.Log.d("SMSBLOCK", "message blocked? " + newMessages.get(0).isBlocked() );
                    recyclerView.setBackgroundResource(R.drawable.bg_rectangle_white_ripple);
                }
                mapMessagesToModels(newMessages);
                Collections.sort(newMessages);
                notifyDataSetChanged();
            }
        }

        private void mapMessagesToModels(List<? extends SMS> messages) {
            messageModels.clear();
            for (SMS current : messages) {
                if (current.getIdx() > 0){
                   // android.util.Log.d("SMS01", "IDX IS NOT 0 " + current.getMsgdata());
                }
                ConversationViewModel viewModel = new ConversationViewModel(); //ViewModelProviders.of(SmsConversationActivity.this).get(current.getId(), ConversationViewModel.class);
                viewModel.setItem(current);
                messageModels.add(viewModel);
            }
        }

        @Override
        public void onChildItemClicked(int parentPosition, int childPosition) {
            ConversationViewModel viewModel = getCurrent(parentPosition);
            update(viewModel);
            ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(parentPosition);
            if (holder != null) {
                holder.itemView.setBackgroundResource(selectedMessageModels.contains(getCurrent(parentPosition)) ? R.color.sms_message_selected : R.color.clear);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            private final ViewDataBinding binding;
            private ConversationViewModel item;
            private final RecyclerView pictureView =this.itemView.findViewById(R.id.pictureView);
            private final ImageView videoView =this.itemView.findViewById(R.id.videoView);

            private final ConstraintLayout VoiceNoteLayout = this.itemView.findViewById(R.id.VoiceNoteLayout);
            private final ImageView playChat1 = this.itemView.findViewById(R.id.playChat1);
            private final SeekBar seekBar1 = this.itemView.findViewById(R.id.sb);
            private final TextView txtSpeed=this.itemView.findViewById(R.id.txtSpeed);
            private final ConstraintLayout layout2 = this.itemView.findViewById(R.id.layout);
            private <T extends ViewDataBinding> ViewHolder(T binding) {
                super(binding.getRoot());
                ImageView dlrError = this.itemView.findViewById(R.id.dlrErrorBtn);
                if(dlrError != null){
                    dlrError.setOnClickListener(view -> {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SmsConversationActivity.this)
                                .setTitle("SMS Send Error")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .setMessage(item.getDlrError());

                        AlertDialog alert = builder.create();

                        alert.setOnShowListener(dialog -> {
                            Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            positiveButton.setTextColor(getResources().getColor(R.color.black, null));
                            negativeButton.setTextColor(getResources().getColor(R.color.black, null));
                        });

                        alert.show();
                    });

                }
                ImageView play = this.itemView.findViewById(R.id.playChat1);
                play.setBackground(Utils.getRipple(SmsConversationActivity.this));
                play.setOnClickListener(v -> {
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    boolean isMuted = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                    if (isMuted)Toast.makeText(SmsConversationActivity.this, "Please turn the volume up", Toast.LENGTH_SHORT).show();
                    int pbState = MediaControllerCompat.getMediaController(SmsConversationActivity.this).getPlaybackState().getState();
                    if (!item.getMMSImage().equals(url) || pbState == PlaybackStateCompat.STATE_STOPPED) {
                        url = item.getMMSImage();
                        currentPlay = play;
                        seekBar = this.itemView.findViewById(R.id.sb);
                        textView = this.itemView.findViewById(R.id.textView40);
                        Uri myUri = Uri.parse(item.getMMSImage());
                        voicemailViewModel.addURItoQueueSms(MediaControllerCompat.getMediaController(SmsConversationActivity.this), myUri,formatPhoneNumber(item.getItem().getSender()));
                        changePlayback(true);
                    } else {
                        changePlayback(false);
                    }
                });
                SeekBar seekBar = this.itemView.findViewById(R.id.sb);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) MediaControllerCompat.getMediaController(SmsConversationActivity.this).getTransportControls().seekTo(progress);}
                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}@Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                String ab = SpeedT.get(i);
                TextView textView1 = this.itemView.findViewById(R.id.txtSpeed);
                textView1.setText(ab);
                textView1.setBackground(Utils.getRipple(SmsConversationActivity.this));
                textView1.setOnClickListener(v -> {
                    if (i != 2 && i < 2) {i++;
                        String ab1 = SpeedT.get(i);
                        textView1.setText(ab1);
                    } else {i = 0;
                        String ab1 = SpeedT.get(i);
                        textView1.setText(ab1);
                    }if (textView1.getText().equals("1")) {SpeedState = 1F;
                        Bundle bundle = new Bundle();
                        bundle.putFloat("SpeedProgress", SpeedState);
                        MediaControllerCompat.getMediaController(SmsConversationActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                    } else if (textView1.getText().equals("1.5")) {SpeedState = 1.5F;
                        Bundle bundle = new Bundle();
                        bundle.putFloat("SpeedProgress", SpeedState);
                        MediaControllerCompat.getMediaController(SmsConversationActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                    } else if (textView1.getText().equals("2")) {SpeedState = 2F;
                        Bundle bundle = new Bundle();
                        bundle.putFloat("SpeedProgress", SpeedState);
                        MediaControllerCompat.getMediaController(SmsConversationActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                    }
                });
                this.binding = binding;
            }

            void bind(ConversationViewModel item) {
                String id = item.getID();
                binding.setVariable(com.telebroad.teleconsole.BR.viewmodel, item);
                binding.executePendingBindings();
                updateBackground();
                //android.util.Log.d("BINDING", item.getMsgData() + " dlr " + item.getDlrError());
                binding.setLifecycleOwner(SmsConversationActivity.this);
                //this.itemView.setBackgroundResource(R.drawable.background_selector);
                this.itemView.setOnClickListener(this);
                this.itemView.setOnLongClickListener(this);
                VoiceNoteLayout.setOnLongClickListener(this);
                playChat1.setOnLongClickListener(this);
                seekBar1.setOnLongClickListener(this);
                txtSpeed.setOnLongClickListener(this);
                layout2.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                update(item);
                updateBackground();
                return true;
            }

            @Override
            public void onClick(View v) {
                if (isSelectedMode) {
                    update(item);
                    updateBackground();
                }
            }
//            public void refreshBackground() {
//                updateBackground();
//            }

            public void updateBackground() {
                if (item != null) {
                    //this.itemView.setSelected(selectedMessageModels.contains(item));
                    this.itemView.setBackgroundResource(selectedMessageModels.contains(item) ? R.color.sms_message_selected : R.color.clear);
                }
            }

            void setMessageViewModel(ConversationViewModel viewModel) {
                this.item = viewModel;
                bind(item);
            }
        }
    }

//    public abstract static class RightDrawableOnTouchListener implements View.OnTouchListener {
//        private Drawable drawable;
//        public RightDrawableOnTouchListener(TextView view) {
//            super();
//            final Drawable[] drawables = view.getCompoundDrawablesRelative();
//            if (drawables != null && drawables.length == 4)
//                this.drawable = drawables[2];
//        }
//
//        /*
//         * (non-Javadoc)
//         *
//         * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
//         */
//        @Override
//        public boolean onTouch(final View v, final MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_DOWN && drawable != null) {
//                final int x = (int) event.getX();
//                final int y = (int) event.getY();
//                final Rect bounds = drawable.getBounds();
//                int fuzz = 10;
//                if (x >= (v.getRight() - bounds.width() - fuzz) && x <= (v.getRight() - v.getPaddingRight() + fuzz)
//                        && y >= (v.getPaddingTop() - fuzz) && y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
//                    return onDrawableTouch(event);
//                }
//            }
//            return false;
//        }
//        public abstract boolean onDrawableTouch(final MotionEvent event);
//
//    }
    @Override
    protected void onStart() {super.onStart();
        MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                MediaControllerCompat mediaController = new MediaControllerCompat(SmsConversationActivity.this, token);
                MediaControllerCompat.setMediaController(SmsConversationActivity.this, mediaController);
                MediaMetadataCompat metadata = mediaController.getMetadata();
                PlaybackStateCompat pbState = mediaController.getPlaybackState();
                mediaController.registerCallback(controllerCallback);
            }
        };
        if (mediaBrowser == null) {
            mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, VoicemailPlayingService.class), connectionCallback, null);
            mediaBrowser.connect();
        }
    }
    private void handleMetaData(MediaMetadataCompat metadata) {
        int duration = (int) metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
        if (duration > 0 && seekBar != null) seekBar.setMax(duration);
    }
    @Override
    protected void onStop() {super.onStop();
//        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
//        if (controller != null) {
//            if (controller.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING) voicemailViewModel.removeQueueItem(controller);
//            controller.unregisterCallback(controllerCallback);
//        }
//        if (mediaBrowser != null && mediaBrowser.isConnected()) mediaBrowser.disconnect();
    }
    void changePlayback(boolean forcePlay) {
        int pbState = MediaControllerCompat.getMediaController(SmsConversationActivity.this).getPlaybackState().getState();
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            if (forcePlay) {
                MediaControllerCompat.getMediaController(this).getTransportControls().play();
                currentPlay.setImageResource(R.drawable.ic_outline_pause_blue);
            } else {
                MediaControllerCompat.getMediaController(this).getTransportControls().pause();
                currentPlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_blue);
            }
        } else if (pbState == PlaybackStateCompat.STATE_PAUSED || pbState == 0) {
            MediaControllerCompat.getMediaController(this).getTransportControls().play();
            currentPlay.setImageResource(R.drawable.ic_outline_pause_blue);
        } else if (pbState == PlaybackStateCompat.STATE_STOPPED){
            seekBar.setProgress(0);
        }
    }

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording && mediaRecorder != null){ // if we are already recording// get the current amplitude
                int x = mediaRecorder.getMaxAmplitude();
                sendTextBinding.VisualizerViewSMS.addAmplitude(x); // update the VisualizeView
                sendTextBinding.VisualizerViewSMS.invalidate();
                handler.postDelayed(this, 40);// update in 40 milliseconds
            }
        }
    };
    private void pauseAudio(){
        mediaPlayer.pause();isPlaying = false;
        seekBarHandler.removeCallbacks(updateSeekbar);
        sendTextBinding.playChat.setImageResource(R.drawable.ic_baseline_play_circle_outline_blue);
    }
    private void stopPlaying() {
        isPlaying = false;mediaPlayer.stop();
        sendTextBinding.playChat.setImageResource(R.drawable.ic_baseline_play_circle_outline_blue);
        seekBarHandler.removeCallbacks(updateSeekbar);
    }
    private void resumeAudio(){
        mediaPlayer.start();isPlaying = true;updateRunnable();
        sendTextBinding.playChat.setImageResource(R.drawable.ic_outline_pause_blue);
        seekBarHandler.postDelayed(updateSeekbar,0);
    }
    private void playAudio() throws IOException {
        mediaPlayer = new MediaPlayer();audioHasStarted = true;
        mediaPlayer.setDataSource(SettingsHelper.getRecordFilePathSMS(getApplicationContext()));
        mediaPlayer.prepare();mediaPlayer.start();isPlaying = true;
        sendTextBinding.playChat.setImageResource(R.drawable.ic_outline_pause_blue);
        mediaPlayer.setOnCompletionListener(mp -> {stopPlaying();audioHasFinished = true;});
        sendTextBinding.sb.setMax(mediaPlayer.getDuration());
        seekBarHandler = new Handler(Looper.getMainLooper());
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekbar, 0);
    }
    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                sendTextBinding.sb.setProgress(mediaPlayer.getCurrentPosition());
                sendTextBinding.txtTimer.setText((Utils.formatLongMilliSeconds(mediaPlayer.getCurrentPosition())));
                seekBarHandler.postDelayed(this, 500);}};
    }
    public void ClearViews() {
        int imageResourceIdMic = R.drawable.ic_mic_blue;
        sendTextBinding.sendButton.setImageResource(R.drawable.ic_mic_blue);
        sendTextBinding.sendButton.setTag(imageResourceIdMic);
        sendTextBinding.VisualizerViewSMS.setVisibility(View.GONE);
        sendTextBinding.constraintLayout3.setVisibility(View.GONE);
        sendTextBinding.newText.setVisibility(View.VISIBLE);
        sendTextBinding.addImage.setVisibility(View.VISIBLE);
        if (mediaRecorder != null && isRecording){mediaRecorder.stop();mediaRecorder.reset(); mediaRecorder.release();mediaRecorder = null;isRecording=false;}
        sendTextBinding.constraintLayout3.setAlpha(1.0f);
        sendTextBinding.constraintLayout3.animate().translationY(0);
        sendTextBinding.constraintLayout3.animate().setListener(null);
        sendTextBinding.VisualizerViewSMS.clear();sendTextBinding.VisualizerViewSMS.invalidate();
    }

    public class SizeLimitedRecorder {
        private static final int ONE_MB_IN_BYTES = 1024 * 1024;
        private final MediaRecorder recorder;
        private final File recordingFile;
        private final Handler handler;

        public SizeLimitedRecorder(MediaRecorder recorder, File recordingFile) {
            this.recorder = recorder;
            this.recordingFile = recordingFile;
            this.handler = new Handler(Looper.getMainLooper());
        }

        public void startRecording() {
            FileObserver observer = new FileObserver(recordingFile.getParent()) {
                @Override
                public void onEvent(int event, String path) {
                    if (path != null && path.equals(recordingFile.getName())) {
                        handler.post(() -> checkFileSize());
                    }
                }
            };
            observer.startWatching();
            try {
                recorder.prepare();
                recorder.start();
            } catch (Exception e) {
                observer.stopWatching();
                throw new RuntimeException("Failed to start recording", e);
            }
        }

        private void checkFileSize() {
            long length = recordingFile.length();
            if (length >= ONE_MB_IN_BYTES) stopRecording();
        }

        public void stopRecording() {
            if (isRecording) {
                isRecording = false;
                if (mediaRecorder != null) {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    //mediaRecorder = null;
                }
                sendTextBinding.recordTimer.stop();
                sendTextBinding.newText.setHint(R.string.say_something);
                sendTextBinding.newText.setCursorVisible(true);
                sendTextBinding.VisualizerViewSMS.setVisibility(View.GONE);
                sendTextBinding.recordTimer.setVisibility(View.GONE);
                sendTextBinding.VisualizerViewSMS.clear();
                sendTextBinding.VisualizerViewSMS.invalidate();
                sendTextBinding.addImage.setImageResource(R.drawable.ic_add_photo_gray);
                sendTextBinding.imagePreview.setVisibility(View.GONE);
                sendTextBinding.constraintLayout3.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaRecorder = null;
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            if (controller.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING) voicemailViewModel.removeQueueItem(controller);
            controller.unregisterCallback(controllerCallback);
        }
        if (mediaBrowser != null && mediaBrowser.isConnected()) mediaBrowser.disconnect();
    }


    public static String formatPhoneNumber(String number) {
        Pattern pattern = Pattern.compile("(\\d{1})(\\d{3})(\\d{3})(\\d{2})(\\d{2})");
        Matcher matcher = pattern.matcher(number);
        if (matcher.matches()) {
            return String.format("%s (%s) %s-%s%s",
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3),
                    matcher.group(4),
                    matcher.group(5));
        }
        return number;
    }
    public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ViewHolder> {
       private final List<MMSLinksViewModels> models;
       private SmsConversationActivity.OnChildItemClickListener listener;
       private int position1;
        public ChildAdapter(List<MMSLinksViewModels> models,SmsConversationActivity.OnChildItemClickListener listener,int position ) {
            Log.d("yehuda", "ChildAdapter: called" + position);
            this.models = models;
            this.listener = listener;
            this.position1 = position;
            notifyDataSetChanged();
        }

        private static final String TAG = "yehuda";
        @Override
       
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("yehuda", "onCreateViewHolder: called");
            MmsImagesBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    R.layout.mms_images, parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.binding.setChildItem(models.get(position));
            holder.binding.setLifecycleOwner(SmsConversationActivity.this);
            holder. binding.executePendingBindings();

            // TODO: 12/6/2023  from here
            String str = models.get(position).url();
            if ( str.length()>0 && Utils.isVidoeType(MimeTypeMap.getFileExtensionFromUrl(models.get(position).url()))) {
                holder.binding.smsImg.setOnClickListener(v -> {
                    Intent intent = new Intent(SmsConversationActivity.this, MMSVideoViewActivity.class);
                    intent.putExtra(MMSVideoViewActivity.MMS_VIDEO_URL, str);
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(SmsConversationActivity.this, holder.binding.smsImg, "beautifulAnimate").toBundle());
                });

                holder.binding.playButtonvideoView.setOnClickListener(v -> {
                    holder.binding.smsImg.performClick();  });

                holder.binding.smsImg.setOnLongClickListener(v -> {
                    int currentPosition = holder.getAbsoluteAdapterPosition();
                    if (listener != null && currentPosition != RecyclerView.NO_POSITION) {
                        listener.onChildItemClicked(position1, position);
                    }
                    return true;
                });

                holder.binding.playButtonvideoView.setOnLongClickListener(v -> {
                    holder.binding.smsImg.performLongClick();
                    return true;
                }
                );
            }
            else {
                // TODO: 12/6/2023 up to here

                holder.binding.smsImg.setOnClickListener(v -> {
                    Intent intent = new Intent(SmsConversationActivity.this, MMSImageViewActivity.class);
                    intent.putExtra(MMSImageViewActivity.MMS_IMAGE_URL, holder.binding.smsImg.getTransitionName());
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(SmsConversationActivity.this, holder.binding.smsImg, "beautifulAnimate").toBundle());
                });
                holder.binding.smsImg.setOnLongClickListener(v -> {
                    int currentPosition = holder.getAbsoluteAdapterPosition();
                    if (listener != null && currentPosition != RecyclerView.NO_POSITION) {
                        listener.onChildItemClicked(position1, position);
                    }
                    return true;
                });
            }
        }

        @Override
        public int getItemCount() {
            return models.size();
        }



        public class ViewHolder extends RecyclerView.ViewHolder {
           private final MmsImagesBinding binding;
            public ViewHolder(MmsImagesBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
    public interface OnChildItemClickListener {
        void onChildItemClicked(int parentPosition, int childPosition);
    }
}
