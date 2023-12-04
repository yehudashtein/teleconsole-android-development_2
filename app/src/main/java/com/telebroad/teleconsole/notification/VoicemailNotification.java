package com.telebroad.teleconsole.notification;

import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.VoicemailOpenActivity;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.Voicemail;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;
import static androidx.core.app.NotificationCompat.PRIORITY_LOW;

public class VoicemailNotification {

    private static final String VOICEMAIL_GROUP = "com.teleconsole.notifications.group.voicemail";

    private String file;
    private String from;
    private String to;
    private long time;
    private String type;
    private String dir;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

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

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Voicemail convertToVoicemail(){
        Voicemail voicemail = new Voicemail();

        voicemail.setId(String.valueOf(getTime()));
        PhoneNumber caller = PhoneNumber.getPhoneNumber(getFrom());
        voicemail.setCaller(caller.getNameString());
        voicemail.setCallerid(caller.fixed());
        voicemail.setCallerid_ext(caller.fixed());
        voicemail.setName(getName());
        voicemail.setTimestamp(getTime());
        voicemail.setMailbox(getTo());
        voicemail.setDirection(Message.Direction.IN);
        voicemail.setNeedsNotification(true);
        voicemail.setDuration(getDurationFromFile());
        return voicemail;
    }

    private int getDurationFromFile(){
        String[] parts = getName().split("_");
        String lastPart = parts[parts.length-1];
        try {
            return Integer.parseInt(lastPart);
        }catch (NumberFormatException nfe){
            //android.util.Log.d("NumberFormatError", " NAN");
        }
        return 0;
    }

    private String getName(){
        return getFile().split("\\.")[0];
    }


    public void showNotification(Context context){
        long time = getTime();
//        new VoicemailRepository(context.getApplicationContext()).loadNewVoicemail(time, getTo());
        TeleConsoleDatabase.getInstance(context).voicemailDao().save(convertToVoicemail());
        PendingIntent pendingOpenIntent = NotificationBuilder.getInstance().getOpenMessagePendingIntent(time, VoicemailOpenActivity.class);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, AppController.VOICEMAIL_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.new_voicemail))
                .setContentText(context.getString(R.string.from) + " " + PhoneNumber.format(getFrom()))
                .setPriority(PRIORITY_HIGH)
                .setContentIntent(pendingOpenIntent)
                .addAction(NotificationBuilder.getInstance().getCallBackAction((int) time, getFrom()))
                .setGroup(VOICEMAIL_GROUP)
                //.setStyle(new MediaStyle())
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) time, notificationBuilder.build());

        if (TeleConsoleDatabase.getInstance(context).voicemailDao().getCountNeedingNotification() > 1){
            showGroupNotification(context);
        }
    }

    private void showGroupNotification(Context context){

//        Intent clearIntent = new Intent(context, NotificationService.class);
//        clearIntent.setAction(MARK_MISSED_CALLS_AS_NOTIFIED);
//        clearIntent.putExtra(EXTRA_OTHER_NUMBER, getSnumber());
//        int time = (int)getStart();
//        android.util.Log.d("HistNotif02", "time = " + time);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppController.MISSED_CALL_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.missed_calls_notif))
//                .setContentText("From: " + contentText)
                .setPriority(PRIORITY_LOW)
                .setGroup(VOICEMAIL_GROUP)
//                .setDeleteIntent(PendingIntent.getService(context, MISSED_CALL_ID, clearIntent, PendingIntent.FLAG_ONE_SHOT))
                .setGroupSummary(true)
//                .setContentIntent(callLogsPendingIntent)
                .setAutoCancel(true);
        NotificationBuilder.getInstance().getNotificationManager().notify(NotificationBuilder.MISSED_CALL_ID, builder.build());
    }

}
