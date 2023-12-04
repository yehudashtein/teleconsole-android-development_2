package com.telebroad.teleconsole.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "fax", primaryKeys = "id")
public class Fax extends Message {

    private String called;
    private String caller;
    private String callerid;
    private String mailbox;
    private String name;
    private int pages;
    private String read_by;
    private String seen;
    private String dlr_status;
    private String dlr_error;

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

    private long dlr_time;

    public String getCalled() {
        return called;
    }

    public void setCalled(String called) {
        this.called = called;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCallerid() {
        return callerid;
    }

    public void setCallerid(String callerid) {
        this.callerid = callerid;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
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

    @NonNull
    @Override
    public String toString() {
        return "FaxViewModel{" +
                "called=" + called +
                "\n caller=" + caller +
                "\n callerid=" + callerid +
                "\n mailbox=" + mailbox +
                "\n name=" + name +
                "\n pages=" + pages +
                "\n read_by=" + read_by +
                "\n seen=" + seen +
                '}';
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FAX;
    }
}
