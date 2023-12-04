package com.telebroad.teleconsole.chat.models;

import androidx.annotation.NonNull;

import com.telebroad.teleconsole.db.models.Attachments;
import com.telebroad.teleconsole.chat.server.DataMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChatMessage {
    private String message;
    private String topic;
    private String from;
    private int seq;
    private List<DataMessage.Reaction> reactions;
    private Object content;
    private String mime;
    private Object reply = new ArrayList<>();
    private Object attachments = new ArrayList<>();
    private String ts;
    private boolean forwarded;
    private Object editText = new ArrayList<>();
//    private Map<String, ?> head;


    public ChatMessage(){

    }
    public ChatMessage(@NonNull DataMessage message){
        this.message = String.valueOf(message.getContent());
        this.topic = message.getTopic();
        this.seq = message.getSeq();
        this.from = message.getFrom();
        this.reactions = message.getReactions();
        this.content = message.getContent();
        this.editText = message.getEdits() != null? message.getEdits():new ArrayList<>();
        android.util.Log.d("CWSM", "head is " + message.getHead());
        if(message.getHead() != null){
            this.mime = String.valueOf(message.getHead().getMime());
           // String reply = String.valueOf(message.getHead().getReply());
//            if (!reply.isEmpty() && !reply.equals("null")){
//                this.reply = (int) Math.round(Double.parseDouble(reply));
//            }
            this.attachments = message.getHead().getAttachments().stream().map(Attachments::new).collect(Collectors.toList());
        }
        if (message.getReplies() != null){
            this.reply = Arrays.stream(message.getReplies()).map(Replies::new).collect(Collectors.toList());
        }

        this.ts = message.getTs();

//        if(attachments instanceof List){
//            this.attachments = (List<Attachments>) attachments;
//        }
//        this.head = message.getHead();
    }

    public Object getEditText() {
        return editText;
    }

    public void setEditText(Object editText) {
        this.editText = editText;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<DataMessage.Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<DataMessage.Reaction> reactions) {
        this.reactions = reactions;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public Object getReply() {
        return reply;
    }

    public void setReply(Object reply) {
        this.reply = reply;
    }

//
    public Object getAttachments() {
        return attachments;
    }

    public void setAttachments(Object attachments) {
        this.attachments = attachments;
    }
    public class Replies{
        private String topic;
        private String from;
        private String ts;
        private int seq;
        private DataMessage.Replies.Head head;
        private DataMessage.Replies.Attachments[] attachments;
        private String content;

        public Replies() {
        }

        public Replies(DataMessage.Replies replies) {
            this.topic = replies.getTopic();
            this.from = replies.getFrom();
            this.ts = replies.getTs();
            this.seq = replies.getSeq();
            this.head = replies.getHead();
            this.attachments = replies.getHead().getAttachments();
            this.content = replies.getContent();
        }

        class Head{
            private String mime;
            private int reply;
        }


        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTs() {
            return ts;
        }

        public void setTs(String ts) {
            this.ts = ts;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public DataMessage.Replies.Head getHead() {
            return head;
        }

        public void setHead(DataMessage.Replies.Head head) {
            this.head = head;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
    public class EmptyObject {
        // EmptyObject implementation, can be an empty class or contain any necessary methods/fields
    }

}
