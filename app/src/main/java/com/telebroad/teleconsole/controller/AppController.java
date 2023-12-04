package com.telebroad.teleconsole.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;

import android.os.Handler;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.google.firebase.storage.FirebaseStorage;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.NewCrashHandlerContentProvider;
import com.telebroad.teleconsole.helpers.Utils;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import java.lang.ref.WeakReference;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.app.NotificationManager.IMPORTANCE_MIN;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by yser on 3/16/2018.
 */

public class AppController extends Application implements LifecycleObserver {

    public static final String VOICEMAIL_CHANNEL = "New Voicemail Notifications";
    public static final String ACTIVE_CALL_CHANNEL = "Active Call Notification";
    public static final String ACTIVE_SIP_CHANNEL = "SIP Active Notification";
    public static final String MISSED_CALL_CHANNEL = "Missed Call Notification";
    public static final String SMS_CHANNEL = "New SMS Notifications";
    public static final String SENDING_FAX_CHANNEL = "Sending Fax Notification";
    public static final String FAX_CHANNEL = "New Fax Notification";
    public static final String ORIG_RINGING_CHANNEL = "Ringing Notifications";
    public static final String RINGING_CHANNEL = "Ringing Notifications_v2";
    public static final String CHAT_CHANNEL = "CHAT CHANNEL";
    public static final String VOICEMAIL_PLAYING_CHANNEL = "Voicemail Playing";
    private static final String TAG = "TeleConsole AppCon";
    private RequestQueue requestQueue;
    // Using weak reference to prevent memory leaks
    private static WeakReference<AppController> weakAppController;
    private Activity activeActivity;
    private Activity currentActivity;
    private boolean isActiveActivityPaused;
    public static FirebaseStorage firebaseStorage;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // initialize the singleton
        weakAppController = new WeakReference<>(this);
        createNotificationChannels();
        AndroidNetworking.initialize(getApplicationContext());
        EmojiManager.install(new GoogleEmojiProvider());
      //  android.util.Log.d("BF", "Android Version " + Build.VERSION.SDK_INT  + " App version " + BuildConfig.VERSION_CODE + " Version name " + BuildConfig.VERSION_NAME);
//        Bugfender.enableLogcatLogging();
        firebaseStorage = FirebaseStorage.getInstance();
        //EmojiCompat.init(new BundledEmojiCompatConfig(this));
        NewCrashHandlerContentProvider.initializeAfterFirebaseContentProvider();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                currentActivity = activity;
                //android.util.Log.d("Activity01", activity + " Resumed");
            }
            @Override
            public void onActivityStarted(Activity activity) {
            }
            @Override
            public void onActivityResumed(Activity activity) {
                activeActivity = activity;
                isActiveActivityPaused = false;
                //android.util.Log.d("Activity01", activity + " Resumed");
            }
            @Override
            public void onActivityPaused(Activity activity) {
                if (activeActivity == activity){
                    isActiveActivityPaused = true;
                }
            }
            @Override
            public void onActivityStopped(Activity activity) {
                if (activeActivity == activity){
                    Utils.deleteLogFiles();
                }
            }
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
            @Override
            public void onActivityDestroyed(Activity activity) {
               // android.util.Log.d("Activity01", activity + " Destroyed? " + (activeActivity == activity) );
                if (activeActivity == activity){
                    activeActivity = null;
                }
            }
        });
    }

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//        SDK.init(this, base, options -> {options.setAppID("ikek6u/sandbox");});
//    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized AppController getInstance() {
        return weakAppController.get();
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    public static Handler getMainHandler(){
        return new Handler(getInstance().getMainLooper());
    }
    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }

    public boolean hasExternalStoragePermissions(){
        return hasPermissions(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);
    }
    public boolean hasPermissions(String... permissions){
        for(String permission : permissions){
            if (ContextCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    private void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel(VOICEMAIL_CHANNEL, "New Voicemail Notifications");
            createChannel(ACTIVE_CALL_CHANNEL, R.string.notification_in_call_title, R.string.notification_in_call_description, IMPORTANCE_DEFAULT, true);
            createChannel(SMS_CHANNEL, "SMS Notifications");
            createChannel(MISSED_CALL_CHANNEL, "Missed call notifications");
            createChannel(FAX_CHANNEL, "Fax Notifications");
            createChannel(CHAT_CHANNEL, "Chat Notifications");
            createChannel(SENDING_FAX_CHANNEL, "Fax in progress", IMPORTANCE_DEFAULT, true);
            createChannel(ACTIVE_SIP_CHANNEL, "Sip Status", IMPORTANCE_MIN, true);
            createChannel(VOICEMAIL_PLAYING_CHANNEL, "Voicemail Playing", IMPORTANCE_DEFAULT, true);
            createRingingChannel();
        }
    }

    @TargetApi(26)
    private void createRingingChannel(){
        NotificationChannel channel = new NotificationChannel(RINGING_CHANNEL, ORIG_RINGING_CHANNEL, IMPORTANCE_HIGH);
        channel.setDescription("Ringing notifications");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager == null){
            return;
        }
        channel.enableVibration(false);
        channel.setSound(null, null);

//        channel.setSound(Uri.parse("android.resource://com.telebroad.teleconsole/raw/toy_mono"), new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .setLegacyStreamType(STREAM_RING)
//                .build());
        notificationManager.createNotificationChannel(channel);
    }
    @TargetApi(26)
    private void createChannel(String name, String description){
        createChannel(name, name, description, IMPORTANCE_HIGH, false);
    }
    @TargetApi(26)
    private void createChannel(String id, String name, String description){
        createChannel(id, name, description, IMPORTANCE_HIGH, false);
    }

    @TargetApi(26)
    private void createChannel(String name, String description, int importance, boolean silent){
        createChannel(name, name, description, importance, silent);
    }

    @TargetApi(26)
    private void createChannel(String id, @StringRes int name,  @StringRes int description, int importance, boolean silent){
        createChannel(id, getAppString(name), getAppString(description), importance, silent);
    }

    @TargetApi(26)
    private void createChannel(String id, String name, String description, int importance, boolean silent) {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager == null){
            return;
        }
        if (silent){
            channel.enableVibration(false);
            channel.setSound(null, null);
        }
        notificationManager.createNotificationChannel(channel);
    }

    public static String getAppString(@StringRes int stringResource, Object... args){
        return getInstance().getString(stringResource, args);
    }
    public Activity getActiveActivity() {
        return activeActivity;
    }
    public Activity getCurrentActivity() {
        return currentActivity;
    }
    public boolean isActiveActivityPaused() {
        return activeActivity == null || isActiveActivityPaused;
    }


}
