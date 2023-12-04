package com.telebroad.teleconsole.chat.viewModels;

import com.telebroad.teleconsole.db.models.ChatMessageDB;

public class FullChatMessageViewModel extends ChatMessageViewModel{
    public FullChatMessageViewModel(ChatMessageDB chatMessageDB) {
        super(chatMessageDB);
    }
}
