package com.telebroad.teleconsole.viewmodels;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.notification.SMSReplyService;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.SmsConversationActivity;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.DlrUpdate;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.SMS;
import com.telebroad.teleconsole.model.repositories.SMSRepository;
import com.telebroad.teleconsole.notification.NotificationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.notification.SMSReplyService.REMOTE_INPUT_KEY;
import static com.telebroad.teleconsole.notification.SMSReplyService.REPLY_TO_SMS_KEY;
import static com.telebroad.teleconsole.helpers.IntentHelper.NOTIFICATION_ID;
import static com.telebroad.teleconsole.notification.NotificationBuilder.MESSAGE_GROUP;

public class SMSViewModel extends MessageViewModel<SMS> {

    public static final ArrayList<String> SUCCESS_LIST = new ArrayList<>(Arrays.asList("", "000", "1", "4"));

    MutableLiveData<Integer> liveDLRVisibility = new MutableLiveData<>();
    LiveData<String> liveDLRError = new MutableLiveData<>();

    @Override
    public PhoneNumber findOtherNumber() {
        return PhoneNumber.getPhoneNumber(getItem().getDirection() == Message.Direction.IN ? getItem().getSender() : getItem().getReceiver());
    }


    @Override
    public PhoneNumber findMyNumber() {
        return PhoneNumber.getPhoneNumber(getItem().getDirection() == Message.Direction.OUT ? getItem().getSender() : getItem().getReceiver());
    }


    @Override
    public boolean isNew() {
        return getItem().getRead() == 0;
    }

    @Override
    public boolean isBlocked(){
        return getItem().isBlocked();
    }


    @Override
    public int getIconResource() {
        return R.drawable.ic_baseline_message_24;
    }

    @Override
    public String getInfo() {
      //  android.util.Log.d("LINK01", "media " + getItem().getMedia());
        if (isNullOrEmpty(getItem().getMsgdata())) {
            if (hasMedia()){
                String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(getItem().getMedia().get(0)));
               // android.util.Log.d("LINK01", "get extension " + MimeTypeMap.getFileExtensionFromUrl(getItem().getMedia().get(0)) + " mimetype " + mimetype);
                if (mimetype != null) {
                    String[] parts = mimetype.split("/");
                    String type = parts[0];
                    //android.util.Log.d("LINK01", "mimetype = " + mimetype + " parts " + Arrays.toString(parts) + " type " + type);
                    return AppController.getAppString(R.string.attachment, type.equals("*") ? "File" : capitalize(type));
                }
            }else {
                return "(1/1)";
            }
        }
        return getItem().getMsgdata();
    }

    private String capitalize(String type) {
        return type.substring(0, 1).toUpperCase() + type.substring(1);
    }

    private boolean hasMedia() {
        return getItem().getMedia() != null && !getItem().getMedia().isEmpty();
    }

    @Override
    public void deleteItem() {
        SMSRepository.getInstance().deleteSMS(getMyNumber().fixed(), getItem());
    }

    @Override
    public void deleteFromList() {
      //  android.util.Log.d("Conversation02", "deleting convo");
        deleteConversation();
    }

    public void deleteConversation() {
        SMSRepository.getInstance().deleteConversation(getMyNumber().fixed(), getOtherNumber().fixed());
    }

    @Override
    public int getIconBackgroundResource() {
        return R.drawable.bg_sms_icon;
    }

    @Override
    public String getID() {
        if (getItem().getIdx() == 0) {
            return getItem().getLid();
        }
        return super.getID();
    }

    @Override
    public boolean matches(String query) {
        if (super.matches(query)) {
            return true;
        }
        return getItem().getMsgdata() != null && getItem().getMsgdata().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SMSViewModel){
            if (super.equals(obj)){
                SMSViewModel otherSMSvm = (SMSViewModel) obj;
                return otherSMSvm.findMyNumber().equals(findMyNumber()) && otherSMSvm.findOtherNumber().equals(findOtherNumber());
            }
            return false;
        }
        return false;
    }

    public boolean isDLRError(){
        if (getDlrError() == null || getDlrError().isEmpty()){
            return false;
        }
        String status = getItem().getDlr_status();
        if (status == null){
            return false;
        }
        return !SUCCESS_LIST.contains(status);
    }

    public void updateDLRVisibility(){
        //android.util.Log.d("UpdateDLRVIS", "updating dlr " + isDLRError() + " msgdata " + getItem().getMsgdata());
        Utils.updateLiveData(liveDLRVisibility, isDLRError() ? VISIBLE : GONE);
    }

    public String getDlrError(){
        return getItem().getDlr_error();
    }

    public void showNotification(Context context) {
        PendingIntent pendingOpenIntent = getOpenConversationIntent(context);
        RemoteInput remoteInput = new RemoteInput.Builder(REMOTE_INPUT_KEY).setLabel(context.getString(R.string.say_something)).build();
        List<SMS> notificationConversation = SMSRepository.getInstance().getNotificationSMSConversation(getMyNumber().fixed(), getOtherNumber().fixed());
        int id = getNotificationID();
        Person otherPerson = new Person.Builder().setName(getOtherNumber().getNameBackground()).build();
        Person me = new Person.Builder().setName("You").build();
        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(otherPerson);
        Collections.reverse(notificationConversation);
        if (notificationConversation.isEmpty()){
            messagingStyle.addMessage(getInfo(), getItem().getTimestamp(), getItem().getDirection() == Message.Direction.IN ? otherPerson : me);
        }
        for (SMS sms : notificationConversation){
            messagingStyle.addMessage(getInfo(), sms.getTimestamp(), sms.getDirection() == Message.Direction.IN ?  otherPerson : me);
        }
       // android.util.Log.d("SMSID", "sending with id = " + id);
        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_send, context.getString(R.string.notif_reply), getReplyIntent(context))
                .addRemoteInput(remoteInput)
                .build();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, AppController.SMS_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.new_sms) + " " + context.getString(R.string.from) + " " + PhoneNumber.format(getOtherNumber().getNameBackground(null)))
                .setContentText(getInfo())
//                .setStyle(messagingStyle)
                .setPriority(PRIORITY_HIGH)
                .setContentIntent(pendingOpenIntent)
                .addAction(replyAction)
                .setGroup(MESSAGE_GROUP)
                .addAction(NotificationBuilder.getInstance().getCallBackAction(id, getOtherNumber().fixed()))
                .setAutoCancel(true);

        if (otherPerson != null ){
            notificationBuilder.setStyle(messagingStyle);
        }
        NotificationBuilder.getInstance().getNotificationManager().notify(id, notificationBuilder.build());
        showGroupNotification(context);
    }

    private PendingIntent getReplyIntent(Context context) {
        Intent intent;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            intent = new Intent(AppController.getInstance(), SMSReplyService.class);
            intent.putExtra(REPLY_TO_SMS_KEY, new Gson().toJson(getItem()));
            intent.putExtra(NOTIFICATION_ID, getNotificationID());
            return PendingIntent.getService(context.getApplicationContext(), getNotificationID(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            return getOpenConversationIntent(context);
        }
    }

    private PendingIntent getOpenConversationIntent(Context context) {
        Intent openIntent = SmsConversationActivity.getIntent(context, getMyNumber(), getOtherNumber());
        return NotificationBuilder.getInstance().getBackStackPendingIntent(openIntent, getNotificationID());
    }

    private void showGroupNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppController.MISSED_CALL_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.missed_calls_notif))
                .setPriority(PRIORITY_HIGH)
                .setGroup(MESSAGE_GROUP)
//                .setDeleteIntent(PendingIntent.getService(context, MISSED_CALL_ID, clearIntent, PendingIntent.FLAG_ONE_SHOT))
                .setGroupSummary(true)
                .setAutoCancel(true);
        NotificationBuilder.getInstance().getNotificationManager().notify(NotificationBuilder.SMS_ID, builder.build());
    }
    public void checkIfNeedToLoadMore(){}

    public void updateDLR(DlrUpdate update) {
        getItem().setDlr_error(update.dlr_error);
        getItem().setDlr_status(update.dlr_status);
        updateDLRVisibility();
    }
}
