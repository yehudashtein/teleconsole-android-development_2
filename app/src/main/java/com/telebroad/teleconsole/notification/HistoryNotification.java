package com.telebroad.teleconsole.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;

import androidx.core.app.NotificationCompat;
import androidx.room.Ignore;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.DashboardActivity;
import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;
import com.telebroad.teleconsole.viewmodels.CallHistoryViewModel;

import java.util.List;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;
import static com.telebroad.teleconsole.helpers.IntentHelper.TAB_TO_OPEN;
import static com.telebroad.teleconsole.notification.NotificationBuilder.MISSED_CALL_ID;
import static com.telebroad.teleconsole.notification.NotificationService.EXTRA_OTHER_NUMBER;
import static com.telebroad.teleconsole.notification.NotificationService.MARK_MISSED_CALLS_AS_NOTIFIED;

/*"dir": "OUTGOING",
          "end": 1537200999,
          "type": "HISTORY",
          "start": 1537200985,
          "stype": "phone",
          "callid": "1537200985.18527001",
          "status": "answer",
          "dnumber": "8615",
          "totaltime": 14

          "dir": "INCOMING",
          "end": 1537201000,
          "type": "HISTORY",
          "sname": "Shlomie",
          "start": 1537200986,
          "stype": "huntgroup",
          "callid": "1537200985.18527001",
          "status": "answer",
          "dnumber": "1907239",
          "snumber": "12124449911",
          "totaltime": 14*/

public class HistoryNotification {

    private static final String MISSED_CALL_GROUP = "com.teleconsole.notifications.group.missed.call";
    public static String callDeclined;
    private Message.Direction dir;
    private long end;
    private long start;
    private String stype;
    private String sname;
    private String snumber;
    private String callid;
    private CallHistory.CallStatus status;
    private String dnumber;
    private String cnumber;
    private int totaltime;
    private int is_owner;

    public CallHistory convertToCallHistory() {
        CallHistory callHistory = new CallHistory();
        callHistory.setCallid(getCallid());
        callHistory.setId(getCallid());
        callHistory.setDnumber(getDnumber());
        if (getDir() == Message.Direction.OUT) {
            callHistory.setSnumber(cnumber);
        } else {
            callHistory.setNeedsNotification(true);
            callHistory.setSnumber(getSnumber());
        }
        callHistory.setDirection(getDir());
        callHistory.setStatus(getStatus());
        callHistory.setSname(getSname());
        callHistory.setDuration(String.valueOf(getTotaltime()));
        callHistory.setTimestamp(getStart());
        //android.util.Log.d("Notification0001", " created call history " + callHistory);
        return callHistory;
    }

    public Message.Direction getDir() {
        return dir;
    }

    public void setDir(Message.Direction dir) {
        this.dir = dir;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public String getStype() {
        return stype;
    }

    public void setStype(String stype) {
        this.stype = stype;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getSnumber() {
        return snumber;
    }

    public void setSnumber(String snumber) {
        this.snumber = snumber;
    }

    public String getCallid() {
        return callid;
    }

    public void setCallid(String callid) {
        this.callid = callid;
    }

    public CallHistory.CallStatus getStatus() {
        return status;
    }

    public void setStatus(CallHistory.CallStatus status) {
        this.status = status;
    }

    public String getDnumber() {
        return dnumber;
    }

    public void setDnumber(String dnumber) {
        this.dnumber = dnumber;
    }

    public int getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(int totaltime) {
        this.totaltime = totaltime;
    }


    public int getIs_owner() {
        return is_owner;
    }

    public void setIs_owner(int is_owner) {
        this.is_owner = is_owner;
    }

    void showMissedCallNotification(Context context) {
        if (getCallid().equals(callDeclined)){
            return;
        }
        Intent clearIntent = new Intent(context, NotificationService.class);
        clearIntent.setAction(MARK_MISSED_CALLS_AS_NOTIFIED);
        clearIntent.putExtra(EXTRA_OTHER_NUMBER, getSnumber());
        int duplicates = TeleConsoleDatabase.getInstance(context).callHistoryDao().getCountBySnumber(getSnumber());
        //android.util.Log.d("GroupNotif", "Duplicates " + duplicates);
        List<HistoryGroupNotification> groups = TeleConsoleDatabase.getInstance(context).callHistoryDao().getAllNeedingNotification();
        //android.util.Log.d("GroupNotif", "groups " + groups);

        String number = "";
        if (duplicates > 1){
            number = " (" + duplicates + ")";
        }
       // android.util.Log.d("MissedNotification", "number" + number);
        Intent showCallLogsIntent = new Intent(context.getApplicationContext(), DashboardActivity.class);
        showCallLogsIntent.putExtra(TAB_TO_OPEN, 1);
        PendingIntent callLogsPendingIntent = NotificationBuilder.getInstance().getBackStackPendingIntent(showCallLogsIntent);
        CallHistoryViewModel callHistoryVM = new CallHistoryViewModel();
        callHistoryVM.setItem(convertToCallHistory());
        int time = (int)getStart();
       // android.util.Log.d("HistNotif02", "time = " + time);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppController.MISSED_CALL_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.missed_call))
                .setContentText("From: " + callHistoryVM.getOtherNumber().getNameString() + number)
                .setPriority(PRIORITY_HIGH)
                .setGroup(MISSED_CALL_GROUP)
                .setDeleteIntent(PendingIntent.getService(context, getSnumber().hashCode(), clearIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE))
                .addAction(NotificationBuilder.getInstance().getCallBackAction(getSnumber().hashCode(), callHistoryVM.getOtherNumber().fixed()))
                .setContentIntent(callLogsPendingIntent)
                .setAutoCancel(true);
        NotificationBuilder.getInstance().getNotificationManager().notify(getSnumber().hashCode(), builder.build());
        if (groups.size() > 1){
            showGroupNotification(context, groups);
        }
    }

    private void showGroupNotification(Context context, List<HistoryGroupNotification> groups) {
        Intent showCallLogsIntent = new Intent(context.getApplicationContext(), DashboardActivity.class);
        showCallLogsIntent.putExtra(TAB_TO_OPEN, 1);
        PendingIntent callLogsPendingIntent = NotificationBuilder.getInstance().getBackStackPendingIntent(showCallLogsIntent);

        Intent clearIntent = new Intent(context, NotificationService.class);
        clearIntent.setAction(MARK_MISSED_CALLS_AS_NOTIFIED);
        clearIntent.putExtra(EXTRA_OTHER_NUMBER, getSnumber());

        String contentText = "";
        for (HistoryGroupNotification group : groups){
            contentText = group.getNotificationString() + ", ";
        }
        contentText = contentText.substring(0, contentText.length() - 2);
//        int time = (int)getStart();
//        android.util.Log.d("HistNotif02", "time = " + time);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppController.MISSED_CALL_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.missed_calls_notif))
//                .setContentText("From: " + contentText)
                .setPriority(PRIORITY_HIGH)
                .setGroup(MISSED_CALL_GROUP)
                .setDeleteIntent(PendingIntent.getService(context, MISSED_CALL_ID, clearIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE))
                .setGroupSummary(true)
                .setContentIntent(callLogsPendingIntent)
                .setAutoCancel(true);
        NotificationBuilder.getInstance().getNotificationManager().notify(NotificationBuilder.MISSED_CALL_ID, builder.build());
    }

    @Override
    public int hashCode() {
        return getSnumber().hashCode();
    }
}

