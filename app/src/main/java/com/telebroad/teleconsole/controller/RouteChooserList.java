package com.telebroad.teleconsole.controller;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.telebroad.teleconsole.databinding.DialogChooseAudioRouteBinding;
import com.telebroad.teleconsole.pjsip.AndroidAudioManager;

public class RouteChooserList extends BottomSheetDialogFragment {

    DialogChooseAudioRouteBinding binding;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Dialog d = super.onCreateDialog(savedInstanceState);
        binding = DialogChooseAudioRouteBinding.inflate(LayoutInflater.from(getContext()));

        binding.routeSpeakerView.setOnClickListener(v -> {
            AndroidAudioManager.getAudioManager().routeAudioToSpeaker();
            d.dismiss();
        });
        binding.bluetoothView.setOnClickListener(v -> {
            AndroidAudioManager.getAudioManager().routeAudioToBluetooth();
            d.dismiss();
        });
        binding.earpieceView.setOnClickListener(v -> {
            AndroidAudioManager.getAudioManager().routeAudioToEarPiece();
            d.dismiss();
        });
        d.setCancelable(true);
        d.setContentView(binding.getRoot());
        return d;
    }


}

