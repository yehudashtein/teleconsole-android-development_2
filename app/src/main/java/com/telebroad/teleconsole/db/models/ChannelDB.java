package com.telebroad.teleconsole.db.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.telebroad.teleconsole.chat.models.Channel;
import com.telebroad.teleconsole.chat.server.MetaMessage;

import java.io.Serializable;

@Entity(tableName = "channels")
public class ChannelDB extends Channel implements Serializable {

    @PrimaryKey
    @NonNull
    private String primaryKey = getSubbedTo() + getTopic() + "";


    public ChannelDB(MetaMessage.Sub sub, String subbedTo) {super(sub, subbedTo);
    }
    public ChannelDB(MetaMessage metaMessage) {
        super(metaMessage);
    }


    public ChannelDB(){
        super();
    }
    //public Channel getAsChannel(){
       // return new Channel(this);
    //}

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean matches(String filter) {
        if (getName().contains(filter)){
            return true;
        }
        return false;
    }
}
