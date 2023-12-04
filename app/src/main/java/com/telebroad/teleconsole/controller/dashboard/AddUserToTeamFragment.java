package com.telebroad.teleconsole.controller.dashboard;

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
import com.bumptech.glide.Glide;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.SetMessage;
import com.telebroad.teleconsole.databinding.ChatAddUserToGroupBinding;
import com.telebroad.teleconsole.databinding.FragmentAddUserToTeamBinding;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.db.models.GroupMembers;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddUserToTeamFragment extends Fragment {
    private ChannelDB channelDB;
    private FragmentAddUserToTeamBinding binding;
    private List<GroupMembers> JoinedMembers;
    public static AddUserToTeamFragment newInstance(ChannelDB channelDB) {
        AddUserToTeamFragment fragment = new AddUserToTeamFragment();
        Bundle args = new Bundle();
        args.putSerializable("channelDB", channelDB);
        fragment.setArguments(args);
        return fragment;
    }


    public AddUserToTeamFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddUserToTeamBinding.inflate(inflater, container, false);
        Bundle args = getArguments();
        if (args != null) {
            channelDB = (ChannelDB) args.getSerializable("channelDB");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LiveData<List<GroupMembers>> groupMembersLiveData = ChatDatabase.getInstance().groupMembersDao().getAll(channelDB.getTopic());
        groupMembersLiveData.observe(getActivity(), groupMembers -> {
             JoinedMembers = groupMembers;
        });
        LiveData<List<ChannelDB>> directChannels = ChatDatabase.getInstance().channelDao().getDirectNotMetChannels();
        directChannels.observe(getActivity(), channelDBS -> {
            AddUserAdapter addUserAdapter = new AddUserAdapter(new DIFF_CALLBACK(),JoinedMembers);
            binding.rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
            binding.rvGroups.setAdapter(addUserAdapter);
            addUserAdapter.submitList(channelDBS);
        });

    }

    static class DIFF_CALLBACK extends DiffUtil.ItemCallback<ChannelDB>{

        @Override
        public boolean areItemsTheSame(@NonNull ChannelDB oldItem, @NonNull ChannelDB newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChannelDB oldItem, @NonNull ChannelDB newItem) {
            return oldItem.getTopic().equals(newItem.getTopic());
        }
    }
    class AddUserAdapter extends ListAdapter<ChannelDB,AddUserAdapter.RootViewHolder> {
        List<GroupMembers> JoinedTopics;

        protected AddUserAdapter(@NonNull DiffUtil.ItemCallback<ChannelDB> diffCallback,List<GroupMembers> JoinedMembers) {
            super(diffCallback);
            Set<GroupMembers> uniqueSet = new HashSet<>(JoinedMembers);
            this.JoinedTopics = new ArrayList<>(uniqueSet);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AddUserAdapter.RootViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AddUserAdapter.RootViewHolder(ChatAddUserToGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AddUserAdapter.RootViewHolder holder, int position) {
            ChannelDB channelDB1 = getCurrentList().get(position);
             if (channelDB1 != null && channelDB1.isGroup() != null && !channelDB1.isGroup() && channelDB1.getImageUrl() == null)
                holder.binding.flag.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(channelDB1.getName()));
            else if (getActivity() != null)Glide.with(getActivity()).load(channelDB1.getImageUrl()).circleCrop().into(holder.binding.flag);
            holder.binding.countryName.setText(channelDB1.getName());
            boolean isJoined = false;
            for (GroupMembers g:JoinedTopics) {
                if (g.getUser().equals(channelDB1.getTopic())) {
                   /// Log.d("topicJoined",g.getTopic());
                    isJoined = true;
                    break;
                }
            }
            if (isJoined) {
               holder.binding.imageView36.setVisibility(View.VISIBLE);
            } else {
                holder.binding.imageView36.setVisibility(View.GONE);
            }
            holder.binding.getRoot().setOnClickListener(v -> {
                SetMessage setMessage = new SetMessage();
                SetMessage.Sub sub = new SetMessage.Sub();
                sub.setUser(channelDB1.getTopic());
                setMessage.setId("newMember");
                setMessage.setSub(sub);
                setMessage.setTopic(channelDB.getTopic());
                ChatWebSocket.getInstance().sendObject("set",setMessage);
                ChatActivity.getDialog().dismiss();
            });

        }

        class RootViewHolder extends RecyclerView.ViewHolder {
            private ChatAddUserToGroupBinding binding;
            public RootViewHolder(@NonNull ChatAddUserToGroupBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}