package com.telebroad.teleconsole.model;

import androidx.room.Entity;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "calls", primaryKeys = "id")
public class CallHistory extends Message{

    public CallHistory(){

    }
    private String phone;
    private String callid;

    @TypeConverters(StatusConverter.class)
    private CallStatus status;

    private String type;
    private String stype;
    private String snumber;
    private String sname;
    private String dtype;
    private String dnumber;
    private String duration;
    private String talktime;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCallid() {
        return callid;
    }

    public void setCallid(String callid) {
        this.callid = callid;
    }

    public CallStatus getStatus() {
        return status;
    }

    public void setStatus(CallStatus status) {
        this.status = status;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStype() {
        return stype;
    }

    public void setStype(String stype) {
        this.stype = stype;
    }

    public String getSnumber() {
        return snumber;
    }

    public void setSnumber(String snumber) {
        this.snumber = snumber;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public String getDnumber() {
        return dnumber;
    }

    public void setDnumber(String dnumber) {
        this.dnumber = dnumber;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTalktime() {
        return talktime;
    }

    public void setTalktime(String talktime) {
        this.talktime = talktime;
    }

    public String getType() {
        return type;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CALL_HISTORY;
    }

    public static class StatusConverter{
        @TypeConverter
        public int fromStatus(CallStatus status){
            if (status == null){
                return -1;
            }
            return status.ordinal();
        }
        @TypeConverter
        public CallStatus toStatus(int ordinal){
            if (ordinal < 0 ){
                return null;
            }
            return CallStatus.values()[ordinal];
        }
    }
    public enum CallStatus{
        @SerializedName(value = "cancel", alternate = {"noanswer", "busy"}) MISSED, @SerializedName("voicemail") VOICEMAIL, @SerializedName("answer") ANSWERED, @SerializedName(value = "chanunav", alternate = "error") WRONG_NUMBER;
    }

    @Override
    public String toString() {
        return "CallHistory{" +
                "phone=" + phone +
                "\n callid=" + callid +
                "\n direction=" + getDirection() +
                "\n id=" + getId() +
                "\n time=" + getTimeStamp() +
                "\n status=" + status +
                "\n type=" + type +
                "\n stype=" + stype +
                "\n snumber=" + snumber +
                "\n sname=" + sname +
                "\n dtype=" + dtype +
                "\n dnumber=" + dnumber +
                "\n duration=" + duration +
                "\n talktime=" + talktime +
                '}';
    }
}
