package com.telebroad.teleconsole.pjsip;

import android.os.Build;
import android.telecom.CallAudioState;
import android.telecom.Conference;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.util.SparseArray;

import androidx.lifecycle.MutableLiveData;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneNumber;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.AudioMediaRecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.telecom.CallAudioState.ROUTE_BLUETOOTH;
import static android.telecom.CallAudioState.ROUTE_SPEAKER;
import static android.telecom.CallAudioState.ROUTE_WIRED_OR_EARPIECE;
import static com.telebroad.teleconsole.pjsip.PjsipError.UNSUPPORTED_OPERATION;
import static com.telebroad.teleconsole.pjsip.SipService.CONFERENCE_PJSIP_ID;

class TeleConsoleConference extends Conference implements CallGroup, CallController {
    private AudioMediaRecorder audioMediaRecorder;
    private AudioMedia localStream;
    private CallOperations callOpsSupported = new CallOperations(true, false, false, false);
    private MutableLiveData<CallStates> liveInCallStates = new MutableLiveData<>();
    private CallStates inCallStates = new CallStates();
    SparseArray<TeleConsoleCall> callsInConference = new SparseArray<>();

    public TeleConsoleConference(){
        super(PJSIPManager.accountHandle);
        Utils.updateLiveData(liveCallStates(), inCallStates);
    }

    public boolean includesCall(int id){
        return callsInConference.get(id) != null;
    }

    @Override
    public PhoneNumber getRemoteNumber() {
        String conference = AppController.getAppString(R.string.conference);
        return PhoneNumber.getPhoneNumber(conference, conference);
    }

    @Override
    public void setHold(boolean on) {
        // Because this is a conference, and we don't want to play music on hold, we just mute the outgoing and incoming streams
        SipService.getInstance().enqueueJob(() -> setHoldBackground(on), "Setting conf hold" );
    }

    private void setHoldBackground(boolean on) {
        //Log.d("ConfHold", "SettingConfOnHold for " + callsInConference.size() + " calls");
        inCallStates.setHold(on);
        silenceLocalStream(on);
        for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {
            TeleConsoleCall call = callsInConference.valueAt(i);
          //  Log.d("ConfHold", "starting call " + i + " with call " + call);
            if (call == null) {
                continue;
            }
            if (call.getRemoteStream() != null) {
               // Log.d("ConfHold", "call " + i);

                try {
                    if (on) {
                        call.getRemoteStream().stopTransmit(localStream);
                    } else {
                        call.getRemoteStream().startTransmit(localStream);
                    }
                } catch (Exception e) {
                    //Log.d("ConfHold", "call " + i + " crashed", e);

                    Utils.logToFile(e);
                    e.printStackTrace();
                }
            }
        }
        Utils.updateLiveData(liveCallStates(), inCallStates);
    }

    @Override
    public int duration() {
        return getLongestDuration();
    }

    @Override
    public void hangup() {
        //Log.d("Conf01", "conf hanging up");
        // If there are only 2 calls in conference transfer one to another.
        if (callsInConference.size() == 2) {
            //Log.d("Conf01", "xfer replacing");
            TeleConsoleCall call = callsInConference.valueAt(0);
            call.getService().enqueueJob(() -> call.xferReplaces(callsInConference.valueAt(1)),"Replacing");
//            callsInConference.valueAt(0).xferReplaces(callsInConference.valueAt(1));
            if(TeleConsoleCall.useConnectionService) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
                }
            }
        } else {
            for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {
                callsInConference.valueAt(i).hangup();
            }
        }
    }

    @Override
    public void record(boolean on) {
        try {
            inCallStates.setRecording(on);
            Utils.updateLiveData(liveInCallStates, inCallStates);
            if (audioMediaRecorder == null) {
                audioMediaRecorder = new AudioMediaRecorder();
                File root = Utils.getRootFolder();
                String filepath = root.getAbsolutePath() + File.separator + "TeleConsole" + File.separator + "Recordings"  + File.separator + "Conference";
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {
                    stringBuilder.append(callsInConference.valueAt(i).getRemoteNumber().getNameString()).append("-");
                }
                stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
                filepath = filepath + File.separator + stringBuilder.toString();
                File dir = new File(filepath);
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();

                String dateTime = new SimpleDateFormat("M\\dd\\yyyy hh:mm a (ss.SSS)", Locale.getDefault()).format(new java.util.Date());//android.text.format.DateFormat.format("M\\dd\\yyyy hh:mm a (ss.SSS)", new java.util.Date()).toString();
                String filePath = dir.getAbsolutePath() + File.separator + dateTime + ".wav";
                audioMediaRecorder.createRecorder(filePath);

            }

            if (audioMediaRecorder != null && localStream != null) {
                if (on) {
                    localStream.startTransmit(audioMediaRecorder);
                    for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {
                        TeleConsoleCall call = callsInConference.get(i);
                        if (call.getRemoteStream() != null) {
                            call.getRemoteStream().startTransmit(audioMediaRecorder);
                        }
                    }
                } else {
                    localStream.stopTransmit(audioMediaRecorder);
                    for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {
                        TeleConsoleCall call = callsInConference.get(i);
                        if (call.getRemoteStream() != null) {
                            call.getRemoteStream().stopTransmit(audioMediaRecorder);
                        }
                    }
                }
            }
        } catch (Exception e) {

            Utils.logToFile(e);
            e.printStackTrace();
        }

    }

    @Override
    public void transfer(String dest) {
        setError();
    }

    private void setError() {
        CallManager.getInstance().setError(UNSUPPORTED_OPERATION);
    }

    @Override
    public void sendDtmf(String digits) {
        setError();
    }

    @Override
    public void hold(boolean on) {
        setHold(on);
    }

    @Override
    public void mute(boolean on) {
        inCallStates.setMute(on);
        Utils.updateLiveData(liveInCallStates, inCallStates);
        SipService.getInstance().enqueueJob(() -> silenceLocalStream(on), "muting conference");
//        silenceLocalStream(on);
    }

    public void silenceLocalStream(boolean on) {
        for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {
            Utils.logToFile("Muting call");
            callsInConference.valueAt(i).mute(on);
        }
    }

    @Override
    public void addToConference() {
        setError();
       // android.util.Log.w("Conference", "Tried adding conference to conference");
    }

    @Override
    public void decline() {

    }

    @Override
    public void answer() {

    }

    @Override
    public void sendDTMF(String digits) {

    }

    @Override
    public String getSipID() {
        return "ConferenceSIPid";
    }

    @Override
    public int getID() {
        return CONFERENCE_PJSIP_ID;
    }

    @Override
    public void setSpeaker(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (Connection connection : getConnections()){
                if (on){
                    connection.setAudioRoute(ROUTE_SPEAKER);
                }else{
                    CallAudioState callAudioState = connection.getCallAudioState();
                    if (callAudioState == null || (callAudioState.getSupportedRouteMask() & ROUTE_SPEAKER) == 0 ){
                        connection.setAudioRoute(ROUTE_WIRED_OR_EARPIECE);
                    }else{
                        connection.setAudioRoute(ROUTE_BLUETOOTH);
                    }
                }
            }
        }
    }

    @Override
    public void answer(boolean ignoreActive) {

    }

    @Override
    public void notifyDisconnected(int cause) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for(Connection connection : getConnections()){
                connection.setDisconnected(new DisconnectCause(cause));
            }
            setDisconnected(new DisconnectCause(cause));
        }
    }

    @Override
    public void ring() {

    }

    @Override
    public void setRoute(int route) {

    }

    @Override
    public boolean isSpeaker() {
        return false;
    }

    @Override
    public MutableLiveData<CallStates> liveCallStates() {
        return liveInCallStates;
    }

    @Override
    public CallOperations opsSupported() {
        return callOpsSupported;
    }

    @Override
    public SipService getService() {
        if(callsInConference.size() > 0){
            return callsInConference.valueAt(0).getService();
        }
        return SipService.getInstance();
    }

    @Override
    public boolean isIncoming() {
        return false;
    }

    @Override
    public CallController getCallController() {
        return this;
    }

    public void removeCall(int id) {
        if (callsInConference.get(id) != null) {
            callsInConference.get(id).onMediaReady = null;
        }
        callsInConference.remove(id);
        // If there is only one or less calls remaining in the conference, finish the conference
        if (callsInConference.size() == 1) {
            callsInConference.valueAt(0).onMediaReady = null;
            CallManager.getInstance().setLiveCall(callsInConference.valueAt(0));
            finishConference();
        }else if (callsInConference.size() < 0){
            finishConference();
        }
    }

    public void removeAll (TeleConsoleCall callToKeep){
        for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {
            TeleConsoleCall call = callsInConference.valueAt(i);
            call.onMediaReady = null;
            if (callToKeep != call){
                call.setHold(true);
            }
        }
        finishConference();
        if (callToKeep != null){
            CallManager.getInstance().setLiveCall(callToKeep);
        }else{
            CallManager.getInstance().setLiveCall(CallManager.getInstance().getOtherCall());
        }
    }
    public void finishConference() {
        localStream = null;
        callsInConference.clear();
        inCallStates.setHold(false);
        inCallStates.setMute(false);
        if (audioMediaRecorder != null) {
            audioMediaRecorder.delete();
            audioMediaRecorder = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        }
//        PJSIPConnectionService.getInstance().addConference(this);
    }

    public void addCall(TeleConsoleCall call) {
        if (callsInConference.size() == 0 && TeleConsoleCall.useConnectionService){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PJSIPConnectionService.getInstance().addConference(this);
                addConnection(PJSIPConnection.connections.get(call.getConnectionID()));
            }
        }
       // Log.d("Conf01", "adding call to conference " + call.getId());
        if (localStream == null) {
            localStream = call.getLocalStream();
        }
        call.getService().enqueueJob(() -> call.mute(false), "Unmuting For Conference");
        if (call.isOnHold()) {
            call.setHold(false);
            call.onMediaReady = () -> addCall(call);
            return;
        }

        for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {

            TeleConsoleCall oldCall = callsInConference.valueAt(i);
            if (oldCall.getId() == call.getId()){
                continue;
            }
            oldCall.getService().enqueueJob(() -> {
                //Log.d("Conf01", "transmitting " + oldCall.getId() + " to " + call.getId());
                try {
                    oldCall.getRemoteStream().startTransmit(call.getRemoteStream());

                  //  Log.d("Conf01", "transmitting " + call.getId() + " to " + oldCall.getId());
                    call.getRemoteStream().startTransmit(oldCall.getRemoteStream());
                } catch (Exception e) {
                    Utils.logToFile(e);
                    e.printStackTrace();
                }
            }, "conferencing calls");

        }
        callsInConference.append(call.getId(), call);
       // Log.d("ConfUI1", "conference size  " + callsInConference.size() + " " + toString());
        if (callsInConference.size() >= 2) {
            CallManager.getInstance().setLiveCall(this);
        }
    }

    public int getLongestDuration() {
        int maxDuration = 0;
        for (int i = 0, nsize = callsInConference.size(); i < nsize; i++) {
            TeleConsoleCall call = callsInConference.valueAt(i);
            maxDuration = Math.max(maxDuration, call.getDuration());
        }
        return maxDuration;
    }


    public void fakeHold(){

    }
}
