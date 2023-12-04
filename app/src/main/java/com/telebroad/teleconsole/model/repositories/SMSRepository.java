package com.telebroad.teleconsole.model.repositories;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.SmsConversationActivity;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.DlrUpdate;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.SMS;
import com.telebroad.teleconsole.model.db.SMSDao;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;
import com.telebroad.teleconsole.viewmodels.SMSViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import static com.android.volley.Request.Method.DELETE;
import static com.android.volley.Request.Method.GET;
import static com.telebroad.teleconsole.helpers.URLHelper.DELETE_CONVERSATION;
import static com.telebroad.teleconsole.helpers.URLHelper.DELETE_SMS;
import static com.telebroad.teleconsole.helpers.URLHelper.GET_CONVERSATION_URL;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_CONVERSATION;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_ID;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_SMSLINE;

public class SMSRepository {
    private final SMSDao smsDao;

    private static SMSRepository instance;

    public static SMSRepository getInstance() {
        return getInstance(AppController.getInstance());
    }

    public static SMSRepository getInstance(Application context) {
        if (instance == null) {
            instance = new SMSRepository(context);
        }
        return instance;
    }

    private SMSRepository(Application context) {
        smsDao = TeleConsoleDatabase.getInstance(context).smsDao();
    }

    private LiveData<List<SMS>> allSMS;

    private LiveData<List<SMS>> filteredSMS;

    public LiveData<List<SMS>> getAllSMS() {
        if (allSMS == null) {
            allSMS = smsDao.getSMSConversationList();
        }
        return allSMS;
    }

    public void setSMSRead(SMS smsRead) {
        AsyncTask.execute(() -> {
            smsDao.setRead(smsRead.getId());
        });
    }

    public void addSMSToConversation(SMS toAdd) {
       // android.util.Log.d("SMS01", toAdd.getMsgdata());
        AsyncTask.execute(() -> {
            SMSViewModel smsViewModel = new SMSViewModel();
            toAdd.setIdx(1);
            smsViewModel.setItem(toAdd);
            smsDao.updateMostRecentSMSFromConversation(smsViewModel.getMyNumber().fixed(), smsViewModel.getOtherNumber().fixed(), toAdd.getMsgdata(), toAdd.getTimestamp());
            smsDao.save(toAdd);
        });
    }

    public void updateDLR(DlrUpdate dlrUpdate){
        smsDao.updateDLR(dlrUpdate.dlr_status, dlrUpdate.dlr_error, dlrUpdate.id);
    }

    public List<SMS> getNotificationSMSConversation(String myNumber, String otherNumber){
        return smsDao.getNotificationConversation(myNumber, otherNumber);
    }

    public LiveData<List<SMS>> getConversation(String myNumber, String otherNumber) {
        AsyncTask.execute(() -> smsDao.setConversationAsNotified(myNumber, otherNumber));
        return smsDao.getConversation(myNumber, otherNumber);
    }

    public LiveData<Boolean> getConversationBlocked(String myNumber, String otherNumber){
        return smsDao.isConversationBlocked(myNumber,otherNumber);
    }
    public void setConversationBlocked(String myNumber, String otherNumber, boolean value){
        smsDao.setConversationBlocked(myNumber, otherNumber, value);
    }

    public void loadConversationFromServer(@NonNull String myNumber, @NonNull String otherNumber, SMS newSMS) {
        Map<String, String> params = getConvoParams(PhoneNumber.fix(myNumber), PhoneNumber.fix(otherNumber));
       // Log.d("delConvo", "mynumber = " + myNumber + " othernumber "+ otherNumber);
        URLHelper.request(GET, GET_CONVERSATION_URL, params, true, results -> {
            if (results instanceof JsonArray) {
                List<SMS> smsList = new Gson().fromJson(results, new TypeToken<List<SMS>>() {}.getType());
                //smsList.sort((one, two) -> (int) (one.getTimeStamp() - two.getTimeStamp()));
                AsyncTask.execute(() -> {
//                    smsDao.refreshConversation(new SMS[]{}, PhoneNumber.fix(myNumber), PhoneNumber.fix(otherNumber));
                    smsDao.deleteConversation(PhoneNumber.fix(myNumber), PhoneNumber.fix(otherNumber));
                    smsDao.save(smsList.toArray(new SMS[]{}));
                    if(newSMS != null){
                        newSMS.send(System.out::println, System.out::println);
                    }
                });
            }
        }, error -> android.util.Log.d("", ""));
    }
    public void loadConversationFromServer1(@NonNull String myNumber, @NonNull String otherNumber, SMS newSMS,int from) {
        Map<String, String> params = getConvoParams1(PhoneNumber.fix(myNumber), PhoneNumber.fix(otherNumber),from);
        // Log.d("delConvo", "mynumber = " + myNumber + " othernumber "+ otherNumber);
        URLHelper.request(GET, GET_CONVERSATION_URL, params, true, results -> {
            if (results instanceof JsonArray) {
                List<SMS> smsList = new Gson().fromJson(results, new TypeToken<List<SMS>>() {}.getType());
                //smsList.sort((one, two) -> (int) (one.getTimeStamp() - two.getTimeStamp()));
                AsyncTask.execute(() -> {
//                    smsDao.refreshConversation(new SMS[]{}, PhoneNumber.fix(myNumber), PhoneNumber.fix(otherNumber));
                    //smsDao.deleteConversation(PhoneNumber.fix(myNumber), PhoneNumber.fix(otherNumber));
                    smsDao.save(smsList.toArray(new SMS[]{}));
                    if(newSMS != null){
                        newSMS.send(System.out::println, System.out::println);
                    }
                });
            }
        }, error -> android.util.Log.d("", ""));
    }
    public void deleteSMS(String myNumber , SMS... toDelete ){
        Map<String, String> params = new HashMap<>();
        params.put(KEY_SMSLINE, myNumber);
        AsyncTask.execute(() -> {
            for (SMS sms : toDelete){
                params.put(KEY_ID, sms.getId());
                URLHelper.request(DELETE, DELETE_SMS, params, results -> {  //android.util.Log.d("Delete01", "success " + results);
                    }, error -> { //android.util.Log.d("Delete01", "error " + error.getFullErrorMessage());
                });
                smsDao.deleteSMS(sms.getId());
            }
        });
    }

    public void deleteConversation(String myNumber, String otherNumber){
        AsyncTask.execute(() -> deleteConversationRequest(myNumber, otherNumber));
    }

    @WorkerThread
    public void deleteConversationRequest(String myNumber, String otherNumber){
        Map<String, String> params = new HashMap<>();
        params.put(KEY_SMSLINE, myNumber);
        params.put(KEY_CONVERSATION, otherNumber);
        URLHelper.request(DELETE, DELETE_CONVERSATION, params, result -> Utils.logToFile("success " + result.toString()), error -> Utils.logToFile("error deleting conversation " + error.getFullErrorMessage()) );
        smsDao.deleteConversation(myNumber, otherNumber);
    }
    public void deleteConversation(String myNumber, List<SMS> toDelete){
       // android.util.Log.d("Delete01", "smsToDelete " + toDelete.size());
        if (toDelete.isEmpty()){
            return;
        }
        deleteSMS(myNumber,  toDelete.toArray(new SMS[]{}));
    }
    private Map<String, String> getConvoParams(@NonNull String myNumber, @NonNull String otherNumber) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_CONVERSATION, PhoneNumber.getPhoneNumber(otherNumber).fixed());
        params.put(KEY_SMSLINE, PhoneNumber.getPhoneNumber(myNumber).fixed());
//        params.put("offset","0");
//        params.put("limit","2");
        return params;
    }
    private Map<String, String> getConvoParams1(@NonNull String myNumber, @NonNull String otherNumber,int i) {
        Map<String, String> params = new HashMap<>();
        params.put(KEY_CONVERSATION, PhoneNumber.getPhoneNumber(otherNumber).fixed());
        params.put(KEY_SMSLINE, PhoneNumber.getPhoneNumber(myNumber).fixed());
        params.put("offset",String.valueOf(i));
        params.put("limit","20");
        return params;
    }

    public void deleteSMSFromLine(String line){
        AsyncTask.execute(() -> smsDao.deleteAllFromLine(line));
    }

    public void loadSMSFromServer() {
        URLHelper.request(GET, URLHelper.GET_SMS_CONVERSATIONS_URL, new HashMap<>(), true, (results) -> {
            if (results instanceof JsonArray) {
                List<SMS> smsList = new Gson().fromJson(results, new TypeToken<List<SMS>>() {}.getType());
                AsyncTask.execute(() -> smsDao.refreshList(smsList.toArray(new SMS[]{})));
            }
        }, error -> {if (error != null){}
           // android.util.Log.d("SMS0009", "Sms error " + error.getFullErrorMessage());
        });
    }


    public void setSMSasSent(long timestamp){
        AsyncTask.execute(() -> smsDao.setAsSent(timestamp));
    }
    public void loadConversationFromServer(String sender, String receiver) {
        loadConversationFromServer(sender, receiver, null);
    }

    public void setSMSUnread(long timestamp) {
        smsDao.setSMSUnread(timestamp);
    }
}

