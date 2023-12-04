package com.telebroad.teleconsole.controller.dashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.DelMessage;
import com.telebroad.teleconsole.chat.client.GetMessage;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.db.models.GroupMembers;
import com.telebroad.teleconsole.chat.server.MetaMessage;
import com.telebroad.teleconsole.databinding.ChatFragmentMembersItemBinding;
import com.telebroad.teleconsole.databinding.FragmentChatMembersBinding;
import com.telebroad.teleconsole.helpers.SettingsHelper;

import java.util.ArrayList;
import java.util.List;


public class ChatMembersFragment extends Fragment  {
    private FragmentChatMembersBinding binding;
    static ChannelDB channelDB;
    List<MetaMessage.Sub> metaMessageList = new ArrayList<>();
    static ChatMembersFragment chatMembersFragment;

    public ChatMembersFragment() {
    }

//    public static ChatMembersFragment getInstance() {
//        if (chatMembersFragment == null){
//            chatMembersFragment = new ChatMembersFragment(channelDB);
//        }else {
//            return chatMembersFragment;
//        }
//        //ChatMembersFragment fragment = new ChatMembersFragment(ChannelDB channelDB);
//        return chatMembersFragment;
//    }
public static ChatMembersFragment newInstance(ChannelDB channelDB) {
    ChatMembersFragment fragment = new ChatMembersFragment();
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
        binding = FragmentChatMembersBinding.inflate(inflater, container, false);
        Bundle args = getArguments();
        if (args != null) {
            channelDB = (ChannelDB) args.getSerializable("channelDB");
            GetMessage getMessage = new GetMessage();
            getMessage.setId("UsersFromGroup");
            getMessage.setTopic(channelDB.getTopic());
            getMessage.setWhat("sub");
            ChatWebSocket.getInstance().sendObject("get",getMessage);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LiveData<List<GroupMembers>> groupMembersLiveData = ChatDatabase.getInstance().groupMembersDao().getAll(channelDB.getTopic());
        groupMembersLiveData.observe(getActivity(), groupMembers -> {
            MemberAdapter memberAdapter = new MemberAdapter(new DIFF_CALLBACK());
            memberAdapter.submitList(groupMembers);
            binding.rvMembers.setLayoutManager(new LinearLayoutManager(getActivity()));
            binding.rvMembers.setAdapter(memberAdapter);
        });
    }

    static class DIFF_CALLBACK extends DiffUtil.ItemCallback<GroupMembers>{

        @Override
        public boolean areItemsTheSame(@NonNull GroupMembers oldItem, @NonNull GroupMembers newItem) {
            return oldItem.equals(newItem);
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull GroupMembers oldItem, @NonNull GroupMembers newItem) {
            return oldItem.getFn().equals(newItem.getFn());
        }
    }
    static class  MemberAdapter extends ListAdapter<GroupMembers, MemberAdapter.ViewHolder> {
        //List<MetaMessage.Sub> subList;


        @Override
        public void submitList(@Nullable List<GroupMembers> list) {
            super.submitList(list);
        }

        protected MemberAdapter(@NonNull DiffUtil.ItemCallback<GroupMembers> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ChatFragmentMembersItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.binding.flag.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(getCurrentList().get(position).getFn()));
            holder.binding.countryName.setText(getCurrentList().get(position).getFn());
            if (getCurrentList().get(position).getNickname() != null){
                holder.binding.txtNick.setVisibility(View.VISIBLE);
                holder.binding.txtNick.setText(getCurrentList().get(position).getNickname());
            }else holder.binding.txtNick.setVisibility(View.GONE);
            holder.binding.imageView36.setOnClickListener(v -> {
                DelMessage delMessage = new DelMessage();
                delMessage.setId("RemoveUser");
                delMessage.setTopic(getCurrentList().get(position).getTopic());
                delMessage.setUser(getCurrentList().get(position).getUser());
                delMessage.setWhat("sub");
                ChatWebSocket.getInstance().sendObject("del",delMessage);
                //Utils.asyncTask(() -> ChatDatabase.getInstance().groupMembersDao().deleteUser(getCurrentList().get(position).getTopic(),getCurrentList().get(position).getUser()));
            });
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ChatFragmentMembersItemBinding binding;
            public ViewHolder(@NonNull ChatFragmentMembersItemBinding binding ) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}