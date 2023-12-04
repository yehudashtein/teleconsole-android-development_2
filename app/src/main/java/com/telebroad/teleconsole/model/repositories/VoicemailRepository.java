package com.telebroad.teleconsole.model.repositories;

import androidx.lifecycle.LiveData;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Line;
import com.telebroad.teleconsole.model.Settings;
import com.telebroad.teleconsole.model.Voicemail;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;
import com.telebroad.teleconsole.model.db.VoicemailDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.telebroad.teleconsole.helpers.URLHelper.GET_VOICEMAIL_URL;
import static com.telebroad.teleconsole.helpers.URLHelper.getDefaultErrorHandler;

public class VoicemailRepository {
    private final VoicemailDao voicemailDao;
    private static VoicemailRepository instance;

    public static VoicemailRepository getInstance(Application application){
        if (instance == null){
            instance = new VoicemailRepository(application);
        }
        return instance;
    }

    public static VoicemailRepository getInstance(){
        return getInstance(AppController.getInstance());
    }
    public VoicemailRepository(Context context) {
        voicemailDao = TeleConsoleDatabase.getInstance(context.getApplicationContext()).voicemailDao();
    }

    private LiveData<List<Voicemail>> allVoicemails;
    private LiveData<List<Voicemail>> filteredVoicemails;

    public LiveData<List<Voicemail>> getAllVoicemails() {
        if (allVoicemails == null){
            allVoicemails = voicemailDao.getVoicemailList();
        }
        return allVoicemails;
    }

    public LiveData<Voicemail> getVoicemailByTime(long timestamp){
         return voicemailDao.getVoicemailFromTime(timestamp);
    }

    public void setAsNotified(long timestamp){
        voicemailDao.setAsNotified(timestamp);
    }

    public LiveData<Voicemail> getVoicemail(String id){
        loadVoicemailsFromServer();
        return voicemailDao.getVoicemail(id);
    }


    public void checkIfNeedToLoadMore(long timestamp){
        // if there are no more than 5 faxes after this time, load more.
        if (voicemailDao.numberOfVoicemailsBefore(timestamp) < 5){
            loadMoreVoicemailsFromServer();
        }
    }

    public void setTranscription(String transcription, Voicemail voicemail){
        Utils.asyncTask(() -> {
            voicemailDao.setTranscription(transcription, voicemail.getId());
        });
    }
    boolean loading = false;
    private void loadMoreVoicemailsFromServer() {
        if (Settings.getInstance() == null || loading){
            return;
        }
        loading = true;
        long earliestTimestamp = voicemailDao.getEarliestTimestamp() -1;// numberOfFaxes = faxDao.numberOfFaxes();
        loadVoicemailRequest(getVoicemailRequestParams(earliestTimestamp, 100), list -> AsyncTask.execute(() -> {
            voicemailDao.save(list.toArray(new Voicemail[]{}));
            loading = false;
        }));
    }


    public void loadVoicemailsFromServer() {
        if (Settings.getInstance() == null){
            return;
        }
        loading = true;
        loadVoicemailRequest( getVoicemailRequestParams(-1, 100), list -> AsyncTask.execute(() -> {
            voicemailDao.refresh(list.toArray(new Voicemail[]{}));
            loading = false;
        }));

    }

    private Map<String, String> getVoicemailRequestParams(long lastTimestamp, int limit) {
        Map<String, String> params = new HashMap<>();
        List<Line> voicemailLines = Settings.getInstance().getVoicemails();
        StringBuilder mailboxParam = new StringBuilder();
        for (Line faxLine : voicemailLines){
            mailboxParam.append(faxLine.getName()).append(",");
        }
        params.put(URLHelper.KEY_MAILBOX, mailboxParam.toString());
        params.put(URLHelper.KEY_OFFSET, "0");
        params.put(URLHelper.KEY_LIMIT, String.valueOf(limit));
        params.put(URLHelper.KEY_DESCENDING, "1");
        params.put(URLHelper.KEY_START, "0");
        params.put(URLHelper.KEY_END, String.valueOf(lastTimestamp));
        return params;
    }


    public void loadNewVoicemail(long timestamp, String mailbox){
        Map<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_MAILBOX, mailbox);
        params.put(URLHelper.KEY_START, String.valueOf(timestamp));
        params.put(URLHelper.KEY_END, String.valueOf(timestamp));
        loadVoicemailRequest(params, (list) -> voicemailDao.save(list.toArray(new Voicemail[]{})));
    }

    public void loadVoicemailRequest(Map<String, String> params, Consumer<List<Voicemail>> save) {
        URLHelper.request(Request.Method.GET, GET_VOICEMAIL_URL, params, Boolean.parseBoolean(String.valueOf(Boolean.parseBoolean(String.valueOf(true)))), (results) -> {
            if (results instanceof JsonArray) {
                List<Voicemail> voicemailList = new Gson().fromJson(results, new TypeToken<List<Voicemail>>(){}.getType());
               // android.util.Log.d("Voicemail0010", "Voicemail List " + voicemailList);
                AsyncTask.execute(() -> save.accept(voicemailList));
            }
        }, getDefaultErrorHandler(error -> {}));
    }

    public void deleteVoicemail(Voicemail voicemail){
        if (voicemail == null){
            return;
        }
        AsyncTask.execute(() -> {
            voicemailDao.deleteVoicemail(voicemail.getId());
            voicemail.delete();
        });
    }
}
