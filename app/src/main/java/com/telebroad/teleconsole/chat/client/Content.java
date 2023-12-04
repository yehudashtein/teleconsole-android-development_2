package com.telebroad.teleconsole.chat.client;

public class Content {
    private String action;
    private String content;
    private String from;
    private int msgseq;
    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int getMsgseq() {
        return msgseq;
    }

    public void setMsgseq(int msgseq) {
        this.msgseq = msgseq;
    }
}
