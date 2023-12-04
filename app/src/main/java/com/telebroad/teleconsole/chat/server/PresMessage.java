package com.telebroad.teleconsole.chat.server;

import java.util.List;

public class PresMessage {
    private String act;
    private Object content;
    private int seq;
    private String src;
    private String topic;
    private String what;
    private List<DelSeq> delseq;
    private Head head;
    private Dacs dacs;
    private String tgt;
    private String ua;

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public String getTgt() {
        return tgt;
    }

    public void setTgt(String tgt) {
        this.tgt = tgt;
    }

    public Dacs getDacs() {
        return dacs;
    }

    public void setDacs(Dacs dacs) {
        this.dacs = dacs;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public List<DelSeq> getDelseq() {
        return delseq;
    }

    public void setDelseq(List<DelSeq> delseq) {
        this.delseq = delseq;
    }

    public List<DelSeq> getDelSeqs() {
        return delseq;
    }

    public void setDelSeqs(List<DelSeq> delSeqs) {
        this.delseq = delSeqs;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public class DelSeq{
        private int low;

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }
    }
    public class Head{
        private Object attachments;
        private int replace;

        public Object getAttachments() {
            return attachments;
        }

        public void setAttachments(Object attachments) {
            this.attachments = attachments;
        }

        public int getReplace() {
            return replace;
        }

        public void setReplace(int replace) {
            this.replace = replace;
        }
    }
    public class Dacs{
        private String want;
        private String given;

        public String getWant() {
            return want;
        }

        public void setWant(String want) {
            this.want = want;
        }

        public String getGiven() {
            return given;
        }

        public void setGiven(String given) {
            this.given = given;
        }
    }
}
