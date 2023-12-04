package com.telebroad.teleconsole.db.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.telebroad.teleconsole.chat.models.ChatMessage;
import com.telebroad.teleconsole.chat.server.DataMessage;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

@Entity(tableName = "messages")
public class ChatMessageDB extends ChatMessage implements Serializable {

    public String messageDate;
    @PrimaryKey
    @NonNull
    private String primaryKey = getTopic() + getSeq();

    public ChatMessageDB(){
        super();
    }
    public ChatMessageDB(DataMessage message) {
        super(message);
        this.messageDate = parseTimestamp(message.getTs());
        if(message.getHead() != null){
            this.setForwarded(message.getHead().isForwarded());
        }

    }

    private String parseTimestamp(String ts) {
        return ts.substring(0,ts.length()-1);
    }

    public String getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(String messageDate) {
        this.messageDate = messageDate;
    }

    @NonNull
    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(@NonNull String primaryKey) {
        this.primaryKey = primaryKey;
    }
}
