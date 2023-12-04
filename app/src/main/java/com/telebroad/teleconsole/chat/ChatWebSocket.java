package com.telebroad.teleconsole.chat;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.chat.client.Content;
import com.telebroad.teleconsole.chat.client.GetMessage;
import com.telebroad.teleconsole.chat.client.HiMessage;
import com.telebroad.teleconsole.chat.client.JoinTopicModel;
import com.telebroad.teleconsole.chat.client.LeaveMessage;
import com.telebroad.teleconsole.chat.client.LoginMessage;
import com.telebroad.teleconsole.chat.client.NoteMessage;
import com.telebroad.teleconsole.chat.client.SubMessage;
import com.telebroad.teleconsole.chat.client.setMassage;
import com.telebroad.teleconsole.chat.server.CtrlMessage;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.chat.server.InfoMessage;
import com.telebroad.teleconsole.chat.server.MetaMessage;
import com.telebroad.teleconsole.chat.server.MetaMessage1;
import com.telebroad.teleconsole.chat.server.PresMessage;
import com.telebroad.teleconsole.chat.viewModels.ChatRepliesViewModel;
import com.telebroad.teleconsole.chat.viewModels.ChatViewModel;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.ChatActivity;
import com.telebroad.teleconsole.controller.dashboard.JoinTopicActivity;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.ChatMessageDB;
import com.telebroad.teleconsole.db.models.GroupMembers;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.helpers.LiveDataList;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;

public class ChatWebSocket extends WebSocketListener {
    private static int reconnectInterval = 0;
    private int mySeq;
    public static boolean isConnected = false;
    private static final Timer reconnectTimer =  new Timer();
    public static final String CHAT_URL = "apiconnact.telebroad.com";
    private static final String APIKEY = "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo";
    public static final String KEY_APIKEY = "apikey";
    public static final String HI_ID = "HI_CHAT";
    public static final String LOGIN_ID = "LOGIN_CHAT";
    public static final String FND_ID = "chat.fnd.id";
    private final ChatRepliesViewModel chatRepliesViewModel;
    private forwardCrr forwardCtr;
    private String topic;
    public String senderName;
    private final MutableLiveData<Integer> ctrlMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> newMessageLiveData = new MutableLiveData<>();
    public MutableLiveData<Integer> getNewMessageLiveData() {
        return newMessageLiveData;
    }

    public MutableLiveData<Integer> getCtrlMessageLiveData() {
        return ctrlMessageLiveData;
    }

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private WebSocket webSocket;

    public void setForwardCtr(forwardCrr forwardCtr) {
        this.forwardCtr = forwardCtr;
    }

    public ChatWebSocket() {
        int min = 20;
        int max = 90;
        //forwardCtr = new ChatForwardActivity();
        chatRepliesViewModel = new ViewModelProvider.AndroidViewModelFactory(AppController.getInstance()).create(ChatRepliesViewModel.class);
        reconnectInterval = ThreadLocalRandom.current().nextInt(min, max + 1);
       // this.connect();
    }

    private static ChatWebSocket instance;
    public void connect() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().pingInterval(40, TimeUnit.SECONDS).callTimeout(10, TimeUnit.MINUTES).addInterceptor(interceptor)
                .retryOnConnectionFailure(true).build();
        //String chatUrl = "ws://46.102.175.1:6060/v0/channels?apikey=AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo";
        String chatUrl = "wss://apiconnact.telebroad.com/v0/channels?apikey=AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo";
        Request request = new Request.Builder().url(chatUrl).build();
        webSocket = client.newWebSocket(request, this);

    }
    private void reconnect() {
        int removedTasks = reconnectTimer.purge();
        if (removedTasks == 0) {
            // Timer has already been cancelled
        } else {
            reconnectTimer.cancel();
        }
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                webSocket = null;
                //connect();
            }
        }, reconnectInterval * 1000L);
       // android.util.Log.d("CWSCReconnecting", "Reconnecting in " +  reconnectInterval * 1000);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
        this.isConnected = false;
        //android.util.Log.d("CWSClosed", "Closed with code " + code+ " reason: " + reason );
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        //Log.d("CWSonClosing", reason + code);
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        this.isConnected = false;
        //android.util.Log.d("CWSFailure", "Failed " + response + " " + webSocket + " " + t.getMessage());
        if(!Objects.equals(t.getMessage(), "Software caused connection abort")){
            this.reconnect();
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Connection closed");
           // Log.d("Conn", "Connection closed");
        }
    }
    private String lastProcessedMessage = null;
    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        try {
            super.onMessage(webSocket, text);
            if (text.equals(lastProcessedMessage)) {
                // Duplicate message, return and reject
                return;
            }

            lastProcessedMessage = text;
            //android.util.Log.d("CWS", "Message " + text);
            JsonObject jsonObject = JsonParser.parseString(text).getAsJsonObject();
            String[] keys = jsonObject.keySet().toArray(new String[]{});
            if (keys.length == 1) {
                try {
                    parseMessage(keys[0], jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    //android.util.Log.e("CWS", "parse message failed", e);
                }
            } else {
                //android.util.Log.w("CWS", "key length mismatch " + keys.length + " " + keys);
            }
        } catch (Exception e) {
            //Log.v("CWSOnMessageError", e.getMessage());
        }
    }


    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        //android.util.Log.d("CWS", "Opened " + response);
        super.onOpen(webSocket, response);
        isConnected = true;
        sendObject("hi", new HiMessage());
    }

    public void sendMessage(String message) {
        webSocket.send(message);
    }

    public static ChatWebSocket getInstance() {
        //forwardCtr = new ChatForwardActivity();
        if (instance == null) {
            instance = new ChatWebSocket();
        }
        return instance;
    }

    public void sendObject(String key, Object object)  {
        if(this.isConnected) {
            JsonObject hiObject = new JsonObject();
            Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
            String innerString = gson.toJson(object);//.replaceAll("\\u003d", "=");
            hiObject.add(key, JsonParser.parseString(innerString));
            String json = gson.toJson(hiObject);
            json = json.replaceAll("\\u003d", "=");
            android.util.Log.d("CWSSendObjectJson",gson.toJson(hiObject));
            boolean hgg =  webSocket.send(gson.toJson(hiObject));
        }
    }
    public void sendTwoObjects(String key1, Object object1, String key2, Object object2) {
        if (this.isConnected) {
            JsonObject hiObject = new JsonObject();
            Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
            String innerString1 = gson.toJson(object1);
            hiObject.add(key1, JsonParser.parseString(innerString1));
            String innerString2 = gson.toJson(object2);
            hiObject.add(key2, JsonParser.parseString(innerString2));
            String json = gson.toJson(hiObject);
            json = json.replaceAll("\\u003d", "=");
            android.util.Log.d("CWSSendObjectJson", gson.toJson(hiObject));
            boolean hgg = webSocket.send(gson.toJson(hiObject));
        }
    }

    String jsonString = "{\"pub\"{\"id\":\"pubMessage\",\"topic\":usrxznaWvYQa-M\",\"noecho\":false,\"content\":\"abc\",\"head\":{\"mime\":\"text/html\"}}}";
    public void parseMessage(String key, JsonObject object) {
        Log.d("CWSSendObject", String.valueOf(object));
        if (key == null) {
            return;
        }
        switch (key) {
            case "ctrl":
                CtrlMessage ctrlMessage = new Gson().fromJson(object.get(key), CtrlMessage.class);
                String id = ctrlMessage.getId();
                if ("delSub".equals(id) || ctrlMessage.getCode().equals("304")){
                    GetMessage getMessage = new GetMessage();
                    getMessage.setWhat("data");
                    getMessage.setTopic(ctrlMessage.getTopic());
                    getMessage.getData().setLimit(25);
                    sendObject("get",getMessage);
                }
                if ("allTopics".equals(id)){
                    GetMessage getMessage = new GetMessage();
                    getMessage.setId("getAllTopics");
                    getMessage.setTopic("fnd");
                    getMessage.setWhat("sub");
                    sendObject("get",getMessage);
                }
                //if ("data".equals(ctrlMessage.getParams().getWhat()) && ctrlMessage.getCode().equals("204")){
                    //Log.d("listSize1","hi 5");
                   // ctrlMessageLiveData.postValue(ctrlMessage);
                    //ChatActivity.getChatData().removeObservers(ChatActivity.class.getl);
                //}
                if ("leaveTopic".equals(id)){
                    Utils.asyncTask(() -> ChatDatabase.getInstance().channelDao().DeleteChanel(ctrlMessage.getTopic()));
                }
                if ("setMyTopic".equals(id)){
                    GetMessage getMessage = new GetMessage();
                    getMessage.setId("getMyMete");
                    getMessage.setTopic(SettingsHelper.getString(SettingsHelper.MY_TOPIC));
                    getMessage.setWhat("desc");
                    sendObject("get",getMessage);
                }
                if ("joinNewP2p".equals(id)){
                    GetMessage getMessage4 = new GetMessage();
                    getMessage4.setId("getMeForSub");
                    getMessage4.setTopic("me");
                    getMessage4.setWhat("sub");
                    sendObject("get",getMessage4);
                }
                if ("fnd".equals(id)){
                    GetMessage getMessage = new GetMessage();
                    getMessage.setId("getFnd");
                    getMessage.setTopic("fnd");
                    getMessage.setWhat("sub");
                    sendObject("get",getMessage);
                    Map<String,String> map = new LinkedHashMap<>();
                    map.put("public","_topics="+SettingsHelper.getString(SettingsHelper.MY_TOPIC));
                    setMassage setMessage = new setMassage();
                    setMessage.setId("setMyTopic");
                    setMessage.setTopic("fnd");
                    setMessage.setDesc(map);
                   sendObject("set",setMessage);
                }
                if ("me".equals(id)){
                    GetMessage getMessage = new GetMessage();
                    getMessage.setId("getMe");
                    getMessage.setTopic("me");
                    getMessage.setWhat("sub");
                    GetMessage getMessage1 = new GetMessage();
                    getMessage1.setId("getMe");
                    getMessage1.setTopic("me");
                    getMessage1.setWhat("desc");
                    sendObject("get",getMessage);
                    sendObject("get",getMessage1);
                }
                if (HI_ID.equals(id)) {
                    String token = SettingsHelper.getString(SettingsHelper.JWT_TOKEN, "");
                    sendObject("login", new LoginMessage(token));
                }
                if ("leaveGroup".equals(id)){
                    Utils.asyncTask(() -> ChatDatabase.getInstance().channelDao().DeleteChanel(ctrlMessage.getTopic()));
                }
                if ("onlyP2p".equals(id)){
                    GetMessage getMessage = new GetMessage();
                    getMessage.setId("getOnlyP2p");
                    getMessage.setTopic("fnd");
                    getMessage.setWhat("sub");
                    sendObject("get",getMessage);
                }
                if ("replace".equals(id)){
                    if(AppController.getInstance().getActiveActivity() != null &&AppController.getInstance().getActiveActivity() instanceof ChatActivity && !AppController.getInstance().isActiveActivityPaused()){
                        //ChatActivity.getInstance().UpdateContent(ctrlMessage.getTopic(),ctrlMessage.getParams().getSeq());
//                        Handler handler = new Handler(Looper.getMainLooper());
//                        handler.post(() -> new ChatActivity().UpdateContent(ctrlMessage.getTopic(),ctrlMessage.getParams().getSeq()));
                    }
                }
                if (LOGIN_ID.equals(id)) {
                    if (ctrlMessage.getParams() != null && ctrlMessage.getParams().getUser() != null) {
                        SettingsHelper.putString(SettingsHelper.MY_TOPIC, ctrlMessage.getParams().getUser());
                    }
                    SubMessage subMessage = new SubMessage();
                    subMessage.setId("me");
                    subMessage.setTopic("me");
                    SubMessage subMessage1 = new SubMessage();
                    subMessage1.setId("fnd");
                    subMessage1.setTopic("fnd");
                    sendObject("sub", subMessage);
                    sendObject("sub", subMessage1);
                    //sendObject("sub", SubMessage.getFnd());
                    //sendObject("get",GetMessage.getFndToMe());
                    if (ctrlMessage.getParams() != null && ctrlMessage.getParams().getToken() != null){
                        SettingsHelper.putString(SettingsHelper.CHAT_TOKEN, ctrlMessage.getParams().getToken());
                    }
                }
                if ("onlyGrp".equals(id)){
                    GetMessage getMessage = new GetMessage();
                    getMessage.setId("getOnlyGrp");
                    getMessage.setTopic("fnd");
                    getMessage.setWhat("sub");
                    sendObject("get",getMessage);
                }
                if (ctrlMessage.getParams() != null && "data".equals(ctrlMessage.getParams().getWhat())) {
                    int seq = ChatDatabase.getInstance().chatMessageDao().maxSeqForTopic(ctrlMessage.getTopic());
                    int replySeq = ChatDatabase.getInstance().repliesDao().maxSeqForTopic(ctrlMessage.getTopic());
                    if (seq>replySeq){
                        sendObject("note", new NoteMessage(ctrlMessage.getTopic(), "recv", seq));
                        sendObject("note", new NoteMessage(ctrlMessage.getTopic(), "read", seq));
                        Utils.asyncTask(() ->ChatDatabase.getInstance().channelDao().setUnreadToZero(ctrlMessage.getTopic()));
                    }else {
                        sendObject("note", new NoteMessage(ctrlMessage.getTopic(), "recv", replySeq));
                        sendObject("note", new NoteMessage(ctrlMessage.getTopic(), "read", replySeq));
                        Utils.asyncTask(() ->ChatDatabase.getInstance().channelDao().setUnreadToZero(ctrlMessage.getTopic()));
                    }
                }
                if ("subForward".equals(id)){
                    if (forwardCtr != null){
                        forwardCtr.GetCtr(ctrlMessage);
                    }
                }
                if ("leaveChat".equals(id)) {
                    Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteAll());
                    Utils.asyncTask(() -> ChatDatabase.getInstance().channelDao().deleteAll());
                    Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteAll());
                    Utils.asyncTask(() -> ChatDatabase.getInstance().reactionsDao().deleteAll());
                    Utils.asyncTask(() -> ChatDatabase.getInstance().groupMembersDao().deleteAll());
                }
                if ("delete".equals(id)){
                }
//                if (id.equals(FND_ID)){
//                }
               if ("0".equals(id)){
               }
                break;
            case "meta":
                MetaMessage metaMessage = null;
                MetaMessage1 metaMessage1 = null;
                try {
                    metaMessage = new Gson().fromJson(object.get(key), MetaMessage.class);
                }catch (Exception e){
                    metaMessage1= new Gson().fromJson(object.get(key), MetaMessage1.class);
                }
                if (metaMessage != null) {
                    //MetaMessage metaMessage = new Gson().fromJson(object.get(key), MetaMessage.class);
                    ChatViewModel.getInstance().updateChannels(metaMessage.getSub(), metaMessage.getTopic());
                    if ("UsersFromGroup".equals(metaMessage.getId())) {
                        for (MetaMessage.Sub s : metaMessage.getSub()) {
                            ChatDatabase.getInstance().groupMembersDao().save(new GroupMembers(s, metaMessage.getTopic()));
                        }
                    }
                }
                if (metaMessage != null) {
                    if ("SubToTopic".equals(metaMessage.getId())){
                        GetMessage getMessage = new GetMessage();
                        getMessage.setId("getMe");
                        getMessage.setTopic("me");
                        getMessage.setWhat("sub");
                        GetMessage getMessage1 = new GetMessage();
                        getMessage1.setId("getMe");
                        getMessage1.setTopic("me");
                        getMessage1.setWhat("desc");
                        sendObject("get",getMessage);
                        sendObject("get",getMessage1);
                        //Log.d("letsSee3",metaMessage.getDesc().getSeq()+"");
                        Utils.updateLiveData(ctrlMessageLiveData, metaMessage.getDesc().getSeq());
                    }
                    if ("getMyMete".equals(metaMessage.getId())) {
                        ChatViewModel.getInstance().updateChannels(metaMessage);
                    }
                    if ("getMyMete".equals(metaMessage.getId())) {
                        Map<String, String> map = new LinkedHashMap<>();
                        map.put("public", "_topics=");
                        setMassage setMessage = new setMassage();
                        setMessage.setId("setNoTopic");
                        setMessage.setTopic("fnd");
                        setMessage.setDesc(map);
                        sendObject("set", setMessage);
                    }
                    if ("updatesForTopic".equals(metaMessage.getId())) {
                        if (metaMessage.getDesc().getPublicParams().getPhoto() != null && metaMessage.getDesc().getPublicParams().getPhoto().get("ref") != null) {
                            MetaMessage finalMetaMessage = metaMessage;
                            Utils.asyncTask(() -> ChatDatabase.getInstance().channelDao().UpdateImageUrl(finalMetaMessage.getDesc().getPublicParams()
                                    .getPhoto().get("ref"), finalMetaMessage.getTopic()));
                        } else {
//                        Log.d("updatesForTopicName",metaMessage.getDesc().getPublicParams().getFn());
//                        Log.d("updatesForTopicName",metaMessage.getTopic());
//                        Log.d("updatesForTopicName",metaMessage.getDesc().getDefacs().get("auth").toString());
//                        Log.d("updatesForTopicName",metaMessage.getDesc().getPublicParams().getDescription());
                            MetaMessage finalMetaMessage2 = metaMessage;
                            if (finalMetaMessage2.getDesc().getDefacs().get("auth") != null) {
                                Log.d("updatesForTopicName", finalMetaMessage2.getDesc().getPublicParams().getFn());
                                Log.d("updatesForTopicName", finalMetaMessage2.getTopic());
                                Log.d("updatesForTopicName", finalMetaMessage2.getDesc().getDefacs().get("auth").toString());
                                Log.d("updatesForTopicName", finalMetaMessage2.getDesc().getPublicParams().getDescription());
                                String acsMode = finalMetaMessage2.getDesc().getDefacs().get("auth").toString();
                                Utils.asyncTask(() -> ChatDatabase.getInstance().channelDao().UpdateName1(finalMetaMessage2.getDesc().getPublicParams()
                                        .getFn(), finalMetaMessage2.getDesc().getPublicParams().getDescription(), finalMetaMessage2.getTopic(), acsMode));
                            } else {
//                            MetaMessage finalMetaMessage1 = metaMessage;
//                            Utils.asyncTask(() -> ChatDatabase.getInstance().channelDao().UpdateName(finalMetaMessage1.getDesc().getPublicParams()
//                                    .getFn(), finalMetaMessage1.getDesc().getPublicParams().getDescription(), finalMetaMessage1.getTopic()));
                            }
                        }

                    }
                    if ("getOnlyP2p".equals(metaMessage.getId())) {
                        List<MetaMessage.Sub> subs = metaMessage.getSub();
                        List<JoinTopicModel> joinTopicModels = new ArrayList<>();
                        for (MetaMessage.Sub s : subs) {
                            joinTopicModels.add(new JoinTopicModel(metaMessage.getId(), s.getPublicParams().get("photo") != null ? s.getPublicParams().get("photo").toString() : "", s.getAcs().get("mode").toString(), s.getPublicParams().get("fn").toString()
                                    , s.getUser()));
                        }
                        String joinTopicJson = gson.toJson(joinTopicModels);
                        Intent intent = new Intent(AppController.getInstance(), JoinTopicActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("getOnlyGrp", joinTopicJson);
                        AppController.getInstance().startActivity(intent);
                    }
                    if ("getOnlyGrp".equals(metaMessage.getId())) {
                        List<MetaMessage.Sub> subs = metaMessage.getSub();
                        List<JoinTopicModel> joinTopicModels = new ArrayList<>();
                        for (MetaMessage.Sub s : subs) {
                            joinTopicModels.add(new JoinTopicModel(metaMessage.getId(), s.getAcs().get("mode").toString(), s.getPublicParams().get("fn").toString()
                                    , (int) Double.parseDouble(s.getPublicParams().get("subCount").toString()), s.getTopic()));
                        }
                        String joinTopicJson = gson.toJson(joinTopicModels);
                        Intent intent = new Intent(AppController.getInstance(), JoinTopicActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("getOnlyGrp", joinTopicJson);
                        AppController.getInstance().startActivity(intent);
                    }
                    if ("getAllTopics".equals(metaMessage.getId())){
                        List<MetaMessage.Sub> subs = metaMessage.getSub();
                        List<JoinTopicModel> joinTopicModels = new ArrayList<>();
                        for (MetaMessage.Sub s : subs) {
                            joinTopicModels.add(new JoinTopicModel(metaMessage.getId(), s.getPublicParams().get("photo") != null ? s.getPublicParams().get("photo").toString() : "", s.getAcs().get("mode").toString(), s.getPublicParams().get("fn").toString()
                                    ,s.getUser() != null ? s.getUser():s.getTopic()));
                        }
                        String joinTopicJson = gson.toJson(joinTopicModels);
                        Intent intent = new Intent(AppController.getInstance(), JoinTopicActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("getAllTopics", joinTopicJson);
                        AppController.getInstance().startActivity(intent);
                    }
                    if (FND_ID.equals(metaMessage.getId())) {
                        //sendObject("sub", SubMessage.getMe());
                    } else if ("me".equals(metaMessage.getTopic()) && metaMessage.getDesc() != null) {
                        String name = (String) metaMessage.getDesc().getPublicParams().getFn();
                    }
                }
                //}
//                if(metaMessage.getSub().equals("sub")){
//            }
                break;
            case "pres":
                PresMessage presMessage = new Gson().fromJson(object.get(key), PresMessage.class);
                //Log.d("CWSSendObject", String.valueOf(object));
                if (("on".equals( presMessage.getWhat())) || "off".equals(presMessage.getWhat()) && "me".equals(presMessage.getTopic()) && presMessage.getUa() != null) {
                    String status = presMessage.getWhat();
                    if ("on".equals(status)) {
                        ChatDatabase.getInstance().channelDao().UpdateStatus(presMessage.getSrc(), 1);
                    } else if ("off".equals(status)) {
                        ChatDatabase.getInstance().channelDao().UpdateStatus(presMessage.getSrc(), 0);
                    }
                }
                if (presMessage.getWhat().equals("on")&& presMessage.getUa()!= null){
                   // Log.d("letsSee",presMessage.getUa()+"");
                    SubMessage subMessage = new SubMessage();
                    subMessage.setId("joinNewP2p");
                    subMessage.setTopic(presMessage.getSrc());
                    sendObject("sub",subMessage);
                }
                if (presMessage.getWhat().equals("off")&& presMessage.getUa()== null){
                    LeaveMessage leaveMessage1 = new LeaveMessage();
                    leaveMessage1.setId("falseLeave");
                    leaveMessage1.setTopic(presMessage.getSrc());
                    leaveMessage1.setUnsub(true);
                    ChatWebSocket.getInstance().sendObject("leave", leaveMessage1);
                }
                if ("gone".equals(presMessage.getWhat())){
                    Utils.asyncTask(() -> ChatDatabase.getInstance().channelDao().DeleteChanel(presMessage.getSrc()));
                }
                if (presMessage.getHead() != null && presMessage.getHead().getReplace() != 0){
                    ChatDatabase.getInstance().chatMessageDao().UpdateContent((String) presMessage.getContent(),presMessage.getHead().getReplace(),presMessage.getSrc());
                }
                if ("msg".equals(presMessage.getWhat()) && "me".equals(presMessage.getTopic())) {
                    ChatDatabase.getInstance().channelDao().incrementUnread(presMessage.getSrc());
                    NoteMessage noteMessage = new Gson().fromJson(object.get(key), NoteMessage.class);
                    //sendObject("note", new NoteMessage( noteMessage.getTopic(), "recv",noteMessage.getSeq()));
                }
                if ("upd".equals(presMessage.getWhat())) {
                    //sendObject("get", GetMessage.getMe());
                    GetMessage getMessage = new GetMessage();
                    getMessage.setId("updatesForTopic");
                    getMessage.setWhat("desc");
                    getMessage.setTopic(presMessage.getSrc());
                    sendObject("get",getMessage);
                }
                if ("read".equals(presMessage.getWhat())){
                    try {
                        Utils.asyncTask(() ->ChatDatabase.getInstance().channelDao().setUnreadToZero(presMessage.getSrc()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                if("acs".equals(presMessage.getWhat())&& presMessage.getAct() == null){
                    //sendObject("get", GetMessage.getMe());
                    GetMessage getMessage = new GetMessage();
                    getMessage.setId("get");
                    getMessage.setTopic(presMessage.getSrc());
                    getMessage.setWhat("desc");
                    sendObject("get",getMessage);
                    GetMessage getMessage1 = new GetMessage();
                    getMessage1.setId("getSub");
                    getMessage1.setTopic(presMessage.getSrc());
                    getMessage1.setWhat("sub");
                    sendObject("get",getMessage1);
                }else if (!"read".equals(presMessage.getWhat())&&!"msg".equals(presMessage.getWhat()) && !"ua".equals(presMessage.getWhat()) && !"on".equals(presMessage.getWhat()) && !"del".equals(presMessage.getWhat()) ){
                    GetMessage getMessage4 = new GetMessage();
                    getMessage4.setId("UsersFromGroup");
                    getMessage4.setTopic(presMessage.getTopic());
                    getMessage4.setWhat("sub");
                    sendObject("get",getMessage4);
                    GetMessage getMessage5 = new GetMessage();
                    getMessage5.setId("UsersFromGroup");
                    getMessage5.setTopic("fnd");
                    getMessage5.setWhat("sub");
                    sendObject("get",getMessage5);
                }
                if ("acs".equals(presMessage.getWhat())&& presMessage.getDacs().getGiven().equals("N")){
                    Utils.asyncTask(() -> ChatDatabase.getInstance().groupMembersDao().deleteUser(presMessage.getTopic(),presMessage.getSrc()));
                }
                if ("del".equals(presMessage.getWhat())) {
                    if ("me".equals(presMessage.getTopic())&& presMessage.getDelSeqs()==null){
                        Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteByTopic(presMessage.getSrc()));
//                        SubMessage subTopic = new SubMessage();
//                        subTopic.setTopic(presMessage.getSrc());
//                        subTopic.setId("delSub");
//                        GetMessage getMessage = new GetMessage();
//                        getMessage.setWhat("data");
//                        getMessage.getData().setLimit(25);
//                        subTopic.setGet(getMessage);
//                        sendObject("sub",subTopic);
                    }else if (!"me".equals(presMessage.getTopic())&& presMessage.getDelSeqs()==null){
                        Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteByTopic(presMessage.getTopic()));
//                        SubMessage subTopic = new SubMessage();
//                        subTopic.setTopic(presMessage.getTopic());
//                        subTopic.setId("delSub");
//                        GetMessage getMessage = new GetMessage();
//                        getMessage.setWhat("data");
//                        subTopic.setGet(getMessage);
//                        sendObject("sub",subTopic);
                    }
                    if (presMessage.getDelSeqs() != null){
                        for (PresMessage.DelSeq d : presMessage.getDelSeqs()) {
                            ListenableFuture<Boolean> hasMessageWithSeq = ChatDatabase.getInstance().chatMessageDao().hasMessageWithSeq(d.getLow(), presMessage.getTopic());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                Futures.addCallback(hasMessageWithSeq,
                                        new FutureCallback<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean result) {
                                                if (result) {
                                                    Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteMassage(presMessage.getTopic(), d.getLow()));
                                                } else {
                                                    ListenableFuture<Replies> replies = ChatDatabase.getInstance().repliesDao().getReplies(d.getLow(),presMessage.getTopic());
                                                    Futures.addCallback(replies, new FutureCallback<Replies>() {
                                                        @Override
                                                        public void onSuccess(Replies result) {
                                                            String replies = gson.toJson(result);
                                                            Type repliesType = new TypeToken<Replies>() {}.getType();
                                                            Replies Replies1 = gson.fromJson(replies, repliesType);
                                                            //Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteMassage(presMessage.getTopic(),Replies1.getHead().getReply()));
                                                            sendObject("get", GetMessage.getLoads1(Replies1.getTopic(),Replies1.getHead().getReply()));
                                                            Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteMassage(presMessage.getTopic(), d.getLow()));
                                                        }
                                                        @Override
                                                        public void onFailure(Throwable t) {}
                                                    }, AppController.getInstance().getMainExecutor());
                                                }
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {}
                                        }, AppController.getInstance().getMainExecutor());
                            }
                        }
                    }
                }
                break;
            case "data":
                DataMessage dataMessage = new Gson().fromJson(object.get(key), DataMessage.class);
                if (dataMessage.getHead() != null && dataMessage.getHead().getReplace() != 0) {
                    sendObject("get", GetMessage.getLoads1(dataMessage.getTopic(),dataMessage.getHead().getReplace()));
                }
                if (currentTopic.equals(dataMessage.getTopic())) {
                    currentTopicList.add(dataMessage);
                    System.out.println(dataMessage);
                }
                if (dataMessage.getContent() != null && dataMessage.getContent().toString().contains("remove")){
                    Type contentType = new TypeToken<Content>() {}.getType();
                    Content content = gson.fromJson(dataMessage.getContent().toString(),contentType);
                   // Log.d("fromAndContent",content.getFrom());
                    //Log.d("fromAndContent",content.getContent());
                    Utils.asyncTask(() -> ChatDatabase.getInstance().reactionsDao().deleteFromReactions(content.getFrom(),content.getContent()));
                }

                if (dataMessage.getHead() != null  && dataMessage.getHead().getReply() != 0 ){
                    Reply reply = new Reply(dataMessage.getTopic(),dataMessage.getFrom(),dataMessage.getTs(),dataMessage.getSeq(),dataMessage.getHead(),dataMessage.getContent(), dataMessage.getReactions());
                    String replies = gson.toJson(reply);
                    Type replyType = new TypeToken< DataMessage.Replies>() {}.getType();
                    DataMessage.Replies r = gson.fromJson(replies,replyType);
                    chatRepliesViewModel.SaveReplies(r);
                    Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().updateReplies(replies,dataMessage.getHead().getReply(),dataMessage.getTopic()));
                }else if (dataMessage.getHead() != null&& dataMessage.getHead().getReaction() != null && dataMessage.getHead().getReaction() && ! dataMessage.getContent().toString().contains("remove")) {
                    Type contentType = new TypeToken<Content>() {}.getType();
                    Content content1 = gson.fromJson(dataMessage.getContent().toString(),contentType);
                    com.telebroad.teleconsole.db.models.Reactions reactions = new com.telebroad.teleconsole.db.models.Reactions(
                            new com.telebroad.teleconsole.db.models.Reactions(content1.getMsgseq()+"",content1.getContent(),content1.getFrom(),dataMessage.getTopic()));
                    Utils.asyncTask(() -> ChatDatabase.getInstance().reactionsDao().save(reactions));
                    String attachments = gson.toJson(dataMessage.getContent());
                    DataMessage.Content content = gson.fromJson(attachments, DataMessage.Content.class);
                    JsonObject jsonObject = new Gson().fromJson(attachments, JsonObject.class);
                    JsonPrimitive element = jsonObject.getAsJsonPrimitive("content");
                    JsonPrimitive elementFrom = jsonObject.getAsJsonPrimitive("from");
                    JsonPrimitive seq = jsonObject.getAsJsonPrimitive("msgseq");
                    mySeq = seq.getAsInt();
                    ListenableFuture<Boolean> isReactionOnReply = ChatDatabase.getInstance().chatMessageDao().hasMessageWithSeq(mySeq,dataMessage.getTopic());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Futures.addCallback(isReactionOnReply,
                                new FutureCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        if (result){
                                            ListenableFuture<String> future = ChatDatabase.getInstance().chatMessageDao().livedataReactionsForTopic(dataMessage.getTopic(),mySeq);
                                            Futures.addCallback(future,
                                                    new FutureCallback<String>() {
                                                        public void onSuccess(String result) {
                                                            if (result!= null) {
                                                                Reactions newReactions = new Reactions(element.getAsString(),elementFrom.getAsString());
                                                                String reactions =  gson.toJson(newReactions);
                                                                try {
                                                                    Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().updateReactions(reactions, content.getMsgseq(), dataMessage.getTopic()));
                                                                } catch (Exception g) {g.printStackTrace();}
                                                            }else {
                                                                newReply newReplies = new newReply(element.getAsString());
                                                                List<newReply> np = Collections.singletonList(newReplies);
                                                                String reactions =  gson.toJson(np);
                                                                Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().updateInitialReactions(reactions,dataMessage.getTopic(),mySeq));
                                                            }
                                                        }
                                                        public void onFailure(@NonNull Throwable thrown) {}
                                                    }, AppController.getInstance().getMainExecutor());
                                        }else {
                                            ListenableFuture<String> future = ChatDatabase.getInstance().repliesDao().livedataReactionsForTopic(dataMessage.getTopic(),mySeq);
                                            Futures.addCallback(future,
                                                    new FutureCallback<String>() {
                                                        public void onSuccess(String result) {
                                                            if (result!= null) {
                                                                Reactions newReactions = new Reactions(element.getAsString(),elementFrom.getAsString());
                                                                String reactions =  gson.toJson(newReactions);
                                                                try {
                                                                    Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().updateReactions(reactions, content.getMsgseq(), dataMessage.getTopic()));
                                                                } catch (Exception g) {
                                                                    g.printStackTrace();
                                                                }
                                                            }else {
                                                                newReply newReplies = new newReply(element.getAsString());
                                                                List<newReply> np = Collections.singletonList(newReplies);
                                                                String reactions =  gson.toJson(np);
                                                                Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().updateInitialReactions(reactions,dataMessage.getTopic(),mySeq));
                                                            }
                                                        }
                                                        public void onFailure(@NonNull Throwable thrown) {}
                                                    }, AppController.getInstance().getMainExecutor());
                                        }
                                    }
                                    @Override
                                    public void onFailure(Throwable t) {}
                                },AppController.getInstance().getMainExecutor());
                    }
                } else if (dataMessage.getContent() != null && !dataMessage.getContent().toString().contains("remove") &&
                       dataMessage.getHead() != null &&  dataMessage.getHead().getReplace() == 0){
                    Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().save(new ChatMessageDB(dataMessage)));
                    Utils.updateLiveData(newMessageLiveData, dataMessage.getSeq());
                    if (dataMessage.getReactions() != null) {
                        List<DataMessage.Reaction> reactionsList = dataMessage.getReactions();
                        for (DataMessage.Reaction r : reactionsList) {
                            com.telebroad.teleconsole.db.models.Reactions reactions = new com.telebroad.teleconsole.db.models.Reactions(
                                    new com.telebroad.teleconsole.db.models.Reactions(dataMessage.getSeq() + "", r.getContent(), r.getFrom(),dataMessage.getTopic()));
                            Utils.asyncTask(() -> ChatDatabase.getInstance().reactionsDao().save(reactions));
                        }
                    }
                    if (dataMessage.getReplies() != null){
                        DataMessage.Replies[] replies = dataMessage.getReplies();
                        for (DataMessage.Replies r:replies){
                            chatRepliesViewModel.SaveReplies(r);
                        }
                    }
                }

                break;
            case "info":
                InfoMessage infoMessage = new Gson().fromJson(object.get(key), InfoMessage.class);
                //if ("what".equals(infoMessage.getWhat())){
                //senderName = ChatDatabase.getInstance().channelDao().getNameByFromColumn(infoMessage.getFrom());
                //}
                if(Objects.equals(infoMessage.getWhat(), "read")){
                    ChatDatabase.getInstance().channelDao().setUnreadToZero(infoMessage.getTopic());
                }
            default:
                //Log.d("CWSW", "Unknown Key " + key);
        }
    }

    String currentTopic = "";
    public LiveDataList<DataMessage> currentTopicList = new LiveDataList<>();

    public void subscribe(String topic) {
        currentTopic = topic;
        currentTopicList.clear();
        sendObject("sub", SubMessage.getFromTopic(topic));
    }

    public String getSenderName() {
        return senderName;
    }
    class Reply{
        private String topic;
        private String from;
        private String ts;
        private int seq;
        private DataMessage.Head head;
        private Object content;
        private List< DataMessage.Reaction> reaction;

        public Reply(String topic, String from, String ts, int seq, DataMessage.Head head, Object content,List< DataMessage.Reaction> reaction) {
            this.topic = topic;
            this.from = from;
            this.ts = ts;
            this.seq = seq;
            this.head = head;
            this.content = content;
            this.reaction = reaction;
        }
    }
    class newReply{
        String content;
        public newReply(String content) {
            this.content = content;
        }
    }
    class Reactions{
        private String content;
        private String from;

        public Reactions(String content, String from) {
            this.content = content;
            this.from = from;
        }
    }
    public interface forwardCrr{
        void GetCtr(CtrlMessage ctrlMessage);
    }


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }


}