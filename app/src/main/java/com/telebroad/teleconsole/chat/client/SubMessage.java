package com.telebroad.teleconsole.chat.client;

import static com.telebroad.teleconsole.chat.ChatWebSocket.FND_ID;

import androidx.annotation.NonNull;

import java.util.Set;

public class SubMessage {
    private String id;
    private String topic;
    private GetMessage get;
    private SetMessage set;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public GetMessage getGet() {
        return get;
    }

    public void setGet(GetMessage get) {
        this.get = get;
    }

    public SetMessage getSet() {
        return set;
    }

    public void setSet(SetMessage set) {
        this.set = set;
    }

    public SubMessage() {
    }

    public SubMessage(String id, String topic) {
        this.id = id;
        this.topic = topic;
    }

    @NonNull
    public static SubMessage getMe(){
        SubMessage meMessage = new SubMessage();
        meMessage.id = "0";
        meMessage.topic = "me";
        meMessage.get = new GetMessage();
        meMessage.get.setWhat("sub desc tags cred");
        return meMessage;
    }

    @NonNull
    public static SubMessage getFnd(){
        SubMessage fndMessage = new SubMessage();
        fndMessage.id = FND_ID;
        fndMessage.topic = "fnd";
        fndMessage.get = new GetMessage();
        fndMessage.get.setWhat("sub");
        return fndMessage;
    }


    public static SubMessage getFromTopic(String topic){
        SubMessage subTopic = new SubMessage();
        subTopic.topic = topic;
        subTopic.id = "SubToTopic";
        subTopic.get = new GetMessage();
        subTopic.get.setWhat("data sub desc");
        subTopic.get.getData().setLimit(25);
        return subTopic;
    }
    public static SubMessage getLoads(String topic,int minSeq){
        SubMessage subMessage = new SubMessage();
        subMessage.id = "lazyLoading";
        subMessage.topic = topic;
        subMessage.get = new GetMessage();
        subMessage.get.getData().setLimit(20);
        subMessage.get.getData().setBefore(minSeq);
        //GetMessage.DataParams dataParams = new GetMessage.DataParams(minSeq,20);
        //subMessage.get.setData(dataParams);
        subMessage.get.setWhat("data");
        return subMessage;
    }


}
