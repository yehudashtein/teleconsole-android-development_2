package com.telebroad.teleconsole.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.RemoteViews;

import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.ActiveCallActivity;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.FaxOpenActivity;
import com.telebroad.teleconsole.controller.IncomingCallActivity;
import com.telebroad.teleconsole.controller.dashboard.DashboardActivity;
import com.telebroad.teleconsole.controller.SmsConversationActivity;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;
import com.telebroad.teleconsole.model.repositories.FaxRepository;
import com.telebroad.teleconsole.model.repositories.SMSRepository;
import com.telebroad.teleconsole.pjsip.AndroidAudioManager;
import com.telebroad.teleconsole.pjsip.CallGroup;
import com.telebroad.teleconsole.pjsip.CallManager;
import com.telebroad.teleconsole.pjsip.NonExistentCall;
import com.telebroad.teleconsole.pjsip.SipService;
import com.telebroad.teleconsole.viewmodels.FaxViewModel;
import com.telebroad.teleconsole.viewmodels.SMSViewModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static androidx.core.app.NotificationCompat.*;
import static com.telebroad.teleconsole.helpers.IntentHelper.MESSAGE_TIME;
import static com.telebroad.teleconsole.helpers.IntentHelper.NOTIFICATION_ID;
import static com.telebroad.teleconsole.helpers.IntentHelper.NUMBER_TO_CALL;
import static com.telebroad.teleconsole.helpers.SettingsHelper.DO_NOT_DISTURB;
import static com.telebroad.teleconsole.pjsip.SipService.EXTRA_PJSIP_ID;

public class NotificationBuilder {
    final Context context;
    private static final int FAX_SENDING = 100;
    static final int MISSED_CALL_ID = 101;
    public static int SMS_ID = 102;
    static final int NO_PERMISSION_ID = 103;
    private static final Map<String, Integer> mapNumbersToID = new HashMap<>();
    private static final int currentID = 0;
    public static final String MESSAGE_GROUP = "com.teleconsole.notifications.group.message";
    private static final String FAX_GROUP = "com.teleconsole.notifications.group.fax";
    private static final String UPDATE_GROUP = "com.teleconsole.notifications.group.update";
    private static final String ACTIVE_CALL_GROUP = "com.teleconsole.notifications.group.active.call";
    private static final String VOICEMAIL_GROUP = "com.teleconsole.notifications.group.voicemail";
    private NotificationManagerCompat notificationManager;
    private static  NotificationBuilder instance;
    public static NotificationBuilder getInstance(){
        return getInstance(AppController.getInstance());
    }

    public static NotificationBuilder getInstance(Context context){
        if (instance == null){
            instance = new NotificationBuilder(context);
        }
        return instance;
    }

    private NotificationBuilder(Context context) {
        if (context == null) {
            context = AppController.getInstance();
        }
        this.context = context;
    }

    public NotificationManagerCompat getNotificationManager() {
        if (notificationManager == null) {
            try {
                notificationManager = NotificationManagerCompat.from(context);
            } catch (NullPointerException npe) {
              //  android.util.Log.d("NPE", "context " + context);
            }
        }
        return notificationManager;
    }

    void showVoicemailNotification(String message) {
        if (!shouldSendNotification(R.string.voicemail)){
            return;
        }
        VoicemailNotification vm = new Gson().fromJson(message, VoicemailNotification.class);
        vm.showNotification(context);
    }
    public void showNoPermissionNotification(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NO_PERMISSION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        Builder builder = new Builder(context, AppController.MISSED_CALL_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.missed_call))
                .setContentText("You missed a call because you didn't grant microphone permissions.")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("You missed a call because you didn't grant microphone permissions."))
                .setPriority(PRIORITY_HIGH)
                .addAction(0, "Grant Permissions", pendingIntent)
                .setAutoCancel(true);
        getNotificationManager().notify(NO_PERMISSION_ID, builder.build());
    }
    void showMissedCallNotification(HistoryNotification historyNotification) {
        if (shouldSendNotification(R.string.missed_calls)){
            if (AppController.getInstance().getActiveActivity() instanceof DashboardActivity){
                DashboardActivity activity = (DashboardActivity) AppController.getInstance().getActiveActivity();
                if(activity.currentTab == 1){
                    TeleConsoleDatabase.getInstance(context).callHistoryDao().setIDAsNotified(historyNotification.getCallid());
                    playNewMessageSound();
                    return;
                }
            }historyNotification.showMissedCallNotification(context);
        }
    }

    private void playNewMessageSound() {
        Utils.getSingleThreadExecutor().execute(() -> {
            SoundPool sp = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            ).build();
            try {
                final AssetFileDescriptor afd = context.getAssets().openFd("new_message.mp3");
                int soundID = sp.load(afd, 1);
                sp.setOnLoadCompleteListener((soundPool, sampleId, status) ->
                        soundPool.play(soundID, 0.25f, 0.25f, 1, 0, 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    PendingIntent getOpenMessagePendingIntent(long time, Class c) {
        Intent openIntent = new Intent(context, c);
        openIntent.putExtra(MESSAGE_TIME, time);
        return getBackStackPendingIntent(openIntent);
    }

    PendingIntent getBackStackPendingIntent(Intent openIntent) {
        return getBackStackPendingIntent(context, openIntent);
    }

    public PendingIntent getBackStackPendingIntent(Intent openIntent, int requestCode) {
        return getBackStackPendingIntent(context, openIntent, requestCode);
    }

    static PendingIntent getBackStackPendingIntent(Context context, Intent openIntent, int requestCode) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(openIntent);
        return stackBuilder.getPendingIntent(requestCode, FLAG_ONE_SHOT | FLAG_IMMUTABLE);
    }

    static PendingIntent getBackStackPendingIntent(Context context, Intent openIntent) {
        return getBackStackPendingIntent(context, openIntent, 0);
    }
    public Action getCallBackAction(int id, String from) {
        return getCallBackAction(id, from, FLAG_ONE_SHOT | FLAG_IMMUTABLE);
    }

    private Action getCallBackAction(int id, String from, int flag) {
        PendingIntent callBackPendingIntent = getCallBackPendingIntent(id, from, flag);
        return new Action(R.drawable.ic_phone_notification, context.getString(R.string.call_back), callBackPendingIntent);
    }

    private PendingIntent getCallBackPendingIntent(int id, String from) {
        return getCallBackPendingIntent(id, from, FLAG_ONE_SHOT | FLAG_IMMUTABLE);
    }

    private PendingIntent getCallBackPendingIntent(int id, String from, int flag) {
        Intent callBackIntent = new Intent(context, DashboardActivity.class);
        //android.util.Log.d("Bug0016", "number sent " + from);
        callBackIntent.putExtra(NUMBER_TO_CALL, from);
        //android.util.Log.d("HistNotif02", "setting notification id = " + id);
        callBackIntent.putExtra(NOTIFICATION_ID, id);
        return PendingIntent.getActivity(context, id, callBackIntent, flag);
    }

    void showFaxNotification(FaxNotification faxNotification) {
        FaxRepository.getInstance(AppController.getInstance()).loadNewFax(faxNotification.getTime(), faxNotification.getTo());
        if (!shouldSendNotification(R.string.fax)){
            return;
        }
        FaxViewModel faxViewModel = new FaxViewModel();
        faxViewModel.setItem(faxNotification.convertToFax());
       // android.util.Log.d("FaxNotif01", faxNotification.toString());
        Builder builder = new Builder(context, AppController.FAX_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.new_fac))
                .setContentIntent(getOpenMessagePendingIntent(faxNotification.getTime(), FaxOpenActivity.class))
                .setContentText("From: " + faxViewModel.getOtherNumber())
                .setGroup(FAX_GROUP)
                .setPriority(PRIORITY_HIGH)
                .setAutoCancel(true);

        getNotificationManager().notify((int) faxNotification.getTime(), builder.build());
    }

    void showSMSNotification(SMSNotification smsNotification) {
        if (!shouldSendNotification(R.string.sms)){
            return;
        }
        SMSViewModel smsViewModel = new SMSViewModel();
        smsViewModel.setItem(smsNotification.convertToSMS());
        if (AppController.getInstance().getActiveActivity() instanceof SmsConversationActivity && !AppController.getInstance().isActiveActivityPaused()){
            SmsConversationActivity smsActivity = (SmsConversationActivity)AppController.getInstance().getActiveActivity();
            if(smsViewModel.getMyNumber().phoneNumberEquals(smsActivity.getMyNumber()) && smsViewModel.getOtherNumber().phoneNumberEquals(smsActivity.getOtherNumber())){
                SMSRepository.getInstance().loadConversationFromServer(smsNotification.getTo(), smsNotification.getFrm(), null);
                playNewMessageSound();
                return;
            }
        }
       // android.util.Log.d("DebugDB", "Loading SMS");
//        SMSRepository.getInstance().loadSMSFromServer();
        smsNotification.showNotification(context);
    }


    public void showSendingFaxNotification(boolean inProgress, boolean error) {
        Builder builder = new Builder(context, AppController.SENDING_FAX_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle("Sending Fax")
                .setGroup(UPDATE_GROUP)
                .setProgress(0, 0, inProgress);
        if (inProgress) {
            builder.setOngoing(true).setAutoCancel(false);
        } else {
            builder.setOngoing(false).setAutoCancel(true);
            if (error) {
                builder.setContentText("error");
            } else {
                builder.setContentText("Fax Sent").setOngoing(false).setAutoCancel(true);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {getNotificationManager().cancel(FAX_SENDING);
                    }, 3 * MINUTE_IN_MILLIS);
            }
        }
        getNotificationManager().notify(FAX_SENDING, builder.build());
    }


    private boolean shouldSendNotification(@StringRes int notificationType){
       // android.util.Log.d("Notification03", "checking if should send " + AppController.getAppString(notificationType));
        return !SettingsHelper.getBoolean(DO_NOT_DISTURB) && SettingsHelper.setContainsString(R.string.notifications, notificationType);
    }

    public Notification getIncomingNotification(CallGroup call) {
        Intent declineIntent = SipService.getCallCommandIntent(SipService.ACTION_DECLINE, call.getID());
        //android.util.Log.d("ANSWER " , call.getID() + "");
        Intent answerIntent = SipService.getCallCommandIntent(SipService.ACTION_ANSWER, call.getID());
        Intent fullScreenIntent = new Intent(AppController.getInstance(), IncomingCallActivity.class);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK);
        fullScreenIntent.setAction("Voip");
        fullScreenIntent.putExtra(EXTRA_PJSIP_ID, call.getID());
        RemoteViews incomingOptions = new RemoteViews(AppController.getInstance().getPackageName(), R.layout.notification_incoming_call );
        incomingOptions.setOnClickPendingIntent(R.id.declineButton, PendingIntent.getService(AppController.getInstance(), 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE));
        incomingOptions.setOnClickPendingIntent(R.id.answerButton, PendingIntent.getService(AppController.getInstance(), 2, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE));
        incomingOptions.setTextViewText(R.id.ringing_text, "Incoming Call");
        //android.util.Log.d("ANSWER " , "Before getting remote");
        String number = call.getRemoteNumber().formatted();
      //  android.util.Log.d("ANSWER " , "After getting remote");
        // By default the name should be the formatted number
        String name = number;
        // If we are not on the main thread, and we should never actually be on the main thread, but we check as an abundance of caution
        if (!Utils.isMainThread()) {
            // We can fetch the name in the background
            name = call.getRemoteNumber().getNameBackground();
        }
        String notifcationName = name;
        if (!name.equals(number)){
            notifcationName += " - " + number;
        }
        incomingOptions.setTextViewText(R.id.number_text, notifcationName);//.formatted());
        NotificationCompat.Builder builder = new Builder(AppController.getInstance(), AppController.RINGING_CHANNEL)
                .setSmallIcon(R.drawable.ic_ringing)
                .setUsesChronometer(true)
                .setStyle(new DecoratedCustomViewStyle())
                .setSound(null)
                .setCustomContentView(incomingOptions)
                .setCustomBigContentView(incomingOptions)
                .setCustomHeadsUpContentView(incomingOptions)
                .setGroup(ACTIVE_CALL_GROUP)
                .setFullScreenIntent(PendingIntent.getActivity(AppController.getInstance(), 2, fullScreenIntent,FLAG_IMMUTABLE), true)
                .setContentIntent(PendingIntent.getActivity(AppController.getInstance(), 3, fullScreenIntent, FLAG_IMMUTABLE))
                .setOngoing(true)
                .setVibrate(new long[]{0L})
                .setVisibility(VISIBILITY_PUBLIC)
                .setCategory(CATEGORY_CALL)
                .setPriority(PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false);
        Notification notificaton = builder.build();
        notificaton.headsUpContentView = notificaton.bigContentView = incomingOptions;
        PowerManager pm = (PowerManager) AppController.getInstance().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive(); // check if screen is on
        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "teleconsole:ringing call");
            wl.acquire(5000); //set your time in milliseconds
        }
        return notificaton;
    }

    public Notification getActiveCallNotification(CallGroup call){
        if (call == null || call.getID() == NonExistentCall.getInstance().getID() ){
            return null;
        }
        if(call.getCallState() == CallGroup.CallState.RINGING || call.isIncomingEarly()){
            Utils.scheduleTask(() -> CallManager.getInstance().stopForeground(call.getService()), 35 * SECOND_IN_MILLIS);
            return getIncomingNotification(call);
        }
        Intent hangupIntent = SipService.getCallCommandIntent(SipService.ACTION_HANGUP, call.getID());
        Intent cancelIntent = SipService.getCallCommandIntent(SipService.ACTION_CANCEL, call.getID());
        Intent fullScreenIntent = new Intent(AppController.getInstance(), ActiveCallActivity.class);
        fullScreenIntent.putExtra(EXTRA_PJSIP_ID, call.getID());
        boolean isSpeaker = call.getCallController().isSpeaker();
       // android.util.Log.d("SPEAKER05", "isspeaker? " + isSpeaker);
        Intent speakerIntent = SipService.getCallCommandIntent(SipService.ACTION_SPEAKER, call.getID());
        speakerIntent.putExtra(SipService.EXTRA_ON, !isSpeaker);
        Intent holdIntent = SipService.getCallCommandIntent(SipService.ACTION_HOLD, call.getID());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(AppController.getInstance(), AppController.ACTIVE_CALL_CHANNEL);
        builder = builder.setSmallIcon(R.drawable.ic_call)
                .setColor(0xFF4285F4)
                .setColorized(true)
//                .setWhen(System.currentTimeMillis() - (call.duration() * 1000))
//                .setFullScreenIntent(PendingIntent.getActivity(AppController.getInstance(), 15, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT), true)
                .setContentIntent(PendingIntent.getActivity(AppController.getInstance(), 16, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE))
                .setGroup(ACTIVE_CALL_GROUP)
                .setContentTitle(call.getRemoteNumber().formatted())
                .addAction(getAction(hangupIntent, R.string.hangup, 10));
        if (!AndroidAudioManager.getAudioManager().isBluetoothHeadsetConnected()){
            builder = builder.addAction(getAction(speakerIntent, isSpeaker ? R.string.turn_speaker_off : R.string.turn_speaker_on, 11));
        }
        switch (call.getCallState()){
            case ACTIVE:
                holdIntent.putExtra(SipService.EXTRA_ON, true);
                builder = builder.setContentText(AppController.getAppString(R.string.ongoing_call))
                        .setShowWhen(true)
                        .addAction(getAction(holdIntent, R.string.hold, 12))
                        .setUsesChronometer(true);
                break;
            case HOLD:
                holdIntent.putExtra(SipService.EXTRA_ON, false);
                builder = builder.setContentText(AppController.getAppString(R.string.call_on_hold))
                        .setShowWhen(false)
//                        .setUsesChronometer(false)
                        .addAction(getAction(holdIntent, R.string.unhold, 13));
                break;
            case CONNECTING:
                builder.setContentText(AppController.getAppString(R.string.call_connecting))
                        .setShowWhen(true)
                        .setUsesChronometer(true);
                break;
            default:
                //android.util.Log.w("CallState", "CallState " + call.getCallState());
                break;
        }
        builder.setVisibility(VISIBILITY_PUBLIC);
        return builder.build();
    }

    public Action getAction(Intent intent, @StringRes int titleRes, int requestCode) {
        return new Action.Builder(
                0,
                AppController.getAppString(titleRes),
                PendingIntent.getService(AppController.getInstance(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE)
        ).build();
    }

    public void dismissNotification(int id){
        getNotificationManager().cancel(id);
    }
}
