package com.telebroad.teleconsole.model.repositories;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelStoreOwner;

import android.os.AsyncTask;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.PhoneContact;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.TCPhoneNumber;
import com.telebroad.teleconsole.model.TeleConsoleContact;
import com.telebroad.teleconsole.model.TeleConsoleProfile;
import com.telebroad.teleconsole.model.db.ContactDao;
import com.telebroad.teleconsole.model.db.PhoneContactDAO;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

//import static io.fabric.sdk.android.services.common.CommonUtils.isNullOrEmpty;

public class ContactRepository {

    private static ContactRepository instance;
    private MediatorLiveData<List<? extends Contact>> allContacts = new MediatorLiveData<>();
    private MediatorLiveData<List<? extends Contact>> personalContacts;
    private LiveData<List<TeleConsoleContact>> companyContacts;
    private MediatorLiveData<List<? extends Contact>> activeContacts;
    private MediatorLiveData<List<? extends Contact>> filteredPersonalContacts;
    private LiveData<List<TeleConsoleContact>> fiteredCompanyContacts;
    private MediatorLiveData<List<? extends Contact>> filteredContacts;
    private List<TeleConsoleContact> teleConsoleContacts = new ArrayList<>();
    private List<PhoneContact> phoneContacts = new ArrayList<>();
    private List<TeleConsoleContact> personalTeleconsoleContactsList = new ArrayList<>();

    final private ContactDao contactDao;

    public static ContactRepository getInstance() {
        return getInstance(AppController.getInstance());
    }

    public static ContactRepository getInstance(Application context) {
        if (instance == null) {
            instance = new ContactRepository(context);
        }
        return instance;
    }

    private ContactRepository(Application context) {
        contactDao = TeleConsoleDatabase.getInstance(context).contactDao();
    }

    private LiveData<List<TeleConsoleContact>> personalTeleConsoleContacts;

    private LiveData<List<TeleConsoleContact>> getTeleConsoleContacts() {
        return contactDao.loadAllContacts();
    }

    private LiveData<List<TeleConsoleContact>> getPersonalTeleConsoleContacts() {
        if (personalTeleConsoleContacts == null) {
            personalTeleConsoleContacts = contactDao.getPersonalContacts();
        }
        return personalTeleConsoleContacts;
    }

    public void loadContactsFromServer() {
        PhoneContact.loadPhoneContacts();
        PhoneContactDAO.getInstance();
        URLHelper.request(Request.Method.GET, URLHelper.GET_PEOPLE_URL, new HashMap<>(), true, (results) -> {
            if (results instanceof JsonArray) {
                List<TeleConsoleContact> contacts = new Gson().fromJson(results, new TypeToken<List<TeleConsoleContact>>() {
                }.getType());
                List<TeleConsoleContact> goodContacts = new ArrayList<>();
                for (TeleConsoleContact teleConsoleContact : contacts) {
                    if (teleConsoleContact.getType() != null && teleConsoleContact.getType().equals("corporate")) {
                        if ((teleConsoleContact.getPbxLine() != null && !teleConsoleContact.getPbxLine().replaceAll("[^\\d.]", "").isEmpty()) ||
                                (teleConsoleContact.getExtension() != null && !teleConsoleContact.getExtension().replaceAll("[^\\d]", "").isEmpty())) {
                            goodContacts.add(teleConsoleContact);
                        }
                    } else if (teleConsoleContact.getAllLines() != null && teleConsoleContact.getAllLines().size() > 0) {
                        //android.util.Log.d("ContactCheck" ,"Name " + teleConsoleContact.getWholeName() + " is good " + (teleConsoleContact.getTelephoneLines().size() > 0));
                        goodContacts.add(teleConsoleContact);
                    }
                }
                //android.util.Log.d("Contacts", "Contacts size " + goodContacts.size());
                AsyncTask.execute(() -> {
                    contactDao.refresh(goodContacts.toArray(new TeleConsoleContact[]{}));
                    refreshPhoneNumbers(goodContacts);
                });
                AsyncTask.execute(() -> contactDao.refresh(goodContacts.toArray(new TeleConsoleContact[]{})));

            }
        }, error -> {
            Activity activeActivity = AppController.getInstance().getActiveActivity();
            if (activeActivity != null) {
                //activeActivity.runOnUiThread();
            }
        });
    }

    public void refreshPhoneNumbers(List<TeleConsoleContact> goodContacts) {
        AsyncTask.execute(() -> {
            contactDao.deleteAllPhoneNumbers();
            for (TeleConsoleContact contact : goodContacts.toArray(new TeleConsoleContact[]{})) {
                contactDao.savePhoneNumbers(splitTelephoneString(contact.getHome(), PhoneNumber.PhoneType.HOME, contact.getId()));
                contactDao.savePhoneNumbers(splitTelephoneString(contact.getWork(), PhoneNumber.PhoneType.WORK, contact.getId()));
                contactDao.savePhoneNumbers(splitTelephoneString(contact.getMobile(), PhoneNumber.PhoneType.MOBILE, contact.getId()));
                contactDao.savePhoneNumbers(splitTelephoneString(contact.getFax(), PhoneNumber.PhoneType.FAX, contact.getId()));
                if (contact.getContactType() != null && contact.getContactType().equals("corporate")) {
                    if (!isNullOrEmpty(contact.getExtension())) {
                        contactDao.savePhoneNumbers(Collections.singletonList(new TCPhoneNumber(contact.getExtension(), PhoneNumber.PhoneType.EXTENSION, contact.getId(), TCPhoneNumber.Source.TELECONSOLE)));
                    } else if (!isNullOrEmpty(contact.getPbxLine())) {
                        contactDao.savePhoneNumbers(Collections.singletonList(new TCPhoneNumber(contact.getPbxLine(), PhoneNumber.PhoneType.EXTENSION, contact.getId(), TCPhoneNumber.Source.TELECONSOLE)));
                    }
                }
            }
        });
    }

    public LiveData<List<TeleConsoleContact>> getCompanyContacts() {
        if (companyContacts == null || companyContacts.getValue() == null || companyContacts.getValue().isEmpty()) {
            companyContacts = contactDao.getCorporateContacts();
        }
        return companyContacts;
    }

    public LiveData<String> getContactName() {
        MutableLiveData<String> contactName = new MutableLiveData<>();
        return contactName;
    }

    public <T extends LifecycleOwner & ViewModelStoreOwner> MediatorLiveData<List<? extends Contact>> getAllContacts(T owner) {
        if (allContacts == null || allContacts.hasActiveObservers()){
            allContacts = new MediatorLiveData<>();
        }
        if (allContacts.getValue() == null || allContacts.getValue().isEmpty()) {
            allContacts.addSource(getTeleConsoleContacts(), contacts -> {
               // android.util.Log.d("Contacts", "Mediator change running tc");
                List<Contact> all = new ArrayList<>();
                if (contacts != null) {
                    //android.util.Log.d("Contacts Async", "Mediator change running tc adding contacts size " + contacts.size());
                    teleConsoleContacts.clear();
                    teleConsoleContacts.addAll(contacts);
                    all.addAll(contacts);
                    all.addAll(phoneContacts);
                }
//                if (PhoneContact.getPhoneContacts() != null && PhoneContact.getPhoneContacts().getValue() != null) {
//                    android.util.Log.d("Contacts Async", "Mediator change running tc adding phone contacts size " + PhoneContact.getPhoneContacts().getValue().size());
//                    all.addAll(PhoneContact.getPhoneContacts().getValue());
//                }
//
//                android.util.Log.d("Contacts Async", "Mediator change running tc set to execute");
                AsyncTask.execute(() -> {
                   // android.util.Log.d("Contacts Async", "Mediator change running tc starting");
                    Collections.sort(all);
                    //android.util.Log.d("Contacts Async", "Mediator change running tc sorting all " + all.size());
                    Utils.updateLiveData(allContacts, all);
                });
            });
            try {
                allContacts.addSource(PhoneContactDAO.getInstance().getContactList(owner), contacts -> {
                  //  android.util.Log.d("Contacts88", "Mediator change running adding phone matchedTCcontacts");
                    List<Contact> all = new ArrayList<>();
                    if (contacts != null) {
                       // android.util.Log.d("Contacts Async", "Mediator change running phone adding contacts size " + contacts.size());
                        phoneContacts.clear();
                        phoneContacts.addAll(contacts);
                        all.addAll(contacts);
                        all.addAll(teleConsoleContacts);
                    }
//                if (!teleConsoleContacts.isEmpty()) {
//                    android.util.Log.d("Contacts Async", "Mediator change running phone adding tc contacts size " + teleConsoleContacts.size());
//                    all.addAll(teleConsoleContacts);
//                }

                   // android.util.Log.d("Contacts Async", "Mediator change running phone set to execute");
                    AsyncTask.execute(() -> {
                       // android.util.Log.d("Contacts Async", "Mediator change running phone sorting all, size " + all.size());
                        Collections.sort(all);
                       // android.util.Log.d("Contacts Async", "Mediator change phone updating live data");
                        Utils.updateLiveData(allContacts, all);
                    });
                });
            }catch (IllegalArgumentException ile){
                ///android.util.Log.d("Contact90", "error", ile);
            }
        }
        return allContacts;
    }

    public  <T extends LifecycleOwner & ViewModelStoreOwner> MediatorLiveData<List<? extends Contact>> getPersonalContacts(T owner) {
        if (personalContacts == null || personalContacts.getValue() == null || personalContacts.getValue().isEmpty()) {
            personalContacts = new MediatorLiveData<>();
            personalContacts.addSource(PhoneContactDAO.getInstance().getContactList(owner), contacts -> {
              //  android.util.Log.d("Contacts88", "Mediator change running adding phone matchedTCcontacts");
                List<Contact> all = new ArrayList<>();
                if (contacts != null) {
                    phoneContacts.clear();
                    phoneContacts.addAll(contacts);
                    all.addAll(contacts);
                    all.addAll(personalTeleconsoleContactsList);
                }
//                if (getPersonalTeleConsoleContacts().getValue() != null) {
//                    all.addAll(getTeleConsoleContacts().getValue());
//                }
                AsyncTask.execute(() -> {
                    Collections.sort(all);
                    Utils.updateLiveData(personalContacts, all);
                });
            });
            personalContacts.addSource(getPersonalTeleConsoleContacts(), contacts -> {
               /// android.util.Log.d("Contacts", "Mediator change running");
                List<Contact> all = new ArrayList<>();
                if (contacts != null) {
                    personalTeleconsoleContactsList.clear();
                    personalTeleconsoleContactsList.addAll(contacts);
                    all.addAll(contacts);
                    all.addAll(phoneContacts);
                }
//                if (PhoneContact.getPhoneContacts() != null && PhoneContact.getPhoneContacts().getValue() != null) {
//                    //noinspection ConstantConditions
//                    all.addAll(PhoneContact.getPhoneContacts().getValue());
//                }
                AsyncTask.execute(() -> {
                    Collections.sort(all);
                    Utils.updateLiveData(personalContacts, all);
                });
            });

        }
        return personalContacts;
    }

    MediatorLiveData<List<? extends Contact>> matchedContacts;
    LiveData<List<PhoneContact>> matchedPhoneContacts = PhoneContact.matchedContacts;
    LiveData<List<TeleConsoleContact>> matchedTCcontacts;

    public MediatorLiveData<List<? extends Contact>> getMatchedContacts() {
        if (matchedContacts == null || matchedContacts.getValue() == null || matchedContacts.getValue().isEmpty()) {
            matchedContacts = new MediatorLiveData<>();
        }
        return matchedContacts;
    }

    public void findContact(String query) {
        matchedContacts.removeSource(matchedTCcontacts);
        matchedContacts.removeSource(matchedPhoneContacts);
        matchedTCcontacts = contactDao.searchContacts("%" + query + "%");
        PhoneContact.searchContacts(query);
        mergeContactSources();
    }

    private void mergeContactSources() {
        matchedContacts.addSource(matchedTCcontacts, teleConsoleContacts -> {
            List<Contact> all = new ArrayList<>();
            if (teleConsoleContacts != null) {
                all.addAll(teleConsoleContacts);
            }
            if (matchedPhoneContacts.getValue() != null) {
                all.addAll(matchedPhoneContacts.getValue());
            }
            Utils.updateLiveData(matchedContacts, all);
            AsyncTask.execute(() -> {
                Collections.sort(all);
                Utils.updateLiveData(matchedContacts, all);
            });
//            matchedContacts.setValue(all);
        });
        matchedContacts.addSource(matchedPhoneContacts, matchedPhoneContacts -> {
            List<Contact> all = new ArrayList<>();
            if (matchedPhoneContacts != null) {
                all.addAll(matchedPhoneContacts);
            }
            if (matchedTCcontacts.getValue() != null) {
                all.addAll(matchedTCcontacts.getValue());
            }
            Utils.updateLiveData(matchedContacts, all);
            AsyncTask.execute(() -> {
                Collections.sort(all);
                Utils.updateLiveData(matchedContacts, all);
            });
            //matchedContacts.setValue(all);
        });
    }

    public  LiveData<List<? extends Contact>> contactsByPhoneNumber(PhoneNumber phoneNumber) {
        MediatorLiveData<List<? extends Contact>> contacts = new MediatorLiveData<>();
        String pnString = phoneNumber.fixed();
        if (pnString.length() <= 7) {
            //android.util.Log.d("Bug0017", "Phone number to check " + phoneNumber);
            contacts.addSource(contactDao.matchCompanyContactsToNumber(pnString), contacts::setValue);
            return contacts;
        } else {
            if (pnString.length() == 11 && pnString.startsWith("1")) {
                pnString = pnString.substring(1);
            }
            String finalString = pnString;
            List<PhoneContact> phoneMatchedContacts = new ArrayList<>();
            List<TeleConsoleContact> teleConsoleMatchedContacts = new ArrayList<>();
            contacts.addSource(contactDao.matchContactsToNumber("%" + pnString + "%"), (matched) -> {

                //android.util.Log.d("ContactDAO03", "matched teleconsole contacts for " + finalString  + " are " + matched);
                if (matched != null) {
                    teleConsoleMatchedContacts.clear();
                    teleConsoleMatchedContacts.addAll(matched);
                    List<Contact> all = new ArrayList<>();
                    all.addAll(teleConsoleMatchedContacts);
                    all.addAll(phoneMatchedContacts);
                    Utils.updateLiveData(contacts, all);
                    //contacts.setValue(all);
                }
            });
             contacts.addSource(PhoneContactDAO.getInstance().matchContactToNumber(pnString), matched -> {
                //android.util.Log.d("ContactDAO03", "matched phone contacts for " + finalString  + " are " + matched);
                if (matched != null) {
                    phoneMatchedContacts.clear();
                    phoneMatchedContacts.addAll(matched);
                    List<Contact> all = new ArrayList<>();
                    all.addAll(teleConsoleMatchedContacts);
                    all.addAll(phoneMatchedContacts);
                    Utils.updateLiveData(contacts, all);
                    //contacts.setValue(all);
                }
            });
        }
        return contacts;
    }

    @WorkerThread
    public List<? extends Contact> contactByPhoneNumberList(PhoneNumber phoneNumber){
        if (phoneNumber.fixed() == null || phoneNumber.fixed().isEmpty()){
            return new ArrayList<>();
        }
        List<Contact> matchedContacts = new ArrayList<>();
        matchedContacts.addAll(PhoneContact.getMatchedContactsList(phoneNumber.fixed()));
        matchedContacts.addAll(contactDao.matchContactsToNumberList(phoneNumber.fixed()));
        Collections.sort(matchedContacts);
        return matchedContacts;
    }
    public void findCompanyContact(String query) {
        matchedContacts.removeSource(matchedTCcontacts);
        matchedContacts.removeSource(matchedPhoneContacts);
        matchedTCcontacts = contactDao.searchCompanyContacts("%" + query + "%");
        matchedContacts.addSource(matchedTCcontacts, (contacts) -> {
            Utils.updateLiveData(matchedContacts, contacts);
        });
    }

    public void findPersonalContacts(String query) {
        matchedContacts.removeSource(matchedTCcontacts);
        matchedContacts.removeSource(matchedPhoneContacts);
        matchedTCcontacts = contactDao.searchPersonalContacts("%" + query + "%");
        PhoneContact.searchContacts(query);
        mergeContactSources();
    }

    public TeleConsoleContact getContactByID(String id) {
        return contactDao.getContactFromID(id);
    }

    public LiveData<TeleConsoleContact> getLiveContactByID(String id){

        return contactDao.getLiveContactFromID(id);
    }

    public PhoneContact getPhoneContactByID(String id ) {
        return PhoneContact.getContactByID(id);
    }

    public void saveContact(TeleConsoleContact... item) {
        AsyncTask.execute(() -> contactDao.save(item));
    }

    public void deleteContact(String contactID) {
        contactDao.deleteContact(contactID);
    }

    private static class RefreshContactAsyncTask extends AsyncTask<TeleConsoleContact, Void, Void> {
        private ContactDao mAsyncTaskDao;

        RefreshContactAsyncTask(ContactDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(TeleConsoleContact... contacts) {

            return null;
        }
    }

    private static List<TCPhoneNumber> splitTelephoneString(String telephoneString, PhoneNumber.PhoneType type, Integer contactID) {
        ArrayList<TCPhoneNumber> results = new ArrayList<>();
        if (telephoneString == null || telephoneString.trim().isEmpty()) {
            return results;
        } else {
            for (String number : telephoneString.split(",")) {
                TCPhoneNumber newNumber = new TCPhoneNumber(number, type, contactID, TCPhoneNumber.Source.TELECONSOLE);
                results.add(newNumber);
            }
            return results;
        }
    }
    public LiveData<String> findContactByID(String id) {
     //   android.util.Log.d("ID", "sent by id");
        MediatorLiveData<String> mediatorLiveData = new MediatorLiveData<>();
        if (TeleConsoleProfile.getInstance() == null || id != TeleConsoleProfile.getInstance().getPbxUid()) {
            mediatorLiveData.addSource(contactDao.getNameFromID("-" + id), contact -> {
                if (contact != null){
                    Utils.updateLiveData(mediatorLiveData, contact.getWholeName());
                }
            });
        }
        return mediatorLiveData;
    }
}
