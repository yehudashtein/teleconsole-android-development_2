package com.telebroad.teleconsole.model;

import android.Manifest;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;

import com.google.common.base.Strings;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.Contacts.LOOKUP_KEY;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.telebroad.teleconsole.helpers.Utils.updateLiveData;
import static com.telebroad.teleconsole.model.PhoneNumber.PhoneType.FAX;
import static android.provider.ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;

public class PhoneContact implements Contact {

    private String id;
    private String name;
    private String photoURI;
    private String thumbnailURI;
    private List<PhoneNumber> telephoneLines = new ArrayList<>();
    private List<String> emailAddresses = new ArrayList<>();
    private static MutableLiveData<List<PhoneContact>> phoneContacts = new MutableLiveData<>();
    public static MutableLiveData<List<PhoneContact>> matchedContacts = new MutableLiveData<>();
    private static Map<String, PhoneContact> contactMap = new HashMap<>();

    public static LiveData<List<PhoneContact>>getPhoneContacts() {
        return phoneContacts;
    }

    public PhoneContact(String name, String id){
        this.name = name;
        this.id = id;
    }

    @Override
    public String getWholeName() {
        return name != null ? name : "";
    }

    @Override
    public String getType() {
        return "mobile";
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof  PhoneContact)){
            return false;
        }
        PhoneContact other = (PhoneContact)obj;
        return id.equals(other.id);
    }

    @Override
    public List<PhoneNumber> getTelephoneLines() {
        return telephoneLines;
    }

    public String getPhotoURI() {
        return photoURI;
    }

    public void setPhotoURI(String photoURI) {
        this.photoURI = photoURI;
    }

    public String getThumbnailURI() {
        return thumbnailURI;
    }

    public void setThumbnailURI(String thumbnailURI) {
        this.thumbnailURI = thumbnailURI;
    }

    @Override
    public List<String> getEmailAddresses() {
        return emailAddresses;
    }


    public void addPhoneNumber(PhoneNumber newNumber){
        //android.util.Log.d("PhoneContacts101", "adding " + newNumber.fixed() + " size " +  telephoneLines.size());
        if (telephoneLines.contains(newNumber)){
            //android.util.Log.d("PhoneContacts101", "not adding");
            return;
        }
        telephoneLines.add(newNumber);
    }


    public static void loadPhoneContacts (){
        if (ContextCompat.checkSelfPermission(AppController.getInstance(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
           return;
        }

        AsyncTask.execute(() -> {
            ContentResolver cr = AppController.getInstance().getContentResolver();
            String[] columns = {LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.PHOTO_URI, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
            String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0";
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, columns , selection, null, null);
//            android.util.Log.d("PhoneContacts10", "results " + cur.getCount());
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(LOOKUP_KEY));
                String name = cur.getString(cur.getColumnIndex(DISPLAY_NAME_PRIMARY));
                String photo = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                String thumbnail = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                PhoneContact phoneContact = new PhoneContact(name, id);
                phoneContact.photoURI = photo;
                phoneContact.thumbnailURI = thumbnail;
                contactMap.put(id, phoneContact);
            }

            cur.close();
            String[] phoneSelection = {Phone.NUMBER, Phone.TYPE, Phone.LOOKUP_KEY};
            Cursor phoneNumbers = cr.query(Phone.CONTENT_URI, phoneSelection, null, null, null);

            while (phoneNumbers != null && phoneNumbers.moveToNext()){
                String lookup = phoneNumbers.getString(phoneNumbers.getColumnIndex(Phone.LOOKUP_KEY));
                int rawType = phoneNumbers.getInt(phoneNumbers.getColumnIndex(Phone.TYPE));
               // android.util.Log.d("PhoneContacts10", "lookup " + lookup + phoneNumbers.getString(phoneNumbers.getColumnIndex(Phone.NUMBER) ) + " Raw type " + rawType);

                PhoneNumber.PhoneType type = getPhoneType(rawType);

//                if (type == FAX) {
//                    contactMap.get(lookup).faxLines.add(new PhoneNumber(phoneNumbers.getString(phoneNumbers.getColumnIndex(Phone.NUMBER)), FAX));
//                } else {
                if (contactMap.get(lookup) != null) {
                    contactMap.get(lookup).addPhoneNumber((new PhoneNumber(phoneNumbers.getString(phoneNumbers.getColumnIndex(Phone.NUMBER)), type)));
                }
//                }
            }
            phoneNumbers.close();
            Utils.updateLiveData(phoneContacts, new ArrayList<PhoneContact>(contactMap.values()));
        });
//        getLoadAsyncTask(Phone.CONTENT_URI, phoneContacts);
    }
    public static LiveData<List<PhoneContact>> matchContactsToPhoneNumber(String phoneNumber){
        MutableLiveData<List<PhoneContact>> matched = new MutableLiveData<>();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( phoneNumber));
        AsyncTask.execute(() -> {
            if (ContextCompat.checkSelfPermission(AppController.getInstance(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                Collection<PhoneContact> contactList = getPhoneContacts(uri);
                if (contactList == null)
                    return;
                updateLiveData(matched, new ArrayList<>(contactList));
            }
        });
        return matched;
    }

    @WorkerThread
    public static List<PhoneContact> getMatchedContactsList(String phoneNumber){
        if (nullToEmpty(phoneNumber).isEmpty()){
            return new ArrayList<>();
        }
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( phoneNumber));
        if (ContextCompat.checkSelfPermission(AppController.getInstance(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Collection<PhoneContact> contactList = getPhoneContacts(uri);
            if (contactList == null)
                return new ArrayList<>();
            return new ArrayList<>(contactList);
        }
        return new ArrayList<>();
    }

    private static Collection<PhoneContact> getPhoneContacts(Uri uri) {
        ContentResolver contentResolver = AppController.getInstance().getContentResolver();
        Cursor cur = contentResolver.query(uri, new String[] { LOOKUP_KEY, ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.TYPE}, null, null, null);

        Map<String, PhoneContact> contacts = new HashMap<>();
        if (cur == null){
            return null;
        }
       // android.util.Log.d("Phone Contacts","cur length " + cur.getCount());
        while (cur.moveToNext()) {
            String id = cur.getString(cur.getColumnIndex(LOOKUP_KEY));
            String name = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY));
            if (contacts.get(id) == null) {
                contacts.put(id, new PhoneContact(Strings.nullToEmpty(name), id));
            }
            int rawType = cur.getInt(cur.getColumnIndex(ContactsContract.PhoneLookup.TYPE));

            PhoneNumber.PhoneType type = getPhoneType(rawType);

//            if (type == FAX) {
//                contacts.get(id).faxLines.add(PhoneNumber.getPhoneNumber(cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)), FAX));
//            } else {
                contacts.get(id).telephoneLines.add(PhoneNumber.getPhoneNumber(cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)), type));
//            }
            //android.util.Log.d("PhoneContact", "ID0 = " + id);
        }
        cur.close();
        return contacts.values();
    }

    public static PhoneContact getContactByID(String id){
        PhoneContact phoneContact = contactMap.get(id);
//        if (phoneContact == null){
//
//        }
        return phoneContact;
    }

    public static void updateContact(String lookup){

    }

    public static void deleteContact(String lookup){
        contactMap.remove(lookup);
    }
    // This method searches for all contacts, regardless of type
    public static void searchContacts(String query){
        if(isNullOrEmpty(query)){
            getLoadAsyncTask(Phone.CONTENT_URI, matchedContacts);
            return;
        }
        Uri contentUri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(query));
        getLoadAsyncTask(contentUri, matchedContacts);
    }

    private static void getLoadAsyncTask(Uri uri, MutableLiveData<List<PhoneContact>> toUpdate){
        getLoadAsyncTask(uri, toUpdate, null);
    }
    private static void getLoadAsyncTask(Uri uri, MutableLiveData<List<PhoneContact>> toUpdate, String selection){
        new AsyncTask<Uri, Void, Void>() {
            @Override
            protected Void doInBackground(Uri... uris) {
                if (ContextCompat.checkSelfPermission(AppController.getInstance(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                    return null;
                }
                ContentResolver contentResolver = AppController.getInstance().getContentResolver();
                Cursor cur = contentResolver.query(uris[0], new String[] {LOOKUP_KEY, Phone.CONTACT_ID ,Phone.DISPLAY_NAME_PRIMARY, Phone.NUMBER, Phone.TYPE}, selection, null, null);
                List<PhoneContact> allContacts = parseContactCursor(cur);
                updateLiveData(toUpdate, allContacts);
                List<TCPhoneNumber> numbers = new ArrayList<>();
                for (PhoneContact phoneContact: allContacts){
                    for (PhoneNumber number : phoneContact.getTelephoneLines()){
                      //  android.util.Log.d("PhoneContact", "ID = " + phoneContact.id);
                        //numbers.add(new TCPhoneNumber(number.fixed(), number.getPhoneType(), phoneContact.id, TCPhoneNumber.Source.PHONE));
                    }
                }
                return null;
            }
        }.execute(uri);
    }

    private static List<PhoneContact> parseContactCursor(Cursor cur) {

        Map<String, PhoneContact> contacts = new HashMap<>();
        if (cur == null) {
            return new ArrayList<>();
        }
        //android.util.Log.d("Phone Contacts", "cur length " + cur.getCount());
        while (cur.moveToNext()) {
            String id = cur.getString(cur.getColumnIndex(LOOKUP_KEY));
            String name = cur.getString(cur.getColumnIndex(Phone.DISPLAY_NAME_PRIMARY));
            if (contacts.get(id) == null) {
                contacts.put(id, new PhoneContact(Strings.nullToEmpty(name), id));
            }
            int rawType = cur.getInt(cur.getColumnIndex(Phone.TYPE));

            PhoneNumber.PhoneType type = getPhoneType(rawType);
            contacts.get(id).telephoneLines.add(PhoneNumber.getPhoneNumber(cur.getString(cur.getColumnIndex(Phone.NUMBER)), type));

        }
        cur.close();
        ArrayList<PhoneContact> results = new ArrayList<>(contacts.values());
        return new ArrayList<>(results);
    }

    @NonNull
    private static PhoneNumber.PhoneType getPhoneType(int rawType) {
        PhoneNumber.PhoneType type;
        switch (rawType) {
            case Phone.TYPE_MOBILE:
            case Phone.TYPE_WORK_MOBILE:
                type = PhoneNumber.PhoneType.MOBILE;
                break;
            case Phone.TYPE_MAIN:
                type = PhoneNumber.PhoneType.MAIN;
                break;
            case Phone.TYPE_HOME:
                type = PhoneNumber.PhoneType.HOME;
                break;
            case Phone.TYPE_FAX_HOME:
            case Phone.TYPE_OTHER_FAX:
            case Phone.TYPE_FAX_WORK:
                type = FAX;
                break;
            case Phone.TYPE_WORK:
                type = PhoneNumber.PhoneType.WORK;
                break;
            default:
                type = PhoneNumber.PhoneType.OTHER;
                break;
        }
        return type;
    }

    @Override
    public String toString() {
        return "PhoneContact{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", photoURI='" + photoURI + '\'' +
                ", thumbnailURI='" + thumbnailURI + '\'' +
                ", telephoneLines=" + telephoneLines +
                ", emailAddresses=" + emailAddresses +
                '}';
    }
}
