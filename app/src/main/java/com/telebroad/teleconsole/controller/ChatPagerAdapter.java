package com.telebroad.teleconsole.controller;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.telebroad.teleconsole.controller.dashboard.AddUserToTeamFragment;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.controller.dashboard.ChatGanerelFragment;
import com.telebroad.teleconsole.controller.dashboard.ChatMembersFragment;

public class ChatPagerAdapter extends FragmentStateAdapter {
    private final ChannelDB channelDB;

    public ChatPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, ChannelDB channelDB) {
        super(fragmentManager, lifecycle);
        this.channelDB = channelDB;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ChatGanerelFragment.newInstance(channelDB);
            case 1:
                return ChatMembersFragment.newInstance(channelDB);
            case 2:
                return AddUserToTeamFragment.newInstance(channelDB);
            default:
                //return  new ChatGanerelFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
