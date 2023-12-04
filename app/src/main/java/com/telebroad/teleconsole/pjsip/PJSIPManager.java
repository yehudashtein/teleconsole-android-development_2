package com.telebroad.teleconsole.pjsip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.Utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static android.Manifest.permission.CALL_PHONE;
import static android.content.Context.TELECOM_SERVICE;
import static android.telecom.PhoneAccount.CAPABILITY_SELF_MANAGED;
import static android.telecom.TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS;
import static android.telecom.TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE;
import static android.telecom.TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE;
import static com.telebroad.teleconsole.pjsip.SipService.EXTRA_FORCE;

public class PJSIPManager implements SipManager<TeleConsoleCall> {

    private static final String PHONE_ACCOUNT_LABEL = "com.telebroad.teleconsole.PJSIP";
    private static PJSIPManager instance = new PJSIPManager();

    private TelecomManager telecomManager;
    public static PhoneAccountHandle accountHandle;

    @NonNull
    public static PJSIPManager getInstance() {
        if (instance == null) {
            Utils.logToFile("get new manager");
            instance = new PJSIPManager();
        } else {
            Utils.logToFile("using old manager");
        }
        return instance;
    }

    private PJSIPManager(){
        telecomManager = (TelecomManager) AppController.getInstance().getSystemService(TELECOM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && TeleConsoleCall.useConnectionService) {
            createAccount();
        }
    }
    @Override
    public void updateUser(boolean force) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_REGISTER);
        intent.putExtra(EXTRA_FORCE, force);
        SipService.addCommand(intent);
        Utils.logToFile("Updating user");
    }

    @Override
    public void restartSip() {
        SipService.addCommand(SipService.ACTION_RESTART);
    }

    private String networkHostAddress;

    @Override
    public void updateNetworkReachability() {
        try {
            List<NetworkInterface> list = Collections.list(NetworkInterface.getNetworkInterfaces());
            NetworkInterface network = null;
            for (NetworkInterface networkInterface : list) {
                if (!networkInterface.isLoopback()) {
                    network = networkInterface;
                    break;
                }
            }
            if (network == null) {
                return;
            }
            Enumeration<InetAddress> networkInetAddresses = network.getInetAddresses();
            while (networkInetAddresses.hasMoreElements()) {
                InetAddress inetAddress = network.getInetAddresses().nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    String hostAddress = inetAddress.getHostAddress();
                    if (hostAddress != null && !hostAddress.equals(networkHostAddress)) {
//                        if (networkHostAddress != null) {
//                            SipService.addCommand(ACTION_NETWORK_CHANGED);
//                        }
                        networkHostAddress = hostAddress;
                    }
                    break;
                }
            }
        } catch (Exception e) {

            Utils.logToFile(e);
            e.printStackTrace();
        }
    }

    @Override
    @Deprecated
    public void hangup() {
        CallGroup activeCall = CallManager.getInstance().getActiveCallGroup();
        hangup(activeCall.getID());
    }

    @Override
    @Deprecated
    public void mute(boolean on) {
        mute(CallManager.getInstance().getActiveCallID(), on);
    }

    @Deprecated
    public void hangup(int id) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_HANGUP);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        SipService.addCommand(intent);
    }

    @Override
    public void sipCall(String destination, Activity currentActivity) {
        Toast.makeText(currentActivity, R.string.calling, Toast.LENGTH_SHORT).show();
        Utils.logToFile("Call Start Toast shown");
        sipCall(destination);
    }

    @Override
    @Deprecated
    public void answer(int id) {
        SipService.addCommand(SipService.getCallCommandIntent(SipService.ACTION_ANSWER, id));
    }

    @Override
    @Deprecated
    public void answer(int id, boolean ignoreActive) {
        Intent answerIntent = SipService.getCallCommandIntent(SipService.ACTION_ANSWER, id);
        answerIntent.putExtra(SipService.EXTRA_IGNORE_MOBILE_CALL_ACTIVE, ignoreActive);
        AppController.getInstance().startService(answerIntent);
    }

    @Deprecated
    public void mute(int id, boolean on) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_MUTE);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        intent.putExtra(SipService.EXTRA_ON, on);
        SipService.addCommand(intent);
    }


    @SuppressLint("MissingPermission")
    public void sipCall(String destination) {
        try {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && TeleConsoleCall.useConnectionService) {
                if (AppController.getInstance().hasPermissions(CALL_PHONE)) {
                    //android.util.Log.d("PJSIPConnectionService", "placing call");
                    telecomManager.placeCall(Uri.fromParts("tel", destination, null), getSipCallBundle());
                }
            }else{
                sendCall(destination);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createAccount() {
        ComponentName connectionServiceName = new ComponentName(AppController.getInstance(), PJSIPConnectionService.class);
        PhoneAccountHandle accountHandle = new PhoneAccountHandle(connectionServiceName, PHONE_ACCOUNT_LABEL, UserHandle.getUserHandleForUid(android.os.Process.myUid()));
//        try {
            PhoneAccount phoneAccount = telecomManager.getPhoneAccount(accountHandle);
            if (phoneAccount == null || (phoneAccount.getCapabilities() & CAPABILITY_SELF_MANAGED) == 0) {
              //  android.util.Log.d("Connection", "Registering");
                phoneAccount = PhoneAccount.builder(accountHandle, PHONE_ACCOUNT_LABEL)
                        .setShortDescription("Telebroad Service")
                        .setCapabilities(CAPABILITY_SELF_MANAGED).build();
                telecomManager.registerPhoneAccount(phoneAccount);
            }
            PJSIPManager.accountHandle = telecomManager.getPhoneAccount(accountHandle).getAccountHandle();

          //  android.util.Log.d("PJSIPConnectionService", " capability self managed? " + (phoneAccount.getCapabilities() & CAPABILITY_SELF_MANAGED));
            if (telecomManager.getPhoneAccount(accountHandle) == null) {
                throw new RuntimeException("cannot create account");
            }

//        } catch (SecurityException e) {
//            throw new RuntimeException("cannot create account", e);
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Bundle getSipCallBundle(){
        Bundle bundle = new Bundle();
//        PhoneAccountHandle handle = new PhoneAccountHandle(new ComponentName(AppController.getInstance(), PJSIPConnectionService.class), PHONE_ACCOUNT_LABEL);
        bundle.putParcelable(EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle);
        bundle.putInt(EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_AUDIO_ONLY);
        return bundle;
    }

    public void sendCall(String destination, boolean hasConnection, int connectionID){
      //  Log.d("PJSIP_CALL_01", "pjsip manager call");
        Intent intent = SipService.getCommandIntent(SipService.ACTION_CALL);
        intent.putExtra(SipService.EXTRA_DEST, destination);
        if (hasConnection){
          //  android.util.Log.d("PJSIPConnectionService", " sending connection to call " +  connectionID);
            intent.putExtra(SipService.EXTRA_CONNECTION_ID, connectionID);
        }
        SipService.addCommand(intent);
    }
    public void sendCall(String destination) {
        sendCall(destination, false, -3);
    }

    public void secondCall(int id, String destination) {
       // android.util.Log.d("PJSIP_CALL_01", "pjsip manager call");
        Intent intent = SipService.getCommandIntent(SipService.ACTION_ADD_CALL);
        intent.putExtra(SipService.EXTRA_DEST, destination);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        SipService.addCommand(intent);
    }

    @Override
    public TeleConsoleCall[] getAllCalls() {
        return CallManager.getInstance().getAllCalls();
    }

    @Override
    public boolean canConference(Consumer<String> handleError) {
        return false;
    }

    @Override
    @Deprecated
    public void setSpeaker(boolean on) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_SPEAKER);
        intent.putExtra(SipService.EXTRA_ON, on);
        SipService.addCommand(intent);
    }

    @Override
    @Deprecated
    public void hold(boolean on) {
        hold(CallManager.getInstance().getActiveCallID(), on);
    }

    @Deprecated
    public void hold(int id, boolean on) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_HOLD);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        intent.putExtra(SipService.EXTRA_ON, on);
        SipService.addCommand(intent);
    }

    @Override
    @Deprecated
    public void record(boolean on){
        //android.util.Log.d("Recording01", "record? " + on);
        record(CallManager.getInstance().getActiveCallID(), on);
        // deprecated
    }

    @Deprecated
    public void record(int id, boolean on) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_RECORD);
       // android.util.Log.d("Recording01", "record " + id +  "? " + on);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        intent.putExtra(SipService.EXTRA_ON, on);
        SipService.addCommand(intent);
    }

    @Override
    public void setCallQuality(CallQuality quality) {
        SipService.addCommand(SipService.ACTION_UPDATE_CALL_QUALITY);
    }

    @Override
    @Deprecated
    public void transfer(String to) {
        transfer(CallManager.getInstance().getActiveCallID(), to);
    }

    @Deprecated
    public void transfer(int id, String to) {
        //android.util.Log.d("PJSIP_CALL_01", "pjsip manager call " +id + " to " + to);
        Intent intent = SipService.getCommandIntent(SipService.ACTION_TRANSFER);
        intent.putExtra(SipService.EXTRA_DEST, to);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        SipService.addCommand(intent);
    }

    @Override
    public void conference() {
        SipService.addCommand(SipService.ACTION_CONFERENCE_ALL);
    }

    @Override
    public void conference(String with) {
        conference(CallManager.getInstance().getActiveCallID(), with);
    }

    @Override
    public SpeakerState getSepakerState() {
        return null;
    }

    @Override
    @Deprecated
    public void sendDTMF(String dtmf) {
        sendDTMF(CallManager.getInstance().getActiveCallID(), dtmf);
    }

    @Deprecated
    public void sendDTMF(int id, String dtmf) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_DTMF);
        intent.putExtra(SipService.EXTRA_DTMF, dtmf);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        SipService.addCommand(intent);

    }

    @Override
    @Deprecated
    public void decline() {
        decline(CallManager.getInstance().getActiveCallID());
    }

    @Override
    @Deprecated
    public void decline(TeleConsoleCall teleConsoleCall) {
        decline(teleConsoleCall.getID());
    }

    @Deprecated
    public void decline(int callID) {
        SipService.addCommand(SipService.getCallCommandIntent(SipService.ACTION_DECLINE, callID));
    }

    @Deprecated
    @Override
    public void accept() {

    }

    @Override
    public void accept(TeleConsoleCall teleConsoleCall) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isDND() {
        return false;
    }

    @Override
    public TeleConsoleCall getCall(String id) {
        return null;
    }

    @Nullable
    @Override
    @Deprecated
    public TeleConsoleCall getOtherCall() {
        return null;
    }

    @Override
    @Deprecated
    public TeleConsoleCall getActiveCall() {
        return CallManager.getInstance().getActiveCall();
    }

    @Override
    public int getCallCount() {
        return CallManager.getInstance().getAllCalls().length;
    }

    @Override
    public boolean isInConference() {
        return CallManager.getInstance().isInConference();
    }

    @Override
    public String getStateDisplayName(TeleConsoleCall teleConsoleCall) {
        if (teleConsoleCall.callStates().isRinging()){
            return "Ringing";
        }
        if (teleConsoleCall.callStates().isHold()){
            return "On Hold";
        }
        if (teleConsoleCall.callStates().isDone()){
            return "Done";
        }
        if (teleConsoleCall.callStates().isEarly()){
            return "Calling...";
        }
        return null;
    }

    @Override
    public int getLongestDurationInConference() {
        return 0;
    }

    @Override
    public LiveData<UIState> getUIState() {
        return null;
    }

    @Override
    public void swap() {

    }

    @Override
    public void split(TeleConsoleCall callToKeep) {
        Intent intent = callToKeep == null ? SipService.getCommandIntent(SipService.ACTION_SPLIT) : SipService.getCallCommandIntent(SipService.ACTION_SPLIT, callToKeep.getID());
        SipService.addCommand(intent);
    }

    @Override
    public void deregister() {
        SipService.addCommand(SipService.ACTION_DEREGISTER);
    }

    public void logout(String username, String domain){
        Intent intent = SipService.getCommandIntent(SipService.ACTION_LOGOUT);

        intent.putExtra(SipService.EXTRA_SIP_ACCOUNT, username);
        intent.putExtra(SipService.EXTRA_SIP_DOMAIN, domain);
        SipService.addCommand(intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void conference(int id, String destination) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && AppController.getInstance().hasPermissions(CALL_PHONE) && TeleConsoleCall.useConnectionService) {
            Bundle bundle = getSipCallBundle();
            Bundle outgoingBundle = new Bundle();

            outgoingBundle.putInt( SipService.EXTRA_PJSIP_ID, id);
            CallManager.getInstance().getCall(id).getCallController().hold(true);
           // android.util.Log.d("Conf01", "Setting extra is conference");
            outgoingBundle.putBoolean(SipService.EXTRA_IS_CONFERENCE, true);
            bundle.putParcelable(EXTRA_OUTGOING_CALL_EXTRAS, outgoingBundle);
            telecomManager.placeCall(Uri.fromParts("tel", destination, null), bundle);
        }else {
            sendConference(id, -3, destination);
        }
    }

    public void sendConference(int id, int connectionID, String destination) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_CONFERENCE);
        intent.putExtra(SipService.EXTRA_DEST, destination);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        SipService.addCommand(intent);
    }

    private boolean isSipServiceRunning() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
//        ActivityManager manager = (ActivityManager) AppController.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (SipService.class.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
    }
}
