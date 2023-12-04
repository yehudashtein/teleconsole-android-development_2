package com.telebroad.teleconsole.chat.viewModels;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.ReactionsDao;
import com.telebroad.teleconsole.db.models.Reactions;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.helpers.Utils;

import java.util.List;


public class ChatReactionRepository {
    private ReactionsDao reactionsDao;

    public ChatReactionRepository(Application application) {
        ChatDatabase chatDatabase = ChatDatabase.getInstance(application);
        reactionsDao = chatDatabase.reactionsDao();
    }
    public void SaveReactions(Reactions reactions){
        Utils.asyncTask(() -> reactionsDao.save(new Reactions(reactions)));
    }
    public LiveData<List<DataMessage.Reaction>> getReactions(String id, String topic){
        LiveData<List<DataMessage.Reaction>> reactionsLiveData = reactionsDao.GetReactions(id,topic);
        return reactionsLiveData;
    }
    public void deleteFromReactions(String from,String content){
        reactionsDao.deleteFromReactions(from,content);
    }
}
