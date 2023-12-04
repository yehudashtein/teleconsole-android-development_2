package com.telebroad.teleconsole.chat.viewModels;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.db.models.ChatMessageDB;
import com.telebroad.teleconsole.controller.AppController;

import java.util.Map;

public class TeamsEventMessageViewModel extends ChatMessageViewModel{

    private ChannelDB actor;
    private ChannelDB user;
    private ChannelDB group;
    private String action = "";
    public TeamsEventMessageViewModel(ChatMessageDB chatMessageDB) {
        super(chatMessageDB);
        Object content = getContent();
        if (content instanceof Map){
            Map<?,?> contentMap = (Map<?,?>) content;
            actor = ChatViewModel.getInstance().getChannelsByTopic().get(contentMap.get("ActorID"));
            user = ChatViewModel.getInstance().getChannelsByTopic().get(contentMap.get("UserID"));
            group = ChatViewModel.getInstance().getChannelsByTopic().get(chatMessageDB.getTopic());
            action = String.valueOf(contentMap.get("Type"));
        }
    }

    @Override
    public String getText() {

        return actor == null ?
                AppController.getAppString(R.string.chat_event_no_actor, user == null ? "Unknown" : user.getName() , getActionText()) :
                AppController.getAppString(R.string.chat_event, user.getName(), getActionText(), actor.getName());

    }

    private String getActionText(){
        String groupName = group == null ? AppController.getAppString(R.string.chat_group) : group.getName();
        switch (action){
            case "null":
            case "":
                return "";
            case "UserJoinedGroup":
                return  AppController.getAppString(actor == null ? R.string.chat_joined : R.string.chat_added, groupName);
            case "UserLeftGroup":
                return  AppController.getAppString(actor == null ? R.string.chat_left : R.string.chat_removed, groupName);
            default:
                return action;
        }

    }
}
