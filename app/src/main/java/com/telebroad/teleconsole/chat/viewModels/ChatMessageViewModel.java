package com.telebroad.teleconsole.chat.viewModels;

import android.support.v4.media.session.PlaybackStateCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.db.models.ChatMessageDB;
import com.telebroad.teleconsole.chat.models.Channel;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.helpers.SettingsHelper;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ChatMessageViewModel implements Serializable {
    private Object content;
    private String topic;
    private transient PlaybackStateCompat pbsc;
    private String type;
    private Object attachments;
    private Object replies;
    private List<DataMessage.Reaction> reactions;
    private String senderName = "Unknown";
    private String ts;
    private int seq;
    private ChatMessageDB item;
    private String imageURL;
    private String text;
    private boolean forwarded;
    private String from;
    private Object edits;


    public Object getEdits() {
        return edits;
    }

    public void setEdits(Object edits) {
        this.edits = edits;
    }

    public String getImageURL() {
        return imageURL;
    }

    public ChatMessageViewModel(ChatMessageDB chatMessageDB) {
        if (chatMessageDB != null) {
            item = chatMessageDB;
            this.attachments = chatMessageDB.getAttachments();
            this.seq = chatMessageDB.getSeq();
            this.replies = chatMessageDB.getReply();
            this.text = chatMessageDB.getMessage();
            this.content = chatMessageDB.getContent();
            this.type = chatMessageDB.getMime();
            this.reactions = chatMessageDB.getReactions();
            this.ts = chatMessageDB.getTs();
            this.topic = chatMessageDB.getTopic();
            this.forwarded = chatMessageDB.isForwarded();
            this.from = chatMessageDB.getFrom();
            this.edits = chatMessageDB.getEditText();
            Map<String, ChannelDB> nn = ChatViewModel.getInstance().getChannelsByTopic();
            Channel senderChannel = ChatViewModel.getInstance().getChannelsByTopic().get(chatMessageDB.getFrom());
            if (senderChannel != null) {
                senderName = senderChannel.getName();
                imageURL = senderChannel.getImageUrl();
            }
        }
    }

    public static ChatMessageViewModel createInstance(ChatMessageDB db) {
        if ("text/teamsevent+json".equals(db.getMime())) {
            return new TeamsEventMessageViewModel(db);
        }
        return new ChatMessageViewModel(db);
    }

    public String getSignature() {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String current = df.format(getDate());
        current += getSenderName();
        return current;
    }

    public CharSequence getContentForTextView() {
        CharSequence content = "";
        Gson gson = new Gson();
        String edits = gson.toJson(getEdits());
        if (!edits.equals("[]")) {
            Type editsType = new TypeToken<List<DataMessage.Edits>>() {
            }.getType();
            List<DataMessage.Edits> editsList = gson.fromJson(edits, editsType);
            if (editsList != null) {
                LinkedList<DataMessage.Edits> editLinkedList = new LinkedList<>(editsList);
                if (editLinkedList.getFirst().getContent() != null && !editLinkedList.getFirst().getContent().isEmpty()) {
                    content = SettingsHelper.reformatHTML(editLinkedList.getFirst().getContent());
                }
            }
        } else {
            content = SettingsHelper.reformatHTML(getText());
        }
        return content;
    }
    public CharSequence getRepliesForTxtReply(){
        CharSequence reply ="";
        LinkedList<Replies> repliesLinkedList = gerReplyLinkedList();
        if (repliesLinkedList != null){
            return  "Last reply: "+SettingsHelper.reformatHTML(repliesLinkedList.getFirst().getContent());
        }
        return "";
    }
    public String getTimeForTxtTimeChatM(){
         SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, h:mm a", Locale.getDefault());
         return sdf.format(getDate());
    }

    public String getClockForTxtChatTime(){
         SimpleDateFormat sdft = new SimpleDateFormat(" h:mm a", Locale.getDefault());
        return sdft.format(getDate());
    }

    public String getTxtNumberReplies(){
        if (gerReplyLinkedList().size()==1){
            return gerReplyLinkedList().size() + " Reply";
        }else if (gerReplyLinkedList().size()>1) {
            return gerReplyLinkedList().size() + " Replies";
        }
        return null;
    }

    public LinkedList<Replies> gerReplyLinkedList(){
        Gson gson = new Gson();
        String replies = gson.toJson(getReplies());
        Type repliesType = new TypeToken<List<Replies>>() {}.getType();
        List<Replies> Replies = gson.fromJson(replies, repliesType);
        Replies.removeIf(Objects::isNull);
        if (Replies.size()>0){
            return  new LinkedList<>(Replies);
        }
        return null;
    }
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public String getTopic() {
        return topic;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public PlaybackStateCompat getPbsc() {
        return pbsc;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public void setPbsc(PlaybackStateCompat pbsc) {
        this.pbsc = pbsc;
    }

    public Object getReplies() {
        return replies;
    }

    public void setReplies(Object replies) {
        this.replies = replies;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public ChatMessageDB getItem() {
        return item;
    }

    public void setItem(ChatMessageDB item) {
        this.item = item;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "ChatMessageViewModel{" +
                "text='" + text + '\'' +
                '}';
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    // public String getImageURL() {
    //return imageURL;
    //}

    public Date getDate() {
        Instant instant = Instant.parse(item.getTs());
        Date date = Date.from(instant);
        return date;
    }


    public void setType(String type) {
        this.type = type;
    }

    public List<DataMessage.Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<DataMessage.Reaction> reactions) {
        this.reactions = reactions;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Object getAttachments() {
        return attachments;
    }

    public void setAttachments(Object attachments) {
        this.attachments = attachments;
    }
}
