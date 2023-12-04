package com.telebroad.teleconsole.chat.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PubMassage2 {
    private String id;
    private String topic;
    private String from;
    private boolean noecho;
    private Object content;
    private PubMassage2.Head head = new PubMassage2.Head();
    private PubMassage2.Extra extra = new PubMassage2.Extra();
    private SetMessage set;

    public PubMassage2() {
    }

    public PubMassage2(String id, String topic, String from, boolean noecho, Object content, PubMassage2.Head head) {
        this.id = id;
        this.topic = topic;
        this.from = from;
        this.noecho = noecho;
        this.content = content;
        this.head = head;
    }
    public PubMassage2(String id, String topic, Object content, PubMassage2.Head head, boolean noecho) {
        this.id = id;
        this.topic = topic;
        this.content = content;
        this.head = head;
        this.noecho = noecho;
    }


    public SetMessage getSet() {
        return set;
    }

    public void setSet(SetMessage set) {
        this.set = set;
    }


    public PubMassage2(String topic, String content) {
        this.topic = topic;
        this.noecho = false;
        this.content = content;
    }
    public static PubMassage2 createReaction(String topic, String reaction, String reactionTo){
        PubMassage2 message = new PubMassage2(topic, null);
        Map<String,String> content = new HashMap();
        content.put("content", reaction);
        content.put("msgseq", reactionTo);
        message.content = content;
        message.head = PubMassage2.Head.createReactionHeaders();
        return message;
    };
    public static class Head {
        private String mime ;
        private boolean isReaction = false;
        private boolean forwarded;
        private List<PubMassage2.Attachments> attachments;
        private List<String> mentions;

        public List<String> getMentions() {
            return mentions;
        }

        public void setMentions(List<String> mentions) {
            this.mentions = mentions;
        }

        public Head(String mime , List<PubMassage2.Attachments> attachments) {
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

        public List<PubMassage2.Attachments> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<PubMassage2.Attachments> attachments) {
            this.attachments = attachments;
        }

        public static PubMassage2.Head createReactionHeaders(){
            PubMassage2.Head head = new PubMassage2.Head();
            head.isReaction = true;
            return head;
        }

    }

    public PubMassage2.Extra getExtra() {
        return extra;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setExtra(PubMassage2.Extra extra) {
        this.extra = extra;
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

    public PubMassage2.Head getHead(String mime) {
        PubMassage2.Head head = new PubMassage2.Head();
        head.mime = mime;
        return head;
    }
    public PubMassage2.Head getHead1(String mime,List<String> mentions) {
        PubMassage2.Head head = new PubMassage2.Head();
        head.mime = mime;
        head.mentions = mentions;
        return head;
    }
    public PubMassage2.Head getHead(String mime, int reply) {
        PubMassage2.Head head = new PubMassage2.Head();
        head.mime = mime;
        return head;
    }
    public PubMassage2.Head getHead(){
        return head;
    }

    public void setHead(PubMassage2.Head head) {
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
    public static PubMassage2.Head setHeadWithAttachments(List<PubMassage2.Attachments> attachments,List<String> mentions){
        PubMassage2.Head head = new PubMassage2.Head();
        head.mime = "text/html";
        head.mentions = mentions;
        head.attachments = attachments;
        return head;
    }
    public static PubMassage2.Head setHeadWithAttachmentsForwards(List<PubMassage2.Attachments> attachments) {
        PubMassage2.Head head = new PubMassage2.Head();
        head.mime = "text/html";
        head.forwarded = true;
        head.attachments = attachments;
        return head;
    }
    public static PubMassage2 setPubForImages(PubMassage2.Head head, String id, String from, String topic, String con, List<String> attach){
        PubMassage2 pubMessage = new PubMassage2();
        pubMessage.head = head;
        pubMessage.id = id;
        pubMessage.from = from;
        pubMessage.noecho = false;
        pubMessage.topic = topic;
        pubMessage.content = con;
        pubMessage.extra.attachments = attach;
        return pubMessage;
    }
    public static PubMassage2 setPubForImagesForward(PubMassage2.Head head, String id, String from, String topic, List<String> attach){
        PubMassage2 pubMessage = new PubMassage2();
        pubMessage.head = head;
        pubMessage.id = id;
        pubMessage.from = from;
        pubMessage.noecho = false;
        pubMessage.topic = topic;
        pubMessage.extra.attachments = attach;
        return pubMessage;
    }
}
