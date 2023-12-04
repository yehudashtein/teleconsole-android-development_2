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
import androidx.lifecycle.Observer;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.SearchCallHistoryModel;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCallHistoryViewModel extends SearchCallHistoryModel {
    public boolean isOut(){
        return getDir().equals("out");
    }
    public boolean missed(){
        return getStatus().equals("cancel");
    }
    public boolean isIn(){
        return getDir().equals("in");
    }

    public int getIconBackgroundResource(){
        if (getDir().equals("in") && (getStatus().equals("cancel")|| getStatus().equals("voicemail"))){
            return R.drawable.bg_missed_call_icon;
        }else {
            return R.drawable.bg_call_icon;
        }
    }
//    public <T extends String> T getNameInfo(String query){
//        String result = new PhoneNumber(getSnumber(), null, null).toString();
//        if (!isNullOrEmpty(getSname()) && !isInt(getSname())) {
//            String result1 = new PhoneNumber(getSname(), null, null).toString();
//            return formatPhoneNumber1(result1);
//        }
//        else if (!isNullOrEmpty(getSname())){
//            SpannableString spannableString = new SpannableString(getSname());
//            int startIdx = getSname().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
//            int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
//            if (startIdx != -1) {
//                spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//            return spannableString;
//        }
//        else if (isNullOrEmpty(getSname())&& !isNullOrEmpty(getSnumber())) return formatPhoneNumber1(result);
//        else if (isNullOrEmpty(getSname())&& isNullOrEmpty(getSnumber()) && !isNullOrEmpty(getDnumber())) return getDnumber();
//        else return "";
//    }
public CharSequence getNameInfo(String query,Context context,Callback callback){
    final SpannableString[] name = new SpannableString[1];
    String result = new PhoneNumber(getSnumber(), null, null).toString();
    if (!isNullOrEmpty(getSname())) {
        new PhoneNumber(getSname(), null, null).getName(null).observe((LifecycleOwner) context, s -> {
            if (!isInt(s)) {
                if (query != null) {
                    SpannableString spannableString = new SpannableString(s);
                    int startIdx = s.toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
                    int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
                    if (startIdx != -1) {
                        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    callback.onResult(name[0]);
                    name[0] = spannableString;

                } else {
                    callback.onResult(SpannableString.valueOf(s));
                    name[0] = SpannableString.valueOf(s);
                }
            }else {
                callback.onResult(getSname());
            }
        });
        return name[0];
    }
//    else if (!isNullOrEmpty(getSname())){
//        SpannableString spannableString = new SpannableString(getSname());
//        int startIdx = getSname().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
//        int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
//        if (startIdx != -1) {
//            spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//        return spannableString;
//    }
    else if (isNullOrEmpty(getSname()) && !isNullOrEmpty(getSnumber())) return formatPhoneNumber1(result);
    else if (isNullOrEmpty(getSname()) && isNullOrEmpty(getSnumber()) && !isNullOrEmpty(getDnumber())) return getDnumber();
    else return "";
}

    public int getIconResource(){
        if(getDir() == null || getStatus() == null){
            return R.drawable.ic_call_history;
        }
        if (getDir().equals("out")){
            return R.drawable.ic_call_made;
        }
        switch (getStatus()){
            case "cancel":
            case "voicemail":
                return R.drawable.ic_call_missed;
            case "answer":
            case "chanunav":
            case "error":
                return R.drawable.ic_call_received;
        }
        return R.drawable.ic_call_received;
    }


//public  String getFormattedTime(long timeInMilliseconds) {
//    LocalDateTime now = LocalDateTime.now();
//    Instant instant = Instant.ofEpochMilli(timeInMilliseconds);
//    LocalDateTime inputTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
//    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm a");
//    DateTimeFormatter fullFormat = DateTimeFormatter.ofPattern("MMM d h:mm a");
//    if (inputTime.toLocalDate().equals(now.toLocalDate())) {
//        // Today
//        return timeFormat.format(inputTime);
//    } else if (inputTime.toLocalDate().equals(now.minusDays(1).toLocalDate())) {
//        // Yesterday
//        return "Yesterday " + timeFormat.format(inputTime);
//    } else {
//        // Older
//        return fullFormat.format(inputTime);
//    }
//}
public  String formatSeconds(int seconds) {
    int minutes = seconds / 60;
    int remainingSeconds = seconds % 60;
    return String.format(Locale.getDefault(), "(%d:%02d)", minutes, remainingSeconds);
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
    public  String formatPhoneNumber(String number) {
        Pattern pattern = Pattern.compile("(\\d{1})(\\d{3})(\\d{3})(\\d{2})(\\d{2})");
        Matcher matcher = pattern.matcher(number);
        if (matcher.matches()) {
            return String.format("%s (%s) %s-%s%s",
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3),
                    matcher.group(4),
                    matcher.group(5));
        }
        return number;
    }
    public  String formatPhoneNumber1(String number) {
        Pattern pattern = Pattern.compile("(\\d{3})(\\d{3})(\\d{4})");
        Matcher matcher = pattern.matcher(number);
        if (matcher.matches()) {
            return String.format("(%s) %s-%s",
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3));
        }
        return number;
    }
    public  boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public interface Callback {
        void onResult(CharSequence result);
    }
}
