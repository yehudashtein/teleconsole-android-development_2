package com.telebroad.teleconsole.controller;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;
import androidx.transition.TransitionManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.pjsip.AndroidAudioManager;
import com.telebroad.teleconsole.pjsip.CallGroup;
import com.telebroad.teleconsole.pjsip.CallManager;
import com.telebroad.teleconsole.pjsip.CallStates;
import com.telebroad.teleconsole.pjsip.NonExistentCall;
import com.telebroad.teleconsole.pjsip.PJSIPManager;
import com.telebroad.teleconsole.pjsip.SipService;
import com.telebroad.teleconsole.pjsip.TeleConsoleCall;
import com.telebroad.teleconsole.viewmodels.ActiveCallViewModel;
import static androidx.constraintlayout.widget.ConstraintSet.BOTTOM;
import static androidx.constraintlayout.widget.ConstraintSet.TOP;
import static com.telebroad.teleconsole.controller.SecondCallActivity.NEW_CALL;
import static com.telebroad.teleconsole.controller.SecondCallActivity.TRANSFER;
import static com.telebroad.teleconsole.controller.SecondCallActivity.TYPE_EXTRA;
import static com.telebroad.teleconsole.pjsip.SipService.EXTRA_PJSIP_ID;
import static com.telebroad.teleconsole.pjsip.SipService.NONEXSISTENT_PJSIP_ID;

public class ActiveCallActivity extends AppCompatActivity {
    private ImageView muteButton;
    private ImageView speakerButton;
    private ImageView holdButton;
    private ImageView newCallButton;
    private ImageView recordButton;
    private ImageView conferenceButton;
    private View muteView;
    private View speakerView;
    private View switchView;
    private View holdView;
    private View transferView;
    private View newCallView;
    private View recordView;
    private View conferenceView;
    private TextView conferenceTitle;
    private ConstraintLayout activeCallConstraintLayout;
    private final ConstraintSet regConstraints = new ConstraintSet();
    private final ConstraintSet dtmfConstraints = new ConstraintSet();
    private ImageButton hangupButton;
    private TextView hideDTMF;
    private TextView callerID;
    private Chronometer duration, firstCallStatus;
    private TextView dtmfTextView;
    private ActiveCallViewModel viewModel;
    private Group multiCallGroup;
    private Group singleCallGroup;
    private TextView firstCallName, secondCallName, speakerText;
    private TextView secondCallStatus;
    private PowerManager.WakeLock wakeLock;
    private final CallManager callManager = CallManager.getInstance();
    private final SipManager sipManager = SipManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.logToFile("ActiveCallActivity created");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_call);
        hangupButton = findViewById(R.id.hangup);
        muteButton = findViewById(R.id.muteButton);
        speakerButton = findViewById(R.id.speakerButton);
        holdButton = findViewById(R.id.holdButton);
        newCallButton = findViewById(R.id.addCallButton);
        recordButton = findViewById(R.id.recordButton);
        conferenceButton = findViewById(R.id.conferenceButton);
        conferenceTitle = findViewById(R.id.conferenceTitle);
        muteView = findViewById(R.id.muteView);
        View dialpadView = findViewById(R.id.dialpadView);
        speakerView = findViewById(R.id.speakerView);
        switchView = findViewById(R.id.switchView);
        holdView = findViewById(R.id.holdView);
        transferView = findViewById(R.id.transferView);
        newCallView = findViewById(R.id.addCallView);
        recordView = findViewById(R.id.recordView);
        conferenceView = findViewById(R.id.conferenceView);
        hideDTMF = findViewById(R.id.hideDTMF);
        callerID = findViewById(R.id.caller_name);
        callerID.setSelected(true);
        activeCallConstraintLayout = findViewById(R.id.activeCallConstraintLayout);
        regConstraints.clone(activeCallConstraintLayout);
        dtmfConstraints.clone(activeCallConstraintLayout);
        dialpadView.setOnClickListener((clicked) -> showDTMF());
        hideDTMF.setOnClickListener((clicked) -> hideDTMF());
        viewModel = new ViewModelProvider(this).get(ActiveCallViewModel.class);
        duration = findViewById(R.id.call_duration);
        dtmfTextView = findViewById(R.id.dtmfTextView);
        singleCallGroup = findViewById(R.id.singleCaller);
        multiCallGroup = findViewById(R.id.multipleCallers);
        secondCallName = findViewById(R.id.secondCallName);
        firstCallName = findViewById(R.id.firstCallName);
        firstCallStatus = findViewById(R.id.firstCallStatus);
        secondCallStatus = findViewById(R.id.secondCallStatus);
        speakerText = findViewById(R.id.speakerText);
        View secondCallView = findViewById(R.id.secondCallerView);
        secondCallView.setOnClickListener(view -> {
            sipManager.hold(currentCall.getID(), true);
            sipManager.hold(callManager.getOtherCall().getID(), false);
            callManager.setLiveCall(callManager.getOtherCall());
        });
        int pjsipID = getIntent().getIntExtra(SipService.EXTRA_PJSIP_ID, NONEXSISTENT_PJSIP_ID);
        setCall(callManager.getCall(pjsipID));
        setupDTMF();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null && powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "teleconsole:activeCallWakelock");
            wakeLock.setReferenceCounted(false);
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
        Utils.logToFile("Active call activity resumed");
        callManager.getLiveCall().observe(this, this::updateUI);
        AndroidAudioManager.getAudioManager().getAudioState().observe(this, this::updateSpeakerUI);
//        viewModel.hasActiveCall.observe(this, this::updateUI);
        viewModel.activeCallGroup.observe(this, this::setCall);
    }

    private void updateSpeakerUI(AndroidAudioManager.AudioState audioState) {
       // android.util.Log.d("SPEAKERUI", "State is " + audioState);
        if (audioState == null){
            return;
        }
        switch (audioState){
            case UNKNOWN:
                break;
            case SPEAKER:
                speakerButton.setSelected(true);
                speakerButton.setImageResource(R.drawable.ic_voicemail_speaker);
                speakerText.setText(R.string.speaker);
                break;
            case BLUETOOTH:
                speakerButton.setSelected(true);
                speakerButton.setImageResource(android.R.drawable.stat_sys_data_bluetooth);
                speakerText.setText(R.string.bluetooth);
                break;
            case EARPIECE:
                if (AndroidAudioManager.getAudioManager().isBluetoothHeadsetConnected()){
                    speakerButton.setSelected(true);
                    speakerButton.setImageResource(R.drawable.ic_speaker_phone);
                    speakerText.setText(R.string.headset);
                }else{
                    speakerButton.setSelected(false);
                    speakerButton.setImageResource(R.drawable.ic_voicemail_speaker);
                    speakerText.setText(R.string.speaker);
                }
                break;
        }
    }
    CallGroup currentCall;
    void setCall(CallGroup activeCall) {
        Utils.logToFile("ActiveCallActivity setting call " + activeCall);
        if (activeCall == null) {
            return;
        }
        if (activeCall == currentCall) {
            return;
        }
        if (activeCall == NonExistentCall.getInstance()){
            if (callManager.hasCalls()){
                callManager.setActiveCall(callManager.getAllCalls()[0]);
            }else{
                finish();
            }
        }
        if (currentCall != null) {
            currentCall.liveCallStates().removeObservers(this);
            currentCall.getRemoteNumber().getName(this).removeObservers(this);
        }
        currentCall = activeCall;
        // Dergesiter the old contact observer and register the new
        currentCall.getRemoteNumber().getName(this).observe(this, name -> {
            if (!callerID.getText().toString().equals(name)) {
               // android.util.Log.d("NameReset", "resetting in setCall");
                callerID.setText(name);
            }
        });
        currentCall.liveCallStates().observe(this, callStates -> {
            if (callStates == null) {
                return;
            }
            if (callStates.isDone()) {
                if (callManager.hasCalls()) {
                    setCall(callManager.getActiveCallGroup());
                } else {
                    finishAndRemoveTask();
                }
            }
            updateUIStates(callStates);
//            holdButton.setSelected(callStates.isHold());
            updateUI(currentCall);
        });
        //currentCall.liveCallStates().observe(this, this::updateUI);
    }


    private void updateUIStates(CallStates states) {
        muteButton.setSelected(states.isMute());
        holdButton.setSelected(states.isHold());
        updateSpeakerUI(AndroidAudioManager.getAudioManager().getAudioState().getValue());
//        speakerButton.setSelected(currentCall.getCallController().isSpeaker());
    }

    @Override
    protected void onPause() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onPause();
    }

    private void updateUI(CallGroup callGroup) {
        if (callManager.hasCalls()) {
            //android.util.Log.d("ACUI", "has multiple groups " + callManager.hasMultipleGroups());
            if (callManager.hasMultipleGroups()) {
                newCallButton.setEnabled(false);
                multiCallGroup.setVisibility(View.VISIBLE);
                singleCallGroup.setVisibility(View.GONE);
                conferenceButton.setImageResource(R.drawable.ic_call_merge);
                conferenceTitle.setText(R.string.merge_calls);
                setupCallUI(callManager.getActiveCallGroup(), firstCallName, firstCallStatus);
                setupCallUI(callManager.getOtherCall(), secondCallName, secondCallStatus);
            } else {
                multiCallGroup.setVisibility(View.GONE);
                singleCallGroup.setVisibility(View.VISIBLE);
              //  android.util.Log.d("ACUI", "is in conference? " + callManager.isInConference());
                if (callManager.isInConference()) {
                    conferenceButton.setImageResource(R.drawable.ic_call_split);
                    conferenceTitle.setText(R.string.split_calls);
                } else {
                    conferenceButton.setImageResource(R.drawable.ic_conference);
                    conferenceTitle.setText(R.string.conference);
                }
                setupCallUI(callGroup, callerID, duration);
            }
        } else {
            finishAndRemoveTask();
        }
    }

    private void updateUI(SipManager.UIState uiState) {
        switch (uiState) {
            case FINISHED:
                this.finishAndRemoveTask();
                break;
            case SINGLE:
                //android.util.Log.d("UIChoice", "Choosing Single Call Layout");
                multiCallGroup.setVisibility(View.GONE);
                singleCallGroup.setVisibility(View.VISIBLE);
                conferenceButton.setImageResource(R.drawable.ic_conference);
                conferenceTitle.setText(R.string.conference);
                setupCallUI(callManager.getActiveCall(), callerID, duration);
                break;
            case CONFERENCE:
                newCallButton.setEnabled(false);
                multiCallGroup.setVisibility(View.GONE);
                singleCallGroup.setVisibility(View.VISIBLE);
                conferenceButton.setImageResource(R.drawable.ic_call_split);
                conferenceTitle.setText("Split Calls");
                callerID.setText(R.string.in_conf);
                duration.setBase(SystemClock.elapsedRealtime() - callManager.getLongestDurationInConference() * 1000);
                duration.start();
                break;
            case MULTI:
                newCallButton.setEnabled(false);
                multiCallGroup.setVisibility(View.VISIBLE);
                singleCallGroup.setVisibility(View.GONE);
                conferenceButton.setImageResource(R.drawable.ic_call_merge);
                conferenceTitle.setText("Merge Calls");
                setupCallUI(callManager.getActiveCallGroup(), firstCallName, firstCallStatus);
                setupCallUI(callManager.getOtherCall(), secondCallName, secondCallStatus);
                break;
        }
    }

    @SuppressLint({"HardwareIds", "InflateParams"})
    private void setupButtons() {
        muteView.setOnClickListener((clicked) -> {
            muteButton.setSelected(!muteButton.isSelected());
            currentCall.getCallController().mute(muteButton.isSelected());
//            sipManager.mute(muteButton.isSelected());
        });
        speakerView.setOnClickListener((clicked) -> {
            if (AndroidAudioManager.getAudioManager().isBluetoothHeadsetConnected()){
                new RouteChooserList().show(getSupportFragmentManager(), "routeAudio");
            }else{
                speakerButton.setSelected(!speakerButton.isSelected());
                if (speakerButton.isSelected()){
                    AndroidAudioManager.getAudioManager().routeAudioToSpeaker();
                }else{
                    AndroidAudioManager.getAudioManager().routeAudioToEarPiece();
                }
            }
//            speakerButton.setSelected(!speakerButton.isSelected());
//            currentCall.getCallController().setSpeaker(speakerButton.isSelected());
//            if (speakerButton.isSelected()) {
//                if (wakeLock != null && wakeLock.isHeld()) {
//                    wakeLock.release();
//                }
//            } else {
//                if (wakeLock != null && !wakeLock.isHeld()) {
//                    wakeLock.acquire();
//                }
//            }
        });
        switchView.setOnClickListener((clicked) -> {
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
            alert.setTitle(R.string.switch_dialog_title);
            alert.setMessage(R.string.switch_dialog_message);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_edit_text, null);
            String deviceNumber = SettingsHelper.getDevicePhoneNumber();
            final EditText input = view.findViewById(R.id.dialog_number);
            final CheckBox checkBox = view.findViewById(R.id.checkBox);
            String formatted = PhoneNumber.format(deviceNumber);
            formatted = formatted.equalsIgnoreCase("Unknown") ? "" : formatted;
            input.setText(formatted);
            input.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setChecked(true);
                }
            });
            alert.setView(view);
            alert.setPositiveButton(R.string.switch_dialog_button, (dialog, which) -> {
                String forwardTo = input.getText().toString();
                String fixedNumber = PhoneNumber.fix(forwardTo);
                if (fixedNumber.length() >= 8 && fixedNumber.length() <= 13) {
                    currentCall.getCallController().transfer(fixedNumber);
//                    sipManager.transfer(fixedNumber);
                    if (checkBox.isChecked()){
                        SettingsHelper.putString(R.string.cell_number_key, forwardTo);
                    }
                } else {
                    Toast.makeText(this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
                }
            });
            alert.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
//            builder.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            AlertDialog dialog = alert.create();
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
            input.clearFocus();
            input.requestFocus();
        });

        holdView.setOnClickListener((clicked) -> {
            holdButton.setSelected(!holdButton.isSelected());
            currentCall.getCallController().hold(holdButton.isSelected());
//            sipManager.hold(holdButton.isSelected());
        });
        transferView.setOnClickListener((clicked) -> startSecondCallActivity(TRANSFER));
        newCallView.setOnClickListener((clicked) -> {
            if (callManager.getCallCount() >= 2) {
                Toast.makeText(this, R.string.maximum_calls_reached, Toast.LENGTH_LONG).show();
                //Snackbar.make(newCallView, R.string.maximum_calls_reached, Snackbar.LENGTH_SHORT).show();
                return;
            }
            startSecondCallActivity(NEW_CALL);
        });
        recordView.setOnClickListener((clicked) -> {
            recordButton.setSelected(!recordButton.isSelected());
            // TODO Check for permissions
            sipManager.record(recordButton.isSelected());
            if (recordButton.isSelected() && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)){
                Toast.makeText(this, "Recordings are saved in the Music/Call Recordings Folder",Toast.LENGTH_LONG).show();
            }
        });
        conferenceView.setOnClickListener((clicked) -> {
//            if (true){
//                com.telebroad.teleconsole.helpers.AlertsHelper.makeLong(this, R.string.conference_not_implemented);
//                startSecondCallActivity(CONFERENCE);
//                return;
//            }
//            if (callManager.getCallCount() < 2) {
//                com.telebroad.teleconsole.helpers.AlertsHelper.makeLong(this, R.string.conference_not_implemented);
//                return;
//            }
            if (callManager.getCallCount() < 2) {
                startSecondCallActivity(SecondCallActivity.CONFERENCE);
            } else if (callManager.isInConference()) {
                SplitBottomDialog.getInstance().show(getSupportFragmentManager(), "split");
            } else {
                sipManager.conference();
            }
//            if (callManager.canConference( error -> android.util.Log.d("Conf01", "error is " + error))){
//                callManager.conference();
//            }
        });
        hangupButton.setOnClickListener((clicked) -> hangup());
    }

    private void removeObservers() {
        if (callManager.getLiveCall() != null) {
            callManager.getLiveCall().removeObservers(this);
        }
        if (viewModel != null && viewModel.activeCallGroup != null) {
            LiveData<CallGroup> call = viewModel.activeCallGroup;
            call.removeObservers(this);
            if (call.getValue() != null && call.getValue().liveCallStates() != null){
                call.getValue().liveCallStates().removeObservers(this);
            }
        }
    }

    private void showDTMF() {
        dtmfConstraints.constrainMaxWidth(R.id.callControls, 56);
        dtmfConstraints.constrainMaxHeight(R.id.callControls, 56);
        dtmfConstraints.clear(R.id.callControls, BOTTOM);
//        TransitionManager.beginDelayedTransition(activeCallConstraintLayout);
//        dtmfConstraints.applyTo(activeCallConstraintLayout);
        dtmfConstraints.constrainMaxHeight(R.id.dtmf, Math.round(getResources().getDimension(R.dimen.dtmfDialpadMaxHeight)));
        dtmfConstraints.constrainMaxWidth(R.id.dtmf, Math.round(getResources().getDimension(R.dimen.dtmfDialpadMaxWidth)));
        dtmfConstraints.connect(R.id.dtmf, BOTTOM, R.id.bottomGuideline, TOP);
        TransitionManager.beginDelayedTransition(activeCallConstraintLayout);
        dtmfConstraints.applyTo(activeCallConstraintLayout);
        duration.animate().alpha(0.0f).withEndAction(() -> dtmfTextView.setVisibility(View.VISIBLE));
        hideDTMF.animate().alpha(1.0f);
    }

    private void hideDTMF() {
        TransitionManager.beginDelayedTransition(activeCallConstraintLayout);
        regConstraints.applyTo(activeCallConstraintLayout);
        dtmfTextView.setVisibility(View.GONE);
        duration.animate().alpha(1.0f);
        updateUI(currentCall);
        //updateUI((SipManager.UIState) sipManager.getUIState().getValue());
        hideDTMF.animate().alpha(0.0f);
    }

    private void hangup() {
        currentCall.getCallController().hangup();
//        sipManager.hangup();
    }

    private void setupDTMF() {
        @SuppressLint("SetTextI18n") View.OnClickListener dtmfDialed = (clicked) -> {
            currentCall.getCallController().sendDTMF(clicked.getTag().toString());
//            sipManager.sendDTMF(clicked.getTag().toString());
            dtmfTextView.setText(dtmfTextView.getText() + clicked.getTag().toString());
        };
        ImageButton dialpad0 = findViewById(R.id.dialpad_0);
        ImageButton dialpad1 = findViewById(R.id.dialpad_1);
        ImageButton dialpad2 = findViewById(R.id.dialpad_2);
        ImageButton dialpad3 = findViewById(R.id.dialpad_3);
        ImageButton dialpad4 = findViewById(R.id.dialpad_4);
        ImageButton dialpad5 = findViewById(R.id.dialpad_5);
        ImageButton dialpad6 = findViewById(R.id.dialpad_6);
        ImageButton dialpad7 = findViewById(R.id.dialpad_7);
        ImageButton dialpad8 = findViewById(R.id.dialpad_8);
        ImageButton dialpad9 = findViewById(R.id.dialpad_9);
        ImageButton dialpadStar = findViewById(R.id.dialpad_star);
        ImageButton dialpadPound = findViewById(R.id.dialpad_pound);
        int whiteColor = Color.argb(255, 255, 255, 255);
        dialpad0.setColorFilter(whiteColor);
        dialpad1.setColorFilter(whiteColor);
        dialpad2.setColorFilter(whiteColor);
        dialpad3.setColorFilter(whiteColor);
        dialpad4.setColorFilter(whiteColor);
        dialpad5.setColorFilter(whiteColor);
        dialpad6.setColorFilter(whiteColor);
        dialpad7.setColorFilter(whiteColor);
        dialpad8.setColorFilter(whiteColor);
        dialpad9.setColorFilter(whiteColor);
        dialpadStar.setColorFilter(whiteColor);
        dialpadPound.setColorFilter(whiteColor);
        dialpad0.setOnClickListener(dtmfDialed);
        dialpad1.setOnClickListener(dtmfDialed);
        dialpad2.setOnClickListener(dtmfDialed);
        dialpad3.setOnClickListener(dtmfDialed);
        dialpad4.setOnClickListener(dtmfDialed);
        dialpad5.setOnClickListener(dtmfDialed);
        dialpad6.setOnClickListener(dtmfDialed);
        dialpad7.setOnClickListener(dtmfDialed);
        dialpad8.setOnClickListener(dtmfDialed);
        dialpad9.setOnClickListener(dtmfDialed);
        dialpadStar.setOnClickListener(dtmfDialed);
        dialpadPound.setOnClickListener(dtmfDialed);
    }

    private void setupCallUI(CallGroup call, TextView name, TextView status) {
        if (call == null){
            return;
        }
        LiveData<String> liveName = call.getRemoteNumber().getName(this);
        liveName.observe(this, (String callerName) -> {
            if (callerName != null) {
                if (!callerName.equals(name.getText().toString())) {
                    android.util.Log.d("NameReset", "resetting in setup call id");
                    name.setText(callerName);
                }
                new Handler().postDelayed(() -> liveName.removeObservers(this), 5000);
            }
        });
        if (status instanceof Chronometer) {
            Chronometer chronometer = (Chronometer) status;
            android.util.Log.d("CallState", "setting up call with " + call.liveCallStates().getValue());
            CallStates callState = call.liveCallStates().getValue();
            if (callState == null){
                return;
            }
            android.util.Log.d("CallState", "setting up call before if ");
            if (callState.isRinging()) {
                chronometer.clearAnimation();
                chronometer.stop();
                status.setText(R.string.ringing);
            } else if (callState.isHold()) {
                chronometer.stop();
                chronometer.clearAnimation();
                status.setText(R.string.on_hold);
            } else if (callState.isEarly()) {
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(500);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                chronometer.startAnimation(anim);
                status.setText(R.string.calling_string);
            } else {
                chronometer.clearAnimation();
                // elapsed time is in millis while call duration is in seconds
                SipService.getInstance().enqueueJob(() -> {
                    int duration = call.duration();
                    runOnUiThread(() ->  chronometer.setBase(SystemClock.elapsedRealtime() - duration * 1000L));

                },"Set duration");
                chronometer.start();
            }
            android.util.Log.d("CallState", "finished setting up call with " + call.liveCallStates().getValue());
        } else {
//            android.util.Log.d("CallState", "getDisplayName " + call.callStates() + " result " + sipManager.getStateDisplayName(call));
            if (sipManager instanceof PJSIPManager && call instanceof TeleConsoleCall){
                status.setText(sipManager.getStateDisplayName(call));
            }
        }
    }

    private void startSecondCallActivity(int type) {
        Intent secondCallIntent = new Intent(this, SecondCallActivity.class);
        secondCallIntent.putExtra(TYPE_EXTRA, type);
        secondCallIntent.putExtra(EXTRA_PJSIP_ID, currentCall.getID());
        startActivity(secondCallIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
