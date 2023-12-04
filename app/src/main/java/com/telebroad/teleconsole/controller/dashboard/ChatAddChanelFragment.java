package com.telebroad.teleconsole.controller.dashboard;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.setMassage;
import com.telebroad.teleconsole.databinding.ChatAddChanelFragmentBinding;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatAddChanelFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        com.telebroad.teleconsole.databinding.ChatAddChanelFragmentBinding binding = ChatAddChanelFragmentBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        binding.CreateNewTeam.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(),ChatNewTeamActivity.class);
            startActivity(intent);
            dismiss();
        });
        binding.JoinTeam.setOnClickListener(v -> {
            Map<String,String> map = new LinkedHashMap<>();
            map.put("public","_type=grp");
            setMassage setMessage = new setMassage();
            setMessage.setId("onlyGrp");
            setMessage.setTopic("fnd");
            setMessage.setDesc(map);
            ChatWebSocket.getInstance().sendObject("set",setMessage);
            dismiss();
        });
        binding.StartDirectChat.setOnClickListener(v -> {
            Map<String,String> map = new LinkedHashMap<>();
            map.put("public","_type=p2p");
            setMassage setMessage = new setMassage();
            setMessage.setId("onlyP2p");
            setMessage.setTopic("fnd");
            setMessage.setDesc(map);
            ChatWebSocket.getInstance().sendObject("set",setMessage);
            dismiss();
        });
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
