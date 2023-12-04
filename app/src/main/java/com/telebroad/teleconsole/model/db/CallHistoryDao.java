package com.telebroad.teleconsole.model.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.notification.HistoryGroupNotification;

import java.util.Arrays;
import java.util.List;

@Dao
public interface CallHistoryDao extends BaseDao<CallHistory> {


    @Query("DELETE FROM calls")
    void deleteAll();

    @Query("DELETE FROM calls WHERE timestamp <= :time ")
    void deleteUntil(long time);

    @Query("SELECT * FROM calls")
    LiveData<List<CallHistory>> getCallHistoryList();

    @Query("SELECT * FROM calls WHERE direction LIKE 'IN' COLLATE NOCASE  AND status <= 1")
    LiveData<List<CallHistory>> getMissedCallHistoryList();

    @Query("SELECT * FROM calls WHERE direction LIKE 'IN' COLLATE NOCASE AND status >= 2")
    LiveData<List<CallHistory>> getReceivedCallHistoryList();

    @Query("SELECT * FROM calls WHERE direction LIKE 'OUT' COLLATE NOCASE")
    LiveData<List<CallHistory>> getOutgoingCallHistoryList();

//    @Query("SELECT * FROM calls WHERE needsNotification > 0 GROUP BY snumber")
//    List<List<CallHistory>> getCallHistoryNeedingNotification();

    @Query("SELECT Count(*) FROM calls")
    int getRowCount();

    default void refresh(List<CallHistory> newEntites){
        refresh(newEntites.toArray(new CallHistory[]{}));
    }

    @Override
    default void refresh(CallHistory[] newEntities) {
        if (newEntities.length > 0) {
            Arrays.sort(newEntities);
            deleteUntil(newEntities[0].getTimeStamp());
        }
        save(newEntities);
    }

    @Query("SELECT Count(*) AS count, sname, snumber FROM calls WHERE needsNotification = 1 GROUP BY sname, snumber")
    List<HistoryGroupNotification> getAllNeedingNotification();

    @Query("SELECT Count(*) FROM calls WHERE snumber LIKE :snumber COLLATE NOCASE AND needsNotification = 1")
    int getCountBySnumber(String snumber);

    @Query("DELETE FROM calls WHERE id LIKE :id")
    void delete(String id);

    @Query("UPDATE calls SET needsNotification = 0")
    void setAllAsNotified();

    @Query("UPDATE calls SET needsNotification = 0 WHERE id LIKE :id COLLATE NOCASE")
    void setIDAsNotified(String id);

    @Query("UPDATE calls SET needsNotification = 0 WHERE snumber LIKE :snumber COLLATE NOCASE")
    void setAsNotified(String snumber);

    @Query("SELECT * FROM calls WHERE snumber IN  (:numbers) ORDER BY timestamp DESC")
    List<CallHistory> getCallForContact(List<String> numbers);
}
