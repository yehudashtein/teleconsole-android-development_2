package com.telebroad.teleconsole.notification;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.google.common.base.Strings;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;

public class NotificationService extends IntentService {
    public NotificationService() {
        super("NotificationService");
    }

    public static final String MARK_ALL_MISSED_CALLS_AS_NOTIFIED = "com.telebroad.teleconsole.notification.NotificationService.mark.all.missed.calls";
    public static final String MARK_MISSED_CALLS_AS_NOTIFIED = "com.telebroad.teleconsole.notification.NotificationService.mark.missed.calls";

    public static final String EXTRA_OTHER_NUMBER = "com.telebroad.teleconsole.notification.NotificationService.other.number";
    @Override
    protected void onHandleIntent(Intent intent) {
        //android.util.Log.d("HistNotifBug", "Intent ran? " + (intent == null));
        if (intent != null) {
           // android.util.Log.d("HistNotifBug", "Action " + intent.getAction());
            final String action = Strings.nullToEmpty(intent.getAction());
            switch (action){
                case MARK_ALL_MISSED_CALLS_AS_NOTIFIED:
                    TeleConsoleDatabase.getInstance(this).callHistoryDao().setAllAsNotified();
                    break;
                case MARK_MISSED_CALLS_AS_NOTIFIED:
                    String otherNumber = intent.getStringExtra(EXTRA_OTHER_NUMBER);
                  //  android.util.Log.d("HistNotifBug", "other number " + otherNumber);
                    if (otherNumber != null) {
                        TeleConsoleDatabase.getInstance(this).callHistoryDao().setAsNotified(otherNumber);
                    }
                    break;
            }
        }
    }
}
