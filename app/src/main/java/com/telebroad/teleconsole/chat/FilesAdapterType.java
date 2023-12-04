package com.telebroad.teleconsole.chat;


import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.db.models.Attachments;

public class FilesAdapterType {
    private String url;
    private String type;

    public static FilesAdapterType createInstance(Attachments attachments) {
        FilesAdapterType filesAdapterType = new FilesAdapterType();
        filesAdapterType.type = attachments.getType();
        filesAdapterType.url = attachments.getPath();
        return filesAdapterType;
    }
    public static FilesAdapterType createInstance(DataMessage.Replies.Attachments attachments) {
        FilesAdapterType filesAdapterType = new FilesAdapterType();
        filesAdapterType.type = attachments.getType();
        filesAdapterType.url = attachments.getPath();
        return filesAdapterType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
