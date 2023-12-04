package com.telebroad.teleconsole.chat.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.chat.server.DataMessage;

import java.util.List;

public class ChatRepliesViewModel extends AndroidViewModel {
    private ChatRepliesRepository chatRepliesRepository;
    LiveData<List<Replies>> replies;

    public ChatRepliesViewModel(@NonNull Application application) {
        super(application);
        chatRepliesRepository = new ChatRepliesRepository(application);
    }

    //public LiveData<List<Replies>>getAllReplies(){
    // return replies;
    //}
    public void SaveReplies(DataMessage.Replies replies) {
        chatRepliesRepository.SaveReplies(replies);
    }
    public LiveData<List<Replies>> getAllRepliesByTopic(String topic, String HeadNom) {
        LiveData<List<Replies>> replies = chatRepliesRepository.getAllRepliesByTopic(topic, HeadNom);
        return replies;
    }
}
