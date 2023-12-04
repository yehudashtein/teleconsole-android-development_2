package com.telebroad.teleconsole.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.db.models.ChatMessageDB;
import com.telebroad.teleconsole.db.models.GroupMembers;
import com.telebroad.teleconsole.db.models.GroupMembersDao;
import com.telebroad.teleconsole.db.models.Reactions;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.chat.server.DataMessage;
import com.telebroad.teleconsole.controller.AppController;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Database( entities = {ChannelDB.class, ChatMessageDB.class, Replies.class, Reactions.class, GroupMembers.class}, version = 27, exportSchema = false)
@TypeConverters({ChatDatabase.ReactionConverter.class, ChatDatabase.ObjectConverter.class, ChatDatabase.ListConverter.class,ChatDatabase.RepliesHeadConverter.class,ChatDatabase.ReactionConverter2.class})
public abstract class ChatDatabase extends RoomDatabase {
    private static ChatDatabase INSTANCE;

    public abstract ChannelDao channelDao();
    public abstract ChatMessageDAO chatMessageDao();
    public abstract RepliesDao repliesDao();
    public abstract ReactionsDao reactionsDao();
    public abstract GroupMembersDao groupMembersDao();
    @NonNull
    public static ChatDatabase getInstance(final Context context){
        synchronized (ChatDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ChatDatabase.class, "teleconsole_chat_database").fallbackToDestructiveMigration().build();
            }
        }
        return INSTANCE;
    }


    public static ChatDatabase getInstance(){
        return getInstance(AppController.getInstance());
    }
    public static class RepliesHeadConverter{
        @TypeConverter
        public String fromRepliesHead(Replies.Head replies){
            if (replies == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            Type type = new TypeToken<Replies.Head>() {
            }.getType();
            return gson.toJson(replies, type);
        }
        @TypeConverter
        public Replies.Head toDataHead(String countryLangString) {
            if (countryLangString == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<Replies.Head>() {
            }.getType();
            return gson.fromJson(countryLangString, type);
        }
    }
    public static class ReactionConverter2 {
        @TypeConverter
        public String fromCountryLangList(DataMessage.Replies.Reaction[] countryLang) {
            if (countryLang == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<DataMessage.Replies.Reaction[]>() {
            }.getType();
            return gson.toJson(countryLang, type);
        }

        @TypeConverter
        public DataMessage.Replies.Reaction[] toCountryLangList(String countryLangString) {
            if (countryLangString == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<DataMessage.Replies.Reaction[]>() {
            }.getType();
            return gson.fromJson(countryLangString, type);
        }
    }

    public static class ReactionConverter {
        @TypeConverter
        public String fromCountryLangList(List<DataMessage.Reaction> countryLang) {
            if (countryLang == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<List<DataMessage.Reaction>>() {
            }.getType();
            return gson.toJson(countryLang, type);
        }

        @TypeConverter
        public List<DataMessage.Reaction> toCountryLangList(String countryLangString) {
            if (countryLangString == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<List<DataMessage.Reaction>>() {
            }.getType();
            return gson.fromJson(countryLangString, type);
        }
    }

    public static class ListConverter {
        @TypeConverter
        public String fromList(List<String> object) {
            if (object == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<Object>() {
            }.getType();
            return gson.toJson(object, type);
        }

        @TypeConverter
        public List<String> toObject(String object) {
            if (object == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<List<String>>() {
            }.getType();
            try {

                return gson.fromJson(object, type);
            }catch (Exception e){
                return new ArrayList<>();
            }
        }
    }

    public static class ObjectConverter {
        @TypeConverter
        public String fromObject(Object object) {
            if (object == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<Object>() {
            }.getType();
            return gson.toJson(object, type);
        }

        @TypeConverter
        public Object toObject(String object) {
            if (object == null) {
                return (null);
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
            Type type = new TypeToken<Object>() {
            }.getType();
            return gson.fromJson(object, type);
        }
    }

}

