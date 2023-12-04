package com.telebroad.teleconsole.model;

import com.google.gson.annotations.SerializedName;

public class SearchSmsMessages implements SearchSms{
    private long id;
    @SerializedName("new")
    private int new_field;
    private String direction;
    private long sender;
    private long receiver;
    private long time;
    private String msgdata;
    private String[] media;
    private String read_by;
    private int sent_by;
    private int seen;
    private String dlr_status;
    private String dlr_error;
    private long dlr_time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNew_field() {
        return new_field;
    }

    public void setNew_field(int new_field) {
        this.new_field = new_field;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public long getSender() {
        return sender;
    }

    public void setSender(long sender) {
        this.sender = sender;
    }

    public long getReceiver() {
        return receiver;
    }

    public void setReceiver(long receiver) {
        this.receiver = receiver;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMsgdata() {
        return msgdata;
    }

    public void setMsgdata(String msgdata) {
        this.msgdata = msgdata;
    }

    public String[] getMedia() {
        return media;
    }

    public void setMedia(String[] media) {
        this.media = media;
    }

    public String getRead_by() {
        return read_by;
    }

    public void setRead_by(String read_by) {
        this.read_by = read_by;
    }

    public int getSent_by() {
        return sent_by;
    }

    public void setSent_by(int sent_by) {
        this.sent_by = sent_by;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public String getDlr_status() {
        return dlr_status;
    }

    public void setDlr_status(String dlr_status) {
        this.dlr_status = dlr_status;
    }

    public String getDlr_error() {
        return dlr_error;
    }

    public void setDlr_error(String dlr_error) {
        this.dlr_error = dlr_error;
    }

    public long getDlr_time() {
        return dlr_time;
    }

    public void setDlr_time(long dlr_time) {
        this.dlr_time = dlr_time;
    }
}
