package com.telebroad.teleconsole.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.model.db.BaseDao;

import java.util.List;
@Dao
public interface RepliesDao extends BaseDao<Replies> {
    @Query("select * from replies")
    LiveData<List<Replies>> getAllReplies();
    @Query("SELECT Max(seq) From replies Where topic Like :topic")
    int maxSeqForTopic(String topic);
    @Override
    @Query("DELETE FROM replies")
    void deleteAll();
    @Query("Delete  FROM replies where topic=:topic")
    void deleteByTopic(String topic);
    @Query("select * from replies where head like :HeadNom and topic = :topic order by seq asc")
    LiveData<List<Replies>> getAllRepliesByTopic(String topic,String HeadNom);
    @Query("select * from replies where head like :HeadNom and topic = :topic order by seq asc")
    ListenableFuture<List<Replies>> getAllRepliesByTopic1(String topic,String HeadNom);
    @Query("UPDATE replies SET reaction = :reactions WHERE topic = :topic AND seq = :seq")
    void updateReplyReactions(String reactions,String topic,int seq);
    @Query("UPDATE replies set reaction=rtrim(reaction,']') ||','||:reac||']' WHERE seq = :seq and topic = :topic")
    void updateReactions(String reac,int seq,String topic);
    @Query("update replies set reaction =:reaction where topic = :topic and seq =:seq")
    void updateInitialReactions(String reaction,String topic,int seq);
    @Query("select reaction from replies where topic = :topic AND seq =:seq")
    ListenableFuture<String> livedataReactionsForTopic(String topic, int seq);
    @Query("DELETE FROM replies WHERE topic = :topic AND seq = :seq")
    void deleteMassage(String topic,int seq);
    @Query("Update replies set content= :content where seq =:seq and topic = :topic")
    void UpdateContent(String content,int seq,String topic);
    @Query("select * from replies where seq=:seq and topic=:topic")
    ListenableFuture<Replies> getReplies(int seq,String topic);
    @Query("select * from replies where seq=:seq and topic=:topic")
    Replies getReplies1(int seq,String topic);
    @Query("select * from replies where seq=:seq and topic=:topic")
    LiveData<Replies> getReplies2(int seq,String topic);
}
