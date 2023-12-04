package com.telebroad.teleconsole.model.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.telebroad.teleconsole.BuildConfig;
import com.telebroad.teleconsole.model.Voicemail;

import java.util.List;

import crl.android.pdfwriter.Base;

@Dao
public interface VoicemailDao extends BaseDao <Voicemail> {
    @Override
    @Query("DELETE FROM voicemail")
    void deleteAll();

    @Query("SELECT * FROM voicemail")
    LiveData<List<Voicemail>> getVoicemailList();

    @Query("SELECT * FROM voicemail WHERE id LIKE :id")
    LiveData<Voicemail> getVoicemail(String id);

    @Query("DELETE FROM voicemail WHERE id LIKE :id")
    void deleteVoicemail(String id);

    @Query("SELECT * FROM voicemail WHERE timestamp = :timestamp")
    LiveData<Voicemail> getVoicemailFromTime(long timestamp);

    @Query("DELETE FROM voicemail WHERE mailbox = :mailbox ")
    void deleteVoicemailsFromBox(String mailbox);

    @Query("UPDATE voicemail SET needsNotification = 0")
    void setAllAsNotified();

    @Query("UPDATE voicemail SET needsNotification = 0 WHERE timestamp = :timestamp")
    void setAsNotified(long timestamp);

    @Query("UPDATE voicemail SET needsNotification = 1 WHERE timestamp = :timestamp")
    void setAsNotNotified(long timestamp);

    @Query("UPDATE voicemail SET transcription = :transcription WHERE id = :id")
    void setTranscription(String transcription, String id);

    @Query("SELECT COUNT(*) FROM voicemail WHERE needsNotification = 1")
    int getCountNeedingNotification();

    @Query("SELECT timestamp FROM voicemail WHERE needsNotification = 1")
    List<Long> getTimestampsNeedingNotification();


    @Query("SELECT COUNT(*) FROM voicemail where timestamp < :timestamp")
    int numberOfVoicemailsBefore(long timestamp);


    @Query("SELECT MIN(timestamp) FROM voicemail")
    long getEarliestTimestamp();
   @Query("select * from voicemail where transcription is not null AND transcription NOT Like '' ")
   List<Voicemail>getVoicemailsWithTranscription();


    @Override
    default void refresh(Voicemail[] newEntities) {
        List<Long> timestampsNeedingNotification = getTimestampsNeedingNotification();
        List<Voicemail> transcriptions = getVoicemailsWithTranscription();
        //android.util.Log.d("TSCPT", ": transcripts size is " + transcriptions.size());
        BaseDao.super.refresh(newEntities);
        for (Long timeStamp : timestampsNeedingNotification){
            setAsNotNotified(timeStamp);
        }
        for (Voicemail vm : transcriptions){
            //android.util.Log.d("TSCPT", "Setting transcription of " + vm.getId() + " to " + vm.getTranscription() );
            setTranscription(vm.getTranscription(), vm.getId());
        }
    }


}
