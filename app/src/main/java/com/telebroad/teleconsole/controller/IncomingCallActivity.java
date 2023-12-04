package com.telebroad.teleconsole.controller;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.pjsip.CallGroup;
import com.telebroad.teleconsole.pjsip.CallManager;
import com.telebroad.teleconsole.pjsip.NonExistentCall;
import com.telebroad.teleconsole.pjsip.SipService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;
import static com.telebroad.teleconsole.helpers.IntentHelper.CALL_ID;

public class IncomingCallActivity extends AppCompatActivity {

    private static final String EXTRA_MOBILE_CALL_ACTIVE = "com.telebroad.teleconsole.controller.incoming.call.activity.mobile.call.active";
    @NonNull
    int pjsipID = SipService.DEFAULT_PJSIP_ID;
    //TextView ellipsisView;
    private CallGroup call;
    boolean running = true;
    private final Runnable hangup = this::finishAndRemoveTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
            getWindow().addFlags(flags);
        }
        pjsipID = getIntent().getIntExtra(SipService.EXTRA_PJSIP_ID, SipService.DEFAULT_PJSIP_ID);
        if (getIntent().getBooleanExtra(EXTRA_MOBILE_CALL_ACTIVE, false)) {
            if (CallManager.getInstance().hasExternalCalls()) {
                AlertDialog alert = new MaterialAlertDialogBuilder(this).
                        setTitle("Active call").
                        setMessage("There is an active call on your phone. We cannot put it on hold or mute it. If you pick up, both calls will be able to hear what you are saying. Do you still want to pick up?").
                        setPositiveButton("Pick up", ((dialog, which) -> {
                            SipManager.getInstance().answer(pjsipID, true);
                            dialog.dismiss();
                        })).
                        setNegativeButton("Cancel", ((dialog, which) -> {

                            dialog.dismiss();
                        })).create();
                alert.setOnShowListener(dialog -> {
                    Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                    negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                });alert.show();
            }
        }
       // android.util.Log.d("IncomingCallActivity", "are extras null? ");
        running = true;
        CallGroup callGroup = CallManager.getInstance().getCall(pjsipID);
        Object object = SipManager.getInstance(getApplicationContext()).getCall(getIntent().getStringExtra(CALL_ID));
        if (callGroup == NonExistentCall.getInstance()){
            finish();
            return;
        }
        TextView nameView = findViewById(R.id.nameLabel);
        nameView.setSelected(true);
        callGroup.getRemoteNumber().getName(this).observe(this, nameView :: setText);
        callGroup.liveCallStates().observe(this, states -> {
            if (states != null && states.isDone()){
                finishAndRemoveTask();
            }
        });
        ((TextView) findViewById(R.id.numberLabel)).setText(callGroup.getRemoteNumber().formatted());
        findViewById(R.id.declineButton).setOnClickListener((clicked) -> {
            SipManager.getInstance().decline(pjsipID);
            finishAndRemoveTask();
        });
        findViewById(R.id.answerButton).setOnClickListener((clicked) -> {
            SipManager.getInstance().answer(pjsipID);
            finishAndRemoveTask();
        });
    }

    public static void showIncomingCall(Context context, int callID, boolean isSecondTry){
        Intent intent = new Intent(context, IncomingCallActivity.class);
        intent.putExtra(SipService.EXTRA_PJSIP_ID, callID);
        intent.putExtra(EXTRA_MOBILE_CALL_ACTIVE, isSecondTry);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_VOLUME_UP || keyCode == KEYCODE_VOLUME_DOWN) {
           return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
