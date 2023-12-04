package com.telebroad.teleconsole.notification;

import android.os.AsyncTask;

import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Fax;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.repositories.FaxRepository;

public class FaxNotification {
    private String from, to , file;
    private Message.Direction dir;
    private long time;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Message.Direction getDir() {
        return dir;
    }

    public void setDir(Message.Direction dir) {
        this.dir = dir;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Fax convertToFax(){
        Fax fax = new Fax();
        fax.setId(String.valueOf(getTime()));
        fax.setTimestamp(getTime());
        fax.setDirection(getDir());
        fax.setMailbox(getTo());
        fax.setCalled(getTo());
        fax.setCaller(getFrom());
        fax.setCallerid(getFrom());
        fax.setName(getFile());
        Utils.asyncTask(() ->{
            FaxRepository.getInstance().saveFaxes(fax);
        });
        return fax;
    }

    @Override
    public String toString() {
        return "FaxNotification{" +
                "from=" + from +
                "\n to=" + to +
                "\n file=" + file +
                "\n dir=" + dir +
                "\n time=" + time +
                '}';
    }
}
