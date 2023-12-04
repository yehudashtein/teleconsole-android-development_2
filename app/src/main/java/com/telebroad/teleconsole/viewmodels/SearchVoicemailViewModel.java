package com.telebroad.teleconsole.viewmodels;

import static com.google.common.base.Strings.isNullOrEmpty;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.SearchVoicemailModel;

import java.util.Locale;

public class SearchVoicemailViewModel extends SearchVoicemailModel {
   public CharSequence getUserInfo(String query){
       if (query != null) {
           if (!isNullOrEmpty(getCallername())) {
               SpannableString spannableString = new SpannableString(getCallername());
               int startIdx = getCallername().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
               int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
               if (startIdx != -1) {
                   spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
               }
               return spannableString;
           } else {
               return PhoneNumber.getPhoneNumber(getCallerid()).toString();
           }
       }else {
           return getCallername();
       }
   }
    public String getFormattedDuration() {
        return Utils.formatSeconds(Integer.parseInt(getDuration()));
    }
    public String getInfo() {
        return "(" + getFormattedDuration() + ")";
    }
}
