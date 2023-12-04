package com.telebroad.teleconsole;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.telebroad.teleconsole.helpers.DTMFPlayer;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.PhoneNumber;


public class DialPadView extends ConstraintLayout {
    private EditText dialedNumber;
    private ImageButton backspace;
    private boolean displayLastCalledNumber = true;
    private boolean saveCalledNumberAsLast = true;
    private final String displayNumber = "";
    public static final int FIRST_CALL = 0, SECOND_CALL = 1, TRANSFER = 2, CONFERENCE = 3;
    private int callAction;
    private Activity activity;

    public DialPadView(Context context) {
        super(context);
        init(context);
    }

    public DialPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray;
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.DialPadView);
        saveCalledNumberAsLast = typedArray.getBoolean(R.styleable.DialPadView_saveLastCalledNumber, true);
        displayLastCalledNumber = typedArray.getBoolean(R.styleable.DialPadView_showLastCalledNumber, true);
        typedArray.recycle();
    }

    public DialPadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.inflate(R.layout.layout_dialpad_expanded, this);
        }
    }

    @Override
    protected void onFinishInflate() {
        dialedNumber = findViewById(R.id.dialed_number);
        dialedNumber.setShowSoftInputOnFocus(false);
        dialedNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher("US"){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                super.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
            }

            @Override
            public synchronized void afterTextChanged(Editable s) {
                backspace.setVisibility(dialedNumber.getText().toString().isEmpty() ? INVISIBLE : VISIBLE);
                super.afterTextChanged(s);
            }
        });
        initializeButtons();
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initializeButtons() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkTheme = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

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
//        if (isDarkTheme){
//            Drawable myFabSrc = ResourcesCompat.getDrawable(getResources(),R.drawable.dial_pad_4,null);
//            Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
//            willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//            dialpad4.setImageDrawable(willBeWhite);
//        }else {
//            Drawable myFabSrc = ResourcesCompat.getDrawable(getResources(),R.drawable.dial_pad_4,null);
//            Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
//            willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//            dialpad4.setImageDrawable(willBeWhite);
//        }
        backspace = findViewById(R.id.backspace);
        ImageButton callButton = findViewById(R.id.callButton);
//        callButton.post(() -> {
//            int padding = Math.round(callButton.getWidth() * 0.3f);
//            android.util.Log.d("CALL_BUTTON", "padding = " + padding + " height = " + callButton.getWidth());
//            callButton.setPadding(padding,padding, padding,padding);
//        });

        @SuppressLint("SetTextI18n") View.OnClickListener numberDialed = (clickedView) -> {
           // android.util.Log.d("DLPD", "Start is " + dialedNumber.getSelectionStart() + " end is " + dialedNumber.getSelectionEnd());
            dialedNumber.getText().replace(dialedNumber.getSelectionStart(), dialedNumber.getSelectionEnd(), clickedView.getTag().toString());
//            dialedNumber.getText().append(clickedView.getTag().toString());
            DTMFPlayer.getInstance().play(clickedView.getTag().toString().charAt(0));
            backspace.setVisibility(View.VISIBLE);
        };
        dialpad0.setOnClickListener(numberDialed);
        dialpad1.setOnClickListener(numberDialed);
        dialpad2.setOnClickListener(numberDialed);
        dialpad3.setOnClickListener(numberDialed);
        dialpad4.setOnClickListener(numberDialed);
        dialpad5.setOnClickListener(numberDialed);
        dialpad6.setOnClickListener(numberDialed);
        dialpad7.setOnClickListener(numberDialed);
        dialpad8.setOnClickListener(numberDialed);
        dialpad9.setOnClickListener(numberDialed);
        dialpadStar.setOnClickListener(numberDialed);
        dialpadPound.setOnClickListener(numberDialed);
        backspace.setOnClickListener((clicked) -> {
            if (dialedNumber.getText().toString().isEmpty()){
                backspace.setVisibility(View.INVISIBLE);
                return;
            }
            int start = dialedNumber.getSelectionStart();
            int end = dialedNumber.getSelectionEnd();
            if (start == end) {
                if (start != 0) {
                    dialedNumber.getText().replace(start - 1, end, "");
                }
            }else{
                dialedNumber.getText().replace(start, end, "");
            }
            if (dialedNumber.getText().length() == 0) {
                backspace.setVisibility(View.INVISIBLE);
            }
        });
        callButton.setOnClickListener(v -> {
            String oldText = dialedNumber.getText().toString();
            if (oldText.isEmpty()) {
                if (displayLastCalledNumber) {
                    String lastCalledNumber = SettingsHelper.getString(SettingsHelper.LAST_CALLED_NUMBER, "");
                    if (!lastCalledNumber.isEmpty()) {
                        dialedNumber.setText(PhoneNumber.fix(lastCalledNumber));
                        backspace.setVisibility(View.VISIBLE);
                    }
                }
                return;
            }
            if (saveCalledNumberAsLast) {
                SettingsHelper.putString(SettingsHelper.LAST_CALLED_NUMBER, oldText);
            }
            apply(PhoneNumber.fix(oldText));
            dialedNumber.getText().clear();
            backspace.setVisibility(INVISIBLE);
        });
        backspace.setOnLongClickListener((clicked) -> {
            dialedNumber.getText().clear();
            backspace.setVisibility(View.INVISIBLE);
            return true;
        });
    }
    public void setActivity(Activity activity){
        this.activity = activity;
    }

    private Activity getActivity() {
        if (activity != null){
            return activity;
        }
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private final static String DIALED_NUMBER = "com.telebroad.dialed.number";
    private final static String STATE_SUPER_CLASS = "com.telebroad.dialpadview.superclass";
    private final static String CAll_ACTION = "com.telebroad.dial.pad.view.custom.action";
    private final static String DISPLAY_LAST_CALLED_NUMBER = "com.telebroad.dial.pad.view.display.last.called.number";
    private final static String SAVE_NUMBER_AS_LAST = "com.telebroad.dial.pad.view.save.number.as.last";

    /**
     * Identifier for the state of the super class.
     */

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER_CLASS,
                super.onSaveInstanceState());
        String dialedNumberString = dialedNumber.getText().toString();
        bundle.putString(DIALED_NUMBER, dialedNumberString);
        bundle.putInt(CAll_ACTION, callAction);
        bundle.putBoolean(DISPLAY_LAST_CALLED_NUMBER, displayLastCalledNumber);
        bundle.putBoolean(SAVE_NUMBER_AS_LAST, saveCalledNumberAsLast);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle
                    .getParcelable(STATE_SUPER_CLASS));
            String savedNumber = bundle.getString(DIALED_NUMBER);
            dialedNumber.setText(PhoneNumber.fix(savedNumber));
            //updateDialedNumber(savedNumber);
            callAction = bundle.getInt(CAll_ACTION, FIRST_CALL);
            backspace.setVisibility(savedNumber == null || savedNumber.isEmpty() ? INVISIBLE : VISIBLE);
        } else
            super.onRestoreInstanceState(state);
    }

    public void setPresetNumber(String number){
        dialedNumber.setText(PhoneNumber.fix(number));
    }

    public void setCallAction(int callAction) {
        this.callAction = callAction;
    }

    private void apply(String phoneNumber) {
        //android.util.Log.d("EMERGENCY", "Is 911 an Emergency Number? " + PhoneNumberUtils.isEmergencyNumber("911"));
        if (PhoneNumberUtils.isEmergencyNumber(phoneNumber)) {
            AlertDialog alert = new MaterialAlertDialogBuilder(getContext()).setTitle(R.string.emergency_dialog_title).setMessage(R.string.emergency_dialog_message)
                    .setPositiveButton(R.string.emergency_dialog_call, (dialog, which) -> {
                        if (getActivity() == null || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Utils.makeCall(getContext(), phoneNumber);
                        dialog.dismiss();
                    }).setNegativeButton(R.string.emergency_dialog_cancel, (dialog, which) -> {
                dialog.dismiss();
            }).create();
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();
            return;
        }
        if (getActivity() == null) {
            return;
        }
        switch (callAction) {
            case FIRST_CALL:
                SipManager.getInstance(this.getActivity().getApplicationContext()).call(phoneNumber, getActivity());
                break;
            case SECOND_CALL:
//                CallManager.getInstance().getActiveCallGroup().getCallController().hold(true);
                SipManager.getInstance(getContext().getApplicationContext()).call(phoneNumber, getActivity());
                getActivity().finish();
                break;
            case TRANSFER:
                SipManager.getInstance(getContext().getApplicationContext()).transfer(phoneNumber);
                getActivity().finish();
                break;
            case CONFERENCE:
                SipManager.getInstance(getContext().getApplicationContext()).conference(phoneNumber);
                getActivity().finish();
                break;
            default:
                //.w("SecondCallActivity", "No Known Action", new IllegalArgumentException("Incorrect calling type"));
                break;
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        // Makes sure that the state of the child views in the side
        // spinner are not saved since we handle the state in the
        // onSaveInstanceState.
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        // Makes sure that the state of the child views in the side
        // spinner are not restored since we handle the state in the
        // onSaveInstanceState.
        super.dispatchThawSelfOnly(container);
    }

    @FunctionalInterface
    public interface CallAction extends Parcelable {
        @Override
        default int describeContents() {
            return 0;
        }

        @Override
        default void writeToParcel(Parcel dest, int flags) {

        }
        void apply(String phoneNumber);
    }
}
