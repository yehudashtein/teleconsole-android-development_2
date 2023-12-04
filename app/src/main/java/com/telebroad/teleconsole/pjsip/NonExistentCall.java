package com.telebroad.teleconsole.pjsip;

import androidx.lifecycle.MutableLiveData;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.model.PhoneNumber;

import static com.telebroad.teleconsole.pjsip.PjsipError.CALL_DOES_NOT_EXIST;
import static com.telebroad.teleconsole.pjsip.SipService.NONEXSISTENT_PJSIP_ID;

public class NonExistentCall implements CallGroup {

    private static NonExistentCall instance;

    private NonExistentCall(){

    }

    public static NonExistentCall getInstance() {
        if (instance == null){
            instance = new NonExistentCall();
        }
        return instance;
    }

    private void setError() {
        CallManager.getInstance().setError(CALL_DOES_NOT_EXIST);
    }

    @Override
    public PhoneNumber getRemoteNumber() {
        setError();
        String doesNotExsist = AppController.getAppString(R.string.call_not_exist);
        return PhoneNumber.getPhoneNumber(doesNotExsist, doesNotExsist);
    }

    @Override
    public void setHold(boolean on) {
        setError();
    }

    @Override
    public int duration() {
        return 0;
    }


    @Override
    public void hangup() {
        setError();
        CallManager.getInstance().stopForeground(SipService.getInstance());
    }

    @Override
    public void record(boolean on) {
        setError();
    }

    @Override
    public void transfer(String dest) {
        setError();
    }

    @Override
    public void sendDtmf(String digits) {
        setError();
    }

    @Override
    public void mute(boolean on) {
        setError();
    }

    @Override
    public void addToConference() {
        setError();
    }

    @Override
    public void decline() {
        setError();
    }

    @Override
    public void answer() {
        setError();
    }

    @Override
    public String getSipID() {
        return "NonExistingSipId ";
    }

    @Override
    public int getID() {
        return NONEXSISTENT_PJSIP_ID;
    }

    @Override
    public MutableLiveData<CallStates> liveCallStates() {
        setError();
        return new MutableLiveData<>();
    }

    @Override
    public CallOperations opsSupported() {
        return new CallOperations(false, false, false, false);
    }

    @Override
    public SipService getService() {
        setError();
        return SipService.getInstance();
    }

    @Override
    public boolean isIncoming() {
        return false;
    }

    @Override
    public CallController getCallController() {
        return null;
    }
}
