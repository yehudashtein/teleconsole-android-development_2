package com.telebroad.teleconsole.chat.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplaceReplyPub {
    private String id;
    private String topic;
    private String from;
    private boolean noecho;
    private Object content;
    private ReplaceReplyPub.Head head = new ReplaceReplyPub.Head();
    private SetMessage set;


    public ReplaceReplyPub(String id, String topic, String from, boolean noecho, Object content, ReplaceReplyPub.Head head) {
        this.id = id;
        this.topic = topic;
        this.from = from;
        this.noecho = noecho;
        this.content = content;
        this.head = head;
    }

    public SetMessage getSet() {
        return set;
    }

    public void setSet(SetMessage set) {
        this.set = set;
    }


    public ReplaceReplyPub(String topic, String content) {
        this.topic = topic;
        this.noecho = false;
        this.content = content;
    }
    public static ReplaceReplyPub createReaction(String topic, String reaction, String reactionTo){
        ReplaceReplyPub message = new ReplaceReplyPub(topic, null);
        Map<String,String> content = new HashMap();
        content.put("content", reaction);
        content.put("msgseq", reactionTo);
        message.content = content;
        message.head = ReplaceReplyPub.Head.createReactionHeaders();
        return message;
    };
    public static class Head {
        private String mime ;
        private int replace;
        private int reply;
        private boolean isReaction = false;
        private boolean forwarded;
        private List<ReplaceReplyPub.Attachments> attachments;
        private List<String> mentions;

        public List<String> getMentions() {
            return mentions;
        }

        public void setMentions(List<String> mentions) {
            this.mentions = mentions;
        }

        public Head(String mime, List<ReplaceReplyPub.Attachments> attachments) {
            this.mime = mime;
            this.attachments = attachments;
        }

        public Head() {
        }

        public Head(boolean forward) {
            this.forwarded = forward;
        }

        public Head(String s, int seq,int reply, List<ReplaceReplyPub.Attachments> enums) {
            this.mime = s;
            this.replace = seq;
            this.reply = reply;
            this.attachments = enums;
        }

        public int getReplace() {
            return replace;
        }

        public void setReplace(int replace) {
            this.replace = replace;
        }

        public int getReply() {
            return reply;
        }

        public void setReply(int reply) {
            this.reply = reply;
        }

        public boolean isForwarded() {
            return forwarded;
        }

        public void setForwarded(boolean forwarded) {
            this.forwarded = forwarded;
        }

        public String getMime() {
            return mime;
        }

        public void setMime(String mime) {
            this.mime = mime;
        }


        public boolean isReaction() {
            return isReaction;
        }

        public void setReaction(boolean reaction) {
            isReaction = reaction;
        }

        public List<ReplaceReplyPub.Attachments> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<ReplaceReplyPub.Attachments> attachments) {
            this.attachments = attachments;
        }

        public static ReplaceReplyPub.Head createReactionHeaders(){
            ReplaceReplyPub.Head head = new ReplaceReplyPub.Head();
            head.isReaction = true;
            return head;
        }

    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isNoecho() {
        return noecho;
    }


    public void setNoecho(boolean noecho) {
        this.noecho = noecho;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public ReplaceReplyPub.Head getHead(String mime) {
        ReplaceReplyPub.Head head = new ReplaceReplyPub.Head();
        head.mime = mime;
        return head;
    }

    public ReplaceReplyPub.Head getHead(){
        return head;
    }

    public void setHead(ReplaceReplyPub.Head head) {
        this.head = head;
    }

    public static class Extra{
        private List<String> attachments = new ArrayList<>();
        public Extra() {}
        public void setAttachments(List<String> attachments) {
            this.attachments = attachments;
        }
        public List<String> getAttachments() {
            return attachments;
        }
        public Extra(List<String> attachments) {
            this.attachments = attachments;
        }
    }
    public static class Attachments {
        private String base64;
        private boolean isAudio;
        private String path;
        private int size;
        private String type;
        private String name;
        private String expiresAt;
        private boolean isImage;
        private boolean isPdf;
        public boolean isVideo;

        public boolean isVideo() {
            return isVideo;
        }

        public void setVideo(boolean video) {
            isVideo = video;
        }

        public void setBase64(String base64){
            this.base64 = base64;
        }
        public void setPath(String path){
            this.path = path;
        }
        public void setSize(int size){
            this.size = size;
        }
        public void setType(String type){
            this.type = type;
        }
        public void setName(String name){
            this.name = name;
        }
        public void setAudio(boolean idAudio){
            this.isAudio= idAudio;
        }

        public String getBase64() {
            return base64;
        }

        public boolean isAudio() {
            return isAudio;
        }

        public String getPath() {
            return path;
        }

        public int getSize() {
            return size;
        }

        public String getType() {
            return type;
        }

        public boolean isImage() {
            return isImage;
        }

        public void setImage(boolean image) {
            isImage = image;
        }

        public boolean isPdf() {
            return isPdf;
        }

        public void setPdf(boolean pdf) {
            isPdf = pdf;
        }

        public String getName() {
            return name;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }

        public Attachments(String name, String path, String type,String exp,boolean isAudio){
            this.name = name;
            this.path = path;
            this.type = type;
            this.expiresAt = exp;
            this.isAudio = isAudio;

        }

        public Attachments( String name, String path, String type,boolean isAudio,boolean isImage) {
            this.isAudio = isAudio;
            this.path = path;
            this.type = type;
            this.name = name;
            this.isImage = isImage;
        }
        public Attachments( String name, String path, String type,boolean isAudio,boolean isImage,boolean isPdf) {
            this.isAudio = isAudio;
            this.path = path;
            this.type = type;
            this.name = name;
            this.isImage = isImage;
        }
        public Attachments( String name, String path, String type,boolean isAudio,boolean isImage,boolean isPdf,boolean isVideo) {
            this.isAudio = isAudio;
            this.path = path;
            this.type = type;
            this.name = name;
            this.isImage = isImage;
            this.isVideo = isVideo;
        }
    }
}
