package com.telebroad.teleconsole.model;

import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public abstract class Message implements Comparable<Message> {
    @NonNull
    private String id;
    @SerializedName("time")
    private long timestamp;
    private boolean needsNotification = false;
    @SerializedName(value = "dir", alternate = "direction")
    @TypeConverters(DirectionConverter.class)
    private Direction direction;
    @Ignore
    public abstract MessageType getMessageType();
    public long getTimeStamp(){
        return timestamp;
    }
    @Override
    public int compareTo(@NonNull Message o) {
        return (int)(o.getTimeStamp() - getTimeStamp());
    }
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isNeedsNotification() {
        return needsNotification;
    }

    public void setNeedsNotification(boolean needs){
        this.needsNotification = needs;
    }

    public enum Direction {
        @SerializedName(value = "in", alternate = {"INBOX", "INCOMING"}) IN, @SerializedName(value = "out", alternate = {"SENT", "OUTGOING"}) OUT, @SerializedName(value = "Old") OLD
    }
    public static class DirectionConverter {

        @TypeConverter
        public String fromStatus(Direction direction){
            if (direction == null){
                return "";
        }
            return direction.name();
        }
        @TypeConverter
        public Direction toStatus(String name){
            try {
                return Direction.valueOf(name);
            }catch (IllegalArgumentException iae){
                return null;
            }
        }
    }
    public enum MessageType{
        CALL_HISTORY, FAX, SMS, VOICEMAIL
    }

}
