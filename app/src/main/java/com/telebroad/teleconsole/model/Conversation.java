package com.telebroad.teleconsole.model;

import com.google.gson.annotations.SerializedName;

public class Conversation extends Message {

    @SerializedName("new")
    private int isNew;
    private String sender;
    private String receiver;
    private String msgdata;
    private String sent_by;
    private String read_by;
    private String seen;

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsgdata() {
        return msgdata;
    }

    public void setMsgdata(String msgdata) {
        this.msgdata = msgdata;
    }

    public String getSent_by() {
        return sent_by;
    }

    public void setSent_by(String sent_by) {
        this.sent_by = sent_by;
    }

    public String getRead_by() {
        return read_by;
    }

    public void setRead_by(String read_by) {
        this.read_by = read_by;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }


    @Override
    public MessageType getMessageType() {
        return MessageType.SMS;
    }
}
