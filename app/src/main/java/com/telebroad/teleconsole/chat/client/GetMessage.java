package com.telebroad.teleconsole.chat.client;

import androidx.annotation.NonNull;

public class GetMessage {
    private String id;
    private String topic;
    private String what;
    private Desc desc = new Desc();
    private Sub sub = new Sub();
    private DataParams data = new DataParams();
    private DeleteParams del = new DeleteParams();

    public static class Desc{
        String ims;
    }

    public static class Sub {
        String ims;
        String user;
        String topic;
        int limit;
    }
    @NonNull
    public static GetMessage getMe(){
        GetMessage meMessage = new GetMessage();
        meMessage.id = "updateImageUrl";
        meMessage.topic = "me";
        meMessage.sub.topic = new Sub().topic = meMessage.getTopic();
        meMessage.setWhat("sub desc");
        return meMessage;
    }

    @NonNull
    public static GetMessage getLoads(String topic,int minSeq){
        GetMessage meMessage = new GetMessage();
        meMessage.data = new DataParams(minSeq,40);
        meMessage.id = "lazyLoading";
        meMessage.topic = topic;
        meMessage.setWhat("data");
        return meMessage;
    }
    @NonNull
    public static GetMessage getLoads1(String topic,int minSeq){
        GetMessage meMessage = new GetMessage();
        meMessage.data = new DataParams();
        meMessage.data.setLimit(1);
        meMessage.data.setSince(minSeq);
        meMessage.data.setBefore(minSeq+1);
        meMessage.id = "";
        meMessage.topic = topic;
        meMessage.setWhat("data");
        return meMessage;
    }
    public static GetMessage getFndToMe(){
        GetMessage getMessage = new GetMessage();
        getMessage.id = "fnd_to_me";
        getMessage.topic = "fnd";
        getMessage.what = "sub";
        return getMessage;
    }

    public static class DataParams {
        private int since;
        private int before;
        private int limit;

        public DataParams(int before, int limit) {
            this.before = before;
            this.limit = limit;
        }


        public DataParams() {
        }

        public int getSince() {
            return since;
        }

        public void setSince(int since) {
            this.since = since;
        }

        public int getBefore() {
            return before;
        }

        public void setBefore(int before) {
            this.before = before;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }

    public static class DeleteParams {
        String since;
        String before;
        int limit;
    }

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

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public Desc getDesc() {
        return desc;
    }

    public void setDesc(Desc desc) {
        this.desc = desc;
    }

    public Sub getSub() {
        return sub;
    }

    public void setSub(Sub sub) {
        this.sub = sub;
    }

    public DataParams getData() {
        return data;
    }

    public void setData(DataParams data) {
        this.data = data;
    }

    public DeleteParams getDel() {
        return del;
    }

    public void setDel(DeleteParams del) {
        this.del = del;
    }

    public static class getData{
        String since;
        int before;
        int limit;
    }
}

