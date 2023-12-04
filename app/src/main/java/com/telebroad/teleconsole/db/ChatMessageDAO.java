package com.telebroad.teleconsole.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;
import com.telebroad.teleconsole.db.models.ChatMessageDB;
import com.telebroad.teleconsole.model.db.BaseDao;

import java.util.List;

@Dao
public interface ChatMessageDAO extends BaseDao<ChatMessageDB> {

    @Query("SELECT DISTINCT  * FROM messages WHERE topic LIKE :topic ORDER BY messageDate asc")
    LiveData<List<ChatMessageDB>> getTopicChat(String topic);
    @Query("select reactions from messages where topic = :topic AND seq =:seq")
    ListenableFuture<String> livedataReactionsForTopic(String topic,int seq);
    @Override
    @Query("DELETE FROM messages")
    void deleteAll();
    @Query("Delete  FROM messages where topic=:topic")
    void deleteByTopic(String topic);
    @Query("SELECT Max(seq) From messages Where topic Like :topic")
    int maxSeqForTopic(String topic);
    @Query("select COUNT(*) from messages Where topic Like :topic")
    int messagesListSize(String topic);
    @Query("SELECT Min(seq) From messages Where topic Like :topic")
    ListenableFuture<Integer> livedataMinSeqForTopic(String topic);
    @Query("select message from messages where message =:message")
    LiveData<List<String>> getTsByMessage(String message);
    @Query("select message from messages where topic =:topic and ts = :ts ")
    LiveData<List<String>> getByMessageByTopicAndTs(String topic,String ts);
    @Query("UPDATE messages set reactions=rtrim(reactions,']') ||','||:reac||']' WHERE seq = :seq and topic = :topic")
    void updateReactions(String reac,int seq,String topic);
    @Query("update messages set reactions =:reaction where topic = :topic and seq =:seq")
    void updateInitialReactions(String reaction,String topic,int seq);
   @Query("UPDATE messages set reply= '['||:replys||',' ||trim(reply,'[')  WHERE seq = :seq and topic = :topic")
    void updateReplies(String replys,int seq,String topic);
    @Query("SELECT count(*) > 0 FROM messages WHERE seq =:seq AND topic = :topic ")
    ListenableFuture<Boolean> hasMessageWithSeq (int seq,String topic);
    @Query("DELETE FROM messages WHERE topic = :topic AND seq = :seq")
    void deleteMassage(String topic,int seq);
    @Query("Update messages set message= :content where seq =:seq and topic = :topic")
    void UpdateContent(String content,int seq,String topic);



}
