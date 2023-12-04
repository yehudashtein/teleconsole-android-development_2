
package com.telebroad.teleconsole.model;

import androidx.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.pjsip.SipManager;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.PUT;
import static com.telebroad.teleconsole.helpers.SettingsHelper.FULL_PHONE;
import static com.telebroad.teleconsole.helpers.SettingsHelper.SIP_DOMAIN;
import static com.telebroad.teleconsole.helpers.SettingsHelper.SIP_PASSWORD;
import static com.telebroad.teleconsole.helpers.SettingsHelper.SIP_USERNAME;
import static com.telebroad.teleconsole.helpers.SettingsHelper.getString;
import static com.telebroad.teleconsole.helpers.SettingsHelper.putString;
import static com.telebroad.teleconsole.helpers.URLHelper.GET_PHONE;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_PHONE;
import static com.telebroad.teleconsole.helpers.URLHelper.request;
import static com.telebroad.teleconsole.helpers.Utils.updateLiveData;

public class FullPhone implements Parcelable {

    private static MutableLiveData<FullPhone> instance = new MutableLiveData<>();

    protected FullPhone(Parcel in) {
        codec = in.readString();
        bnumber = in.readString();
        delivery = in.readString();
        emergencyAddressCode = in.readString();
        unumberGroup = in.readString();
        utype = in.readString();
        dtype = in.readString();
        dialplan = in.readString();
        dnumber = in.readString();
        plan = in.readString();
        provision1 = in.readString();
        btype = in.readString();
        name = in.readString();
        description = in.readString();
        emergencyAddress2 = in.readString();
        ltype = in.readString();
        emergencyAddress3 = in.readString();
        ringtone = in.readString();
        ping = in.readString();
        emergencyLocationCode = in.readString();
        source = in.readString();
        lastcaller = in.readString();
        calleridLocation = in.readString();
        panel = in.readString();
        missedemail = in.readString();
        hardwareAddress = in.readString();
        language = in.readString();
        dnd = in.readString();
        screen = in.readString();
        presentationExternal = in.readString();
        mailbox = in.readString();
        recordgroup = in.readString();
        callBalance = in.readString();
        lnumber = in.readString();
        pin = in.readString();
        forwarding = in.readString();
        calleridExternal = in.readString();
        emergencyAddressOther = in.readString();
        display = in.readString();
        lastcalled = in.readString();
        media = in.readString();
        lastcallertime = in.readString();
        music = in.readString();
        lastprovisionTime = in.readString();
        callMaximum = in.readString();
        provisioning = in.readString();
        callernameExternal = in.readString();
        registrar = in.readString();
        expectRegistered = in.readString();
        ringtime = in.readString();
        unumber = in.readString();
        direct = in.readString();
        lphone = in.readString();
        emergencyAddress1 = in.readString();
        calleridInternal = in.readString();
        emergencyAddressCity = in.readString();
        cos = in.readString();
        emergencyUpdated = in.readString();
        musicRinging = in.readString();
        callernameInternal = in.readString();
        owner = in.readString();
        acr = in.readString();
        failedCount = in.readString();
        lastprovisionFrom = in.readString();
        totalmaximum = in.readString();
        emergencyCountry = in.readString();
        lastcalledtime = in.readString();
        emergencyAddressState = in.readString();
        customer = in.readString();
    }

    public FullPhone() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(codec);
        dest.writeString(bnumber);
        dest.writeString(delivery);
        dest.writeString(emergencyAddressCode);
        dest.writeString(unumberGroup);
        dest.writeString(utype);
        dest.writeString(dtype);
        dest.writeString(dialplan);
        dest.writeString(dnumber);
        dest.writeString(plan);
        dest.writeString(provision1);
        dest.writeString(btype);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(emergencyAddress2);
        dest.writeString(ltype);
        dest.writeString(emergencyAddress3);
        dest.writeString(ringtone);
        dest.writeString(ping);
        dest.writeString(emergencyLocationCode);
        dest.writeString(source);
        dest.writeString(lastcaller);
        dest.writeString(calleridLocation);
        dest.writeString(panel);
        dest.writeString(missedemail);
        dest.writeString(hardwareAddress);
        dest.writeString(language);
        dest.writeString(dnd);
        dest.writeString(screen);
        dest.writeString(presentationExternal);
        dest.writeString(mailbox);
        dest.writeString(recordgroup);
        dest.writeString(callBalance);
        dest.writeString(lnumber);
        dest.writeString(pin);
        dest.writeString(forwarding);
        dest.writeString(calleridExternal);
        dest.writeString(emergencyAddressOther);
        dest.writeString(display);
        dest.writeString(lastcalled);
        dest.writeString(media);
        dest.writeString(lastcallertime);
        dest.writeString(music);
        dest.writeString(lastprovisionTime);
        dest.writeString(callMaximum);
        dest.writeString(provisioning);
        dest.writeString(callernameExternal);
        dest.writeString(registrar);
        dest.writeString(expectRegistered);
        dest.writeString(ringtime);
        dest.writeString(unumber);
        dest.writeString(direct);
        dest.writeString(lphone);
        dest.writeString(emergencyAddress1);
        dest.writeString(calleridInternal);
        dest.writeString(emergencyAddressCity);
        dest.writeString(cos);
        dest.writeString(emergencyUpdated);
        dest.writeString(musicRinging);
        dest.writeString(callernameInternal);
        dest.writeString(owner);
        dest.writeString(acr);
        dest.writeString(failedCount);
        dest.writeString(lastprovisionFrom);
        dest.writeString(totalmaximum);
        dest.writeString(emergencyCountry);
        dest.writeString(lastcalledtime);
        dest.writeString(emergencyAddressState);
        dest.writeString(customer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FullPhone> CREATOR = new Creator<FullPhone>() {
        @Override
        public FullPhone createFromParcel(Parcel in) {
            return new FullPhone(in);
        }

        @Override
        public FullPhone[] newArray(int size) {
            return new FullPhone[size];
        }
    };

    public static void setupPhone(Line.PhoneLine phoneLine) {

        String oldUsername = getString(SIP_USERNAME, "");
        String oldPassword = getString(SIP_PASSWORD, "");
        String oldDomain = getString(SIP_DOMAIN, "");
        putString(SIP_USERNAME, phoneLine.getName());
        putString(SIP_PASSWORD, phoneLine.secret);
        putString(SIP_DOMAIN, phoneLine.getAppServer());
        // If any of these things have changed, we need to restart SIP with the correct credentials
        if (!oldUsername.equals(phoneLine.getName()) || !oldPassword.equals(phoneLine.secret) || !oldDomain.equals(phoneLine.getAppServer()) ){
            SipManager.getInstance().restartSip();
        }else{
            SipManager.getInstance().updateUser(true);
        }
        //android.util.Log.d("SIPDOMAIN", "setting domain to " + phoneLine.getAppServer());
        fetchPhone(phoneLine);
    }

    public static void fetchPhone(Line.PhoneLine phoneLine) {
        //Log.d("Phone", "Fetching");
        HashMap<String, String> params = new HashMap<>();
        params.put(KEY_PHONE, phoneLine.getName());
        request(Request.Method.GET, GET_PHONE, params, (result) -> {
            setInstance(new Gson().fromJson(result, FullPhone.class));
           // Log.d("CallerID", "Fetched " + instance.getValue());
        }, (error) -> Log.e("Phone", "Error " + error.getFullErrorMessage()));
    }

    public static MutableLiveData<FullPhone> getLiveInstance() {
        return instance;
    }

    public static FullPhone getInstance() {
        if (instance.getValue() == null) {
            return new Gson().fromJson(SettingsHelper.getString(FULL_PHONE), FullPhone.class);
        }
        return instance.getValue();
    }

    public static void setInstance(FullPhone phone) {
        SettingsHelper.putString(SettingsHelper.FULL_PHONE, new Gson().toJson(phone));
        updateLiveData(instance, phone);
//        if (isMainThread()){
//            instance.setValue(phone);
//        }else {
//            instance.postValue(phone);
//        }
    }

    public void updatePhone() {
        AsyncTask.execute(() -> {
            setInstance(this);
            String phoneJson = new Gson().toJson(this);
            TypeToken<Map<String, String>> mapTypeToken = new TypeToken<Map<String, String>>() {
            };
            Map<String, String> params = new Gson().fromJson(phoneJson, mapTypeToken.getType());
            //android.util.Log.d("UpdatePhone", "json " + phoneJson + " toString " + toString());
            params.put(URLHelper.KEY_PHONE, getName());
            request(PUT, GET_PHONE, params, result ->
                            android.util.Log.d("PhoneResult", result.toString())
                    , error -> android.util.Log.d("PhoneResult", " Error " + error.getFullErrorMessage() + " params " + params + " json form " + phoneJson));
        });
    }

    //<editor-fold desc="Variable declarations">
    private String codec;
    private String bnumber;
    private String delivery;
    @SerializedName("emergency_address_code")
    private String emergencyAddressCode;
    @SerializedName("unumber_group")
    private String unumberGroup;
    private String utype;
    private String dtype;
    private String dialplan;
    private String dnumber;
    private String plan;
    private String provision1;
    private String btype;
    private String name;
    private String description;
    @SerializedName("emergency_address2")
    private String emergencyAddress2;
    private String ltype;
    @SerializedName("emergency_address3")
    private String emergencyAddress3;
    private String ringtone;
    private String ping;
    @SerializedName("emergency_location_code")
    private String emergencyLocationCode;
    private String source;
    private String lastcaller;
    @SerializedName("callerid_location")
    private String calleridLocation;
    private String panel;
    private String missedemail;
    @SerializedName("hardware_address")
    private String hardwareAddress;
    private String language;
    private String dnd;
    private String screen;
    @SerializedName("presentation_external")
    private String presentationExternal;
    private String mailbox;
    private String recordgroup;
    private String callBalance;
    private String lnumber;
    private String pin;
    private String forwarding;
    @SerializedName(value = "callerid", alternate = "callerid_external")
    private String calleridExternal;
    @SerializedName("emergency_address_other")
    private String emergencyAddressOther;
    private String display;
    private String lastcalled;
    private String media;
    private String lastcallertime;
    private String music;
    @SerializedName("lastprovision_time")
    private String lastprovisionTime;
    @SerializedName("call_maximum")
    private String callMaximum;
    private String provisioning;
    @SerializedName("callername_external")
    private String callernameExternal;
    private String registrar;
    @SerializedName("expect_registered")
    private String expectRegistered;
    private String ringtime;
    private String unumber;
    private String direct;
    private String lphone;
    @SerializedName("emergency_address1")
    private String emergencyAddress1;
    @SerializedName("callerid_internal")
    private String calleridInternal;
    @SerializedName("emergency_address_city")
    private String emergencyAddressCity;
    private String cos;
    @SerializedName("emergency_updated")
    private String emergencyUpdated;
    @SerializedName("music_ringing")
    private String musicRinging;
    private String callernameInternal;
    private String owner;
    private String acr;
    @SerializedName("failed_count")
    private String failedCount;
    @SerializedName("lastprovision_from")
    private String lastprovisionFrom;
    private String totalmaximum;
    @SerializedName("emergency_country")
    private String emergencyCountry;
    private String lastcalledtime;
    @SerializedName("emergency_address_state")
    private String emergencyAddressState;
    private String customer;
    @SerializedName("user_agent")
    private Object userAgent;
    //</editor-fold>

    //<editor-fold desc="getters and setters">
    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public String getBnumber() {
        return bnumber;
    }

    public void setBnumber(String bnumber) {
        this.bnumber = bnumber;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public String getEmergencyAddressCode() {
        return emergencyAddressCode;
    }

    public void setEmergencyAddressCode(String emergencyAddressCode) {
        this.emergencyAddressCode = emergencyAddressCode;
    }

    public String getUnumberGroup() {
        return unumberGroup;
    }

    public void setUnumberGroup(String unumberGroup) {
        this.unumberGroup = unumberGroup;
    }

    public String getUtype() {
        return utype;
    }

    public void setUtype(String utype) {
        this.utype = utype;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public String getDialplan() {
        return dialplan;
    }

    public void setDialplan(String dialplan) {
        this.dialplan = dialplan;
    }

    public String getDnumber() {
        return dnumber;
    }

    public void setDnumber(String dnumber) {
        this.dnumber = dnumber;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getProvision1() {
        return provision1;
    }

    public void setProvision1(String provision1) {
        this.provision1 = provision1;
    }

    public String getBtype() {
        return btype;
    }

    public void setBtype(String btype) {
        this.btype = btype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmergencyAddress2() {
        return emergencyAddress2;
    }

    public void setEmergencyAddress2(String emergencyAddress2) {
        this.emergencyAddress2 = emergencyAddress2;
    }

    public String getLtype() {
        return ltype;
    }

    public void setLtype(String ltype) {
        this.ltype = ltype;
    }

    public String getEmergencyAddress3() {
        return emergencyAddress3;
    }

    public void setEmergencyAddress3(String emergencyAddress3) {
        this.emergencyAddress3 = emergencyAddress3;
    }

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public String getPing() {
        return ping;
    }

    public void setPing(String ping) {
        this.ping = ping;
    }

    public String getEmergencyLocationCode() {
        return emergencyLocationCode;
    }

    public void setEmergencyLocationCode(String emergencyLocationCode) {
        this.emergencyLocationCode = emergencyLocationCode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLastcaller() {
        return lastcaller;
    }

    public void setLastcaller(String lastcaller) {
        this.lastcaller = lastcaller;
    }

    public String getCalleridLocation() {
        return calleridLocation;
    }

    public void setCalleridLocation(String calleridLocation) {
        this.calleridLocation = calleridLocation;
    }

    public String getPanel() {
        return panel;
    }

    public void setPanel(String panel) {
        this.panel = panel;
    }

    public String getMissedemail() {
        return missedemail;
    }

    public void setMissedemail(String missedemail) {
        this.missedemail = missedemail;
    }

    public String getHardwareAddress() {
        return hardwareAddress;
    }

    public void setHardwareAddress(String hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDnd() {
        return dnd;
    }

    public void setDnd(String dnd) {
        this.dnd = dnd;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getPresentationExternal() {
        return presentationExternal;
    }

    public void setPresentationExternal(String presentationExternal) {
        this.presentationExternal = presentationExternal;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

    public String getRecordgroup() {
        return recordgroup;
    }

    public void setRecordgroup(String recordgroup) {
        this.recordgroup = recordgroup;
    }

    public String getCallBalance() {
        return callBalance;
    }

    public void setCallBalance(String callBalance) {
        this.callBalance = callBalance;
    }

    public String getLnumber() {
        return lnumber;
    }

    public void setLnumber(String lnumber) {
        this.lnumber = lnumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getForwarding() {
        return forwarding;
    }

    public void setForwarding(String forwarding) {
        this.forwarding = forwarding;
    }

    public String getCallerIDExtRaw() {
        return calleridExternal;
    }

    public String getCalleridExternal() {
        if (calleridExternal == null || calleridExternal.isEmpty()) {
            return AppController.getInstance().getString(R.string.unknown);
        } else if (calleridExternal.equals("default") && TeleConsoleProfile.getInstance().getPbxCustomer() != null) {
            return TeleConsoleProfile.getInstance().getPbxCustomer().getCallerid();
        }
        return calleridExternal;
    }

    public void setCalleridExternal(String calleridExternal) {
        this.calleridExternal = calleridExternal;
    }

    public String getEmergencyAddressOther() {
        return emergencyAddressOther;
    }

    public void setEmergencyAddressOther(String emergencyAddressOther) {
        this.emergencyAddressOther = emergencyAddressOther;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getLastcalled() {
        return lastcalled;
    }

    public void setLastcalled(String lastcalled) {
        this.lastcalled = lastcalled;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getLastcallertime() {
        return lastcallertime;
    }

    public void setLastcallertime(String lastcallertime) {
        this.lastcallertime = lastcallertime;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getLastprovisionTime() {
        return lastprovisionTime;
    }

    public void setLastprovisionTime(String lastprovisionTime) {
        this.lastprovisionTime = lastprovisionTime;
    }

    public String getCallMaximum() {
        return callMaximum;
    }

    public void setCallMaximum(String callMaximum) {
        this.callMaximum = callMaximum;
    }

    public String getProvisioning() {
        return provisioning;
    }

    public void setProvisioning(String provisioning) {
        this.provisioning = provisioning;
    }

    public String getCallernameExternal() {
        return callernameExternal;
    }

    public void setCallernameExternal(String callernameExternal) {
        this.callernameExternal = callernameExternal;
    }

    public String getRegistrar() {
        return registrar;
    }

    public void setRegistrar(String registrar) {
        this.registrar = registrar;
    }

    public String getExpectRegistered() {
        return expectRegistered;
    }

    public void setExpectRegistered(String expectRegistered) {
        this.expectRegistered = expectRegistered;
    }

    public String getRingtime() {
        return ringtime;
    }

    public void setRingtime(String ringtime) {
        this.ringtime = ringtime;
    }

    public String getUnumber() {
        return unumber;
    }

    public void setUnumber(String unumber) {
        this.unumber = unumber;
    }

    public String getDirect() {
        return direct;
    }

    public void setDirect(String direct) {
        this.direct = direct;
    }

    public String getLphone() {
        return lphone;
    }

    public void setLphone(String lphone) {
        this.lphone = lphone;
    }

    public String getEmergencyAddress1() {
        return emergencyAddress1;
    }

    public void setEmergencyAddress1(String emergencyAddress1) {
        this.emergencyAddress1 = emergencyAddress1;
    }

    public String getCalleridInternal() {
        return calleridInternal;
    }

    public void setCalleridInternal(String calleridInternal) {
        this.calleridInternal = calleridInternal;
    }

    public String getEmergencyAddressCity() {
        return emergencyAddressCity;
    }

    public void setEmergencyAddressCity(String emergencyAddressCity) {
        this.emergencyAddressCity = emergencyAddressCity;
    }

    public String getCos() {
        return cos;
    }

    public void setCos(String cos) {
        this.cos = cos;
    }

    public String getEmergencyUpdated() {
        return emergencyUpdated;
    }

    public void setEmergencyUpdated(String emergencyUpdated) {
        this.emergencyUpdated = emergencyUpdated;
    }

    public String getMusicRinging() {
        return musicRinging;
    }

    public void setMusicRinging(String musicRinging) {
        this.musicRinging = musicRinging;
    }

    public String getCallernameInternal() {
        return callernameInternal;
    }

    public void setCallernameInternal(String callernameInternal) {
        this.callernameInternal = callernameInternal;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAcr() {
        return acr;
    }

    public void setAcr(String acr) {
        this.acr = acr;
    }

    public String getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(String failedCount) {
        this.failedCount = failedCount;
    }

    public String getLastprovisionFrom() {
        return lastprovisionFrom;
    }

    public void setLastprovisionFrom(String lastprovisionFrom) {
        this.lastprovisionFrom = lastprovisionFrom;
    }

    public String getTotalmaximum() {
        return totalmaximum;
    }

    public void setTotalmaximum(String totalmaximum) {
        this.totalmaximum = totalmaximum;
    }

    public String getEmergencyCountry() {
        return emergencyCountry;
    }

    public void setEmergencyCountry(String emergencyCountry) {
        this.emergencyCountry = emergencyCountry;
    }

    public String getLastcalledtime() {
        return lastcalledtime;
    }

    public void setLastcalledtime(String lastcalledtime) {
        this.lastcalledtime = lastcalledtime;
    }

    public String getEmergencyAddressState() {
        return emergencyAddressState;
    }

    public void setEmergencyAddressState(String emergencyAddressState) {
        this.emergencyAddressState = emergencyAddressState;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Object getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(Object userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "FullPhone{" +
                "\n codec=" + codec +
                "\n bnumber=" + bnumber +
                "\n delivery=" + delivery +
                "\n emergencyAddressCode=" + emergencyAddressCode +
                "\n unumberGroup=" + unumberGroup +
                "\n utype=" + utype +
                "\n dtype=" + dtype +
                "\n dialplan=" + dialplan +
                "\n dnumber=" + dnumber +
                "\n plan=" + plan +
                "\n provision1=" + provision1 +
                "\n btype=" + btype +
                "\n name=" + name +
                "\n description=" + description +
                "\n emergencyAddress2=" + emergencyAddress2 +
                "\n ltype=" + ltype +
                "\n emergencyAddress3=" + emergencyAddress3 +
                "\n ringtone=" + ringtone +
                "\n ping=" + ping +
                "\n emergencyLocationCode=" + emergencyLocationCode +
                "\n source=" + source +
                "\n lastcaller=" + lastcaller +
                "\n calleridLocation=" + calleridLocation +
                "\n panel=" + panel +
                "\n missedemail=" + missedemail +
                "\n hardwareAddress=" + hardwareAddress +
                "\n language=" + language +
                "\n dnd=" + dnd +
                "\n screen=" + screen +
                "\n presentationExternal=" + presentationExternal +
                "\n mailbox=" + mailbox +
                "\n recordgroup=" + recordgroup +
                "\n callBalance=" + callBalance +
                "\n lnumber=" + lnumber +
                "\n pin=" + pin +
                "\n forwarding=" + forwarding +
                "\n calleridExternal=" + calleridExternal +
                "\n emergencyAddressOther=" + emergencyAddressOther +
                "\n display=" + display +
                "\n lastcalled=" + lastcalled +
                "\n media=" + media +
                "\n lastcallertime=" + lastcallertime +
                "\n music=" + music +
                "\n lastprovisionTime=" + lastprovisionTime +
                "\n callMaximum=" + callMaximum +
                "\n provisioning=" + provisioning +
                "\n callernameExternal=" + callernameExternal +
                "\n registrar=" + registrar +
                "\n expectRegistered=" + expectRegistered +
                "\n ringtime=" + ringtime +
                "\n unumber=" + unumber +
                "\n direct=" + direct +
                "\n lphone=" + lphone +
                "\n emergencyAddress1=" + emergencyAddress1 +
                "\n calleridInternal=" + calleridInternal +
                "\n emergencyAddressCity=" + emergencyAddressCity +
                "\n cos=" + cos +
                "\n emergencyUpdated=" + emergencyUpdated +
                "\n musicRinging=" + musicRinging +
                "\n callernameInternal=" + callernameInternal +
                "\n owner=" + owner +
                "\n acr=" + acr +
                "\n failedCount=" + failedCount +
                "\n lastprovisionFrom=" + lastprovisionFrom +
                "\n totalmaximum=" + totalmaximum +
                "\n emergencyCountry=" + emergencyCountry +
                "\n lastcalledtime=" + lastcalledtime +
                "\n emergencyAddressState=" + emergencyAddressState +
                "\n customer=" + customer +
                "\n userAgent=" + userAgent +
                '}';
    }
    //</editor-fold>
}
