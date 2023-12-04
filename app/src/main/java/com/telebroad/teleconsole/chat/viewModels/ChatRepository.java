package com.telebroad.teleconsole.chat.viewModels;

import android.app.Application;

import com.telebroad.teleconsole.db.ChannelDao;
import com.telebroad.teleconsole.db.ChatDatabase;

public class ChatRepository {
    ChannelDao channelDao;

    public ChatRepository(Application application) {
        ChatDatabase chatDatabase = ChatDatabase.getInstance(application);
        channelDao = chatDatabase.channelDao();
    }
}
