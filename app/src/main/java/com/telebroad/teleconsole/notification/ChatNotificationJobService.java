package com.telebroad.teleconsole.notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.core.app.RemoteInput;

import com.google.common.base.Strings;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.PubMassage2;
import com.telebroad.teleconsole.chat.client.pubMessage;
import com.telebroad.teleconsole.controller.SmsConversationActivity;
import com.telebroad.teleconsole.helpers.SettingsHelper;

import java.io.File;

public class ChatNotificationJobService extends JobService {
    private String frm;
    private String seq;
    private String head;
    private String time;
    private String teams;
    private String frmFn;
    private String topic;
    private String message;
    private PubMassage2 pubMessage;
    private boolean isCanceled;

    @Override
    public boolean onStartJob(JobParameters params) {
        pubMessage = new PubMassage2();
        frm= params.getExtras().getString("frm");
        seq= params.getExtras().getString("seq");
        head= params.getExtras().getString("head");
        time= params.getExtras().getString("time");
        teams= params.getExtras().getString("teams");
        frmFn= params.getExtras().getString("frmFn");
        topic= params.getExtras().getString("topic");
        message= params.getExtras().getString("message");
        isCanceled = false;
        return true;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = Strings.nullToEmpty(intent.getAction());
        if (action.equals("DeleteIntent")){
            ChatNotifications.getChatNotificationsArrayList().clear();
        }else {
            if (!isCanceled) {
                Bundle results = RemoteInput.getResultsFromIntent(intent);
                if (results != null) {
                    if (ChatWebSocket.isConnected) {
                        CharSequence quickReplyResult = results.getCharSequence(ChatNotifications.KEY_REPLY);
                        ChatNotifications chatNotifications = new ChatNotifications(frm, seq, head, time, teams, null, topic, (String) quickReplyResult);
                        ChatNotifications.getInstance(this).DisplayNotification(chatNotifications);
                        PubMassage2 PubMessage = new PubMassage2("pubMessage", topic, quickReplyResult
                                , pubMessage.getHead("text/*"), false);
                        ChatWebSocket.getInstance().sendObject("pub", PubMessage);
                    }else {
                        ChatWebSocket.getInstance().connect();
                        new Handler().postDelayed(() -> {
                            CharSequence quickReplyResult = results.getCharSequence(ChatNotifications.KEY_REPLY);
                            ChatNotifications chatNotifications = new ChatNotifications(frm, seq, head, time, teams, null, topic, (String) quickReplyResult);ChatNotifications.getInstance(this).DisplayNotification(chatNotifications);
                            PubMassage2 PubMessage = new PubMassage2("pubMessage", topic, quickReplyResult
                                    , pubMessage.getHead("text/*"), false);
                            ChatWebSocket.getInstance().sendObject("pub", PubMessage);
                        }, 1000);
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        isCanceled = true;
        return true;
    }
}
