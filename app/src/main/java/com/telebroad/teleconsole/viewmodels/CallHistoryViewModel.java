package com.telebroad.teleconsole.viewmodels;

import androidx.lifecycle.LiveData;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.repositories.CallHistoryRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CallHistoryViewModel extends MessageViewModel<CallHistory> {

    @Override
    public PhoneNumber findMyNumber() {
        return PhoneNumber.getPhoneNumber(getItem().getPhone());
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public PhoneNumber findOtherNumber() {
        return PhoneNumber.getPhoneNumber(getItem().getSnumber(), getItem().getSname());
    }
    @Override
    public LiveData<String> getOtherName(){
        if (getItem() == null || getItem().getSname() == null){
            return getOtherNumber().getName(null);
        }
        if (getItem().getSname().equals(getItem().getSnumber())){
            return getOtherNumber().getName(null);
        }
        return getOtherNumber().getName(getItem().getSname(), null);
    }

    public String getInfo(){
        return getOtherNumber().formatted() + " (" +(getTalkTime()) + ")";
    }

    @Override
    public void deleteItem() {
        CallHistoryRepository.getInstance().delete(getID());
    }

    private String getTalkTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Initialize to 0 as a last fallback
        int seconds = 0;
        // We'd rather use talktime than duration, but we first have to check if talktime is available
        if (getItem().getTalktime() != null){
            seconds = Integer.parseInt(getItem().getTalktime());
            // if it isn't available we will try to use duration
        }else if (getItem().getDuration() != null){
            seconds = Integer.parseInt(getItem().getDuration());
        }
        Date date = new Date(seconds * 1000);
        return dateFormat.format(date);
    }

    @Override
    public int getIconResource(){
        if(getItem().getDirection() == null || getItem().getStatus() == null){
            return R.drawable.ic_call_history;
        }
        if (getItem().getDirection() == Message.Direction.OUT){
            return R.drawable.ic_call_made;
        }
        switch (getItem().getStatus()){
            case MISSED:
            case VOICEMAIL:
                return R.drawable.ic_call_missed;
            case ANSWERED:
            case WRONG_NUMBER:
                return R.drawable.ic_call_received;
        }
        return R.drawable.ic_call_received;
    }

    @Override
    public int getIconBackgroundResource(){
        if (getItem().getDirection() == Message.Direction.IN && (getItem().getStatus() == CallHistory.CallStatus.MISSED || getItem().getStatus() == CallHistory.CallStatus.VOICEMAIL)){
            return R.drawable.bg_missed_call_icon;
        }else {
            return R.drawable.bg_call_icon;
        }
    }

    public void checkIfNeedToLoadMore(){

    }
}
