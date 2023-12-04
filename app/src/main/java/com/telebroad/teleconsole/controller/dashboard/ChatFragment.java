package com.telebroad.teleconsole.controller.dashboard;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SearchView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.SetMessage;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.chat.models.Channel;
import com.telebroad.teleconsole.chat.viewModels.ChatViewModel;
import com.telebroad.teleconsole.databinding.FragmentChat2Binding;
import com.telebroad.teleconsole.databinding.ItemChannelBinding;
import com.telebroad.teleconsole.databinding.NotificationItemBinding;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {
    private FragmentChat2Binding binding;
    private ConnectivityManager connectivityManager;
    private ChannelAdapter teamAdapter;
    private ChannelAdapter directAdapter;
    private ConnectivityManager.NetworkCallback networkCallback;
    private String query ="";
    private SearchView searchView;

    public ChatFragment() {}
    public ChatFragment newInstance() {return new ChatFragment();}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.chat_toolbar,menu);
    }
//@Override
//public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
//    inflater.inflate(R.menu.chat_toolbar,menu);
//    MenuItem searchItem=menu.findItem(R.id.app_bar_search);
//    searchView=(SearchView)searchItem.getActionView();
//    searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//        @Override
//        public boolean onMenuItemActionExpand(MenuItem item) {
//            searchView.setIconified(false);
//            String searchValue = searchView.getQuery().toString();
//            return true;
//        }
//
//        @Override
//        public boolean onMenuItemActionCollapse(MenuItem item) {
//            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
//            ChatViewModel.getInstance().getLiveDirectChannels().observe(getViewLifecycleOwner(), directAdapter::submitList);
//            ChatViewModel.getInstance().getLiveTeamChannels().observe(getViewLifecycleOwner(), teamAdapter::submitList);
//            return true;
//        }
//    });
//    if (searchView != null) {
//        searchView.setOnCloseListener(() -> {
//            searchItem.collapseActionView();
//            return true;
//        });
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                List<ChannelDB> adapterList1 = new ArrayList<>();
//                for (ChannelDB s1:teamAdapter.getCurrentList()){
//                    if (s1.getName().toLowerCase().contains(newText.toLowerCase().toLowerCase())){
//                        adapterList1.add(s1);
//                    }
//                }
//                teamAdapter.submitList(adapterList1);
//                List<ChannelDB> adapterList2 = new ArrayList<>();
//                for (ChannelDB s1:directAdapter.getCurrentList()){
//                    if (s1.getName().toLowerCase().contains(newText.toLowerCase().toLowerCase())){
//                        adapterList2.add(s1);
//                    }
//                }
//                directAdapter.submitList(adapterList2);
//                return true;
//            }
//        });
//        searchView.setQueryHint(getActivity().getResources().getString(R.string.search_contacts));
//    }
//}
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        searchManager.startSearch(initialQuery, selectInitialQuery, requireActivity().getComponentName(), appSearchData, globalSearch);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_search:
                Intent intent = new Intent(requireActivity(),SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.mute:
                Map<String, String> descMap = new HashMap<>();
                descMap.put("notifications","none");
                SetMessage.Desc desc = new SetMessage.Desc();
                desc.setPrivateInfo(descMap);
                SetMessage setMessage = new SetMessage();
                setMessage.setId("mute");
                setMessage.setTopic("me");
                setMessage.setDesc(desc);
                ChatWebSocket.getInstance().sendObject("set", setMessage);
                Toast.makeText(requireActivity(), "Notification's is Muted", Toast.LENGTH_SHORT).show();
                break;
            case R.id.Unmute:
                Map<String, String> descMap1 = new HashMap<>();
                descMap1.put("notifications", "message");
                SetMessage.Desc desc1 = new SetMessage.Desc();
                desc1.setPrivateInfo(descMap1);
                SetMessage setMessage1 = new SetMessage();
                setMessage1.setId("mute");
                setMessage1.setTopic("me");
                setMessage1.setDesc(desc1);
                ChatWebSocket.getInstance().sendObject("set", setMessage1);
                Toast.makeText(requireActivity(),"Notification's is UnMuted", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.addChanel.setOnClickListener(v -> {
            new ChatAddChanelFragment().show(getActivity(). getSupportFragmentManager(),"");
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //new ChatWebSocket();
        ChatWebSocket.getInstance().connect();
        connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChat2Binding.inflate(inflater, container, false);
//        binding.directView.setBackground(Utils.getRipple(requireActivity()));
//        binding.teamView.setBackground(Utils.getRipple(requireActivity()));
        Utils.setRippleBackground(binding.directView, requireActivity());
        Utils.setRippleBackground(binding.teamView, requireActivity());
        Drawable myFabSrc = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_message_24,null);
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
        willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        binding.addChanel.setImageDrawable(willBeWhite);
        //binding = FragmentChatBinding.inflate(inflater, container, false);
        //notificationItemBinding = NotificationItemBinding.bind(binding.getRoot());
        networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull Network network) {
                if (!ChatWebSocket.isConnected){
                    //ChatWebSocket.getInstance().connect();
                }
            }
        };
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
        binding.teamRoot.setOnClickListener(view -> {
            if (binding.teamRecycler.getVisibility() == View.VISIBLE) {
                binding.teamRecycler.setVisibility(View.GONE);
                Animation rotate = AnimationUtils.loadAnimation(getContext(),R.anim.conter_clockwise);
                rotate.setRepeatCount(Animation.ABSOLUTE);
                binding.dropdownbutton.startAnimation(rotate);
            } else {
                Animation rotate1 = AnimationUtils.loadAnimation(getContext(),R.anim.clockwisw);
                rotate1.setRepeatCount(Animation.ABSOLUTE);
                binding.dropdownbutton.startAnimation(rotate1);
                binding.teamRecycler.setVisibility(View.VISIBLE);
            }
        });
        teamAdapter = new ChannelAdapter(new ChannelDiff());
        binding.teamRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.teamRecycler.setAdapter(teamAdapter);
        directAdapter = new ChannelAdapter(new ChannelDiff());
        binding.directRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.directRecycler.setAdapter(directAdapter);
        ChatViewModel.getInstance().getLiveDirectChannels().observe(getViewLifecycleOwner(), directAdapter::submitList);
        ChatViewModel.getInstance().getLiveTeamChannels().observe(getViewLifecycleOwner(), teamAdapter::submitList);
        binding.directRoot.setOnClickListener(view -> {
            if (binding.directRecycler.getVisibility() == View.VISIBLE) {
                binding.directRecycler.setVisibility(View.GONE);
                Animation rotate = AnimationUtils.loadAnimation(getContext(),R.anim.conter_clockwise);
                rotate.setRepeatCount(Animation.ABSOLUTE);
                binding.directDropdownbutton.startAnimation(rotate);
            } else {
                binding.directRecycler.setVisibility(View.VISIBLE);
                Animation rotate1 = AnimationUtils.loadAnimation(getContext(),R.anim.clockwisw);
                rotate1.setRepeatCount(Animation.ABSOLUTE);
                binding.directDropdownbutton.startAnimation(rotate1);
            }
        });
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    public static class ChannelDiff extends DiffUtil.ItemCallback<ChannelDB> {
        @Override
        public boolean areItemsTheSame(@NonNull ChannelDB oldItem, @NonNull ChannelDB newItem) {
            return oldItem.getTopic().equals(newItem.getTopic());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChannelDB oldItem, @NonNull ChannelDB newItem) {
            if ( oldItem.getImageUrl() != null && newItem.getImageUrl() != null &&!oldItem.getImageUrl().equals(newItem.getImageUrl())) return false;
            if (!oldItem.getName().equals(newItem.getName()))return false;
            return oldItem.isOnline() == newItem.isOnline();
        }
    }

    public class ChannelAdapter extends ListAdapter<ChannelDB, ChannelAdapter.ViewHolder> {
        protected ChannelAdapter(@NonNull DiffUtil.ItemCallback<ChannelDB> diffCallback) {
            super(diffCallback);
        }

        @Override
        public void submitList(@Nullable List<ChannelDB> list) {
            List<ChannelDB> tempList = list == null ? new ArrayList<>() : new ArrayList<>(list);
            tempList.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            super.submitList(tempList);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemChannelBinding itemChannelBinding = ItemChannelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemChannelBinding);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Channel channel = getCurrentList().get(position);
            //android.util.Log.d("Channel", channel.getName() + " " + channel.isGroup() + " " + channel.isNotPrivate());
            LiveData<Integer>  status = ChatDatabase.getInstance().channelDao().getStatus(channel.getTopic());
            status.observe(getViewLifecycleOwner(), (statusInt) -> {
                if (statusInt!= null){
                    if (statusInt.equals(1)) {
                        holder.binding.onlineView.setVisibility(View.VISIBLE);
                        holder.binding.onlineView.setColorFilter(getContext().getResources().getColor(R.color.onlineGreen,null)  );
                    } else {
                        holder.binding.onlineView.setVisibility(View.VISIBLE);
                        holder.binding.onlineView.setColorFilter(getContext().getResources().getColor(R.color.offlineGray,null) );
                    }
                }
            });
            ChatDatabase.getInstance().channelDao().getAcsModeForFnd(getCurrentList().get(position).getTopic()).observe(getActivity(), channelDB -> {
                if (channelDB != null && isAdded()) {
                    if (channelDB.getAcsMode().toLowerCase().contains("j")) {
                        holder.binding.onlineView.setVisibility(View.GONE);
                        Drawable myDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_outline_group_24, null);
                        holder.binding.avatar.setBackground(myDrawable);
                    } else {
                        holder.binding.onlineView.setVisibility(View.GONE);
                        Drawable myDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_outline_lock_24, null);
                        holder.binding.avatar.setBackground(myDrawable);
                    }
                }
            });
            if (channel.getImageUrl() == null && !channel.isGroup())
                holder.binding.avatar.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(channel.getName()));
             else if (channel.getImageUrl() != null && !channel.isGroup())
                 Glide.with(getContext()).load(getImageDrawable(channel)).circleCrop().into(holder.binding.avatar);
            LiveData<Integer> unreadNumber = ChatDatabase.getInstance().channelDao().getUnreadNumberByTopic(channel.getTopic());
            unreadNumber.observe(getViewLifecycleOwner(), (unreadInt) -> {
                if (unreadInt != null){
                    holder.notificationItemBinding.txtUnread.setText(String.valueOf(unreadInt));
                    if (unreadInt.equals(0)) {
                        holder.binding.channelNameTextView.setTypeface(Typeface.DEFAULT);
                        holder.notificationItemBinding.txtUnread.setVisibility(View.GONE);
                    } else if (unreadInt > 0) {
                        holder.binding.channelNameTextView.setTypeface(null, Typeface.BOLD);
                        holder.notificationItemBinding.txtUnread.setVisibility(View.VISIBLE);
                    }
                };});
            //holder.notificationItemBinding.txtUnread.setVisibility( channel.getUnread() > 0 ? View.VISIBLE : View.GONE);
            //return getResources().getDrawable(R.drawable.ic_contacts);
            //holder.binding.avatar.(getImageDrawable(channel));
            holder.binding.channelNameTextView.setText(channel.getName());
            holder.binding.getRoot().setOnClickListener(view -> {
                ChatWebSocket.getInstance().subscribe(channel.getTopic());
//                GetMessage getMessage = new GetMessage();
//                getMessage.setId("getTopicSeq");
//                getMessage.setWhat("desc");
//                getMessage.setTopic( channel.getTopic());
//                ChatWebSocket.getInstance().sendObject("get",getMessage);
                Intent intent = new Intent(ChatFragment.this.getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.CURRENT_CHAT_EXTRA, channel.getTopic());
                ChatFragment.this.getActivity() .startActivity(intent);
                Bundle bndlanimation = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.slide_in_right,R.anim.slide_out_left).toBundle();
            });
        }
        private String getImageDrawable(Channel channel) {
            return channel.getImageUrl();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final ItemChannelBinding binding;
            private final NotificationItemBinding notificationItemBinding;
            private ViewHolder(ItemChannelBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                notificationItemBinding = NotificationItemBinding.bind(this.binding.getRoot());
            }
        }
    }

   /* public long hashCode1(String stringInput) {
        long h = 0;
        final int len = stringInput.length();
        if (h == 0 && len > 0) {
            for (int i = 0; i < len; i++) {
                h = 31 * h + stringInput.charAt(i);
            }
        }
        return h;
    }*/

//    public int getBackgroundColor(String stringInput) {
////        int hash = stringInput.chars().reduce((acc, cha) => {
////            return cha + ((acc << 5) - acc);
////        }, 0);))
//        //stringInput.hashCode();
//        int hue = stringInput.hashCode() % 360;
//        //double hue = hashCode1(stringInput) % 360;
//        return ColorUtils.HSLToColor(new float[]{(float) hue, 0.95f, 0.35f});
////        return "hsl(" + (hash % 360) + ", 95%, 35%)";
//    }
    public static String getBackgroundColor(String stringInput) {
        int stringUniqueHash = stringInput.chars()
                .reduce(0, (acc, c) -> c + ((acc << 5) - acc));
        return String.format("hsl(%d, 95%%, 35%%)", stringUniqueHash % 360);
    }
    private static float[] parseHSL(String hsl) {
        String[] parts = hsl.split(",");
        float hue = Float.parseFloat(parts[0].substring(parts[0].indexOf("(") + 1));
        float saturation = Float.parseFloat(parts[1].replaceAll("%", "")) / 100f;
        float lightness = Float.parseFloat(parts[2].replaceAll("[^\\d.]", "")) / 100f;
        return new float[]{hue, saturation, lightness};
    }
//    }

    private static int hslToRgb(float hue, float saturation, float lightness) {
        float chroma = (1 - Math.abs(2 * lightness - 1)) * saturation;
        float huePrime = hue / 60f;
        float x = chroma * (1 - Math.abs(huePrime % 2 - 1));
        float r, g, b;
        if (huePrime >= 0 && huePrime < 1) {
            r = chroma;
            g = x;
            b = 0;
        } else if (huePrime >= 1 && huePrime < 2) {
            r = x;
            g = chroma;
            b = 0;
        } else if (huePrime >= 2 && huePrime < 3) {
            r = 0;
            g = chroma;
            b = x;
        } else if (huePrime >= 3 && huePrime < 4) {
            r = 0;
            g = x;
            b = chroma;
        } else if (huePrime >= 4 && huePrime < 5) {
            r = x;
            g = 0;
            b = chroma;
        } else {
            r = chroma;
            g = 0;
            b = x;
        }
        float m = lightness - chroma / 2f;
        int red = Math.round((r + m) * 255);
        int green = Math.round((g + m) * 255);
        int blue = Math.round((b + m) * 255);
        return Color.rgb(red, green, blue);
    }


}
    /*public static class Color1 {
        public static String getBackgroundColor(String stringInput) {
            int stringUniqueHash = stringInput.chars().reduce(
                    (acc, char) -> char + ((acc << 5) - acc),
                    0);
            return "hsl(" + (stringUniqueHash % 360) + ", 95%, 35%)";
        }
    }*/



