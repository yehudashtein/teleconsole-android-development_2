package com.telebroad.teleconsole.chat;

public class ChatManager {
    private ChatManager instance;
    public ChatManager getInstance(){
        if (instance == null){
            instance = new ChatManager();
        }
        return instance;
    }

    public void addChannels(){

    }
}
