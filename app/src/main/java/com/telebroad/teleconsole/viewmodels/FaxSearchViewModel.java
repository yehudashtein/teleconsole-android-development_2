package com.telebroad.teleconsole.viewmodels;

import android.content.Context;
import android.text.format.DateUtils;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.SearchFaxModel;

public class FaxSearchViewModel extends SearchFaxModel {
    public int getStatusColor() {
            int status = Integer.parseInt(getDlr_status());
            if (status == 0){
                return R.color.fax_successful;
            } else{
                return R.color.fax_failed;
            }
    }
    public PhoneNumber findOtherNumber() {
        return PhoneNumber.getPhoneNumber(getDir().equals("INBOX") ? getCallerid() : getCalled());
    }
    public String getInfo(){
        if (getDir().equals("INBOX")){
            return "Fax Received";
        }
        if (getDlr_status().isEmpty() ||getDlr_status() == null){
            return "Fax Submitted";
        }
        try {
            int status = Integer.parseInt(getDlr_status());
            if (status == 0){
                return "Fax Sent Successfully";
            }
            if (status < 0){
                return "Fax Sending";
            } else{
                return "Failed to send Fax";
            }
        }catch (NumberFormatException numberFormatException){
            return "Fax Submitted";
        }
//        return getItem().getDirection() == Message.Direction.IN ? "Fax Received" : "Fax Sent";
    }
    public String getFormattedTime(Context context, long timeInMilliseconds) {
        long millis = timeInMilliseconds * 1000;
        boolean isToday = DateUtils.isToday(millis);
        boolean isYesterday = DateUtils.isToday(millis + DateUtils.DAY_IN_MILLIS);
        if (isToday) {
            return DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_SHOW_TIME);
        } else if (isYesterday) {
            return "Yesterday " + DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_SHOW_TIME);
        } else {
            return DateUtils.formatDateTime(context, millis,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH);
        }
    }
}
