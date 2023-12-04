package com.telebroad.teleconsole.pjsip;


import android.app.Service;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneNumber;

public interface CallGroup {

    PhoneNumber getRemoteNumber();
    void setHold(boolean on);
    int duration();
    void hangup();
    void record(boolean on);
    void transfer(String dest);
    void sendDtmf(String digits);
    void mute(boolean on);
    void addToConference();
    void decline();
    void answer();

    String getSipID();
    @NonNull
    int getID();
    MutableLiveData<CallStates> liveCallStates();
    CallOperations opsSupported();

    default CallState getCallState(){
        CallStates states = liveCallStates().getValue();
        if (states == null){
           // android.util.Log.d("CallState", "States are null");
            return CallState.UNKNOWN;
        }
        if (states.isRinging()){
            return CallState.RINGING;
        }
        if (states.isHold()){
            return CallState.HOLD;
        }
        if(!states.isRinging() && !states.isHold()){
            return CallState.ACTIVE;
        }

    //    android.util.Log.d("CallState", "Ringing? " + states.isRinging() + " hold? " + states.isHold() );
        return CallState.UNKNOWN;
    }

    default boolean isEarly(){
        CallStates states = liveCallStates().getValue();
        return states != null && states.isEarly();
    }
    default CallStates callStates(){
        if (liveCallStates().getValue() == null){
            CallStates callStates = new CallStates();
            Utils.updateLiveData(liveCallStates(), callStates);
            return callStates;
        }
        return liveCallStates().getValue();
    }
    SipService getService();


    @RequiresApi(api = Build.VERSION_CODES.O)
    default void setConnectionID(int id){
       // android.util.Log.d("PJSIPConnectionService", "Setting connection id with id " + id + " class is " + getClass().getName());
        if (this instanceof TeleConsoleCall){
           // android.util.Log.d("PJSIPConnectionService", "Setting connection id with id " + id);
            TeleConsoleCall teleConsoleCall = (TeleConsoleCall) this;
            teleConsoleCall.setConnectionID(id);
            teleConsoleCall.callController = new ConnectionController(id);
        }
    }

    default boolean isIncomingEarly(){
       // android.util.Log.d("Incooming", "is incoming? " + isIncoming() + " is early " + isEarly());
        return isIncoming() && isEarly();
    }

    boolean isIncoming();


    enum CallState{
        RINGING, CONNECTING, HOLD, ACTIVE, UNKNOWN
    }
//    LiveData<Boolean> getIsHold();
//    LiveData<Boolean> getIsSpeaker();
//    LiveData<Boolean> getIsMute();

    default int getConnectionID(){
        return -3;
    }

    CallController getCallController();

}

class CallOperations {
    private final boolean canHold;
    private final boolean canTransfer;
    private final boolean canDTMF;
    private final boolean canAddToConference;

    public CallOperations(boolean canHold, boolean canTransfer, boolean canDTMF, boolean canAddToConference) {
        this.canHold = canHold;
        this.canTransfer = canTransfer;
        this.canDTMF = canDTMF;
        this.canAddToConference = canAddToConference;
    }

    public boolean isCanHold() {
        return canHold;
    }

    public boolean isCanTransfer() {
        return canTransfer;
    }

    public boolean isCanDTMF() {
        return canDTMF;
    }

    public boolean isCanAddToConference() {
        return canAddToConference;
    }
}
