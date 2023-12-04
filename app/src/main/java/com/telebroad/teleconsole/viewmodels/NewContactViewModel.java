package com.telebroad.teleconsole.viewmodels;

import android.os.Build;

import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.ViewModel;
import androidx.databinding.Observable;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.TeleConsoleContact;
import com.telebroad.teleconsole.model.repositories.ContactRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.android.volley.Request.Method.PUT;
import static com.google.common.base.Strings.nullToEmpty;

public class NewContactViewModel extends ViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    private PhoneNumber numberToAdd;

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    private ArrayList<PhoneNumber> numbers;
    private TeleConsoleContact item;

    public TeleConsoleContact getItem() {
        if (item == null) {
            item = new TeleConsoleContact();
        }
        return item;
    }

    public void setItem(TeleConsoleContact item) {
        this.item = item;
        notifyChange();
    }
    public ArrayList<PhoneNumber> getNumbers(){

        if (numbers == null && item != null){
            numbers = new ArrayList<>(item.getTelephoneLines());
            if (numberToAdd != null) {
                numbers.add(numberToAdd);
//                numberToAdd = null;
            }

          //  android.util.Log.d("ChooseContact01.1", " numbers " + numbers + "item telephone line size " + item.getTelephoneLines().size() + " new number " + numberToAdd + " work " + item.getWork());
        }
        return numbers;
    }

    public String getFirstName() {
        return item.getFname();
    }

    public void setFirstName(String firstName) {
        item.setFname(firstName);
    }

    public String getLastName() {
        return item.getLname();
    }

    public void setLastName(String lastName) {
        item.setLname(lastName);
    }

    public String getCompany() {
        return item.getOrganization();
    }

    public void setCompany(String company) {
        item.setOrganization(company);
    }

    public Boolean isPublic() {
        if (item == null || item.getPublic() == null) {
            return false;
        } else {
            return item.getPublic() >= 1;
        }
    }

    public void setPublic(Boolean aPublic) {
        item.setPublic(aPublic ? 1 : 0);
    }

    void notifyChange() {
        callbacks.notifyCallbacks(this, 0, null);
        numbers = null;
        getNumbers();
    }

    void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

    private String getEmailAsString() {
        if (item == null) {
            return "";
        }
        return Utils.join(",", getItem().getEmailAddresses());
    }

    private String getTelephoneNumberString(PhoneNumber.PhoneType type) {
        if (item == null){
            return "";
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
            return Utils.join(",", getNumbers().stream().filter(item -> item.getPhoneType().equals(type)).map(PhoneNumber::fixed).collect(Collectors.toList()));
        }else{
            ArrayList<String> numbers = new ArrayList<>();

            for (PhoneNumber number : getNumbers()){
                if (number.getPhoneType() == type){
                    numbers.add(number.fixed());
                }
            }
            return Utils.join(",", numbers);
        }
    }

//    private String getFaxString(){
//        ArrayList<String> faxes = new ArrayList<>();
//
//        for (PhoneNumber number : getItem().getFaxLines()){
//                faxes.add(number.fixed());
//        }
//
//        return Utils.join(",", faxes);
//    }
    public void save(Consumer<TeleConsoleError> completionHandler) {
        if (item == null) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        if (item.getId() != null) {
            params.put(URLHelper.KEY_ID, String.valueOf(item.getId()));
        }else{
            params.put(URLHelper.KEY_ID, "");
        }
        if (item.getType() == null){
            item.setContactType("personal");
        }
        params.put(URLHelper.KEY_FNAME, nullToEmpty(item.getFname()));
        params.put(URLHelper.KEY_LNAME, nullToEmpty(item.getLname()));
        params.put(URLHelper.KEY_ORGANIZATION, nullToEmpty(item.getOrganization()));
        params.put(URLHelper.KEY_EMAIL, getEmailAsString());
        item.setHome(getTelephoneNumberString(PhoneNumber.PhoneType.HOME));
        params.put(URLHelper.KEY_HOME, item.getHome());
        item.setMobile(getTelephoneNumberString(PhoneNumber.PhoneType.MOBILE));
        params.put(URLHelper.KEY_MOBILE, item.getMobile());
        item.setWork(getTelephoneNumberString(PhoneNumber.PhoneType.WORK));
        params.put(URLHelper.KEY_WORK, item.getWork());
        item.setFax( getTelephoneNumberString(PhoneNumber.PhoneType.FAX));
        params.put(URLHelper.KEY_FAX, item.getFax());
        params.put(URLHelper.KEY_WEBSITE, nullToEmpty(getItem().getWebsite()));
        params.put(URLHelper.KEY_PUBLIC, String.valueOf(getItem().getPublic()));
        //android.util.Log.d("Contact12", params.toString());
        // android.util.Log.d("Contact12", "contact " + item.toString());
        URLHelper.request(PUT, URLHelper.GET_CONTACT_URL, params, result -> {
            if (result.isJsonPrimitive() && result.getAsJsonPrimitive().isNumber()){
                item.setId(result.getAsInt());
            }
            ContactRepository.getInstance().saveContact(item);
            completionHandler.accept(null);
        }, URLHelper.getDefaultErrorHandler(completionHandler));
    }

    public void setNumberToAdd(PhoneNumber numberToAdd) {
        this.numberToAdd = numberToAdd;
    }
}