package com.telebroad.teleconsole.notification;

import android.app.IntentService;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.SMS;
import com.telebroad.teleconsole.model.repositories.SMSRepository;
import com.telebroad.teleconsole.notification.NotificationBuilder;
import com.telebroad.teleconsole.viewmodels.SMSViewModel;

import androidx.core.app.NotificationManagerCompat;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class SMSReplyService extends IntentService {


    public SMSReplyService() {
        super("SMSReplyService");
    }

    public static String REMOTE_INPUT_KEY = "com.telebroad.teleconsole.sms_reply_service.remote.input";
    public static String REPLY_TO_SMS_KEY = "com.telebroad.teleconsole.sms_reply_service.reply.to.sms";

    @Override
    protected void onHandleIntent(Intent intent) {
      //  android.util.Log.d("SMS0001", "handling Intent");
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null){
           // android.util.Log.d("SMS0001", "Yay we have remote input");

        }else{
           // android.util.Log.d("SMS0001", "Uh oh no remote input");
        }
        if (intent != null && intent.getStringExtra(REPLY_TO_SMS_KEY) != null && remoteInput != null && remoteInput.getCharSequence(REMOTE_INPUT_KEY) != null)   {

            SMS replyingTo = new Gson().fromJson(intent.getStringExtra(REPLY_TO_SMS_KEY), SMS.class);
            SMSViewModel smsViewModel = new SMSViewModel();
            smsViewModel.setItem(replyingTo);
            //android.util.Log.d("SMS0001", "" + (intent.getExtras() == null));
            long timestamp = System.currentTimeMillis() / SECOND_IN_MILLIS;
            String receiver = smsViewModel.getOtherNumber().fixed();
            String sender = smsViewModel.getMyNumber().fixed();
            String msgData = remoteInput.getCharSequence(REMOTE_INPUT_KEY).toString();
            SMS sending = new SMS(timestamp, receiver, sender, msgData, Message.Direction.OUT);
            sending.setNeedsNotification(true);
            sending.send(a -> {
                SMSRepository.getInstance().addSMSToConversation(sending);
                SMSViewModel sentSMSVM = new SMSViewModel();
                sentSMSVM.setItem(sending);
                AsyncTask.execute(() -> sentSMSVM.showNotification(this));
            }, b -> {
//                stopSelf();
                //android.util.Log.d("SMS0001", "error " + b.toString());
            });
        }
    }


}
