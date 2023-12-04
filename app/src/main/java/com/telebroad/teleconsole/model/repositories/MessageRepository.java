package com.telebroad.teleconsole.model.repositories;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Message;

import java.util.ArrayList;
import java.util.List;

import static com.telebroad.teleconsole.model.repositories.MessageRepository.MessageListType.ALL;

public class MessageRepository {

    private final FaxRepository faxRepository;
    private final VoicemailRepository voicemailRepository;
    private final SMSRepository smsRepository;
    private MediatorLiveData<List<? extends Message>> activeList = new MediatorLiveData<>();
    private MediatorLiveData<List<Message>> allMessages;
    private MediatorLiveData<List<? extends Message>> filteredList;
    private MessageListType currentType;

    private static MessageRepository instance;
    public static MessageRepository getInstance(){
        if (instance == null){
            instance = new MessageRepository(AppController.getInstance());
        }
        return instance;
    }
    private MessageRepository(Application application){
        faxRepository = FaxRepository.getInstance(application);
        voicemailRepository = new VoicemailRepository(application);
        smsRepository = SMSRepository.getInstance(application);
    }

    public void refresh(){
        faxRepository.loadFaxesFromServer();
        voicemailRepository.loadVoicemailsFromServer();
        smsRepository.loadSMSFromServer();
    }


    public LiveData<List<? extends Message>> getActiveMessageList() {
        if (activeList.getValue() == null){
            setMessageListType(ALL);
        }
        return activeList;
    }

    public void setMessageListType(MessageListType type) {
        if (type == currentType){
            return;
        }
        currentType = type;
        switch (type){
            case ALL:
                activeList.removeSource(smsRepository.getAllSMS());
                activeList.removeSource(voicemailRepository.getAllVoicemails());
                activeList.removeSource(faxRepository.getAllFaxes());
                activeList.addSource(getAllMessages(), activeList::setValue);
                break;
            case SMS:
                activeList.removeSource(voicemailRepository.getAllVoicemails());
                activeList.removeSource(faxRepository.getAllFaxes());
                activeList.removeSource(allMessages);
                activeList.addSource(smsRepository.getAllSMS(), activeList::setValue);
                break;
            case FAX:
                activeList.removeSource(smsRepository.getAllSMS());
                activeList.removeSource(allMessages);
                activeList.removeSource(voicemailRepository.getAllVoicemails());
                activeList.addSource(faxRepository.getAllFaxes(), activeList::setValue);
                break;
            case VOICEMAIL:
                activeList.removeSource(smsRepository.getAllSMS());
                activeList.removeSource(allMessages);
                activeList.removeSource(faxRepository.getAllFaxes());
                activeList.addSource(voicemailRepository.getAllVoicemails(), activeList::setValue);
                break;
        }
        return;
    }

    public MediatorLiveData<List<Message>> getAllMessages() {
        if (allMessages == null){
            try {
                addMessageSources();
            }catch (IllegalStateException ise){
                Utils.logToFile("get all messages illegalstateexception " + ise.getMessage());
            }
        }
       // android.util.Log.d("LiveData02", "getting all " + allMessages);
        return allMessages;
    }

    private synchronized void addMessageSources() {
        allMessages = new MediatorLiveData<>();
        allMessages.addSource(voicemailRepository.getAllVoicemails(), voicemails -> {
            // android.util.Log.d("LiveData02", "voicemail observation " + voicemails.size() + " adding to " + allMessages);
                    Utils.updateLiveData(allMessages, mergeLists());
//            allMessages.setValue(mergeLists());
            });
        allMessages.addSource(faxRepository.getAllFaxes(), faxes -> allMessages.setValue(mergeLists()));
        allMessages.addSource(smsRepository.getAllSMS(), sms -> allMessages.setValue(mergeLists()));
    }

    private List<Message> mergeLists(){
        //android.util.Log.d("LiveData02", "merging");
        List<Message> messages = new ArrayList<>();
        messages.clear();
        addIfNotNull(messages, voicemailRepository.getAllVoicemails().getValue());
        addIfNotNull(messages, faxRepository.getAllFaxes().getValue());
        addIfNotNull(messages, smsRepository.getAllSMS().getValue());
        return messages;
    }

    private void addIfNotNull(List<Message> messages, List<? extends Message> repository) {
        if (repository != null){
            messages.addAll(repository);
        }
    }

    public LiveData<List<? extends Message>> getFilteredList() {
        if (filteredList == null){
            filteredList = new MediatorLiveData<>();
        }
        return filteredList;
    }

    public enum MessageListType{
        SMS, FAX , VOICEMAIL, ALL;
    }

}
