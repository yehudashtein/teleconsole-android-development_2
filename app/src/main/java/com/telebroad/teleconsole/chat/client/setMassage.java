package com.telebroad.teleconsole.chat.client;

import java.util.Map;

public class setMassage {
    String id;
    String topic;
    Map<String,?> desc;

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

    public Map<String, ?> getDesc() {
        return desc;
    }

    public void setDesc(Map<String, ?> desc) {
        this.desc = desc;
    }
}
