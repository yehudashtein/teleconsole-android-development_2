package com.telebroad.teleconsole.chat.viewModels;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.chat.server.MetaMessage;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatViewModel {
    private static final ChatViewModel instance = new ChatViewModel();
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    List<ChannelDB> mention;
    Callback callback;
    Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, ChannelDB> channelsByUser = new HashMap<>();
    public Map<String,ChannelDB> getFNDGroupChannels = new HashMap<>();
    public Map<String,ChannelDB> getFNDGroupChannelsToMe = new HashMap<>();
    public Map<String,ChannelDB> getTopicFromName = new HashMap<>();
    private final LiveData<Map<String, ChannelDB>> TopicFromName;
    private final LiveData<Map<String, ChannelDB>> users;
    private final LiveData<Map<String, ChannelDB>> groups;
    private final LiveData<Map<String, ChannelDB>> meGroups;

    public ChatViewModel(Callback callback) {
        this();
        this.callback = callback;
    }

    public static ChatViewModel getInstance(){
        try {
            return instance;
        }catch (ExceptionInInitializerError ignored){}
        return instance;
    }
    public void observe(){
        users.observeForever( (data) -> {
            if (data != null) {
                android.util.Log.d("CUnull", "Clearing");
                channelsByUser.clear();
                android.util.Log.d("CUnull", "putting all " + data);
                channelsByUser.putAll(data);
                android.util.Log.d("CUnull", "finished putting");
            }
        });
        groups.observeForever((data) -> {
            if (data != null){
                getFNDGroupChannels.clear();
                getFNDGroupChannels.putAll(data);
            }
        });
        meGroups.observeForever(stringChannelDBMap -> {
            getFNDGroupChannelsToMe.clear();
            getFNDGroupChannelsToMe.putAll(stringChannelDBMap);
        });
        TopicFromName.observeForever(stringChannelDBMap -> {
            getTopicFromName.clear();
            getTopicFromName.putAll(stringChannelDBMap);
        });
    }
    private ChatViewModel(){
        users = ChatDatabase.getInstance().channelDao().getUsersByName();
        groups = ChatDatabase.getInstance().channelDao().getFNDGroupChannels();
        TopicFromName = ChatDatabase.getInstance().channelDao().getUsersByTopic();
        meGroups = ChatDatabase.getInstance().channelDao().getMeUsersByTopic();
        if (Utils.isMainThread()) {
            observe();
        } else {
            new Handler(Looper.getMainLooper()).post(this::observe);
        }
    }

    public Map<String, ChannelDB> getChannelsByTopic() {
        return channelsByUser;
    }

    public List<ChannelDB> getDirectChannelList(){
        return getLiveDirectChannels().getValue();
    }
    public LiveData<List<ChannelDB>>  getLiveDirectChannels(){
        return ChatDatabase.getInstance().channelDao().getDirectChannels();
    }
    public Map<String, ChannelDB> getChannelsByName() {
        return getTopicFromName;
    }


    public List<ChannelDB> getTeamChannelList(){
        return getLiveTeamChannels().getValue();
    }
    public LiveData<List<ChannelDB>>  getLiveTeamChannels(){
        return ChatDatabase.getInstance().channelDao().getTeamChannels();
    }

    public Void getLiveMentionChannelsSearch(){
        executorService.execute(() -> {
            mention   =  ChatDatabase.getInstance().channelDao().getChannelsForMention();
            handler.post(() -> callback.onComplete(mention));
        });
        return null;
    }
    public void updateChannels(List<MetaMessage.Sub> subs, String subbedTo){
        if (subs == null){
            return;
        }
        ChatDatabase.getInstance(AppController.getInstance()).channelDao()
                .save(subs.stream().map(sub -> new ChannelDB(sub, subbedTo))
                        .filter(sub -> sub.getName() != null && !sub.getName().isEmpty() )
                        .toArray(ChannelDB[]::new));
//        List<Channel> allChats = subs.stream().map(Channel::new).collect(Collectors.toList());
//        directChannels.replaceAll(allChats.stream().filter(Channel::isDirect).collect(Collectors.toList()));
//        directChannels.stream().map(Channel::getName).forEach(name -> android.util.Log.d("CWSChanDirect", "Name is " + name));
//        teamChannels.replaceAll(allChats.stream().filter(Channel::isGroup).collect(Collectors.toList()));
//        teamChannels.stream().map(Channel::getName).forEach(name -> android.util.Log.d("CWSChanGroup", "Name is " + name));
    }
    public void updateChannels(MetaMessage metaMessage){
        ChatDatabase.getInstance(AppController.getInstance()).channelDao().save(new ChannelDB(metaMessage));
    }
    public interface Callback {
        void onComplete(List<ChannelDB> result);
    }
}
