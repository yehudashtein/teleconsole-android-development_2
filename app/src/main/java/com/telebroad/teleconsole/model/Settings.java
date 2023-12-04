
package com.telebroad.teleconsole.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.os.AsyncTask;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.db.SMSDao;
import com.telebroad.teleconsole.model.repositories.CallHistoryRepository;
import com.telebroad.teleconsole.model.repositories.FaxRepository;
import com.telebroad.teleconsole.model.repositories.SMSRepository;
import com.telebroad.teleconsole.model.repositories.VoicemailRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.MALFORMED_ERROR;
import static com.telebroad.teleconsole.helpers.URLHelper.GET_USER_SETTING;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_TELECONSOLE;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_TYPE;
import static com.telebroad.teleconsole.helpers.URLHelper.request;
import static com.telebroad.teleconsole.helpers.Utils.updateLiveData;

public class Settings {

    // Custom implementation of mutable live data to save any updates to disk, and to try to get from disk if null
    private static MutableLiveData<Settings> instance = new MutableLiveData<Settings>();

    @SerializedName("defaultSmsLine")
    private String defaultSMSLine = "";
    @SerializedName("activePhoneLine")
    private Line.PhoneLine phoneLine;
    @SerializedName("activeSmsLines")
    private List<Line> smsLines = new ArrayList<>();
    @SerializedName("activeVoicemails")
    private List<Line> voicemails = new ArrayList<>();
    @SerializedName("activeFaxes")
    private List<Line> faxLines = new ArrayList<>();
    @SerializedName("fwPhoneNr")
    private Map<String, String> fwPhoneNumbers = new HashMap<>();
//    private int userStatuses;

    @Nullable
    public static Settings getInstance() {
        // Something is causing the settings to be null
        if (instance.getValue() == null) {
            // try again
            fetchSettings((error) -> {
                if (error != null) {
                   // android.util.Log.d("Error ", error.getFullErrorMessage());
                }
            });
            return createFromSaved();
        }
        return instance.getValue();
    }

    public static LiveData<Settings> getLiveInstance() {
        return instance;
    }

    public String getDefaultSMSLine() {
        if (defaultSMSLine == null || defaultSMSLine.trim().isEmpty()) {
            if (!smsLines.isEmpty()) {
                defaultSMSLine = PhoneNumber.fix(smsLines.get(0).getName());
            }
        }
        return defaultSMSLine != null ? defaultSMSLine : "none";
    }

    public void setDefaultSMSLine(String newLine) {
        this.defaultSMSLine = newLine;
        updateAndSave();
    }

    public Line.PhoneLine getPhoneLine() {
        return phoneLine;
    }

    public List<Line> getSmsLines() {
        return smsLines;
    }

    public List<Line> getVoicemails() {
        return voicemails;
    }

    public List<Line> getFaxLines() {
        return faxLines;
    }

    public void setSmsLines(List<Line> smsLines) {
        // Make a temp list containing all the old channels
        List<Line> temp = new ArrayList<>(this.smsLines);
        // Remove all the new channels, Anything that remains are channels that have been removed, we need to remove the messages from the database
        temp.removeAll(smsLines);
        while (temp.contains(null)){
            temp.remove(null);
        }
        while (smsLines.contains(null)){
            smsLines.remove(null);
        }
        for (Line line : temp){
            if (line == null ){
                continue;
            }
            SMSRepository.getInstance().deleteSMSFromLine(line.getName());
        }
        this.smsLines = smsLines;
        // Since we rely on the server to give us the correct SMS messages we need to wait for the server to respond to the set settings page
        updateAndSave(result -> SMSRepository.getInstance().loadSMSFromServer());
    }

    public void setVoicemails(List<Line> voicemails) {
        this.voicemails = voicemails;
        updateAndSave();
        new VoicemailRepository(AppController.getInstance()).loadVoicemailsFromServer();
    }

    public void setPhoneLine(Line.PhoneLine newPhone) {
        this.phoneLine = newPhone;
        updateAndSave();
        CallHistoryRepository.getInstance().loadCallHistoryFromServer();
    }

    public void setFaxLines(List<Line> faxLines) {
        this.faxLines = faxLines;
        updateAndSave();
        FaxRepository.getInstance(AppController.getInstance()).loadFaxesFromServer();
    }

    public void setFwPhoneNumbers(Map<String, String> fwPhoneNumbers) {
        this.fwPhoneNumbers = fwPhoneNumbers;
    }

    public void setForwardingNumber(String phoneLine, String forwardingNumber) {
        fwPhoneNumbers.put(phoneLine, forwardingNumber);
        updateAndSave();
    }

    public Map<String, String> getFwPhoneNumbers() {
        return fwPhoneNumbers;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "\n defaultSMSLine=" + defaultSMSLine +
                "\n phoneLine=" + phoneLine +
                "\n smsLines=" + smsLines +
                "\n voicemails=" + voicemails +
                "\n faxLines=" + faxLines +
                "\n fwPhoneNumbers=" + fwPhoneNumbers +
                '}';
    }

    private void save() {
        SettingsHelper.putString(SettingsHelper.TELECONSOLE_SETTINGS, new Gson().toJson(this));
        //SettingsHelper.putString();
    }

    public static void fetchSettings(Consumer<TeleConsoleError> completionHandler) {
        // While it is null, let us try to fetch the settings
        if (instance.getValue() == null){
            restoreFromSaved();
        }
        HashMap<String, String> params = new HashMap<>();
        params.put(KEY_TYPE, KEY_TELECONSOLE);
        //android.util.Log.d("Settings01", "requesting");
        request(Request.Method.GET, GET_USER_SETTING, params, false, (result) -> {
            // android.util.Log.d("Settings01", "result = " + result);
            if (result.isJsonObject()) {
               // android.util.Log.d("Settings01", "result is json object");
                JsonElement settingsJson = result.getAsJsonObject().get(KEY_TELECONSOLE);
                if (settingsJson.isJsonObject()) {
                    try {
                        Utils.updateLiveData(instance, new Gson().fromJson(settingsJson, Settings.class));
                    } catch (JsonSyntaxException jse) {
                        new Settings().createAndSave();
                    }
                   // android.util.Log.d("Pubnub", "fetching");
                    PubnubInfo pubnubInfo = new PubnubInfo();
                    PubnubInfo.fetchPubnubInfo();
                    FaxRepository.getInstance(AppController.getInstance()).loadFaxesFromServer();
                    new VoicemailRepository(AppController.getInstance()).loadVoicemailsFromServer();
                    SMSRepository.getInstance().loadSMSFromServer();
                    TeleConsoleProfile profile = TeleConsoleProfile.getInstance();
                    if (instance.getValue() != null && instance.getValue().phoneLine != null && profile.getPhones().contains(instance.getValue().phoneLine)) {
                        int phoneIndex = profile.getPhones().indexOf(instance.getValue().phoneLine);
                        Line.PhoneLine profilePhone = profile.getPhones().get(phoneIndex);
                        instance.getValue().phoneLine.setAppServer(profilePhone.getAppServer());
                        if (!profilePhone.secret.equals(instance.getValue().phoneLine.secret)) {
                            instance.getValue().phoneLine.secret = profilePhone.secret;
                            instance.getValue().save();
                        }
                    } else if (!profile.getPhones().isEmpty() && instance.getValue()!= null) {
                        instance.getValue().phoneLine = profile.getPhones().get(0);
                        instance.getValue().save();
                    }if (instance.getValue() != null) {
                        FullPhone.setupPhone(instance.getValue().phoneLine);
                        //android.util.Log.d("Settings", "Settings are good? " + instance.getValue().toString());
                        completionHandler.accept(null);
                        return;
                    }
                }
            } else if (result.isJsonNull()) {
                //android.util.Log.d("Settings01", "creating new");
                new Settings().createAndSave();
            } else {
                completionHandler.accept(MALFORMED_ERROR);
            }
        }, completionHandler);
    }

    public static void restoreFromSaved() {
        Settings tempSettings = createFromSaved();
        if (tempSettings != null){
            Utils.updateLiveData(instance, tempSettings);
        }
    }

    public static Settings createFromSaved(){
        String savedSettingsJson = SettingsHelper.getString(SettingsHelper.TELECONSOLE_SETTINGS);
        if (savedSettingsJson != null){
            Settings savedSettings = new Gson().fromJson(savedSettingsJson, Settings.class);
            return savedSettings;
        }
        return null;
    }

    private void createAndSave() {
        TeleConsoleProfile profile = TeleConsoleProfile.getInstance();
        if (profile != null && (!profile.getPhones().isEmpty())) {
            Line.PhoneLine first = profile.getPhones().get(0);
            this.setPhoneLine(first);
            FullPhone.setupPhone(first);
            this.updateAndSave();
        }
    }

    private void updateAndSave() {
        AsyncTask.execute(() -> updateAndSave(this::log));
    }

    private void updateAndSave(Consumer<JsonElement> resultHandler) {
        //android.util.Log.d("Bug0018", "is settings updating");
        updateLiveData(instance, this);
        Map<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_TYPE, URLHelper.KEY_TELECONSOLE);
        params.put("settings", new Gson().toJson(this));
       // android.util.Log.d("Bug0018", "is settings requesting");
        URLHelper.request(Request.Method.PUT, URLHelper.GET_USER_SETTING, params, resultHandler, this::log);
        save();
    }

    private void log(Object s) {
        android.util.Log.d("Bug0018", s.toString());
    }

    public void logout() {
        Utils.updateLiveData(instance, null);
    }


//    public int getUserStatuses() {
//        return userStatuses;
//    }
//
//    public void setUserStatuses(List userStatuses) {
//        this.userStatuses = userStatuses.size();
//    }
}
