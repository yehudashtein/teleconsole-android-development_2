package com.telebroad.teleconsole.db.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.telebroad.teleconsole.chat.server.DataMessage;

@Entity(tableName = "attachments")
public class Attachments {
    @PrimaryKey
    @NonNull
    private String primaryKey= getName()+"-"+getType();
    private double height;
    private String name;
    private String path;
    private double size;
    private String type;

    public Attachments() {
    }

    public Attachments(DataMessage.Head.ServerAttachments attachments) {
        this(attachments.getName() +"-" + attachments.getType(),
                attachments.getHeight(), attachments.getName(), attachments.getPath(),
                attachments.getSize(), attachments.getType());
    }

    public Attachments(String primaryKey, double height, String name, String path, double size, String type) {
        this.primaryKey = primaryKey;
        this.height = height;
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
