package com.telebroad.teleconsole.db.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.telebroad.teleconsole.chat.server.MetaMessage;

import java.util.List;
import java.util.Objects;

@Entity(tableName = "GroupMembers")
public class GroupMembers {
    @PrimaryKey()
    @NonNull
    private String id;
    private String topic;
    private String fn;
    private String nickname;
    private String user;

    public GroupMembers() {
    }

public GroupMembers(MetaMessage.Sub metaMessage,String topic) {
            this.id = topic+metaMessage.getUser();
            this.topic = topic;
            this.fn = metaMessage.getPublicParams().get("fn") != null ? metaMessage.getPublicParams().get("fn").toString()  :"";
            this.nickname = metaMessage.getPublicParams().get("nickname") !=null ? metaMessage.getPublicParams().get("nickname").toString():"";
            this.user = metaMessage.getUser();
    }

    public GroupMembers(@NonNull String id, String topic, String fn, String nickname, String user) {
        this.id = topic+user;
        this.topic = topic;
        this.fn = fn;
        this.nickname = nickname;
        this.user = user;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
