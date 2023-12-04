package com.telebroad.teleconsole.pjsip;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.DisconnectCause;
import android.telecom.TelecomManager;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
//import com.telebroad.teleconsole.helpers.BluetoothManager;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.Utils;

import static android.media.AudioManager.MODE_NORMAL;
import static com.telebroad.teleconsole.pjsip.SipService.ACTION_UPDATE_NOTIFICATION;
import static com.telebroad.teleconsole.pjsip.SipService.CONFERENCE_PJSIP_ID;
import static com.telebroad.teleconsole.pjsip.SipService.NONEXSISTENT_PJSIP_ID;
import static com.telebroad.teleconsole.pjsip.SipService.SERVICE_NOTIFICATION_ID;

public class CallManager implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "Call Manager";
    private TeleConsoleConference conference = new TeleConsoleConference();
    private SparseArray<TeleConsoleCall> calls = new SparseArray<>();
    private MutableLiveData<Boolean> liveRegistration = new MutableLiveData<>();
    private MutableLiveData<CallGroup> liveCallGroup = new MutableLiveData<>();
    private MutableLiveData<TeleConsoleError> liveError = new MutableLiveData<>();

    private static CallManager instance;

    private TeleConsoleCall activeCall;
    private boolean isBluetoothRegistered = true;

    private CallManager() {
        super();
    }

   public synchronized static CallManager getInstance() {
        if (instance == null) {
            instance = new CallManager();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public boolean hasExternalCalls(){
        TelecomManager telecomManager = (TelecomManager) AppController.getInstance().getSystemService(Context.TELECOM_SERVICE);
        if (!AppController.getInstance().hasPermissions(Manifest.permission.READ_PHONE_STATE) || telecomManager == null){
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && TeleConsoleCall.useConnectionService) {
            return telecomManager.isInManagedCall();
        }
        return telecomManager.isInCall();
    }

    public void stopForeground(SipService service){
        Utils.logToFile(AppController.getInstance(), "Stopping Foreground if no calls, calls left " + calls.size());
        if (service == null){
            Utils.logToFile("Service is null, it is probably ended already");
            return;
        }
        if (calls.size() <= 0){
//            SipService.getInstance().enqueueJob(() -> {
//                Endpoint endpoint = service.getEndpointInstance(false);
//                if (endpoint != null){
//                    try {
////                        endpoint.audDevManager().setCaptureDev(-99);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, "Stopping captureDev");

            Utils.logToFile("Stopping Foreground");
            service.stopForeground(true);

            NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(SERVICE_NOTIFICATION_ID);
            Utils.logToFile("Foreground Stopped");
        }
    }

    public boolean hasMultipleGroups(){
        if (conference == null || conference.callsInConference.size() == 0){

           // android.util.Log.d("ConfUI", "conference calls are empty conference is null? " + (conference == null)  + " conference is " + conference.toString());
            return calls.size() > 1;
        }else{

            //android.util.Log.d("ConfUI", "call size " + calls.size() + " calls in conf " + conference.callsInConference.size());
            return calls.size() > conference.callsInConference.size();
        }
    }

    public boolean isInConference() {
        return getActiveCallID() == CONFERENCE_PJSIP_ID;
    }


    void addCall(TeleConsoleCall call) {

       // Log.d("PJSIPConnectionService", "adding call in manager with PJ_ID " + call.getId() + " " + call);
        Utils.logToFile( "adding call " + call.getId() + " calls before appending " + calls.size());
        if (calls.size() == 0 && !TeleConsoleCall.useConnectionService){
//            AudioManager audioManager = (AudioManager) call.getService().getSystemService(AUDIO_SERVICE);
//            oldMode = audioManager.getMode();
//            oldBluetooth = audioManager.isBluetoothScoOn();
//            audioManager.requestAudioFocus(this, STREAM_VOICE_CALL, AUDIOFOCUS_GAIN);
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//
//            if (BluetoothWrapper.canBluetooth(audioManager)){
//                if (isBluetoothConnected) {
//                    AppController.getMainHandler().post(() ->audioManager.setBluetoothScoOn(true));
//                }else{
//                    AppController.getMainHandler().post(audioManager::startBluetoothSco);
//                }
//                AppController.getInstance().registerReceiver(bluetoothReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
//                isBluetoothRegistered = true;
//            }
//            oldSpeakerphone = audioManager.isSpeakerphoneOn();
        }

       // Log.d("PJSIPConnectionService", "appending call in manager with PJ_ID " + call.getId() + " " + call);
        if (!containsSipID(call.getSipID())) {
            calls.append(call.getId(), call);
        }
        if ((call.isIncoming() && call.callStates().isRinging()) || calls.size() == 1 || (liveCallGroup.getValue() != null && liveCallGroup.getValue().callStates().isHold())){
           setLiveCall(call);
        }
    }

    private boolean containsSipID(String sipID){
        if (sipID == null){
            return false;
        }
        for (int i = 0; i < calls.size(); i++){
            TeleConsoleCall call = calls.valueAt(i);
            if (sipID.equals(call.getSipID())){
                return true;
            }

        }
        return  false;
    }
    void triggerUIUpdate(){
        Utils.updateLiveData(getActiveCallGroup().liveCallStates(), getActiveCallGroup().callStates());
    }
    int oldMode = MODE_NORMAL;
    boolean oldSpeakerphone = false;
    boolean oldBluetooth = false;
    void removeCall(int id) {
        Utils.logToFile("Removing call " + id + " calls before removing " + calls.size());
       // android.util.Log.d("PJSIPConnectionService", "Removing call in manager with PJ_ID " + id);
        //android.util.Log.d("Call_Manager", "Removing call " + id);
        TeleConsoleCall call = calls.get(id);
        calls.remove(id);
        conference.removeCall(id);
        AndroidAudioManager.getAudioManager().callEnded();
        if (call != null) {
            CallStates callStates = call.liveCallStates().getValue();
            callStates = callStates == null ? new CallStates() : callStates;
            callStates.setDone(true);
            Utils.updateLiveData(call.liveCallStates(), callStates);
        }

        if (call == getLiveCall().getValue() && conference.callsInConference.size() == 0 && calls.size() > 0) {
            activeCall = calls.valueAt(0);
            //android.util.Log.d("Call_Manager", "new call is " + call + " index is " + calls.keyAt(0));
            if (activeCall.callStates().isHold()) {
                try {
                    activeCall.setHold(false);
                } catch (Exception e) {
                    Utils.logToFile(e);
                    e.printStackTrace();
                }
            }
            Utils.updateLiveData(liveCallGroup, activeCall);
            SipService.addCommand(SipService.getCallCommandIntent(ACTION_UPDATE_NOTIFICATION, activeCall.getID()));
        }else if (liveCallGroup != null && liveCallGroup.getValue() != null){
            Utils.updateLiveData(liveCallGroup, liveCallGroup.getValue());
            SipService.addCommand(SipService.getCallCommandIntent(ACTION_UPDATE_NOTIFICATION, liveCallGroup.getValue().getID()));
        }
        // No more calls turn off the speaker and finish the service
        if (calls.size() == 0) {
            if (call == null || call.getService() == null){
                return;
            }
            Utils.logToFile( "Stopping Foreground from remove calls since no calls are left");
            stopForeground(call.account.getService());
            if (!TeleConsoleCall.useConnectionService) {
//                AudioManager audioManager = (AudioManager) call.getService().getSystemService(AUDIO_SERVICE);
////                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//                audioManager.setSpeakerphoneOn(oldSpeakerphone);
//                android.util.Log.d("OldAudio", "bluetooth " + oldBluetooth + " speaker " + oldSpeakerphone + " mode " + oldMode);

//                AppController.getMainHandler().post(() ->audioManager.setBluetoothScoOn(oldBluetooth));
//                audioManager.setBluetoothScoOn(oldBluetooth);
//                audioManager.abandonAudioFocus(this);
//                if (BluetoothWrapper.canBluetooth(audioManager)){
//                    AppController.getMainHandler().post(() -> {
//                        android.util.Log.d("BLuetooth", "stopping? " + !oldBluetooth);
//                        if(!oldBluetooth) {
//                            audioManager.stopBluetoothSco();
//                            audioManager.setBluetoothScoOn(oldBluetooth);
//                            isBluetoothConnected = false;
//                        }
//                    });
//                }

//                try {
//                    if (isBluetoothRegistered) {
//                        AppController.getInstance().unregisterReceiver(bluetoothReceiver);
//                        isBluetoothRegistered = false;
//                    }
//                }catch (IllegalArgumentException iae){
//                    Utils.logToFile("Unable to deregister bluetooth receiver");
//                }
//                audioManager.setMode(oldMode);
//                BluetoothWrapper.getInstance().setBluetoothOn(false);
            }
        }else{
            // Calls still available
            CallGroup newActiveCall = calls.valueAt(0);
            setLiveCall(newActiveCall);
        }


    }

//    int bluetoothTimesStarted = 0;
    public void addCallToConference(TeleConsoleCall call) {
       // android.util.Log.d("Conf01", "Call Manager adding to conf " + call.getId());
        conference.addCall(call);
        if (conference.callsInConference.size() >= 2){
            Utils.updateLiveData(liveCallGroup, conference);
        }
    }

    public TeleConsoleCall[] getAllCalls() {
        TeleConsoleCall[] arrayList = new TeleConsoleCall[calls.size()];
        for (int i = 0; i < calls.size(); i++)
            arrayList[i] = calls.valueAt(i);
        return arrayList;
    }

    public int getCallCount(){
        return calls.size();
    }

    boolean hasSipID(String sipID){
        for (int i = 0; i < calls.size(); i++) {
           // android.util.Log.d("DUPLICATE", "Sip ID " + sipID + " matches " + calls.valueAt(i).getSipID() );
            if (calls.valueAt(i).getSipID().equals(sipID)){
               //android.util.Log.d("DUPLICATE", "Returning true");
                return true;
            }
        }

       // android.util.Log.d("DUPLICATE", "Returning false");
        return false;
    }
    @NonNull
    public CallGroup getCall(int id) {

       // android.util.Log.d("PJSIPConnectionService", "getting call in manager with PJ_ID " + id);
        if (id == CONFERENCE_PJSIP_ID) {
            return conference;
        }
        CallGroup result = calls.get(id);
       // android.util.Log.d("PJSIPConnectionService", "got call in manager with PJ_ID " + id + " " + result);
        if (id == NONEXSISTENT_PJSIP_ID ) {
            return NonExistentCall.getInstance();
        }
        if (result == null){
            if (liveCallGroup.getValue() != null){
                return liveCallGroup.getValue();
            }
            return NonExistentCall.getInstance();
        }
//        if (conference.callsInConference.get(id) != null){
//            return conference;
//        }
        return result;
    }


    @Deprecated
    public TeleConsoleCall getActiveCall() {
        return activeCall;
    }

    @NonNull
    public CallGroup getActiveCallGroup() {
        if (liveCallGroup.getValue() == null){
            return NonExistentCall.getInstance();
        }
        return liveCallGroup.getValue();
    }

    public CallGroup getOtherCall(){

        int callCount = getCallCount();
        if (callCount <= 1){
            return NonExistentCall.getInstance();
        }
        if (calls.valueAt(0) != liveCallGroup.getValue()){
            return calls.valueAt(0);
        }else {
            return calls.valueAt(1);
        }
//        return null;
    }
    public int getActiveCallID(){
        return getActiveCallGroup().getID();
    }

    public LiveData<CallGroup> getLiveCall() {
        return liveCallGroup;
    }

    public void setLiveCall(CallGroup call) {
        // Call is already set
        if (call == liveCallGroup.getValue()){
            return;
        }
        // The call we want to set is in the conference already displayed.
        if (liveCallGroup.getValue() == conference && conference.includesCall(call.getID())){
            return;
        };
        Utils.updateLiveData(liveCallGroup, call);
        call.getService().startForeground(call);
    }

    void setLiveCall(int id) {
        setLiveCall(getCall(id));
    }

    void setRegistration(Boolean isRegistered){
        Utils.updateLiveData(liveRegistration, isRegistered);
    }


    void setError(TeleConsoleError error) {
        Utils.updateLiveData(liveError, error);
    }

    public void setActiveCall(TeleConsoleCall activeCall) {
        this.activeCall = activeCall;
    }

    public void split(TeleConsoleCall callToKeep) {
        conference.removeAll(callToKeep);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            conference.setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        }

    }

    public int getLongestDurationInConference() {
        if (conference == null){
            return 0;
        }
        return conference.getLongestDuration();
    }

    public boolean hasCalls() {
        return getCallCount() >= 1;
    }

    public TeleConsoleConference getConference() {
        return conference;
    }

    public void addAllToConference() {
        for (int i = 0; i < calls.size(); i++) {
            calls.valueAt(i).addToConference();
        }
    }
    public void androidCallActive(){
        if (liveCallGroup.getValue() != null){
            getLiveCall().getValue().setHold(true);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    public void setBluetoothOn(boolean on) {
//        AudioManager audioManager = (AudioManager) SipService.getInstance().getSystemService(AUDIO_SERVICE);
//        if(isBluetoothConnected){
//
//            AppController.getMainHandler().post(() ->audioManager.setBluetoothScoOn(true));
////            audioManager.setBluetoothScoOn(on);
//        }else if (on){
//            AppController.getMainHandler().post(audioManager::startBluetoothSco);
//        }
    }
}

enum PjsipError implements TeleConsoleError {
    CALL_DOES_NOT_EXIST(404, R.string.nonexsitent_call_error),
    UNSUPPORTED_OPERATION(100, R.string.unsupported_op_error);

    PjsipError(int code, String errorMessage) {
        this.setCode(code);
        this.setErrorMessage(errorMessage);
    }

    PjsipError(int code, @StringRes int stringRes) {
        this(code, AppController.getInstance().getString(stringRes));
    }

    private int code;
    private String errorMessage;

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}

