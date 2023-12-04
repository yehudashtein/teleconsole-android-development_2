package com.telebroad.teleconsole.chat.server;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.telebroad.teleconsole.chat.Cred;

import java.util.List;
import java.util.Map;

public class MetaMessage {
    private String id;
    private String topic;
    private String ts;
    private Desc desc;
    private List<Sub> sub;
    private List<String> tags;
    private List<Cred> cred;
    private Map<String, ?> del;


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

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public Desc getDesc() {
        return desc;
    }

    public void setDesc(Desc desc) {
        this.desc = desc;
    }

    public List<Sub> getSub() {
        return sub;
    }

    public void setSub(List<Sub> sub) {
        this.sub = sub;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Cred> getCred() {
        return cred;
    }

    public void setCred(List<Cred> cred) {
        this.cred = cred;
    }

    public Map<String, ?> getDel() {
        return del;
    }

    public void setDel(Map<String, ?> del) {
        this.del = del;
    }

    @Override
    @NonNull
    public String toString() {
        return "MetaMessage{" +
                "id='" + id + '\'' +
                ", topic='" + topic + '\'' +
                ", ts='" + ts + '\'' +
                ", desc=" + desc +
                ", sub=" + sub +
                ", tags=" + tags +
                ", cred=" + cred +
                ", del=" + del +
                '}';
    }

    public static class Desc{
        @NonNull
        @Override
        public String toString() {
            return "Desc{" +
                    "created='" + created + '\'' +
                    ", updated='" + updated + '\'' +
                    ", touched='" + touched + '\'' +
                    ", state='" + state + '\'' +
                    ", status='" + status + '\'' +
                    ", defacs=" + defacs +
                    ", acs=" + acs +
                    ", seq=" + seq +
                    ", read=" + read +
                    ", recv=" + recv +
                    ", clear=" + clear +
                    ", trusted=" + trusted +
                    ", publicParams=" + publicParams +
                    ", privateParams=" + privateParams +
                    '}';
        }

        private String created;
        private String updated;
        private String touched;
        private String state;
        private String status;
        private Map<String, ?> defacs;
        private Map<String, ?> acs;
        private Integer seq;
        private Integer read;
        private Integer recv;
        private Integer clear;
        private Map<String, ?> trusted;
        @SerializedName("public")
        private Public publicParams;
        @SerializedName("private")
        private Map<String, ?> privateParams;

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getTouched() {
            return touched;
        }

        public void setTouched(String touched) {
            this.touched = touched;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getUpdated() {
            return updated;
        }

        public void setUpdated(String updated) {
            this.updated = updated;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Map<String, ?> getDefacs() {
            return defacs;
        }

        public void setDefacs(Map<String, ?> defacs) {
            this.defacs = defacs;
        }

        public Map<String, ?> getAcs() {
            return acs;
        }

        public void setAcs(Map<String, ?> acs) {
            this.acs = acs;
        }

        public Integer getSeq() {
            return seq;
        }

        public void setSeq(Integer seq) {
            this.seq = seq;
        }

        public Integer getRead() {
            return read;
        }

        public void setRead(Integer read) {
            this.read = read;
        }

        public Integer getRecv() {
            return recv;
        }

        public void setRecv(Integer recv) {
            this.recv = recv;
        }

        public Integer getClear() {
            return clear;
        }

        public void setClear(Integer clear) {
            this.clear = clear;
        }

        public Map<String, ?> getTrusted() {
            return trusted;
        }

        public void setTrusted(Map<String, ?> trusted) {
            this.trusted = trusted;
        }

        public Public getPublicParams() {
            return publicParams;
        }

        public void setPublicParams(Public publicParams) {
            this.publicParams = publicParams;
        }

        public Map<String, ?> getPrivateParams() {
            return privateParams;
        }

        public void setPrivateParams(Map<String, ?> privateParams) {
            this.privateParams = privateParams;
        }
        public class Public {
            private List<String> alltags;
            private String fn;
            //private Map<String,String> photo;
            private Map<String,String> photo;
            private String description;

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public List<String> getAlltags() {
                return alltags;
            }

            public void setAlltags(List<String> alltags) {
                this.alltags = alltags;
            }

            public String getFn() {
                return fn;
            }

            public void setFn(String fn) {
                this.fn = fn;
            }

            public Map<String, String> getPhoto() {
                return photo;
            }

            public void setPhoto(Map<String, String> photo) {
                this.photo = photo;
            }
        }
    }

    public static class Sub {
        @Override
        public String toString() {
            return "Sub{" +
                    "user='" + user + '\'' +
                    ", updated='" + updated + '\'' +
                    ", touched='" + touched + '\'' +
                    ", acs=" + acs +
                    ", read=" + read +
                    ", recv=" + recv +
                    ", clear=" + clear +
                    ", trusted=" + trusted +
                    ", publicParams=" + publicParams +
                    ", privateParams=" + privateParams +
                    ", online='" + online + '\'' +
                    ", topic='" + topic + '\'' +
                    ", seq=" + seq +
                    ", seen=" + seen +
                    '}';
        }

        private String user;
        private String updated;
        private String touched;
        private Map<String, ?> acs;
        private Integer read;
        private Integer recv;
        private Integer clear;
        private Map<String, ?> trusted;
        @SerializedName("public")
        private Map<String, ?> publicParams;
        @SerializedName("private")
        private Object privateParams;
        private String online;
        private String topic;
        private int seq;
        private Map<String, ?> seen;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getUpdated() {
            return updated;
        }

        public void setUpdated(String updated) {
            this.updated = updated;
        }

        public String getTouched() {
            return touched;
        }

        public void setTouched(String touched) {
            this.touched = touched;
        }

        public Map<String, ?> getAcs() {
            return acs;
        }

        public void setAcs(Map<String, ?> acs) {
            this.acs = acs;
        }

        public Integer getRead() {
            return read;
        }

        public void setRead(Integer read) {
            this.read = read;
        }

        public Integer getRecv() {
            return recv;
        }

        public void setRecv(Integer recv) {
            this.recv = recv;
        }

        public Integer getClear() {
            return clear;
        }

        public void setClear(Integer clear) {
            this.clear = clear;
        }

        public Map<String, ?> getTrusted() {
            return trusted;
        }

        public void setTrusted(Map<String, ?> trusted) {
            this.trusted = trusted;
        }

        public Map<String, ?> getPublicParams() {
            return publicParams;
        }

        public void setPublicParams(Map<String, ?> publicParams) {
            this.publicParams = publicParams;
        }

        public Object getPrivateParams() {
            return privateParams;
        }

        public void setPrivateParams(Object privateParams) {
            this.privateParams = privateParams;
        }

        public String getOnline() {
            return online;
        }

        public void setOnline(String online) {
            this.online = online;
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

        public Map<String, ?> getSeen() {
            return seen;
        }

        public void setSeen(Map<String, ?> seen) {
            this.seen = seen;
        }
    }
}
