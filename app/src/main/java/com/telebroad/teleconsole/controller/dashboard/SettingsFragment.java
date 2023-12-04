package com.telebroad.teleconsole.controller.dashboard;

import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.lifecycle.LiveData;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.SwitchPreference;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.ListMessageDialogFragment;
import com.telebroad.teleconsole.helpers.ListMessagePreference;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.FullPhone;
import com.telebroad.teleconsole.model.Line;
import com.telebroad.teleconsole.model.Number;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.PubnubInfo;
import com.telebroad.teleconsole.model.Settings;
import com.telebroad.teleconsole.model.TeleConsoleProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static com.telebroad.teleconsole.helpers.SettingsHelper.SIP_PASSWORD;
import static com.telebroad.teleconsole.helpers.SettingsHelper.SIP_USERNAME;
import static com.telebroad.teleconsole.helpers.SettingsHelper.USE_TLS;
import static com.telebroad.teleconsole.helpers.SettingsHelper.putBoolean;


public class SettingsFragment extends PreferenceFragmentCompat implements ScrollableFragment {

    public SettingsFragment() {
    }

    private Settings settings;
    private TeleConsoleProfile profile;
    private FullPhone phone;
    private List<Number> ownedPhoneNumbers;
    private View syncMenuView;
    private final LiveData<Settings> liveSettings = Settings.getLiveInstance();

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //android.util.Log.d("SettingsFragment", "onCreate");
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        //android.util.Log.d("SettingsFragment", "onCreatePreferences");
        liveSettings.observe(this, settings -> {
            this.settings = settings;
            updateSettings();
        });
        TeleConsoleProfile.getLiveInstance().observe(this, profile -> {
            this.profile = profile;
            updateSettings();
        });
        EditTextPreference cellNumberPref = findPreference(getString(R.string.cell_number_key));
        setupTLSswitch();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
       // android.util.Log.d("SettingsFragment", "onDisplayPreferenceDialog " + preference);
        if (preference instanceof ListMessagePreference && getFragmentManager() != null) {
            DialogFragment dialogFragment = ListMessageDialogFragment.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            return;
        }
        super.onDisplayPreferenceDialog(preference);
    }
    private void initializeSettings() {
        liveSettings.observe(this, settings -> {
            this.settings = settings;
            updateSettings();
        });
    }


    private void updateSettings() {
        android.util.Log.d("SettingsFragment", "updateSettings");
        if (profile == null || settings == null) {
//            android.util.Log.d("SettingsFragmentProfile", profile + "");
//            android.util.Log.d("SettingsFragmentProfileSettings", settings+"");
////            android.util.Log.d("SettingsFragmentProfilePubnubInfo", PubnubInfo.getInstance()+"");
//            android.util.Log.d("SettingsFragmentProfile", "return");
            //initializeSettings();
            return;
        }
        setChannelPref("voicemail", settings.getVoicemails(),
                profile.getVoxBoxes(),
                settings::setVoicemails);
        setChannelPref("fax", settings.getFaxLines(), profile.getFaxBoxes(), settings::setFaxLines);
        setChannelPref("sms", settings.getSmsLines(), profile.getSmsLines(), settings::setSmsLines);
        profile.fetchNumbers();
        AsyncTask.execute(() -> {
            if (getActivity() == null) {
                return;
            }
            String[] smsArray = Line.convertLineListToStringList(profile.getSmsLines()).toArray(new String[]{});
            getActivity().runOnUiThread(() -> {
                setupDefaultSMSLine(smsArray);
                FullPhone.getLiveInstance().observe(this, (fullPhone -> {
                    phone = fullPhone;
                    setUpCallerIDPref();
                    setupPhoneLinePref();
                    setupForwardingPref();
                }));
            });
        });
        profile.getLiveOwnedPhoneNumber().observe(this, phoneNumbers -> {
            this.ownedPhoneNumbers = phoneNumbers;
            setUpCallerIDPref();
        });
        ListPreference callQualityPreference = findPreference("quality");
        callQualityPreference.setOnPreferenceChangeListener(((preference, newValue) -> {
            SipManager.CallQuality callQuality;
            try {
                callQuality = SipManager.CallQuality.valueOf(newValue.toString());
            } catch (Exception e) {
                callQuality = SipManager.CallQuality.MEDIUM;
            }
           // android.util.Log.d("Settings", newValue.toString());
            SipManager.getInstance().setCallQuality(callQuality);
            Toast.makeText(getContext(), R.string.call_quality_set_message, Toast.LENGTH_LONG).show();
            return true;
        }));
    }

    private void setupDefaultSMSLine(String[] smsArray) {
        //android.util.Log.d("SettingsFragment", "setupDefaultSMSLine");
        ListPreference defaultSMSPref = findPreference("defaultSMS");
        defaultSMSPref.setEntries(smsArray);
        defaultSMSPref.setEntryValues(smsArray);
        if (settings.getDefaultSMSLine() != null) {
            defaultSMSPref.setValue(PhoneNumber.getPhoneNumber(settings.getDefaultSMSLine()).formatted());
        }
        defaultSMSPref.setOnPreferenceChangeListener((preference, newValue) -> {
            settings.setDefaultSMSLine(newValue.toString());
            return true;
        });
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void setupForwardingPref() {
       // android.util.Log.d("SettingsFragment", "setupForwardingPref");
        if (getContext() == null) {
            return;
        }
        SwitchPreference forward = findPreference("forward");
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (phone.getForwarding() == null || phone.getForwarding().isEmpty() || phone.getForwarding().equalsIgnoreCase("null")) {
                    forward.setChecked(false);
                    forward.setSummary(R.string.forward_summary_negative);
                } else {
                    forward.setChecked(true);
                    forward.setSummary(getString(R.string.forward_summary_positive, PhoneNumber.getPhoneNumber(phone.getForwarding()).formatted()));
                }
            });
        }
        forward.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof Boolean) {
                boolean on = (Boolean) newValue;
                if (on) {
                    showForwardDialog(forward);
                } else {
                    // We only need to update if it was checked to begin with, and not if it was a canceled change
                    if (forward.isChecked()) {
                        forward.setSummary(R.string.forward_summary_negative);
                        forward.setChecked(false);
                        phone.setForwarding("");
                        phone.updatePhone();
                    }
                }
            }
            return false;
        });
    }

    private void showForwardDialog(SwitchPreference forward) {
       // android.util.Log.d("SettingsFragment", "showForwardDialog");
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getContext());
        final EditText input = new EditText(getContext());
        input.setSingleLine();
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        String savedFwNumber = settings.getFwPhoneNumbers().get(SettingsHelper.getString(SIP_USERNAME));
        if (savedFwNumber == null || savedFwNumber.isEmpty()) {
            if (AppController.getInstance().hasPermissions(READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE)) {
                savedFwNumber = SettingsHelper.getDevicePhoneNumber();
                if (savedFwNumber.replaceAll("[/?0]", "").isEmpty()) {
                    input.setHint(R.string.phone_number);
                } else {
                    input.setHint(savedFwNumber);
                }
            }
        } else {
            input.setHint(savedFwNumber);
        }
        input.setText("");
        FrameLayout container = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.text_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.text_margin);
        input.setLayoutParams(params);
        container.addView(input);
        alert.setView(container);
        alert.setTitle(R.string.forward_to);
        alert.setNegativeButton(android.R.string.cancel, ((dialog, which) -> {
            forward.setChecked(false);
            dialog.dismiss();
        }));
        alert.setPositiveButton(android.R.string.ok, ((dialog, which) -> {
           // android.util.Log.d("TimeTest0001", "Started");
            String rawNumber = input.getText() == null ? "" : input.getText().toString();
            String hint = input.getHint() == null ? "" : input.getHint().toString();
            if (rawNumber.isEmpty()) {
                if (!hint.equalsIgnoreCase(getString(R.string.phone_number))) {
                    rawNumber = hint;
                } else {
                    dialog.dismiss();
                    return;
                }
            }
            forward.setChecked(true);
            forward.setSummary(getContext().getString(R.string.forward_summary_positive, PhoneNumber.getPhoneNumber(rawNumber).formatted()));
            settings.setForwardingNumber(SettingsHelper.getString(SIP_USERNAME), PhoneNumber.getPhoneNumber(rawNumber).fixed());
            phone.setForwarding(PhoneNumber.getPhoneNumber(rawNumber).fixed());
            phone.updatePhone();
            input.clearFocus();
            dialog.dismiss();
            //android.util.Log.d("SettingsFragment", "Finished");
        }));
        androidx.appcompat.app.AlertDialog dialog = alert.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = ((androidx.appcompat.app.AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.black, null));
            negativeButton.setTextColor(getResources().getColor(R.color.black, null));
        });
        alert.show();
    }

    private void showTLSDialog(SwitchPreferenceCompat tlsSwitch) {
        //android.util.Log.d("SettingsFragment", "showTLSDialog" + tlsSwitch);
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getContext());
        alert.setTitle("Turn Off TLS");
        alert.setMessage(R.string.use_tls_warning);
        alert.setNegativeButton(android.R.string.cancel, ((dialog, which) -> {
            dialog.dismiss();
        }));
        alert.setPositiveButton(R.string.tls_button_title, ((dialog, which) -> {
            updateTLS(tlsSwitch, false);
            dialog.dismiss();
        }));
        androidx.appcompat.app.AlertDialog dialog = alert.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = ((androidx.appcompat.app.AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.black, null));
            negativeButton.setTextColor(getResources().getColor(R.color.black, null));
        });
        alert.show();
        SipManager.getInstance().restartSip();
    }

    private void setupPhoneLinePref() {
        //android.util.Log.d("SettingsFragment", "setupPhoneLinePref");
        if (getActivity() == null) {
            return;
        }
        ListPreference phoneLinePref = findPreference("phone_line");
        if (profile.getPhones().size() <= 1) {
            getActivity().runOnUiThread(() -> phoneLinePref.setVisible(false));
        } else {
            List<String> phoneListStrings = Line.convertLineListToStringList(profile.getPhones());
            List<String> phoneListFormatted = Line.PhoneLine.getFormattedPhoneLines(profile.getPhones());
            getActivity().runOnUiThread(() -> {
                phoneLinePref.setEntryValues(phoneListStrings.toArray(new String[]{}));
                phoneLinePref.setEntries(phoneListFormatted.toArray(new String[]{}));
                phoneLinePref.setValue(PhoneNumber.getPhoneNumber(settings.getPhoneLine().getName()).formatted());
            });
            phoneLinePref.setOnPreferenceChangeListener((preference, newValue) -> {
                //noinspection SuspiciousMethodCalls
                Line.PhoneLine newPhoneLine = profile.getPhones().get(profile.getPhones().indexOf(new Line(PhoneNumber.getPhoneNumber(newValue.toString()).fixed())));
                if (newPhoneLine.equals(settings.getPhoneLine())) {
                    // Everything is the same it is time to say goodbye
                    return false;
                }
                PubnubInfo.getInstance().unSubscribeToChannels(settings.getPhoneLine());
                PubnubInfo.getInstance().subscribeToChannels(newPhoneLine);
                settings.setPhoneLine(newPhoneLine);
                // Set the username and password
                SettingsHelper.putString(SIP_USERNAME, newPhoneLine.getName());
                SettingsHelper.putString(SIP_PASSWORD, newPhoneLine.getSecret());
                // Tell the manger to update his user
                SipManager.getInstance().updateUser(true);
                // Update the phone
                FullPhone.fetchPhone(newPhoneLine);
                return false;
            });
        }
    }

    private void setUpCallerIDPref() {
       // android.util.Log.d("SettingsFragment", "setUpCallerIDPref");
        ListPreference callerIDpreference = findPreference("CallerID");
        if (profile == null || phone == null || ownedPhoneNumbers == null) {
            //android.util.Log.d("SettingsFragment", "profile is null snd phone == null ownedPhoneNumbers == null");
            return;
        }
        if (profile.getOwnedPhoneNumber() == null || profile.getOwnedPhoneNumber().isEmpty()) {
            //android.util.Log.d("SettingsFragment", "profile isEmpty");
            setListPreferenceEmpty(callerIDpreference);
        } else {
            AsyncTask.execute(() -> {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                List<PhoneNumber> phoneNumbers = PhoneNumber.convertNumberListToPhoneNumbers(profile.getOwnedPhoneNumber());
//                for (PhoneNumber p : phoneNumbers) {
//                    android.util.Log.d("SettingsFragment", p.formatted());
//                }
                List<String> entries = new ArrayList<>(PhoneNumber.convertPhoneNumberListToFormatted(phoneNumbers));
                List<String> entryValues = /*new ArrayList<>(PhoneNumber.convertPhoneNumberListToFixed(phoneNumbers));
                entryValues = */ profile.getOwnedPhoneNumbersAsStrings();
                entries.add(0, "Blocked");
                entries.add(0, "Default Caller ID");
                entryValues.add(0, "");
                entryValues.add(0, "default");
                activity.runOnUiThread(() -> {
                    callerIDpreference.setEntryValues(entryValues.toArray(new String[]{}));
                    callerIDpreference.setEntries(entries.toArray(new String[]{}));
                    //android.util.Log.d("Settings", PhoneNumber.getPhoneNumber(phone.getCallerIDExtRaw()).formatted());
                    callerIDpreference.setValue(phone.getCallerIDExtRaw());
                    callerIDpreference.setOnPreferenceChangeListener((preference, newValue) -> {
                        //android.util.Log.d("SettingsFragment", "Setting to " + newValue.toString());
                        phone.setCalleridExternal(newValue.toString());
                        phone.updatePhone();
                        return false;
                    });
                });
            });
        }
    }

    private void setListPreferenceEmpty(MultiSelectListPreference voicemailPreference) {
       // android.util.Log.d("SettingsFragment", "setListPreferenceEmpty");
        voicemailPreference.setEntryValues(new String[]{});
        voicemailPreference.setEntries(new String[]{});
    }

    private void setListPreferenceEmpty(ListPreference voicemailPreference) {
       // android.util.Log.d("SettingsFragment", "setListPreferenceEmpty");
        voicemailPreference.setEntryValues(new String[]{});
        voicemailPreference.setEntries(new String[]{});
    }

    private void setChannelPref(String key, List<Line> selectedLines, List<Line> allLines, OnValueChanged<Line> onLineChanged) {
       // android.util.Log.d("SettingsFragment", "setChannelPref");
        MultiSelectListPreference channelPreference = findPreference(key);
        if (profile == null || settings == null) {
            setListPreferenceEmpty(channelPreference);
            return;
        }
        if (allLines.isEmpty()) {
            channelPreference.setVisible(false);
        } else {
            channelPreference.setVisible(true);
        }
        //PubnubInfo pubnubInfo = new PubnubInfo();
//        pubnubInfo.getMutableLiveData().observe(getViewLifecycleOwner(), s -> {
//            Log.d("SettingsFragmentProfile",s);
//            PubnubInfo pubnubInstance = new Gson().fromJson(s, PubnubInfo.class);
//            OnValueChanged<String> stringValueChanged = stringList -> onLineChanged.update(pubnubInstance.getLines(stringList));
//            setMultiListOptions(channelPreference, selectedLines, allLines, stringValueChanged,
//                    pubnubInstance::subscribeToChannelsByName, pubnubInstance::unSubscribeToChannelsByName);
//        });
//        pubnubInfo.getInstance(requireActivity() ,instance -> {
//            Log.d("SettingsFragmentProfile",instance+"");
////            OnValueChanged<String> stringValueChanged = stringList -> onLineChanged.update(instance.getLines(stringList));
////            setMultiListOptions(channelPreference, selectedLines, allLines, stringValueChanged,
////                    instance::subscribeToChannelsByName, instance::unSubscribeToChannelsByName);
//        });
        if (PubnubInfo.getInstance() != null) {
            OnValueChanged<String> stringValueChanged = stringList -> onLineChanged.update(PubnubInfo.getInstance().getLines(stringList));
            setMultiListOptions(channelPreference, selectedLines, allLines, stringValueChanged,
                    PubnubInfo.getInstance()::subscribeToChannelsByName, PubnubInfo.getInstance()::unSubscribeToChannelsByName);
        }
    }

    private void setupTLSswitch() {
        //android.util.Log.d("SettingsFragment", "setupTLSswitch");
        SwitchPreferenceCompat tlsSwitch = findPreference("tls");
        tlsSwitch.setChecked(SettingsHelper.getBoolean(USE_TLS, true));
        tlsSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof Boolean) {
                boolean on = (Boolean) newValue;
                if (on) {
                    updateTLS(tlsSwitch, on);
                } else {
                    showTLSDialog(tlsSwitch);
                }
            }
            return false;
        });
    }

    private void updateTLS(SwitchPreferenceCompat tlsSwitch, boolean on) {
        //android.util.Log.d("SettingsFragment", "updateTLS");
        putBoolean(USE_TLS, on);
        tlsSwitch.setChecked(on);
        AsyncTask.execute(() -> SipManager.getInstance().restartSip());
    }

    private <T extends Line> void setMultiListOptions(MultiSelectListPreference preference, List<T> selectedLines, List<T> allLines, OnValueChanged<String> onValueChanged, OnValuesAdded<String> onValuesAdded, OnValuesRemoved<String> onValuesRemoved) {
       //android.util.Log.d("SettingsFragment", "setMultiListOptions");
        AsyncTask.execute(() -> {
            if (getActivity() == null) {
                return;
            }
            List<String> selectedLineStrings = Line.convertLineListToStringList(selectedLines);
            Collections.sort(allLines, (a, b) -> {
                if (a.isOwner() && !b.isOwner()) {
                    return -1;
                }
                if (!a.isOwner() && b.isOwner()) {
                    return 1;
                }
                return 0;
            });
            List<String> allLineStrings = Line.convertLineListToStringList(allLines);
            allLineStrings.removeAll(selectedLineStrings);
            allLineStrings.addAll(0, selectedLineStrings);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Sets selected values
                    preference.setValues(new HashSet<>(selectedLineStrings));
                    // sets all values backend values
                    preference.setEntryValues(allLineStrings.toArray(new String[]{}));
                    // set all entries frontend display string
                    preference.setEntries(allLineStrings.toArray(new String[]{}));
                    preference.setOnPreferenceChangeListener((Preference preference1, Object newValue) -> {
                        // If nothing changed, do nothing
                        if (newValue.equals(selectedLineStrings)) {
                            return true;
                        }
                        @SuppressWarnings("unchecked")
                        Set<String> newValues = (Set<String>) newValue;
                        List<String> removedValues = new ArrayList<>(preference.getValues());
                        List<String> addedValues = new ArrayList<>(newValues);
                        addedValues.removeAll(preference.getValues());
                        removedValues.removeAll(newValues);
                        onValueChanged.update(new ArrayList<>(newValues));
                        onValuesAdded.add(addedValues);
                        onValuesRemoved.remove(removedValues);
                        return true;
                    });
                });
            }
        });

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //android.util.Log.d("SettingsFragment", "onCreateOptionsMenu");
        inflater.inflate(R.menu.settings, menu);
        if (getActivity() == null) {
            return;
        }
        syncMenuView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.sync_menu, null);
        syncMenuView.setOnClickListener(v -> {
            Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotatation);
            rotate.setRepeatCount(Animation.INFINITE);
            syncMenuView.startAnimation(rotate);
            Settings.fetchSettings(error -> {
                //android.util.Log.d("SettingsFragment", "Error Syncing Settings? " + error);
                syncMenuView.clearAnimation();
            });
            if (profile != null) {
                profile.fetchNumbers();
            }
        });
        menu.findItem(R.id.action_sync).setActionView(syncMenuView);
        //android.util.Log.d("SettingsFragment", "sync menu view is " + syncMenuView);
    }

    @Override
    public void onDestroyOptionsMenu() {
       // android.util.Log.d("SettingsFragment", "Destroying Options Menu");
        syncMenuView.clearAnimation();
        super.onDestroyOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // android.util.Log.d("SettingsFragment", "Is this selected");
        if (item.getItemId() == R.id.action_sync) {
            Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotatation);
            rotate.setRepeatCount(Animation.INFINITE);
            syncMenuView.startAnimation(rotate);
            Settings.fetchSettings(error -> {
                //android.util.Log.d("SettingsFragment", "Error Syncing Settings? " + error);
                syncMenuView.clearAnimation();
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public RecyclerView recyclerView() {
        return getListView();
    }
}

interface OnValueChanged<T> {
    void update(List<T> updated);
}

interface OnValuesRemoved<T> {
    void remove(List<T> removed);
}

interface OnValuesAdded<T> {
    void add(List<T> added);
}

