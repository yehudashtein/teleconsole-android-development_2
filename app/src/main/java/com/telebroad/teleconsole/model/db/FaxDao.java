package com.telebroad.teleconsole.model.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.telebroad.teleconsole.model.Fax;

import java.util.List;

@Dao
public interface FaxDao extends BaseDao<Fax> {

    @Override
    @Query("DELETE FROM fax")
    void deleteAll();

    @Query("DELETE FROM fax WHERE id LIKE :id")
    void deleteFax(String id);

    @Query("DELETE FROM fax WHERE mailbox LIKE :mailbox")
    void deleteFaxFromBox(String mailbox);

    @Query("SELECT * FROM fax")
    LiveData<List<Fax>> getFaxList();

    @Query("SELECT * FROM fax where id LIKE :id")
    LiveData<Fax> getFax(String id);

    @Query("SELECT COUNT(*) FROM fax")
    int numberOfFaxes();

    @Query("SELECT COUNT(*) FROM fax where timestamp < :timestamp")
    int numberOfFaxesBefore(long timestamp);


    @Query("SELECT MIN(timestamp) FROM fax")
    long getEarliestTimestamp();

    @Query("SELECT * FROM fax WHERE timestamp = :timestamp")
    LiveData<Fax> getFaxFromTime(long timestamp);


    @Query("UPDATE fax SET needsNotification = 0")
    void setAllAsNotified();

    @Query("UPDATE fax SET dlr_status = :status, dlr_error = :error WHERE id LIKE :id")
    void updateDLR(String status, String error, String id);

    @Query("UPDATE fax SET needsNotification = 0 WHERE id - :id")
    void setAsNotified(String id);

}
