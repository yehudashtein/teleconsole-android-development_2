package com.telebroad.teleconsole.model.repositories;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import android.os.AsyncTask;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.model.DlrUpdate;
import com.telebroad.teleconsole.model.Fax;
import com.telebroad.teleconsole.model.Line;
import com.telebroad.teleconsole.model.Settings;
import com.telebroad.teleconsole.model.db.FaxDao;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.DELETE;
import static com.telebroad.teleconsole.helpers.URLHelper.DELETE_FAX;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_DIR;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_FILE;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_MAILBOX;

public class FaxRepository {
    private final FaxDao faxDao;

    private static FaxRepository instance;
    public static FaxRepository getInstance(Application context){
        if (instance == null){
            instance = new FaxRepository(AppController.getInstance());
        }
        return instance;
    }

    public static FaxRepository getInstance(){
        return getInstance(AppController.getInstance());
    }
    private FaxRepository(Application context) {
        faxDao = TeleConsoleDatabase.getInstance(context).faxDao();
    }

    private LiveData<List<Fax>> allFaxes;
    private MediatorLiveData<List<Fax>> filteredFaxes = new MediatorLiveData<>();

    public LiveData<List<Fax>> getAllFaxes() {
        if (allFaxes == null){
            allFaxes = faxDao.getFaxList();
        }
        return allFaxes;
    }

    public LiveData<Fax> getFax(String id) {
        return faxDao.getFax(id);
    }

    public void loadNewFax(long timestamp, String mailbox){
        Map<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_MAILBOX, mailbox);
        params.put(URLHelper.KEY_START, String.valueOf(timestamp));
        params.put(URLHelper.KEY_END, String.valueOf(timestamp));

        loadFaxRequest(params, (list) -> AsyncTask.execute(() -> faxDao.save(list.toArray(new Fax[]{}))));
    }

    public void loadFaxesFromServer() {
        if (Settings.getInstance() == null || loading){
            return;
        }
//        Map<String, String> params = getFaxRequestParams(0, 100);

        loading = true;
        loadFaxRequest(getFaxRequestParams(-1, 100), list -> AsyncTask.execute(() -> {
            faxDao.refresh(list.toArray(new Fax[]{}));
            loading = false;
        }));
    }

    // Check if we need to load more faxes;
    public void checkIfNeedToLoadMore(long timestamp){
        // if there are no more than 5 faxes after this time, load more.
        if (faxDao.numberOfFaxesBefore(timestamp) < 5){
            loadMoreFaxesFromServer();
        }
    }

    public void updateDLR(DlrUpdate dlrUpdate){
        faxDao.updateDLR(dlrUpdate.dlr_status, dlrUpdate.dlr_error, dlrUpdate.id);
    }
    boolean loading = false;
    public void loadMoreFaxesFromServer(){

        if (Settings.getInstance() == null || loading){
            return;
        }
        loading = true;
        long earliestTimestamp = faxDao.getEarliestTimestamp() -1;// numberOfFaxes = faxDao.numberOfFaxes();
        loadFaxRequest(getFaxRequestParams(earliestTimestamp, 100), list -> AsyncTask.execute(() -> {

            faxDao.save(list.toArray(new Fax[]{}));

            loading = false;
        }));
    }
    private Map<String, String> getFaxRequestParams(long lastTimestamp, int limit) {
        Map<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_DESCENDING, "1");
        params.put(URLHelper.KEY_START, "0");

        List<Line> faxLines = Settings.getInstance().getFaxLines();
        StringBuilder mailboxParam = new StringBuilder();
        for (Line faxLine : faxLines){
            mailboxParam.append(faxLine.getName()).append(",");
        }
        params.put(URLHelper.KEY_MAILBOX, mailboxParam.toString());
        params.put(URLHelper.KEY_OFFSET, "0");
        params.put(URLHelper.KEY_LIMIT, String.valueOf(limit));
        params.put(URLHelper.KEY_END, String.valueOf(lastTimestamp));
        return params;
    }

    public void loadFaxRequest(Map<String, String> params, Consumer<List<Fax>> resultHandler) {
        URLHelper.request(Request.Method.GET, URLHelper.GET_FAX_HISTORY, params, true, (results) -> {
            if (results instanceof JsonArray) {
                List<Fax> faxList = new Gson().fromJson(results, new TypeToken<List<Fax>>(){}.getType());
                resultHandler.accept(faxList);

            }
        }, error -> {
        });
    }

    public LiveData<Fax> getFaxFromTime(long timestamp){
        return faxDao.getFaxFromTime(timestamp);
    }

    public void deleteFax(String mailbox, String fileName, String dir, String id) {
        AsyncTask.execute(() -> {
            faxDao.deleteFax(id);
            Map<String, String> params = new HashMap<>();
            params.put(KEY_MAILBOX, mailbox);
            params.put(KEY_FILE, fileName);
            params.put(KEY_DIR, dir);
            URLHelper.request(DELETE, DELETE_FAX, params, results -> {}, error -> {} );
        });
    }

    public LiveData<List<Fax>> getFilteredList(){
        return filteredFaxes;
    }

    public void filter(String query){

       // android.util.Log.d("LiveData03", "Filtered = " + filteredFaxes);
    }

    public void saveFaxes(Fax fax) {
        faxDao.save(fax);
    }


//    private static class RefreshFaxesAsyncTask extends AsyncTask<Fax, Void, Void> {
//        private FaxDao mAsyncTaskDao;
//
//        RefreshFaxesAsyncTask(FaxDao dao) {
//            mAsyncTaskDao = dao;
//        }
//
//        @Override
//        protected Void doInBackground(Fax... faxes) {
//
//            mAsyncTaskDao.refresh(faxes);
//
//            return null;
//        }
//    }
}
