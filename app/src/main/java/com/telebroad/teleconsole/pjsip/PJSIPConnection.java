package com.telebroad.teleconsole.pjsip;

import android.net.Uri;
import android.os.Build;
import android.telecom.Conference;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.SparseArray;

import androidx.annotation.RequiresApi;

import static android.telecom.CallAudioState.ROUTE_BLUETOOTH;
//import static android.telecom.CallAudioState.ROUTE_SPEAKER;
import static android.telecom.CallAudioState.ROUTE_WIRED_OR_EARPIECE;
import static android.telecom.TelecomManager.PRESENTATION_ALLOWED;
import static android.telecom.VideoProfile.STATE_AUDIO_ONLY;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PJSIPConnection extends Connection {

    private int connectionID;
    private int callID;
    PJSIPCallController pjsipCallController;
    public PJSIPConnection(Uri address){
        setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        setConnectionCapabilities(Connection.CAPABILITY_HOLD | Connection.CAPABILITY_MUTE
                | Connection.CAPABILITY_SUPPORT_HOLD | Connection.CAPABILITY_SEPARATE_FROM_CONFERENCE | Connection.CAPABILITY_DISCONNECT_FROM_CONFERENCE

        );
        setAddress(address, PRESENTATION_ALLOWED);
        setVideoState(STATE_AUDIO_ONLY);
        setAudioModeIsVoip(true);
        connectionID = ++connectionCounter;
       // android.util.Log.d("PJSIPConnectionService", " adding with new connection id " + connectionID);
        connections.put(connectionID, this);

    }

    private static int connectionCounter = 0;
    public static SparseArray<PJSIPConnection> connections = new SparseArray<>();

    @Override
    public void onHold() {
      //  android.util.Log.d("PJSIPConnection", "set on hold");
        setOnHold();
        pjsipCallController.hold(true);
//        super.onHold();
    }

    @Override
    public void onUnhold() {
        //android.util.Log.d("PJSIPConnection", "setting on unhold");
        setActive();
        pjsipCallController.hold(false);
        super.onUnhold();
    }

    public void notifyDisconnected(DisconnectCause cause){
//        connections.remove(connectionID);
        setDisconnected(cause);
//        destroy();
    }

    @Override
    public void onStateChanged(int state) {

        if (state == STATE_DISCONNECTED){
           // android.util.Log.d("ConnectionState", "state is disconnected");
            connections.remove(connectionID);
            destroy();
        }
    }

    @Override
    public void onDisconnect() {
       // android.util.Log.d("PJSIPConnection", "Disconnecting");
        notifyDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
    }


    public void mute(boolean on){
        pjsipCallController.mute(on);
    }
    @Override
    public void onShowIncomingCallUi() {
        pjsipCallController.ring();
//        setAudioRoute(ROUTE_SPEAKER);
//        super.onShowIncomingCallUi();
    }

    public void setCallID(int callID) {

        this.callID = callID;
        pjsipCallController = new PJSIPCallController(callID);
        CallManager.getInstance().getCall(callID).setConnectionID(this.connectionID);
    }


    @Override
    public void onAnswer() {
        setActive();
        if (getCallAudioState() == null){
            return;
        }
        if ((getCallAudioState().getSupportedRouteMask() & ROUTE_BLUETOOTH) > 0){
            setAudioRoute(ROUTE_BLUETOOTH);
        }else{
            setAudioRoute(ROUTE_WIRED_OR_EARPIECE);
        }
        pjsipCallController.answer();
    }

    @Override
    public void onReject() {
        notifyDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
        pjsipCallController.decline();
    }


    @Override
    public void onPlayDtmfTone(char c) {
        pjsipCallController.sendDTMF(c + "");
    }

    @Override
    public void onReject(String replyMessage) {
        pjsipCallController.decline();
    }

    @Override
    public void onReject(int rejectReason) {
        pjsipCallController.decline();
    }

    public int getConnectionID() {
        return this.connectionID;
    }

    public void fakeHold() {
        setOnHold();
    }
}
