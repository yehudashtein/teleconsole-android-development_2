package com.telebroad.teleconsole.viewmodels;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelStoreOwner;

import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.TeleConsoleContact;
import com.telebroad.teleconsole.model.repositories.ContactRepository;

import java.util.List;

import static com.telebroad.teleconsole.viewmodels.ContactViewModel.ContactListType.ALL;
import static com.telebroad.teleconsole.viewmodels.ContactViewModel.ContactListType.COMPANY;
import static com.telebroad.teleconsole.viewmodels.ContactViewModel.ContactListType.PERSONAL;

public class ContactViewModel extends ViewModel{

    public MediatorLiveData<List<? extends Contact>> viewableContacts = new MediatorLiveData<>();
    private boolean isFiltering;
    private MediatorLiveData<List<? extends Contact>> activeContacts;

    private ContactRepository contactRepository = ContactRepository.getInstance(AppController.getInstance());
    public static LiveData<List<? extends Contact>> personalContacts;
    private LiveData<List<? extends Contact>> allContacts;
    private LiveData<List<TeleConsoleContact>> companyContacts = contactRepository.getCompanyContacts();

    public MediatorLiveData<List<? extends Contact>> filteredContacts = contactRepository.getMatchedContacts();

    public void setCurrentType(ContactListType currentType) {
        this.currentType = currentType;
    }

    private ContactListType currentType;
    public <T extends LifecycleOwner & ViewModelStoreOwner> void setActiveContacts(ContactListType type, T owner) {
        if (type == currentType){
            return;
        }
        currentType = type;

        switch (type){
            case ALL:
                if (activeContacts != null && allContacts != null) {
                    Utils.updateLiveData(activeContacts, allContacts.getValue());
                }
                break;
            case COMPANY:
                if (activeContacts != null) {
                    Utils.updateLiveData(activeContacts, companyContacts.getValue());
                }
                break;
            case PERSONAL:
                if (activeContacts != null && personalContacts != null) {
                    Utils.updateLiveData(activeContacts, personalContacts.getValue());
                }
                break;
        }
    }

    private ContactListType getCurrentType(){
        return currentType != null ? currentType : ALL;
    }

    public void searchContact(String query){
        switch (getCurrentType()){
            case ALL:
                contactRepository.findContact(query);
                break;
            case COMPANY:
                contactRepository.findCompanyContact(query);
                break;
            case PERSONAL:
                contactRepository.findPersonalContacts(query);
                break;
        }
    }

    public  <T extends LifecycleOwner & ViewModelStoreOwner>  MediatorLiveData<List<? extends Contact>> getActiveContacts(T owner) {
        if (activeContacts == null) {
            allContacts = contactRepository.getAllContacts(owner);
            personalContacts = contactRepository.getPersonalContacts(owner);
            companyContacts = contactRepository.getCompanyContacts();
            activeContacts = new MediatorLiveData<>();
            activeContacts.addSource(allContacts, contacts -> {
                if (getCurrentType() == ALL) {
                    Utils.updateLiveData(activeContacts, contacts);
                }
            });
            activeContacts.addSource(personalContacts, contacts -> {
                if (getCurrentType() == PERSONAL) {
                    Utils.updateLiveData(activeContacts, contacts);
                }
            });
            activeContacts.addSource(companyContacts, contacts -> {
                if (getCurrentType() == COMPANY) {
                    Utils.updateLiveData(activeContacts, contacts);
                }
            });
        }
        return activeContacts;
    }

    public enum ContactListType {
        ALL, COMPANY, PERSONAL
    }

}
