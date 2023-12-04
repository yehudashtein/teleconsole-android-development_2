package com.telebroad.teleconsole.chat.client;

import com.google.gson.annotations.SerializedName;
import com.telebroad.teleconsole.chat.Cred;

import java.util.List;
import java.util.Map;

public class SetMessage {
    private String id;
    private String topic;
    private Desc desc;
    private Sub sub;
    private List<String> tags;
    private Cred cred;

    public SetMessage(Desc desc) {
        this.desc = desc;
    }

    public SetMessage() {
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Cred getCred() {
        return cred;
    }

    public void setCred(Cred cred) {
        this.cred = cred;
    }

    public static class Desc {


        public Map<String, ?> getDefacs() {
            return defacs;
        }

        public void setDefacs(Map<String, ?> defacs) {
            this.defacs = defacs;
        }

        public Map<String, ?> getTrusted() {
            return trusted;
        }

        public void setTrusted(Map<String, ?> trusted) {
            this.trusted = trusted;
        }

        public Map<String, ?> getPublicInfo() {
            return publicInfo;
        }

        public void setPublicInfo(Map<String, ?> publicInfo) {
            this.publicInfo = publicInfo;
        }

        public Map<String, ?> getPrivateInfo() {
            return privateInfo;
        }

        public void setPrivateInfo(Map<String, ?> privateInfo) {
            this.privateInfo = privateInfo;
        }

        private Map<String, ?> defacs;
        private Map<String, ?> trusted;
        @SerializedName("public")
        private Map<String, ?> publicInfo;
        @SerializedName("private")
        private Map<String, ?> privateInfo;
        //private Map<String,?> desc;

        public Desc() {
        }

        public Desc(Map<String, ?> defacs, Map<String, ?> desc) {
            this.defacs = defacs;
            this.publicInfo = desc;
        }

//        public Desc(Map<String, ?> desc) {
//            this.desc = desc;
//        }
//
//
//        public Map<String, ?> getDesc() {
//            return desc;
//        }
//
//        public void setDesc(Map<String, ?> desc) {
//            this.desc = desc;
//        }
    }

    public static class Sub {
        private String user;
        private String mode;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

}
