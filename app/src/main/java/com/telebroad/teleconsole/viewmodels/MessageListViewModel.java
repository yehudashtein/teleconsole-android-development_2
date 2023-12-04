package com.telebroad.teleconsole.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.model.Fax;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.repositories.FaxRepository;
import com.telebroad.teleconsole.model.repositories.MessageRepository;
import com.telebroad.teleconsole.model.repositories.SMSRepository;
import com.telebroad.teleconsole.model.repositories.VoicemailRepository;

import java.util.List;

public class MessageListViewModel extends ViewModel {

    private MessageRepository messageRepository = MessageRepository.getInstance();
    //public LiveData<List<? extends Message>> messagesList = messageRepository.getActiveMessageList();

    public void refresh() {
        messageRepository.refresh();
    }

    public LiveData<? extends List<? extends Message>> getMessageList(MessageRepository.MessageListType type) {
        // messageRepository.getMessageList(type);
        switch (type) {
            case SMS:
                return SMSRepository.getInstance().getAllSMS();
            case FAX:
                return FaxRepository.getInstance(AppController.getInstance()).getAllFaxes();
            case VOICEMAIL:
                return new VoicemailRepository(AppController.getInstance()).getAllVoicemails();
            case ALL:
                return messageRepository.getAllMessages();
            default:
                return messageRepository.getAllMessages();
        }
    }

    public LiveData<? extends List<? extends Message>> getFilteredMessageList(MessageRepository.MessageListType type) {
        switch (type) {

            case SMS:
                break;
            case FAX:
                break;
            case VOICEMAIL:
                break;
            case ALL:
                break;
            default:
                return messageRepository.getFilteredList();
        }
        return messageRepository.getFilteredList();
    }

}
