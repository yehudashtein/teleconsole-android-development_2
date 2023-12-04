package com.telebroad.teleconsole.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.telebroad.teleconsole.db.models.ChatMessageDB;
import com.telebroad.teleconsole.model.repositories.ChatMassagesRepository;

import java.util.List;

public class ChatViewModel2 extends ViewModel {
    private final ChatMassagesRepository repository;
    private String topic;
    private static ChatViewModel2 instance;

    public ChatViewModel2(@NonNull String topic) {
        repository = new ChatMassagesRepository();
        this.topic = topic;
    }
    public static synchronized ChatViewModel2 getInstance(Context context, String topic) {
        if (instance == null) {
            instance = new ViewModelProvider((ViewModelStoreOwner) context, new ChatMassagesViewModelFactory(topic)).get(ChatViewModel2.class);
        }
        return instance;
    }

    public LiveData<List<ChatMessageDB>> getTopicChatLiveData() {
        LiveData<List<ChatMessageDB>> topicChatLiveData = repository.getAllRepliesByTopic(topic);
        return topicChatLiveData;
    }
}
