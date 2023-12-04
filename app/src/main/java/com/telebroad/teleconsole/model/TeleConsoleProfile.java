
package com.telebroad.teleconsole.model;

import android.app.Activity;
import android.app.NotificationManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.LeaveMessage;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.login.SignInActivity;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;
import com.telebroad.teleconsole.model.repositories.CallHistoryRepository;
import com.telebroad.teleconsole.pjsip.PJSIPManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.android.volley.Request.Method.PUT;
import static com.google.common.base.Strings.nullToEmpty;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.DISABLED_USER_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.NO_PHONES_ERROR;
import static com.telebroad.teleconsole.helpers.Utils.updateLiveData;
import static com.telebroad.teleconsole.model.Line.PhoneLine;

public class TeleConsoleProfile implements Parcelable {

    //<editor-fold desc="variable declerations">
    private String id;
    private String username;
    @SerializedName("pbx_uid")
    private String pbxUid;
    @SerializedName("pbx_cid")
    private String pbxCid;
    @SerializedName("pbx_role")
    private String pbxRole;
    private String title;
    private String firstname;
    private String lastname;
    private String email;
    private String telephone;
    private String extension;
    private String mobile;
    private String fax;
    private String address1;
    private String address2;
    private String address3;
    @SerializedName("address_city")
    private String addressCity;
    @SerializedName("address_state")
    private String addressState;
    @SerializedName("address_code")
    private String addressCode;
    private String country;
    @SerializedName("address_other")
    private String addressOther;
    private String language;
    private String timezone;
    @SerializedName("opt_email")
    private String optEmail;
    private String photo;
    private String status;
    @SerializedName("status_msg")
    private String statusMsg;
    @SerializedName("status_time")
    private String statusTime;
    @SerializedName("pbx_customer")
    private PbxCustomer pbxCustomer;
    private List<PhoneLine> phones = new ArrayList<>();
    @SerializedName("sms_lines")
    private List<Line> smsLines;
    @SerializedName("fax_lines")
    private List<Line> faxLines;
    @SerializedName("fax_boxes")
    private List<Line> faxBoxes;
    @SerializedName("vox_boxes")
    private List<Line> voxBoxes;

    private final MutableLiveData<List<Number>> ownedPhoneNumber;
    //</editor-fold>

    //<editor-fold desc="parcelable implentation">
    protected TeleConsoleProfile(Parcel in) {
        id = in.readString();
        username = in.readString();
        pbxUid = in.readString();
        pbxCid = in.readString();
        pbxRole = in.readString();
        title = in.readString();
        firstname = in.readString();
        lastname = in.readString();
        email = in.readString();
        telephone = in.readString();
        extension = in.readString();
        mobile = in.readString();
        fax = in.readString();
        address1 = in.readString();
        address2 = in.readString();
        address3 = in.readString();
        addressCity = in.readString();
        addressState = in.readString();
        addressCode = in.readString();
        country = in.readString();
        addressOther = in.readString();
        language = in.readString();
        timezone = in.readString();
        optEmail = in.readString();
        photo = in.readString();
        status = in.readString();
        statusMsg = in.readString();
        statusTime = in.readString();
        ownedPhoneNumber = new MutableLiveData<>();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(pbxUid);
        dest.writeString(pbxCid);
        dest.writeString(pbxRole);
        dest.writeString(title);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(email);
        dest.writeString(telephone);
        dest.writeString(extension);
        dest.writeString(mobile);
        dest.writeString(fax);
        dest.writeString(address1);
        dest.writeString(address2);
        dest.writeString(address3);
        dest.writeString(addressCity);
        dest.writeString(addressState);
        dest.writeString(addressCode);
        dest.writeString(country);
        dest.writeString(addressOther);
        dest.writeString(language);
        dest.writeString(timezone);
        dest.writeString(optEmail);
        dest.writeString(photo);
        dest.writeString(status);
        dest.writeString(statusMsg);
        dest.writeString(statusTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TeleConsoleProfile> CREATOR = new Creator<TeleConsoleProfile>() {
        @Override
        public TeleConsoleProfile createFromParcel(Parcel in) {
            return new TeleConsoleProfile(in);
        }

        @Override
        public TeleConsoleProfile[] newArray(int size) {
            return new TeleConsoleProfile[size];
        }
    };

    public static LiveData<TeleConsoleProfile> getLiveInstance(){
        if (instance.getValue() == null) {
            AsyncTask.execute(TeleConsoleProfile::getInstance);
        }
        return instance;
    }
    //</editor-fold>

    @NonNull
    public static TeleConsoleProfile getInstance() {

       // android.util.Log.d("GETPROFILE", "Getting instance");
        // Profile is null, usually when the app is starting.
        if (instance.getValue() == null){
            // First set up from saved
            setupFromSaved();
            // sign in again
            signIn((error) -> {
                if (error != null) {
                    Activity activity = AppController.getInstance().getActiveActivity();
                    if (activity != null){
                        activity.runOnUiThread(() -> {
                            Intent intent = new Intent(activity, SignInActivity.class);
                            activity.startActivity(intent);
                        });
                    }
                }
            });
            String savedProfile = SettingsHelper.getString(SettingsHelper.TELECONSOLE_PROFILE);
            if (savedProfile != null){
                TeleConsoleProfile tempProfile = new Gson().fromJson(savedProfile, TeleConsoleProfile.class);
                if (tempProfile == null){
                    tempProfile = new TeleConsoleProfile();
                }
                return tempProfile;
            }else{
                return new TeleConsoleProfile();
            }
        }
        return instance.getValue();
    }

    public static void setInstance(TeleConsoleProfile newProfile) {
        if (newProfile != null) {
            SettingsHelper.putString(SettingsHelper.TELECONSOLE_PROFILE, new Gson().toJson(newProfile));
            Activity activity = AppController.getInstance().getActiveActivity();
        }
        updateLiveData(instance, newProfile);
//        if (activity != null){
//            AppController.getInstance().getActiveActivity().runOnUiThread(() -> TeleConsoleProfile.instance.setValue(newProfile));
//        }else{
//            TeleConsoleProfile.instance.postValue(newProfile);
//        }
    }

    public TeleConsoleProfile(){
        ownedPhoneNumber = new MutableLiveData<>();
        username = SettingsHelper.getString(SettingsHelper.TELEBROAD_USERNAME);
    }

    //<editor-fold desc="getters and setters">
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPbxUid() {
        return pbxUid;
    }
    public void setPbxUid(String pbxUid) {
        this.pbxUid = pbxUid;
    }
    public String getPbxCid() {
        return pbxCid;
    }
    public void setPbxCid(String pbxCid) {
        this.pbxCid = pbxCid;
    }
    public String getPbxRole() {
        return pbxRole;
    }
    public void setPbxRole(String pbxRole) {
        this.pbxRole = pbxRole;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public String getExtension() {
        return extension;
    }
    public void setExtension(String extension) {
        this.extension = extension;
    }
    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getFax() {
        return fax;
    }
    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getAddress1() {
        return address1;
    }
    public void setAddress1(String address1) {
        this.address1 = address1;
    }
    public String getAddress2() {
        return address2;
    }
    public void setAddress2(String address2) {
        this.address2 = address2;
    }
    public String getAddress3() {
        return address3;
    }
    public void setAddress3(String address3) {
        this.address3 = address3;
    }
    public String getAddressCity() {
        return addressCity;
    }
    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }
    public String getAddressState() {
        return addressState;
    }
    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }
    public String getAddressCode() {
        return addressCode;
    }
    public void setAddressCode(String addressCode) {
        this.addressCode = addressCode;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getAddressOther() {
        return addressOther;
    }
    public void setAddressOther(String addressOther) {
        this.addressOther = addressOther;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language ;
    }
    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    public String getOptEmail() {
        return optEmail;
    }
    public void setOptEmail(String optEmail) {
        this.optEmail = optEmail;
    }
    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatusMsg() {
        return statusMsg;
    }
    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }
    public String getStatusTime() {
        return statusTime;
    }
    public void setStatusTime(String statusTime) {
        this.statusTime = statusTime;
    }
    public PbxCustomer getPbxCustomer() {
        return pbxCustomer;
    }
    public void setPbxCustomer(PbxCustomer pbxCustomer) {
        this.pbxCustomer = pbxCustomer;
    }

    public List<PhoneLine> getPhones() {
        return phones;
    }
    public void setPhones(List<PhoneLine> phones) {
        this.phones = phones;
    }
    public List<Line> getSmsLines() {
        return smsLines;
    }
    public void setSmsLines(List<Line> smsLines) {
        this.smsLines = smsLines;
    }
    public List<Line> getFaxLines() {
        return faxLines;
    }
    public void setFaxLines(List<Line> faxLines) {
        this.faxLines = faxLines;
    }
    public List<Line> getFaxBoxes() {
        return faxBoxes;
    }
    public void setFaxBoxes(List<Line> faxBoxes) {
        this.faxBoxes = faxBoxes;
    }
    public List<Line> getVoxBoxes() {
        return voxBoxes;
    }
    public void setVoxBoxes(List<Line> voxBoxes) {
        this.voxBoxes = voxBoxes;
    }


    public LiveData<List<Number>> getLiveOwnedPhoneNumber(){
        return ownedPhoneNumber;
    }
    public List<Number> getOwnedPhoneNumber() {
        try {
            return ownedPhoneNumber.getValue();
        }catch (ClassCastException cse){
            return new ArrayList<>();
        }
    }

    public List<String> getOwnedPhoneNumbersAsStrings(){
        if (ownedPhoneNumber.getValue() == null){
            return new ArrayList<>();
        }
        return ownedPhoneNumber.getValue().stream().map(Number::getSnumber).collect(Collectors.toList());
    }
    public void setOwnedPhoneNumber(List<Number> ownedPhoneNumber) {
        Utils.updateLiveData(this.ownedPhoneNumber, ownedPhoneNumber);
    }

    //</editor-fold>

    public String getFullName(){
        String first = nullToEmpty(firstname);
        String last = nullToEmpty(lastname);
        if ((first + last).trim().isEmpty()){
            return "Unknown";
        }
        return (first + " " + last).trim();
    }

    @Override
    public String toString() {
        return "TeleConsoleProfile{" +
                "\nid=" + id +
                " \nusername=" + username +
                " \npbxUid=" + pbxUid +
                " \npbxCid=" + pbxCid +
                " \npbxRole=" + pbxRole +
                " \ntitle=" + title +
                " \nfirstname=" + firstname +
                " \nlastname=" + lastname +
                " \nemail=" + email +
                " \ntelephone=" + telephone +
                " \nextension=" + extension +
                " \nmobile=" + mobile +
                " \nfax=" + fax +
                " \naddress1=" + address1 +
                " \naddress2=" + address2 +
                " \naddress3=" + address3 +
                " \naddressCity=" + addressCity +
                " \naddressState=" + addressState +
                " \naddressCode=" + addressCode +
                " \ncountry=" + country +
                " \naddressOther=" + addressOther +
                " \nlanguage=" + language +
                " \ntimezone=" + timezone +
                " \noptEmail=" + optEmail +
                " \nphoto=" + photo +
                " \nstatus=" + status +
                " \nstatusMsg=" + statusMsg +
                " \nstatusTime=" + statusTime +
                " \npbxCustomer=" + pbxCustomer +
                " \nphones=" + phones +
                " \nsmsLines=" + smsLines +
                " \nfaxLines=" + faxLines +
                " \nfaxBoxes=" + faxBoxes +
                " \nvoxBoxes=" + voxBoxes +
                '}';
    }

    private static HashMap<String, TeleConsoleError>  errors = new HashMap<>();

    public static void signIn(Consumer<TeleConsoleError> completionHandler){
        // Right away set up from saved info
        setupFromSaved();
       // android.util.Log.d("Profile01", "requesting");
        // Update the profile from the server
        fetchProfile(URLHelper.getDefaultErrorHandler(completionHandler));

    }

    public static void fetchProfile(Consumer<TeleConsoleError> completionHandler) {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put(URLHelper.KEY_PHONES, "1");
        loginParams.put(URLHelper.KEY_VOX, "1");
        loginParams.put(URLHelper.KEY_FAX, "1");
        loginParams.put(URLHelper.KEY_SMS, "1");
        URLHelper.request(Request.Method.GET, URLHelper.GET_MYPROFILE_URL , loginParams, (JsonElement results) -> {
            //android.util.Log.d("Profile01", "results = " + results);
            Gson gson = new Gson();
            setInstance(gson.fromJson(results, TeleConsoleProfile.class));
            if (getInstance().getPhones() == null || getInstance().getPhones().isEmpty()){
                completionHandler.accept(NO_PHONES_ERROR);
                return;
            }
            if (getInstance().getPbxRole().equals("6")){
                completionHandler.accept(DISABLED_USER_ERROR);
                return;
            }
            //android.util.Log.d("Dashboard", "start gathering info ");
            getInstance().fetchNumbers ();
            Contact.loadContacts((error) -> {
               // android.util.Log.d("Dashboard", "loading contacts error " + error);
                if (error != null){
                    errors.put("Contacts", error);
                }
                completeSignIn(completionHandler);
            });
            Settings.fetchSettings((error) -> {
                if (error != null){
                    errors.put("Settings", error);
                }
                completeSignIn(completionHandler);
            });
            CallHistoryRepository.getInstance().loadCallHistoryFromServer();
//            android.util.Log.d("MainActivity" ,"MyProfile results " + getInstance().toString());
        }, (error) -> {
            if (error != null && !error.getErrorMessage().isEmpty()){
               // android.util.Log.e("LOGIN", "error " + error);
                completionHandler.accept(error);
            }
        });
    }

    private static void setupFromSaved() {
        String savedProfile = SettingsHelper.getString(SettingsHelper.TELECONSOLE_PROFILE);
        if (savedProfile != null){
            setInstance(new Gson().fromJson(savedProfile, TeleConsoleProfile.class));
        }else{

        }
        String savedPhone = SettingsHelper.getString(SettingsHelper.FULL_PHONE);
        if (savedPhone != null){
            FullPhone.setInstance(new Gson().fromJson(savedPhone, FullPhone.class));
        }
    }

    private static MutableLiveData<TeleConsoleProfile> instance = new MutableLiveData<>();
    private static void completeSignIn(Consumer<TeleConsoleError> completionHandler){
        completionHandler.accept(errors.get("Contact"));
    }

    public static void logout(Context context) {
        // Logging out let's do some cleanup
        // Deregister
        //android.util.Log.d("PJSIP_LOGOUT", " in profile username " + SettingsHelper.getString(SettingsHelper.SIP_USERNAME) + " domain " + SettingsHelper.getString(SettingsHelper.SIP_DOMAIN));
        SipManager.getInstance().logout(SettingsHelper.getString(SettingsHelper.SIP_USERNAME), SettingsHelper.getString(SettingsHelper.SIP_DOMAIN));
        // Clear shared prefs
        PJSIPManager.getInstance().logout(SettingsHelper.getString(SettingsHelper.SIP_USERNAME, ""), SettingsHelper.getString(SettingsHelper.SIP_DOMAIN));
        SettingsHelper.clear();
        // Dismiss all Notifications
        NotificationManagerCompat.from(AppController.getInstance()).cancelAll();
        ((NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        // Logout from the push notifications
        if (PubnubInfo.getInstance() != null){
            PubnubInfo.getInstance().logout();
        }
        LeaveMessage leaveMessage = new LeaveMessage("leaveChat","fnd");
        ChatWebSocket.getInstance().sendObject("leave",leaveMessage);
        // Clear the database (Async so it doesn't block the main thread)
        AsyncTask.execute(() -> {
            TeleConsoleDatabase.getInstance(context).clearAllTables();
            TeleConsoleProfile.setInstance(null);
            if( Settings.getInstance() != null) Settings.getInstance().logout();
        });
    }

    public static class PbxCustomer {
        private String id;
        private String name;
        private String callerid;
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getCallerid() {
            return callerid;
        }
        public void setCallerid(String callerid) {
            this.callerid = callerid;
        }

        @Override
        public String toString() {
            return "PbxCustomer{" +
                    "\nid=" + id +
                    "\nname=" + name +
                    "\ncallerid=" + callerid +
                    '}';
        }
    }

    public void fetchNumbers(){
        URLHelper.request(Request.Method.GET, URLHelper.GET_NUMBERS_URL, new HashMap<>(), true, (results) -> {
            //android.util.Log.d("OwnedPN", results.toString());
            if (results instanceof JsonArray) {
                results.getAsJsonArray().iterator();
                Utils.updateLiveData(ownedPhoneNumber, new Gson().fromJson(results, new TypeToken<List<Number>>(){}.getType()));
                //android.util.Log.d("OwnedPN", ownedPhoneNumber.getValue() + "");
            }
        }, error -> {
           // android.util.Log.d("OwnedPN", "error " + error.getFullErrorMessage());
        });
    }

    public void updateCredentials(String newUsername, String newPassword, Consumer<JsonElement> completionHandler, Consumer<TeleConsoleError> errorHandler ){
        Map<String, String> params = new HashMap<>();
        setUsername(newUsername);
        params.put(URLHelper.KEY_NEW_USERNAME, newUsername);
        params.put(URLHelper.KEY_NEW_PASSWORD, newPassword);
        URLHelper.request(PUT, URLHelper.GET_MYCREDENTIALS_URL, params, completionHandler, errorHandler);
    }

    public void updateProfile(String fname, String lname, String email,Consumer<JsonElement> completionHandler, Consumer<TeleConsoleError> errorHandler){
        setFirstname(fname);
        setLastname(lname);
        setEmail(email);
        Map<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_FIRSTNAME, getFirstname());
        params.put(URLHelper.KEY_LASTNAME, getLastname());
        params.put(URLHelper.KEY_EMAIL, getEmail());
        params.put(URLHelper.KEY_EXTENSION, getExtension());
        params.put(URLHelper.KEY_MOBILE, getMobile());
        params.put(URLHelper.KEY_COUNTRY, getCountry());
        params.put(URLHelper.KEY_TIMEZONE, getTimezone());
        URLHelper.request(PUT, URLHelper.GET_MYPROFILE_URL, params,  completionHandler, errorHandler);
    }
}
