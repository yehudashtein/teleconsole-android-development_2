package com.telebroad.teleconsole.chat.server;

import android.content.Context;

import java.util.Map;

public class CtrlMessage {
    private String topic;
    private String id;
    private String code;
    private String text;
    //private Map<?,?> params;
    private Params params ;
    private String ts;

    public CtrlMessage(String topic, String id, String code, String text, Params params, String ts) {
        this.topic = topic;
        this.id = id;
        this.code = code;
        this.text = text;
        this.params = params;
        this.ts = ts;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Params getParams() {
        return params;
    }
    //public Map<?, ?> getParams() {
        //return params;
    //}

    //public void setParams(Map<?, ?> params) {
        //this.params = params;
    //}

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }
    public static class Params {
        private String authlvl;
        private String expires;
        private String token;
        private String user;
        private String what;
        private String url;
        private int seq;

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public Params() {
        }

        public Params(String authlvl, String expires, String token, String user, String what, String url) {
            this.authlvl = authlvl;
            this.expires = expires;
            this.token = token;
            this.user = user;
            this.what = what;
            this.url = url;
        }

        public void setAuthlvl(String authlvl) {
            this.authlvl = authlvl;
        }

        public void setExpires(String expires) {
            this.expires = expires;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public void setWhat(String what) {
            this.what = what;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWhat() {
            return what;
        }

        public String getAuthlvl() {
            return authlvl;
        }

        public String getExpires() {
            return expires;
        }

        public String getToken() {
            return token;
        }

        public String getUser() {
            return user;
        }

    }

}
