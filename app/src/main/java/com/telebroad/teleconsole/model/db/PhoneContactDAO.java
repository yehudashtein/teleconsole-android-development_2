package com.telebroad.teleconsole.model.db;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneContact;
import com.telebroad.teleconsole.model.PhoneNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER;
import static android.provider.ContactsContract.CommonDataKinds.Phone.PHOTO_URI;
import static android.provider.ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY;
import static android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY;
import static android.provider.ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER;
import static android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
import static android.provider.ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI;
import static android.provider.ContactsContract.CommonDataKinds.Phone.TYPE;


public class PhoneContactDAO {

    private static PhoneContactDAO instance;
    private final MutableLiveData<List<PhoneContact>> contacts = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Set<PhoneContact>>> contactsByPhoneNumber = new MutableLiveData<>();
    private final String[] contactProjection = {LOOKUP_KEY, DISPLAY_NAME_PRIMARY, HAS_PHONE_NUMBER, PHOTO_URI, PHOTO_THUMBNAIL_URI,  NUMBER, TYPE};
    private String[] phoneProjection = {ContactsContract.PhoneLookup.LOOKUP_KEY, ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY, ContactsContract.PhoneLookup.HAS_PHONE_NUMBER,
            ContactsContract.PhoneLookup.PHOTO_URI, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,  ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.TYPE};


    public static PhoneContactDAO getInstance(){
        if (instance == null){
            instance = new PhoneContactDAO();
            instance.initContactList();
        }
        return instance;
    }

    private List<PhoneContact> parseCursorContactList(Cursor cur, String lookupKey, String displayNameKey, String photoKey, String thumbnailKey, String phoneTypeKey, String phoneNumberKey){
        Map<String, PhoneContact> contactMap = new HashMap<>();
        Map<String, Set<PhoneContact>> phoneNumberMap = new HashMap<>();

        if (cur.isClosed()){
            return new ArrayList<>();
        }
        // Storing the indices to improve performance
        Map<String, Integer> columnIndices = new HashMap<>();
        findIndices(cur, columnIndices, lookupKey);
        findIndices(cur, columnIndices, displayNameKey);
        findIndices(cur, columnIndices, photoKey);
        findIndices(cur, columnIndices, thumbnailKey);
        findIndices(cur, columnIndices, phoneTypeKey);
        findIndices(cur, columnIndices, phoneNumberKey);

        long startTime = SystemClock.elapsedRealtime();
        while (cur.moveToNext()) {
//            android.util.Log.d("ContactTime", "time " + SystemClock.elapsedRealtime());
            String id = cur.getString(columnIndices.get(lookupKey));
            PhoneContact phoneContact = contactMap.get(id);
            if (phoneContact == null) {
                String name = cur.getString(columnIndices.get(displayNameKey));
                String photo = cur.getString(columnIndices.get(photoKey));
                String thumbnail = cur.getString(columnIndices.get(thumbnailKey));
                phoneContact = new PhoneContact(name, id);
                phoneContact.setPhotoURI(photo);
                phoneContact.setThumbnailURI(thumbnail);
                contactMap.put(id, phoneContact);
            }
            int rawType = cur.getInt(columnIndices.get(phoneTypeKey));
            PhoneNumber.PhoneType type = getPhoneType(rawType);
            PhoneNumber newNumber = new PhoneNumber(cur.getString(columnIndices.get(phoneNumberKey)), type);
            phoneContact.addPhoneNumber(newNumber);
            if (phoneNumberMap.get(newNumber.fixed()) == null){
                phoneNumberMap.put(newNumber.fixed(), new HashSet<>());
            }
            phoneNumberMap.get(newNumber.fixed()).add(phoneContact);
        }
        Utils.logToFile(AppController.getInstance(), "Contact Parsing Time. Average time for " + cur.getCount() + " contacts: " + ((SystemClock.elapsedRealtime() - startTime) / (cur.getCount() * 1.0)) + " total time: " + (SystemClock.elapsedRealtime() - startTime));
        Utils.updateLiveData(contactsByPhoneNumber, phoneNumberMap);
        return new ArrayList<>(contactMap.values());
    }

    private List<PhoneContact> parseCursorContactList(Cursor cur){
        return parseCursorContactList(cur, LOOKUP_KEY, DISPLAY_NAME_PRIMARY, PHOTO_URI, PHOTO_THUMBNAIL_URI, TYPE, NUMBER);
    }

    private void findIndices(Cursor cur, Map<String, Integer> columnIndices, String index) {
        columnIndices.put(index, cur.getColumnIndex(index));
    }

    // Initialize the contactlist without an observer
    private void initContactList(){
        if (ContextCompat.checkSelfPermission(AppController.getInstance(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        AsyncTask.execute(() -> {
            Cursor cur = AppController.getInstance().getContentResolver().query(CONTENT_URI, contactProjection, HAS_PHONE_NUMBER + " > 0", null, null);
            if (cur == null){
                try {
                    Thread.sleep(2000L);cur = AppController.getInstance().getContentResolver().query(CONTENT_URI, contactProjection, HAS_PHONE_NUMBER + " > 0", null, null);
                    if (cur == null){
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            List<PhoneContact> contactList = parseCursorContactList(cur);
            Utils.updateLiveData(contacts, contactList);
            cur.close();
        });
    }

    // Get the contactList
    public <T extends LifecycleOwner & ViewModelStoreOwner> LiveData<List<PhoneContact>> getContactList(T owner){
        if (ContextCompat.checkSelfPermission(AppController.getInstance(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return contacts;
        }

        LoaderManager loaderManager = LoaderManager.getInstance(owner);
        loaderManager.initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            Cursor cur;
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                return new CursorLoader(AppController.getInstance(), CONTENT_URI, contactProjection, HAS_PHONE_NUMBER + " > 0", null, null);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                cur = data;
                Utils.asyncTask(() -> {
                    List<PhoneContact> parsed = parseCursorContactList(cur);
                    if (parsed != null && !parsed.isEmpty()) {
                        Utils.updateLiveData(contacts, parsed);
                    }
                });
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {
                if (cur != null) cur.close();
            }
        });
        return contacts;
    }

    public LiveData<Set<PhoneContact>> matchContactToNumber(String number){
        MediatorLiveData<Set<PhoneContact>> matchedContacts = new MediatorLiveData();
        matchedContacts.addSource(contacts, contacts -> {
           // android.util.Log.d("ContactDAO03", "updating contacts");

        });
        matchedContacts.addSource(contactsByPhoneNumber, phoneNumberMap -> {
            Utils.updateLiveData(matchedContacts, phoneNumberMap.get(PhoneNumber.fix(number)));
            //android.util.Log.d("number", matchedContacts.toString());
        });

//        MutableLiveData<List<PhoneContact>> matchedContacts = new MutableLiveData<>();
//        AsyncTask.execute(() -> {
//            Cursor cursor = AppController.getInstance().getContentResolver().query(getPhoneLookupUri(number), phoneProjection, null, null, null);
//            if (cursor == null) {
//                return;
//            }
//            List<PhoneContact> contactList = parsePhoneLookupCursor(cursor);
//            android.util.Log.d("ContactDAO02", "matched contacts for " + number  + " are " + contactList);
//            Utils.updateLiveData(matchedContacts, contactList);
//            cursor.close();
//        });
        return matchedContacts;
    }

    private List<PhoneContact> parsePhoneLookupCursor(Cursor cursor) {
        return parseCursorContactList(cursor, ContactsContract.PhoneLookup.LOOKUP_KEY, ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY,
                ContactsContract.PhoneLookup.PHOTO_URI, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,  ContactsContract.PhoneLookup.TYPE, ContactsContract.PhoneLookup.NUMBER);
    }

    private Uri getPhoneLookupUri(String number) {
        return Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( number));
    }

    public <T extends LifecycleOwner & ViewModelStoreOwner> LiveData<PhoneContact> getContactByLookup(String lookup, T owner){
        MutableLiveData<PhoneContact> contact = new MutableLiveData<>();
        LoaderManager loaderManager = LoaderManager.getInstance(owner);
        String selection = LOOKUP_KEY + " LIKE ?";
        String[] selectionArgs = {lookup};
        loaderManager.initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            Cursor cur;
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                return new CursorLoader(AppController.getInstance(), CONTENT_URI, contactProjection, selection, selectionArgs, null);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                this.cur = data;
                if (!cur.isAfterLast()) {
                    Utils.updateLiveData(contact, parseContactCursor(cur));
                }
            }

            private PhoneContact parseContactCursor(Cursor cur){
                PhoneContact phoneContact = null;
                if (cur == null){
                    //android.util.Log.d("PhoneContactsDAO01", "cursor is null");
                    return null;
                }
                if(cur.getCount() == 0){
                    //android.util.Log.d("PhoneContactsDAO01", "cursor is empty");
                }
                while (cur.moveToNext()) {
                    if (phoneContact == null) {
                        String id = cur.getString(cur.getColumnIndexOrThrow(LOOKUP_KEY));
                        String name = cur.getString(cur.getColumnIndexOrThrow(DISPLAY_NAME_PRIMARY));
                        String photo = cur.getString(cur.getColumnIndexOrThrow(PHOTO_URI));
                        String thumbnail = cur.getString(cur.getColumnIndexOrThrow(PHOTO_THUMBNAIL_URI));
                        phoneContact = new PhoneContact(name, id);
                        phoneContact.setPhotoURI(photo);
                        phoneContact.setThumbnailURI(thumbnail);
                    }
                    int rawType = cur.getInt(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE));
                    PhoneNumber.PhoneType type = getPhoneType(rawType);
                    phoneContact.addPhoneNumber((new PhoneNumber(cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)), type)));
                }

                return phoneContact;
            }
            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {
                //android.util.Log.d("PhoneContactsDAO01", "loader reset");
                if (cur != null){
                    cur.close();
                }
            }
        });
        return contact;
    }

    public LiveData<List<PhoneNumber>> getContactPhoneNumbers(){
        MutableLiveData<List<PhoneNumber>> numbers = new MutableLiveData<>();
        return numbers;
    }

    public LiveData<List<PhoneContact>> searchContact(String query){
        MutableLiveData<List<PhoneContact>> matchedContacts = new MutableLiveData<>();
        return matchedContacts;
    }

    private static PhoneNumber.PhoneType getPhoneType(int rawType) {
        PhoneNumber.PhoneType type;
        switch (rawType) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                type = PhoneNumber.PhoneType.MOBILE;
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                type = PhoneNumber.PhoneType.MAIN;
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                type = PhoneNumber.PhoneType.HOME;
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                type = PhoneNumber.PhoneType.FAX;
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                type = PhoneNumber.PhoneType.WORK;
                break;
            default:
                type = PhoneNumber.PhoneType.OTHER;
                break;
        }
        return type;
    }
}
