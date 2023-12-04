package com.telebroad.teleconsole.chat.client;

public class LeaveMessage {
    private String id;
    private String topic;
    private boolean unsub;

    public LeaveMessage() {
    }



    public LeaveMessage(String id, String topic) {
        this.id = id;
        this.topic = topic;
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

    public boolean isUnsub() {
        return unsub;
    }

    public void setUnsub(boolean unsub) {
        this.unsub = unsub;
    }
}
