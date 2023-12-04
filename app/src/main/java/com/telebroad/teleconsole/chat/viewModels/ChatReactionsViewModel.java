package com.telebroad.teleconsole.chat.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.telebroad.teleconsole.db.models.Reactions;
import com.telebroad.teleconsole.chat.server.DataMessage;

import java.util.List;


public class ChatReactionsViewModel extends AndroidViewModel {
    private ChatReactionRepository chatReactionRepository;

    public ChatReactionsViewModel(@NonNull Application application) {
        super(application);
        chatReactionRepository = new ChatReactionRepository(application);
    }
    public void SaveReactions(Reactions reactions){
        chatReactionRepository.SaveReactions(reactions);
    }
    public LiveData<List<DataMessage.Reaction>> getReactions(String id, String topic){
        LiveData<List<DataMessage.Reaction>> reactionsLiveData = chatReactionRepository.getReactions(id,topic);
        return reactionsLiveData;
    }
    public void deleteFromReactions(String from,String content){
        chatReactionRepository.deleteFromReactions(from,content);
    }
}
