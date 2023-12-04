package com.telebroad.teleconsole.chat.viewModels;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.RepliesDao;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.helpers.Utils;

import java.util.List;

public class ChatRepliesRepository {
    private RepliesDao repliesDao;
    private  LiveData<List<Replies>> replies;
    //ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ChatRepliesRepository(Application application) {
        ChatDatabase chatDatabase = ChatDatabase.getInstance(application);
        repliesDao = chatDatabase.repliesDao();
        //replies = repliesDao.getAllReplies();

    }
    public void SaveReplies(DataMessage.Replies replies){
        //executorService.execute(() -> repliesDao.save(new Replies(replies)));
        Utils.asyncTask(() -> repliesDao.save(new Replies(replies)));
    }
    public LiveData<List<Replies>> getAllRepliesByTopic(String topic, String HeadNom){
        LiveData<List<Replies>> replies = repliesDao.getAllRepliesByTopic(topic,HeadNom);
        return  replies;
    }

}
