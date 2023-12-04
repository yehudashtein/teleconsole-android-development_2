package com.telebroad.teleconsole.chat.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PubMassage3 {
    private String id;
    private String topic;
    private String from;
    private boolean noecho;
    private Object content;
    private PubMassage3.Head head = new PubMassage3.Head();
    private SetMessage set;

    public PubMassage3() {
    }

    public PubMassage3(String id, String topic, String from, boolean noecho, Object content, PubMassage3.Head head) {
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


    public PubMassage3(String topic, String content) {
        this.topic = topic;
        this.noecho = false;
        this.content = content;
    }
    public static PubMassage3 createReaction(String topic, String reaction, String reactionTo){
        PubMassage3 message = new PubMassage3(topic, null);
        Map<String,String> content = new HashMap();
        content.put("content", reaction);
        content.put("msgseq", reactionTo);
        message.content = content;
        message.head = PubMassage3.Head.createReactionHeaders();
        return message;
    };
    public static class Head {
        private String mime ;
        private boolean isReaction = false;
        private boolean forwarded;
        private List<PubMassage3.Attachments> attachments;
        private List<String> mentions;

        public List<String> getMentions() {
            return mentions;
        }

        public void setMentions(List<String> mentions) {
            this.mentions = mentions;
        }

        public Head(String mime , List<PubMassage3.Attachments> attachments) {
            this.mime = mime;
            this.attachments = attachments;
        }

        public Head() {
        }

        public Head(boolean forwarded) {
            this.forwarded = forwarded;
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

        public List<PubMassage3.Attachments> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<PubMassage3.Attachments> attachments) {
            this.attachments = attachments;
        }

        public static PubMassage3.Head createReactionHeaders(){
            PubMassage3.Head head = new PubMassage3.Head();
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

    public PubMassage3.Head getHead(String mime) {
        PubMassage3.Head head = new PubMassage3.Head();
        head.mime = mime;
        return head;
    }
    public PubMassage3.Head getHead1(String mime, List<String> mentions) {
        PubMassage3.Head head = new PubMassage3.Head();
        head.mime = mime;
        head.mentions = mentions;
        return head;
    }
    public PubMassage3.Head getHead(String mime, int reply) {
        PubMassage3.Head head = new PubMassage3.Head();
        head.mime = mime;
        return head;
    }
    public PubMassage3.Head getHead(){
        return head;
    }

    public void setHead(PubMassage3.Head head) {
        this.head = head;
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
    public static PubMassage3.Head setHeadWithAttachments(List<PubMassage3.Attachments> attachments, List<String> mentions){
        PubMassage3.Head head = new PubMassage3.Head();
        head.mime = "text/html";
        head.mentions = mentions;
        head.attachments = attachments;
        return head;
    }
    public static PubMassage3.Head setHeadWithAttachments(List<PubMassage3.Attachments> attachments){
        PubMassage3.Head head = new PubMassage3.Head();
        head.mime = "text/html";
        head.attachments = attachments;
        return head;
    }
    public static PubMassage3.Head setHeadWithAttachmentsForwards(List<PubMassage3.Attachments> attachments) {
        PubMassage3.Head head = new PubMassage3.Head();
        head.mime = "text/html";
        head.forwarded = true;
        head.attachments = attachments;
        return head;
    }
    public static PubMassage3 setPubForImages(PubMassage3.Head head, String id, String from, String topic, String con){
        PubMassage3 pubMessage = new PubMassage3();
        pubMessage.head = head;
        pubMessage.id = id;
        pubMessage.from = from;
        pubMessage.noecho = false;
        pubMessage.topic = topic;
        pubMessage.content = con;
        return pubMessage;
    }
    public static PubMassage3 setPubForImagesForward(PubMassage3.Head head, String id, String from, String topic){
        PubMassage3 pubMessage = new PubMassage3();
        pubMessage.head = head;
        pubMessage.id = id;
        pubMessage.from = from;
        pubMessage.noecho = false;
        pubMessage.topic = topic;
        return pubMessage;
    }
}
