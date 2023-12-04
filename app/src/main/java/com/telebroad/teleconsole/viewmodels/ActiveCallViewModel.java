package com.telebroad.teleconsole.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.pjsip.CallGroup;
import com.telebroad.teleconsole.pjsip.CallManager;

public class ActiveCallViewModel extends ViewModel {
    public LiveData<SipManager.UIState> hasActiveCall = SipManager.getInstance().getUIState();
    public LiveData<CallGroup> activeCallGroup = CallManager.getInstance().getLiveCall();
}
