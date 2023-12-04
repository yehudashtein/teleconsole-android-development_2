package com.telebroad.teleconsole.model.repositories;

import android.app.Application;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import android.os.AsyncTask;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.db.CallHistoryDao;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.DELETE;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_ID;

public class CallHistoryRepository {

    private static int offset = 0;
    private final CallHistoryDao callHistoryDao;
    private static CallHistoryRepository INSTANCE;

    public static CallHistoryRepository getInstance(Application context) {
        if (INSTANCE == null) {
            INSTANCE = new CallHistoryRepository(context);
        }
        return INSTANCE;
    }

    public static CallHistoryRepository getInstance() {
        return getInstance(AppController.getInstance());
    }

    private CallHistoryRepository(Application context) {
        callHistoryDao = TeleConsoleDatabase.getInstance(context).callHistoryDao();
    }

    private LiveData<List<CallHistory>> callHistory;
    private LiveData<List<CallHistory>> missedCalls;
    private LiveData<List<CallHistory>> incomingCalls;
    private LiveData<List<CallHistory>> outgoingCalls;
    private CallListType currentType;
    private final MediatorLiveData<List<CallHistory>> activeList = new MediatorLiveData<>();

    public LiveData<List<CallHistory>> getCallHistoryList() {
        if (callHistory == null) {
            callHistory = callHistoryDao.getCallHistoryList();

        }
        return callHistory;
    }

    @WorkerThread
    public List<CallHistory> getCallLogsForContact(Contact contact){
        return callHistoryDao.getCallForContact(PhoneNumber.convertPhoneNumberListToFixed(contact.getAllLines()));
    }
    public LiveData<List<CallHistory>> getMissedCallHistoryList() {
        if (missedCalls == null) {
            missedCalls = callHistoryDao.getMissedCallHistoryList();
        }
        return missedCalls;
    }

    public LiveData<List<CallHistory>> getIncomingCallHistoryList() {
        if (incomingCalls == null) {
            incomingCalls = callHistoryDao.getReceivedCallHistoryList();
        }
        return incomingCalls;
    }

    public LiveData<List<CallHistory>> getOutgoingCallHistoryList() {
        if (outgoingCalls == null) {
            outgoingCalls = callHistoryDao.getOutgoingCallHistoryList();
        }
        return outgoingCalls;
    }

    public MediatorLiveData<List<CallHistory>> getActiveCallList() {
        if (activeList.getValue() == null) {
            setActiveCallList(CallListType.ALL);
        }
        return activeList;
    }

    public void setActiveCallList(CallListType callListType) {
        if (callListType == currentType) {
            return;
        }
        currentType = callListType;
        switch (callListType) {
            case ALL:
                activeList.removeSource(getMissedCallHistoryList());
                activeList.removeSource(getIncomingCallHistoryList());
                activeList.removeSource(getOutgoingCallHistoryList());
                activeList.addSource(getCallHistoryList(), activeList::setValue);
                break;
            case INCOMING:
                activeList.removeSource(getMissedCallHistoryList());
                activeList.removeSource(getCallHistoryList());
                activeList.removeSource(getOutgoingCallHistoryList());
                activeList.addSource(getIncomingCallHistoryList(), activeList::setValue);
                break;
            case OUTGOING:
                activeList.removeSource(getMissedCallHistoryList());
                activeList.removeSource(getIncomingCallHistoryList());
                activeList.removeSource(getCallHistoryList());
                activeList.addSource(getOutgoingCallHistoryList(), activeList::setValue);
                break;
            case MISSED:
                activeList.removeSource(getCallHistoryList());
                activeList.removeSource(getIncomingCallHistoryList());
                activeList.removeSource(getOutgoingCallHistoryList());
                activeList.addSource(getMissedCallHistoryList(), activeList::setValue);
                break;
        }
        return;
    }

    public void addCallHistoryFromServer(int offset1) {

        AsyncTask.execute(() -> {
            int offset = callHistoryDao.getRowCount();
           // android.util.Log.d("Bug0008", "Adding " + offset + " shouldn't load? " + (offset == CallHistoryRepository.offset) + " call history Repo offset " + CallHistoryRepository.offset);
            if (offset == CallHistoryRepository.offset) {
                return;
            }
            CallHistoryRepository.offset = offset;
            //int callHistorySize = getCallHistoryList().getValue() == null ? 0 : getCallHistoryList().getValue().size();
            //android.util.Log.d("Bug0008", "Loading... " + offset);
            loadCallHistoryFromServer(String.valueOf(offset), callHistoryDao::save);
        });
    }

    public void loadCallHistoryFromServer() {
        loadCallHistoryFromServer("0", callHistoryDao::refresh);
    }

    private void loadCallHistoryFromServer(String offset, CallHistoryUpdater updater) {
        Map<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_PHONE, SettingsHelper.getString(SettingsHelper.SIP_USERNAME, ""));
        params.put(URLHelper.KEY_OFFSET, offset);
        URLHelper.request(Request.Method.GET, URLHelper.GET_CDRS_URL, params, true, (results) -> {
            if (results instanceof JsonArray) {
                List<CallHistory> callHistoryList = new Gson().fromJson(results, new TypeToken<List<CallHistory>>() {
                }.getType());
                AsyncTask.execute(() -> {
                    updater.update(callHistoryList.toArray(new CallHistory[]{}));
                });
            }
        }, URLHelper.getDefaultErrorHandler(error -> {
            if (error != null) {
                CallHistoryRepository.offset -= 100;
            }
        }));
    }

    public void saveCallHistory(CallHistory... history) {
        callHistoryDao.save(history);
    }

    public void delete(String... ids) {
        AsyncTask.execute(() -> {

            for (String id : ids) {
                callHistoryDao.delete(id);
                Map<String, String> params = new HashMap();
                params.put(KEY_ID, id);
                URLHelper.request(DELETE, URLHelper.GET_CDRS_URL, params, result -> {}, error -> {});
            }
        });
    }

    interface CallHistoryUpdater {
        void update(CallHistory[] callHistory);
    }

    public enum CallListType {
        ALL, INCOMING, OUTGOING, MISSED;
    }

}
