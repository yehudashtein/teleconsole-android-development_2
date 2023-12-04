package com.telebroad.teleconsole.chat.viewModels;

import com.telebroad.teleconsole.chat.client.pubMessage;

import java.util.List;

public class PubUploadViewModel {
    private String id;
    private Object content;
    private String from;
    private boolean noecho;
    private String topic;
    Head head;

    public PubUploadViewModel(pubMessage pubMessage) {
        this.id = pubMessage.getId();
        this.content = pubMessage.getContent();
        this.from = pubMessage.getFrom();
        this.noecho = pubMessage.isNoecho();
        this.topic = pubMessage.getTopic();
        head.attachments = pubMessage.getHead().getAttachments();
        head.mime = pubMessage.getHead().getMime();
        head.attachments.get(0).setName(pubMessage.getHead().getAttachments().get(0).getName());



    }
    static class Head{
      private String mime;
      List<pubMessage.Attachments> attachments;
    }
//    static class Attachments{
//        private String name;
//        private String path;
//        private int size;
//        private String type;
//
//    }
}
