package com.telebroad.teleconsole.controller.dashboard;

import static android.Manifest.permission.RECORD_AUDIO;
import static com.google.common.base.Strings.isNullOrEmpty;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.FilesAdapterType;
import com.telebroad.teleconsole.chat.ImagesUriRV;
import com.telebroad.teleconsole.chat.client.DelMessage;
import com.telebroad.teleconsole.chat.client.Extra;
import com.telebroad.teleconsole.chat.client.ReplaceReplyPub;
import com.telebroad.teleconsole.chat.client.pubMessage;
import com.telebroad.teleconsole.chat.models.FileUploadService;
import com.telebroad.teleconsole.chat.viewModels.ChatReactionsViewModel;
import com.telebroad.teleconsole.controller.MMSImageViewActivity;
import com.telebroad.teleconsole.databinding.ItemChatImagesBinding;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.Attachments;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.chat.server.CtrlMessage;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.chat.viewModels.ChatMessageViewModel;
import com.telebroad.teleconsole.chat.viewModels.ChatRepliesViewModel;
import com.telebroad.teleconsole.chat.viewModels.ChatViewModel;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.PhotoSourceDialogList;
import com.telebroad.teleconsole.databinding.ChatFilesLayoutBinding;
import com.telebroad.teleconsole.databinding.ItemChatBinding;
import com.telebroad.teleconsole.databinding.ItemReactionBinding;
import com.telebroad.teleconsole.databinding.ReplyActivityBinding;
import com.telebroad.teleconsole.databinding.VideoChatViewBinding;
import com.telebroad.teleconsole.databinding.VoicnotesChatBinding;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.notification.VoicemailPlayingService;
import com.telebroad.teleconsole.viewmodels.VoicemailViewModel;
import com.vanniktech.emoji.EmojiPopup;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatReplyActivity extends AppCompatActivity implements ChatViewModel.Callback {
    private VoicemailViewModel voicemailViewModel;
    private static final String MMS_IMAGE_FOLDER = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Chat Files";
    private ReplyActivityBinding binding;
    private int fileSize;
    private final List<String> mentions = new ArrayList<>();
    int min;
    private int i = 0;
    private PopupMenu popupMenuChatEtText;
    private List<ChannelDB> newList;
    private File file;
    private ChatReactionsViewModel chatReactionsViewModel;
    private URL url1;
    private ChannelDB myChannelDB;
    private final SimpleDateFormat sdft = new SimpleDateFormat(" h:mm a", Locale.getDefault());
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, h:mm a", Locale.getDefault());
    private boolean isPlaying,audioHasStarted = false,audioHasFinished = false;
    private final String auth = "token";
    private final String apikey = "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo";
    private final String secret = SettingsHelper.getString(SettingsHelper.CHAT_TOKEN);
    private final String domain = "https://apiconnact.telebroad.com";
    private ArrayList<String> SpeedT;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private List<CtrlMessage> ctrls;List<DataMessage.Replies> ListNonNullReplies;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private List<String> urls;
    private String name1;
    private List<pubMessage.Attachments> attachmentsList;
    private pubMessage pub;
    private pubMessage.Head head;
    private ImageView currentPlay;
    private String url = "",encodeString, id, topic;
    private Float SpeedState;
    private RepliesAdapter repliesAdapter;
    private SeekBar seekBar;
    private Handler seekBarHandler;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    public static final int REQUEST_AUDIO_CODE = 100;
    boolean compressed = false,isRecording, finishedRecording = false;
    private MediaBrowserCompat mediaBrowser;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekbar;
    private ChatMessageViewModel chat;
    private TextView textView;
    public List<CtrlMessage> getCtrls() {return ctrls;}
    public String getName1() {return name1;}
    private final Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 200);
            long longProgress = MediaControllerCompat.getMediaController(ChatReplyActivity.this).getPlaybackState().getPosition();
            seekBar.setProgress((int) longProgress);
           // textView.setText((Utils.formatLongMilliSeconds(longProgress)));
        }
    };
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (currentPlay != null) {
                if (state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
                } else if (state.getState() == PlaybackStateCompat.STATE_PLAYING) handler.post(updateProgress);
                else if (state.getState() != PlaybackStateCompat.STATE_PAUSED) {
                    handler.removeCallbacks(updateProgress);
                    currentPlay.setImageResource(R.drawable.ic_outline_play_arrow_24);}
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            handleMetaData(metadata);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ReplyActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) chat= getIntent().getSerializableExtra("originalMassage",ChatMessageViewModel.class);
        else {
          Bundle extras = getIntent().getExtras();
          if (extras != null && extras.containsKey("originalMassage")) {
              Object messageObject = extras.get("originalMassage");
              if (messageObject instanceof ChatMessageViewModel) {
                 chat = (ChatMessageViewModel) messageObject;
              }
          }
        }
       // new ChatActivity.ChatAdapter(new ChatActivity.ChatDiff(), this);
        repliesAdapter = new RepliesAdapter(new ChatReplyActivity.ChatDiff());
        String urlString = getIntent().getStringExtra(ChatImageActivity.MMS_IMAGE_URL);
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent,null));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        popupMenuChatEtText = new PopupMenu(ChatReplyActivity.this, binding.chatEtText);
        chatReactionsViewModel = new ViewModelProvider.AndroidViewModelFactory(AppController.getInstance()).create(ChatReactionsViewModel.class);
        ChatRepliesViewModel chatRepliesViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ChatRepliesViewModel.class);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {@Override public void handleOnBackPressed() {finish();}};
        getOnBackPressedDispatcher().addCallback(this,callback);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.relRoot)).build(binding.chatEtText);
        binding.emoji.setOnClickListener(v -> popup.toggle());
        pub = new pubMessage();
        topic = chat.getTopic();
        ChatViewModel chatViewModel = new ChatViewModel(this);chatViewModel.getLiveMentionChannelsSearch();
        SpeedT = new ArrayList<>();ctrls = new ArrayList<>();attachmentsList = new ArrayList<>();urls = new ArrayList<>();ListNonNullReplies = new ArrayList<>();
        SpeedT.add("1");SpeedT.add("1.5");SpeedT.add("2");
        ChatDatabase.getInstance().channelDao().getMeUsersByTopic(topic).observe(this, channelDB -> {
            myChannelDB = channelDB;
            if (channelDB != null &&  channelDB.getName() != null){
                name1 = channelDB.getName();
                binding.ThreadName.setText(channelDB.getName());
                binding.chatEtText.setHint("Message " + channelDB.getName().replaceAll("\\n", "...").replaceAll("\\r", "..."));
            }
        });
        voicemailViewModel = new VoicemailViewModel();
        LiveData<List<Replies>> LiveDataReplies = chatRepliesViewModel.getAllRepliesByTopic(chat.getTopic(), "%\"reply\":"+chat.getSeq() +"%" );
        LiveDataReplies.observe(this, replies1 -> {
            if (replies1.size() == 1) binding.textView40.setText(replies1.size()+" Reply");
            else binding.textView40.setText(replies1.size()+" Replies");
            //repliesAdapter.notifyDataSetChanged();
            binding.RVReplies.setLayoutManager(new LinearLayoutManager(ChatReplyActivity.this));
            repliesAdapter.submitList(replies1);
            binding.RVReplies.setAdapter(repliesAdapter);

        });
        String attachments1 = gson.toJson(chat.getAttachments());
        Type collectionType1 = new TypeToken<Collection<Attachments>>() {}.getType();
        Collection<Attachments> enums1 = gson.fromJson(attachments1, collectionType1);
        if (enums1.size()== 1){
            for (Attachments a:enums1){
                String type = a.getType();
                if (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/jpg") || type.equals("image/gif") || type.equals("image/webp") || type.equals("image/tiff") || type.equals("image/raw") || type.equals("image/bmp") || type.equals("image/heif") || type.equals("image/jpeg2000") || type.equals("image/jfif") || type.equals("image/.jfif")|| type.equals("Image")) {
                    binding.chatForwardImageView.setVisibility(View.VISIBLE);
                    binding.chatForwardImageView.setVisibility(View.GONE);
                try {
                    String newURL  = domain + a.getPath() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                    Glide.with(this).load(newURL).into(binding.chatForwardImageView);
                } catch (UnsupportedEncodingException e) {e.printStackTrace();}
            }else if (type.equals("video/webm;codecs=vp8") || Objects.equals(type, "image/m4v")){
                    binding.chatForwardImageView.setVisibility(View.VISIBLE);
                    binding.PlayVideo.setVisibility(View.VISIBLE);
                    try {
                        String newURL = domain + a.getPath() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                        Glide.with(this).load(newURL).into(binding.chatForwardImageView);
                    } catch (UnsupportedEncodingException e) {e.printStackTrace();}

                }
            }
        }else {
            binding.chatForwardImageView.setVisibility(View.GONE);
            binding.attachmentsRV.setVisibility(View.VISIBLE);
            String attachments = gson.toJson(chat.getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
            ArrayList<FilesAdapterType> strings = enums.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
            PhotoAdapter photoAdapter = new PhotoAdapter(strings,null);
            binding.attachmentsRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            binding.attachmentsRV.setAdapter(photoAdapter);
        }
        binding.RVReplies.addOnItemTouchListener(new RecyclerViewItemTouchListener(this, binding.RVReplies, (view, position, motionEvent) -> {
            binding.confirmEdit.setOnClickListener(v -> {
                Replies replies = repliesAdapter.getCurrentList().get(position);
                String attachments = gson.toJson(replies.getHead().getAttachments());
                Type collectionType = new TypeToken<List<ReplaceReplyPub.Attachments>>() {}.getType();
                List<ReplaceReplyPub.Attachments> enums = gson.fromJson(attachments, collectionType);
                ReplaceReplyPub.Head head = new ReplaceReplyPub.Head("text/*", repliesAdapter.getCurrentList().get(position).getSeq(),replies.getHead().getReply(),enums);
                ReplaceReplyPub pubMessage = new ReplaceReplyPub("replace",topic,SettingsHelper.getString(SettingsHelper.MY_TOPIC),true,binding.editText.getText().toString(), head);
                ChatWebSocket.getInstance().sendObject("pub",pubMessage);
                //Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().UpdateContent(binding.editText.getText().toString(), repliesAdapter.repliesCollectionList.get(position).getSeq(), repliesAdapter.repliesCollectionList.get(position).getTopic()));
                Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().UpdateContent(binding.editText.getText().toString(), repliesAdapter.getCurrentList().get(position).getSeq(), repliesAdapter.getCurrentList().get(position).getTopic()));
                binding.relTxtLayout.setVisibility(View.VISIBLE);
                binding.relEtLayout.setVisibility(View.GONE);
            });
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                View child = binding.RVReplies.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (child != null && child.findViewById(R.id.playChat1) != null) {
                    Rect rect = new Rect();
                    child.findViewById(R.id.playChat1).getGlobalVisibleRect(rect);
                    if (rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) return;
                }
                if (child != null && child.findViewById(R.id.reactionRecycler) != null) {
                    Rect rect = new Rect();
                    child.findViewById(R.id.reactionRecycler).getGlobalVisibleRect(rect);
                    if (rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) return;
                }
                if (child != null && child.findViewById(R.id.parentVideo) != null) {
                    Rect rect = new Rect();
                    child.findViewById(R.id.parentVideo).getGlobalVisibleRect(rect);
                    if (rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) return;
                }
                if (child != null && child.findViewById(R.id.displayImage) != null) {
                    Rect rect = new Rect();
                    child.findViewById(R.id.displayImage).getGlobalVisibleRect(rect);
                    if (rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) return;
                }
                if (child != null && child.findViewById(R.id.dropDown) != null) {
                    Rect rect = new Rect();
                    child.findViewById(R.id.dropDown).getGlobalVisibleRect(rect);
                    if (rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) return;
                }
                if (child != null && child.findViewById(R.id.btnFile) != null) {
                    Rect rect = new Rect();
                    child.findViewById(R.id.btnFile).getGlobalVisibleRect(rect);
                    if (rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) return;
                }
                ChannelDB channelDB1 = ChatViewModel.getInstance().getChannelsByName().get(repliesAdapter.getCurrentList().get(position).getSenderName());
                String newTopic = channelDB1.getTopic();
                if (newTopic.equals(SettingsHelper.getString(SettingsHelper.MY_TOPIC))) {
                    View popupView = View.inflate(ChatReplyActivity.this, R.layout.chat_popup_me_reply, null);
                    PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
                    popupWindow.getContentView().setVisibility(View.GONE);
                    popupWindow.showAsDropDown(view, (int) motionEvent.getX()+500, (int) motionEvent.getY() - 160);
                    popupWindow.getContentView().setVisibility(View.VISIBLE);
                    if (popupWindow.getContentView().getVisibility() == View.VISIBLE) {
                        ImageButton imageButtonDelete = popupView.findViewById(R.id.imageButtonDelete);
                        imageButtonDelete.setOnClickListener(view15 -> {
                            List<DelMessage.Delseq> delseqList = new ArrayList<>();
                            DelMessage.Delseq delseq = new DelMessage.Delseq(repliesAdapter.getCurrentList().get(position).getSeq());
                            delseqList.add(delseq);
                            DelMessage delMessage = new DelMessage("delete", repliesAdapter.getCurrentList().get(position).getTopic(), "msg", true, delseqList);
                            ChatWebSocket.getInstance().sendObject("del", delMessage);
                            for (DelMessage.Delseq d : delseqList) {
                                Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteMassage(newTopic, d.getLow()));
                            }popupWindow.dismiss();
                        });
                        ImageButton imageButtonForward = popupView.findViewById(R.id.imageButtonForward);
                        imageButtonForward.setOnClickListener(view13 -> {
                            String replies1 = gson.toJson(repliesAdapter.getCurrentList().get(position));
                            Intent intent = new Intent(ChatReplyActivity.this,ChatForwardActivity.class);
                            intent.putExtra("topic",topic);
                            intent.putExtra("messageObject",replies1);
                            startActivity(intent);
                            finish();
                            popupWindow.dismiss();
                        });
                        ImageButton imageButtonEdit = popupView.findViewById(R.id.imageButtonEdit);
                        imageButtonEdit.setOnClickListener(v -> {
                            popupView.setVisibility(View.GONE);
                            popupWindow.getContentView().setVisibility(View.GONE);
                            binding.relTxtLayout.setVisibility(View.GONE);
                            binding.relEtLayout.setVisibility(View.VISIBLE);
                            if (repliesAdapter.getCurrentList().get(position).getContent() != null) {
                                binding.editText.setText(SettingsHelper.reformatHTML(repliesAdapter.getCurrentList().get(position).getContent().toString()));
                            }
                        });
                    }
                } else {
                    View popupView1 = View.inflate(ChatReplyActivity.this, R.layout.chat_popup_reply, null);
                    PopupWindow popupWindow1 = new PopupWindow(popupView1, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
                    popupWindow1.getContentView().setVisibility(View.GONE);
                    popupWindow1.showAsDropDown(view, (int) motionEvent.getX(), (int) motionEvent.getY() - 160);
                    popupWindow1.getContentView().setVisibility(View.VISIBLE);
                    if (popupWindow1.getContentView().getVisibility() == View.VISIBLE) {
                        ImageButton imageButtonDelete = popupView1.findViewById(R.id.imageButtonDelete);
                        imageButtonDelete.setOnClickListener(view15 -> {
                            List<DelMessage.Delseq> delseqList = new ArrayList<>();
                            DelMessage.Delseq delseq = new DelMessage.Delseq(repliesAdapter.getCurrentList().get(position).getSeq());
                            delseqList.add(delseq);
                            DelMessage delMessage = new DelMessage("delete",repliesAdapter.getCurrentList().get(position).getTopic(), "msg", true, delseqList);
                            ChatWebSocket.getInstance().sendObject("del", delMessage);
                            popupWindow1.dismiss();
                            for (DelMessage.Delseq d : delseqList) {
                                Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteMassage( repliesAdapter.getCurrentList().get(position).getTopic(), d.getLow()));}
                        });
                        ImageButton imageButtonForward = popupView1.findViewById(R.id.imageButtonForward);
                        imageButtonForward.setOnClickListener(view13 -> {
                            String replies1 = gson.toJson(repliesAdapter.getCurrentList().get(position));
                            Intent intent = new Intent(ChatReplyActivity.this,ChatForwardActivity.class);
                            intent.putExtra("messageObject",replies1);
                            startActivity(intent);
                            popupWindow1.dismiss();
                            finish();
                        });
                    }
                }
            }
        }, (view, position, motionEvent, motionEvent1) -> {}));
        setSupportActionBar(binding.toolbar);
        if (urlString != null && !urlString.isEmpty()) {
            binding.chatForwardImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(urlString).into(binding.chatForwardImageView);
        }
        connectivityManager = (ConnectivityManager) ChatReplyActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (!ChatWebSocket.isConnected){
                    ChatWebSocket.getInstance().connect();
                    new CountDownTimer(500, 1000) {
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {
                            if (ChatWebSocket.isConnected) ChatWebSocket.getInstance().subscribe(topic);}}.start();}
            }
        };connectivityManager.registerDefaultNetworkCallback(networkCallback);
        binding.cancelEdit.setOnClickListener(v -> {
            binding.editText.setText("");
            binding.relTxtLayout.setVisibility(View.VISIBLE);
            binding.relEtLayout.setVisibility(View.GONE);
        });
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        binding.toolbar.setNavigationOnClickListener(v -> {callback.handleOnBackPressed();});
        if (chat.getImageURL() == null) binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(chat.getTopic()));
        else Glide.with(ChatReplyActivity.this).load(chat.getImageURL()).circleCrop().into(binding.imageView32);
        if (!chat.getText().isEmpty()){
            binding.txtFirstReply.setVisibility(View.VISIBLE);
            binding.txtFirstReply.setText(SettingsHelper.reformatHTML(chat.getText()));
        } else binding.txtFirstReply.setVisibility(View.GONE);
        String t = sdf.format(getDate());
        binding.txtReplyTime.setText(t);
        binding.txtReplySenderName.setText(chat.getSenderName());
        ViewTreeObserver vto = binding.chatEtText.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.chatEtText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getApplicationContext(), 0);
                popupMenuChatEtText = new PopupMenu(contextWrapper, binding.chatEtText);
                binding.chatEtText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().startsWith("@") && s.toString().length() == 0) {
                            popupMenuChatEtText.getMenu().clear();
                            for (ChannelDB item : newList) {
                                popupMenuChatEtText.getMenu().add(item.getName());
                            }
                        } else if (s.toString().length() > 0 && s.toString().endsWith("@")) {
                            popupMenuChatEtText.getMenu().clear();
                            for (ChannelDB item : newList) {
                                popupMenuChatEtText.getMenu().add(item.getName());
                            }
                        } else if (s.toString().length() > 1 && '@' == s.toString().charAt(s.toString().length() - 2)) {
                            popupMenuChatEtText.getMenu().clear();
                            char c = s.toString().charAt(s.toString().length() - 1);
                            for (ChannelDB item : newList) {
                                if (item.getName().contains(Character.toString(c))) popupMenuChatEtText.getMenu().add(item.getName());
                            }
                        } else if (s.toString().length() > 2 && '@' == s.toString().charAt(s.toString().length() - 3)) {
                            popupMenuChatEtText.getMenu().clear();
                            CharSequence lastTwoCharacters = binding.chatEtText.getText().subSequence(binding.chatEtText.getText().length() - 2, binding.chatEtText.getText().length());
                            String lastTwoCharactersString = lastTwoCharacters.toString();
                            for (ChannelDB item : newList) {
                                if (item.getName().toLowerCase().contains(lastTwoCharactersString.toLowerCase())) popupMenuChatEtText.getMenu().add(item.getName());
                            }
                        }else if (s.toString().length() > 3 && '@' == s.toString().charAt(s.toString().length() - 4)) {
                            popupMenuChatEtText.getMenu().clear();
                            CharSequence lastTwoCharacters = binding.chatEtText.getText().subSequence(binding.chatEtText.getText().length() - 3, binding.chatEtText.getText().length());
                            String lastTwoCharactersString = lastTwoCharacters.toString();
                            for (ChannelDB item : newList) {
                                if (item.getName().toLowerCase().contains(lastTwoCharactersString.toLowerCase())) popupMenuChatEtText.getMenu().add(item.getName());
                            }
                        }else if (s.toString().length() > 4 && '@' == s.toString().charAt(s.toString().length() - 5)) {
                            popupMenuChatEtText.getMenu().clear();
                            CharSequence lastTwoCharacters = binding.chatEtText.getText().subSequence(binding.chatEtText.getText().length() - 4, binding.chatEtText.getText().length());
                            String lastTwoCharactersString = lastTwoCharacters.toString();
                            for (ChannelDB item : newList) {
                                if (item.getName().toLowerCase().contains(lastTwoCharactersString.toLowerCase())) popupMenuChatEtText.getMenu().add(item.getName());
                            }
                        } else {
                            popupMenuChatEtText.getMenu().clear();
                        }popupMenuChatEtText.setOnMenuItemClickListener(item -> {
                            if (binding.chatEtText.length() >= 2) {
                                String newText = binding.chatEtText.getText().toString().substring(0, binding.chatEtText.length() - 2);
                                binding.chatEtText.setText(newText + " @" + item.getTitle());
                                ChannelDB channelDB5 = ChatViewModel.getInstance().getChannelsByName().get(item.getTitle());
                                mentions.add(channelDB5.getTopic());
                                binding.chatEtText.setSelection(binding.chatEtText.length());
                                popupMenuChatEtText.getMenu().clear();
                            } else if (binding.chatEtText.length() == 1) {
                                String newText = binding.chatEtText.getText().toString().substring(0, binding.chatEtText.length() - 1);
                                binding.chatEtText.setText(newText + " @" + item.getTitle());
                                ChannelDB channelDB5 = ChatViewModel.getInstance().getChannelsByName().get(item.getTitle());
                                mentions.add(channelDB5.getTopic());
                                binding.chatEtText.setSelection(binding.chatEtText.length());
                                popupMenuChatEtText.getMenu().clear();
                            } else {
                                binding.chatEtText.setText(binding.chatEtText.getText().toString() + " @" + item.getTitle());
                                binding.chatEtText.setSelection(binding.chatEtText.length());
                                ChannelDB channelDB5 = ChatViewModel.getInstance().getChannelsByName().get(item.getTitle());
                                mentions.add(channelDB5.getTopic());
                            }return true;
                        });popupMenuChatEtText.show();
                        if (!binding.chatEtText.getText().toString().isEmpty()) {
                            binding.chatEtText.setMaxLines(50);
                            binding.mic.setImageResource(R.drawable.ic_baseline_send_24);
                            ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.relTxtLayout.getLayoutParams();
                            constraintLayout.topMargin = 0;
                            constraintLayout.leftMargin = 0;
                            constraintLayout.rightMargin = 10;
                            binding.relTxtLayout.setLayoutParams(constraintLayout);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (binding.chatEtText.getText().toString().isEmpty()) {
                            popupMenuChatEtText.getMenu().clear();
                            binding.chatEtText.setMaxLines(1);
                            binding.mic.setImageResource(R.drawable.ic_microphone);
                            ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.relTxtLayout.getLayoutParams();
                            constraintLayout.topMargin = 0;
                            constraintLayout.leftMargin = 0;
                            constraintLayout.rightMargin = 105;
                            binding.relTxtLayout.setLayoutParams(constraintLayout);
                        }
                        binding.imageView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        binding.pdfLayout.imgCancel.setOnClickListener(v -> {
            binding.imgToSend.animate().translationY(binding.imgToSend.getHeight()).alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);ClearImageVies();}
            });
        });
        binding.mic.setOnClickListener(v -> {
            binding.RVReplies.smoothScrollToPosition(repliesAdapter.getItemCount());
            if (!binding.chatEtText.getText().toString().isEmpty() && binding.imgToSend.getVisibility()== View.GONE) {
                pubMessage PubMessage = new pubMessage("pubMessage", myChannelDB.getTopic(), binding.chatEtText.getText().toString()
                        ,pub.getHead("text/*",chat.getSeq()), false);
                ChatWebSocket.getInstance().sendObject("pub", PubMessage);
                binding.chatEtText.setText("");
                binding.mic.setImageResource(R.drawable.ic_microphone);
                ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.relTxtLayout.getLayoutParams();
                constraintLayout.topMargin = 0;
                constraintLayout.leftMargin = 0;
                constraintLayout.rightMargin = 105;
                binding.relTxtLayout.setLayoutParams(constraintLayout);
            } else if (binding.imgToSend.getVisibility()==View.VISIBLE) {
                if (ctrls.size() >0) {
                    Extra extra = new Extra();
                    List<String> attachments5 = new ArrayList<>();
                    for (CtrlMessage ctr : ctrls) {
                        id = ctr.getId();
                        String url = ctr.getParams().getUrl();
                        attachments5.add(url);
                        extra.setAttachments(attachments5);
                        String urlEnd = url.substring(url.lastIndexOf(".") + 1);
                        if ("121164".equals(id) && ((urlEnd.equals("jfif") || urlEnd.equals("png") || urlEnd.equals("jpg") ||
                                urlEnd.equals("jpeg") || urlEnd.equals("gif") || urlEnd.equals("webp") ||
                                urlEnd.equals("tiff") || urlEnd.equals("raw") || urlEnd.equals("bmp") ||
                                urlEnd.equals("heif") || urlEnd.equals("jpeg2000")))) {
                            pubMessage.Attachments attachments = new pubMessage.Attachments("img_" +
                                    SettingsHelper.getDateString() + "." + urlEnd, url, "image/" + urlEnd, ctr.getParams().getExpires(), false);
                            attachmentsList.add(attachments);
                            urls.add(ctr.getParams().getUrl());
                            head = pubMessage.setHeadWithAttachmentsForReplies(attachmentsList,chat.getSeq(),mentions);
                        }else if (urlEnd.equals("mp4") || urlEnd.equals("m4v")){
                            pubMessage.Attachments attachments = new pubMessage.Attachments("video_" +
                                    SettingsHelper.getDateString() + "." + urlEnd, url, "video/webm;codecs=vp8", ctr.getParams().getExpires(), false);
                            attachmentsList.add(attachments);
                            urls.add(ctr.getParams().getUrl());
                            head = pubMessage.setHeadWithAttachmentsForReplies(attachmentsList,chat.getSeq(),mentions);
                        }
                    }
                    pub = pubMessage.setPubForImages(head, id, SettingsHelper.getString(SettingsHelper.MY_TOPIC, ""), myChannelDB.getTopic(), binding.chatEtText.getText().toString());
                    ChatWebSocket.getInstance().sendTwoObjects("pub", pub,"extra",extra);
                    ClearImageVies();
                }
            } else if (binding.chatEtText.getText().toString().isEmpty() && binding.imgToSend.getVisibility()==View.GONE && !finishedRecording) {
                if (AppController.getInstance().hasPermissions(RECORD_AUDIO)) {
                    if (!isRecording) {
                        isRecording = true;
                        binding.mic.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                        binding.visualizer.setVisibility(View.VISIBLE);
                        binding.emoji.setVisibility(View.GONE);
                        binding.chatEtText.setVisibility(View.GONE);
                        binding.recordTimer.setVisibility(View.VISIBLE);
                        binding.imageView33.setVisibility(View.VISIBLE);
                        binding.recordTimer.setBase(SystemClock.elapsedRealtime());
                        binding.recordTimer.start();
                        Utils.asyncTask(() ->{
                            mediaRecorder = new MediaRecorder();
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            mediaRecorder.setOutputFile(SettingsHelper.getRecordFilePath(getApplicationContext()));
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            handler.post(updateVisualizer);
                            try {
                                mediaRecorder.prepare();
                            } catch (IOException e) {e.printStackTrace();}
                            mediaRecorder.start();
                        });
                    } else {
                        binding.chatEtText.setVisibility(View.GONE);
                        binding.emoji.setVisibility(View.GONE);
                        binding.imageView33.setVisibility(View.GONE);
                        binding.recordTimer.setVisibility(View.GONE);
                        binding.recordTimer.stop();
                        binding.voiceNoteToSend.getRoot().setVisibility(View.VISIBLE);
                        binding.mic.setImageResource(R.drawable.ic_baseline_send_24);
                        Utils.asyncTask(() ->{isRecording = false;
                            mediaRecorder.stop();mediaRecorder.release();mediaRecorder = null;
                            try {
                                file = new File(SettingsHelper.getRecordFilePath(getApplicationContext()));
                                fileSize = Integer.parseInt(String.valueOf(file.length() / 1024));
                                byte[] fileContent = Files.readAllBytes(file.toPath());
                                encodeString = Base64.getEncoder().encodeToString(fileContent);
                                AndroidNetworking.upload("https://apiconnact.telebroad.com/v0/file/u/?")
                                        .addHeaders("x-tinode-apikey", "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo")
                                        .addHeaders("x-tinode-auth", "Token " + secret)
                                        .addMultipartFile("file", file).addMultipartParameter("id", "121165")
                                        .setPriority(Priority.HIGH).build().setUploadProgressListener((bytesUploaded, totalBytes) -> {
                                        }).getAsString(new StringRequestListener() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                                                    ctrls.add(gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class));
                                                    finishedRecording = true;
                                                } catch (Exception e) {e.printStackTrace();}
                                            }@Override
                                            public void onError(ANError anError) {}
                                        });
                            } catch (Exception e) {e.printStackTrace();}
                        });
                    }
                }else ActivityCompat.requestPermissions(ChatReplyActivity.this, new String[]{RECORD_AUDIO,},REQUEST_AUDIO_CODE);
            } else if (binding.imgToSend.getVisibility()==View.GONE && binding.chatEtText.getText().toString().isEmpty() && finishedRecording) {
                Extra extra = new Extra();
                List<String> attachments5 = new ArrayList<>();
                for (CtrlMessage ctr : ctrls) {
                    id = ctr.getId();
                    String url = ctr.getParams().getUrl(); //.replace(".bin","");
                    attachments5.add(url);
                    extra.setAttachments(attachments5);
                    String urlEnd = url.substring(url.lastIndexOf(".") + 1);
                    if ("121165".equals(id) && urlEnd.equals("bin")) {
                        pubMessage.Attachments attachments = new pubMessage.Attachments("voice_note" +
                                SettingsHelper.getDateString() + ".wav", url, "audio/wav", ctr.getParams().getExpires(), true);
                        attachmentsList.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head = pubMessage.setHeadWithAttachmentsForReplies(attachmentsList,chat.getSeq(),mentions);
                    }
                }
                pub = pubMessage.setPubForImages(head, id, SettingsHelper.getString(SettingsHelper.MY_TOPIC), topic, binding.chatEtText.getText().toString());
                ChatWebSocket.getInstance().sendTwoObjects("pub", pub,"extra",extra);ClearViews();
            }
        });
        binding.imageView.setOnClickListener(v -> new PhotoSourceDialogList(ChatReplyActivity.this::handleImageURI).show(getSupportFragmentManager(), "chooseSource"));
        binding.imageView33.setOnClickListener(v -> {
            binding.visualizer.animate().translationY(binding.voiceNoteToSend.getRoot().getHeight()).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);binding.visualizer.animate().translationY(0);binding.visualizer.setAlpha(1.0f);ClearViews();binding.visualizer.animate().setListener(null);}});
            binding.recordTimer.animate().translationY(binding.recordTimer.getHeight()).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);binding.recordTimer.setAlpha(1.0f);binding.recordTimer.animate().translationY(0);binding.recordTimer.animate().setListener(null);}});
            binding.imageView33.animate().translationY(binding.imageView33.getHeight()).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);binding.imageView33.setAlpha(1.0f);binding.imageView33.animate().translationY(0);binding.imageView33.animate().setListener(null);}
            });
        });
        binding.voiceNoteToSend.cancelImg.setOnClickListener(v -> {
            binding.voiceNoteToSend.getRoot().animate().translationY(binding.voiceNoteToSend.getRoot().getHeight()).alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);ClearViews();binding.voiceNoteToSend.getRoot().setAlpha(1.0f);binding.voiceNoteToSend.getRoot().animate().translationY(0);}});
        });
        binding.voiceNoteToSend.playChat.setOnClickListener(v -> {
            if (isPlaying) pauseAudio();
            else {
                try {
                    if (audioHasStarted && !audioHasFinished) resumeAudio();
                    else {playAudio();audioHasFinished =false;}
                } catch (IOException e) {e.printStackTrace();}
            }
        });
    }

    @Override
    public void onComplete(List<ChannelDB> result) {
        Set<ChannelDB> uniqueSet = new HashSet<>(result);
        newList = new ArrayList<>(uniqueSet);
    }

    private void handleImageURI(List<Uri> extraPhotoURI) throws IOException {
        binding.progressBar.setVisibility(View.VISIBLE);
        Utils.asyncTask(() ->{
            int counter = 0;
            for (Uri uri : extraPhotoURI) {
                counter++;
                if (uri.getPath().contains("video")){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        File tempFile = File.createTempFile("data", ".mp4", getFilesDir());
                        OutputStream outputStream = new FileOutputStream(tempFile);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();
                        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                        httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://apiconnact.telebroad.com/v0/")
                                .client(httpClient.build())
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                        FileUploadService service = retrofit.create(FileUploadService.class);
                        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), "121164");
                        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("video/*"), tempFile);
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", tempFile.getName(), fileRequestBody);
                        Call<ResponseBody> call = service.uploadFile("AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo", "Token " + secret, id, filePart);
                        int finalCounter1 = counter;
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                String responseBodyString = null;
                                try {responseBodyString = response.body().string();} catch (IOException e) {e.printStackTrace();}
                                JsonObject jsonObject = JsonParser.parseString(responseBodyString).getAsJsonObject();
                                ctrls.add(gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class));
                                binding.imgToSend.setVisibility(View.VISIBLE);
                                ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.relTxtLayout.getLayoutParams();
                                constraintLayout.topMargin = 0;
                                constraintLayout.leftMargin = 0;
                                constraintLayout.rightMargin = 10;
                                binding.relTxtLayout.setLayoutParams(constraintLayout);
                                binding.mic.setImageResource(R.drawable.ic_baseline_send_24);
                                if (finalCounter1 == extraPhotoURI.size() && ctrls.size() > 0) DisplayImages();
                            }
                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {}
                        });

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        BitmapFactory.decodeStream(inputStream);
                        Bitmap bitmap = Utils.getBitmapFromURI(getContentResolver(), uri);
                        File fileToSend = File.createTempFile("data", ".jpg", getFilesDir());
                        FileOutputStream stream = new FileOutputStream(fileToSend);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        stream.close();
                        inputStream.close();
                        compressed = true;
                        //BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),bitmap);
                        //binding.chatEtText.setCompoundDrawablesWithIntrinsicBounds(bitmapDrawable, null, null, null);
                        int finalCounter = counter;
                        AndroidNetworking.upload("https://apiconnact.telebroad.com/v0/file/u/?")
                                .addHeaders("x-tinode-apikey", "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo")
                                .addHeaders("x-tinode-auth", "Token " + secret)
                                .addMultipartFile("file", fileToSend)
                                .addMultipartParameter("id", "121164")
                                .setPriority(Priority.HIGH).build().setUploadProgressListener((bytesUploaded, totalBytes) -> {
                                }).getAsString(new StringRequestListener() {
                                    @Override
                                    public void onResponse(String response) {
                                        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                                        ctrls.add(gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class));
                                        binding.imgToSend.setVisibility(View.VISIBLE);
                                        ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.relTxtLayout.getLayoutParams();
                                        constraintLayout.topMargin = 0;
                                        constraintLayout.leftMargin = 0;
                                        constraintLayout.rightMargin = 10;
                                        binding.relTxtLayout.setLayoutParams(constraintLayout);
                                        binding.mic.setImageResource(R.drawable.ic_baseline_send_24);
                                        if (finalCounter == extraPhotoURI.size() && ctrls.size() > 0) DisplayImages();
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                    }
                                });
                        byte[] fileContent = Files.readAllBytes(fileToSend.toPath());
                        //encodeString = Base64.getEncoder().encodeToString(fileContent);
                        //fileSize = Integer.parseInt(String.valueOf(file.length() / 1024));
                    } catch (Exception e) {e.printStackTrace();}
                }
            }
        });
    }
    private void DisplayImages(){
        ImagesUriRV imagesUriRV = new ImagesUriRV(new ImagesUriRV.DIFF_CALLBACK(), ChatReplyActivity.this);
        binding.imgToSend.setLayoutManager(new LinearLayoutManager(ChatReplyActivity.this, LinearLayoutManager.HORIZONTAL, false));
        binding.imgToSend.setAdapter(imagesUriRV);
        imagesUriRV.submitList(ctrls);
        binding.progressBar.setVisibility(View.GONE);
    }
    public static class ChatDiff extends DiffUtil.ItemCallback<Replies> {
        @Override
        public boolean areItemsTheSame(@NonNull Replies oldItem, @NonNull Replies newItem) {
            return String.valueOf(oldItem.getSeq()).equals(String.valueOf(newItem.getSeq()));
        }

        @Override
        @SuppressLint("DiffUtilEquals")
        public boolean areContentsTheSame(@NonNull Replies oldItem, @NonNull Replies newItem) {
            if (!oldItem.getContent().equals(newItem.getContent())) return false;
            if (oldItem.getDate().toInstant().getEpochSecond() != newItem.getDate().toInstant().getEpochSecond()) return false;
            //if (!oldItem.getReplies().equals(newItem.getReplies()))return false;
            if (oldItem.getImageURL() != null && newItem.getImageURL() != null &&!oldItem.getImageURL().equals(newItem.getImageURL()))return false;
            //if (!oldItem.getEdits().equals(newItem.getEdits()))return false;
            return oldItem.getTopic().equals(newItem.getTopic());
        }
    }

    public class RepliesAdapter extends ListAdapter<Replies, RepliesAdapter.RootViewHolder> {
        List<Replies> tempList;

        protected RepliesAdapter(@NonNull DiffUtil.ItemCallback<Replies> diffCallback) {
            super(diffCallback);
        }

        @Override
        public void submitList(@Nullable List<Replies> list) {
            tempList = list == null ? new ArrayList<>() : new ArrayList<>(list);
            if (tempList.size()>0 && getCurrentList().size()>0 && list.get(tempList.size()-1).getSeq() != getCurrentList().get(getCurrentList().size()-1).getSeq()) {
                min = tempList.get(tempList.size()-1).getSeq()-getCurrentList().get(getCurrentList().size()-1).getSeq() ;
                notifyItemChanged(list.size()-1);
            }
            super.submitList(list);
        }
        //        public RepliesAdapter(List<Replies> repliesCollectionList) {
//            if (repliesCollectionList.size()>0 && this.repliesCollectionList .size()>0 && repliesCollectionList.get(repliesCollectionList.size()-1).getSeq() != this.repliesCollectionList.get(this.repliesCollectionList.size()-1).getSeq()) {
//                min = repliesCollectionList.get(repliesCollectionList.size()-1).getSeq()-this.repliesCollectionList .get(this.repliesCollectionList .size()-1).getSeq() ;
//                notifyItemChanged(repliesCollectionList.size()-1);
//            }
//            this.repliesCollectionList = repliesCollectionList;
//        }

        @Override
        public int getItemViewType(int position) {
            if ( getCurrentList().get(position).getHead().getAttachments() != null){
                DataMessage.Replies.Attachments[] attachments = getCurrentList().get(position).getHead().getAttachments();
                for (DataMessage.Replies.Attachments a:attachments){
                    String type = a.getType();
                    if (type != null && a.getType().equals("audio/wav")) return 1;
                    else if (type!= null && a.getType().equals("video/webm;codecs=vp8"))return 3;
                    else if (type == null || type.equals("text/plain") || type.equals("application/pdf")|| type.equals("application/zip")
                            || type.equals("text/html")  || type.equals("text/javascript")|| type.equals("application/msword") ||type.equals("application/vnd.ms-powerpoint")
                            ||type.equals("application/vnd.ms-excel")||type.equals("application/x-rar-compressed") ||type.equals("application/rtf"))return 4;
                }
            }
            return super.getItemViewType(position);
        }
        @NonNull @Override
        public RootViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1){ return new VoiceNoteViewHolder(VoicnotesChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));}
            else if (viewType == 3){return new VideoViewHolder(VideoChatViewBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));}
            else if (viewType == 4){return new FileHolder(ChatFilesLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));}
            else{ return new ViewHolder(ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));}
        }

        @Override
        public void onBindViewHolder(@NonNull RepliesAdapter.RootViewHolder holder, int position) {
            if (holder instanceof VoiceNoteViewHolder){
                onBindVoiceNoteHolder((VoiceNoteViewHolder) holder, position);
            }else if (holder instanceof ViewHolder){try {onBindHolder((ViewHolder) holder, position);} catch (UnsupportedEncodingException e) {e.printStackTrace();}
            }else if (holder instanceof VideoViewHolder) {try {onBindVoicHolder((VideoViewHolder)holder,position);} catch (UnsupportedEncodingException e) {e.printStackTrace();}}
            else if (holder instanceof FileHolder){try {onBindFileHolder((FileHolder)holder,position);} catch (Exception e) {e.printStackTrace();}}
        }
        private void onBindFileHolder(@NonNull FileHolder holder, int position) throws UnsupportedEncodingException {
            int newLinePosition1 = tempList.size()-min;
            if (min>0 && position== newLinePosition1){
                holder.binding.vNewLine.setVisibility(View.VISIBLE);
                holder.binding.txtNew.setVisibility(View.VISIBLE);
            }else {
                holder.binding.vNewLine.setVisibility(View.GONE);
                holder.binding.txtNew.setVisibility(View.GONE);
            }
            Replies replies = getCurrentList().get(position);
            String senderName = replies.getSenderName();
            if (replies.getContent().isEmpty()) holder.binding.txtContent.setVisibility(View.GONE);
            else {
                holder.binding.txtContent.setVisibility(View.VISIBLE);
                holder.binding.txtContent.setText(SettingsHelper.reformatHTML(replies.getContent()));
            }
            boolean isSenderTheSame = position != 0 && replies.getSignature().equals(getCurrentList().get(position - 1).getSignature());
            if (isSenderTheSame || senderName.equals("Meetings Bot")) {
                holder.binding.txtSenderName.setVisibility(View.GONE);
                holder.binding.txtTimeChatM.setVisibility(View.GONE);
                holder.binding.imageView32.setVisibility(View.GONE);
                holder.binding.txtClock.setVisibility(View.GONE);
                holder.binding.getRoot().setOnClickListener(v -> {
                    if (holder.binding.txtClock.getVisibility()==View.VISIBLE)holder.binding.txtClock.setVisibility(View.GONE);
                    else holder.binding.txtClock.setVisibility(View.VISIBLE);
                });
            } else {
                //root.binding.txtChatTime.setVisibility(View.GONE);
                holder.binding.txtSenderName.setVisibility(View.VISIBLE);
                holder.binding.txtTimeChatM.setVisibility(View.VISIBLE);
                holder.binding.imageView32.setVisibility(View.VISIBLE);
                holder.binding.txtSenderName.setText(senderName);
                String t = sdf.format(replies.getDate());
                holder.binding.txtTimeChatM.setText(t);
                String imageUrl = replies.getImageURL();
                if (imageUrl == null) holder.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                else Glide.with(ChatReplyActivity.this).load(imageUrl).circleCrop().into(holder.binding.imageView32);
                if(senderName.equals("Unknown") && imageUrl == null){
                    holder.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                    holder.binding.txtSenderName.setText(senderName);
                }
            }
            String attachments = gson.toJson(replies.getHead().getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
            if (enums.size() == 1) {
                for (Attachments a : enums) {
                    String path = a.getPath();
                    String ext = path.substring(path.lastIndexOf("."));
                    String type = path.substring(path.lastIndexOf("/") + 1);
                    holder.binding.btnFile.setText("OPEN " + type + " FILE");
                    String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                    holder.binding.dropDown.setOnClickListener(v -> {
                        PopupMenu popupMenu = new PopupMenu(ChatReplyActivity.this, v);
                        popupMenu.inflate(R.menu.chat_file_menu);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            switch (item.getItemId()) {
                                case R.id.download:
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(newURL));
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setTitle("Downloading File"); // Set the title of the download notification
                                    request.setDescription("Downloading"); // Set the description of the download notification
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "filename" + ext); // Set the destination path and file name
                                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                    downloadManager.enqueue(request);
                                    return true;
                                case R.id.delete:
                                    List<DelMessage.Delseq> delseqList = new ArrayList<>();
                                    DelMessage.Delseq delseq = new DelMessage.Delseq(getCurrentList().get(position).getSeq());
                                    delseqList.add(delseq);
                                    DelMessage delMessage = new DelMessage("delete", getCurrentList().get(position).getTopic(), "msg", true, delseqList);
                                    ChatWebSocket.getInstance().sendObject("del", delMessage);
                                    for (DelMessage.Delseq d : delseqList) {
                                        Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteMassage(topic, d.getLow()));
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        });
                        popupMenu.show();
                    });
                    holder.binding.btnFile.setOnClickListener(v -> {
                        try {
                            url1 = new URL(newURL);
                            openFile(a.getType());
                        } catch (URISyntaxException | MalformedURLException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }else if (enums.size() >1){
                holder.binding.fileCardView.setVisibility(View.GONE);
                holder.binding.imgRV.setVisibility(View.VISIBLE);
                ArrayList<FilesAdapterType> strings = enums.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
                PhotoAdapter photoAdapter = new PhotoAdapter(strings,replies);
                photoAdapter.notifyDataSetChanged();
                holder.binding.imgRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.binding.imgRV.setAdapter(photoAdapter);
            }else {
                holder.binding.fileCardView.setVisibility(View.GONE);
                holder.binding.imgRV.setVisibility(View.GONE);
            }
            if (replies.getReaction() != null){
                holder.binding.fileReactions.setVisibility(View.VISIBLE);
                LiveData<List<DataMessage.Reaction>> Rld =chatReactionsViewModel.getReactions(replies.getSeq()+"%",topic);
                Rld.observe(ChatReplyActivity.this, reactions -> {
                    ChatActivity.ReactionAdapter adapter = new ChatActivity.ReactionAdapter(reactions);
                    adapter.notifyDataSetChanged();
                    holder.binding.fileReactions.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                    holder.binding.fileReactions.setAdapter(adapter);
                });
            }else holder.binding.fileReactions.setVisibility(View.GONE);
        }

        private void onBindVoicHolder(@NonNull VideoViewHolder holder, int position) throws UnsupportedEncodingException {
            int newLinePosition1 = tempList.size()-min;
            if (min>0 && position== newLinePosition1){
                holder.videoChatViewBinding.vNewLine.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.txtNew.setVisibility(View.VISIBLE);
            }else {
                holder.videoChatViewBinding.vNewLine.setVisibility(View.GONE);
                holder.videoChatViewBinding.txtNew.setVisibility(View.GONE);
            }
            Replies replies = getCurrentList().get(position);
            String senderName = replies.getSenderName();
            boolean isSenderTheSame = position != 0 && replies.getSignature().equals(getCurrentList().get(position - 1).getSignature());
            if (isSenderTheSame || senderName.equals("Meetings Bot")) {
                holder.videoChatViewBinding.txtSenderName.setVisibility(View.GONE);
                holder.videoChatViewBinding.txtTimeChatM.setVisibility(View.GONE);
                holder.videoChatViewBinding.imageView32.setVisibility(View.GONE);
                holder.videoChatViewBinding.txtClock.setVisibility(View.GONE);
                holder.videoChatViewBinding.getRoot().setOnClickListener(v -> {
                    if (holder.videoChatViewBinding.txtClock.getVisibility()==View.VISIBLE)holder.videoChatViewBinding.txtClock.setVisibility(View.GONE);
                    else holder.videoChatViewBinding.txtClock.setVisibility(View.VISIBLE);
                });
            } else {
                //root.binding.txtChatTime.setVisibility(View.GONE);
                holder.videoChatViewBinding.txtSenderName.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.txtTimeChatM.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.imageView32.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.txtSenderName.setText(senderName);
                String t = sdf.format(replies.getDate());
                holder.videoChatViewBinding.txtTimeChatM.setText(t);
                String imageUrl = replies.getImageURL();
                if (imageUrl == null) holder.videoChatViewBinding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                else Glide.with(ChatReplyActivity.this).load(imageUrl).circleCrop().into(holder.videoChatViewBinding.imageView32);
                if(senderName.equals("Unknown") && imageUrl == null){
                    holder.videoChatViewBinding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                    holder.videoChatViewBinding.txtSenderName.setText(senderName);
                }
            }
            String attachments = gson.toJson(replies.getHead().getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
            if (enums.size()==1) {
                for (Attachments a : enums) {
                    String path = a.getPath();
                    String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                    holder.videoChatViewBinding.chatVideoThumbNail.setOnClickListener(v -> {
                        String replies1 = gson.toJson(getCurrentList().get(position));
                        Intent intent = new Intent(ChatReplyActivity.this, CheatVideoPlaying.class);
                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatReplyActivity.this, v, "lets_play_it");
                        intent.putExtra("videoURL", newURL);
                        intent.putExtra("replyAttachments", attachments);
                        intent.putExtra("repliesCollectionList", replies1);
                        startActivity(intent, optionsCompat.toBundle());
                        //finish();
                    });
                    Glide.with(ChatReplyActivity.this).asBitmap().load(newURL).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.videoChatViewBinding.chatVideoThumbNail);
                }
            }else if (enums.size()>1){
                holder.videoChatViewBinding.PlayVideo.setVisibility(View.GONE);
                holder.videoChatViewBinding.chatVideoThumbNail.setVisibility(View.GONE);
                holder.videoChatViewBinding.imgRV.setVisibility(View.VISIBLE);
                ArrayList<FilesAdapterType> strings = enums.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
                PhotoAdapter photoAdapter = new PhotoAdapter(strings,replies);
                photoAdapter.notifyDataSetChanged();
                holder.videoChatViewBinding.imgRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.videoChatViewBinding.imgRV.setAdapter(photoAdapter);
            }else {
                holder.videoChatViewBinding.PlayVideo.setVisibility(View.GONE);
                holder.videoChatViewBinding.chatVideoThumbNail.setVisibility(View.GONE);
                holder.videoChatViewBinding.imgRV.setVisibility(View.GONE);
            }
            if (replies.getReaction() != null) {
                holder.videoChatViewBinding.videoReaction.setVisibility(View.VISIBLE);
                chatReactionsViewModel.getReactions(replies.getSeq() + "%", topic).observe(ChatReplyActivity.this, reactions -> {
                    ChatActivity.ReactionAdapter adapter = new ChatActivity.ReactionAdapter(reactions);
                    adapter.notifyDataSetChanged();
                    holder.videoChatViewBinding.videoReaction.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                    holder.videoChatViewBinding.videoReaction.setAdapter(adapter);
                });
            } else holder.videoChatViewBinding.videoReaction.setVisibility(View.GONE);
        }
        private void onBindVoiceNoteHolder(@NonNull VoiceNoteViewHolder root, int position) {
            int newLinePosition1 =tempList.size()-min;
            if (min>0 && position== newLinePosition1){
                root.voicnotesChatBinding.vNewLine.setVisibility(View.VISIBLE);
                root.voicnotesChatBinding.txtNew.setVisibility(View.VISIBLE);
            }else {
                root.voicnotesChatBinding.vNewLine.setVisibility(View.GONE);
                root.voicnotesChatBinding.txtNew.setVisibility(View.GONE);
            }
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_telebroad_logo_only);
            Replies replies = getCurrentList().get(position);
            String senderName = replies.getSenderName();
            root.voicnotesChatBinding.txtSenderName.setText(senderName);
            String t = sdf.format(replies.getDate());
            String clock = sdft.format(replies.getDate());
            root.voicnotesChatBinding.txtTimeChatM.setText(t);
            //root.binding.txtChatTime.setText(clock);
            boolean isSenderTheSame = position != 0 && replies.getSignature().equals(getCurrentList().get(position - 1).getSignature());
            if (isSenderTheSame || senderName.equals("Meetings Bot")) {
                root.voicnotesChatBinding.txtSenderName.setVisibility(View.GONE);
                root.voicnotesChatBinding.txtTimeChatM.setVisibility(View.GONE);
                root.voicnotesChatBinding.imageView32.setVisibility(View.GONE);
                root.voicnotesChatBinding.txtClock.setVisibility(View.VISIBLE);
                root.voicnotesChatBinding.getRoot().setOnClickListener(v -> {
                    if (root.voicnotesChatBinding.txtClock.getVisibility()==View.VISIBLE)root.voicnotesChatBinding.txtClock.setVisibility(View.GONE);
                    else root.voicnotesChatBinding.txtClock.setVisibility(View.VISIBLE);
                });
            } else {
                //root.binding.txtChatTime.setVisibility(View.GONE);
                root.voicnotesChatBinding.txtSenderName.setVisibility(View.VISIBLE);
                root.voicnotesChatBinding.txtTimeChatM.setVisibility(View.VISIBLE);
                root.voicnotesChatBinding.imageView32.setVisibility(View.VISIBLE);
                String imageUrl = replies.getImageURL();
                if (imageUrl == null) root.voicnotesChatBinding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                else Glide.with(ChatReplyActivity.this).load(imageUrl).circleCrop().into(root.voicnotesChatBinding.imageView32);
                if(senderName.equals("Unknown") && imageUrl == null){
                    root.voicnotesChatBinding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                    root.voicnotesChatBinding.txtSenderName.setText(senderName);
                }
            }
            String ab = SpeedT.get(i);
            root.voicnotesChatBinding.include.txtSpeed.setText(ab);
            root.voicnotesChatBinding.include.speedLayout.setOnClickListener(v -> {
                if (i != 2 && i < 2) {i++;
                    String ab1 = SpeedT.get(i);
                    root.voicnotesChatBinding.include.txtSpeed.setText(ab1);
                } else {i = 0;
                    String ab1 = SpeedT.get(i);
                    root.voicnotesChatBinding.include.txtSpeed.setText(ab1);
                }if (root.voicnotesChatBinding.include.txtSpeed.getText().equals("1")) {SpeedState = 1F;
                    Bundle bundle = new Bundle();
                    bundle.putFloat("SpeedProgress", SpeedState);
                    MediaControllerCompat.getMediaController(ChatReplyActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                } else if (root.voicnotesChatBinding.include.txtSpeed.getText().equals("1.5")) {SpeedState = 1.5F;
                    Bundle bundle = new Bundle();
                    bundle.putFloat("SpeedProgress", SpeedState);
                    MediaControllerCompat.getMediaController(ChatReplyActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                } else if (root.voicnotesChatBinding.include.txtSpeed.getText().equals("2")) {SpeedState = 2F;
                    Bundle bundle = new Bundle();
                    bundle.putFloat("SpeedProgress", SpeedState);
                    MediaControllerCompat.getMediaController(ChatReplyActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                }
            });root.voicnotesChatBinding.playChat1.setOnClickListener(v -> {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                boolean isMuted = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                if (isMuted){Toast.makeText(ChatReplyActivity.this, "Please turn the volume up", Toast.LENGTH_SHORT).show();}
                String attachments = gson.toJson(replies.getHead().getAttachments());
                Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
                Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
                if (enums.size()==1) {
                    for (Attachments a : enums) {
                        String path = a.getPath();
                        try {
                            currentPlay = root.voicnotesChatBinding.playChat1;
                            seekBar = root.voicnotesChatBinding.sb;
                            // textView = root.binding.textView40;
                            String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                            if (!newURL.equals(url)) {
                                url = newURL;
                                Uri myUri = Uri.parse(url);
                                voicemailViewModel.addURItoQueueChat(MediaControllerCompat.getMediaController(ChatReplyActivity.this), myUri, senderName, bitmap);
                                //root.binding.textView40.setText((Utils.formatLongMilliSeconds(longProgress)));
                                //initReceiver();
                                changePlayback();
                            } else {
                                int pbState = MediaControllerCompat.getMediaController(ChatReplyActivity.this).getPlaybackState().getState();
                                if (pbState == PlaybackStateCompat.STATE_PLAYING) changePlayback();
                                else if (pbState == PlaybackStateCompat.STATE_PAUSED)
                                    changePlayback();
                            }
                        } catch (Exception e) {
                        }
                    }
                }else if (enums.size()>1){
                    for (Attachments a : enums) {
                        if (a.getType().equals("audio/wav")){
                            root.voicnotesChatBinding.playChat1.setOnClickListener(v1 -> {
                                AudioManager audioManager1 = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                boolean isMuted1 = audioManager1.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                                if (isMuted1){Toast.makeText(ChatReplyActivity.this, "Please turn the volume up", Toast.LENGTH_SHORT).show();}
                                String path = a.getPath();
                                try {currentPlay = root.voicnotesChatBinding.playChat1;seekBar = root.voicnotesChatBinding.sb;
                                    // textView = root.binding.textView40;
                                    String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                                    if (!newURL.equals(url)) {
                                        url = newURL;
                                        Uri myUri = Uri.parse(url);
                                        voicemailViewModel.addURItoQueueChat(MediaControllerCompat.getMediaController(ChatReplyActivity.this), myUri,senderName,bitmap);
                                        //root.binding.textView40.setText((Utils.formatLongMilliSeconds(longProgress)));
                                        //initReceiver();
                                        changePlayback();
                                    } else {
                                        int pbState = MediaControllerCompat.getMediaController(ChatReplyActivity.this).getPlaybackState().getState();
                                        if (pbState == PlaybackStateCompat.STATE_PLAYING )changePlayback();
                                        else if (pbState == PlaybackStateCompat.STATE_PAUSED) changePlayback();}} catch (Exception e) {}
                            });
                        }
                        root.voicnotesChatBinding.imgRV.setVisibility(View.VISIBLE);
                        ArrayList<FilesAdapterType> strings = enums.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
                        PhotoAdapter photoAdapter = new PhotoAdapter(strings,replies);
                        photoAdapter.notifyDataSetChanged();
                        root.voicnotesChatBinding.imgRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                        root.voicnotesChatBinding.imgRV.setAdapter(photoAdapter);
                    }
                }else root.voicnotesChatBinding.imgRV.setVisibility(View.GONE);
            });
            VoicemailPlayingService.getLiveState().observe(ChatReplyActivity.this, integer -> {
                if (integer == ExoPlayer.STATE_ENDED) {
                    if (MediaControllerCompat.getMediaController(ChatReplyActivity.this).getTransportControls() != null) {
                        MediaControllerCompat.getMediaController(ChatReplyActivity.this).getTransportControls().seekTo(0);
                        MediaControllerCompat.getMediaController(ChatReplyActivity.this).getTransportControls().pause();
                        currentPlay.setImageResource(R.drawable.ic_outline_play_arrow_24);
                        root.voicnotesChatBinding.sb.setProgress(0, true);
                    }
                }
            });root.voicnotesChatBinding.sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) MediaControllerCompat.getMediaController(ChatReplyActivity.this).getTransportControls().seekTo(progress);}
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}@Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            if (replies.getReaction() != null) {
                root.voicnotesChatBinding.reactionRecycler.setVisibility(View.VISIBLE);
                chatReactionsViewModel.getReactions(replies.getSeq() + "%", topic).observe(ChatReplyActivity.this, reactions -> {
                    ChatActivity.ReactionAdapter adapter = new ChatActivity.ReactionAdapter(reactions);
                    adapter.notifyDataSetChanged();
                    root.voicnotesChatBinding.reactionRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                    root.voicnotesChatBinding.reactionRecycler.setAdapter(adapter);
                });
            } else root.voicnotesChatBinding.reactionRecycler.setVisibility(View.GONE);
        }
        
        private void onBindHolder(ViewHolder holder, int position) throws UnsupportedEncodingException {
            int newLinePosition1 = tempList.size()-min;
            if (min>0 && position== newLinePosition1){
                holder.itemChatBinding.vNewLine.setVisibility(View.VISIBLE);
                holder.itemChatBinding.txtNew.setVisibility(View.VISIBLE);
            }else {
                holder.itemChatBinding.vNewLine.setVisibility(View.GONE);
                holder.itemChatBinding.txtNew.setVisibility(View.GONE);
            }
            Replies replies = getCurrentList().get(position);
            holder.itemChatBinding.contentView.setText(SettingsHelper.reformatHTML(getCurrentList().get(position).getContent()));
            boolean isSenderTheSame = position != 0 && getCurrentList().get(position).getSignature().equals(getCurrentList().get(position - 1).getSignature());
            if (getCurrentList().get(position).getContent() != null) {
                if (SettingsHelper.reformatHTML(getCurrentList().get(position).getContent()).toString().equals("null") || getCurrentList().get(position).getContent().isEmpty()) holder.itemChatBinding.contentView.setVisibility(View.GONE);
                else {
                    holder.itemChatBinding.contentView.setVisibility(View.VISIBLE);
                    holder.itemChatBinding.contentView.setText(SettingsHelper.reformatHTML(getCurrentList().get(position).getContent()));
                }
                if (isSenderTheSame || getCurrentList().get(position).getSenderName().equals("Meetings Bot")) {
                    holder.itemChatBinding.txtSenderName.setVisibility(View.GONE);
                    holder.itemChatBinding.txtTimeChatM.setVisibility(View.GONE);
                    holder.itemChatBinding.imageView32.setVisibility(View.GONE);
                    holder.itemChatBinding.txtChatTime.setVisibility(View.GONE);
                    holder.itemChatBinding.getRoot().setOnClickListener(v -> {
                        if (holder.itemChatBinding.txtChatTime.getVisibility()==View.VISIBLE)holder.itemChatBinding.txtChatTime.setVisibility(View.GONE);
                        else holder.itemChatBinding.txtChatTime.setVisibility(View.VISIBLE);
                    });
                } else {
                    holder.itemChatBinding.txtSenderName.setText(getCurrentList().get(position).getSenderName());
                    holder.itemChatBinding.txtChatTime.setVisibility(View.GONE);
                    holder.itemChatBinding.txtSenderName.setVisibility(View.VISIBLE);
                    holder.itemChatBinding.txtTimeChatM.setVisibility(View.VISIBLE);
                    holder.itemChatBinding.imageView32.setVisibility(View.VISIBLE);
                }
            }
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        ChatReplyActivity.this.runOnUiThread(() -> {
                            String dateFormat1 = sdf.format(SettingsHelper. getDate(getCurrentList().get(position).getTs()));
                            String dateClockFormat1 = sdft.format(SettingsHelper. getDate(getCurrentList().get(position).getTs()));
                            //long timeAgo = chat.getDate().getTime();
                            long timeAgo = SettingsHelper.getDate(getCurrentList().get(position).getTs()).getTime();
                            String time = SettingsHelper.getTimeAgo(timeAgo, dateFormat1, dateClockFormat1);
                            String clockTime = SettingsHelper.getTimeAgoInClockTime(timeAgo, dateFormat1, dateClockFormat1);
                            holder.itemChatBinding.txtTimeChatM.setText(time);
                            holder.itemChatBinding.txtChatTime.setText(clockTime);
                        });
                    }
                }, 0, 60 * 1000);
                // holder.itemChatBinding.txtTimeChatM.setText(dateFormat);
                if (replies.getImageURL() !=null)Glide.with(ChatReplyActivity.this).load(replies.getImageURL()).circleCrop().into(holder.itemChatBinding.imageView32);
                else holder.itemChatBinding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(replies.getSenderName()));
            if (getCurrentList().get(position).getHead().getAttachments() != null && getCurrentList().get(position).getHead().getAttachments().length == 1 ){
                for (DataMessage.Replies.Attachments a : getCurrentList().get(position).getHead().getAttachments()) {
                    String path = a.getPath();
                    String type = a.getType();
                    if (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/jpg") || type.equals("image/gif") || type.equals("image/webp") || type.equals("image/tiff") || type.equals("image/raw") || type.equals("image/bmp") || type.equals("image/heif") || type.equals("image/jpeg2000") || type.equals("image/jfif") || type.equals("image/.jfif")) {
                        holder.itemChatBinding.displayImage.setVisibility(View.VISIBLE);
                        String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                        Glide.with(ChatReplyActivity.this).load(newURL).into(holder.itemChatBinding.displayImage);
                        holder.itemChatBinding.displayImage.setTransitionName(newURL);
                    }else {
                        holder.itemChatBinding.displayImage.setVisibility(View.GONE);
                    }
                }
            }else if (getCurrentList().get(position).getHead().getAttachments() != null && getCurrentList().get(position).getHead().getAttachments().length >= 2){
                holder.itemChatBinding.displayImage.setVisibility(View.GONE);
                holder.itemChatBinding.imgRV.setVisibility(View.VISIBLE);
                ArrayList<DataMessage.Replies.Attachments> arrayList = new ArrayList<>(Arrays.asList(getCurrentList().get(position).getHead().getAttachments()));
                ArrayList<FilesAdapterType> strings = arrayList.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
                PhotoAdapter photoAdapter = new PhotoAdapter(strings,getCurrentList().get(position));
                photoAdapter.notifyDataSetChanged();
                holder.itemChatBinding.imgRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.itemChatBinding.imgRV.setAdapter(photoAdapter);
            }else {
                holder.itemChatBinding.displayImage.setVisibility(View.GONE);
                holder.itemChatBinding.imgRV.setVisibility(View.GONE);
            }
            if (getCurrentList().get(position).getReaction() != null){
                List<DataMessage.Replies.Reaction> reactions =  Arrays.stream(getCurrentList().get(position).getReaction()).collect(Collectors.toList());
                holder.itemChatBinding.reactionRecycler.setVisibility(View.VISIBLE);
                ReactionAdapter adapter = new ReactionAdapter(reactions);
                adapter.notifyDataSetChanged();
                holder.itemChatBinding.reactionRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.itemChatBinding.reactionRecycler.setAdapter(adapter);
            }
            holder.itemChatBinding.displayImage.setOnClickListener(v -> {
                String Replies = gson.toJson(getCurrentList().get(position));
                String attachments = gson.toJson(getCurrentList().get(position).getHead().getAttachments());
                Intent intent = new Intent(ChatReplyActivity.this, ChatImageActivity.class);
                intent.putExtra(MMSImageViewActivity.MMS_IMAGE_URL, v.getTransitionName());
                intent.putExtra("replyAttachments",attachments);
                intent.putExtra("repliesCollectionList",Replies);
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(ChatReplyActivity.this, v, v.getTransitionName()).toBundle());
            });
        }

        public class ViewHolder extends RootViewHolder{
            ItemChatBinding itemChatBinding;
            public ViewHolder(@NonNull ItemChatBinding itemChatBind) {
                super(itemChatBind.getRoot());
                itemChatBinding = itemChatBind;
                itemChatBinding.replyCard.setVisibility(View.GONE);
            }
        }
        public class VoiceNoteViewHolder extends RootViewHolder{
            VoicnotesChatBinding voicnotesChatBinding;
            public VoiceNoteViewHolder(@NonNull VoicnotesChatBinding itemView) {
                super(itemView.getRoot());
                voicnotesChatBinding = itemView;
            }
        }
        class FileHolder extends RootViewHolder {
            private final ChatFilesLayoutBinding binding;
            private FileHolder(ChatFilesLayoutBinding binding) {super(binding.getRoot());
                this.binding = binding;
            }
        }
        class VideoViewHolder extends RootViewHolder {
            private final VideoChatViewBinding videoChatViewBinding;
            private VideoViewHolder(VideoChatViewBinding binding) {super(binding.getRoot());
                this.videoChatViewBinding = binding;
            }
        }
        class RootViewHolder extends RecyclerView.ViewHolder {
            public RootViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
    static class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ViewHolder> {
        private final List<DataMessage.Replies.Reaction> reactions;
        public ReactionAdapter(List<DataMessage.Replies.Reaction> reactions) {
            this.reactions = reactions;
        }
        //List<List<DataMessage.Reaction>> reactions = new ArrayList<>();
        private void setReactions(List<DataMessage.Reaction> reactions) {
            //if (reactions == null) return;else this.reactions.addAll(reactions.stream().collect(Collectors.groupingBy(DataMessage.Reaction::getContent)).values());
        }

        @NonNull @Override
        public ReactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ReactionAdapter.ViewHolder(ItemReactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ReactionAdapter.ViewHolder holder, int position) {
            DataMessage.Replies.Reaction reactionList = reactions.get(position);
            holder.binding.reactionText.setText(reactionList.getContent());
            holder.binding.countText.setText("1");
        }

        @Override
        public int getItemCount() {
            return reactions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemReactionBinding binding;
            public ViewHolder(@NonNull ItemReactionBinding binding) {super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
    class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ImageViewHolder> {
        ArrayList<FilesAdapterType> imgUrls;
        Replies replies;
        public PhotoAdapter(ArrayList<FilesAdapterType> imgUrls,Replies replies) {
            this.imgUrls = imgUrls;
            this.replies = replies;
        }
        @NonNull @Override
        public PhotoAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PhotoAdapter.ImageViewHolder(ItemChatImagesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoAdapter.ImageViewHolder holder, int position) {
                String type = imgUrls.get(position).getType();
                if (type != null && type.equals("video/webm;codecs=vp8") || Objects.equals(type, "image/m4v")) {
                    holder.imagesBinding.play.setVisibility(View.VISIBLE);
                    holder.imagesBinding.fileCardView.setVisibility(View.GONE);
                    try {
                        String imgUrl = domain + imgUrls.get(position).getUrl() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                        Glide.with(ChatReplyActivity.this).load(imgUrl).into(holder.imagesBinding.displayImages);
                        holder.imagesBinding.displayImages.setTransitionName(imgUrl);
                        if (replies != null) {
                            holder.imagesBinding.displayImages.setOnClickListener(v -> {
                                String attachments = gson.toJson(replies.getHead().getAttachments());
                                String Replies = gson.toJson(replies);
                                Intent intent = new Intent(ChatReplyActivity.this, CheatVideoPlaying.class);
                                intent.putExtra("videoURL", v.getTransitionName());
                                intent.putExtra("replyAttachments", attachments);
                                intent.putExtra("repliesCollectionList", Replies);
                                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(ChatReplyActivity.this, v, v.getTransitionName()).toBundle());
                            });
                        }
                    } catch (UnsupportedEncodingException e) {e.printStackTrace();}
                } else if (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/jpg") || type.equals("image/gif") || type.equals("image/webp") || type.equals("image/tiff") || type.equals("image/raw") || type.equals("image/bmp") || type.equals("image/heif") || type.equals("image/jpeg2000") || type.equals("image/jfif") || type.equals("image/.jfif") || type.equals("Image")) {
                    holder.imagesBinding.play.setVisibility(View.GONE);
                    holder.imagesBinding.fileCardView.setVisibility(View.GONE);
                    try {
                        String imgUrl = domain + imgUrls.get(position).getUrl() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                        Glide.with(ChatReplyActivity.this).load(imgUrl).into(holder.imagesBinding.displayImages);
                        holder.imagesBinding.displayImages.setTransitionName(imgUrl);
                        if (replies != null) {
                            holder.imagesBinding.displayImages.setOnClickListener(v -> {
                                Intent intent = new Intent(ChatReplyActivity.this, ChatImageActivity.class);
                                String attachments = gson.toJson(replies.getHead().getAttachments());
                                String Replies = gson.toJson(replies);
                                intent.putExtra("replyAttachments", attachments);
                                intent.putExtra("repliesCollectionList", Replies);
                                intent.putExtra(MMSImageViewActivity.MMS_IMAGE_URL, v.getTransitionName());
                                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(ChatReplyActivity.this, v, v.getTransitionName()).toBundle());
                            });
                        }
                    } catch (UnsupportedEncodingException e) {e.printStackTrace();}
                } else if (type.equals("text/plain") || type.equals("application/pdf") || type.equals("application/zip") || type.equals("text/html") || type.equals("text/javascript") || type.equals("application/msword") || type.equals("application/vnd.ms-powerpoint") || type.equals("application/vnd.ms-excel") || type.equals("application/x-rar-compressed") || type.equals("application/rtf") || type.equals("application/json") || type.equals("application/javascript") || type.equals("text/css") || type.equals("application/vnd.android.package-archive")) {
                    holder.imagesBinding.fileCardView.setVisibility(View.VISIBLE);
                    holder.imagesBinding.play.setVisibility(View.GONE);
                    holder.imagesBinding.displayImages.setVisibility(View.GONE);
                    String type1 = imgUrls.get(position).getUrl().substring(imgUrls.get(position).getUrl().lastIndexOf("/") + 1);
                    holder.imagesBinding.btnFile.setText("OPEN " + type1 + " FILE");
                    String newURL = null;
                    try {
                        newURL = domain + imgUrls.get(position).getUrl() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String finalNewURL = newURL;
                    holder.imagesBinding.btnFile.setOnClickListener(v -> {
                        try {
                            url1 = new URL(finalNewURL);
                            openFile(imgUrls.get(position).getType());
                        } catch (URISyntaxException | MalformedURLException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    holder.imagesBinding.fileCardView.setVisibility(View.GONE);
                    holder.imagesBinding.play.setVisibility(View.GONE);
                    holder.imagesBinding.displayImages.setVisibility(View.GONE);
                }
        }

        @Override
        public int getItemCount() {
            return imgUrls.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ItemChatImagesBinding imagesBinding;
            public ImageViewHolder(@NonNull ItemChatImagesBinding imagesBinding) {super(imagesBinding.getRoot());
                this.imagesBinding = imagesBinding;
            }
        }
    }

    @Override
    protected void onPause() {super.onPause();overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);}

    public Date getDate(){
        Instant instant = Instant.parse(chat.getTs());
        Date date = Date.from(instant);
        return date;
    }
    @Override
    protected void onStart() {super.onStart();
        MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                MediaControllerCompat mediaController = new MediaControllerCompat(ChatReplyActivity.this, token);
                MediaControllerCompat.setMediaController(ChatReplyActivity.this, mediaController);
                mediaController.registerCallback(controllerCallback);
            }
        };
        if (mediaBrowser == null) {
            mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, VoicemailPlayingService.class), connectionCallback, null);
            mediaBrowser.connect();
        }
    }
    void changePlayback() {
        int pbState = MediaControllerCompat.getMediaController(ChatReplyActivity.this).getPlaybackState().getState();
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            MediaControllerCompat.getMediaController(this).getTransportControls().pause();
            currentPlay.setImageResource(R.drawable.ic_outline_play_arrow_24);
        } else if (pbState == PlaybackStateCompat.STATE_PAUSED || pbState == 0) {
            MediaControllerCompat.getMediaController(this).getTransportControls().play();
            currentPlay.setImageResource(R.drawable.ic_baseline_pause_24);
        }
    }
    private void handleMetaData(MediaMetadataCompat metadata) {
        int duration = (int) metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
        if (duration > 0) seekBar.setMax(duration);
    }
    private void ClearImageVies(){
        ctrls.clear();urls.clear();attachmentsList.clear();
        binding.imgToSend.setVisibility(View.GONE);
        binding.imgToSend.setVisibility(View.GONE);
        //binding.imgCancel.setVisibility(View.GONE);
        binding.imgToSend.setAlpha(1.0f);
        binding.imgToSend.animate().translationY(0);
        binding.imgToSend.animate().setListener(null);
        binding.chatEtText.setMaxLines(1);
        binding.mic.setImageResource(R.drawable.ic_microphone);
        ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.relTxtLayout.getLayoutParams();
        constraintLayout.topMargin = 0;
        binding.chatEtText.setText("");
        constraintLayout.leftMargin = 0;
        constraintLayout.rightMargin = 105;
        binding.relTxtLayout.setLayoutParams(constraintLayout);
    }
    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording){ // if we are already recording// get the current amplitude
                int x = mediaRecorder.getMaxAmplitude();
                binding.visualizer.addAmplitude(x); // update the VisualizeView
                binding.visualizer.invalidate(); // refresh the VisualizerView
                handler.postDelayed(this, 40);// update in 40 milliseconds
            }
        }
    };
public void ClearViews() {
    //pubMassage2 = null;
    ctrls.clear();urls.clear();attachmentsList.clear();mentions.clear();
    //mentions.clear();attachmentsList1.clear();
    binding.visualizer.setVisibility(View.GONE);
    binding.fileLayout.getRoot().setVisibility(View.GONE);
    binding.voiceNoteToSend.getRoot().setVisibility(View.GONE);
    binding.emoji.setVisibility(View.VISIBLE);
    binding.pdfLayout.getRoot().setVisibility(View.GONE);
    binding.chatEtText.setVisibility(View.VISIBLE);
    binding.recordTimer.setVisibility(View.GONE);
    binding.imageView33.setVisibility(View.GONE);
    binding.mic.setImageResource(R.drawable.ic_microphone);
    finishedRecording = false;isRecording = false;
    if (mediaRecorder != null && mediaRecorder.getMaxAmplitude()>0){mediaRecorder.stop();mediaRecorder.release();mediaRecorder = null;}
    binding.voiceNoteToSend.getRoot().setAlpha(1.0f);
    binding.voiceNoteToSend.getRoot().animate().translationY(0);
    binding.voiceNoteToSend.getRoot().animate().setListener(null);
    binding.visualizer.clear();binding.visualizer.invalidate();
    ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.relTxtLayout.getLayoutParams();
    constraintLayout.topMargin = 0;
    constraintLayout.leftMargin = 0;
    constraintLayout.rightMargin = 105;
    binding.relTxtLayout.setLayoutParams(constraintLayout);
}
    private void stopPlaying() {
        isPlaying = false;mediaPlayer.stop();
        binding.voiceNoteToSend.playChat.setImageResource(R.drawable.ic_outline_play_arrow_24);
        seekBarHandler.removeCallbacks(updateSeekbar);
    }
    private void pauseAudio(){
        mediaPlayer.pause();isPlaying = false;
        seekBarHandler.removeCallbacks(updateSeekbar);
        binding.voiceNoteToSend.playChat.setImageResource(R.drawable.ic_outline_play_arrow_24);
    }
    private void resumeAudio(){
        mediaPlayer.start();isPlaying = true;updateRunnable();
        binding.voiceNoteToSend.playChat.setImageResource(R.drawable.ic_baseline_pause_24);
        seekBarHandler.postDelayed(updateSeekbar,0);
    }
    private void playAudio() throws IOException {
        mediaPlayer = new MediaPlayer();audioHasStarted = true;
        mediaPlayer.setDataSource(SettingsHelper.getRecordFilePath(getApplicationContext()));
        mediaPlayer.prepare();mediaPlayer.start();isPlaying = true;
        binding.voiceNoteToSend.playChat.setImageResource(R.drawable.ic_baseline_pause_24);
        mediaPlayer.setOnCompletionListener(mp -> {
            stopPlaying();audioHasFinished = true;
        });
        binding.voiceNoteToSend.sb.setMax(mediaPlayer.getDuration());
        seekBarHandler = new Handler(Looper.getMainLooper());
        updateRunnable();
        binding.recordTimer.setBase(SystemClock.elapsedRealtime());
        seekBarHandler.postDelayed(updateSeekbar, 0);
    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                binding.voiceNoteToSend.sb.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this, 500);}};
    }
    private void openFile(File url) {
        try {
            Uri uri = FileProvider.getUriForFile(ChatReplyActivity.this, "com.telebroad.teleconsole.fileprovider", url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {intent.setDataAndType(uri, "application/pdf");
            }else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")){intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip")) {intent.setDataAndType(uri, "application/zip");
            } else if (url.toString().contains(".rar")){intent.setDataAndType(uri, "application/x-rar-compressed");
            } else if (url.toString().contains(".rtf")) {intent.setDataAndType(uri, "application/rtf");
            }  else if (url.toString().contains(".txt")) {intent.setDataAndType(uri, "text/plain");
            }else if (url.toString().contains(".zip")) {intent.setDataAndType(uri, "application/zip");
            } else if (url.toString().contains(".brf")) {intent.setDataAndType(uri, "application/x-brf");
            }else if (url.toString().contains(".bin")) {intent.setDataAndType(uri, "application/bin");
            }else {intent.setDataAndType(uri, "*/*");}
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ChatReplyActivity.this,"No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }
    private void openFile(String type) throws URISyntaxException {
        File file = getFile(new File(MMS_IMAGE_FOLDER),type);
        if (file.exists()) openFile(file);
        else Utils.asyncTask(() ->{if(!isNullOrEmpty(String.valueOf(url1))) downloadImage(null  ,() -> openFile(file),type);});
    }
    private void downloadImage(Runnable onStart, Runnable onComplete,String type) {
        if (url1 != null){
            File fileDir = new File(MMS_IMAGE_FOLDER);
            fileDir.mkdirs();
            final File file = getFile(fileDir,type);
            try {
                if (url1 != null){
                    if (onStart != null) runOnUiThread(onStart);
                    InputStream is = url1.openStream();
                    OutputStream os = new FileOutputStream( file);
                    byte[] b = new byte[1024];
                    int length;
                    while((length = is.read(b)) != -1){
                        os.write(b,0, length);
                    }
                    is.close();
                    os.close();
                }
            } catch (IOException e) {e.printStackTrace();} finally {
                if (onComplete != null) runOnUiThread(onComplete);
            }
        }
    }
    private File getFile(File fileDir,String type) {
        String urlFile = url1.getFile();
        String tillExtension = urlFile.substring(0, urlFile.indexOf('?'));
        //String Extension = tillExtension.substring(urlFile.indexOf('.'));
        String Extension = null;
        if ("application/msword".equals(type)) Extension = ".doc";
        else if ("application/pdf".equals(type))Extension= ".pdf";
        else if ("text/html".equals(type))Extension=".html";
        else if ("application/vnd.ms-powerpoint".equals(type))Extension=".pptx";
        else if ("application/vnd.ms-excel".equals(type))Extension = ".xlsx";
        else if ("application/zip".equals(type))Extension = ".zip";
        else if ("application/x-rar-compressed".equals(type))Extension=".rar";
        else if ("application/rtf".equals(type))Extension = ".rtf";
        else if ("text/plain".equals(type))Extension=".txt";
        else if ("application/x-brf".equals(type))Extension=".brf";
        else if ("application/bin".equals(type))Extension =".bin";
        else if ("application/json".equals(type))Extension=".json";
        else if ("application/javascript".equals(type))Extension =".js";
        else if ("text/css".equals(type))Extension=".css";
        else Extension = tillExtension.substring(urlFile.indexOf('.'));
        String fileName;
        if (urlFile.contains("?")) fileName = urlFile.substring(urlFile.lastIndexOf("/"), urlFile.indexOf('?'));
        else fileName = urlFile.substring(urlFile.lastIndexOf("/"));
        fileName = fileName.substring(0,fileName.lastIndexOf("."));
        return new File(fileDir.getAbsoluteFile() + File.separator + fileName+Extension);
    }
    public static class RecyclerViewItemTouchListener implements RecyclerView.OnItemTouchListener {
        private final GestureDetector mGestureDetector;
        private final RecyclerViewItemTouchListener.OnItemLongClickListener mListener;
        private final RecyclerView mRecyclerView;
        private final RecyclerViewItemTouchListener.OnItemClickListener mClickListener;

        public RecyclerViewItemTouchListener(Context context, final RecyclerView recyclerView, RecyclerViewItemTouchListener.OnItemLongClickListener listener, RecyclerViewItemTouchListener.OnItemClickListener clickListener) {
            mListener = listener;
            mClickListener = clickListener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (childView != null && mClickListener != null) {
                        int[] childViewLocation = new int[2];
                        childView.getLocationOnScreen(childViewLocation);
                        int[] recyclerViewLocation = new int[2];
                        mRecyclerView.getLocationOnScreen(recyclerViewLocation);
                        int x = childViewLocation[0] - recyclerViewLocation[0] - (int) e.getX();
                        int y = childViewLocation[1] - recyclerViewLocation[1] - (int) e.getY();
                        mClickListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView), MotionEvent.obtain(e.getDownTime(), e.getEventTime(), e.getAction(), x, y, e.getMetaState()),e);
                    }
                    return super.onSingleTapConfirmed(e);
                }
                @Override
                public void onLongPress(MotionEvent e) {
                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (childView != null && mListener != null) {
                        int[] childViewLocation = new int[2];
                        childView.getLocationOnScreen(childViewLocation);
                        int[] recyclerViewLocation = new int[2];
                        mRecyclerView.getLocationOnScreen(recyclerViewLocation);
                        int x = childViewLocation[0] - recyclerViewLocation[0] - (int) e.getX();
                        int y = childViewLocation[1] - recyclerViewLocation[1] - (int) e.getY();
                        mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView), MotionEvent.obtain(e.getDownTime(), e.getEventTime(), e.getAction(), x, y, e.getMetaState()));
                    }
                }
            });
            mRecyclerView = recyclerView;
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {mGestureDetector.onTouchEvent(motionEvent);return false;}

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}

        public interface OnItemLongClickListener { void onItemLongClick(View view, int position, MotionEvent motionEvent);}

        public interface OnItemClickListener { void onItemClick(View view, int position, MotionEvent motionEvent,MotionEvent motionEvent1);}
    }
    @Override
    protected void onDestroy() {super.onDestroy();connectivityManager.unregisterNetworkCallback(networkCallback);}

}