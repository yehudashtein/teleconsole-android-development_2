package com.telebroad.teleconsole.model.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.telebroad.teleconsole.model.SMS;

import java.util.List;

@Dao
public interface SMSDao extends BaseDao<SMS> {
    @Override
    @Query("DELETE FROM sms WHERE idx > 0")
    void deleteAll();

    @Query("SELECT * FROM sms WHERE idx > 0 ")
    LiveData<List<SMS>> getSMSConversationList();

    @Query("SELECT blocked FROM sms WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber)) AND blocked == 1 LIMIT 1")
    LiveData<Boolean> isConversationBlocked(String myNumber, String otherNumber);


    @Query("UPDATE sms SET blocked = :value WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber)) ")
    void setConversationBlocked(String myNumber, String otherNumber, boolean value);
    @Query("DELETE FROM sms WHERE idx > 0")
    void deleteSMSConversationList();

    @Query ("DELETE FROM sms WHERE id = :id")
    void deleteSMS(String id);

    @Query("DELETE FROM sms WHERE timestamp <= :timestamp")
    void deleteUntil(long timestamp);

    @Query("SELECT * FROM sms WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber)) AND idx > 0")
    SMS getMostRecentSMSFromConversation(String myNumber, String otherNumber);

    @Query("UPDATE sms SET timestamp = :timestamp, msgdata = :msgData WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber)) AND idx > 0")
    void updateMostRecentSMSFromConversation(String myNumber, String otherNumber, String msgData, long timestamp);

    @Query("UPDATE sms SET idx = 1 WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber)) AND idx > 0")
    void setSMSForConversation(String myNumber, String otherNumber);

    @Query("DELETE FROM sms WHERE (direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :smsLine) OR (direction LIKE 'OUT' COLLATE NOCASE AND sender LIKE :smsLine)")
    void deleteAllFromLine(String smsLine);

    @Query("DELETE FROM sms WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber))" +
            " AND (idx = 0 OR lid LIKE 'sent')")
    void deleteConversation(String myNumber, String otherNumber);

    @Query("DELETE FROM sms WHERE ((direction LIKE 'IN' COLLATE NOCASE" +
            " AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE " +
            "AND receiver LIKE :otherNumber AND sender LIKE :myNumber)) " +
            "AND idx == 0 AND timestamp <= :time")
    void deleteConversation(String myNumber, String otherNumber, long time);

    @Query("SELECT * FROM sms WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber)) AND (idx = 0 OR lid LIKE 'sent')  ORDER BY timestamp DESC")
    LiveData<List<SMS>> getConversation(String myNumber, String otherNumber);



    @Query("SELECT * FROM sms WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber)) AND needsNotification = 1 ORDER BY timestamp DESC")
    List<SMS> getNotificationConversation(String myNumber, String otherNumber);

    @Query("SELECT * FROM sms WHERE ((direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber))")
    List<SMS> getConversationList(String myNumber, String otherNumber);

    @Query("UPDATE sms SET isNew = 0 WHERE id LIKE :id")
    void setRead(String id);

    @Query("UPDATE sms SET  dlr_status = :status, dlr_error = :error WHERE id LIKE :id")
    void updateDLR(String status, String error, String id);

    @Query("UPDATE sms SET needsNotification = 0")
    void setAllAsNotified();

    @Query("UPDATE sms SET sending = 0 WHERE timestamp = :timestamp")
    void setAsSent(long timestamp);
    @Query("UPDATE sms SET needsNotification = 0 WHERE id - :id")
    void setAsNotified(String id);

    @Query("SELECT timestamp FROM sms WHERE needsNotification = 1")
    List<Long> getTimestampsNeedingNotification();

    @Query("UPDATE sms SET needsNotification = 1 WHERE timestamp = :timestamp")
    void setAsNotNotified(long timestamp);

    @Query("UPDATE sms SET needsNotification = 0 WHERE (direction LIKE 'IN' COLLATE NOCASE AND receiver LIKE :myNumber AND sender LIKE :otherNumber) " +
            "OR (direction LIKE 'OUT' COLLATE NOCASE AND receiver LIKE :otherNumber AND sender LIKE :myNumber)")
    void setConversationAsNotified(String myNumber, String otherNumber);

    @Override
    default void refresh(SMS[] newEntities) {
        //android.util.Log.d("Refresh", "Refreshing");
        List<Long> timestampsNeedingNotification  = getTimestampsNeedingNotification();
        if (newEntities.length == 0){
            deleteAll();
        }else{
            deleteUntil(newEntities[0].getTimestamp());
        }
        save(newEntities);
        for (Long timeStamp : timestampsNeedingNotification){
            setAsNotNotified(timeStamp);
        }
    }

    @Transaction
    default void refreshConversation(SMS[] newEntities, String myNumber, String otherNumber){
        deleteConversation(myNumber, otherNumber);
        save(newEntities);
    }

    @Transaction
    default void refreshList(SMS[] newEntities){
        deleteSMSConversationList();
        save(newEntities);
    }

    @Query("UPDATE sms SET isNew = 1 WHERE timestamp LIKE :timestamp")
    void setSMSUnread(long timestamp);
}
