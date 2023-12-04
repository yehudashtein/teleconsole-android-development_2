package com.telebroad.teleconsole.pjsip;

import android.os.AsyncTask;

import com.android.volley.Request;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
//import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.notification.NotificationBuilder;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStartedParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.SipHeader;
import org.pjsip.pjsua2.pj_qos_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_call_hold_type;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.telebroad.teleconsole.helpers.SettingsHelper.DIRECT_CALLS_ONLY;
import static com.telebroad.teleconsole.helpers.SettingsHelper.DO_NOT_DISTURB;
import static com.telebroad.teleconsole.helpers.SettingsHelper.SIP_PASSWORD;
import static com.telebroad.teleconsole.helpers.SettingsHelper.SIP_USERNAME;
import static com.telebroad.teleconsole.helpers.SettingsHelper.USE_TLS;
import static com.telebroad.teleconsole.helpers.SettingsHelper.getBoolean;
import static com.telebroad.teleconsole.helpers.SettingsHelper.getSipDomain;
import static com.telebroad.teleconsole.helpers.SettingsHelper.getString;
import static org.pjsip.pjsua2.pj_constants_.PJ_FALSE;

public class TelebroadSipAccount extends Account {

    private TeleConsoleCall call;
    Runnable onRegistrationOK;
    private CallManager callManager = CallManager.getInstance();
    private Timer timerCheck = new Timer();
    private boolean loggedOut = false;


    public TelebroadSipAccount(SipService service) throws Exception {
            AccountConfig acfg = getAccountConfig();
            SipHeader mPushHeader = new SipHeader();
            try {
                String token = Tasks.await(FirebaseMessaging.getInstance().getToken());
                mPushHeader.setHName("X-MPUSH");
                mPushHeader.setHValue("a:com.telebroad.teleconsole:" + token);
            }catch (Exception e){
                Utils.logToFile("Unable to get firebase token");
            }
            acfg.getRegConfig().getHeaders().add(mPushHeader);
            try {
                create(acfg);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    private AccountConfig getAccountConfig() {
        AccountConfig acfg = new AccountConfig();
        boolean useTls = SettingsHelper.getBoolean(USE_TLS, true);
       // android.util.Log.d("SIPDOMAIN", " domain is " + getSipDomain());
        if (useTls) {
            acfg.getSipConfig().getProxies().add("sip:" + getSipDomain()+ ";hide;transport=tls");
        }
        String username = getString(SIP_USERNAME, ""), password = getString(SIP_PASSWORD,""), domain = getSipDomain();
        acfg.setIdUri("sip:" + username + "@" + domain);
        acfg.getRegConfig().setRegistrarUri("sip:" + domain);
        acfg.getRegConfig().setDropCallsOnFail(false);
        acfg.getCallConfig().setHoldType(pjsua_call_hold_type.PJSUA_CALL_HOLD_TYPE_RFC3264);
        acfg.getNatConfig().setViaRewriteUse(PJ_FALSE);
        acfg.getNatConfig().setContactRewriteUse(PJ_FALSE);
        acfg.getRegConfig().setTimeoutSec(3);


        AuthCredInfo cred = new AuthCredInfo("digest", AppController.getAppString(R.string.tb_reg), username, 0, password);

        acfg.getSipConfig().getAuthCreds().add(cred);

        acfg.getRegConfig().setTimeoutSec(180);
        acfg.getMediaConfig().getTransportConfig().setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
        SipHeader piidHeader = new SipHeader();
        piidHeader.setHName("X-PIID");
        String md5 = Utils.md5(username + ":" + AppController.getAppString(R.string.tb_reg) + ":" + password);
        piidHeader.setHValue(md5);
        acfg.getRegConfig().getHeaders().add(piidHeader);
        return acfg;
    }

    boolean isRegistered()  {
        try {
            return swigCMemOwn && isValid() && getInfo().getRegIsActive() && getInfo().getRegStatus() == pjsip_status_code.PJSIP_SC_OK;
        } catch (Exception e) {
            Utils.logToFile(e);
            e.printStackTrace();
            return false;
        }
    }

    public void register(){
        register(false);
    }

    public void register(boolean force) {
        loggedOut = false;
      //  android.util.Log.w("PJSIP_REG", "Registering ");
        try {
            if ( (isRegistered() && !force) || CallManager.getInstance().hasCalls() ){
                return;
            }
            Utils.logToFile("Setting reregistration");
            getService().enqueueJob(() -> {
                try {
                    if (swigCMemOwn) {
                        Utils.logToFile("Setting reregistration");
                        setRegistration(true);
                        Utils.logToFile(" reregistration set");
                    }else{
                        Utils.logToFile(" Registration not tried, because we don't own the memory");
                    }
                } catch (Exception e) {
                   // android.util.Log.e("PJSIP Error", "message of exception " + e.getMessage());
    //                if (e.getMessage().contains("ETIMEDOUT"))

                    Utils.logToFile(e);
                    e.printStackTrace();

                    if (e.getMessage().contains("PJ_ETIMEDOUT") || e.getMessage().contains("PJ_ENOMEM")){
                       // android.util.Log.e("PJSIP_SERVICE", "Stop");
                        Utils.logToFile("Error registering " + e.getMessage());
                        getService().stopSelf();
                    }else{
                        Utils.logToFile("register crashed with message " + e.getMessage());
                    }
                }
            }, "Account_Registering");
        } catch (Exception e) {
            Utils.logToFile(e);
            e.printStackTrace();
        }
    }

    public void deregister() {
        getService().enqueueJob(() -> {
            try {
                setRegistration(false);
            } catch (Exception e) {
               // android.util.Log.e("PJSIP Error", "message of exception " + e.getMessage());
                Utils.logToFile(e);
                e.printStackTrace();

                if (e.getMessage().contains("PJ_ETIMEDOUT")){
                    //android.util.Log.e("PJSIP_SERVICE", "Stop");
                    getService().stopSelf();
                }
            }
        }, "Account Deregistering");
    }

    public void logout(String username, String domain) {
        AccountConfig acfg = getAccountConfig();
        acfg.setIdUri("sip:" + username + "@" + domain);
        SipHeader mPushHeader = new SipHeader();
        mPushHeader.setHName("X-MPUSH");
        mPushHeader.setHValue("a:com.telebroad.teleconsole:null");
//        acfg.getRegConfig().setTimeoutSec(0);
        acfg.getRegConfig().getHeaders().add(mPushHeader);
        getService().enqueueJob(() -> {
        try {
            //android.util.Log.d("PJSIP_LOGOUT", "Logging out");
            loggedOut = true;
            if (isValid()) {
                modify(acfg);
            }
//            setRegistration(false);
        } catch (Exception e) {
            //android.util.Log.d("PJSIP_LOGOUT", "Crashed Logging out " + e.getMessage());

            Utils.logToFile(e);
            e.printStackTrace();
        }
        }, "Logging out");
//        deregister();
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        boolean isOK = prm.getCode() == pjsip_status_code.PJSIP_SC_OK;
        if (prm.getCode() == pjsip_status_code.PJSIP_SC_SERVICE_UNAVAILABLE){

        }
        getService().enqueueJob(() -> handleRegState(isOK), "Handling Reg State");
    }

    @Override
    public void onRegStarted(OnRegStartedParam prm) {
        //android.util.Log.d("PJSIP_LOGOUT", "logged out? " + loggedOut);
        if (loggedOut) {
//            prm.setRenew(false);
        }
      //  android.util.Log.d("PJSIP_LOGOUT", "renew? " + prm.getRenew());
        super.onRegStarted(prm);
    }

    public void handleRegState(boolean isOK) {
        try {
            FirebaseCrashlytics.getInstance().log("Starting Registration");
            boolean registered = swigCMemOwn && isOK && getInfo().getRegIsActive();
          //  android.util.Log.d("PJSIP_REG", "registered? " + registered );
            Utils.updateLiveData(SipManager.isRegistered, registered);
            if (registered){
                if (onRegistrationOK != null){
                    //android.util.Log.d("PJSIP_REG", "on registration ok running "  );
                    onRegistrationOK.run();
                    onRegistrationOK = null;
                }
                //android.util.Log.d("PJSIP_REG", "on registration ok not needed "  );
            }else {
                FirebaseCrashlytics.getInstance().log("Not registered");
                if (!CallManager.getInstance().hasCalls() && AppController.getInstance().isActiveActivityPaused()){
                    timerCheck.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            FirebaseCrashlytics.getInstance().log("Time task starting");
                            try {
                                if (!swigCMemOwn || (!getInfo().getRegIsActive() && !CallManager.getInstance().hasCalls() && AppController.getInstance().isActiveActivityPaused())){
                                    getService().stopSelf();
                                }
                                FirebaseCrashlytics.getInstance().log("Time task successful");
                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().log("Time task crashed");
                                Utils.logToFile(e);
                                e.printStackTrace();
                            }
                        }
                    }, 30 * SECOND_IN_MILLIS);
                }else {
                   // android.util.Log.d("Account_Registering", "From on Reg state");
                    if (!loggedOut) {
                        getService().enqueueJob(this::register, "Retrying Register");
                    }
                }
            }
        } catch (Exception e) {
           // android.util.Log.d("PJSIP_REG", "registered throws an error" );
            Utils.logToFile("Registered throws error " + e.getMessage() );
            FirebaseCrashlytics.getInstance().log("Crashed " + e.getMessage());
            Utils.logToFile(e);
            e.printStackTrace();
        }
        FirebaseCrashlytics.getInstance().log("Finished onHandleReg");
    }


    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
       // android.util.Log.d("Call", "Yay Incoming call " + prm.getRdata().getInfo());
//        getService().enqueueJob(() -> handleIncomingCall(prm), "Incoming Call");
        handleIncomingCall(prm);
        //super.onIncomingCall(prm);
    }

    public void handleIncomingCall(OnIncomingCallParam prm) {
        call = new TeleConsoleCall(this, prm.getCallId());
        call.setIncoming(true);
        call.callStates().setEarly(false);
        Utils.updateLiveData(call.liveCallStates(), call.callStates());
       // android.util.Log.d("PJSIPConnectionService", "adding call to manager with PJ_ID " + prm.getCallId());
//        if (callManager.hasSipID(call.getSipID())){
//            Utils.logToFile("Incoming call, found double");
//            android.util.Log.d("DUPLICATE", "SIP ID already exists");
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && TeleConsoleCall.useConnectionService) {
//                call.getCallController().decline();
//            }else {
//                getService().enqueueJob(call::replyBusy, "Call exists, reply busy Job ID: " + call.getSipID());
//            }
//            return;
//        }


//        call.callStates().setRinging(true);
//        Utils.updateLiveData(call.liveCallStates(), call.callStates());

        CallManager.getInstance().addCall(call);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && TeleConsoleCall.useConnectionService) {
//            TelecomManager manager = (TelecomManager) AppController.getInstance().getSystemService(Context.TELECOM_SERVICE);
//            Bundle bundle = PJSIPManager.getSipCallBundle();
//            bundle.putInt(EXTRA_PJSIP_ID, prm.getCallId());
//            android.util.Log.d("PJSIPConnectionService", ""+manager.isIncomingCallPermitted(PJSIPManager.accountHandle));
//            manager.addNewIncomingCall(PJSIPManager.accountHandle, bundle);
//
//        }else {
//            call.getService().enqueueJob(() -> {
//                try {
////                    call.ring();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }, "Ringing call");
//        }

       // android.util.Log.d("PJSIPConnectionService", "what's going on?");
        try {
            if (callManager.getCallCount() > 2){
                call.getCallController().decline();
//                getService().enqueueJob(call::replyBusy, "Too many calls, reply busy");
                return;
            }

          //  android.util.Log.d("DUPLICATE", "Checking for doubles");

            String callUsername = call.getInfo().getLocalUri();
            if (call != null) {
                call.getServerID(prm.getRdata().getWholeMsg());
            }
            if (isDND() || Utils.isPhoneDND() || !isLoggedIn(callUsername)) {
                Utils.logToFile("Incoming call DND, isDnd()? " + isDND() +  " phone DND? " + Utils.isPhoneDND() + " is Logged in? "  + isLoggedIn(callUsername));
               // android.util.Log.d("PJSIP_CALL","First DND is dnd " + isDND() + " phone dnd " + Utils.isPhoneDND()  );
                call.getCallController().decline();
//                getService().enqueueJob(call::replyBusy, "First DND, reply Busy");
            }else if (!AppController.getInstance().hasPermissions(RECORD_AUDIO)){
                Utils.logToFile("Incoming call DND,  No record permissions");
//                getService().enqueueJob(call::replyBusy, "Rejecting because no record permissions");
                call.getCallController().decline();
                NotificationBuilder.getInstance().showNoPermissionNotification();
            } else if (getBoolean(DIRECT_CALLS_ONLY, false)) {
                Utils.logToFile("Incoming call Direct Calls, YIKES!!!");
                String sipMessage = prm.getRdata().getWholeMsg();
                AsyncTask.execute(() -> {
                    if (call != null) {
                        checkDirectCallDND(call.getServerID(sipMessage));
                    }
                });
            }else {
                Utils.logToFile("It's not on DND Ringing");
;
                call.getCallController().ring();

            }
        } catch (Exception e) {
            Utils.logToFile(e);
            e.printStackTrace();
        }
    }

    private void checkDirectCallDND(String serverID) {

        Utils.logToFile("Incoming call Direct Calls, Sending off request");
        URLHelper.request(Request.Method.GET, URLHelper.RINGING_URL, new HashMap<>(), true, result -> {
            Utils.logToFile("Incoming call Direct Calls, result " + (result != null ? result.toString() : " no result ") );
            JsonArray array = result == null? new JsonArray() : result.getAsJsonArray();
            JsonObject serverCall = null;
            if (serverID == null && array.size() > 0) {
                Utils.logToFile("Incoming call Direct Calls, guessing server because server call id is null. calls " + array.size() );
                serverCall = array.get(0).getAsJsonObject();
            } else {
                for (int i = 0; i < array.size(); i++) {
                    JsonObject callObj = array.get(i).getAsJsonObject();
                    if (callObj.get("uniqueid").getAsString().equals(serverID)) {
                        serverCall = callObj;
                        break;
                    }
                }
            }
            try {
                Utils.logToFile("Incoming call Direct Calls, trying with server call " + (serverCall == null ? "null" : serverCall.toString()) );
                if (serverCall != null && serverCall.get("is_owner") != null && serverCall.get("is_owner").getAsString().equals("0")
                        && serverCall.get("ptransferred") != null && serverCall.get("ptransferred").getAsString().equals("0")
                ) {

                    Utils.logToFile("Incoming call Direct Calls, Not a direct call, Replying busy"  );
                   // android.util.Log.d("PJSIP_CALL","Direct Calls DND" );

                   // android.util.Log.d("DC", "Replying busy" );
                    call.getCallController().decline();
//                    if (getService() != null)
//                        getService().enqueueJob(() -> {
//                        try {
//                            call.replyBusy();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }, "Not logged in, Busy");
                    return;
                } else {

                    Utils.logToFile("Incoming call Direct Calls, Direct call, We can Ring"  );
                    directCallRing();
                }
            } catch (Exception e) {
                Utils.logToFile(e);
                e.printStackTrace();
            }
            //android.util.Log.d("RINGING01", "call id " + serverID);
        }, error -> {
            directCallRing();
        });
    }

    private void directCallRing() {
        if (getService() != null) {
            getService().enqueueJob(() -> {
                try {
                    if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_INCOMING) {
                        Utils.logToFile("Incoming call Direct Calls, Direct call, Finally Ringing");
                        call.getCallController().ring();
                    } else if (call.getInfo().getState() != pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                        Utils.logToFile("Incoming call already disconnecte, don't do anything");
                    } else {
                        Utils.logToFile("Incoming call Direct Calls, All this HTTP mixing with SIP nonsense, made us miss the call :-(");
                    }
                } catch (Exception e) {
                    Utils.logToFile(e);
                    e.printStackTrace();
                }
            }, "Ringing");
        }
    }

    private String extractServerID(String wholeMessage) {
        String headersPart = wholeMessage.split("\n\n")[0];
        String[] headers = headersPart.split("\n");
        HashMap<String, String> headersMap = new HashMap<>();
        for (String header : headers) {
            String[] parsed = header.split(":");
            if (parsed.length >= 2) {
                headersMap.put(parsed[0].trim(), parsed[1].trim());
            }
        }
        return headersMap.get("X-Enswitch-Uniqueid");
    }

    public boolean isDND() {
        return SettingsHelper.getBoolean(DO_NOT_DISTURB, false);
    }

    private boolean isLoggedIn(String username) {
        String domain = getSipDomain();
        String loggedIn = "sip:" + getString(SIP_USERNAME) + "@" + domain;
       // android.util.Log.d("PJSIP CALL", "username " + username + " logged in " + loggedIn);
        return username != null && username.contains(getString(SIP_USERNAME));
    }

    @Override
    public synchronized void delete() {
        timerCheck.cancel();
        super.delete();
        SipService.getInstance().setAccount(null);
    }

    @Nullable
    public SipService getService() {
        return SipService.getInstance();
    }

    public void removeCall(TeleConsoleCall teleConsoleCall) {
        if (call == teleConsoleCall){
            call = null;
        }
    }
}
