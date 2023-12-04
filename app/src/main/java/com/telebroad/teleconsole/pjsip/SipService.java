package com.telebroad.teleconsole.pjsip;


import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.telebroad.teleconsole.BuildConfig;
import com.telebroad.teleconsole.NetworkChangeCallBack;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.ActiveCallActivity;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.IncomingCallActivity;
import com.telebroad.teleconsole.helpers.DTMFPlayer;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.notification.NotificationBuilder;

import org.jetbrains.annotations.NotNull;
import org.pjsip.pjsua2.AudioDevInfoVector2;
import org.pjsip.pjsua2.CodecInfoVector2;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.IpChangeParam;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pj_qos_type;
import org.pjsip.pjsua2.pjsip_transport_type_e;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.telebroad.teleconsole.controller.AppController.RINGING_CHANNEL;
import static com.telebroad.teleconsole.helpers.SettingsHelper.USE_TLS;
import static com.telebroad.teleconsole.helpers.Utils.getActiveNetwork;
import static com.telebroad.teleconsole.helpers.Utils.logToFile;

public class SipService extends BackgroundService {

    public static int foregroundTry = 0;
    public static final String CALLING_SERVER = "push.telebroad.com";
    private static final String TAG = "PJSIP_TB_SERVICE";
    public static final String BASE_ACTION = "com.telebroad.teleconsole.pjsip.action.";
    public static final String BASE_EXTRA = "com.telebroad.teleconsole.pjsip.extra.";
    public static final String EXTRA_DEST = BASE_EXTRA + "dest";
    public static final String EXTRA_PJSIP_ID = BASE_EXTRA + "pjsip.id";
    public static final String EXTRA_SIP_ACCOUNT = BASE_EXTRA + "sip.account";
    public static final String EXTRA_SIP_DOMAIN = BASE_EXTRA + "sip.domain";
    public static final String EXTRA_DTMF = BASE_EXTRA + "dtmf";
    public static final String EXTRA_IGNORE_MOBILE_CALL_ACTIVE = BASE_EXTRA + "ignore.mobile.call.active";
    public static final String EXTRA_FOREGROUND = BASE_EXTRA + "foreground";
    public static final String EXTRA_FOREGROUND_TRY = BASE_EXTRA + "foreground.try";
    public static final String EXTRA_CONNECTION_ID = "com.telebroad.teleconsole.pjsip.extra.connection.id";
    public static final String EXTRA_IS_CONFERENCE = BASE_EXTRA + ".is.conference";
    static final int SERVICE_NOTIFICATION_ID = 1857424;
    static final int SIP_SERVICE_INITIALIZING_ID = 300;
    // Extra to used for things that can be turned on/off e.g. hold, speaker, record.
    public static final String EXTRA_ON = BASE_EXTRA + "on";
    public static final String ACTION_REGISTER = BASE_ACTION + "register";
    public static final String ACTION_DEREGISTER = BASE_ACTION + "deregister";
    public static final String ACTION_LOGOUT = BASE_ACTION + "logout";
    public static final String ACTION_RESTART = BASE_ACTION + "restart";
    public static final String ACTION_CALL = BASE_ACTION + "call";
    public static final String ACTION_ADD_CALL = BASE_ACTION + "add.call";
    public static final String ACTION_HANGUP = BASE_ACTION + "hangup";
    public static final String ACTION_HOLD = BASE_ACTION + "hold";
    public static final String ACTION_SPEAKER = BASE_ACTION + "speaker";
    public static final String ACTION_RECORD = BASE_ACTION + "record";
    public static final String ACTION_TRANSFER = BASE_ACTION + "transfer";
    public static final String ACTION_MUTE = BASE_ACTION + "mute";
    public static final String ACTION_DTMF = BASE_ACTION + "dtmf";
    public static final String ACTION_CONFERENCE = BASE_ACTION + "conference";
    public static final String ACTION_SPLIT = BASE_ACTION + "split";
    public static final String ACTION_UPDATE_CALL_QUALITY = BASE_ACTION + "update.call.quality";
    public static final String ACTION_CONFERENCE_ALL = BASE_ACTION + "conference.all";
    public static final String ACTION_DECLINE = BASE_ACTION + "decline";
    public static final String ACTION_ANSWER = BASE_ACTION + "answer";
    public static final String ACTION_UPDATE_NOTIFICATION = BASE_ACTION + "update.notification";
    public static final String ACTION_CANCEL = BASE_ACTION + "cancel";
    public static final String ACTION_NETWORK_CHANGED = BASE_ACTION + "network.changed";
    public static final int DEFAULT_PJSIP_ID = -1;
    public static final int CONFERENCE_PJSIP_ID = -2;
    public static final int NONEXSISTENT_PJSIP_ID = -4;
    public static final String EXTRA_FORCE = BASE_EXTRA + "force.reregister";
    private static WeakReference<SipService> instance;
    private static boolean stopping = false;

    @Nullable
    public static SipService getInstance() {
        return instance.get();
    }

    @RequiresApi(31)
    class TelephonyCallbackImpl extends TelephonyCallback implements TelephonyCallback.CallStateListener{
        @Override
        public void onCallStateChanged(int state) {
            if (state == CALL_STATE_OFFHOOK) {
                android.util.Log.d("sip", "Offhook");
                CallManager.getInstance().androidCallActive();
            }
        }
    }

    public PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            if (state == CALL_STATE_OFFHOOK) {
                CallManager.getInstance().androidCallActive();
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseCrashlytics.getInstance().log("Created, starting foreground");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startForeground();
        }
        FirebaseCrashlytics.getInstance().log("foreground started");
        logToFile("creating sip service");
        instance = new WeakReference<>(this);
        registerPhoneState();
        setupPjsip();
        NetworkChangeCallBack.getInstance().register();
//        BluetoothWrapper.getInstance().register();
//        BluetoothManager.getInstance().registerReceiver(this);
    }

    private boolean phoneStateRegistered = false;
    void registerPhoneState() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            TelephonyManager tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            tmgr.listen(this.phoneStateListener, LISTEN_CALL_STATE);
        }else if (AppController.getInstance().hasPermissions(READ_PHONE_STATE)) {
            try {
                TelephonyManager tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                tmgr.registerTelephonyCallback(getApplicationContext().getMainExecutor(), new TelephonyCallbackImpl());
            }catch (Exception e){
                Utils.logToFile(e.getMessage());
            }
        }
        phoneStateRegistered = true;
    }


    private void startForeground() {
        Notification notification;
        if (CallManager.getInstance().hasCalls() ){
            if (startForeground(CallManager.getInstance().getActiveCallGroup())) {
                return;
            }
        }
        NotificationCompat.Builder build = new NotificationCompat.Builder(AppController.getInstance(), AppController.ACTIVE_SIP_CHANNEL)
                .setSmallIcon(R.drawable.ic_sip_phone).setUsesChronometer(true).setContentText("Phone Initializing, Available For Calls");
        FirebaseCrashlytics.getInstance().log("starting foreground with backup");
        try {
            if (Build.VERSION.SDK_INT >= 30){
                startForeground(SIP_SERVICE_INITIALIZING_ID, build.build()/*, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION*/);
            }else{
                startForeground(SIP_SERVICE_INITIALIZING_ID, build.build());
            }
        }catch (Exception foregroundServiceStartNotAllowedException){
            Utils.logToFile("We do not have permission to start a foreground service");
        }
        new Timer("CancelNotif").schedule(new TimerTask() {
            @Override
            public void run() {
                CallManager.getInstance().stopForeground(SipService.this);
            }
        }, 2 * SECOND_IN_MILLIS);
        android.util.Log.d("sip", "starting");
    }

    boolean startForeground(CallGroup call) {
        if (call == NonExistentCall.getInstance()) {
            // Stop foreground through call manager, which will check if there are still any remaining calls
            Utils.logToFile(getApplicationContext(), "Trying to start foreground with nonexistent call");
            CallManager.getInstance().stopForeground(this);
            return false;
        }
        // Call is Done do not display a notification
        if (call.callStates().isDone()) {
            Utils.logToFile(getApplicationContext(), "Trying to start foreground with finished call");
            CallManager.getInstance().stopForeground(this);
            return false;
        }
        if (call == CallManager.getInstance().getActiveCallGroup() || call.callStates().isRinging()) {
            if (SipService.isThreadRegistered()) {
                startForegroundWithNotification(call);
            }else{
                enqueueJob(() -> startForegroundWithNotification(call), "Start FG W/ Notif");
            }
            return true;
        }
        return false;
    }

    private void startForegroundWithNotification(CallGroup call) {
        Notification callNotification = NotificationBuilder.getInstance().getActiveCallNotification(call);
        logToFile(getApplicationContext(), "Trying to start foreground with notification " + callNotification);
            startForeground(callNotification);

    }

    private boolean notificationsPaused(NotificationManager manager){
        if (Build.VERSION.SDK_INT >= 29){
            return manager.areNotificationsPaused();
        }
        return false;
    }
    private boolean notificationsEnabled(NotificationManager manager){
        return manager.areNotificationsEnabled();
    }


    public void startForeground(Notification notification) {
        if (notification != null) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int id = SERVICE_NOTIFICATION_ID;
            if (RINGING_CHANNEL.equals(notification.getChannelId())){
                id = SERVICE_NOTIFICATION_ID + 10;
            }
            Utils.logToFile("Starting foreground with notification " + notification  + " enabled? " + notificationsEnabled(manager) + " paused? " + notificationsPaused(manager));
            manager.notify(id, notification);
            try {
                startForeground(id, notification);
            }catch (Exception foregroundServiceStartNotAllowedException){
                Utils.logToFile("We do not have permission to start foreground services");
            }
            Utils.logToFile("Foregrond started with notification " + notification);
        } else {
            android.util.Log.e("sip", "notifciation is null");
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
       android.util.Log.d("sip", "intent");
        if (intent != null) {
            if (endpointInstance == null)
                Utils.scheduleTask(() -> handleNewIntent(intent), 1000);
            else
                handleNewIntent(intent);
        }
        return START_NOT_STICKY;
    }

    public void handleNewIntent(Intent intent){
        handleNewIntent(intent, 0);
    }

    public void handleNewIntent(@Nullable Intent intent, int retries) {
        int tries = intent == null ? -1 : intent.getIntExtra(EXTRA_FOREGROUND_TRY, -2);
        FirebaseCrashlytics.getInstance().log("new intent, action is " + intent.getAction() + ", is null " + (intent == null) + " should be foreground? " + (intent == null || intent.getBooleanExtra( EXTRA_FOREGROUND, false)) + " try #" + tries );
        FirebaseCrashlytics.getInstance().setCustomKey("Last SipService Action", intent.getAction());
        if (intent == null || intent.getBooleanExtra(EXTRA_FOREGROUND, false)){
            FirebaseCrashlytics.getInstance().log("should be foreground, calling now, try #" + tries);
                startForeground();
            FirebaseCrashlytics.getInstance().log("new intent foreground started,  #" + tries);
        }
        if (endpointInstance == null) {
            logToFile( "Endpoint instance is still null");
            if (intent != null && retries < 3) {
                Executors.newSingleThreadScheduledExecutor().schedule(() -> handleNewIntent(intent, retries + 1), 1 , TimeUnit.SECONDS);
            }else {
                CallManager.getInstance().stopForeground(this);
            }
            return;
        }
        if (intent != null && intent.getAction() != null) {
            Log.d("sip", "action " + intent.getAction());
            int id = getId(intent);
            Log.d("sip", "id " + getId(intent));
            CallGroup call = CallManager.getInstance().getCall(id);
             Log.d("sip", "call " + call);
            switch (intent.getAction()) {
                case ACTION_REGISTER:
                    boolean force = intent.getBooleanExtra(EXTRA_FORCE, true);
                    Boolean regeistered = SipManager.isRegistered.getValue();
                    boolean isUnregistered = regeistered == null || !regeistered;
                    if (force || !isUnregistered){
                        enqueueJob(() -> updateUser(force), "updating user" + (isUnregistered ? " unregistered" : "")+ (force ? " forced": "" )  );
                    }
                    break;
                case ACTION_DEREGISTER:
                    CallManager.getInstance().stopForeground(this);
                    enqueueJob(account::deregister, "Deregister", false);
                    break;
                case ACTION_LOGOUT:
                    String username = intent.getStringExtra(EXTRA_SIP_ACCOUNT);
                    String domain = intent.getStringExtra(EXTRA_SIP_DOMAIN);
                    Log.d("sip", "username " + username + " domain " + domain);
                    if (username != null && !username.isEmpty() && account != null) {
                        enqueueJob(() -> {
                            if(account != null) {
                                account.logout(username, domain);
                            }
                        }, "Logout");
                    }
                    break;
                case ACTION_CALL:
                    String dest = intent.getStringExtra(EXTRA_DEST);
                    int connectionID = intent.getIntExtra(EXTRA_CONNECTION_ID, -3);
                    if (dest != null) {
                        Log.d("sip", "has calls? " + CallManager.getInstance().hasCalls());
                        if (CallManager.getInstance().hasCalls()) {
                            secondCall(CallManager.getInstance().getActiveCallGroup(), dest, connectionID);
                        } else {
                            sipCall(dest, connectionID);
                        }
                    }
                    break;
                case ACTION_ADD_CALL:
                    String newDest = intent.getStringExtra(EXTRA_DEST);
                    int connectionID2 = intent.getIntExtra(EXTRA_CONNECTION_ID, -3);
                    secondCall(call, newDest, connectionID2);
                    break;
                case ACTION_HANGUP:
                    logToFile("Hung up");
//                    CallManager.getInstance().removeCall(call.getID());
                    enqueueJob(() -> {
                        try {
                            Log.d("sip", "hanging up call " + call);
//                            if (call == NonExistentCall.) {
//                                return;
//                            }
                            call.hangup();
                        } catch (Exception e) {
                            logToFile(e);
                            e.printStackTrace();
                        }
                    }, "hangup Job ID: " + call.getSipID());
                    CallManager.getInstance().stopForeground(this);
                    break;
                case ACTION_HOLD:
                    Log.d("sip", "enquing hold");
                    enqueueJob(() -> {
                        try {
                            call.setHold(intent.getBooleanExtra(EXTRA_ON, false));
                        } catch (Exception e) {
                            logToFile(e);
                            e.printStackTrace();
                        }
                    }, "hold Job ID: " + call.getSipID());
                    Log.d("sip", "done enquing call ");
                    break;
                case ACTION_SPEAKER:
                    if (TeleConsoleCall.useConnectionService) {
                        call.getCallController().setSpeaker(intent.getBooleanExtra(EXTRA_ON, false));
                        CallManager.getInstance().triggerUIUpdate();
                    }else{
                        setSpeaker(intent.getBooleanExtra(EXTRA_ON, false));
                    }
                case ACTION_UPDATE_NOTIFICATION:
                    if (call != null && call != NonExistentCall.getInstance()) {
                        startForeground(call);
                    }else{
                        stopForeground(true);
                    }
                    break;
                case ACTION_RECORD:
                    enqueueJob(() -> {
                        try {
                            call.record(intent.getBooleanExtra(EXTRA_ON, false));
                        } catch (Exception e) {
                            logToFile(e);
                            e.printStackTrace();
                        }
                    }, "Record Job ID: " + call.getSipID());
                    break;
                case ACTION_TRANSFER:
                    enqueueJob(() -> {
                        String transDest = intent.getStringExtra(EXTRA_DEST);
                        if (transDest != null) {
                            try {
                                call.transfer(transDest);
                            } catch (Exception e) {
                                logToFile(e);
                            }
                        }
                    }, "Transfer Job ID: " + call.getSipID());
                    break;
                case ACTION_MUTE:
                    enqueueJob(() -> {
                        try {
                            call.mute(intent.getBooleanExtra(EXTRA_ON, false));
                        } catch (Exception e) {
                            logToFile(e);
                            e.printStackTrace();
                        }
                    }, "Muting call");
                    break;
                case ACTION_DTMF:
                    String digits = intent.getStringExtra(EXTRA_DTMF);
                    if (id >= 0) {
                        enqueueJob(() -> {
                            try {
                                CallManager.getInstance().getCall(id).sendDtmf(digits);
                                DTMFPlayer.getInstance().play(digits);
                            } catch (Exception e) {
                                logToFile(e);
                                e.printStackTrace();
                            }
                        }, "DTMF Job ID: " + call.getSipID() + " digits = " + digits);
                    }
                    break;
                case ACTION_CONFERENCE:
                    if (id >= 0) {
                        enqueueJob(() -> {
                            String confDest = intent.getStringExtra(EXTRA_DEST);
                            if (confDest != null) {
                                if (account == null) {
                                    return;
                                }
                                TeleConsoleCall newCall = new TeleConsoleCall(account);
                                Runnable confRun = () -> {
//                                    CallManager.getInstance().addAllToConference();
                                    try {
                                        call.addToConference();
                                    } catch (Exception e) {
                                        logToFile(e);
                                        e.printStackTrace();
                                    }
                                    newCall.addToConference();
                                };
                                try {
                                    newCall.onMediaReady = confRun;
                                    newCall.makeCall(confDest);
                                } catch (Exception e) {
                                    logToFile(e);
                                }
                            }
                        }, "Conference Job ID: " + call.getSipID());
                        CallManager.getInstance().setLiveCall(CallManager.getInstance().getConference());
                    }
                    break;
                case ACTION_SPLIT:
                    enqueueJob(() -> {
                        if (call instanceof TeleConsoleCall) {
                            CallManager.getInstance().getConference().removeAll((TeleConsoleCall) call);
                        } else {
                            CallManager.getInstance().getConference().removeAll(null);
                        }
                    }, "Split  Job ID: " + call.getSipID());
                    CallManager.getInstance().setLiveCall(CallManager.getInstance().getConference());
                    break;
                case ACTION_RESTART:
                    enqueueJob(() -> {
                       Log.d("Test", "restarting");
                        if (!stopping) {
                            stopPJSip();
                        }
                        setupPjsip();
                    }, "Restart", true);
                    break;
                case ACTION_UPDATE_CALL_QUALITY:
                    enqueueJob(() -> {
                        try {
                            setCallQuality(getEndpointInstance(false));
                        } catch (Exception e) {
                            logToFile(e);
                            e.printStackTrace();
                        }
                    }, "Update Quality");
                    break;
                case ACTION_DECLINE:
                    enqueueJob(() -> {
                        call.decline();
                    }, "Decline call  Job ID: " + call.getSipID());
                    break;
                case ACTION_ANSWER:
                    CallGroup active = CallManager.getInstance().getActiveCallGroup();
                    if (CallManager.getInstance().hasExternalCalls()) {
                        boolean ignoreActive = intent.getBooleanExtra(EXTRA_IGNORE_MOBILE_CALL_ACTIVE, false);
                        if (!ignoreActive) {
                            IncomingCallActivity.showIncomingCall(this, call.getID(), true);
                            return;
                        }
                    }
                    this.showActiveCallActivity(active.getID());
                    if (active != NonExistentCall.getInstance() && call != active) {
                        enqueueJob(() -> active.setHold(true), "Answer Holding Job ID: " + active.getSipID());
                    }
                    enqueueJob(call::answer, "Answer Job ID: " + call.getSipID());
                    break;
                case ACTION_CANCEL:
                    if (call instanceof TeleConsoleCall) {
                        TeleConsoleCall tcCall = (TeleConsoleCall) call;
                        enqueueJob(tcCall::hangup, "Cancel Job ID: " + call.getSipID());
                    }
                    break;
                case ACTION_CONFERENCE_ALL:
                    CallManager.getInstance().addAllToConference();
                    break;
                case ACTION_NETWORK_CHANGED:
                    enqueueJob(() -> {
                                IpChangeParam ipChangeParam = new IpChangeParam();
                                try {
                                    Endpoint endpoint = getEndpointInstance(false);
                                    if (endpoint != null) {
                                        Log.e("sip", "starting to change IP");
                                        getEndpointInstance(false).handleIpChange(new IpChangeParam());
                                    }else{
                                        logToFile("No use updating the IP address if the Endpoint is not set up");
                                    }
                                } catch (Exception e) {
                                    logToFile(e);
                                    e.printStackTrace();
                                }
                            }, "Network Changed");
                    break;
            }
        }
    }

    public void setAccount(TelebroadSipAccount account){
        this.account = account;
        if (account == null){
            stopPJSip();
        }
    }

    public void secondCall(CallGroup call, String newDest, int connectionID) {
        android.util.Log.d("sip", "Making second call");
//        call.getCallController().hold(true);
        Runnable run = () -> {
            android.util.Log.d("sip", "call is " + call.getClass());
            if (call instanceof TeleConsoleCall  && call.isEarly() ){
                  android.util.Log.d("SecondCall1", "call is in instanceof " );
                TeleConsoleCall tcCall = (TeleConsoleCall) call;
                tcCall.onMediaReady = () -> {
                    android.util.Log.d("sip", "enquing job " );
                    enqueueJob(() -> {
                        android.util.Log.d("sip", "setting hold ");
                        call.setHold(true);
                        tcCall.onMediaReady = null;
                    }, "Auto hold ringing call");
                    return;
                };
            }
            enqueueJob(
                    () -> {
                        android.util.Log.d("sip", "In enqued job");
                        call.setHold(true);
                    }, "Auto hold first call " + call.getSipID()
            );
        };
        enqueueJob(() -> {
            if (newDest != null) {
                if (account == null) {
                    return;
                }
                TeleConsoleCall newCall = new TeleConsoleCall(account);
                newCall.callStates().setEarly(true);
                Utils.updateLiveData(newCall.liveCallStates(), newCall.callStates());
                try {
                    newCall.onCallStart = run;
                    newCall.makeCall(newDest, connectionID);
                } catch (Exception e) {
                    Utils.logToFile(e);
                }
            }
        }, "Second Call Job ID: " + call.getSipID());
    }

    private int getId(@NotNull Intent intent) {
        return intent.getIntExtra(EXTRA_PJSIP_ID, DEFAULT_PJSIP_ID);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        android.util.Log.d("sip", "removed");
        android.util.Log.d("sip", "removed");
        // TODO Cancel active call notifications
        // TODO Try to send
//        if (CallManager.getInstance().getCallCount() == 0) {
//            stopSelf();
//        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        android.util.Log.d("sip", "Destroying");
       android.util.Log.d("sip", "Destroying");
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            telephonyManager.listen(phoneStateListener, LISTEN_NONE);
        }
        enqueueJob(this::stopPJSip, "Stopping PJSIP", true);
        NetworkChangeCallBack.getInstance().deregister();
//        BluetoothManager.getInstance().deregisterReceiver(this);
//        BluetoothWrapper.getInstance().unregister();
        super.onDestroy();
    }

    @Override
    boolean stopped() {
        return stopping || endpointInstance == null;
    }

    private synchronized void stopPJSip() {
        if (stopped()){
            return;
        }
       android.util.Log.d("sip", "Starting to stop");
        stopping = true;
        Utils.logToFile("Stopping PJSIP");
        if (account != null) {
            Utils.logToFile("Stopping PJSIP, account is not null, deregistering");
            account.delete();
            Utils.logToFile("Stopping PJSIP, account is not null, setting account null");
            account = null;
        }
        try {
            Utils.logToFile("Stopping PJSIP, Destroying library");
            endpointInstance.libDestroy();
            Utils.logToFile("Stopping PJSIP, deleting endpoint");
            endpointInstance.delete();
            Utils.logToFile("Stopping PJSIP, setting endpoint null");
            endpointInstance = null;
        } catch (Exception e) {
            Utils.logToFile("Error stopping, " + e.getMessage());
            Utils.logToFile(e);
            endpointInstance = null;
            stopSelf();
            e.printStackTrace();
        }
        stopping = false;
        Utils.logToFile("Stopping PJSIP, done");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public long getThreadID() {
        return getThreadID(Looper.myLooper());
    }

    private long getThreadID(Looper looper) {
        return looper == null ? -1 : looper.getThread().getId();
    }

    private void setupPjsip() {
        Utils.logToFile("setting up PJSIP");
        android.util.Log.d("sip", "Setting up");
        System.loadLibrary("pjsua2");
        setupEndpoint();
    }

    String name = "main";
    private void setupEndpoint() {
        if (stopping) {
            Utils.logToFile("Library was stopping");
            Utils.scheduleTask(this::setupEndpoint, 1000);
//            AsyncTask.execute(() -> {});
//            enqueueDelayedJob(this::getEndpointInstance, 1000, "Retry to get library");
        } else {
            android.util.Log.d("sip", Thread.currentThread().getName());
            enqueueJob(this::getEndpointInstanceNonNull, "Get Endpoint Instance", true);
        }
    }

    private Endpoint endpointInstance;
    // TODO should become a list
    private TelebroadSipAccount account;
    LogWriter logWriter;

    private synchronized Endpoint getEndpointInstanceNonNull(){
        return getEndpointInstance(true);
    }
    @Nullable
    public synchronized Endpoint getEndpointInstance(boolean initializeIfNull) {
        Utils.logToFile("Getting " + endpointInstance + " initalize if null " + initializeIfNull + " is null? " + (endpointInstance == null));
        if (endpointInstance == null && initializeIfNull) {
            try {
                //android.util.Log.d("Lock", "Beginning");
                try {
                   // android.util.Log.d("Endpoint.instance", "Success");
                    endpointInstance = new Endpoint();
                } catch (Exception e) {
                    endpointInstance = new Endpoint();
                    Utils.logToFile(e);
                   // android.util.Log.d("Endpoint.instance", "Failover");
                }
               // android.util.Log.d("Test", "libCreate");
                endpointInstance.libCreate();
               // android.util.Log.d("Test", "libCreate done");
                EpConfig epConfig = new EpConfig();
                epConfig.getUaConfig().setUserAgent("Android TeleConsole : PJv" + endpointInstance.libVersion().getFull() + " App v" + BuildConfig.VERSION_CODE);
                epConfig.getLogConfig().setConsoleLevel(9);
                epConfig.getLogConfig().setLevel(9);
                epConfig.getMedConfig().setHasIoqueue(true);
                epConfig.getMedConfig().setClockRate(16000);
                epConfig.getMedConfig().setQuality(10);
                epConfig.getMedConfig().setEcOptions(1);
                epConfig.getMedConfig().setEcTailLen(200);
                epConfig.getMedConfig().setThreadCnt(2);
                Utils.logToFile(this, "Audio Stats: Frame Rate " + epConfig.getMedConfig().getClockRate() + " Ptime " + epConfig.getMedConfig().getAudioFramePtime());
                LogConfig log_cfg = epConfig.getLogConfig();
                logWriter = new LogWriter() {
                    @Override
                    public void write(LogEntry entry) {
                        try {
                            String msg = entry.getMsg();
                            if (entry.getLevel() < 5) {
                                AsyncTask.execute(() -> {
                                    logToFile(SipService.this, msg, false);
                                });
                            }
                        }catch (Exception e){
                            Utils.logToFile("Unable to log");
                        }
                    }
                };
                log_cfg.setWriter(logWriter);
                log_cfg.setDecor(log_cfg.getDecor() & ~(pj_log_decoration.PJ_LOG_HAS_CR | pj_log_decoration.PJ_LOG_HAS_NEWLINE));
                Utils.logToFile( "libInit starting");
                endpointInstance.libInit(epConfig);
//                BluetoothManager.getInstance().initBluetooth();
                Utils.logToFile( "libInit done");
                boolean useTls = SettingsHelper.getBoolean(USE_TLS, true);
                if (useTls) {
                    endpointInstance.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS, getTLSCfg());
                } else {
                    endpointInstance.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, getCfg());
                    endpointInstance.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP, getCfg());
                }
                Utils.logToFile( "lib starting");
                endpointInstance.libStart();
                setCallQuality(endpointInstance);
                Utils.logToFile( "setting capture dev to -99");
//                endpointInstance.audDevManager().setCaptureDev(-99);
//                endpointInstance.audDevManager().setPlaybackDev(-2);
                Utils.logToFile( "setting up new TB account");
                account = new TelebroadSipAccount(this);
                Utils.logToFile( "new TB account set up");
//                listenForNetworkChange();
            } catch (NullPointerException npe) {
                stopSelf();
                throw npe;
            } catch (Exception e) {
                stopSelf();
                Utils.logToFile(e.getMessage());
                Utils.logToFile(e);
               // android.util.Log.e(TAG, "Error", e);
            }
        }
       // android.util.Log.d("Lock", "Ending");
        return endpointInstance;
    }

    public static boolean isThreadRegistered(){
        if (getInstance().getEndpointInstance(false) == null){
            Utils.logToFile("Thread unregistered, endpoint is null");
            return false;
        }
        return getInstance().getEndpointInstance(false).libIsThreadRegistered();
    }
    private void setCallQuality(Endpoint endpoint) throws Exception {
        if (endpoint == null){
            return;
        }
        short PRIORITY_MAX = 254;
        short PRIORITY_DISABLED = 0;
        String qualityString = SettingsHelper.getString("quality", SipManager.CallQuality.MEDIUM.name());
        android.util.Log.d("sip", " Qualtity String " + qualityString);
        SipManager.CallQuality quality = SipManager.CallQuality.valueOf(qualityString);
        android.util.Log.d("sip", " Qualtity " + quality);
        switch (quality) {
            case LOW:
                endpoint.codecSetPriority("PCMA/8000", (PRIORITY_DISABLED));
                endpoint.codecSetPriority("PCMU/8000", (PRIORITY_DISABLED));
                endpoint.codecSetPriority("G722/16000", (PRIORITY_DISABLED));
                endpoint.codecSetPriority("ilbc/8000", (PRIORITY_DISABLED));
                break;
            case MEDIUM:
                endpoint.codecSetPriority("PCMA/8000", (short) (PRIORITY_MAX - 1));
                endpoint.codecSetPriority("PCMU/8000", (short) (PRIORITY_MAX - 2));
                endpoint.codecSetPriority("G722/16000", (PRIORITY_DISABLED));
                endpoint.codecSetPriority("ilbc/8000", (PRIORITY_DISABLED));
                break;
            case HIGH:
                endpoint.codecSetPriority("PCMA/8000", (short) (PRIORITY_MAX - 3));
                endpoint.codecSetPriority("PCMU/8000", (short) (PRIORITY_MAX - 2));
                endpoint.codecSetPriority("G722/16000", (short) (PRIORITY_MAX - 1));
                endpoint.codecSetPriority("ilbc/8000", (PRIORITY_DISABLED));
                break;
            case ILBC:
                endpoint.codecSetPriority("PCMA/8000", (PRIORITY_DISABLED));
                endpoint.codecSetPriority("PCMU/8000", (PRIORITY_DISABLED));
                endpoint.codecSetPriority("G722/16000", (PRIORITY_DISABLED));
                endpoint.codecSetPriority("ilbc/8000", (short) (PRIORITY_MAX - 1));
                break;
        }
        AudioDevInfoVector2 devList = endpoint.audDevManager().enumDev2();
        for (int i = 0; i < devList.size(); i++) {
            android.util.Log.d("sip", String.valueOf(devList.get(i).getRoutes()));
        }
        CodecInfoVector2 codecList = endpoint.codecEnum2();
        for (int i = 0; i < codecList.size(); i++) {
            android.util.Log.d("sip", codecList.get(i).getCodecId());
        }
        String[] disabledCodecs = new String[]{"speex/8000", "speex/16000", "speex/32000", "GSM/8000"};
        for (String codec : disabledCodecs) {
            endpoint.codecSetPriority(codec, (short) PRIORITY_DISABLED);
        }
        android.util.Log.d("sip", "done setting");
//        if (BluetoothManager.getInstance().isBluetoothHeadsetAvailable()) {
//            BluetoothManager.getInstance().routeAudioToBluetooth();
//            return;
//        }
    }

    private TransportConfig getCfg() {
        TransportConfig cfg = new TransportConfig();
        cfg.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
        return cfg;
    }

    private TransportConfig getTLSCfg() {
        TransportConfig cfg = new TransportConfig();
        cfg.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
        return cfg;
    }

    public void updateUser(boolean force) {
        android.util.Log.d("sip", "From updateUser");
        Utils.logToFile("registering with account " + account);
        if (account != null) {
            account.register(force);
        }else{
            Utils.logToFile("No account available");
        }
    }

    public void updateNetworkReachability() throws Exception {
        IpChangeParam ipChangeParam = new IpChangeParam();
    }

    public void sipCall(String destination, int connectionID) {
        enqueueJob(() -> {
            if (account == null) {
                Utils.logToFile("Unable to make call, no account");
                return;
            }
            TeleConsoleCall call = new TeleConsoleCall(account);
//            call.setConnectionID(connectionID);
            android.util.Log.d("sip", " service calling id " + call.getId());
            try {
                if (account.isRegistered()) {
                    call.makeCall(destination, connectionID);
                } else {
                    account.onRegistrationOK = () -> {
                        enqueueJob(() -> {
                            try {
                                call.makeCall(destination, connectionID);
                            } catch (Exception e) {
                                Utils.logToFile(e);
                                e.printStackTrace();
                            }
                        }, "Call After Registration");
                        account.onRegistrationOK = null;
                    };
                   android.util.Log.d("sip", "From sip Call");
                    account.register();
                }
            } catch (Exception e) {
                // TODO show normal error
                e.printStackTrace();
                Utils.logToFile(e);
            }
        }, "Make call");
    }

    public void split(TeleConsoleCall callToKeep) {
        CallManager.getInstance().split(callToKeep);
    }

    void showActiveCallActivity(int id) {
        startForeground(CallManager.getInstance().getCall(id));
        Intent intent = new Intent(this, ActiveCallActivity.class);
        intent.putExtra(EXTRA_PJSIP_ID, id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Utils.logToFile("Starting active call activity");
        startActivity(intent);
    }

    static Intent getCommandIntent(String action) {
        return getCommandIntent(AppController.getInstance(), action);
    }

    static Intent getCommandIntent(Context context, String action) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(action);
        return intent;
    }

    static Intent getCallCommandIntent(Context context, String action, int id) {
        Intent intent = getCommandIntent(context, action);
        intent.putExtra(EXTRA_PJSIP_ID, id);
        return intent;
    }

    public static Intent getCallCommandIntent(String action, int id) {
        Intent intent = getCommandIntent(AppController.getInstance(), action);
        intent.putExtra(EXTRA_PJSIP_ID, id);
        return intent;
    }

    public static Intent getOnCommandIntent(String action, int id) {
        Intent intent = getCommandIntent(AppController.getInstance(), action);
        intent.putExtra(EXTRA_PJSIP_ID, id);
        return intent;
    }

    void setSpeaker(boolean on) {
        if (on){
            AndroidAudioManager.getAudioManager().routeAudioToSpeaker();
        }else{
            AndroidAudioManager.getAudioManager().routeToBluetoothOrEarpiece();
        }
    }

    @Deprecated
    public static boolean isSpeaker() {
        AudioManager audioManager = (AudioManager) AppController.getInstance().getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn();
    }

    static void addCommand(Intent intent) {
        if (AppController.getInstance() != null) {
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addCommand(AppController.getInstance(), intent);
            //}
        }
    }

    static void addCommand(Context context, Intent intent) {
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            FirebaseCrashlytics.getInstance().log( "Lifecycle state is " + ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name());
            try {
                context.startService(intent);
            }catch (IllegalStateException ise){
                intent.putExtra(SipService.EXTRA_FOREGROUND, true);
                intent.putExtra(SipService.EXTRA_FOREGROUND_TRY, foregroundTry++);
                ContextCompat.startForegroundService(context, intent);
            }
        }else{
            intent.putExtra(SipService.EXTRA_FOREGROUND, true);
            intent.putExtra(SipService.EXTRA_FOREGROUND_TRY, foregroundTry++);
            FirebaseCrashlytics.getInstance().log("Starting foreground SipService 821 try #" + foregroundTry );
            try{
                ContextCompat.startForegroundService(context, intent);
            }catch (Exception foregroundServiceStartNotAllowedException){
                Utils.logToFile("Start Foreground Service not allowed");
            }
        }
    }

    // Helper for simple intents without any inputs
    static void addCommand(String action) {
        addCommand(getCommandIntent(action));
    }
    int mLastNetwork;
    public void listenForNetworkChange(){
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectivityManager == null || mConnectivityManager.getActiveNetworkInfo() == null)
            return;
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        Network activeNetwork = getActiveNetwork(mConnectivityManager, networkInfo);
        android.util.Log.d("sip", "is connected " + (networkInfo != null && networkInfo.isConnected()));
        if (networkInfo == null || !networkInfo.isConnected()) {
            android.util.Log.d("sip", "Disconnected");
        } else if (networkInfo.isConnected() && activeNetwork != null) {
            int currentNetwork = activeNetwork.hashCode();
            android.util.Log.d("sip", "current Network = " + currentNetwork + " last network = " + mLastNetwork);
            if (currentNetwork != mLastNetwork) {
                //if kind of network has changed, we need to notify network_reachable(false) to make sure all current connections are destroyed.
                //they will be re-created during setNetworkReachable(true).
                addCommand(ACTION_NETWORK_CHANGED);
            }
            mLastNetwork = currentNetwork;
        }
    }

    public boolean isAccountNull() {
        return account == null;
    }
}
