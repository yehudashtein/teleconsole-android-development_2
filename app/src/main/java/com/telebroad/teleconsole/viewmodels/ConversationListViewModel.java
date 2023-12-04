package com.telebroad.teleconsole.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.SMS;
import com.telebroad.teleconsole.model.repositories.SMSRepository;

import java.util.List;

public class ConversationListViewModel extends ViewModel {


    private final String myNumber;
    private final String otherNumber;
    private final SMSRepository smsRepository = SMSRepository.getInstance();
    public LiveData<List<SMS>> conversationList;
    public LiveData<Boolean> isBlocked;

    ConversationListViewModel(String myNumber, String otherNumber){
        this.myNumber = myNumber;
        this.otherNumber = otherNumber;
        conversationList = smsRepository.getConversation(myNumber, otherNumber);
        isBlocked = smsRepository.getConversationBlocked(myNumber, otherNumber);
    }

    public void setBlocked(boolean value){
        Utils.asyncTask(() -> smsRepository.setConversationBlocked(myNumber, otherNumber, value));
    }
    public void delete(){
        smsRepository.deleteConversation(myNumber, conversationList.getValue());
    }

}
