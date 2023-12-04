package com.telebroad.teleconsole.model.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.annotation.NonNull;

import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.model.Fax;
import com.telebroad.teleconsole.model.SMS;
import com.telebroad.teleconsole.model.TCPhoneNumber;
import com.telebroad.teleconsole.model.TeleConsoleContact;
import com.telebroad.teleconsole.model.Voicemail;

@Database( entities = {TeleConsoleContact.class, CallHistory.class, Fax.class, SMS.class, Voicemail.class, TCPhoneNumber.class}, version = 29, exportSchema = false)
public abstract class TeleConsoleDatabase extends RoomDatabase {

    private static TeleConsoleDatabase INSTANCE;

    public abstract ContactDao contactDao();
    public abstract FaxDao faxDao();
    public abstract CallHistoryDao callHistoryDao();
    public abstract SMSDao smsDao();
    public abstract VoicemailDao voicemailDao();
    @NonNull
    public static TeleConsoleDatabase getInstance(final Context context){
        synchronized (TeleConsoleDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TeleConsoleDatabase.class, "teleconsole_database").fallbackToDestructiveMigration().build();
            }
        }
        return INSTANCE;
    }
}
