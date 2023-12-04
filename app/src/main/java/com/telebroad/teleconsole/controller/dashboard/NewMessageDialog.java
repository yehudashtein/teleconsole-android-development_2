package com.telebroad.teleconsole.controller.dashboard;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.NewFaxActivity;
import com.telebroad.teleconsole.controller.NewTextActivity;
import com.telebroad.teleconsole.databinding.FragementNewMessageDialogBinding;


public class NewMessageDialog extends BottomSheetDialogFragment {

    FragementNewMessageDialogBinding binding;
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Dialog d = super.onCreateDialog(savedInstanceState);
        binding = FragementNewMessageDialogBinding.inflate(LayoutInflater.from(getContext()));
        Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(AppController.getInstance().getApplicationContext(), R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
        binding.textMessageView.setOnClickListener(v -> {
            if (getActivity() == null){
                Toast.makeText(getContext(), "Unable to create text message, Activity is null", Toast.LENGTH_LONG).show();
            }
            getActivity().startActivity(new Intent(getActivity(), NewTextActivity.class), transitionBundle);
            dismiss();
        });
        binding.faxView.setOnClickListener( v -> {
            if (getActivity() == null){
                Toast.makeText(getContext(), "Unable to create fax, Activity is null", Toast.LENGTH_LONG).show();
            }
            NewFaxActivity.showNewFaxActivity(getActivity(), transitionBundle);
            dismiss();
        });
        d.setCancelable(true);
        d.setContentView(binding.getRoot());
        return d;
    }


}
