package com.telebroad.teleconsole.model.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.telebroad.teleconsole.model.TCPhoneNumber;
import com.telebroad.teleconsole.model.TeleConsoleContact;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface ContactDao extends BaseDao<TeleConsoleContact> {

    default String getTableName(){
        return "contact";
    }

    @Query("DELETE FROM contact")
    void deleteAll();

    @Query("SELECT * FROM contact WHERE home LIKE :queryString OR mobile LIKE :queryString  OR work LIKE :queryString OR fax LIKE :queryString OR fname || ' ' || lname LIKE :queryString COLLATE NOCASE")
    LiveData<List<TeleConsoleContact>> searchContacts(String queryString);

    @Query("SELECT * FROM contact WHERE (home LIKE :queryString OR mobile LIKE :queryString OR work LIKE :queryString OR fax LIKE :queryString OR fname || ' ' || lname LIKE :queryString COLLATE NOCASE) AND contactType LIKE 'corporate'")
    LiveData<List<TeleConsoleContact>> searchCompanyContacts(String queryString);

    @Query("SELECT * FROM contact WHERE (home LIKE :queryString OR mobile LIKE :queryString OR work LIKE :queryString OR fax LIKE :queryString OR fname || ' ' || lname LIKE :queryString COLLATE NOCASE) AND contactType LIKE 'personal' ")
    LiveData<List<TeleConsoleContact>> searchPersonalContacts(String queryString);

    @Query("SELECT * FROM contact WHERE (pbxLine LIKE :phoneNumber OR extension LIKE :phoneNumber ) AND contactType LIKE 'corporate'")
    LiveData<List<TeleConsoleContact>> matchCompanyContactsToNumber(String phoneNumber);

    @Query("SELECT * FROM contact WHERE home LIKE :phoneNumber OR mobile LIKE :phoneNumber OR work LIKE :phoneNumber OR fax LIKE :phoneNumber" +
            " OR ((extension LIKE :phoneNumber OR pbxLine LIKE :phoneNumber) AND contactType LIKE 'corporate')  ")
    LiveData<List<TeleConsoleContact>> matchContactsToNumber(String phoneNumber);

    @Query("SELECT * FROM contact WHERE home LIKE :phoneNumber OR mobile LIKE :phoneNumber OR work LIKE :phoneNumber OR fax LIKE :phoneNumber")
    List<TeleConsoleContact> matchContactsToNumberList(String phoneNumber);

    @Query("SELECT * FROM contact")
    LiveData<List<TeleConsoleContact>> loadAllContacts();

    @Query("SELECT * FROM contact WHERE contactType LIKE 'corporate' ORDER BY (fname || ' ' || lname) COLLATE NOCASE")
    LiveData<List<TeleConsoleContact>> getCorporateContacts();

    @Query("SELECT * FROM contact WHERE contactType LIKE 'personal'")
    LiveData<List<TeleConsoleContact>> getPersonalContacts();

    @Query("SELECT * FROM contact WHERE id LIKE :id")
    LiveData<TeleConsoleContact> getNameFromID (String id);

    @Query("SELECT * FROM contact WHERE id LIKE :id")
    TeleConsoleContact getContactFromID(String id);

    @Query("SELECT * FROM contact WHERE id LIKE :id")
    LiveData<TeleConsoleContact> getLiveContactFromID(String id);

    @Insert(onConflict = REPLACE)
    void savePhoneNumbers(List<TCPhoneNumber> numbers);

    @Query("DELETE FROM phoneNumber")
    void deleteAllPhoneNumbers();

    default void refresh(TeleConsoleContact[] newEntities){
        //android.util.Log.d("Contacts Ref", "starting");
        deleteAll();
        //android.util.Log.d("Contacts Ref", "deleting");
        save(newEntities);
        //android.util.Log.d("Contacts Ref", "saved");
    }

    @Query("DELETE FROM contact WHERE id LIKE :contactID")
    void deleteContact(String contactID);
}
