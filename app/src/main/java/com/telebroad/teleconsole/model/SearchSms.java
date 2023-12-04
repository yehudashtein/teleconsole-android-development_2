package com.telebroad.teleconsole.model;

public interface SearchSms {
    long getId();
    int getNew_field();
    String getDirection();
    long getSender();
    long getReceiver();
    long getTime();
    String getMsgdata();
    String[] getMedia();
    String getRead_by();
    int getSent_by();
    int getSeen();
    String getDlr_status();
    String getDlr_error();
    long getDlr_time();
}
