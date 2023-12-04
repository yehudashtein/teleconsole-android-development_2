package com.telebroad.teleconsole.viewmodels;

import static com.google.common.base.Strings.isNullOrEmpty;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;

import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.SearchSmsMessages;

import java.util.Locale;

public class SearchSmsMessageViewModel extends SearchSmsMessages {

    public PhoneNumber findOtherNumber() {
        return PhoneNumber.getPhoneNumber(String.valueOf(getDirection().equals("in")? getSender() : getReceiver()));
    }
    public PhoneNumber findMyNumber() {
        return PhoneNumber.getPhoneNumber(String.valueOf(getDirection().equals("out") ? getSender() : getReceiver()));
    }
    public SpannableString getDataInfo(String query){
        if (query != null) {
            SpannableString spannableString = new SpannableString(getMsgdata());
            int startIdx = getMsgdata().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
            int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
            if (startIdx != -1) {
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannableString;
        }else {
            return null;
        }
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

    public boolean isIn(){
        return getDirection().equals("in");
    }

    public CharSequence getNameInfo(String query,Context context) {
        final SpannableString[] name = new SpannableString[1];
        if (!isNullOrEmpty(String.valueOf(getReceiver())) ){
            new PhoneNumber(String.valueOf(getReceiver()), null, null).getName(null).observe((LifecycleOwner) context, s -> {
                if (!isNullOrEmpty(s) && isInt(String.valueOf(getReceiver()))) {
                    if (query != null) {
                        SpannableString spannableString = new SpannableString(s);
                        int startIdx = s.toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
                        int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
                        if (startIdx != -1) {
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        name[0] = spannableString;
                        Log.d("letsSee", name[0].toString());
                    }
                }
            });
            if (name[0] != null){
                return name[0];
            }else {
                return String.valueOf(getSender());
            }

        }
        return "";
    }

    public  boolean isInt(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
