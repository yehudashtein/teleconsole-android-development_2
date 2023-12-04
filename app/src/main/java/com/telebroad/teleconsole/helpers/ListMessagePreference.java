package com.telebroad.teleconsole.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;

public class ListMessagePreference extends ListPreference {
    CharSequence messageString;
    public ListMessagePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getMessageString(context, attrs);
    }

    private void getMessageString(Context context, AttributeSet attrs) {
        int[] set = {android.R.attr.dialogMessage};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        messageString = a.getText(0);
        a.recycle();
    }

    public ListMessagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListMessagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        getMessageString(context, attrs);
       // android.util.Log.d("LMP", "preference manager " + getPreferenceManager());
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
    }

    @Override
    protected void onClick() {
        //android.util.Log.d("LMP", "preference manager " + getPreferenceManager().getOnDisplayPreferenceDialogListener());
        super.onClick();
    }

    //    @Override
//    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
//        builder.setMessage(messageString);
//        super.onPrepareDialogBuilder(builder);
//    }
//
//    @Override
//    protected View onCreateDialogView() {
//        View dialogView = super.onCreateDialogView();
//        LinearLayout linearLayout = new LinearLayout(getContext());
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        linearLayout.addView(dialogView);
//        TextView messageView = new TextView(getContext());
//        messageView.setText(messageString);
//        linearLayout.addView(messageView);
//        return linearLayout;
//    }

    public ListMessagePreference(Context context) {
        this(context, null);
    }
}
