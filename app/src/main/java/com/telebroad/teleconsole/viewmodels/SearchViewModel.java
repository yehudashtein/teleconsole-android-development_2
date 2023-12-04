package com.telebroad.teleconsole.viewmodels;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.SearchActivity;
import com.telebroad.teleconsole.controller.dashboard.SearchAllFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchCallHistoryFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchFaxFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchSmsConversationsFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchVoicemailsFragment;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.PhoneContact;
import com.telebroad.teleconsole.model.SearchContactsModel;
import com.telebroad.teleconsole.model.SearchSms;
import com.telebroad.teleconsole.model.SearchSmsConversationsModel;
import com.telebroad.teleconsole.model.SearchSmsMessages;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchViewModel extends ViewModel {
    private static final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> jsonArrayMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> cdrsMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<? extends SearchSms>> smsConversationsMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<? extends SearchSms>> smsMessagesMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> smsMessagesMutableLiveData2 = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> faxMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> voicemailsMutableLiveData = new MutableLiveData<>();
    private final MediatorLiveData<Boolean> allEmptyLiveData = new MediatorLiveData<>();
    public MutableLiveData<List<Contact>> contactsModelsLiveData = new MutableLiveData<>();
    public MutableLiveData<List<? extends Contact>> apiContactsLiveData = new MutableLiveData<>();
    public MediatorLiveData<List<Contact>> mergedContactsLiveData = new MediatorLiveData<>();
    public MediatorLiveData<List<SearchSms>> mergedSmsLiveData = new MediatorLiveData<>();
    public MediatorLiveData<Boolean> getAllEmptyLiveData() {
        return allEmptyLiveData;
    }
    public MutableLiveData<List<Contact>> getContactsModelsLiveData() {return contactsModelsLiveData;}
    public MutableLiveData<JsonArray> getCdrsMutableLiveData() {
        return cdrsMutableLiveData;
    }
    public MutableLiveData<List<? extends SearchSms>> getSmsConversationsMutableLiveData() {return smsConversationsMutableLiveData;}
    public MutableLiveData<JsonArray> getFaxMutableLiveData() {
        return faxMutableLiveData;
    }
    public MutableLiveData<JsonArray> getVoicemailsMutableLiveData() {return voicemailsMutableLiveData;}
    public MutableLiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public MutableLiveData<JsonArray> getSmsMessagesMutableLiveData2() {return smsMessagesMutableLiveData2;
    }

    public MutableLiveData<JsonArray> getJsonArrayMutableLiveData() {return jsonArrayMutableLiveData;}

    public MutableLiveData<List<? extends SearchSms>> getSmsMessagesMutableLiveData() {
        return smsMessagesMutableLiveData;
    }

    public void doMySearch(String query, Activity context) throws AuthFailureError {
        List<Contact> contactsModels = new ArrayList<>();
        if (contactsModelsLiveData.getValue() != null && apiContactsLiveData.getValue() != null) {
            contactsModelsLiveData.getValue().clear();
            apiContactsLiveData.getValue().clear();
        }
        ContactViewModel.personalContacts.observe((LifecycleOwner) context, contacts -> {
            for (Contact c : contacts) {
                if (c.getWholeName() != null) {
                    // Log.d("letsSee1",c.getWholeName());
                    if (c.getWholeName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT).replaceAll("&", " "))) {
                        contactsModels.add(c);
                    }
                }
            }
            if (contactsModels.size() > 0) {
                Utils.updateLiveData(contactsModelsLiveData, contactsModels);
            }
        });
        if (SearchActivity.getBinding().viewPager.getCurrentItem()!=1){
            SearchActivity.getBinding().SearchView.setVisibility(View.VISIBLE);
        }
        //SearchActivity.getBinding().SearchView.setVisibility(View.VISIBLE);
        // SearchVoicemailsFragment.getProgressBar().setVisibility(View.VISIBLE);
//        SearchFaxFragment.getProgressBar().setVisibility(View.VISIBLE);
        //  SearchSmsConversationsFragment.getProgressBar().setVisibility(View.VISIBLE);
        //  SearchCallHistoryFragment.getProgressBar().setVisibility(View.VISIBLE);
        SearchActivity.getBinding().viewPager.setVisibility(View.VISIBLE);
        mergedContactsLiveData.postValue(null);
        //SearchActivity.getBinding().SearchView.setVisibility(View.VISIBLE);
        SearchActivity.getTextView().setHint(query.replaceAll("&", " "));
        Utils.updateLiveData(searchQuery, query);
        HashMap<String, String> params = new HashMap<>();
        params.put("q", query.trim());
        params.put("limit", "100");
        params.put("categories", "fax,vox,smsc,smsm,cdrs");
        String url = URLHelper.getBaseUrl() + "/search/query?";
        URLHelper.request(Request.Method.GET, url, params, jsonElement -> {
            context.runOnUiThread(() -> {
                SearchActivity.getBinding().SearchView.setVisibility(View.GONE);
                SearchActivity.getBinding().viewPager.setVisibility(View.VISIBLE);
            });
            JsonArray contactsArray = jsonElement.getAsJsonObject().getAsJsonArray("contacts");
            JsonArray cdrsArray = jsonElement.getAsJsonObject().getAsJsonArray("cdrs");
            JsonArray smsConversationsArray = jsonElement.getAsJsonObject().getAsJsonArray("smsConversations");
            JsonArray smsMessagesArray = jsonElement.getAsJsonObject().getAsJsonArray("smsMessages");
            JsonArray faxArray = jsonElement.getAsJsonObject().getAsJsonArray("fax");
            JsonArray voicemailsArray = jsonElement.getAsJsonObject().getAsJsonArray("voicemails");
            if (voicemailsArray.size() > 0) {
                Utils.updateLiveData(voicemailsMutableLiveData, voicemailsArray);
            } else Utils.updateLiveData(voicemailsMutableLiveData, null);
            if (faxArray.size() > 0) {
                Utils.updateLiveData(faxMutableLiveData, faxArray);
            } else Utils.updateLiveData(faxMutableLiveData, null);
            if (smsConversationsArray.size() > 0) {
                Gson gson = new Gson();
                Type userListType = new TypeToken<List<SmsConversationsSearchViewModel>>() {}.getType();
                List<SmsConversationsSearchViewModel> users = gson.fromJson(smsConversationsArray.toString(), userListType);
                Utils.updateLiveData(smsConversationsMutableLiveData, users);
            } else Utils.updateLiveData(smsConversationsMutableLiveData, null);
            if (smsMessagesArray.size()>0){
                Gson gson = new Gson();
                Type userListType = new TypeToken<List<SearchSmsMessageViewModel>>() {}.getType();
                List<SearchSmsMessageViewModel> users = gson.fromJson(smsMessagesArray.toString(), userListType);
                Utils.updateLiveData(smsMessagesMutableLiveData2,smsMessagesArray);
                Utils.updateLiveData(smsMessagesMutableLiveData,users);
            }
            if (cdrsArray.size() > 0) {
                Utils.updateLiveData(cdrsMutableLiveData, cdrsArray);
            } else Utils.updateLiveData(cdrsMutableLiveData, null);
            if (contactsArray.size() > 0) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(SearchContactsModel.class, new SearchAllFragment.SearchContactsModelDeserializer())
                        .create();
                Type userListType = new TypeToken<List<SearchContactsModel>>() {}.getType();
                List<SearchContactsModel> users = gson.fromJson(contactsArray.toString(), userListType);
                Utils.updateLiveData(apiContactsLiveData, users);
                //mergedContactsLiveData.postValue(null);
                Utils.updateLiveData(jsonArrayMutableLiveData, contactsArray);
            } else Utils.updateLiveData(jsonArrayMutableLiveData, null);
            context.runOnUiThread(() -> {
                mergedSmsLiveData.removeSource(smsConversationsMutableLiveData);
                mergedSmsLiveData.removeSource(smsMessagesMutableLiveData);
                allEmptyLiveData.removeSource(jsonArrayMutableLiveData);
                allEmptyLiveData.removeSource(cdrsMutableLiveData);
                allEmptyLiveData.removeSource(smsConversationsMutableLiveData);
                allEmptyLiveData.removeSource(faxMutableLiveData);
                allEmptyLiveData.removeSource(voicemailsMutableLiveData);
                mergedContactsLiveData.removeSource(contactsModelsLiveData);
                mergedContactsLiveData.removeSource(apiContactsLiveData);
                allEmptyLiveData.addSource(jsonArrayMutableLiveData, newData -> checkIfAllEmpty());
                allEmptyLiveData.addSource(cdrsMutableLiveData, newData -> checkIfAllEmpty());
                allEmptyLiveData.addSource(smsConversationsMutableLiveData, newData -> checkIfAllEmpty());
                allEmptyLiveData.addSource(faxMutableLiveData, newData -> checkIfAllEmpty());
                allEmptyLiveData.addSource(voicemailsMutableLiveData, newData -> checkIfAllEmpty());
                mergedContactsLiveData.addSource(contactsModelsLiveData, newData -> combineListsAndPost());
                mergedContactsLiveData.addSource(apiContactsLiveData, newData -> combineListsAndPost());
                mergedSmsLiveData.addSource(smsConversationsMutableLiveData, newData -> combineSmsLists());
                mergedSmsLiveData.addSource(smsMessagesMutableLiveData, newData -> combineSmsLists());
            });
        }, teleConsoleError -> {
            mergedContactsLiveData.postValue(null);
        });
    }

    private void checkIfAllEmpty() {
        boolean allEmpty = (jsonArrayMutableLiveData.getValue() == null || jsonArrayMutableLiveData.getValue().size() == 0) &&
                (cdrsMutableLiveData.getValue() == null || cdrsMutableLiveData.getValue().size() == 0) &&
                (smsConversationsMutableLiveData.getValue() == null || smsConversationsMutableLiveData.getValue().size() == 0) &&
                (faxMutableLiveData.getValue() == null || faxMutableLiveData.getValue().size() == 0) &&
                (voicemailsMutableLiveData.getValue() == null || voicemailsMutableLiveData.getValue().size() == 0);
        allEmptyLiveData.setValue(allEmpty);
    }

    private void combineListsAndPost() {
        List<? extends Contact> contactsModels = contactsModelsLiveData.getValue();
        List<? extends Contact> apiContacts = apiContactsLiveData.getValue();
        if (contactsModels != null && apiContacts != null && apiContacts.size() > 0 && contactsModels.size() > 0) {
            List<Contact> combinedList = new ArrayList<>(contactsModels);
            combinedList.addAll(apiContacts);
            mergedContactsLiveData.postValue(combinedList);
        } else {
            mergedContactsLiveData.postValue(null);
        }
    }
    private void combineSmsLists() {
        List<? extends SearchSms> contactsModels = smsConversationsMutableLiveData.getValue();
        List<? extends SearchSms> apiContacts = smsMessagesMutableLiveData.getValue();
        if (contactsModels != null && apiContacts != null && apiContacts.size() > 0 && contactsModels.size() > 0) {
            List<SearchSms> combinedList = new ArrayList<>(contactsModels);
            combinedList.addAll(apiContacts);
            mergedSmsLiveData.postValue(combinedList);
        } else {
            mergedSmsLiveData.postValue(null);
        }
    }
}
