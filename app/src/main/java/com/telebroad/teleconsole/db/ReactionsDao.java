package com.telebroad.teleconsole.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.telebroad.teleconsole.db.models.Reactions;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.model.db.BaseDao;

import java.util.List;


@Dao
public interface ReactionsDao extends BaseDao<Reactions> {
    @Override
    @Query("DELETE FROM reactions")
    void deleteAll();
    @Query("select * from reactions where id like:id and topic=:from order by id")
    LiveData<List<DataMessage.Reaction>> GetReactions(String id, String from);
    @Query("delete from reactions where `from` =:from and content = :content")
    void deleteFromReactions(String from,String content);
    @Query("delete from reactions where topic=:topic")
    void deleteTopicReactions(String topic);
}
