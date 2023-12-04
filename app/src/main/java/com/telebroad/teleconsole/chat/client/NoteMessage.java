package com.telebroad.teleconsole.chat.client;

public class NoteMessage {
    private int seq;
    private String topic;
    private String what;

    public NoteMessage( String topic, String what,int seq) {
        this.topic = topic;
        this.what = what;
        this.seq = seq;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
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
}
