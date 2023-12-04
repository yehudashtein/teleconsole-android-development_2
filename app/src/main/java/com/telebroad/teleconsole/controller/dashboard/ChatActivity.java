package com.telebroad.teleconsole.controller.dashboard;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.Intent.EXTRA_STREAM;
import static com.google.common.base.Strings.isNullOrEmpty;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.BlankFragment;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.FilesAdapterType;
import com.telebroad.teleconsole.chat.ImagesUriRV;
import com.telebroad.teleconsole.chat.client.DelMessage;
import com.telebroad.teleconsole.chat.client.Extra;
import com.telebroad.teleconsole.chat.client.GetMessage;
import com.telebroad.teleconsole.chat.client.LeaveMessage;
import com.telebroad.teleconsole.chat.client.PubMassage2;
import com.telebroad.teleconsole.chat.client.PubMassage3;
import com.telebroad.teleconsole.chat.client.ReplacePub;
import com.telebroad.teleconsole.chat.client.SetMessage;
import com.telebroad.teleconsole.chat.client.pubMessage;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.Attachments;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.chat.models.Channel;
import com.telebroad.teleconsole.chat.server.CtrlMessage;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.chat.viewModels.ChatMessageViewModel;
import com.telebroad.teleconsole.chat.viewModels.ChatReactionsViewModel;
import com.telebroad.teleconsole.chat.viewModels.ChatViewModel;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.ChatPagerAdapter;
import com.telebroad.teleconsole.controller.MMSImageViewActivity;
import com.telebroad.teleconsole.controller.PhotoSourceDialogList;
import com.telebroad.teleconsole.databinding.ActivityChatBinding;
import com.telebroad.teleconsole.databinding.ChatFilesLayoutBinding;
import com.telebroad.teleconsole.databinding.ItemChatBinding;
import com.telebroad.teleconsole.databinding.ItemChatImagesBinding;
import com.telebroad.teleconsole.databinding.ItemReactionBinding;
import com.telebroad.teleconsole.databinding.ReplyInitialsIconBinding;
import com.telebroad.teleconsole.databinding.VideoChatViewBinding;
import com.telebroad.teleconsole.databinding.VoicnotesChatBinding;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.UploadFile;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.notification.ChatNotifications;
import com.telebroad.teleconsole.notification.VoicemailPlayingService;
import com.telebroad.teleconsole.viewmodels.ChatMassagesViewModelFactory;
import com.telebroad.teleconsole.viewmodels.ChatViewModel2;
import com.telebroad.teleconsole.viewmodels.VoicemailViewModel;
import com.vanniktech.emoji.EmojiPopup;
import java.io.File;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class ChatActivity extends AppCompatActivity implements ChatViewModel.Callback,BlankFragment.FilesUri{
    private VoicemailViewModel voicemailViewModel;
    private static final String MMS_IMAGE_FOLDER = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Chat Files";
    private ActivityChatBinding binding;
    private final List<String> mentions = new ArrayList<>();
    private int fileSize;
    private String path;
    private int min;
    private int topicSeq;
    private int newSeq;
    private int unreadNom;
    private PubMassage3.Head head1;
    private boolean shouldGoToBottom = false;
    private final List<PubMassage3.Attachments> attachmentsList1 = new ArrayList<>();
    private int i = 0;
    private PubMassage2 pubMassage2;
    private List<ChannelDB> newList;
    private PopupMenu popupMenuChatEtText;
    private File file;
    private ChatReactionsViewModel chatReactionsViewModel;
    private URL url1;
    private ChannelDB myChannelDB;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, h:mm a", Locale.getDefault());
    private boolean isPlaying, audioHasStarted = false, audioHasFinished = false;
    private final String auth = "token";
    private final String apikey = "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo";
    private final String secret = SettingsHelper.getString(SettingsHelper.CHAT_TOKEN);
    private final String domain = "https://apiconnact.telebroad.com";
    private ArrayList<String> SpeedT;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private List<CtrlMessage> ctrls;
    private Gson gson;
    private List<String> urls;
    private String name1;
    private List<PubMassage2.Attachments> attachmentsList;
    private PubMassage2.Head head;
    private ImageView currentPlay;
    private String url = "", encodeString, id, topic;
    private Float SpeedState;
    private static Dialog dialog;
    private ChatAdapter chatAdapter;
    private SeekBar seekBar;
    private Handler seekBarHandler;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    public static final int REQUEST_AUDIO_CODE = 100;
    boolean compressed = false, isRecording, finishedRecording = false;
    private MediaBrowserCompat mediaBrowser;
    private final UploadFile uploadFile = new UploadFile();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekbar;
    public static final String CURRENT_CHAT_EXTRA = "com.telebroad.teleconsole.controller.dashboard.chatactivity.current.chat.extra";
    private final Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 200);
            long longProgress = MediaControllerCompat.getMediaController(ChatActivity.this).getPlaybackState().getPosition();
            ///Log.v("longProgress", String.valueOf(longProgress));
            Bundle bundle = new Bundle();
            bundle.putLong("SeekBarProgress", longProgress);
            Intent intent = new Intent();
            intent.setAction("SeekBarUpdate");
            intent.putExtras(bundle);
            sendBroadcast(intent);
            seekBar.setProgress((int) longProgress);
            //textView.setText((Utils.formatLongMilliSeconds(longProgress)));
        }
    };
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (currentPlay != null) {
                if (state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
                } else if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    handler.post(updateProgress);
                    currentPlay.setImageResource(R.drawable.ic_baseline_pause_24);
                } else if (state.getState() != PlaybackStateCompat.STATE_PAUSED) {
                    handler.removeCallbacks(updateProgress);
                    currentPlay.setImageResource(R.drawable.ic_outline_play_arrow_24);
                    changePlayback(false);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {super.onMetadataChanged(metadata);handleMetaData(metadata);}
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pubMassage2 = new PubMassage2();
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent, null));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        // ChatViewModel.getInstance().observe(this)
        ChatNotifications.getChatNotificationsArrayList().clear();
        popupMenuChatEtText = new PopupMenu(ChatActivity.this, binding.chatEtText);
        chatReactionsViewModel = new ViewModelProvider.AndroidViewModelFactory(AppController.getInstance()).create(ChatReactionsViewModel.class);
        supportPostponeEnterTransition();
        ChatWebSocket.getInstance().getCtrlMessageLiveData().observe(this, integer -> {
            if (integer!=null) {
                topicSeq = integer;
            }
        });
        ChatWebSocket.getInstance().getNewMessageLiveData().observe(this, integer -> {
            if (integer!=null) {
                newSeq = integer;
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LeaveMessage leaveMessage1 = new LeaveMessage();
                leaveMessage1.setId("falseLeave");
                leaveMessage1.setTopic(topic);
                leaveMessage1.setUnsub(false);
                ChatWebSocket.getInstance().sendObject("leave", leaveMessage1);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.relRoot)).setOnEmojiClickListener((emoji, imageView) -> {}).build(binding.chatEtText);
        binding.emoji.setOnClickListener(v -> popup.toggle());
        topic = getIntent().getStringExtra(ChatActivity.CURRENT_CHAT_EXTRA);
        Uri imageUri = getIntent().getParcelableExtra(EXTRA_STREAM);
        List<Uri>imageUriList = new ArrayList<>();
        imageUriList.add(imageUri);
        if (imageUri != null)handleImageURI(imageUriList);
        ChatDatabase.getInstance().channelDao().getUnreadNumberByTopic(topic).observe(this, integer -> {if (integer != null) unreadNom=integer;});
        //ChatWebSocket.getInstance().setGetUnreadSeq(this);
        if (Objects.equals(getIntent().getAction(), "SubscribeToTopic")) ChatWebSocket.getInstance().subscribe(topic);
        ChatViewModel chatViewModel = new ChatViewModel(this);
        chatViewModel.getLiveMentionChannelsSearch();
//        chatViewModel.observe(this);
        SpeedT = new ArrayList<>();
        ctrls = new ArrayList<>();
        attachmentsList = new ArrayList<>();
        urls = new ArrayList<>();
        SpeedT.add("1");SpeedT.add("1.5");SpeedT.add("2");
            ChatDatabase.getInstance().channelDao().getFndUsersByTopic(topic).observe(this, channelDB -> {
                if (channelDB != null) {
                    myChannelDB = channelDB;
                    binding.chatEtText.setHint("Message " + channelDB.getName().replaceAll("\\n", "...").replaceAll("\\r", "..."));
                    binding.txtChatName.setText(channelDB.getName());
                    if (channelDB.isGroup() != null && channelDB.isGroup() && channelDB.getAcsMode().toLowerCase().contains("j")) binding.imgChatAvator.setImageResource(R.drawable.ic_outline_group_24);
                    else if (channelDB.isGroup() != null && channelDB.isGroup() && !channelDB.getAcsMode().toLowerCase().contains("j")) binding.imgChatAvator.setImageResource(R.drawable.ic_outline_lock_24);
                    else if (channelDB.isGroup() != null && !channelDB.isGroup() && channelDB.getImageUrl() == null) binding.imgChatAvator.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(channelDB.getName()));
                    else Glide.with(ChatActivity.this).load(channelDB.getImageUrl()).circleCrop().into(binding.imgChatAvator);
                }else {
                    ChatDatabase.getInstance().channelDao().getMeUsersByTopic(topic).observe(this, channelDB1 -> {
                        myChannelDB = channelDB1;
                        if (channelDB1 != null &&  channelDB1.getName() != null){
                            name1 = channelDB1.getName();
                            binding.chatEtText.setHint("Message " + channelDB1.getName().replaceAll("\\n", "...").replaceAll("\\r", "..."));
                            binding.txtChatName.setText(channelDB1.getName());
                            if (!myChannelDB.isGroup() && myChannelDB.getImageUrl() != null) Glide.with(ChatActivity.this).load(myChannelDB.getImageUrl()).circleCrop().into(binding.imgChatAvator);
                            else if (!myChannelDB.isGroup() && myChannelDB.getImageUrl() == null)binding.imgChatAvator.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(channelDB1.getName()));
                        }
                    });
                }
            });
        ChatDatabase.getInstance().channelDao().getStatus(topic).observe(this, integer -> {
            if (integer!= null) if (integer.equals(1)){
                binding.txtLastSeen.setVisibility(View.VISIBLE);
                binding.txtLastSeen.setText("Online now");
            } else ChatDatabase.getInstance().channelDao().getFndUsersByTopic(topic).observe(this, channelDB -> {
                if (channelDB != null&& channelDB.getWhen() != null && !channelDB.getWhen().equals("") && integer.equals(0)) {
                    ChatDatabase.getInstance().channelDao().getStatus(topic).observe(this, integer1 -> {
                        if (integer1 != null && integer1.equals(0)) {
                            binding.txtLastSeen.setVisibility(View.VISIBLE);
                            runOnUiThread(() -> {
                                Date date = SettingsHelper.getDate(channelDB.getWhen());
                            String t = sdf.format(date);
                            long timeAgo = date.getTime();
                            String time = SettingsHelper.getTimeAgo(timeAgo, t, "");
                            binding.txtLastSeen.setText("Last online: " + time);
                            });
                        }
                    });
                }
            });
        });
        binding.fragmentChat.setVisibility(View.GONE);
        gson = new GsonBuilder().create();
        voicemailViewModel = new VoicemailViewModel();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(false);
        chatAdapter = new ChatAdapter(new ChatDiff(), this);
        binding.chatRecycler.setLayoutManager(linearLayoutManager);
        binding.chatRecycler.setAdapter(chatAdapter);
        binding.chatRecycler.addOnItemTouchListener(new RecyclerViewItemTouchListener(this, binding.chatRecycler, (view, position, e) -> {
            binding.confirmEdit.setOnClickListener(v -> {
                String attachments = gson.toJson(chatAdapter.getCurrentList().get(position).getAttachments());
                Type collectionType = new TypeToken<List<pubMessage.Attachments>>() {}.getType();
                List<ReplacePub.Attachments> enums = gson.fromJson(attachments, collectionType);
                ReplacePub.Head head = new ReplacePub.Head("text/*", chatAdapter.getCurrentList().get(position).getSeq(), enums);
                ReplacePub pubMessage = new ReplacePub("replace", topic, SettingsHelper.getString(SettingsHelper.MY_TOPIC), true, binding.editText.getText().toString(), head);
                ChatWebSocket.getInstance().sendObject("pub", pubMessage);
                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().UpdateContent(binding.editText.getText().toString(), chatAdapter.getCurrentList().get(position).getSeq(), chatAdapter.getCurrentList().get(position).getTopic()));
                binding.relTxtLayout.setVisibility(View.VISIBLE);
                binding.relEtLayout.setVisibility(View.GONE);
            });
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
//                View child = binding.chatRecycler.findChildViewUnder(e.getX(), e.getY());
//                if (child != null && child.findViewById(R.id.playChat1) != null) {
//                    Rect rect = new Rect();
//                    child.findViewById(R.id.playChat1).getGlobalVisibleRect(rect);
//                    if (rect.contains((int) e.getRawX(), (int) e.getRawY())) return;
//                }
                ChannelDB channelDB1 = ChatViewModel.getInstance().getChannelsByName().get(chatAdapter.getCurrentList().get(position).getSenderName());
                String newTopic = channelDB1.getTopic();
                if (newTopic.equals(SettingsHelper.getString(SettingsHelper.MY_TOPIC))) {
                    View popupView = View.inflate(ChatActivity.this, R.layout.chat_popup_me, null);
                    PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
                    popupWindow.getContentView().setVisibility(View.GONE);
                    popupWindow.showAsDropDown(view, (int) e.getX() + 500, (int) e.getY() - 160);
                    popupWindow.getContentView().setVisibility(View.VISIBLE);
                    if (popupWindow.getContentView().getVisibility() == View.VISIBLE) {
                        ImageButton imageButtonReply = popupView.findViewById(R.id.imageButtonReply);
                        imageButtonReply.setOnClickListener(view12 -> {
                            Intent i = new Intent(ChatActivity.this, ChatReplyActivity.class);
                            i.putExtra("originalMassage", chatAdapter.getCurrentList().get(position));
                            popupWindow.dismiss();
                            startActivity(i);
                        });
                        ImageButton imageButtonDelete = popupView.findViewById(R.id.imageButtonDelete);
                        imageButtonDelete.setOnClickListener(view15 -> {
                            List<DelMessage.Delseq> delseqList = new ArrayList<>();
                            DelMessage.Delseq delseq = new DelMessage.Delseq(chatAdapter.getCurrentList().get(position).getSeq());
                            delseqList.add(delseq);
                            DelMessage delMessage = new DelMessage("delete", chatAdapter.getCurrentList().get(position).getTopic(), "msg", true, delseqList);
                            ChatWebSocket.getInstance().sendObject("del", delMessage);
                            for (DelMessage.Delseq d : delseqList) {
                                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteMassage(topic, d.getLow()));
                            }
                        });
                        ImageButton imageButtonForward = popupView.findViewById(R.id.imageButtonForward);
                        imageButtonForward.setOnClickListener(view13 -> {
                            Intent intent = new Intent(ChatActivity.this, ChatForwardActivity.class);
                            intent.putExtra("topic", topic);
                            intent.putExtra("messageObject", chatAdapter.getCurrentList().get(position));
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
                            String edits = gson.toJson(chatAdapter.getCurrentList().get(position).getEdits());
                            if (!edits.equals("[]")) {
                                Type editsType = new TypeToken<List<DataMessage.Edits>>() {}.getType();
                                List<DataMessage.Edits> editsList = gson.fromJson(edits, editsType);
                                if (editsList != null) {
                                    LinkedList<DataMessage.Edits> editLinkedList = new LinkedList<>(editsList);
                                    if (!editLinkedList.getLast().getContent().isEmpty()) binding.editText.setText(SettingsHelper.reformatHTML(editLinkedList.getLast().getContent()));
                                }
                            } else if (chatAdapter.getCurrentList().get(position).getContent() != null) binding.editText.setText(SettingsHelper.reformatHTML(chatAdapter.getCurrentList().get(position).getContent().toString()));
                        });
                    }
                } else {
                    View popupView1 = View.inflate(ChatActivity.this, R.layout.chat_popup, null);
                    PopupWindow popupWindow1 = new PopupWindow(popupView1, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
                    popupWindow1.getContentView().setVisibility(View.GONE);
                    popupWindow1.showAsDropDown(view, (int) e.getX(), (int) e.getY() - 160);
                    popupWindow1.getContentView().setVisibility(View.VISIBLE);
                    if (popupWindow1.getContentView().getVisibility() == View.VISIBLE) {
                        ImageButton imageButtonReply = popupView1.findViewById(R.id.imageButtonReply);
                        imageButtonReply.setOnClickListener(view12 -> {
                            Intent i = new Intent(ChatActivity.this, ChatReplyActivity.class);
                            i.putExtra("originalMassage", chatAdapter.getCurrentList().get(position));
                            startActivity(i);
                            popupWindow1.dismiss();
                            popupWindow1.getContentView().setVisibility(View.GONE);
                        });
                        ImageButton imageButtonDelete = popupView1.findViewById(R.id.imageButtonDelete);
                        imageButtonDelete.setOnClickListener(view15 -> {
                            List<DelMessage.Delseq> delseqList = new ArrayList<>();
                            DelMessage.Delseq delseq = new DelMessage.Delseq(chatAdapter.getCurrentList().get(position).getSeq());
                            delseqList.add(delseq);
                            DelMessage delMessage = new DelMessage("delete", chatAdapter.getCurrentList().get(position).getTopic(), "msg", true, delseqList);
                            ChatWebSocket.getInstance().sendObject("del", delMessage);
                            for (DelMessage.Delseq d : delseqList) {
                                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteMassage(topic, d.getLow()));
                            }
                            popupWindow1.dismiss();
                        });
                        ImageButton imageButtonForward = popupView1.findViewById(R.id.imageButtonForward);
                        imageButtonForward.setOnClickListener(view13 -> {
                            Intent intent = new Intent(ChatActivity.this, ChatForwardActivity.class);
                            intent.putExtra("messageObject", chatAdapter.getCurrentList().get(position));
                            startActivity(intent);
                            popupWindow1.dismiss();
                            finish();
                            popupWindow1.getContentView().setVisibility(View.GONE);
                        });
                    }
                }
            }
        }, (view, position, motionEvent, e) -> {
//            Intent i = new Intent(ChatActivity.this, ChatReplyActivity.class);
//            i.putExtra("originalMassage", chatAdapter.getCurrentList().get(position));
//            startActivity(i);
        }));binding.chatRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(-1)) {
                    ListenableFuture<Integer> future = ChatDatabase.getInstance().chatMessageDao().livedataMinSeqForTopic(topic);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Futures.addCallback(future,
                                new FutureCallback<Integer>() {
                                    public void onSuccess(Integer result) {
                                            if (result != null) ChatWebSocket.getInstance().sendObject("get", GetMessage.getLoads(topic, result));
                                    }

                                    public void onFailure(@NonNull Throwable thrown) {}
                                }, ChatActivity.this.getMainExecutor()
                        );
                    }
                }
            }
        });
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        connectivityManager = (ConnectivityManager) ChatActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (!ChatWebSocket.isConnected) {
                   ChatWebSocket.getInstance().connect();
                    new CountDownTimer(500, 400) {
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {
                            if (ChatWebSocket.isConnected) ChatWebSocket.getInstance().subscribe(topic);
                        }
                    }.start();
                }
            }
        };
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
        binding.cancelEdit.setOnClickListener(v -> {
            binding.editText.setText("");
            binding.relTxtLayout.setVisibility(View.VISIBLE);
            binding.relEtLayout.setVisibility(View.GONE);
        });
        new ViewModelProvider(this, new ChatMassagesViewModelFactory(topic)).get(ChatViewModel2.class).getTopicChatLiveData().observe(this, chatMessageDBS ->
                chatAdapter.submitList(chatMessageDBS.stream().distinct().map(ChatMessageViewModel::createInstance).collect(Collectors.toList()), () -> {
                    if (!shouldGoToBottom) {
                        binding.chatRecycler.smoothScrollToPosition(chatMessageDBS.size());
                        shouldGoToBottom = true;
                    }
                }));
        binding.floatingActionButton.setOnClickListener(v -> {
            binding.floatingActionButton.setSelected(!binding.floatingActionButton.isSelected());
            if (binding.floatingActionButton.isSelected()) {
                openKeyBoard();
                binding.fragmentChat.setVisibility(View.GONE);
                Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quater_rotate_1);
                rotate.setRepeatCount(Animation.ABSOLUTE);
                binding.floatingActionButton.startAnimation(rotate);
            } else if (!binding.floatingActionButton.isSelected()) {
                closeKeyBoard();
                binding.fragmentChat.setVisibility(View.VISIBLE);
                Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quater_rotate);
                rotate.setRepeatCount(Animation.ABSOLUTE);
                binding.floatingActionButton.startAnimation(rotate);
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            BlankFragment blankFragment = new BlankFragment();
            blankFragment.setFilesUri(this);
            fragmentTransaction.replace(R.id.fragmentChat, blankFragment);
            fragmentTransaction.commit();
        });
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
                        } else if (s.toString().length() > 3 && '@' == s.toString().charAt(s.toString().length() - 4)) {
                            popupMenuChatEtText.getMenu().clear();
                            CharSequence lastTwoCharacters = binding.chatEtText.getText().subSequence(binding.chatEtText.getText().length() - 3, binding.chatEtText.getText().length());
                            String lastTwoCharactersString = lastTwoCharacters.toString();
                            for (ChannelDB item : newList) {
                                if (item.getName().toLowerCase().contains(lastTwoCharactersString.toLowerCase())) popupMenuChatEtText.getMenu().add(item.getName());
                            }
                        } else if (s.toString().length() > 4 && '@' == s.toString().charAt(s.toString().length() - 5)) {
                            popupMenuChatEtText.getMenu().clear();
                            CharSequence lastTwoCharacters = binding.chatEtText.getText().subSequence(binding.chatEtText.getText().length() - 4, binding.chatEtText.getText().length());
                            String lastTwoCharactersString = lastTwoCharacters.toString();
                            for (ChannelDB item : newList) {
                                if (item.getName().toLowerCase().contains(lastTwoCharactersString.toLowerCase())) popupMenuChatEtText.getMenu().add(item.getName());
                            }
                        } else popupMenuChatEtText.getMenu().clear();
                        popupMenuChatEtText.setOnMenuItemClickListener(item -> {
                            if (binding.chatEtText.length() >= 2) {
                                String newText = binding.chatEtText.getText().toString().substring(0, binding.chatEtText.length() - 2);
                                ChannelDB channelDB5 = ChatViewModel.getInstance().getChannelsByName().get(item.getTitle());
                                mentions.add(channelDB5.getTopic());
                                binding.chatEtText.setText(newText + " @" + item.getTitle());
                                binding.chatEtText.setSelection(binding.chatEtText.length());
                                popupMenuChatEtText.getMenu().clear();
                            } else if (binding.chatEtText.length() == 1) {
                                ChannelDB channelDB5 = ChatViewModel.getInstance().getChannelsByName().get(item.getTitle());
                                mentions.add(channelDB5.getTopic());
                                String newText = binding.chatEtText.getText().toString().substring(0, binding.chatEtText.length() - 1);
                                binding.chatEtText.setText(newText + " @" + item.getTitle());
                                binding.chatEtText.setSelection(binding.chatEtText.length());
                                popupMenuChatEtText.getMenu().clear();
                            } else {
                                ChannelDB channelDB5 = ChatViewModel.getInstance().getChannelsByName().get(item.getTitle());
                                mentions.add(channelDB5.getTopic());
                                binding.chatEtText.setText(binding.chatEtText.getText().toString() + " @" + item.getTitle());
                                binding.chatEtText.setSelection(binding.chatEtText.length());
                            }
                            return true;
                        });
                        popupMenuChatEtText.show();
                        if (!binding.chatEtText.getText().toString().isEmpty()) {
                            binding.chatEtText.setMaxLines(50);
                            DisplayTheMargins();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (binding.chatEtText.getText().toString().isEmpty()) {
                            popupMenuChatEtText.getMenu().clear();
                            binding.chatEtText.setMaxLines(1);
                            ClearViews();
                        }
                        binding.imageView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        binding.pdfLayout.imgCancel.setOnClickListener(v -> {ClearViews();});
        binding.mic.setOnClickListener(v -> {
            pubMassage2 =new PubMassage2();
            binding.chatRecycler.smoothScrollToPosition(chatAdapter.getCurrentList().size());
            if (!binding.chatEtText.getText().toString().isEmpty() && binding.pdfLayout.getRoot().getVisibility() == View.GONE && binding.fileLayout.getRoot().getVisibility() == View.GONE && binding.imgToSend.getVisibility() == View.GONE) {
                PubMassage2 pubMassage = new PubMassage2("pubMessage",topic,SettingsHelper.getString(SettingsHelper.MY_TOPIC),false,binding.chatEtText.getText().toString(),pubMassage2.getHead1("text/*",mentions));
                ChatWebSocket.getInstance().sendObject("pub", pubMassage);ClearViews();
                binding.chatEtText.setText("");
            } else if (binding.imgToSend.getVisibility() == View.VISIBLE && binding.pdfLayout.getRoot().getVisibility() == View.GONE && binding.fileLayout.getRoot().getVisibility() == View.GONE) {
                if (ctrls.size() > 0) {
                    Extra extra = new Extra();
                    List<String> attachments5 = new ArrayList<>();
                    for (CtrlMessage ctr : ctrls) {
                        id = ctr.getId();
                        String url = ctr.getParams().getUrl();
                        attachments5.add(url);//.replace(".bin","");
                        extra.setAttachments(attachments5);
                        String urlEnd = url.substring(url.lastIndexOf(".") + 1);
                        if ("121164".equals(id) && ((urlEnd.equals("jfif") || urlEnd.equals("png") || urlEnd.equals("jpg") ||
                                urlEnd.equals("jpeg") || urlEnd.equals("gif") || urlEnd.equals("webp") ||
                                urlEnd.equals("tiff") || urlEnd.equals("raw") || urlEnd.equals("bmp") ||
                                urlEnd.equals("heif") || urlEnd.equals("jpeg2000")))) {
                            PubMassage3.Attachments attachments = new PubMassage3.Attachments("img_" + SettingsHelper.getDateString() + "." + urlEnd, url, "image/" + urlEnd, ctr.getParams().getExpires(), false);
                            attachmentsList1.add(attachments);
                            urls.add(ctr.getParams().getUrl());
                            head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                        } else if (urlEnd.equals("mp4") || urlEnd.equals("m4v")) {
                            PubMassage3.Attachments attachments = new PubMassage3.Attachments("video_" + SettingsHelper.getDateString() + "." + urlEnd, url, "video/webm;codecs=vp8", ctr.getParams().getExpires(), false);
                            attachmentsList1.add(attachments);
                            urls.add(ctr.getParams().getUrl());
                            head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                        }
                    }
                    PubMassage3 pubMassage3 = PubMassage3.setPubForImages(head1, id, SettingsHelper.getString(SettingsHelper.MY_TOPIC, ""), myChannelDB.getTopic(), binding.chatEtText.getText().toString());
                    ChatWebSocket.getInstance().sendTwoObjects("pub", pubMassage3,"extra",extra);ClearImageVies();
                }
            } else if (binding.chatEtText.getText().toString().isEmpty() && binding.imgToSend.getVisibility() == View.GONE && !finishedRecording && binding.pdfLayout.getRoot().getVisibility() == View.GONE && binding.fileLayout.getRoot().getVisibility() == View.GONE) {
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
                        Utils.asyncTask(() -> {
                            mediaRecorder = new MediaRecorder();
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                            mediaRecorder.setOutputFile(SettingsHelper.getRecordFilePath(getApplicationContext()));
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            handler.post(updateVisualizer);
                            try {mediaRecorder.prepare();} catch (IOException e) {e.printStackTrace();}
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
                        Utils.asyncTask(() -> {
                            isRecording = false;
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            mediaRecorder = null;
                            try {
                                file = new File(SettingsHelper.getRecordFilePath(getApplicationContext()));
                                long fileSizeInBytes = file.length();
                                long fileSizeInKB = fileSizeInBytes / 1024;
                                System.out.println("FileSize" + fileSizeInKB + " KB");
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
                                            }

                                            @Override
                                            public void onError(ANError anError) {}
                                        });
                            } catch (Exception e) {e.printStackTrace();}
                        });
                    }
                } else ActivityCompat.requestPermissions(ChatActivity.this, new String[]{RECORD_AUDIO,}, REQUEST_AUDIO_CODE);
            } else if (binding.imgToSend.getVisibility() == View.GONE && binding.chatEtText.getText().toString().isEmpty() && finishedRecording && binding.pdfLayout.getRoot().getVisibility() == View.GONE && binding.fileLayout.getRoot().getVisibility() == View.GONE) {
                Extra extra = new Extra();
                List<String> attachments5 = new ArrayList<>();
                for (CtrlMessage ctr : ctrls) {
                    id = ctr.getId();
                    String url = ctr.getParams().getUrl();
                    attachments5.add(url);//.replace(".bin","");
                    extra.setAttachments(attachments5);
                    String urlEnd = url.substring(url.lastIndexOf(".") + 1);
                    if ("121165".equals(id) && urlEnd.equals("bin")) {
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("voice_note" +
                                SettingsHelper.getDateString() + ".wav", url, "audio/wav", ctr.getParams().getExpires(), true);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }
                }
                PubMassage3 pubMassage3 = PubMassage3.setPubForImages(head1, id, SettingsHelper.getString(SettingsHelper.MY_TOPIC), topic, binding.chatEtText.getText().toString());
                ChatWebSocket.getInstance().sendTwoObjects("pub", pubMassage3,"extra",extra);ClearViews();
            }else if (binding.pdfLayout.getRoot().getVisibility() == View.VISIBLE && binding.fileLayout.getRoot().getVisibility() == View.GONE){
                Extra extra = new Extra();
                List<String> attachments5 = new ArrayList<>();
                for (CtrlMessage ctr : ctrls) {
                    id = ctr.getId();
                    String url = ctr.getParams().getUrl(); //.replace(".bin","");
                    attachments5.add(url);
                    extra.setAttachments(attachments5);
                    String urlEnd = url.substring(url.lastIndexOf(".") + 1);
                    if ("pdf".equals(id) && urlEnd.equals("pdf")) {
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("pdf" +
                                SettingsHelper.getDateString() + ".pdf", url, "application/pdf", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }
                }
                PubMassage3 pubMassage3 = PubMassage3.setPubForImages(head1, id, SettingsHelper.getString(SettingsHelper.MY_TOPIC), topic, binding.chatEtText.getText().toString());
                ChatWebSocket.getInstance().sendTwoObjects("pub",pubMassage3,"extra",extra);ClearViews();
            }else if (binding.fileLayout.getRoot().getVisibility() == View.VISIBLE &&  binding.pdfLayout.getRoot().getVisibility() == View.GONE){
                Extra extra = new Extra();
                List<String> attachments5 = new ArrayList<>();
                for (CtrlMessage ctr : ctrls) {
                    id = ctr.getId();
                    String url = ctr.getParams().getUrl(); //.replace(".bin","");
                    attachments5.add(url);
                    extra.setAttachments(attachments5);
                    if ("docs".equals(id)) {
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("docx" +
                                SettingsHelper.getDateString() + ".docx", url, "application/msword", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("html".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("html" +
                                SettingsHelper.getDateString() + ".html", url, "text/html", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("txt".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("txt" +
                                SettingsHelper.getDateString() + ".txt", url, "text/plain", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    } else if ("json".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("json" + SettingsHelper.getDateString() + ".json", url, "application/json", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("js".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("js" + SettingsHelper.getDateString() + ".js", url, "application/javascript", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("css".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("css" + SettingsHelper.getDateString() + ".css", url, "text/css", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("ppt".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("ppt" + SettingsHelper.getDateString() + ".ppt", url, "application/vnd.ms-powerpoint", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("xls".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("xls" + SettingsHelper.getDateString() + ".xls", url, "application/vnd.ms-excel", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("zip".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("zip" + SettingsHelper.getDateString() + ".zip", url, "application/zip", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("rar".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("rar" + SettingsHelper.getDateString() + ".rar", url, "application/x-rar-compressed", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("rtf".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("rtf" + SettingsHelper.getDateString() + ".rtf", url, "application/rtf", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }else if ("brf".equals(id)){
                        PubMassage3.Attachments attachments = new PubMassage3.Attachments("brf" + SettingsHelper.getDateString() + ".brf", url, "application/x-brf", ctr.getParams().getExpires(), false);
                        attachmentsList1.add(attachments);
                        urls.add(ctr.getParams().getUrl());
                        head1 = PubMassage3.setHeadWithAttachments(attachmentsList1,mentions);
                    }
                }
                PubMassage3 pubMassage3 = PubMassage3.setPubForImages(head1, id, SettingsHelper.getString(SettingsHelper.MY_TOPIC), topic, binding.chatEtText.getText().toString());
                ChatWebSocket.getInstance().sendTwoObjects("pub", pubMassage3,"extra",extra);ClearViews();
            }
        });
        binding.imageView.setOnClickListener(v -> new PhotoSourceDialogList(ChatActivity.this::handleImageURI).show(getSupportFragmentManager(), "chooseSource"));
        binding.imageView33.setOnClickListener(v -> {
            binding.visualizer.animate().translationY(binding.voiceNoteToSend.getRoot().getHeight()).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);
                    binding.visualizer.animate().translationY(0);binding.visualizer.setAlpha(1.0f);ClearViews();binding.visualizer.animate().setListener(null);
                }
            });
            binding.recordTimer.animate().translationY(binding.recordTimer.getHeight()).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);
                    binding.recordTimer.setAlpha(1.0f);binding.recordTimer.animate().translationY(0);binding.recordTimer.animate().setListener(null);}});
            binding.imageView33.animate().translationY(binding.imageView33.getHeight()).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);
                    ClearViews();binding.imageView33.setAlpha(1.0f);binding.imageView33.animate().translationY(0);binding.imageView33.animate().setListener(null);
                }
            });
        });
        binding.voiceNoteToSend.cancelImg.setOnClickListener(v -> {
            binding.voiceNoteToSend.getRoot().animate().translationY(binding.voiceNoteToSend.getRoot().getHeight()).alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);
                    ClearViews();
                    binding.voiceNoteToSend.getRoot().setAlpha(1.0f);
                    binding.voiceNoteToSend.getRoot().animate().translationY(0);
                }
            });
        });
        binding.voiceNoteToSend.playChat.setOnClickListener(v -> {
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
    }
    @Override
    public void onComplete(List<ChannelDB> result) {
        Set<ChannelDB> uniqueSet = new HashSet<>(result);
        newList = new ArrayList<>(uniqueSet);
    }


    public void UpdateContent(String topic, int seq) {
        //binding.editText.setText("");
    }

    public ActivityChatBinding getBinding() {return binding;}

    @Override
    protected void onStart() {
        MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                MediaControllerCompat mediaController = new MediaControllerCompat(ChatActivity.this, token);
                MediaControllerCompat.setMediaController(ChatActivity.this, mediaController);
                MediaMetadataCompat metadata = mediaController.getMetadata();
                PlaybackStateCompat pbState = mediaController.getPlaybackState();
                mediaController.registerCallback(controllerCallback);

            }
        };
        if (mediaBrowser == null) {
            mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, VoicemailPlayingService.class), connectionCallback, null);
            mediaBrowser.connect();
        }
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String myTopic = SettingsHelper.getString(SettingsHelper.MY_TOPIC);
         if (myChannelDB != null && !myChannelDB.isGroup()) getMenuInflater().inflate(R.menu.chat_topic_menu, menu);
         else {
             LiveData<List<ChannelDB>> groupMembersLiveData = ChatDatabase.getInstance().channelDao().getGroupsMembersByChannel(myTopic);
             groupMembersLiveData.observe(this, channelDBS -> {
                 boolean conditionMet = false; // Variable to track if the condition is met
                 for (ChannelDB d : channelDBS) {
                     if (d.getSubbedTo().equals(topic) && d.getAcsMode().contains("O")) {
                         conditionMet = true;
                         break;
                     }
                 }
                 menu.clear();
                 if (conditionMet) getMenuInflater().inflate(R.menu.chat_owner_group, menu);
                  else getMenuInflater().inflate(R.menu.manage_not_owned_teams, menu);

             });
         }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ChannelDB channelDB = ChatViewModel.getInstance().getChannelsByTopic().get(topic);
        switch (item.getItemId()) {
            case android.R.id.home:
                LeaveMessage leaveMessage1 = new LeaveMessage();leaveMessage1.setId("falseLeave");leaveMessage1.setTopic(topic);leaveMessage1.setUnsub(false);
                ChatWebSocket.getInstance().sendObject("leave", leaveMessage1);
                break;
            case R.id.ManageTeams:
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this, R.style.DialogStyle);
                View dialogView1 = LayoutInflater.from(ChatActivity.this).inflate(R.layout.chat_manage_team, null);
                builder.setView(dialogView1);
                TabLayout tabLayout = dialogView1.findViewById(R.id.tab_layout);
                ViewPager2 viewPager = dialogView1.findViewById(R.id.view_pager);
                ChatPagerAdapter adapter = new ChatPagerAdapter(getSupportFragmentManager(), getLifecycle(), myChannelDB);
                viewPager.setOffscreenPageLimit(1);
                viewPager.setAdapter(adapter);
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    if (position == 0) tab.setText("General");
                    else if (position == 1) tab.setText("Members");
                    else tab.setText("ADD MEMBERS");}).attach();
                dialog = builder.create();dialog.show();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                break;
            case R.id.leave:
                LeaveMessage leaveMessage = new LeaveMessage();leaveMessage.setId("leaveGroup");leaveMessage.setTopic(topic);leaveMessage.setUnsub(true);
                ChatWebSocket.getInstance().sendObject("leave", leaveMessage);
                finish();
                break;
            case R.id.teamDetails:
                String channelDBString = gson.toJson(channelDB);
                Intent intent = new Intent(this, ChatNewTeamActivity.class);
                intent.putExtra("shouldDisable", true);
                intent.putExtra("channelDB", channelDBString);
                startActivity(intent);
                break;
            case R.id.deleteGroup:
                View dialogView = getLayoutInflater().inflate(R.layout.alert_textview, null);
                TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
                String message = "Are you sure you want to delete the team?<br/><b>All data will be lost</b>";
                dialogMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
                new MaterialAlertDialogBuilder(ChatActivity.this)
                        .setTitle("Delete team").setView(dialogView).setPositiveButton("DELETE", (dialog, which) -> {
                            DelMessage delMessage = new DelMessage();
                            delMessage.setId("ownerDel");
                            delMessage.setTopic(topic);
                            delMessage.setHard(true);
                            delMessage.setWhat("topic");
                            ChatWebSocket.getInstance().sendObject("del", delMessage);
                            dialog.dismiss();
                            finish();
                        }).setNegativeButton("CANCEL", (dialog, which) -> {dialog.dismiss();
                        }).create().show();
                break;
            case R.id.leaveChat:
                LeaveMessage leaveMessage2 = new LeaveMessage();leaveMessage2.setId("leaveTopic");leaveMessage2.setTopic(topic);leaveMessage2.setUnsub(true);
                ChatWebSocket.getInstance().sendObject("leave", leaveMessage2);
                finish();
                break;
            case R.id.mute:
                Map<String,String> descMap = new HashMap<>();
                descMap.put("notifications","none");
                SetMessage.Desc desc = new SetMessage.Desc();
                desc.setPrivateInfo(descMap);
                SetMessage setMessage = new SetMessage();
                setMessage.setId("mute");
                setMessage.setTopic(topic);
                setMessage.setDesc(desc);
                ChatWebSocket.getInstance().sendObject("set",setMessage);
                Toast.makeText(this, myChannelDB.getName()+ " has muted", Toast.LENGTH_SHORT).show();
                break;
            case R.id.Unmute:
                Map<String,String> descMap1 = new HashMap<>();
                descMap1.put("notifications","message");
                SetMessage.Desc desc1 = new SetMessage.Desc();
                desc1.setPrivateInfo(descMap1);
                SetMessage setMessage1 = new SetMessage();
                setMessage1.setId("mute");
                setMessage1.setTopic(topic);
                setMessage1.setDesc(desc1);
                ChatWebSocket.getInstance().sendObject("set",setMessage1);
                Toast.makeText(this, myChannelDB.getName()+ " has UnMuted", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mentionUnMute:
                Map<String,String> descMap2 = new HashMap<>();
                descMap2.put("notifications","direct");
                SetMessage.Desc desc2 = new SetMessage.Desc();
                desc2.setPrivateInfo(descMap2);
                SetMessage setMessage2 = new SetMessage();
                setMessage2.setId("mute");
                setMessage2.setTopic(topic);
                setMessage2.setDesc(desc2);
                ChatWebSocket.getInstance().sendObject("set",setMessage2);
                Toast.makeText(this, myChannelDB.getName()+ " has UnMuted for Mentions", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public List<CtrlMessage> getCtrls() {return ctrls;}

    public static Dialog getDialog() {
        return dialog;
    }

    private void handleImageURI(List<Uri> extraPhotoURI) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Utils.asyncTask(() -> {
            int counter = 0;
            for (Uri uri : extraPhotoURI) {
                counter++;
                if (uri.getPath().contains("video")) {
                    try {
                        int finalCounter = counter;
                        uploadFile.uploadFile(uri, getContentResolver(),"121165",secret, jsonObject -> {
                            runOnUiThread(() -> {
                                ctrls.add(gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class));
                                binding.imgToSend.setVisibility(View.VISIBLE);
                                if (finalCounter == extraPhotoURI.size() && ctrls.size() > 0){DisplayImages();DisplayTheMargins();}
                            });
                        });
                       // File compressedFile = File.createTempFile("compressed", ".mp4", getFilesDir());
                    } catch (Exception e) {e.printStackTrace();}
                } else {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        BitmapFactory.decodeStream(inputStream);
                        Bitmap bitmap = Utils.getBitmapFromURI1(getContentResolver(), uri);
                        File fileToSend = File.createTempFile("data", ".jpg", getFilesDir());
                        FileOutputStream stream = new FileOutputStream(fileToSend);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        stream.close();
                        inputStream.close();
                        compressed = true;
                        //BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),bitmap);
                        //binding.chatEtText.setCompoundDrawablesWithIntrinsicBounds(bitmapDrawable, null, null, null);
                        int finalCounter = counter;
                        uploadFile.uploadFile(fileToSend, secret,"121164", response -> {
                            runOnUiThread(() -> {
                                ctrls.add(gson.fromJson(response.get("ctrl"), CtrlMessage.class));
                                binding.imgToSend.setVisibility(View.VISIBLE);
                                DisplayTheMargins();
                                if (finalCounter == extraPhotoURI.size() && ctrls.size() > 0) DisplayImages();
                            });
                           // if (finalCounter == extraPhotoURI.size() && ctrls.size() > 0){DisplayImages();DisplayTheMargins();}
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
        ImagesUriRV imagesUriRV = new ImagesUriRV(new ImagesUriRV.DIFF_CALLBACK(), ChatActivity.this);
        binding.imgToSend.setLayoutManager(new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.HORIZONTAL, false));
        binding.imgToSend.setAdapter(imagesUriRV);
        imagesUriRV.submitList(ctrls);
        binding.progressBar.setVisibility(View.GONE);
    }
    public String getName1() {return name1;}
    @Override
    protected void onPause() {super.onPause();overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);}

    @Override
    public void getFilesUri(Uri uri) throws IOException {
        String type = null;
        Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            type = fileName.substring(fileName.lastIndexOf("."));
        }
        if (cursor != null) {cursor.close();}
        if ("docx".equals(type)) ctrlFiles("docx",uri);
        if (".pdf".equals(type))ctrlFiles("pdf",uri);
        if (".txt".equals(type)) ctrlFiles("txt",uri);
        if (".js".equals(type)) ctrlFiles("js",uri);
        if (".json".equals(type)) ctrlFiles("json",uri);
        if (".html".equals(type)) ctrlFiles("html",uri);
        if (".css".equals(type)) ctrlFiles("css",uri);
        if (".ppt".equals(type)||".pptx".equals(type)) ctrlFiles("ppt",uri);
        if (".xls".equals(type)||".xlsx".equals(type)) ctrlFiles("xls",uri);
        if (".zip".equals(type)) ctrlFiles("zip",uri);
        if (".rar".equals(type)) ctrlFiles("rar",uri);
        if (".rtf".equals(type)) ctrlFiles("rtf",uri);
        if (".brf".equals(type)) ctrlFiles("brf",uri);

    }
    private void DisplayTheMargins(){
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.relTxtLayout);  // clone constraints from ConstraintLayout
        constraintSet.setMargin(binding.relTxtLayout.getId(), ConstraintSet.TOP, 0);
        constraintSet.setMargin(binding.relTxtLayout.getId(), ConstraintSet.START, 0);
        constraintSet.setMargin(binding.relTxtLayout.getId(), ConstraintSet.END, 10);
        constraintSet.applyTo(binding.relTxtLayout); // apply the changes to the ConstraintLayout
        binding.mic.setImageResource(R.drawable.ic_baseline_send_24);
    }

    public static class ChatDiff extends DiffUtil.ItemCallback<ChatMessageViewModel> {
        @Override
        public boolean areItemsTheSame(@NonNull ChatMessageViewModel oldItem, @NonNull ChatMessageViewModel newItem) {
            return String.valueOf(oldItem.getSeq()).equals(String.valueOf(newItem.getSeq()));
        }

        @Override
        @SuppressLint("DiffUtilEquals")
        public boolean areContentsTheSame(@NonNull ChatMessageViewModel oldItem, @NonNull ChatMessageViewModel newItem) {
            if (!oldItem.getContent().equals(newItem.getContent())) return false;
            if (oldItem.getDate().toInstant().getEpochSecond() != newItem.getDate().toInstant().getEpochSecond()) return false;
            if (!oldItem.getReplies().equals(newItem.getReplies()))return false;
            if (oldItem.getImageURL() != null && newItem.getImageURL() != null &&!oldItem.getImageURL().equals(newItem.getImageURL()))return false;
            if (!oldItem.getEdits().equals(newItem.getEdits()))return false;
            return oldItem.getTopic().equals(newItem.getTopic());
        }
    }

    public class ChatAdapter extends ListAdapter<ChatMessageViewModel, ChatAdapter.RootViewHolder> {
        List<ChatMessageViewModel> tempList;
        public ChatAdapter(@NonNull DiffUtil.ItemCallback<ChatMessageViewModel> diffCallback, Context context) {super(diffCallback);}
        @Override
        public void submitList(@Nullable List<ChatMessageViewModel> list,Runnable runnable) {
            tempList = list == null ? new ArrayList<>() : new ArrayList<>(list);
            min = newSeq-topicSeq;
            notifyItemChanged(tempList.size()-1);
            super.submitList(tempList,runnable);
        }

        private class seekBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                long seekProgress = intent.getLongExtra("SeekBarProgress", 0);
                //textView.setText((Utils.formatLongMilliSeconds(seekProgress)));
                //seekBar.setProgress((int) seekProgress,true);
            }
        }

        private void initReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("SeekBarUpdate");
            ChatAdapter.seekBroadcastReceiver seekBroadcastReceiver = new seekBroadcastReceiver();
            registerReceiver(seekBroadcastReceiver, intentFilter);
        }
        @Override
        public int getItemViewType(int position) {super.getItemViewType(position);
            ChatMessageViewModel chat = getCurrentList().get(position);
            String attachments = gson.toJson(chat.getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
            for (Attachments a : enums) {
                String type = a.getType();
                if (type != null && type.equals("audio/wav")) return 1;
                else if (type != null && type.equals("video/webm;codecs=vp8") || Objects.equals(type, "image/m4v")) return 3;
                else if (type == null || type.equals("text/plain") || type.equals("application/pdf") || type.equals("application/zip")
                        || type.equals("text/html") || type.equals("text/javascript") || type.equals("application/msword") || type.equals("application/vnd.ms-powerpoint")
                        || type.equals("application/vnd.ms-excel") || type.equals("application/x-rar-compressed") || type.equals("application/rtf") || type.equals("application/json")
                        || type.equals("application/javascript") || type.equals("text/css")||type.equals("application/vnd.android.package-archive") )
                    return 4;
            }
            return super.getItemViewType(position);
        }

        @NonNull
        @Override
        public RootViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1) {return new VoiceNoteViewHolder(VoicnotesChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else if (viewType == 3) {return new VideoViewHolder(VideoChatViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else if (viewType == 4) {return new FileHolder(ChatFilesLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else {return new ViewHolder(ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));}
        }

        @Override
        public void onBindViewHolder(@NonNull RootViewHolder root, int position) {
            if (root instanceof ViewHolder) onBindViewHolder((ViewHolder) root, position);
            else if (root instanceof VoiceNoteViewHolder) onBindVoicHolder((VoiceNoteViewHolder) root, position);
            else if (root instanceof VideoViewHolder) {try {onBindVoicHolder((VideoViewHolder) root, position);} catch (UnsupportedEncodingException e) {e.printStackTrace();}
            } else if (root instanceof FileHolder) {try {onBindFileHolder((FileHolder) root, position);} catch (UnsupportedEncodingException | MalformedURLException | URISyntaxException e) {e.printStackTrace();}}
        }

        @Override
        public int getItemCount() {
            return getCurrentList().size();
        }

        private void onBindFileHolder(@NonNull FileHolder holder, int position) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
            int newLinePosition1 = getCurrentList().size()-min;
            if (unreadNom>0 && min<=0) {
                int newLinePosition2 = getCurrentList().size() - unreadNom;
                if (position == newLinePosition2) {
                    holder.binding.vNewLine.setVisibility(View.VISIBLE);
                    holder.binding.txtNew.setVisibility(View.VISIBLE);
                }else {
                    holder.binding.vNewLine.setVisibility(View.GONE);
                    holder.binding.txtNew.setVisibility(View.GONE);
                }
            }else {
                if (min>0 && unreadNom<=0 && newLinePosition1> position) {
                    holder.binding.vNewLine.setVisibility(View.VISIBLE);
                    holder.binding.txtNew.setVisibility(View.VISIBLE);
                }else if (unreadNom<=0 && min>0){
                    holder.binding.vNewLine.setVisibility(View.GONE);
                    holder.binding.txtNew.setVisibility(View.GONE);
                }
            }
            ChatMessageViewModel chat = getCurrentList().get(position);
            if (chat.isForwarded())holder.binding.txtForward.setVisibility(View.VISIBLE);
            else holder.binding.txtForward.setVisibility(View.GONE);
            if (!isNullOrEmpty(chat.getContentForTextView().toString())){
                holder.binding.txtContent.setVisibility(View.VISIBLE);
                holder.binding.txtContent.setText(chat.getContentForTextView());
            }else holder.binding.txtContent.setVisibility(View.GONE);
            String senderName = chat.getSenderName();
            if (!isNullOrEmpty(chat.getRepliesForTxtReply().toString())){
                holder.binding.replyCard.setVisibility(View.VISIBLE);
                holder.binding.txtLastReply.setText(chat.getRepliesForTxtReply());
                holder.binding.numberReplies.setText(chat.getTxtNumberReplies());
                ReplyAdapter replyAdapter = new ReplyAdapter(chat.gerReplyLinkedList());
                holder.binding.recyclerViewReply.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.binding.replyCard.setOnClickListener(v -> {
                    Intent i = new Intent(ChatActivity.this,ChatReplyActivity.class);
                    i.putExtra("originalMassage",chat);
                    startActivity(i);
                });
                holder.binding.recyclerViewReply.setAdapter(replyAdapter);
            }else holder.binding.replyCard.setVisibility(View.GONE);
            holder.binding.txtTimeChatM.setText(chat.getTimeForTxtTimeChatM());
            holder.binding.txtClock.setText(chat.getClockForTxtChatTime());
            boolean isSenderTheSame = position != 0 && chat.getSignature().equals(getCurrentList().get(position - 1).getSignature());
            if (isSenderTheSame || senderName.equals("Meetings Bot")) {
                holder.binding.txtSenderName.setVisibility(View.GONE);
                holder.binding.txtTimeChatM.setVisibility(View.GONE);
                holder.binding.imageView32.setVisibility(View.GONE);
                holder.binding.txtClock.setVisibility(View.VISIBLE);
                holder.binding.getRoot().setOnClickListener(v -> {
                    if (holder.binding.txtClock.getVisibility()==View.VISIBLE)holder.binding.txtClock.setVisibility(View.GONE);
                    else holder.binding.txtClock.setVisibility(View.VISIBLE);
                });
            } else {
                holder.binding.txtClock.setVisibility(View.GONE);
                holder.binding.txtSenderName.setVisibility(View.VISIBLE);
                holder.binding.txtTimeChatM.setVisibility(View.VISIBLE);
                holder.binding.imageView32.setVisibility(View.VISIBLE);
                holder.binding.txtSenderName.setText(senderName);
                String imageUrl = chat.getImageURL();
                if (imageUrl == null) holder.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                else Glide.with(ChatActivity.this).load(imageUrl).circleCrop().into(holder.binding.imageView32);
                if (senderName.equals("Unknown") && imageUrl == null) {
                    holder.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                    holder.binding.txtSenderName.setText(senderName);
                }
            }
            String attachments = gson.toJson(chat.getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
            if (enums.size() == 1) {
                holder.binding.fileCardView.setVisibility(View.VISIBLE);
                holder.binding.imgRV.setVisibility(View.GONE);
                for (Attachments a : enums) {
                    String path = a.getPath();
                    String type = path.substring(path.lastIndexOf("/")+1);
                    String ext = path.substring(path.lastIndexOf("."));
                    holder.binding.btnFile.setText("OPEN " + type + " FILE");
                    String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                    holder.binding.dropDown.setOnClickListener(v -> {
                        PopupMenu popupMenu = new PopupMenu(ChatActivity.this, v);
                        popupMenu.inflate(R.menu.chat_file_menu);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            switch (item.getItemId()) {
                                case R.id.download:
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(newURL));
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setTitle("Downloading File"); // Set the title of the download notification
                                    request.setDescription("Downloading"); // Set the description of the download notification
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "filename"+ext); // Set the destination path and file name
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
                                        Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteMassage(topic, d.getLow()));
                                    }return true;
                                default: return false;
                            }
                        });
                        popupMenu.show();
                    });
                    holder.binding.btnFile.setOnClickListener(v -> {
                        try {
                            url1 = new URL(newURL);
                            openFile(a.getType());
                        } catch (URISyntaxException | MalformedURLException e) {e.printStackTrace();}
                    });
                }
            } else if(enums.size() >1){
                holder.binding.fileCardView.setVisibility(View.GONE);
                holder.binding.imgRV.setVisibility(View.VISIBLE);
                ArrayList<FilesAdapterType> strings = enums.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
                PhotoAdapter photoAdapter = new PhotoAdapter(strings,chat);
                photoAdapter.notifyDataSetChanged();
                holder.binding.imgRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.binding.imgRV.setAdapter(photoAdapter);
            }else {
                holder.binding.fileCardView.setVisibility(View.GONE);
                holder.binding.imgRV.setVisibility(View.GONE);
            }
            if (chat.getReactions() != null) {
                chatReactionsViewModel.getReactions(chat.getSeq() + "%", topic).observe(ChatActivity.this, reactions -> {
                    if (reactions.size()>0) {
                        holder.binding.fileReactions.setVisibility(View.VISIBLE);
                        ReactionAdapter adapter = new ReactionAdapter(reactions);
                        adapter.notifyDataSetChanged();
                        holder.binding.fileReactions.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                        holder.binding.fileReactions.setAdapter(adapter);
                    }else holder.binding.fileReactions.setVisibility(View.GONE);
                });
            } else holder.binding.fileReactions.setVisibility(View.GONE);
        }

        private void onBindVoicHolder(@NonNull VideoViewHolder holder, int position) throws UnsupportedEncodingException {
//            int newLinePosition1 = tempList.size()-min;
//            if (min>0 && position== newLinePosition1){
//                holder.videoChatViewBinding.vNewLine.setVisibility(View.VISIBLE);
//                holder.videoChatViewBinding.txtNew.setVisibility(View.VISIBLE);
//            }else {
//                holder.videoChatViewBinding.vNewLine.setVisibility(View.GONE);
//                holder.videoChatViewBinding.txtNew.setVisibility(View.GONE);
//            }
            int newLinePosition1 = getCurrentList().size()-min;
            if (unreadNom>0 && min<=0) {
                int newLinePosition2 = getCurrentList().size() - unreadNom;
                if (position == newLinePosition2) {
                    holder.videoChatViewBinding.vNewLine.setVisibility(View.VISIBLE);
                    holder.videoChatViewBinding.txtNew.setVisibility(View.VISIBLE);
                }else {
                    holder.videoChatViewBinding.vNewLine.setVisibility(View.GONE);
                    holder.videoChatViewBinding.txtNew.setVisibility(View.GONE);
                }
            }else {
                if (min>0 && unreadNom<=0 && position== newLinePosition1) {
                    holder.videoChatViewBinding.vNewLine.setVisibility(View.VISIBLE);
                    holder.videoChatViewBinding.txtNew.setVisibility(View.VISIBLE);
                }else if (unreadNom<=0 && min>0){
                    holder.videoChatViewBinding.vNewLine.setVisibility(View.GONE);
                    holder.videoChatViewBinding.txtNew.setVisibility(View.GONE);
                }
            }
            ChatMessageViewModel chat = getCurrentList().get(position);
            if (chat.isForwarded())holder.videoChatViewBinding.txtForward.setVisibility(View.VISIBLE);
            else holder.videoChatViewBinding.txtForward.setVisibility(View.GONE);
            if (!isNullOrEmpty(chat.getContentForTextView().toString())){
                holder.videoChatViewBinding.txtContent.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.txtContent.setText(chat.getContentForTextView());
            }else holder.videoChatViewBinding.txtContent.setVisibility(View.GONE);
            if (!isNullOrEmpty(chat.getRepliesForTxtReply().toString())){
                holder.videoChatViewBinding.replyCard.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.txtLastReply.setText(chat.getRepliesForTxtReply());
                holder.videoChatViewBinding.numberReplies.setText(chat.getTxtNumberReplies());
                ReplyAdapter replyAdapter = new ReplyAdapter(chat.gerReplyLinkedList());
                holder.videoChatViewBinding.recyclerViewReply.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.videoChatViewBinding.replyCard.setOnClickListener(v -> {
                    Intent i = new Intent(ChatActivity.this,ChatReplyActivity.class);
                    i.putExtra("originalMassage",chat);
                    startActivity(i);
                });
                holder.videoChatViewBinding.recyclerViewReply.setAdapter(replyAdapter);
            }else holder.videoChatViewBinding.replyCard.setVisibility(View.GONE);
            String senderName = chat.getSenderName();
            holder.videoChatViewBinding.txtTimeChatM.setText(chat.getTimeForTxtTimeChatM());
            holder.videoChatViewBinding.txtClock.setText(chat.getClockForTxtChatTime());
            boolean isSenderTheSame = position != 0 && chat.getSignature().equals(getCurrentList().get(position - 1).getSignature());
            if (isSenderTheSame || senderName.equals("Meetings Bot")) {
                holder.videoChatViewBinding.txtSenderName.setVisibility(View.GONE);
                holder.videoChatViewBinding.txtTimeChatM.setVisibility(View.GONE);
                holder.videoChatViewBinding.imageView32.setVisibility(View.GONE);
                holder.videoChatViewBinding.txtClock.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.getRoot().setOnClickListener(v -> {
                    if (holder.videoChatViewBinding.txtClock.getVisibility()==View.VISIBLE)holder.videoChatViewBinding.txtClock.setVisibility(View.GONE);
                    else holder.videoChatViewBinding.txtClock.setVisibility(View.VISIBLE);
                });
            } else {
                holder.videoChatViewBinding.txtClock.setVisibility(View.GONE);
                holder.videoChatViewBinding.txtSenderName.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.txtTimeChatM.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.imageView32.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.txtSenderName.setText(senderName);
                String imageUrl = chat.getImageURL();
                if (imageUrl == null) holder.videoChatViewBinding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                else Glide.with(ChatActivity.this).load(imageUrl).circleCrop().into(holder.videoChatViewBinding.imageView32);
                if (senderName.equals("Unknown") && imageUrl == null) {
                    holder.videoChatViewBinding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                    holder.videoChatViewBinding.txtSenderName.setText(senderName);
                }
            }
            String attachments = gson.toJson(chat.getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
            if (enums.size() == 1) {
                holder.videoChatViewBinding.imgRV.setVisibility(View.GONE);
                holder.videoChatViewBinding.chatVideoThumbNail.setVisibility(View.VISIBLE);
                holder.videoChatViewBinding.PlayVideo.setVisibility(View.VISIBLE);
                for (Attachments a : enums) {
                    String path = a.getPath();
                    String type = a.getType();
                    if (type.equals("video/webm;codecs=vp8") || Objects.equals(type, "image/m4v")) {
                        String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                        holder.videoChatViewBinding.chatVideoThumbNail.setOnClickListener(v -> {
                            Intent intent = new Intent(ChatActivity.this, CheatVideoPlaying.class);
                            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, v, "lets_play_it");
                            intent.putExtra("videoURL", newURL);
                            intent.putExtra("ChatMessageViewModel", getCurrentList().get(position));
                            startActivity(intent, optionsCompat.toBundle());
                        });
                        Glide.with(ChatActivity.this).asBitmap().load(newURL).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.videoChatViewBinding.chatVideoThumbNail);
                    }
                }
            }else if (enums.size() >1){
                holder.videoChatViewBinding.PlayVideo.setVisibility(View.GONE);
                holder.videoChatViewBinding.chatVideoThumbNail.setVisibility(View.GONE);
                holder.videoChatViewBinding.imgRV.setVisibility(View.VISIBLE);
                ArrayList<FilesAdapterType> strings = enums.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
                PhotoAdapter photoAdapter = new PhotoAdapter(strings,chat);
                photoAdapter.notifyDataSetChanged();
                holder.videoChatViewBinding.imgRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.videoChatViewBinding.imgRV.setAdapter(photoAdapter);
            }else {
                holder.videoChatViewBinding.PlayVideo.setVisibility(View.GONE);
                holder.videoChatViewBinding.chatVideoThumbNail.setVisibility(View.GONE);
                holder.videoChatViewBinding.imgRV.setVisibility(View.GONE);
            }
            if (chat.getReactions() != null) {
                chatReactionsViewModel.getReactions(chat.getSeq() + "%", topic).observe(ChatActivity.this, reactions -> {
                    if (reactions.size()>0) {
                        holder.videoChatViewBinding.videoReaction.setVisibility(View.VISIBLE);
                        ReactionAdapter adapter = new ReactionAdapter(reactions);
                        adapter.notifyDataSetChanged();
                        holder.videoChatViewBinding.videoReaction.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                        holder.videoChatViewBinding.videoReaction.setAdapter(adapter);
                    }else holder.videoChatViewBinding.videoReaction.setVisibility(View.GONE);
                });
            } else holder.videoChatViewBinding.videoReaction.setVisibility(View.GONE);
    }
        private void onBindVoicHolder(@NonNull VoiceNoteViewHolder root, int position) {
            int newLinePosition1 = getCurrentList().size()-min;
            if (unreadNom>0 && min<=0) {
                int newLinePosition2 = getCurrentList().size() - unreadNom;
                if (position == newLinePosition2) {
                    root.speedChatBinding.vNewLine.setVisibility(View.VISIBLE);
                    root.speedChatBinding.txtNew.setVisibility(View.VISIBLE);
                }else {
                    root.speedChatBinding.vNewLine.setVisibility(View.GONE);
                    root.speedChatBinding.txtNew.setVisibility(View.GONE);
                }
            }else {
                if (min>0 && unreadNom<=0 && position== newLinePosition1) {
                    root.speedChatBinding.vNewLine.setVisibility(View.VISIBLE);
                    root.speedChatBinding.txtNew.setVisibility(View.VISIBLE);
                }else if (unreadNom<=0 && min>0){
                    root.speedChatBinding.vNewLine.setVisibility(View.GONE);
                    root.speedChatBinding.txtNew.setVisibility(View.GONE);
                }
            }
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_telebroad_logo_only);
            Drawable drawable = ContextCompat.getDrawable(ChatActivity.this, R.drawable.ic_telebroad_logo_only_small);
            Bitmap bitmap = Utils.drawableToBitmap(drawable);
            ChatMessageViewModel chat = getCurrentList().get(position);
            if (chat.isForwarded())root.speedChatBinding.txtForward.setVisibility(View.VISIBLE);
            else root.speedChatBinding.txtForward.setVisibility(View.GONE);
            if (!isNullOrEmpty(chat.getContentForTextView().toString())){
                root.speedChatBinding.txtContent.setVisibility(View.VISIBLE);
                root.speedChatBinding.txtContent.setText(chat.getContentForTextView());
            }else root.speedChatBinding.txtContent.setVisibility(View.GONE);
            if (!isNullOrEmpty(chat.getRepliesForTxtReply().toString())){
                root.speedChatBinding.replyCard.setVisibility(View.VISIBLE);
                root.speedChatBinding.txtLastReply.setText(chat.getRepliesForTxtReply());
                root.speedChatBinding.numberReplies.setText(chat.getTxtNumberReplies());
                ReplyAdapter replyAdapter = new ReplyAdapter(chat.gerReplyLinkedList());
                root.speedChatBinding.recyclerViewReply.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                root.speedChatBinding.replyCard.setOnClickListener(v -> {
                    Intent i = new Intent(ChatActivity.this,ChatReplyActivity.class);
                    i.putExtra("originalMassage",chat);
                    startActivity(i);
                });
                root.speedChatBinding.recyclerViewReply.setAdapter(replyAdapter);
            }else root.speedChatBinding.replyCard.setVisibility(View.GONE);
            String senderName = chat.getSenderName();
            root.binding.txtSenderName.setText(senderName);
            root.binding.txtTimeChatM.setText(chat.getTimeForTxtTimeChatM());
            root.binding.txtClock.setText(chat.getClockForTxtChatTime());
            boolean isSenderTheSame = position != 0 && chat.getSignature().equals(getCurrentList().get(position - 1).getSignature());
            if (isSenderTheSame || senderName.equals("Meetings Bot")) {
                root.binding.txtSenderName.setVisibility(View.GONE);
                root.binding.txtTimeChatM.setVisibility(View.GONE);
                root.binding.imageView32.setVisibility(View.GONE);
                root.binding.txtClock.setVisibility(View.VISIBLE);
                root.binding.getRoot().setOnClickListener(v -> {
                    if (root.binding.txtClock.getVisibility()==View.VISIBLE)root.binding.txtClock.setVisibility(View.GONE);
                    else root.binding.txtClock.setVisibility(View.VISIBLE);
                });
            } else {
                root.binding.txtClock.setVisibility(View.GONE);
                root.binding.txtSenderName.setVisibility(View.VISIBLE);
                root.binding.txtTimeChatM.setVisibility(View.VISIBLE);
                root.binding.imageView32.setVisibility(View.VISIBLE);
                String imageUrl = chat.getImageURL();
                if (imageUrl == null) root.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                else Glide.with(ChatActivity.this).load(imageUrl).circleCrop().into(root.binding.imageView32);
                if(senderName.equals("Unknown") && imageUrl == null){
                    root.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
                    root.binding.txtSenderName.setText(senderName);
                }
            }
            String ab = SpeedT.get(i);
            root.speedChatBinding.include.txtSpeed.setText(ab);
            root.speedChatBinding.include.speedLayout.setOnClickListener(v -> {
                if (i != 2 && i < 2) {i++;
                    String ab1 = SpeedT.get(i);
                    root.speedChatBinding.include.txtSpeed.setText(ab1);
                } else {i = 0;
                    String ab1 = SpeedT.get(i);
                    root.speedChatBinding.include.txtSpeed.setText(ab1);
                }if (root.speedChatBinding.include.txtSpeed.getText().equals("1")) {SpeedState = 1F;
                    Bundle bundle = new Bundle();
                    bundle.putFloat("SpeedProgress", SpeedState);
                    MediaControllerCompat.getMediaController(ChatActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                } else if (root.speedChatBinding.include.txtSpeed.getText().equals("1.5")) {SpeedState = 1.5F;
                    Bundle bundle = new Bundle();
                    bundle.putFloat("SpeedProgress", SpeedState);
                    MediaControllerCompat.getMediaController(ChatActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                } else if (root.speedChatBinding.include.txtSpeed.getText().equals("2")) {SpeedState = 2F;
                    Bundle bundle = new Bundle();
                    bundle.putFloat("SpeedProgress", SpeedState);
                    MediaControllerCompat.getMediaController(ChatActivity.this).sendCommand("speed", bundle, new ResultReceiver(new Handler(Looper.getMainLooper())));
                }
            });
            String attachments = gson.toJson(chat.getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
            if (enums.size() == 1){
                root.binding.imgRV.setVisibility(View.GONE);
                for (Attachments a : enums) {
                    Bitmap finalBitmap = bitmap;
                    root.binding.playChat1.setOnClickListener(v -> {
                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        boolean isMuted = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                        if (isMuted){Toast.makeText(ChatActivity.this, "Please turn the volume up", Toast.LENGTH_SHORT).show();}
                        String path = a.getPath();
                        try {
                            currentPlay = root.binding.playChat1;
                            seekBar = root.binding.sb;
                            String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                            if (!newURL.equals(url)) {
                                url = newURL;
                                Uri myUri = Uri.parse(newURL);
                                voicemailViewModel.addURItoQueueChat(MediaControllerCompat.getMediaController(ChatActivity.this), myUri, senderName, finalBitmap);
                                changePlayback(true);
                            } else changePlayback(false);
                        } catch (Exception e) {e.printStackTrace();}
                    });
                }
            }else if (enums.size() >1){
                root.binding.VoiceNoteLayout.setVisibility(View.GONE);
                root.binding.imgRV.setVisibility(View.VISIBLE);
                ArrayList<FilesAdapterType> strings = enums.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
                PhotoAdapter photoAdapter = new PhotoAdapter(strings, chat);
                photoAdapter.notifyDataSetChanged();
                root.binding.imgRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                root.binding.imgRV.setAdapter(photoAdapter);
            }else root.binding.imgRV.setVisibility(View.GONE);
            VoicemailPlayingService.getLiveState().observe(ChatActivity.this, integer -> {
                    if (integer!=null &&integer == ExoPlayer.STATE_ENDED) {
                        if (MediaControllerCompat.getMediaController(ChatActivity.this).getTransportControls() != null) root.binding.sb.setProgress(0, true);
                    }
            });
            root.binding.sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) MediaControllerCompat.getMediaController(ChatActivity.this).getTransportControls().seekTo(progress);}
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}@Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            if (chat.getReactions() != null) {
                chatReactionsViewModel.getReactions(chat.getSeq() + "%", topic).observe(ChatActivity.this, reactions -> {
                    if (reactions.size()>0) {
                        root.binding.reactionRecycler.setVisibility(View.VISIBLE);
                        ReactionAdapter adapter = new ReactionAdapter(reactions);
                        adapter.notifyDataSetChanged();
                        root.binding.reactionRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                        root.binding.reactionRecycler.setAdapter(adapter);
                    }else root.binding.reactionRecycler.setVisibility(View.GONE);
                });
            } else root.binding.reactionRecycler.setVisibility(View.GONE);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int newLinePosition1 = getCurrentList().size()-min;
            if (unreadNom>0 && min<=0) {
                int newLinePosition2 = getCurrentList().size() - unreadNom;
                if (position == newLinePosition2) {
                    holder.binding.vNewLine.setVisibility(View.VISIBLE);
                    holder.binding.txtNew.setVisibility(View.VISIBLE);
                }else {
                    holder.binding.vNewLine.setVisibility(View.GONE);
                    holder.binding.txtNew.setVisibility(View.GONE);
                }
            }else {
                if (unreadNom<=0 &&  position== newLinePosition1) {
                    holder.binding.vNewLine.setVisibility(View.VISIBLE);
                    holder.binding.txtNew.setVisibility(View.VISIBLE);
                }else if (unreadNom<=0 && min>0){
                    holder.binding.vNewLine.setVisibility(View.GONE);
                    holder.binding.txtNew.setVisibility(View.GONE);
                }
            }
            ChatMessageViewModel chat = getCurrentList().get(position);
            if (!isNullOrEmpty(chat.getContentForTextView().toString())){
                holder.binding.contentView.setVisibility(View.VISIBLE);
                holder.binding.contentView.setText(chat.getContentForTextView());
            }else holder.binding.contentView.setVisibility(View.GONE);
            EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.relRoot)).setOnEmojiClickListener((emoji, imageView) -> {String selectedEmoji = imageView.getUnicode();}).build(binding.chatEtText);
            if (chat.isForwarded())holder.binding.txtForward.setVisibility(View.VISIBLE);
            else holder.binding.txtForward.setVisibility(View.GONE);
            String senderName = chat.getSenderName();

            if (!isNullOrEmpty(chat.getRepliesForTxtReply().toString())){
                holder.binding.replyCard.setVisibility(View.VISIBLE);
                holder.binding.txtLastReply.setText(chat.getRepliesForTxtReply());
                holder.binding.numberReplies.setText(chat.getTxtNumberReplies());
                ReplyAdapter replyAdapter = new ReplyAdapter(chat.gerReplyLinkedList());
                holder.binding.recyclerViewReply.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.binding.replyCard.setOnClickListener(v -> {
                    Intent i = new Intent(ChatActivity.this,ChatReplyActivity.class);
                    i.putExtra("originalMassage",chat);
                    startActivity(i);
                });
                holder.binding.recyclerViewReply.setAdapter(replyAdapter);
            }else holder.binding.replyCard.setVisibility(View.GONE);
            String attachments = gson.toJson(chat.getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            Collection<Attachments> enums = gson.fromJson(attachments, collectionType);
            if (enums.size() == 1) {
                holder.binding.displayImage.setVisibility(View.VISIBLE);
                holder.binding.imgRV.setVisibility(View.GONE);
                //holder.binding.containerCardStack.setVisibility(View.GONE);
                  for (Attachments a : enums) {path = a.getPath();String type = a.getType();
                       try {
                          if (Utils.isImageType(type)){
                             url = domain + a.getPath() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                             holder.binding.displayImage.setClipToOutline(true);
                             Glide.with(ChatActivity.this).load(url).into(holder.binding.displayImage);
                             holder.binding.displayImage.setTransitionName(url);
                          }
                       }catch (Exception e) {}
                  }
            }else if (enums.size() >= 2) {
                holder.binding.displayImage.setVisibility(View.GONE);
                holder.binding.imgRV.setVisibility(View.VISIBLE);
                ArrayList<FilesAdapterType> strings = enums.stream().map(FilesAdapterType::createInstance).collect(Collectors.toCollection(ArrayList::new));
                PhotoAdapter photoAdapter = new PhotoAdapter(strings,chat);
                photoAdapter.notifyDataSetChanged();
                holder.binding.imgRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                holder.binding.imgRV.setAdapter(photoAdapter);
                //holder.binding.containerCardStack.setVisibility(View.VISIBLE);
//                ChatImgListAdapter chatImgListAdapter = new ChatImgListAdapter(ChatActivity.this,R.layout.item_chat_images,strings);
//                ChatBaseImgAdapter chatBaseImgAdapter = new ChatBaseImgAdapter(strings,ChatActivity.this);
//                holder.binding.containerCardStack.setAdapter(chatImgListAdapter);
                //holder.binding.containerCardStack.setVisibility(View.GONE);
            }else{
                holder.binding.displayImage.setVisibility(View.GONE);
                holder.binding.imgRV.setVisibility(View.GONE);
            }holder.binding.displayImage.setOnClickListener(v -> {
                Intent intent = new Intent(ChatActivity.this, ChatImageActivity.class);
                intent.putExtra(ChatImageActivity.MMS_IMAGE_URL, v.getTransitionName());
                intent.putExtra("originalMassage",chat);
                intent.putExtra("path",path);
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, v, "lets_do_it").toBundle());
            });
            boolean isSenderTheSame = position != 0 && chat.getSignature().equals(getCurrentList().get(position - 1).getSignature());
//            Timer timer = new Timer();
//            timer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    ChatActivity.this.runOnUiThread(() -> {
//                        String t = sdf.format(chat.getDate());
//                        String clock = sdft.format(chat.getDate());
//                        long timeAgo = chat.getDate().getTime();
//                        String time = SettingsHelper.getTimeAgo(timeAgo,t,clock);
//                        String clockTime = SettingsHelper.getTimeAgoInClockTime(timeAgo,t,clock);
//                        holder.binding.txtTimeChatM.setText(time);
//                        holder.binding.txtChatTime.setText(clockTime);
//                    });
//                }
//            }, 0, 60 * 1000);
                  // String t = sdf.format(chat.getDate());
                   holder.binding.txtTimeChatM.setText(chat.getTimeForTxtTimeChatM());
                    holder.binding.txtChatTime.setText(chat.getClockForTxtChatTime());
                    long timeAgo = chat.getDate().getTime();
                    String time = SettingsHelper.getTimeAgo(timeAgo,chat.getTimeForTxtTimeChatM(),chat.getClockForTxtChatTime());
                    String clockTime = SettingsHelper.getTimeAgoInClockTime(timeAgo,chat.getTimeForTxtTimeChatM(),chat.getClockForTxtChatTime());
                if (isSenderTheSame || senderName.equals("Meetings Bot")) {
                    holder.binding.txtSenderName.setVisibility(View.GONE);
                    holder.binding.txtTimeChatM.setVisibility(View.GONE);
                    holder.binding.imageView32.setVisibility(View.GONE);
                    holder.binding.txtChatTime.setVisibility(View.GONE);
                    holder.binding.getRoot().setOnClickListener(v -> {
                        if (holder.binding.txtChatTime.getVisibility()==View.VISIBLE)holder.binding.txtChatTime.setVisibility(View.GONE);
                        else holder.binding.txtChatTime.setVisibility(View.VISIBLE);
                    });
                } else {
                    holder.binding.txtChatTime.setVisibility(View.GONE);
                    holder.binding.txtSenderName.setVisibility(View.VISIBLE);
                    holder.binding.txtTimeChatM.setVisibility(View.VISIBLE);
                    holder.binding.imageView32.setVisibility(View.VISIBLE);
                }
            if (chat.getReactions() == null) holder.binding.reactionRecycler.setVisibility(View.GONE);
             else {
                chatReactionsViewModel.getReactions(chat.getSeq()+"%", topic).observe(ChatActivity.this, reactions -> {
                    if (reactions.size()>0) {
                        holder.binding.reactionRecycler.setVisibility(View.VISIBLE);
                        ReactionAdapter adapter = new ReactionAdapter(reactions);
                        adapter.notifyDataSetChanged();
                        holder.binding.reactionRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
                        holder.binding.reactionRecycler.setAdapter(adapter);
                    }else holder.binding.reactionRecycler.setVisibility(View.GONE);
                });
//                ReactionAdapter adapter = new ReactionAdapter(chat.getReactions());
//                adapter.notifyDataSetChanged();
//                holder.binding.reactionRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
//                holder.binding.reactionRecycler.setAdapter(adapter);
            }
            holder.binding.txtSenderName.setText(senderName);
            if (chat.getImageURL() == null) holder.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderName));
            else Glide.with(ChatActivity.this).load(chat.getImageURL()).circleCrop().into(holder.binding.imageView32);
           if(senderName.equals("Unknown") && chat.getImageURL() == null){
               holder.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(myChannelDB.getName()));
               holder.binding.txtSenderName.setText(myChannelDB.getName());
            }
        }

        class ViewHolder extends RootViewHolder {
            private final ItemChatBinding binding;
            private ViewHolder(ItemChatBinding binding) {super(binding.getRoot());this.binding = binding;}
        }
        class FileHolder extends RootViewHolder {
            private final ChatFilesLayoutBinding binding;
            private FileHolder(ChatFilesLayoutBinding binding) {super(binding.getRoot());this.binding = binding;}
        }
        class VideoViewHolder extends RootViewHolder {
            private final VideoChatViewBinding videoChatViewBinding;
            private VideoViewHolder(VideoChatViewBinding binding) {super(binding.getRoot());this.videoChatViewBinding = binding;}
        }

        class RootViewHolder extends RecyclerView.ViewHolder {
            public RootViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        class VoiceNoteViewHolder extends RootViewHolder {
            private final VoicnotesChatBinding speedChatBinding;
            private final VoicnotesChatBinding binding;
            private VoiceNoteViewHolder(VoicnotesChatBinding binding) {super(binding.getRoot());this.binding = binding;
                speedChatBinding = VoicnotesChatBinding.bind(this.binding.getRoot());

            }
        }
    }

    static class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ViewHolder> {
        List<DataMessage.Reaction>reactions;
        public ReactionAdapter(List<DataMessage.Reaction> reactions) {this.reactions = reactions;
            // setReactions(reactions);
        }
        //List<List<DataMessage.Reaction>> reactions = new ArrayList<>();
        private void setReactions(List<DataMessage.Reaction> reactions) {
            //if (reactions == null) return;else this.reactions.addAll(reactions.stream().collect(Collectors.groupingBy(DataMessage.Reaction::getContent)).values());
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemReactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DataMessage.Reaction reactionList = reactions.get(position);
            holder.binding.reactionText.setText(reactionList.getContent());
            holder.binding.countText.setText("1");
        }

        @Override
        public int getItemCount() {
            return reactions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemReactionBinding binding;
            public ViewHolder(@NonNull ItemReactionBinding binding) {super(binding.getRoot());this.binding = binding;}
        }
    }

    class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ImageViewHolder> {
        ArrayList<FilesAdapterType> imgUrls;
        ChatMessageViewModel chatMessageViewModel;

        public PhotoAdapter(ArrayList<FilesAdapterType> imgUrls,ChatMessageViewModel chatViewModel) {this.imgUrls = imgUrls;this.chatMessageViewModel = chatViewModel;}
        @NonNull @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImageViewHolder(ItemChatImagesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String type = imgUrls.get(position).getType();
            if (type != null && type.equals("video/webm;codecs=vp8") || Objects.equals(type, "image/m4v")){
                holder.imagesBinding.play.setVisibility(View.VISIBLE);
                holder.imagesBinding.fileCardView.setVisibility(View.GONE);
                holder.imagesBinding.VoiceNoteLayout.setVisibility(View.GONE);
                try {
                    String imgUrl = domain + imgUrls.get(position) .getUrl()+ "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                    Glide.with(ChatActivity.this).load(imgUrl).into(holder.imagesBinding.displayImages);
                    holder.imagesBinding.displayImages.setTransitionName(imgUrl);
                    holder.imagesBinding.displayImages.setOnClickListener(v -> {
                        Intent intent = new Intent(ChatActivity.this, CheatVideoPlaying.class);
                        intent.putExtra("videoURL", v.getTransitionName());
                        intent.putExtra("ChatMessageViewModel",chatMessageViewModel);
                        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, v, v.getTransitionName()).toBundle());
                    });
                } catch (UnsupportedEncodingException e) {e.printStackTrace();}
            }else if (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/jpg") || type.equals("image/gif") || type.equals("image/webp") || type.equals("image/tiff") || type.equals("image/raw") || type.equals("image/bmp") || type.equals("image/heif") || type.equals("image/jpeg2000") || type.equals("image/jfif") || type.equals("image/.jfif")|| type.equals("Image")) {
                holder.imagesBinding.play.setVisibility(View.GONE);
                holder.imagesBinding.fileCardView.setVisibility(View.GONE);
                holder.imagesBinding.VoiceNoteLayout.setVisibility(View.GONE);
                try {
                    String imgUrl = domain + imgUrls.get(position).getUrl() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                    Glide.with(ChatActivity.this).load(imgUrl).into(holder.imagesBinding.displayImages);
                    holder.imagesBinding.displayImages.setTransitionName(imgUrl);
                    holder.imagesBinding.displayImages.setOnClickListener(v -> {
                        Intent intent = new Intent(ChatActivity.this, ChatImageActivity.class);
                        intent.putExtra("originalMassage",chatMessageViewModel);
                        intent.putExtra(MMSImageViewActivity.MMS_IMAGE_URL, v.getTransitionName());
                        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, v, v.getTransitionName()).toBundle());
                    });
                } catch (UnsupportedEncodingException e) {e.printStackTrace();}
            } else if (type.equals("text/plain") || type.equals("application/pdf") || type.equals("application/zip") || type.equals("text/html") || type.equals("text/javascript") || type.equals("application/msword") || type.equals("application/vnd.ms-powerpoint") || type.equals("application/vnd.ms-excel") || type.equals("application/x-rar-compressed") || type.equals("application/rtf") || type.equals("application/json") || type.equals("application/javascript") || type.equals("text/css") || type.equals("application/vnd.android.package-archive")) {
                holder.imagesBinding.fileCardView.setVisibility(View.VISIBLE);
                holder.imagesBinding.play.setVisibility(View.GONE);
                holder.imagesBinding.displayImages.setVisibility(View.GONE);
                holder.imagesBinding.VoiceNoteLayout.setVisibility(View.GONE);
                String type1 = imgUrls.get(position).getUrl().substring(imgUrls.get(position).getUrl().lastIndexOf("/")+1);
                holder.imagesBinding.btnFile.setText("OPEN " + type1 + " FILE");
                String newURL = null;
                try {newURL = domain + imgUrls.get(position).getUrl() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);} catch (UnsupportedEncodingException e) {e.printStackTrace();}
                String finalNewURL = newURL;
                holder.imagesBinding.btnFile.setOnClickListener(v -> {
                    try {url1 = new URL(finalNewURL);openFile(imgUrls.get(position).getType());} catch (URISyntaxException | MalformedURLException e) {e.printStackTrace();}
                });
            }else if (type.equals("audio/wav")){
                holder.imagesBinding.fileCardView.setVisibility(View.GONE);
                holder.imagesBinding.play.setVisibility(View.GONE);
                holder.imagesBinding.displayImages.setVisibility(View.GONE);
                holder.imagesBinding.VoiceNoteLayout.setVisibility(View.VISIBLE);
                holder.imagesBinding.playChat1.setOnClickListener(v -> {
                     AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    boolean isMuted = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
                    if (isMuted) Toast.makeText(ChatActivity.this, "Please turn the volume up", Toast.LENGTH_SHORT).show();
                    String path = imgUrls.get(position).getUrl();
                    try {
                        currentPlay = holder.imagesBinding.playChat1;
                        seekBar = holder.imagesBinding.sb;
                        // textView = root.binding.textView40;
                        String newURL = domain + path + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
                        if (!newURL.equals(url)) {
                            url = newURL;
                            Uri myUri = Uri.parse(newURL);
                            voicemailViewModel.addURItoQueueChat(MediaControllerCompat.getMediaController(ChatActivity.this), myUri);
                            changePlayback(true);
                        } else changePlayback(false);
                    } catch (Exception e) {}
                });
            } else {
                holder.imagesBinding.fileCardView.setVisibility(View.GONE);
                holder.imagesBinding.play.setVisibility(View.GONE);
                holder.imagesBinding.displayImages.setVisibility(View.GONE);
                holder.imagesBinding.VoiceNoteLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return imgUrls.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ItemChatImagesBinding imagesBinding;
            public ImageViewHolder(@NonNull ItemChatImagesBinding imagesBinding) {super(imagesBinding.getRoot());this.imagesBinding = imagesBinding;}
        }
    }

    private void openKeyBoard() {
        binding.chatEtText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.chatEtText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void changePlayback(boolean forcePlay) {
       int pbState = MediaControllerCompat.getMediaController(ChatActivity.this).getPlaybackState().getState();
       if (pbState == PlaybackStateCompat.STATE_PLAYING) {
          if (forcePlay) {
              MediaControllerCompat.getMediaController(this).getTransportControls().play();
              currentPlay.setImageResource(R.drawable.ic_baseline_pause_24);
          } else {
             MediaControllerCompat.getMediaController(this).getTransportControls().pause();
             currentPlay.setImageResource(R.drawable.ic_outline_play_arrow_24);
         }
       } else if (pbState == PlaybackStateCompat.STATE_PAUSED || pbState == 0) {
           MediaControllerCompat.getMediaController(this).getTransportControls().play();
           currentPlay.setImageResource(R.drawable.ic_baseline_pause_24);
    } else if (pbState == PlaybackStateCompat.STATE_STOPPED){
           seekBar.setProgress(0);
       }
}

    private void handleMetaData(MediaMetadataCompat metadata) {
        int duration = (int) metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
        if (duration > 0) seekBar.setMax(duration);
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
        pubMassage2 = null;
        ctrls.clear();urls.clear();attachmentsList.clear();mentions.clear();attachmentsList1.clear();
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

    private void playAudio() throws IOException {
        mediaPlayer = new MediaPlayer();audioHasStarted = true;
        mediaPlayer.setDataSource(SettingsHelper.getRecordFilePath(getApplicationContext()));
        mediaPlayer.prepare();mediaPlayer.start();isPlaying = true;
        binding.voiceNoteToSend.playChat.setImageResource(R.drawable.ic_baseline_pause_24);
        mediaPlayer.setOnCompletionListener(mp -> {stopPlaying();audioHasFinished = true;});
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
    public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder>{
        List<Replies> repliesList;
        public ReplyAdapter(List<Replies> repliesList) {
            Set<Replies> repliesSet = new HashSet<>(repliesList);
            List<Replies> repliesList1 = new ArrayList<>(repliesSet);
            this.repliesList = repliesList1;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ReplyInitialsIconBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Channel senderChannel = ChatViewModel.getInstance().getChannelsByTopic().get(repliesList.get(position).getFrom());
            holder.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(senderChannel != null && senderChannel.getName() != null?senderChannel.getName():"UnKnown"));
        }

        @Override
        public int getItemCount() {return repliesList.size();}

        class ViewHolder extends RecyclerView.ViewHolder{
            ReplyInitialsIconBinding binding;
            public ViewHolder(@NonNull ReplyInitialsIconBinding initialsIconBinding) {super(initialsIconBinding.getRoot());this.binding = initialsIconBinding;}
        }
    }
    private void openFile(File url) {
        try {
            Uri uri = FileProvider.getUriForFile(ChatActivity.this, "com.telebroad.teleconsole.fileprovider", url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {intent.setDataAndType(uri, "application/pdf");
            } else if (url.toString().contains(".html")| url.toString().contains(".htm")) {intent.setDataAndType(uri, "text/html");
            }else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")){intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip")) {intent.setDataAndType(uri, "application/zip");
            } else if (url.toString().contains(".rar")){intent.setDataAndType(uri, "application/x-rar-compressed");
            } else if (url.toString().contains(".rtf")) {intent.setDataAndType(uri, "application/rtf");
            }  else if (url.toString().contains(".txt")) {intent.setDataAndType(uri, "text/plain");
            //} else if (url.toString().contains(".brf")) {intent.setDataAndType(uri, "application/x-brf");
            } else if (url.toString().contains(".brf")) {intent.setDataAndType(uri, "text/plain");
            }else if (url.toString().contains(".bin")) {intent.setDataAndType(uri, "text/plain");
            }else if (url.toString().contains(".json")) {intent.setDataAndType(uri, "text/plain");
            }else if (url.toString().contains(".js")) {intent.setDataAndType(uri, "text/plain");
            }else if (url.toString().contains(".css")) {intent.setDataAndType(uri, "text/plain");
            }else {intent.setDataAndType(uri, "*/*");}
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ChatActivity.this,"No application found which can open the file", Toast.LENGTH_SHORT).show();
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
                    byte[] b = new byte[8192];
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
    private void ClearImageVies(){
        ctrls.clear();urls.clear();attachmentsList.clear();
        binding.imgToSend.setVisibility(View.GONE);
        binding.imgToSend.setAlpha(1.0f);
        binding.imgToSend.animate().translationY(0);
        binding.imgToSend.animate().setListener(null);
        binding.chatEtText.setMaxLines(1);
        binding.chatEtText.setText("");
        binding.mic.setImageResource(R.drawable.ic_microphone);
        if (mediaRecorder != null && mediaRecorder.getMaxAmplitude()>0){mediaRecorder.stop();mediaRecorder.release();mediaRecorder = null;}
        ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.relTxtLayout.getLayoutParams();
        constraintLayout.topMargin = 0;
        constraintLayout.leftMargin = 0;
        constraintLayout.rightMargin = 105;
        binding.relTxtLayout.setLayoutParams(constraintLayout);
    }
    public static class RecyclerViewItemTouchListener implements RecyclerView.OnItemTouchListener {
        private final GestureDetector mGestureDetector;
        private final RecyclerViewItemTouchListener.OnItemLongClickListener mListener;
        private final RecyclerView mRecyclerView;
        private final RecyclerViewItemTouchListener.OnItemClickListener mClickListener;

        public RecyclerViewItemTouchListener(Context context, final RecyclerView recyclerView, RecyclerViewItemTouchListener.OnItemLongClickListener listener,RecyclerViewItemTouchListener.OnItemClickListener clickListener) {
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
    protected void onDestroy() {super.onDestroy();
        connectivityManager.unregisterNetworkCallback(networkCallback);
        connectivityManager.unregisterNetworkCallback(networkCallback);
        dialog = null;
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            if (controller.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING) voicemailViewModel.removeQueueItem(controller);
            controller.unregisterCallback(controllerCallback);
        }
        if (mediaBrowser != null && mediaBrowser.isConnected()) mediaBrowser.disconnect();
        LeaveMessage leaveMessage1 = new LeaveMessage();
        leaveMessage1.setId("falseLeave");
        leaveMessage1.setTopic(topic);
        leaveMessage1.setUnsub(false);
        ChatWebSocket.getInstance().sendObject("leave", leaveMessage1);
    }
    private void ctrlFiles(String type,Uri uri) throws IOException {
        uploadFile.uploadFile(uri, getContentResolver(), type, secret, jsonObject -> {
            runOnUiThread(() -> {
//                CtrlMessage ct = gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class);
//                ctrls.add(gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class));
//                binding.fileLayout.fileName.setText(ct.getParams().getUrl());
//                binding.pdfLayout.pdfName.setText(ct.getParams().getUrl());
//                DisplayTheMargins();
//                if (ct.getParams().getUrl().substring(ct.getParams().getUrl().lastIndexOf(".")).equals(".pdf")) binding.pdfLayout.getRoot().setVisibility(View.VISIBLE);
//                else binding.fileLayout.getRoot().setVisibility(View.VISIBLE);
            });

        });
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile ("data", "."+type, getFilesDir());
        OutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        AndroidNetworking.upload("https://apiconnact.telebroad.com/v0/file/u/?")
                .addHeaders("x-tinode-apikey", "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo")
                .addHeaders("x-tinode-auth", "Token " + secret)
                .addMultipartFile("file", tempFile)
                .addMultipartParameter("id", type)
                .setPriority(Priority.HIGH).build().setUploadProgressListener((bytesUploaded, totalBytes) -> {
                }).getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
//                        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
//                        CtrlMessage ct = gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class);
//                        ctrls.add(gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class));
//                        binding.fileLayout.fileName.setText(ct.getParams().getUrl());
//                        binding.pdfLayout.pdfName.setText(ct.getParams().getUrl());
//                        DisplayTheMargins();
//                        if (ct.getParams().getUrl().substring(ct.getParams().getUrl().lastIndexOf(".")).equals(".pdf")) binding.pdfLayout.getRoot().setVisibility(View.VISIBLE);
//                        else binding.fileLayout.getRoot().setVisibility(View.VISIBLE);

                    }
                    @Override
                    public void onError(ANError anError) {}
                });
    }
}