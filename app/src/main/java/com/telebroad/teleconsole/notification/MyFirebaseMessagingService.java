package com.telebroad.teleconsole.notification;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.repositories.CallHistoryRepository;
import com.telebroad.teleconsole.model.repositories.SMSRepository;

import java.util.Map;

import static com.telebroad.teleconsole.helpers.SettingsHelper.DIRECT_CALLS_ONLY;
import static com.telebroad.teleconsole.helpers.SettingsHelper.getBoolean;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onNewToken(String s) {
      // android.util.Log.d("FCM", "new token " + s);
        super.onNewToken(s);
    }

    @NonNull
    private NotificationBuilder notificationBuilder = NotificationBuilder.getInstance(this);
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //android.util.Log.d("PNMessage", remoteMessage.getData().toString());
        String nmsg = remoteMessage.getData().get("nmsg");
        String type = remoteMessage.getData().get("type");
        if (nmsg != null){
            SipManager manager = SipManager.getInstance(getApplicationContext());
            Utils.logToFile(this, "Notification received type is mizuMessage");
            if (manager != null){
                android.util.Log.d("8881", "nmsg arrived");
                manager.updateUser(true);
            }else{
                Utils.logToFile("Manager is null");
            }
            return;
        }
        Map<String, String> data = remoteMessage.getData();
        if (type == null) {
            return;
        }
        Utils.logToFile(this, "Notification received type is " + type);
       // android.util.Log.d("HistNotif01", "type is " + type);
        switch (type) {
            case "MAIL":
                notificationBuilder.showVoicemailNotification(data.get(URLHelper.KEY_MESSAGE));
                break;
            case "VOIP":
                JsonObject messageJson = JsonParser.parseString(data.get(URLHelper.KEY_MESSAGE)).getAsJsonObject();
                String subType = messageJson.get(URLHelper.KEY_TYPE).getAsString();
                if (subType.equals("HISTORY")){
                   // android.util.Log.d("", "message json" + messageJson);
                    HistoryNotification historyNotification = new Gson().fromJson(messageJson, HistoryNotification.class);
                    CallHistoryRepository.getInstance().saveCallHistory(historyNotification.convertToCallHistory());
                    if (historyNotification.getDir() == Message.Direction.IN && historyNotification.getStatus() == CallHistory.CallStatus.MISSED && !checkGroupCallDND(historyNotification)){
                        notificationBuilder.showMissedCallNotification(historyNotification);
//                        historyNotification.showMissedCallNotification(this);
                    }
                }else{
                    SipManager manager = SipManager.getInstance(getApplicationContext());
                    //android.util.Log.d("8881", "voip message arrived");
                    manager.updateUser(false);
                }
                messageJson.get("dir");
                break;
            case "SMS":
                SMSNotification smsNotification = new Gson().fromJson(new Gson().toJson(remoteMessage.getData()), SMSNotification.class);
               // android.util.Log.d("AddingSMS", "ID is " + smsNotification.convertToSMS().getId());
                SMSRepository.getInstance().addSMSToConversation(smsNotification.convertToSMS());
                notificationBuilder.showSMSNotification(smsNotification);
                break;
            case "FAX":
                messageJson = new JsonParser().parse(data.get(URLHelper.KEY_MESSAGE)).getAsJsonObject();
                notificationBuilder.showFaxNotification( new Gson().fromJson(messageJson, FaxNotification.class));
                break;
            case "teams":
                String head = remoteMessage.getData().get("head");
                String head1 = head.replace("\\","");
                ChatNotifications chatNotifications2 = new ChatNotifications(remoteMessage.getData().get("frm"),
                    remoteMessage.getData().get("seq"),remoteMessage.getData().get("head"),
                        remoteMessage.getData().get("time"),remoteMessage.getData().get("type"),remoteMessage.getData().get("frmFn")
                ,remoteMessage.getData().get("topic"),remoteMessage.getData().get("message"));
                ChatNotifications.getInstance(this).showChatNotification(chatNotifications2);
                 //ChatNotifications chatNotifications3 = new Gson().fromJson(new Gson().toJson(chatNotifications2), ChatNotifications.class);
                //Log.d("chat_notification_data",new Gson().toJson(chatNotifications2));
                break;
        }
    }

    private boolean checkGroupCallDND(HistoryNotification historyNotification){
        if (getBoolean(DIRECT_CALLS_ONLY, false)){
            String stype = historyNotification.getStype();
            if ("queue".equals(stype) || "huntgroup".equals(stype)){
                return historyNotification.getIs_owner() == 0;
            }
            return false;
        }else{
            return false;
        }
    }

    private boolean isGroupCall(HistoryNotification historyNotification){
        if ("phone".equals(historyNotification.getStype())){
            return false;
        }
        return historyNotification.getIs_owner() == 0;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
       // android.util.Log.d("TASK", "task removed");
        super.onTaskRemoved(rootIntent);
    }
}


