package com.telebroad.teleconsole.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "phoneNumber",  indices = {
        @Index(name = "contactID", value = {"contactID"})
})
public class TCPhoneNumber extends PhoneNumber {

    private Integer contactID;
    @PrimaryKey
    @NonNull
    private String id ;

    @TypeConverters(SourceConverter.class)
    private Source source;

    public TCPhoneNumber(String phoneNumber, PhoneType phoneType, int contactID, Source source) {
        super(phoneNumber, phoneType);
        this.contactID = contactID;
        this.id = contactID + source.ordinal() + fixed();
        this.source = source;
    }

    public Integer getContactID() {
        return contactID;
    }

    public void setContactID(Integer contactID) {
        this.contactID = contactID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
    public enum Source {
        TELECONSOLE, PHONE
    }

    public static class SourceConverter {

        @TypeConverter
        public String fromStatus(Source source){
            if (source == null){
                return "";
            }
            return source.name();
        }
        @TypeConverter
        public Source toStatus(String name){
            try {
                return Source.valueOf(name);
            }catch (IllegalArgumentException iae){
                return null;
            }
        }
    }
}

