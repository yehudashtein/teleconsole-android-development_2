package com.telebroad.teleconsole.pjsip;

import android.telecom.DisconnectCause;

import androidx.annotation.IntRange;

public interface CallController {

    int SPEAKER_ROUTE = 101, BLUETOOTH_ROUTE = 102, EARPIECE_ROUTE = 103;
    void hold(boolean on);
    void mute(boolean on);
    void hangup();
    void record(boolean on);
    void transfer(String dest);
    void decline();
    void answer();
    void sendDTMF(String digits);
    int getID();
    void setSpeaker(boolean on);
    void answer(boolean ignoreActive);
    void notifyDisconnected(int cause);
    void ring();
    void setRoute(@IntRange(from = SPEAKER_ROUTE, to = EARPIECE_ROUTE) int route);
    boolean isSpeaker();

    void fakeHold();
    void addToConference();
}
