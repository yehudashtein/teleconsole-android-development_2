package com.telebroad.teleconsole.chat.client;

import java.util.ArrayList;
import java.util.List;

public class Extra {
    private List<String> attachments = new ArrayList<>();

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public Extra(List<String> attachments) {
        this.attachments = attachments;
    }

    public Extra() {
    }
}
