package com.telebroad.teleconsole.db.models;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.chat.models.Channel;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.chat.viewModels.ChatViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@Entity(tableName = "Replies")
public class Replies {
    @PrimaryKey
    @NonNull
    private String id;
    private String content;
    public String from;
    private int seq;
    private String topic;
    private String ts;
    @TypeConverters({ChatDatabase.ReactionConverter2.class})
    private DataMessage.Replies.Reaction[] reaction;
    @TypeConverters({ChatDatabase.RepliesHeadConverter.class})
    public Head head;
    private String senderName;
    private String imageURL;


   public class Head{
        private String mime;
        private int reply;
        private DataMessage.Replies.Attachments[] attachments;


        public String getMime() {
            return mime;
        }

        public void setMime(String mime) {
            this.mime = mime;
        }

        public int getReply() {
            return reply;
        }

        public void setReply(int reply) {
            this.reply = reply;
        }

        public DataMessage.Replies.Attachments[] getAttachments() {
            return attachments;
        }

        public void setAttachments(DataMessage.Replies.Attachments[] attachments) {
            this.attachments = attachments;
        }

        public Head(String mime, int reply, DataMessage.Replies.Attachments[] attachments) {
            this.mime = mime;
            this.reply = reply;
            this.attachments = attachments;
        }
    }

    public Replies() {
    }

    public Replies(DataMessage.Replies replies) {
            this.content = replies.getContent();
            this.from = replies.getFrom();
            this.seq = replies.getSeq();
            this.topic = replies.getTopic();
            this.head = new Head(replies.getHead().getMime(),replies.getHead().getReply(),replies.getHead().getAttachments());
            this.ts = replies.getTs();
            this.reaction = replies.getReaction();
            this.id = replies.getSeq() +replies.getTopic();
        Channel senderChannel = ChatViewModel.getInstance().getChannelsByTopic().get(replies.getFrom());
        if(senderChannel != null){
            senderName = senderChannel.getName();
            imageURL = senderChannel.getImageUrl();
        }
    }

    public String getTs() {
        return ts;
    }
    public String getSignature(){
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String current = df.format(getDate());
        current += getSenderName();
        return  current;
    }
    public Date getDate(){
        Instant instant = Instant.parse(getTs());
        Date date = Date.from(instant);
        return date;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getContent() {
        return content;
    }

    public DataMessage.Replies.Reaction[] getReaction() {
        return reaction;
    }

    public void setReaction(DataMessage.Replies.Reaction[] reaction) {
        this.reaction = reaction;
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

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Replies myClass = (Replies) o;
        return Objects.equals(from, myClass.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from);
    }
}
