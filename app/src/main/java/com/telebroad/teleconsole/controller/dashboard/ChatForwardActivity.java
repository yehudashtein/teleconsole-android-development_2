package com.telebroad.teleconsole.controller.dashboard;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.PubMassage2;
import com.telebroad.teleconsole.chat.client.SubMessage;
import com.telebroad.teleconsole.db.models.Attachments;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.chat.server.CtrlMessage;
import com.telebroad.teleconsole.chat.viewModels.ChatMessageViewModel;
import com.telebroad.teleconsole.chat.viewModels.ChatViewModel;
import com.telebroad.teleconsole.databinding.ActivityChatForwardBinding;
import com.telebroad.teleconsole.databinding.AutoCompleteRowBinding;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ChatForwardActivity extends AppCompatActivity implements ChatViewModel.Callback,ChatWebSocket.forwardCrr{
    private  ActivityChatForwardBinding binding;
    private List<ChannelDB> forwardList;
    private OnBackPressedCallback callback;
    private String attachments;
    private List<ChannelDB> filtereddList;
    private ForwardAdapter forwardAdapter;
    private SearchView searchView;
    private List<CtrlMessage> ctrlMessageList;
    private ChatMessageViewModel chatMessageViewModel;
    private Collection<Attachments> enums;
    private OnBackPressedListener onBackPressedListener;
    private Replies replies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatForwardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        filtereddList = new ArrayList<>();ctrlMessageList = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        ChatWebSocket.getInstance().setForwardCtr(this);
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent, null));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ChatViewModel chatViewModel = new ChatViewModel(this);
        chatViewModel.getLiveMentionChannelsSearch();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            chatMessageViewModel = getIntent().getSerializableExtra  ("messageObject",ChatMessageViewModel.class);
            String replies1 = getIntent().getStringExtra("messageObject");
            replies = gson.fromJson(replies1,new TypeToken<Replies>() {}.getType());
            attachments = getIntent().getStringExtra("replyAttachments");
        }
        else {
            // Handle lower SDK versions
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("messageObject")) {
                Object messageObject = extras.get("messageObject");
                if (messageObject instanceof ChatMessageViewModel) {
                    chatMessageViewModel = (ChatMessageViewModel) messageObject;
                    String replies1 = getIntent().getStringExtra("messageObject");
                    replies = gson.fromJson(replies1,new TypeToken<Replies>() {}.getType());
                    attachments = getIntent().getStringExtra("replyAttachments");
                }
            }
        }
        onBackPressedListener = this::finish;
        if (chatMessageViewModel != null){
            String attachments = gson.toJson(chatMessageViewModel.getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            enums = gson.fromJson(attachments, collectionType);
        }else {
            //String attachments = gson.toJson(replies.getHead().getAttachments());
            Type collectionType = new TypeToken<Collection<Attachments>>() {}.getType();
            enums = gson.fromJson(attachments, collectionType);
        }

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedListener.onBack();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    public interface OnBackPressedListener {
        void onBack();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                //callback.handleOnBackPressed();
                break;
            case R.id.send:
                if (ctrlMessageList.size() != 0){
                    for (CtrlMessage c:ctrlMessageList){
                        if (enums.size()==0) {
                            if (chatMessageViewModel!= null){
                                PubMassage2 forwardPub = new PubMassage2(c.getTopic() + ":" + chatMessageViewModel.getSeq(), c.getTopic(), SettingsHelper.getString(SettingsHelper.MY_TOPIC), true, chatMessageViewModel.getContent()
                                        , new PubMassage2.Head(true));
                                ChatWebSocket.getInstance().sendObject("pub", forwardPub);
                            }else {
                                PubMassage2 forwardPub = new PubMassage2(c.getTopic() + ":" + replies.getSeq(), c.getTopic(), SettingsHelper.getString(SettingsHelper.MY_TOPIC), true, replies.getContent()
                                        , new PubMassage2.Head(true));
                                ChatWebSocket.getInstance().sendObject("pub", forwardPub);
                            }

                        }else {
                            List<PubMassage2.Attachments> attachmentsList = new ArrayList<>();
                            List<String> urls = new ArrayList<>();
                            for (Attachments a:enums){
                                if (a.getType().equals("video/webm;codecs=vp8")){
                                    PubMassage2.Attachments attachments = new PubMassage2.Attachments("video" +
                                            SettingsHelper.getDateString() + ".mp4",a.getPath(),"Video",false,false,false,true);
                                    attachmentsList.add(attachments);
                                    urls.add(a.getPath());
                                }else if (a.getType().equals("audio/wav")){
                                    PubMassage2.Attachments attachments = new PubMassage2.Attachments("voice_note" +
                                            SettingsHelper.getDateString() + ".wav",a.getPath(),"Audio",true,false);
                                    attachmentsList.add(attachments);
                                    urls.add(a.getPath());
                                }else if (a.getType().startsWith("image")){
                                    String urlEnd = a.getPath().substring(a.getPath().lastIndexOf(".") + 1);
                                    PubMassage2.Attachments attachments = new PubMassage2.Attachments("img_" +
                                            SettingsHelper.getDateString() + "." + urlEnd,a.getPath(),"Image",false,true);
                                    attachmentsList.add(attachments);
                                    urls.add(a.getPath());
                                }else if (a.getType().equals("application/pdf")){
                                    String urlEnd = a.getPath().substring(a.getPath().lastIndexOf(".") + 1);
                                    PubMassage2.Attachments attachments = new PubMassage2.Attachments("pdf_" +
                                            SettingsHelper.getDateString() + "." + urlEnd,a.getPath(),"PDF",false,false,true);
                                    attachmentsList.add(attachments);
                                    urls.add(a.getPath());
                                }
                            }
                            if (chatMessageViewModel != null&& chatMessageViewModel.getContent() != null && !chatMessageViewModel.getContent().toString().equals("")){
                                PubMassage2.Head head = PubMassage2.setHeadWithAttachmentsForwards(attachmentsList);
                                PubMassage2 pub = PubMassage2.setPubForImages(head, "forward", SettingsHelper.getString(SettingsHelper.MY_TOPIC), c.getTopic(),chatMessageViewModel.getContent().toString(), urls);
                                ChatWebSocket.getInstance().sendObject("pub", pub);
                            }else if (chatMessageViewModel != null){
                                PubMassage2.Head head = PubMassage2.setHeadWithAttachmentsForwards(attachmentsList);
                                PubMassage2 pub = PubMassage2.setPubForImagesForward(head, "forward", SettingsHelper.getString(SettingsHelper.MY_TOPIC), c.getTopic(), urls);
                                ChatWebSocket.getInstance().sendObject("pub", pub);
                            }else if (chatMessageViewModel == null && !replies.getContent().equals("")){
                                PubMassage2.Head head = PubMassage2.setHeadWithAttachmentsForwards(attachmentsList);
                                PubMassage2 pub = PubMassage2.setPubForImages(head, "forward", SettingsHelper.getString(SettingsHelper.MY_TOPIC), c.getTopic(), replies.getContent(), urls);
                                ChatWebSocket.getInstance().sendObject("pub", pub);
                            }else {
                                PubMassage2.Head head = PubMassage2.setHeadWithAttachmentsForwards(attachmentsList);
                                PubMassage2 pub = PubMassage2.setPubForImagesForward(head, "forward", SettingsHelper.getString(SettingsHelper.MY_TOPIC), c.getTopic(), urls);
                                ChatWebSocket.getInstance().sendObject("pub", pub);
                            }
                        }
                    }
                    ctrlMessageList.clear();
                    binding.chatForwardEditText.setText("");
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onPause() {super.onPause();overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_forward, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setOnCloseListener(() -> {
                searchItem.collapseActionView();
                forwardAdapter.submitList(forwardList);
                return false;
            });
        }
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                searchView.setIconified(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtereddList.clear();
                for (ChannelDB s1:forwardList){
                    if (s1.getName().toLowerCase().contains(newText.toLowerCase())){
                        filtereddList.add(s1);
                    }
                }
                forwardAdapter.submitList(filtereddList);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onComplete(List<ChannelDB> result) {
        forwardList = result;
        forwardAdapter = new ForwardAdapter(new DIFF_CALLBACK(),binding);
        forwardAdapter.submitList(result);
        binding.forwardRv.setLayoutManager(new WrapContentLinearLayoutManager(this));
        //binding.forwardRv.setLayoutManager(new LinearLayoutManager(this));
        binding.forwardRv.setAdapter(forwardAdapter);
    }


    @Override
    public void GetCtr(CtrlMessage ctrlMessage) {
       if (ctrlMessage.getId().equals("subForward")){
           ctrlMessageList.add(ctrlMessage);
       }

    }

    static class DIFF_CALLBACK extends DiffUtil.ItemCallback<ChannelDB>{

        @Override
        public boolean areItemsTheSame(@NonNull ChannelDB oldItem, @NonNull ChannelDB newItem) {
            return oldItem.getPrimaryKey().equals(newItem.getPrimaryKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChannelDB oldItem, @NonNull ChannelDB newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    }
    static class  ForwardAdapter extends ListAdapter<ChannelDB, ForwardAdapter.ViewHolder>{
        List<ChannelDB> newForwardList;

        @Override
        public void submitList(@Nullable List<ChannelDB> list) {
            Set<ChannelDB> uniqueSet = new HashSet<>(list);
            newForwardList = new ArrayList<>(uniqueSet);
            super.submitList(newForwardList);
            notifyDataSetChanged();
        }
        private final WeakReference<ActivityChatForwardBinding> mBindingRef;

        public ForwardAdapter(@NonNull DiffUtil.ItemCallback<ChannelDB> diffCallback,ActivityChatForwardBinding binding) {
            super(diffCallback);
            mBindingRef = new WeakReference<>(binding);
        }

        @NonNull
        @Override
        public ForwardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(AutoCompleteRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ForwardAdapter.ViewHolder holder, int position) {
            ActivityChatForwardBinding binding = mBindingRef.get();
                holder.binding.flag.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(newForwardList.get(position).getName()));
                holder.binding.countryName.setText(newForwardList.get(position).getName());
                holder.binding.checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        try {
                            binding.chatForwardEditText.setText(binding.chatForwardEditText.getText().toString()
                                    + newForwardList.get(position).getName() + ",");
                            ChannelDB channelDB5 = ChatViewModel.getInstance().getChannelsByName().get(newForwardList.get(position).getName());
                            SubMessage subMessage = new SubMessage("subForward", channelDB5.getTopic());
                            ChatWebSocket.getInstance().sendObject("sub", subMessage);
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }

                    }
                });
        }

        @Override
        public int getItemCount() {
            return newForwardList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            AutoCompleteRowBinding binding;
            public ViewHolder(@NonNull AutoCompleteRowBinding binding) {
                super(binding.getRoot());this.binding = binding;
            }
        }
    }


    public class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        //... constructor
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }
}