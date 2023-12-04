package com.telebroad.teleconsole.db.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.telebroad.teleconsole.model.db.BaseDao;

import java.util.List;

@Dao
public interface GroupMembersDao extends BaseDao<GroupMembers> {
    @Query("DELETE FROM GroupMembers")
    void deleteAll();
    @Query("select * from groupmembers where topic =:topic")
    LiveData<List<GroupMembers>>getAll(String topic);
    @Query("delete from groupmembers where topic = :topic and user =:fn")
    void deleteUser(String topic,String fn);
}
