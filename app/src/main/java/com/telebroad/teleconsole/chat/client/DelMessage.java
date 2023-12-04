package com.telebroad.teleconsole.chat.client;

import java.util.List;
public class DelMessage {
    private String id;
    private String topic;
    private String what;
    private boolean hard;
    private List<Delseq> delseq;
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public static class Delseq{
        private int low;

        public Delseq(int low) {
            this.low = low;
        }

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }
    }

    public DelMessage(String id, String topic, String what, boolean hard, List<Delseq> delseq) {
        this.id = id;
        this.topic = topic;
        this.what = what;
        this.hard = hard;
        this.delseq = delseq;
    }

    public DelMessage() {
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

    public boolean isHard() {
        return hard;
    }

    public void setHard(boolean hard) {
        this.hard = hard;
    }

    public List<Delseq> getDelseq() {
        return delseq;
    }

    public void setDelseq(List<Delseq> delseq) {
        this.delseq = delseq;
    }
}
