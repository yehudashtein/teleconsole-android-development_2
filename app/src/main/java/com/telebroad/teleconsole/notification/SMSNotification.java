package com.telebroad.teleconsole.notification;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.SMS;
import com.telebroad.teleconsole.model.Settings;
import com.telebroad.teleconsole.viewmodels.SMSViewModel;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSNotification {
    private String to;
    private String frm;
    private long time;
    private String type;
    private String message;
    private String media;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrm() {
        return frm;
    }

    public void setFrm(String frm) {
        this.frm = frm;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        String message = this.message;
        Matcher matcher = Pattern.compile("Msg:").matcher(message);
        if (matcher.find()) {
            message = message.substring(matcher.end());
        }else{
            //android.util.Log.d("SMS0001", "No Match found");
        }
        return message.trim();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private SMS converted;
    public SMS convertToSMS(){
        if (converted != null){
            return converted;
        }
        Message.Direction direction = Settings.getInstance() != null && Settings.getInstance().getSmsLines().contains(frm) ? Message.Direction.OUT : Message.Direction.IN;
        SMS result = new SMS(getTime(),getTo(), getFrm(), getMessage(), direction);
        result.setMedia(new Gson().fromJson(media, new TypeToken<ArrayList<String>>(){}.getType()));
        result.setNeedsNotification(true);
        result.setIsNew(1);
        result.setId(PhoneNumber.fix(getFrm()));
        converted = result;
        return result;
    }

    public void showNotification(Context context) {
        SMSViewModel smsViewModel = new SMSViewModel();
        smsViewModel.setItem(convertToSMS());
        smsViewModel.showNotification(context);
    }

    private SMSViewModel smsViewModel;
    private SMSViewModel getSMSViewModel(){
        if (smsViewModel == null){
            smsViewModel = new SMSViewModel();
            smsViewModel.setItem(convertToSMS());
        }
        return smsViewModel;
    }

}
