package com.telebroad.teleconsole.helpers;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import android.widget.TextView;

import com.telebroad.teleconsole.R;

public class ListMessageDialogFragment extends ListPreferenceDialogFragmentCompat {
   
    public static ListMessageDialogFragment newInstance(String key) {
        final ListMessageDialogFragment fragment =
                new ListMessageDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        TextView messageView = new TextView(getContext());
        messageView.setText(R.string.voip_dialog_message);
        int padding = getResources().getDimensionPixelSize(R.dimen.voip_dialog_side_margin);
        messageView.setPadding(padding,getResources().getDimensionPixelSize(R.dimen.voip_dialog_top_margin),padding,0);
        builder.setView(messageView);
        super.onPrepareDialogBuilder(builder);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //android.util.Log.d("LMP", "creating dialog");
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        TextView messageView = new TextView(getContext());
        messageView.setText("Test testy test ");
        dialog.setContentView(messageView);
        //android:dialogMessage="When VoIP is turned off, outgoing calls will be made by calling you cellphone first, and when you pick up, the outgoing call will be placed "
        //dialog.addContentView(messageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return dialog;
    }

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
//   }
}
