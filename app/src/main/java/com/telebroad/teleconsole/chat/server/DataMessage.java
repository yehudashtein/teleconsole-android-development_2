package com.telebroad.teleconsole.chat.server;

import androidx.annotation.NonNull;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataMessage implements Serializable {
    private String topic;
    private String from;
    private String ts;
    private Head head;
    private List<Reaction> reactions ;
    private int seq;
    private Replies[] replies;
    private Object content;
    private int limit;
    private int before;
    private List<Edits> edits;

    public List<Edits> getEdits() {
        return edits;
    }

    public void setEdits(List<Edits> edits) {
        this.edits = edits;
    }

    public DataMessage(int limit, int before) {
        this.limit = limit;
        this.before = before;
    }
    public static   DataMessage getLoad(int limit, int before){
        DataMessage dataMessage = new DataMessage(limit,before);
        return dataMessage;
    }

    public DataMessage(String topic, String from, String ts, Head head,List<Reaction> reactions, int seq,Replies[] reply, Object content) {
        this.topic = topic;
        this.from = from;
        this.ts = ts;
        this.head = head;
        this.reactions = reactions;
        this.seq = seq;
        this.replies = reply;
        this.content = content;
    }

    public static class Reaction implements Serializable{
        private String content;
        private String from;
        private String topic;

        public Reaction(String content, String from) {
            this.content = content;
            this.from = from;
        }
        public Reaction(DataMessage.Reaction reaction) {
            this.content = reaction.getContent();
            this.from = reaction.getFrom();
            this.topic = reaction.getTopic();
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

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @NonNull
        @Override
        public String toString() {
            return "Reaction{" +
                    ", content='" + content + '\'' +
                    '}';
        }
    }

    public String getTopic() {
        return topic;
    }

    public String getFrom() {
        return from;
    }

    public String getTs() {
        return ts;
    }

    public Head getHead() {
        return head;
    }



    public int getSeq() {
        return seq;
    }


    public Object getContent() {
        return content;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public List<Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<Reaction> reactions) {
        this.reactions = reactions;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Replies[] getReplies() {
        return replies;
    }

    public void setReplies(Replies[] replies) {
        this.replies = replies;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getBefore() {
        return before;
    }

    public void setBefore(int before) {
        this.before = before;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @NonNull
    @Override
    public String toString() {
        return "DataMessage{" +
                "topic='" + topic + '\'' +
                ", from='" + from + '\'' +
                ", ts='" + ts + '\'' +
                ", head=" + head +
                ", reactions=" + reactions +
                ", seq=" + seq +
                ", reply=" + replies +
                ", content='" + content + '\'' +
                '}';
    }
    public class Head{
        private List<ServerAttachments> attachments;
        private String mime;
        private int reply;
        private int replace;
        @SerializedName("x-reaction")
        Boolean reaction;
        boolean forwarded;

        public Head(List<ServerAttachments> attachments, String mime, int reply, int replace, Boolean reaction) {
            this.attachments = attachments;
            this.mime = mime;
            this.reply = reply;
            this.replace = replace;
            this.reaction = reaction;
        }

        public Boolean getReaction() {
            return reaction;
        }

        public void setReaction(Boolean reaction) {
            this.reaction = reaction;
        }


        public int getReplace() {
            return replace;
        }

        public void setReplace(int replace) {
            this.replace = replace;
        }

        @NonNull
        public List<ServerAttachments> getAttachments() {
            return attachments == null ? new ArrayList<>() : attachments;
        }

        public boolean isForwarded() {
            return forwarded;
        }

        public void setForwarded(boolean forwarded) {
            this.forwarded = forwarded;
        }

        public void setAttachments(List<ServerAttachments> attachments) {
            this.attachments = attachments;
        }

        public int getReply() {
            return reply;
        }

        public void setReply(int reply) {
            this.reply = reply;
        }

        public String getMime() {
            return mime;
        }

        public void setMime(String mime) {
            this.mime = mime;
        }



        public class ServerAttachments {
            private double height;
            private String name;
            private String path;
            private double size;

            public double getHeight() {
                return height;
            }

            public void setHeight(double height) {
                this.height = height;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public double getSize() {
                return size;
            }

            public void setSize(double size) {
                this.size = size;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            private String type;
        }
    }
    public class Content{
        private String content;
        private int msgseq;

        public int getMsgseq() {
            return msgseq;
        }

        public void setMsgseq(int msgseq) {
            this.msgseq = msgseq;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

    }
    public static class Replies  {
        private String topic;
        private String from;
        private String ts;
        private int seq;
        private Head head;
        private String content;
        private Reaction[] reactions;

        public Replies() {
        }


        public Replies(Replies replies) {
        }



        public Reaction[] getReaction() {
            return reactions;
        }

        public void setReaction(Reaction[] reaction) {
            this.reactions = reaction;
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

        public Head getHead() {
            return head;
        }

        public void setHead(Head head) {
            this.head = head;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
        public class Reaction{
            private String from;
            private String content;

            public Reaction(Reaction reaction) {
                this.content = reaction.getContent();
                this.from = reaction.getFrom();
            }

            public String getFrom() {
                return from;
            }

            public void setFrom(String from) {
                this.from = from;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }

        public class Head{
            private String mime;
            private int reply;
            private Attachments[] attachments;

            public Attachments[] getAttachments() {
                return attachments;
            }

            public void setAttachments(Attachments[] attachments) {
                this.attachments = attachments;
            }

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
        }
        public class Attachments{
            private String name;
            private String path;
            private String type;


            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

        }
    }
    public class Edits{
        private String content;
        private String from;
        private Head head;
        private int seq;
        private String topic;
        private String ts;

        public String getContent() {
            return content;
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

        public Head getHead() {
            return head;
        }

        public void setHead(Head head) {
            this.head = head;
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

        public String getTs() {
            return ts;
        }

        public void setTs(String ts) {
            this.ts = ts;
        }
    }

}
