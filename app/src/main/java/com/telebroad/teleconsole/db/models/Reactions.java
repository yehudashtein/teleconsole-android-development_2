package com.telebroad.teleconsole.db.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Reactions")
public class Reactions {
    @PrimaryKey
    @NonNull
    private String id;
    private String content;
    private String from;
    private String topic;

//    public Reactions(int id, String content, String from) {
//        this.id = id;
//        this.content = content;
//        this.from = from;
//    }


    public Reactions() {
    }

    public Reactions(@NonNull String id, String content, String from,String topic) {
        this.id = id;
        this.content = content;
        this.from = from;
        this.topic = topic;
    }

    public Reactions(Reactions reactions) {
        int i =+ 1;
        this.id = reactions.getId() + reactions.getContent()+i;
        this.content = reactions.getContent();
        this.from = reactions.getFrom();
        this.topic = reactions.getTopic();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
