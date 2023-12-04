package com.telebroad.teleconsole.notification;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import com.telebroad.teleconsole.model.PhoneNumber;

public class HistoryGroupNotification {
    @ColumnInfo(name = "count") public int count;
    @ColumnInfo(name = "snumber") public String snumber;
    @ColumnInfo(name = "sname") public String sname;

    @Ignore
    public String getNotificationString(){
        String number = "";
        if (count > 1 ){
            number = " (" + count + ")";
        }
        return PhoneNumber.getPhoneNumber(snumber, sname).getNameString() + number;
    }
}
