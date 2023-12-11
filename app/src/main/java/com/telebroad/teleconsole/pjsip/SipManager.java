package com.telebroad.teleconsole.pjsip;

import static android.Manifest.permission.RECORD_AUDIO;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.helpers.SettingsHelper.SIP_USERNAME;
import static com.telebroad.teleconsole.helpers.SettingsHelper.getString;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_CALLERIDD;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_DNUMBER;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_SENDER;
import static com.telebroad.teleconsole.helpers.URLHelper.KEY_SNUMBER;
import static com.telebroad.teleconsole.helpers.URLHelper.POST_SEND_CALL;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.FullPhone;
import com.telebroad.teleconsole.model.PhoneNumber;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yser on 3/13/2018.
 */

public interface SipManager<Call> {

    MutableLiveData<Boolean> isRegistered = new MutableLiveData<>();


    // call this method when you change users
    void updateUser(boolean force);

    void restartSip();
    void updateNetworkReachability();
    @Deprecated
    void hangup();
    @Deprecated
    void mute(boolean on);

    default void call(String destination) {
        call(destination, AppController.getInstance().getActiveActivity());
    }
    void sipCall(String destination, Activity currentActivity);
    default void call(String destination, Activity currentActivity) {

       // android.util.Log.d("Activity01", "" + currentActivity);
        // USE_SIP should be reinstated at some point
        if (!AppController.getInstance().hasPermissions(RECORD_AUDIO/*, USE_SIP*/)) {
            showNoPermissionsDialog(destination);
            return;
        }
        if (!Utils.isConnectedToInternet()) {
            showNoInternetDialog();
            return;
        }

        destination = PhoneNumber.fix(destination);
        if (shouldUseVoip()) {
//            if (LinphoneManager.isRegistered.getValue() != Boolean.TRUE) {
//                retryDeregistered(destination, currentActivity);
//                return;
//            }
            sipCall(destination, currentActivity);
        } else {
            serverCall(destination, currentActivity);
        }
    }

    @Deprecated
    void answer(int id);
    @Deprecated
    void answer(int id, boolean ignoreActive);
    @Deprecated
    void decline(int id);

    void logout(String account, String string);

    default void showNoInternetDialog() {
        Activity activity = AppController.getInstance().getActiveActivity();
        if (activity == null) {
            return;
        }
        AlertDialog alert = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.no_internet)
                .setMessage(R.string.check_internet)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    dialog.dismiss();
                })).create();
        alert.setOnShowListener(dialog -> {
            Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
            positiveButton.setTextColor(activity.getResources().getColor(R.color.black,null));
            negativeButton.setTextColor(activity.getResources().getColor(R.color.black,null));
        });alert.show();
    }

    Call[] getAllCalls();

    default boolean canConference(){
        return canConference(null);
    }

    boolean canConference(Consumer<String> handleError);

    default void showNoPermissionsDialog(String dest) {
        Activity activity = AppController.getInstance().getActiveActivity();
        if (activity == null) {
            return;
        }
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(activity, R.style.DialogStyle)
                .setTitle(R.string.no_mic_perm_title)
                .setMessage(R.string.no_mic_perm_message)
                .setPositiveButton("Give Permission", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:com.telebroad.teleconsole"));
                    activity.startActivity(intent);
                    dialog.dismiss();
                });
        // Uncomment the following lines if you want to include the "Call Anyway" button
        // .setNeutralButton("Call Anyway", (dialog, which) -> {
        //     sipCall(dest, activity);
        //     dialog.dismiss();
        // });

        AlertDialog alert = alertBuilder.create();

        alert.setOnShowListener(dialog -> {
            Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
            positiveButton.setTextColor(activity.getResources().getColor(R.color.black,null));
            negativeButton.setTextColor(activity.getResources().getColor(R.color.black,null));
        });alert.show();
    }

    default void serverCall(String destination, Activity currentActivity) {
        Map<String, String> params = new HashMap<>();
        String devicePhoneNumber = SettingsHelper.getDevicePhoneNumber();
        String reason = "Please enter your device phone number, so we can start the call over your mobile phone network";
        if (isNullOrEmpty(devicePhoneNumber)){
            LayoutInflater layoutInflater = (LayoutInflater) currentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.dialog_device_number, null);

            final EditText input = view.findViewById(R.id.input);
            input.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            AlertDialog serverCallAlert = new MaterialAlertDialogBuilder(currentActivity)
                    .setTitle("Device Phone Number")
                    .setView(view)
                    .setMessage(reason).setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                SettingsHelper.putString(R.string.cell_number_key, PhoneNumber.fix(input.getText().toString()));
                serverCall(destination, currentActivity);
                dialog.dismiss();
            })).create();
            serverCallAlert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(currentActivity.getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(currentActivity.getResources().getColor(R.color.black,null));
            });serverCallAlert.show();
            return;
        }
        params.put(KEY_SNUMBER, PhoneNumber.fix(devicePhoneNumber));
        params.put(KEY_DNUMBER, destination);
        params.put(KEY_SENDER, getString(SIP_USERNAME, ""));
        params.put(KEY_CALLERIDD, FullPhone.getInstance() == null ? "" : FullPhone.getInstance().getCalleridExternal());
       // android.util.Log.d("PostCall01", "params " + params.toString());
        URLHelper.request(Request.Method.POST, POST_SEND_CALL, params, (result) -> {
           // android.util.Log.d("PostCall01", "result " + result.toString());
        }, error -> android.util.Log.d("PostCall01", "error " + error.getFullErrorMessage()));
        if (currentActivity != null) {
            AlertDialog serverCallAlert =  new MaterialAlertDialogBuilder(currentActivity).setTitle(R.string.server_call_title).setMessage((currentActivity.getString(R.string.server_call_message, PhoneNumber.getPhoneNumber(SettingsHelper.getDevicePhoneNumber()).formatted())))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).create();
            serverCallAlert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(currentActivity.getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(currentActivity.getResources().getColor(R.color.black,null));
            });serverCallAlert.show();
        }
    }

    default boolean shouldUseVoip() {
        String voipSetting = SettingsHelper.getString(R.string.voip_key, AppController.getAppString(R.string.voip_always));
        if (voipSetting == null){
            return true;
        }
        if (voipSetting.equals(AppController.getAppString(R.string.voip_always))) {
            return true;
        } else if (voipSetting.equals(AppController.getAppString(R.string.voip_never))) {
            return false;
        } else if (voipSetting.equals(AppController.getAppString(R.string.voip_wifi))) {
            return Utils.getConnectionStatus() == Utils.ConnectionStatus.WIFI;
        }
        return true;
    }

    void setSpeaker(boolean on);

    @Deprecated
    void hold(boolean on);

    @Deprecated
    void hold(int id, boolean on);

    @Deprecated
    void record(boolean on);

    void setCallQuality(CallQuality quality);

    @Deprecated
    void transfer(String to);

    void conference();

    void conference(String with);

    SpeakerState getSepakerState();
    @Deprecated
    void sendDTMF(String dtmf);

    @Deprecated
    void decline();
    @Deprecated
    default void decline(String callID){
        decline(getCall(callID));
    }
    @Deprecated
    void decline(Call call);

    @Deprecated
    void accept();

    void accept(Call call);

    void destroy();

    default void accept(String callID){
        accept(getCall(callID));
    }

    boolean isDND();

    Call getCall(String id);

    @Nullable
    Call getOtherCall();

    Call getActiveCall();

    int getCallCount();

    boolean isInConference();

    String getStateDisplayName(Call call);

    int getLongestDurationInConference();

    LiveData<UIState> getUIState();

    void swap();

    default void log(Object object) {
       // android.util.Log.d("SipManager", "Logging " + object.toString());
    }

    default void split(String callIDtoKeep){
        if (callIDtoKeep == null){
            split((Call) null);
        }
        split(getCall(callIDtoKeep));
    }
    default void split(Integer callIDtoKeep){
        if (callIDtoKeep == null){
            split((Call) null);
        }else {
            split((Call) CallManager.getInstance().getCall(callIDtoKeep));
        }
    }

    void split(Call callToKeep);

    static SipManager<?> getInstance(Context context) {
        return PJSIPManager.getInstance();
    }

    static SipManager<?> getInstance() {
        return getInstance(AppController.getInstance());
    }

    void deregister();

    void conference(int thisCall, String phoneNumber);

    enum UIState {
        SINGLE, CONFERENCE, MULTI, FINISHED
    }

    enum CallQuality {
        HIGH, MEDIUM, LOW, ILBC
    }

}
