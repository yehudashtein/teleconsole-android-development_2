package com.telebroad.teleconsole.chat.client;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.telebroad.teleconsole.db.models.ChannelDB;

import java.io.Serializable;

public class JoinTopicModel implements Parcelable {
    private String id;
    private String mode;
    private String fn;
    private int subCount;
    private String topic;
    private String user;
    private String photo;

    public JoinTopicModel(String id, String mode, String fn, int subCount, String topic) {
        this.id = id;
        this.mode = mode;
        this.fn = fn;
        this.subCount = subCount;
        this.topic = topic;
    }

    public JoinTopicModel(String id,String photo, String mode, String fn,String user) {
        this.id = id;
        this.photo = photo;
        this.mode = mode;
        this.fn = fn;
        this.user = user;
    }
    public JoinTopicModel(ChannelDB channelDB){
        this.id = "shareList";
        this.photo = channelDB.getImageUrl();
        this.mode = channelDB.getAcsMode();
        this.fn = channelDB.getName();
        this.user = channelDB.getTopic();
    }
    public static JoinTopicModel createInstance(ChannelDB channelDB){
        return new JoinTopicModel(channelDB);
    }
    public JoinTopicModel(String id,String photo, String mode, String fn,String user,String topic) {
        this.id = id;
        this.photo = photo;
        this.mode = mode;
        this.fn = fn;
        this.user = user;
        this.topic = topic;
    }

    protected JoinTopicModel(Parcel in) {
        mode = in.readString();
        fn = in.readString();
        subCount = in.readInt();
        topic = in.readString();
    }

    public static final Creator<JoinTopicModel> CREATOR = new Creator<JoinTopicModel>() {
        @Override
        public JoinTopicModel createFromParcel(Parcel in) {
            return new JoinTopicModel(in);
        }

        @Override
        public JoinTopicModel[] newArray(int size) {
            return new JoinTopicModel[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public int getSubCount() {
        return subCount;
    }

    public void setSubCount(int subCount) {
        this.subCount = subCount;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mode);
        dest.writeString(fn);
        dest.writeInt(subCount);
        dest.writeString(topic);
    }
}
