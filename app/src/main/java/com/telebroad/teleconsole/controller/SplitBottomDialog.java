package com.telebroad.teleconsole.controller;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.pjsip.CallManager;
import com.telebroad.teleconsole.pjsip.TeleConsoleCall;

//import org.linphone.core.LinphoneCall;

import androidx.annotation.NonNull;
public class SplitBottomDialog extends BottomSheetDialogFragment implements View.OnClickListener{


    public static SplitBottomDialog getInstance(){
        return new SplitBottomDialog();
    }

    TextView firstCall;
    TextView secondCall;
    TextView both;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.split_dialog, null);

        firstCall = view.findViewById(R.id.split_first);
        secondCall = view.findViewById(R.id.split_second);
        both = view.findViewById(R.id.pause_both);
        TeleConsoleCall[] calls = CallManager.getInstance().getAllCalls();
        if (calls.length == 2){
            firstCall.setText(getString(R.string.stay_with, calls[0].getRemoteNumber().formatted()));
//            android.util.Log.d("Split01", "First " + calls[0].getRemoteAddress().getUserName() + " with id " + calls[0].getCallLog().getCallId());
            firstCall.setOnClickListener(v -> {
                SipManager.getInstance().split(calls[0].getID());
                dismiss();
            });
            secondCall.setText(getString(R.string.stay_with, calls[1].getRemoteNumber().formatted()));

            secondCall.setOnClickListener(v -> {
                SipManager.getInstance().split(calls[1].getID());
                dismiss();
            });
//            android.util.Log.d("Split01", "Second " + calls[1].getRemoteAddress().getUserName() + " with id " + calls[1].getCallLog().getCallId());
            //secondCall.setTag(calls[1].getId());
            both.setOnClickListener(v -> {
                SipManager.getInstance().split((Integer) null);
                dismiss();
            });
        }else{
            dismiss();
        }

        d.setContentView(view);
        return d;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissAllowingStateLoss();
    }

    @Override
    public void onClick(View v) {
       // android.util.Log.d("Split01", "Keeping " + v.getTag());
        if (v.getTag() == null){
            SipManager.getInstance().split((String) null);
//            SipManager.getInstance().split(N);
        }else{
            SipManager.getInstance().split(v.getTag().toString());
        }
        dismiss();
    }
}
