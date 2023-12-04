package com.telebroad.teleconsole.controller;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.telebroad.teleconsole.controller.dashboard.SearchAllFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchCallHistoryFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchContactsFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchFaxFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchSmsConversationsFragment;
import com.telebroad.teleconsole.controller.dashboard.SearchVoicemailsFragment;

public class SearchPagerAdapter extends FragmentStateAdapter  {
    public SearchPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return SearchAllFragment.newInstance();
            case 1:
                return SearchContactsFragment.newInstance();
            case 2:
                return SearchCallHistoryFragment.newInstance();
            case 3:
                return SearchSmsConversationsFragment.newInstance();
            case 4:
               return SearchFaxFragment.newInstance();
            case 5 :
                return SearchVoicemailsFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
