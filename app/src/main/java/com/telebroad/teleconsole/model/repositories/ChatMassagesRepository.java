package com.telebroad.teleconsole.model.repositories;
import androidx.lifecycle.LiveData;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.ChatMessageDAO;
import com.telebroad.teleconsole.db.models.ChatMessageDB;

import java.util.List;

public class ChatMassagesRepository {
    private ChatMessageDAO chatMessageDao;
    private static ChatMassagesRepository INSTANCE ;
    public static ChatMassagesRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatMassagesRepository();
        }
        return INSTANCE;
    }

    public ChatMassagesRepository() {
        chatMessageDao= ChatDatabase.getInstance().chatMessageDao();
    }
    public LiveData<List<ChatMessageDB>> getAllRepliesByTopic(String topic){
        LiveData<List<ChatMessageDB>> replies = chatMessageDao.getTopicChat(topic);
        return  replies;
    }
}
