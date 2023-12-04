package com.telebroad.teleconsole.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ChatMassagesViewModelFactory implements ViewModelProvider.Factory {
    private String topic;
    public ChatMassagesViewModelFactory(String topic) {
        this.topic = topic;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatViewModel2.class)) {
            return (T) new ChatViewModel2(topic);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
