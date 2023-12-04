package com.telebroad.teleconsole.pjsip;

import androidx.annotation.NonNull;

public class CallStates{

    private boolean isHold = false;
    private boolean isMute = false;
    private boolean isRecording = false;
    private boolean isRinging = false;
    private boolean isDone = false;
    private boolean isEarly = false;

    public boolean isHold() {
        return isHold;
    }

    public void setHold(boolean hold) {
        isHold = hold;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isRinging() {
        return isRinging;
    }

    public void setRinging(boolean ringing) {
        isRinging = ringing;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @Override
    @NonNull
    public String toString() {
        return "CallStates{" +
                "isHold=" + isHold +
                ", isMute=" + isMute +
                ", isRecording=" + isRecording +
                ", isRinging=" + isRinging +
                ", isEarly=" + isEarly +
                ", isDone=" + isDone +
                "}, id=" + hashCode() ;
    }

    public boolean isEarly() {
        return isEarly;
    }

    public void setEarly(boolean early) {
        isEarly = early;
    }
}
