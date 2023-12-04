package com.telebroad.teleconsole.pjsip;

import android.content.Intent;

import com.telebroad.teleconsole.controller.AppController;

public class PJSIPCallController implements CallController{

    final int id;

    public PJSIPCallController(int id){
        this.id = id;
    }

    @Override
    public void hold(boolean on) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_HOLD);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        intent.putExtra(SipService.EXTRA_ON, on);
        SipService.addCommand(intent);
    }

    @Override
    public void mute(boolean on) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_MUTE);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        intent.putExtra(SipService.EXTRA_ON, on);
        SipService.addCommand(intent);
    }

    @Override
    public void hangup() {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_HANGUP);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        SipService.addCommand(intent);
    }

    @Override
    public void record(boolean on) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_RECORD);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        intent.putExtra(SipService.EXTRA_ON, on);
        SipService.addCommand(intent);
    }

    @Override
    public void transfer(String to) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_TRANSFER);
        intent.putExtra(SipService.EXTRA_DEST, to);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        SipService.addCommand(intent);
    }

    @Override
    public void decline() {
        SipService.addCommand(SipService.getCallCommandIntent(SipService.ACTION_DECLINE, id));
    }

    @Override
    public void answer() {
        SipService.addCommand(SipService.getCallCommandIntent(SipService.ACTION_ANSWER, id));
    }

    @Override
    public void sendDTMF(String digits) {
        Intent intent = SipService.getCommandIntent(SipService.ACTION_DTMF);
        intent.putExtra(SipService.EXTRA_DTMF, digits);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, id);
        SipService.addCommand(intent);
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void setSpeaker(boolean on) {
        SipManager.getInstance(AppController.getInstance()).setSpeaker(on);
    }

    @Override
    public void answer(boolean ignoreActive) {
        Intent answerIntent = SipService.getCallCommandIntent(SipService.ACTION_ANSWER, id);
        answerIntent.putExtra(SipService.EXTRA_IGNORE_MOBILE_CALL_ACTIVE, ignoreActive);
        AppController.getInstance().startService(answerIntent);
    }

    @Override
    public void notifyDisconnected(int cause) {

    }

    public void fakeHold(){

    }
    @Override
    public void ring() {
        TeleConsoleCall call = (TeleConsoleCall) CallManager.getInstance().getCall(getID());
//        android.util.Log.d("PJSIPConnectionService", "getting call with id " + getID() + " is " + (SipManager.getInstance().getCall(String.valueOf(getID()))));
//        if (call == null){
//            return;
//        }
        call.getService().enqueueJob(() -> {
            try {
                call.ring();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Ringing");
    }

    @Override
    public void setRoute(int route) {

//        android.util.Log.d("BluetoothDM", "setting route " + route);
//        TeleConsoleCall call = (TeleConsoleCall) CallManager.getInstance().getCall(getID());
//        switch (route){
//            case BLUETOOTH_ROUTE:
//                try {
//                    SipService.getInstance().getEndpointInstance(false).audDevManager().setOutputRoute(pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_BLUETOOTH, true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case SPEAKER_ROUTE:
//                try {
//                    SipService.getInstance().getEndpointInstance(false).audDevManager().setOutputRoute(pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_LOUDSPEAKER, true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case EARPIECE_ROUTE:
//                try {
//                    SipService.getInstance().getEndpointInstance(false).audDevManager().setOutputRoute(pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_EARPIECE, true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//        }break
    }

    @Override
    public boolean isSpeaker() {
        return SipService.isSpeaker();
    }

    @Override
    public void addToConference() {

        TeleConsoleCall call = (TeleConsoleCall) CallManager.getInstance().getCall(getID());

        CallManager.getInstance().addCallToConference(call);
//        CallManager.getInstance().getCall(getID()).addToConference();
    }
}
