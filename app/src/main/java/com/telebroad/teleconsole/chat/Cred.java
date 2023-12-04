package com.telebroad.teleconsole.chat;

import java.util.Map;

public class Cred {
    private String meth;
    private String val;
    private boolean done;
    private String resp;
    private Map<String, ?> params;

    public String getMeth() {
        return meth;
    }

    public void setMeth(String meth) {
        this.meth = meth;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getResp() {
        return resp;
    }

    public void setResp(String resp) {
        this.resp = resp;
    }

    public Map<String, ?> getParams() {
        return params;
    }

    public void setParams(Map<String, ?> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "Cred{" +
                "meth='" + meth + '\'' +
                ", val='" + val + '\'' +
                ", done=" + done +
                ", resp='" + resp + '\'' +
                ", params=" + params +
                '}';
    }
}
