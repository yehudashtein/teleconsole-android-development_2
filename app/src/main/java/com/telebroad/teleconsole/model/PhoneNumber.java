package com.telebroad.teleconsole.model;

import android.os.AsyncTask;
import android.os.Build;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.repositories.ContactRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.room.Ignore;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import static com.google.common.base.Strings.nullToEmpty;


//@Entity(tableName = "phoneNumber", foreignKeys = @ForeignKey(entity = TeleConsoleContact.class, parentColumns = "id", childColumns = "contactID", onDelete = CASCADE))
public class PhoneNumber {
    private static Map<String, PhoneNumber> savedNumbers = new HashMap<>();
    @SerializedName("snumber")
    private String phoneNumber;
    @TypeConverters(PhoneTypeConverter.class)
    private PhoneType phoneType;
    private String defaultName;
    @Ignore
    private String fixed;
    @Ignore
    private String formatted;
    @Ignore
    private MediatorLiveData<String> name;
    @Ignore
    private LiveData<List<? extends Contact>> matchedContacts;
    @Ignore
    private List<? extends Contact> matchedContactsList;
    // Flag for if the phone number is the main number of the customer
    @Ignore
    private boolean isCustomer;
    @Ignore
    public PhoneNumber(String phoneNumber, PhoneType type) {
        this(phoneNumber, type, null);
    }
    public PhoneNumber(String phoneNumber, PhoneType phoneType, String defaultName) {
        if (phoneNumber == null) {
            phoneNumber = AppController.getAppString(R.string.unknown);
        }
        this.phoneNumber = phoneNumber;
        this.phoneType = phoneType;
        this.setDefaultName(defaultName);
    }

    public PhoneNumber(String number) {
        this (number, null);
    }

    public static List<String> convertNumberListToFormatted(List<Number> numbers){
        if (numbers == null || numbers.isEmpty()){
            return new ArrayList<>();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return numbers.stream().map(Number::toFormattedPhoneNumber).collect(Collectors.toList());
        } else {
            List<String> phoneNumbers = new ArrayList<>();
            for (Number number : numbers) {
                phoneNumbers.add(number.toPhoneNumber().formatted());
            }
            return phoneNumbers;
        }
    }
    public static List<PhoneNumber> convertNumberListToPhoneNumbers(List<Number> numbers) {
        if (numbers == null || numbers.isEmpty()){
            return new ArrayList<>();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return numbers.stream().map(Number::toPhoneNumber).collect(Collectors.toList());
        } else {
            List<PhoneNumber> phoneNumbers = new ArrayList<>();
            for (Number number : numbers) {
                phoneNumbers.add(number.toPhoneNumber());
            }
            return phoneNumbers;
        }
    }

    public static List<String> convertPhoneNumberListToFormatted(List<PhoneNumber> phoneNumbers) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return phoneNumbers.stream().map(PhoneNumber::formatted).collect(Collectors.toList());
        } else {
            List<String> strings = new ArrayList<>();
            for (PhoneNumber number : phoneNumbers) {
                strings.add(number.formatted());
            }
            return strings;
        }
    }

    public static List<String> convertPhoneNumberListToFixed(List<PhoneNumber> phoneNumbers) {
        if (phoneNumbers == null) {
            return new ArrayList<>();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return phoneNumbers.stream().map(PhoneNumber::fixed).collect(Collectors.toList());
        } else {
            List<String> strings = new ArrayList<>();
            for (PhoneNumber number : phoneNumbers) {
                strings.add(number.fixed());
            }
            return strings;
        }
    }

    public static String format(String toFormat) {
        return getPhoneNumber(toFormat).formatted();
    }

    public static boolean stringEqualsString(String one, String other){
        return fix(one).equals(fix(other));
    }

    public static String formatLine(Line line) {
        if (line == null){
            return PhoneNumber.format("");
        }
        return getPhoneNumber(line.getName()).formatted();
    }

    @Ignore
    public static PhoneNumber getPhoneNumber(String phoneNumber) {
        return getPhoneNumber(phoneNumber, null, null);
    }

    @Ignore
    public static PhoneNumber getPhoneNumber(String phoneNumber, PhoneType type) {
        return getPhoneNumber(phoneNumber, type, null);
    }

    @Ignore
    public static PhoneNumber getPhoneNumber(String phoneNumber, String defaultName) {
        return new PhoneNumber(phoneNumber, null,  defaultName);
    }

    @Ignore
    public static PhoneNumber getPhoneNumber(String phoneNumber, PhoneType phoneType, String defaultName) {
        String key = fix(phoneNumber);
        PhoneNumber result = savedNumbers.get(key);
        if (result == null){
            result = new PhoneNumber(phoneNumber, phoneType, defaultName);
            savedNumbers.put(key, result);
        }
        return result;
    }

    public String fixed() {
        if (fixed == null) {
            fixed = fix(nullToEmpty(phoneNumber));
        }
        return fixed;
    }

    public static String fix(String phoneNumber) {
        phoneNumber = Strings.nullToEmpty(phoneNumber);
        // First Remove all formatting by removing everything except text
        boolean hadStar =  (phoneNumber.contains("*"));
        if (hadStar){
            //android.util.Log.d("STAR01", "does it have a star? " + phoneNumber);
        }
        String fixing = phoneNumber.replaceAll("[^*#\\d]", "");
        if (hadStar){
           // android.util.Log.d("STAR01", "does it still have a star? " + fixing);
        }
        if (fixing.isEmpty()) {
            return phoneNumber;
        }
        // US Numbers start with a 1 and have 10 additional digits
        // Sometimes the 1 is omitted by lazy people so we need to add it for them
        if (fixing.length() == 10 && !fixing.startsWith("1")) {
            return "1" + fixing;
        }
        return fixing;
    }

    @Override
    public int hashCode() {
        return fixed().hashCode();
    }

    public String formatted() {
        if (formatted == null) {
            // Start with a clean slate
            String result = fixed();
            if (result.isEmpty()) {
                formatted = "Unknown";
                return formatted;
            }
            if (result.startsWith("*") || result.startsWith("#") || result.replaceAll("[^*#\\d]", "").isEmpty()) {
                formatted = result;
                return formatted;
            }
            try {
//                boolean hadStar = result.startsWith("*") || result.startsWith("#");
//                if (hadStar){
//                    android.util.Log.d("STAR01", "does it still have a star in formatted? " + result);
//                }
                Phonenumber.PhoneNumber gPhoneNumber = PhoneNumberUtil.getInstance().parse(result, "US");
//                if (hadStar){
//                    android.util.Log.d("STAR01", "does it still have a star in gphone? " + gPhoneNumber);
//                }
////                formatted = PhoneNumberUtils.formatNumber(result, "US");
                formatted = PhoneNumberUtil.getInstance().format(gPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                return formatted;
            } catch (NumberParseException e) {
                e.printStackTrace();
                formatted = result;
                return formatted;
            }
        }
        return formatted;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        // All fixed and formatted numbers should be recalculated
        fixed = null;
        formatted = null;
       // android.util.Log.d("Contact12", "phonenumber " + phoneNumber + " this " + this.phoneNumber);
        if (fixed().equals(defaultName)){
            setDefaultName("");
        }
    }

   @Override
    public boolean equals(Object other) {
        if (!(other instanceof  PhoneNumber)){
            return false;
        }
      //  android.util.Log.d("PhoneContacts101", "this " + fixed()  + " other " + fixed() );
        return this.fixed().equals(((PhoneNumber)other).fixed());
    }

    public boolean phoneNumberEquals(String other) {
        return equals(getPhoneNumber(other));
    }

    public PhoneType getPhoneType() {
        if (phoneType == null){
            return PhoneType.OTHER;
        }
        return phoneType;
    }

    public void setPhoneType(PhoneType newType) {
        this.phoneType = newType;
    }

    public boolean isShort() {
        return fixed().length() <= 7;
    }

    public boolean isFull(){
        return fixed().length() == 11;
    }

    public boolean isText(){
        return fixed().length() >=5 && fixed().length() <= 16;
    }

    @NonNull
    public <T extends LifecycleOwner & ViewModelStoreOwner> LiveData<String> getName(T owner) {
        return getName(defaultName, owner);
    }

    public  <T extends LifecycleOwner & ViewModelStoreOwner> LiveData<String> resetNameOwner(T owner){
        name = null;
        matchedContacts = null;
        return getName(owner);
    }
    @NonNull
    public <T extends LifecycleOwner & ViewModelStoreOwner> LiveData<String> getName(String defName, T owner) {
       // android.util.Log.d("Match05", "Name for " + formatted() + " is " + (name == null ? "name null": name.getValue()));
        if (name == null) {
           // android.util.Log.d("Match05", "Matches for " + formatted() + " Setting formatted");
            name = new MediatorLiveData<>();
            Utils.updateLiveData(name, this.formatted());
            AsyncTask.execute(() -> getNameBackground(defName));
            name.addSource(getMatchedContacts(), matched -> {
                if (matched == null || matched.isEmpty()) {
                    return;
                }
                matchedContactsList = matched;
//                Utils.updateLiveData(matchedContacts, matched);
                if (isCustomer) {
                    return;
                }
                String name = getStringFromContactList(matched);
                if (name != null && !name.isEmpty()) {
                    Utils.updateLiveData(this.name, name);
                }
            });
        } else {
            //android.util.Log.d("Match04", "name is not null");
        }
        return name;
    }
    // Use this method if name accuracy is not that important and you just want to get at least something
    public String getNameString(){
        if (name == null || name.getValue() == null || name.getValue().isEmpty()){
            return formatted();
        }
        return name.getValue();
    }

    // Matched is list of contacts that were matched
    public String getStringFromContactList(List<? extends Contact> matched) {
        String name;
        // If matched is null, or it is empty
        if (matched == null || matched.isEmpty()){
            // Return null. This will be handled upstream, and should return the phone number formatted correctly
            return null;
            // If there is only one matched contact
        }if (matched.size() == 1) {
            // Get the name of the contact
            name = matched.get(0).getWholeName();
            // If there are 2 contacts matched
        } else if (matched.size() == 2) {
            // Return {contact 1 name} or {contact 2 name}
            name = matched.get(0).getWholeName() + " or " + matched.get(1).getWholeName();
            // Or else, there are more than 2 contacts (list size can't be negative, as it can't contain negative elements)
        } else {
            // Return {contact 1 name} or {x} others
            // Since matched is not ordered essentially what this is doing is picking a contact at random, displaying the name, or x others
            name = matched.get(0).getWholeName() + " or " + (matched.size() - 1) + " others";
        }
        return name;
    }

    @WorkerThread
    public String getNameBackground(){
        return getNameBackground(this.defaultName);
    }

    @WorkerThread
    /**
     * get the caller ID.
     * This should be done in the background, as it contains a database read
     *
     * @param defName provide a name to use if no other is found, can be null or empty
     */
    public String getNameBackground(String defName) {
        // Android stuff
        if (name == null) {
            name = new MediatorLiveData<>();
        }
        // this refers to a PhoneNumber object, it contains a phone number field
        // phoneNumberEquals, checks if the phone number is equal to a given number
        // If the phone number is equal to 611, or Telebroad's main number
        if (this.phoneNumberEquals("611") || this.phoneNumberEquals("12124449911") || this.phoneNumberEquals("2124449911")) {
//            android.util.Log.d("Match04", fixed() + " name is telebroad ");
            // Android way of setting a value, set the name field to Telebroad
            Utils.updateLiveData(name, "Telebroad");
            // Set a flag that this is the main number of a customer (Technically Telebroad is a customer of Telebroad)
            // The reason that this is done, because often enough, people have multiple contacts within Telebroad,
            // we don't want people to see Random Telebroad Employee or x others, therefore we just return Telebroad.
            isCustomer = true;
            return "Telebroad";
            // Else get the number of the customer (found in the profile) if it matches we know that this is the customers number
        } else if (TeleConsoleProfile.getInstance().getPbxCustomer() != null && this.phoneNumberEquals(TeleConsoleProfile.getInstance().getPbxCustomer().getCallerid())) {
            // Set a flag that this is the main number of a customer
            isCustomer = true;
            // Get the customers name from the profile.
            // The reason this is done is the same reason as above, many times the main number of a company will have a lot of contacts.
            Utils.updateLiveData(name, TeleConsoleProfile.getInstance().getPbxCustomer().getName());
            return TeleConsoleProfile.getInstance().getPbxCustomer().getName();
        } else {
            // This does a database read, we don't want this to happen more than once, we want to lazy load it.
            // If the database read was not yet done, and the list is still null
            if (matchedContactsList == null){
                // Execute the database read, query all contacts by phone number
                matchedContactsList = ContactRepository.getInstance().contactByPhoneNumberList(this);
            }
            // Get the name from the contact list (see getStringFromContactList)
            String tempName = getStringFromContactList(matchedContactsList);
            // If the name from that method is not null and not empty
            if (tempName != null && !tempName.isEmpty()){
                // Set the name to the temp name
                Utils.updateLiveData(name, tempName);
                return tempName;
            }
            // fix is a static method that takes away all formatting from a phone number, fixed is a method that calls the fix method on the phone number field
            // if the defaultName is null or empty, or the default name is the phone number
            if (defName == null || defName.isEmpty() || fix(defName).equals(fixed())) {
                // return the formatted version. This step ensures that if defName has a quirky formatting, that it is reset to a consistent formatting.
                Utils.updateLiveData(name, formatted());
                return formatted();
            } else {
                // Or else, defName might acutully be helpful, return the default name
                Utils.updateLiveData(name, defName);
                return defName;
            }
        }
    }

    public LiveData<List<? extends Contact>> getMatchedContacts() {
        if (matchedContacts == null){
            matchedContacts = ContactRepository.getInstance().contactsByPhoneNumber(this);
        }
        return ContactRepository.getInstance(AppController.getInstance()).contactsByPhoneNumber(this);
    }

    public List<? extends Contact> getMatchedContactsList() {
        return matchedContactsList;
    }

    @NonNull
    @Override
    public String toString() {
        return formatted();
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        // Don't set the default name if it is the same as fixed phonenumber
        if (fixed().equals(defaultName)){
            return;
        }
        this.defaultName = defaultName;
    }

    public enum PhoneType {
        MOBILE, WORK, FAX, HOME, MAIN, OTHER, EXTENSION;

        public String formattedName(){
            return String.valueOf(name().charAt(0)).concat(name().toLowerCase().substring(1));
        }
    }

    public static class PhoneTypeConverter {

        @TypeConverter
        public String fromStatus(PhoneType type) {
            if (type == null) {
                return "";
            }
            return type.name();
        }

        @TypeConverter
        public PhoneType toStatus(String name) {
            try {
                return PhoneType.valueOf(name);
            } catch (IllegalArgumentException iae) {
//                android.util.Log.d("ViewContact02", "Type is invalid " + toString());
                return null;
            }
        }
    }
}
