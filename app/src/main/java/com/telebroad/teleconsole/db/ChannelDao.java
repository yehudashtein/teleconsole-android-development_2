package com.telebroad.teleconsole.db;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.MapInfo;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.model.db.BaseDao;

import java.util.List;
import java.util.Map;

@Dao
public interface ChannelDao extends BaseDao<ChannelDB> {

    @Query("SELECT * From channels")
    List<ChannelDB> getChannelsForMention();
    @MapInfo(keyColumn = "name", valueColumn = "name")
    @Query("SELECT * FROM channels")
    LiveData<Map<String, ChannelDB>> getUsersByTopic();
    @MapInfo(keyColumn = "topic", valueColumn = "name")
    @Query("SELECT * FROM channels where subbedTo LIKE 'me'")
    LiveData<Map<String, ChannelDB>> getMeUsersByTopic();
    @Query("SELECT * FROM channels where subbedTo LIKE 'me' AND topic=:topic")
    LiveData<ChannelDB> getMeUsersByTopic(String topic);
    @Query("SELECT * FROM channels where subbedTo LIKE 'fnd' AND topic=:topic")
    LiveData<ChannelDB> getFndUsersByTopic(String topic);
    @Query("SELECT * FROM channels where subbedTo LIKE 'fnd' AND topic=:topic")
    ChannelDB getFndUsersByTopic1(String topic);
    @Query("SELECT * FROM channels WHERE subbedTo LIKE 'me' AND `group` > 0 order by name asc ")
    LiveData<List<ChannelDB>> getTeamChannels();
    @Query("SELECT * FROM channels WHERE subbedTo LIKE 'me' AND `group` > 0 order by name asc ")
    ListenableFuture<List<ChannelDB>> getTeamChannelsListenableFuture();
    @Query("SELECT * FROM channels WHERE subbedTo LIKE 'me' AND `group` = 0")
    LiveData<List<ChannelDB>> getDirectChannels();
    @Query("SELECT * FROM channels WHERE  `group` = 0")
    LiveData<List<ChannelDB>> getDirectNotMetChannels();
    @Query("SELECT * FROM channels WHERE  name Like :search ")
    List<ChannelDB> getDirectChannelsSearch(String search);
    @MapInfo(keyColumn = "topic", valueColumn = "topic")
    @Query("SELECT * FROM channels")
    LiveData<Map<String, ChannelDB>> getUsersByName();
      @Query("SELECT * FROM channels where topic =:topic ")
      ListenableFuture<ChannelDB> getUsersByName(String topic);
    @MapInfo(keyColumn = "id", valueColumn = "id")
    @Query("select *, topic as id from channels where subbedTo = 'fnd' And `group` = 1 and not subbedTo = 'me'")
    LiveData<Map<String, ChannelDB>> getFNDGroupChannels();
    @Query("update channels set unread = unread + 1 where topic = :topic AND subbedTo = 'me'")
    void incrementUnread(String topic);
    @Query("update channels set unread = 0 where topic = :topic")
    void setUnreadToZero(String topic);
    @Query("DELETE FROM channels")
    void deleteAll();
    @Query("select * from channels where not subbedTo ='me' and not subbedTo = 'fnd' and topic=:topic")
    LiveData<List<ChannelDB>> getGroupsMembersByChannel(String topic);
    @Query("select unread from channels where topic = :topic and subbedTo ='me' ")
    LiveData<Integer>getUnreadNumberByTopic(String topic);
    @Query("update channels set online = :status where topic = :topic AND subbedTo = 'me'")
    void UpdateStatus(String topic,int status);
    @Query("select online from channels where topic = :topic and subbedTo ='me' AND `group`='0' ")
    LiveData<Integer>getStatus(String topic );
    @Query("delete from channels where subbedTo ='me' and topic = :topic")
    void DeleteChanel(String topic);
    @Query("UPDATE channels set imageUrl=:imageUrl  where topic = :name")
    void UpdateImageUrl(String imageUrl,String name);
    @Query("UPDATE channels SET name = :name, acsMode = :acsMode, description = :description WHERE topic = :topic AND subbedTo = 'me'")
    void UpdateName(String name,String topic,String acsMode,String description);
    @Query("UPDATE channels set name=:name , description = :description where topic = :topic and subbedTo ='me'")
    void UpdateName(String name,String description ,String topic);
    @Query("UPDATE channels set name=:name , description = :description,acsMode = :acsMode where topic = :topic")
    void UpdateName1(String name,String description ,String topic,String acsMode);
    @Query("SELECT * FROM channels WHERE subbedTo = 'fnd' AND topic = :topic AND `group`='1'")
    LiveData<ChannelDB> getAcsModeForFnd(String topic);
    @Query("SELECT * FROM channels")
    ListenableFuture<List<ChannelDB>> getAllChannels();
}
