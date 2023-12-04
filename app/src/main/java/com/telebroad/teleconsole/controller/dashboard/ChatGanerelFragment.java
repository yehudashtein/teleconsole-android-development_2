package com.telebroad.teleconsole.controller.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.SetMessage;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.databinding.FragmentChatGanerelBinding;

import java.util.HashMap;
import java.util.Map;


public class ChatGanerelFragment extends Fragment {
    private FragmentChatGanerelBinding binding;
    private ChannelDB channelDB;
    private SetMessage setMessage;

    public ChatGanerelFragment() {}

    public static ChatGanerelFragment newInstance(ChannelDB channelDB) {
        ChatGanerelFragment fragment = new ChatGanerelFragment();
        Bundle args = new Bundle();
        args.putSerializable("channelDB", channelDB);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatGanerelBinding.inflate(inflater, container, false);
        binding.btnCreate.setBackgroundColor(Color.BLACK);
        binding.btnCreate.setBackgroundResource(R.color.black);
        Bundle args = getArguments();
        if (args != null) {
            channelDB = (ChannelDB) args.getSerializable("channelDB");
        }
        return binding.getRoot();
    }
    @Override
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        LayoutInflater inflater = super.onGetLayoutInflater(savedInstanceState);
        Context contextThemeWrapper = new ContextThemeWrapper(requireContext(), R.style.AppTheme_CreateTeam);
        return inflater.cloneInContext(contextThemeWrapper);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.groupName.setText(channelDB.getName());
        binding.etName.setText(channelDB.getName());
        binding.etDescription.setText(channelDB.getDescription());
        if (channelDB.isNotPrivate()){
            binding.rbPrivacy.setChecked(false);
            binding.rbPublic.setChecked(true);
        }else {
            binding.rbPrivacy.setChecked(true);
            binding.rbPublic.setChecked(false);
        }
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rbPublic:
                            setMessage = new SetMessage();
                            Map<String,String> map1 = new HashMap<>();
                            map1.put("fn",binding.etName.getText().toString());
                            map1.put("description",binding.etDescription.getText().toString());
                            Map<String,String> defacsMap = new HashMap<>();
                            defacsMap.put("auth","JRWPSD");
                            defacsMap.put("anon","N");
                            SetMessage.Desc desc = new SetMessage.Desc();
                            desc.setPublicInfo(map1);
                            desc.setDefacs(defacsMap);
                            setMessage.setId("updateMyTeam");
                            setMessage.setTopic(channelDB.getTopic());
                            setMessage.setDesc(desc);
                            break;
                        case R.id.rbPrivacy:
                            setMessage = new SetMessage();
                            Map<String,String> map12 = new HashMap<>();
                            map12.put("fn",binding.etName.getText().toString());
                            map12.put("description",binding.etDescription.getText().toString());
                            Map<String,String> defacsMap1 = new HashMap<>();
                            defacsMap1.put("auth","RWPSD");
                            defacsMap1.put("anon","N");
                            SetMessage.Desc desc1 = new SetMessage.Desc();
                            desc1.setPublicInfo(map12);
                            desc1.setDefacs(defacsMap1);
                            setMessage.setId("updateMyTeam");
                            setMessage.setTopic(channelDB.getTopic());
                            setMessage.setDesc(desc1);
                            break;

                    }
        });
        binding.btnCreate.setOnClickListener(v -> {
            if (setMessage == null){
                setMessage = new SetMessage();
                Map<String,String> map12 = new HashMap<>();
                map12.put("fn",binding.etName.getText().toString());
                map12.put("description",binding.etDescription.getText().toString());
                Map<String,String> defacsMap1 = new HashMap<>();
                String acs = channelDB.getAcsMode().replace("O","");
                defacsMap1.put("auth",acs);
                defacsMap1.put("anon","N");
                SetMessage.Desc desc1 = new SetMessage.Desc();
                desc1.setPublicInfo(map12);
                desc1.setDefacs(defacsMap1);
                setMessage.setId("updateMyTeam");
                setMessage.setTopic(channelDB.getTopic());
                setMessage.setDesc(desc1);
                ChatWebSocket.getInstance().sendObject("set",setMessage);
            }else {
                ChatWebSocket.getInstance().sendObject("set",setMessage);
            }
            if (ChatActivity.getDialog() != null){
                ChatActivity.getDialog().dismiss();
            }
        });
    }
    //            FragmentManager fragmentManager = getParentFragmentManager();
//            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            transaction.remove(this); // 'this' refers to the current fragment
//            transaction.commit();
}