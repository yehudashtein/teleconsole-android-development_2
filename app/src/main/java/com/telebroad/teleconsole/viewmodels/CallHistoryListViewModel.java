package com.telebroad.teleconsole.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.model.repositories.CallHistoryRepository;

import java.util.List;

public class CallHistoryListViewModel extends ViewModel {

    private CallHistoryRepository callHistoryRepository = CallHistoryRepository.getInstance();
    public LiveData<List<CallHistory>> callHistoryList = callHistoryRepository.getActiveCallList();

    public void loadMoreHistory(int offset){
        callHistoryRepository.addCallHistoryFromServer(offset);
    }

    public void setCallListType(CallHistoryRepository.CallListType type){
        callHistoryRepository.setActiveCallList(type);
    }

    public void refreshCallHistory(){
        callHistoryRepository.loadCallHistoryFromServer();
    }

}
