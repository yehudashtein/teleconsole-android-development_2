package com.telebroad.teleconsole.pjsip;

import android.os.Build;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;

import androidx.annotation.RequiresApi;

import static android.telecom.CallAudioState.ROUTE_BLUETOOTH;
import static android.telecom.CallAudioState.ROUTE_SPEAKER;
import static android.telecom.CallAudioState.ROUTE_WIRED_OR_EARPIECE;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ConnectionController implements CallController{

    private final PJSIPConnection connection;
    private boolean isSpeaker = false;
    public ConnectionController(int connectionID){
        connection = PJSIPConnection.connections.get(connectionID);
        if (connection == null){
            throw new IllegalArgumentException("No connection with ID " + connectionID);
        }
    }
    @Override
    public void hold(boolean on) {
        if (on){
            //android.util.Log.d("PJSIPConnectionService", "placing on hold");
            connection.setOnHold();
            connection.onHold();
        }else {
            connection.setActive();
            connection.onUnhold();
            setSpeaker(isSpeaker);
        }
    }

    @Override
    public void mute(boolean on) {
        connection.mute(on);
    }

    @Override
    public void hangup() {
        connection.setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        connection.pjsipCallController.hangup();
    }

    @Override
    public void record(boolean on) {
        connection.pjsipCallController.record(on);
    }

    @Override
    public void transfer(String dest) {
        connection.pjsipCallController.transfer(dest);
    }

    @Override
    public void decline() {
        connection.onReject();
    }

    @Override
    public void answer()
    {
        connection.onAnswer();
    }

    @Override
    public void sendDTMF(String digits) {
        connection.pjsipCallController.sendDTMF(digits);
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public void setSpeaker(boolean on) {
        isSpeaker = on;
        if (on) {
            connection.setAudioRoute(ROUTE_SPEAKER);
        }else if ((connection.getCallAudioState().getSupportedRouteMask() & ROUTE_BLUETOOTH) > 0){
            connection.setAudioRoute(ROUTE_BLUETOOTH);
        }else{
            connection.setAudioRoute(ROUTE_WIRED_OR_EARPIECE);
        }
        SipService.addCommand(SipService.getCallCommandIntent(SipService.ACTION_UPDATE_NOTIFICATION, connection.pjsipCallController.getID()));
    }

    @Override
    public void answer(boolean ignoreActive) {
    }

    @Override
    public void notifyDisconnected(int cause) {
        connection.setDisconnected(new DisconnectCause(cause));
        connection.onDisconnect();
    }

    @Override
    public void ring() {
        connection.setRinging();
    }

    @Override
    public void setRoute(int route) {
        int audioRoute = ROUTE_WIRED_OR_EARPIECE;

        switch(route){
            case SPEAKER_ROUTE:
                audioRoute = ROUTE_SPEAKER;
                break;
            case BLUETOOTH_ROUTE:
                audioRoute = ROUTE_BLUETOOTH;
                break;
            case EARPIECE_ROUTE:
                audioRoute = ROUTE_WIRED_OR_EARPIECE;
                break;

        }
        connection.setAudioRoute(audioRoute);
    }

    @Override
    public boolean isSpeaker() {
        CallAudioState state = connection.getCallAudioState();
//        return state != null && state.getRoute() == ROUTE_SPEAKER ;
        return isSpeaker;
    }

    @Override
    public void addToConference() {
        connection.pjsipCallController.addToConference();
    }


    public void fakeHold(){
        connection.fakeHold();
    }
}
