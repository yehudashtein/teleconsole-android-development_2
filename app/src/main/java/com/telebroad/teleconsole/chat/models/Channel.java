package com.telebroad.teleconsole.chat.models;

import android.net.Uri;

import androidx.room.Ignore;

import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.chat.server.MetaMessage;
import com.telebroad.teleconsole.chat.viewModels.ChatViewModel;

import java.util.Map;
import java.util.Objects;

public class Channel {

    private String name;
    private String imageUrl;
    private int unread;
    private boolean online;
    private String topic;
    private Boolean group;
    private String subbedTo;
    private String acsMode;
    private String description;
    private String when;
    @Ignore
    private boolean isPrivate;
    @Ignore
    private boolean isOwner;

    public Channel(){}
    public Channel(MetaMessage metaMessage){
        try {
            name = metaMessage.getDesc().getPublicParams().getFn() != null ?  metaMessage.getDesc().getPublicParams().getFn() : "";
            description =  metaMessage.getDesc().getPublicParams().getDescription() != null ? metaMessage.getDesc().getPublicParams().getDescription()  : "";
            imageUrl = metaMessage.getDesc().getPublicParams().getPhoto().get("ref") != null ?metaMessage.getDesc().getPublicParams().getPhoto().get("ref")  : "";
        }catch (ClassCastException | NullPointerException ignored){}
        //Object acsModeObj = sub.getAcs().get("mode");
        acsMode = metaMessage.getDesc().getAcs().get("mode") == null ?  metaMessage.getDesc().getAcs().get("mode").toString() :"";
       // online = sub.getOnline() != null && !sub.getOnline().isEmpty() && !sub.getOnline().equalsIgnoreCase("null");
        topic =metaMessage.getTopic() != null ?  metaMessage.getTopic() : "";
        group = getTopic().startsWith("grp");
        //this.unread = (sub.getRecv() == null ? 0 : sub.getRecv()) - (sub.getRead() == null ? 0 : sub.getRead() ) ;
        this.subbedTo = metaMessage.getTopic();
        //int unread = this.unread;
    }
    public Channel(MetaMessage.Sub sub, String subbedTo){
         try {
             name = sub.getPublicParams().get("fn") != null ? sub.getPublicParams().get("fn").toString() : "";
             description = sub.getPublicParams().get("description") != null ? sub.getPublicParams().get("description").toString() : "";
             imageUrl = ((Map<String, ?>) sub.getPublicParams().get("photo")).get("ref").toString();
        }catch (ClassCastException | NullPointerException ignored){}
         Object acsModeObj = sub.getAcs().get("mode");
         acsMode = acsModeObj == null ? null : acsModeObj.toString();
        this.when = sub.getSeen() != null && sub.getSeen().get("when") != null?sub.getSeen().get("when").toString():"";
        online = sub.getOnline() != null && !sub.getOnline().isEmpty() && !sub.getOnline().equalsIgnoreCase("null");
        topic = sub.getTopic() == null ? sub.getUser() == null ? "": sub.getUser() : sub.getTopic();
        group = getTopic().startsWith("grp");
        this.unread = (sub.getSeq() ==0 ? 0 : sub.getSeq()) - (sub.getRead() == null ? 0 : sub.getRead() ) ;
        //this.unread =  sub.getRecv() - sub.getRead();
        this.subbedTo = subbedTo;
    }

    public Channel(ChannelDB channelDB){
        this.name = channelDB.getName();
        this.imageUrl = channelDB.getImageUrl();
        this.unread = channelDB.getUnread();
        this.online = channelDB.isOnline();
        this.topic = channelDB.getTopic();
        this.group = channelDB.isGroup();
        this.subbedTo = channelDB.getSubbedTo();
        this.unread = channelDB.getUnread();
    }


    public boolean isDirect() {
        return !isGroup();
    }
    public Boolean isGroup(){
        return group;
    }


    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    @Ignore
    private Uri imageUri;
    @Ignore
    private boolean isUriNull = false;
    public Uri getImageUri(){
        if (isUriNull || imageUrl == null){
            return null;
        }
        if (imageUri == null){
            imageUri = Uri.parse(getImageUrl());
            if (imageUri == null) {
                isUriNull = true;
            }
        }
        return imageUri;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public int getUnread() {
        return unread;
    }

    public boolean isOnline() {
        return online;
    }

    public String getTopic() {
        return topic;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public String getSubbedTo() {
        return subbedTo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSubbedTo(String subbedTo) {
        this.subbedTo = subbedTo;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", unread=" + unread +
                ", online=" + online +
                ", topic='" + topic + '\'' +
                ", group=" + group +
                ", subbedTo='" + subbedTo + '\'' +
                '}';
    }
    public int getColorByTopic(String topic){
        int i = 0;
        return i;
    }

    public String getAcsMode() {
        return acsMode;
    }

    public void setAcsMode(String acsMode) {
        this.acsMode = acsMode;
    }
    public boolean isNotPrivate(){
        ChannelDB fndChannel =  ChatViewModel.getInstance().getFNDGroupChannelsToMe.get(getTopic());
        String fndAcsMode = fndChannel == null ? "" : fndChannel.getAcsMode();
        if (fndAcsMode.contains("J")){
            isPrivate = true;
        }else {
            isPrivate = false;
        }
        return isPrivate;
    }
    public boolean isOwner(){
        ChannelDB fndChannel =  ChatViewModel.getInstance().getFNDGroupChannelsToMe.get(getTopic());
        String fndAcsMode = fndChannel == null ? "" : fndChannel.getAcsMode();
        if (fndAcsMode.contains("O")){
            isOwner = true;
        }else {
            isOwner = false;
        }
        return isOwner;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelDB channel = (ChannelDB) o;
        return Objects.equals(name, channel.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
