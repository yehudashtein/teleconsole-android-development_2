package com.telebroad.teleconsole.controller.dashboard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.FaxOpenActivity;
import com.telebroad.teleconsole.controller.SmsConversationActivity;
import com.telebroad.teleconsole.controller.VoicemailOpenActivity;
import com.telebroad.teleconsole.databinding.ItemMessageBinding;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.IntentHelper;
import com.telebroad.teleconsole.model.DlrUpdate;
import com.telebroad.teleconsole.model.Fax;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PubnubInfo;
import com.telebroad.teleconsole.model.SMS;
import com.telebroad.teleconsole.model.Voicemail;
import com.telebroad.teleconsole.model.repositories.FaxRepository;
import com.telebroad.teleconsole.model.repositories.SMSRepository;
import com.telebroad.teleconsole.viewmodels.FaxViewModel;
import com.telebroad.teleconsole.viewmodels.MessageListViewModel;
import com.telebroad.teleconsole.viewmodels.MessageViewModel;
import com.telebroad.teleconsole.viewmodels.SMSViewModel;
import com.telebroad.teleconsole.viewmodels.VoicemailViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static com.telebroad.teleconsole.helpers.ListPopupWindowHelper.addItem;
import static com.telebroad.teleconsole.model.repositories.MessageRepository.MessageListType.ALL;
import static com.telebroad.teleconsole.model.repositories.MessageRepository.MessageListType.FAX;
import static com.telebroad.teleconsole.model.repositories.MessageRepository.MessageListType.SMS;
import static com.telebroad.teleconsole.model.repositories.MessageRepository.MessageListType.VOICEMAIL;

public class MessagesFragment extends Fragment implements ScrollableFragment {
    private static MessagesFragment instance;
    private MessageListViewModel messageListViewModel;
    private FloatingActionButton addFab;
    private MessagesAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private LiveData<? extends List<? extends Message>> activeList;
    private static final DiffUtil.ItemCallback<MessageViewModel<? extends Message>> diffCallback = new DiffUtil.ItemCallback<MessageViewModel<? extends  Message>>() {
        @Override
        public boolean areItemsTheSame(@NonNull MessageViewModel oldItem, @NonNull MessageViewModel newItem) {
            return oldItem.getItem().getId().equals(newItem.getItem().getId()) && oldItem.getClass().equals(newItem.getClass());
        }

        @Override
        public boolean areContentsTheSame(@NonNull MessageViewModel oldItem, @NonNull MessageViewModel newItem) {
            // Messages are immutable, therefore if they are the same items, the content is also the same.
            return areItemsTheSame(oldItem, newItem);
        }
    };

    public final Consumer<JsonObject> FAX_DLR_LISTENER = object -> {
        DlrUpdate dlrupdate = new Gson().fromJson(object, DlrUpdate.class);
        FaxRepository.getInstance().updateDLR(dlrupdate);
//        SMSRepository.getInstance().loadConversationFromServer(myNumber, otherNumber, null);
      //  android.util.Log.d("DLR_Running", "FAX DLR IS RUNNING " + object.toString());
//        adapter.updateDLR(dlrupdate);
    };
    public MessagesFragment() {}

    private boolean isFiltering(){
        return searchView != null && searchView.getQuery().length() > 2;
    }

    public static MessagesFragment getInstance() {
        if (instance == null) {
            instance = new MessagesFragment();
        }
        return instance;
    }

    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        messageListViewModel = ViewModelProviders.of(this).get(MessageListViewModel.class);
        messageListViewModel.refresh();
        String chatChannel = PubnubInfo.getInstance() == null ? null : PubnubInfo.getInstance().getChat();
        if (chatChannel  != null && !chatChannel.isEmpty()){
            PubnubInfo.getInstance().addListener(chatChannel, FAX_DLR_LISTENER);
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.messages, menu);
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        searchView = (SearchView) searchItem.getActionView();
//        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                searchView.setIconified(false);
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                if (getActivity() == null) {
//                    return true;
//                }
//                if (Utils.hideKeyboard(getActivity(), searchView)) return true;
//                searchView.setQuery("", false);
//                return true;
//            }
//        });
//        if (searchView == null){
//            return;
//        }
//        searchView.setOnCloseListener(() -> {
//            searchItem.collapseActionView();
//            searchView.setQuery("", false);
//            return false;
//        });
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                if (Strings.isNullOrEmpty(newText)){
//                    stopFiltering();
//                }else {
//                    startFiltering();
//                    // TODO Replace with Call History
//                }
//                return false;
//            }
//        });
//        searchView.setQueryHint(getString(R.string.search_message));
    }

    private void stopFiltering() {
        adapter.submitList(adapter.messageViewModels);
    }

    private void startFiltering() {
        adapter.filter(searchView.getQuery().toString());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        activeList.removeObservers(this);
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(requireActivity(),SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.action_all:
                activeList = messageListViewModel.getMessageList(ALL);
                break;
            case R.id.action_fax:
                activeList = messageListViewModel.getMessageList(FAX);
                break;
            case R.id.action_voicemail:
                activeList = messageListViewModel.getMessageList(VOICEMAIL);
                break;
            case R.id.action_sms:
                activeList = messageListViewModel.getMessageList(SMS);
                break;
        }
        activeList.observe(this, adapter::setMessages);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages_list, container, false);
        //android.util.Log.d("MFrag01", "Creating view");
        addFab = view.findViewById(R.id.add_message_fab);
        addFab.setOnClickListener(v -> {
            List<HashMap<String, Object>> data = new ArrayList<>();
            data.add(addItem(getString(R.string.send_text), R.drawable.ic_messages));
            data.add(addItem(getString(R.string.send_fax), R.drawable.ic_fax));
            new NewMessageDialog().show(getActivity().getSupportFragmentManager(), "new_text");
            //  new ContactSaveLocationDialog().show(getActivity().getSupportFragmentManager(), "contacts");
//            setupListPopupWindow(getContext(), view.findViewById(R.id.anchor_view), data, addFab, (parent, view1, position, id) -> {
//                // position is 0 which is send text
//                if (position == 0) {
//                    startActivity(new Intent(getActivity(), NewTextActivity.class));
//                }else if (position == 1){
//                    // position is 1 which is send fax
//                    NewFaxActivity.showNewFaxActivity(getActivity());
//                }
//            });
        });
        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new MessagesAdapter();
        //android.util.Log.d("LiveData02", "observing " + activeList);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(messageListViewModel::refresh);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        activeList = messageListViewModel.getMessageList(ALL);
        if (!activeList.hasActiveObservers()){
            activeList.observe(this, adapter::setMessages);
        }
        //android.util.Log.d("MFrag01", "resuming is view model null " + (messageListViewModel == null));
        if (messageListViewModel != null){
            messageListViewModel.refresh();
        }
    }

    @Override
    public void onPause(){
        activeList.removeObservers(this);
        //android.util.Log.d("MFrag01", "Pausing ");
        super.onPause();
    }

    @Override
    public RecyclerView recyclerView() {
        return recyclerView;
    }
    class MessagesAdapter extends ListAdapter<MessageViewModel<? extends Message>, MessagesAdapter.ViewHolder> {
        private List<? extends Message> messages;
        private List<MessageViewModel<? extends Message>> messageViewModels = new ArrayList<>();
        @NonNull
        private List<MessageViewModel<? extends Message>> selectedMessageModels = new ArrayList<>();
        private boolean isSelectedMode = false;
        private ActionMode selectedMode;
        private final ActionMode.Callback selectedModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.messages_selected, menu);
//                Drawable upArrow = ContextCompat.getDrawable(requireActivity(), R.drawable.abc_ic_ab_back_material);
//                if (upArrow != null) {
//                    upArrow.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_ATOP);
//                    //mode.setNavigationIcon(upArrow);
//                }
                menu.findItem(R.id.copy).setVisible(false);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete){
                    String suffix = selectedMessageModels.size() == 1 ? "" : "s";
                    AlertDialog alert = new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("Delete " + selectedMessageModels.size() + " Message" + suffix + "?")
                           // .setMessage("Are you sure you want to delete " + selectedMessageModels.size() + " message" + suffix +  " and/or conversation" + suffix )
                            .setMessage(getSelectedMessagesDeleteMessage())
                            .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                                for(MessageViewModel<? extends Message> model : selectedMessageModels){
//                                    notifyItemRemoved(getCurrentList().indexOf(model));
                                    messageViewModels.remove(model);
                                    model.deleteFromList();
                                }
                                selectedMode.finish();
//                                submitList(messageViewModels);
                                notifyDataSetChanged();
                                dialog.dismiss();
                            })).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
                    alert.setOnShowListener(dialog -> {
                        Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                        negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                    });alert.show();
                }
                return false;
            }
            private String getSelectedMessagesDeleteMessage(){
                // WARNING AHEAD!! CODE WAS WRITTEN UNDER SHORT DEADLINE! THIS IS BAD BAD CODE!
                // THIS CODE IS EXTREMLEY WET. I MIGHT DRY IT OUT IN THE FUTURE.
                 int smsCount = 0;
                int voicemailCount = 0;
                int faxCount = 0;
                for (MessageViewModel model : selectedMessageModels){
                    if (model instanceof SMSViewModel){
                        smsCount++;
                        continue;
                    }
                    if(model instanceof FaxViewModel){
                        faxCount++;
                        continue;
                    }
                    if(model instanceof  VoicemailViewModel){
                        voicemailCount++;
                    }
                }
                int types = Math.min(smsCount, 1) + Math.min(voicemailCount, 1) +  Math.min(faxCount, 1);
                String firstConjuctrue = types <= 1 ? "" : types == 2 ? " and " : ", ";
                String secondConjucture = types <=2 ? "" : types == 3 ? ", and " : " and ";
                String smsString = getStringForType("conversation", "s", smsCount);
                String faxString = getStringForType("fax", "es", faxCount);
                String voicemailString = getStringForType("voicemail", "s", voicemailCount);
                String firstPart = "", secondPart = "", thirdPart = "";
                int smsRank = 3, faxRank = 3, voicemailRank = 3;
                if (smsCount < faxCount){
                    faxRank--;
                }else{
                    smsRank--;
                }
                if(smsCount < voicemailCount){
                    voicemailRank--;
                }else{
                    smsRank--;
                }
                if (voicemailCount < faxCount){
                    faxRank--;
                }else{
                    voicemailRank--;
                }
                firstPart = smsRank == 1 ? smsString : voicemailRank == 1 ? voicemailString : faxRank == 1 ? faxString : "";
                secondPart = smsRank == 2 ? smsString : voicemailRank == 2 ? voicemailString : faxRank == 2 ? faxString : "";
                thirdPart = smsRank == 3 ? smsString : voicemailRank == 3 ? voicemailString : faxRank == 3 ? faxString : "";
                return "Are you sure you want to delete " + firstPart + firstConjuctrue + secondPart + secondConjucture + thirdPart + "?";
            }

            private String getStringForType(String main, String suffix, int count){
                if (count == 0){
                    return "";
                }
                if (count == 1){
                    return selectedMessageModels.size() == 1 ? "this " + main : "1 " + main;
                }else{
                    return count + " " + main + suffix;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                setSelectedMode(false);
                selectedMessageModels.clear();
                notifyDataSetChanged();
            }
        };

        public MessagesAdapter() {
            super(diffCallback);
        }

        private void setSelectedMode(boolean on) {
            isSelectedMode = on;
            if (isSelectedMode){
                selectedMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(selectedModeCallback);
            }else{
                selectedMode.finish();
            }
        }

        private void update(MessageViewModel update){
            if (selectedMessageModels.contains(update)){
                selectedMessageModels.remove(update);
                if (selectedMessageModels.isEmpty() && selectedMode != null){
                    setSelectedMode(false);
                }
            }else{
                if (selectedMessageModels.isEmpty()){
                    setSelectedMode(true);
                }
                selectedMessageModels.add(update);
            }
            if (selectedMode != null){
                selectedMode.setTitle(selectedMessageModels.size() + " Selected");
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (messages != null) {
                Message current;
                MessageViewModel currentViewModel = getCurrentList().get(position);
                holder.setMessageViewModel(currentViewModel);
                if (currentViewModel.isNew()) {
                    holder.binding.nameTextView.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.binding.infoTextView.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.binding.timeTextView.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.binding.nameTextView.setTextColor(getResources().getColor(R.color.unread,null));
                    holder.binding.infoTextView.setTextColor(getResources().getColor(R.color.unread,null));
                    holder.binding.timeTextView.setTextColor(getResources().getColor(R.color.unread,null));
                }else{
                    holder.binding.nameTextView.setTypeface(Typeface.DEFAULT);
                    holder.binding.infoTextView.setTypeface(Typeface.DEFAULT);
                    holder.binding.timeTextView.setTypeface(Typeface.DEFAULT);
                    holder.binding.nameTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
                    holder.binding.infoTextView.setTextColor(getResources().getColor(currentViewModel.getStatusColor(),null));
                    holder.binding.timeTextView.setTextColor(getResources().getColor(R.color.colorPrimary,null));
                }
                AsyncTask.execute(() -> currentViewModel.checkIfNeedToLoadMore());
            }
        }

        @Override
        public int getItemCount() {
            if (getCurrentList() != null) {
//                int itemCount = isFiltering() ? filteredMessageModels.size() : messages.size();
//                return itemCount;
                return getCurrentList().size();
            }
            //android.util.Log.d("Match03", "get item count messages zero");
            return 0;
        }

//        private void prepareSearch(){
//            for (MessageViewModel model : messageViewModels){
//                AsyncTask.execute(() -> android.util.Log.d("match02", model.getOtherName().getValue() + ""));
//            }
//        }

        private void filter(String query) {
            if (query.length() < 2) {
                return;
            }
            List<MessageViewModel<? extends Message>> filteredMessageModels = new ArrayList<>();
            //            filteredMessageModels.clear();
            for (MessageViewModel<? extends Message> model : messageViewModels) {
                if (model.matches(query)) {
                    filteredMessageModels.add(model);
                }
            }
            submitList(filteredMessageModels);
            notifyDataSetChanged();
        }

        public void setMessages(List<? extends Message> messages) {
            swipeRefreshLayout.setRefreshing(false);
            if (messages != null) {
                this.messages = messages;
                Collections.sort(messages);
                mapMessagesToModels(messages);
                if (messages.isEmpty()) {
                    recyclerView.setBackgroundResource(R.drawable.bg_no_messages);
                } else {
                    TypedValue typedValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
                    int backgroundResId = typedValue.resourceId;
                    recyclerView.setBackgroundResource(backgroundResId);
                    // recyclerView.setBackgroundResource(R.drawable.bg_rectangle_white_ripple);
                }
                if (isFiltering()){
                    filter(searchView.getQuery().toString());
                }else{
                    requireActivity().runOnUiThread(() -> submitList(messageViewModels));
                }
                notifyDataSetChanged();
            } else {
                //android.util.Log.d("Feature0001", "this is null");
            }
        }

        private void mapMessagesToModels(List<? extends Message> messages){
            messageViewModels.clear();
            for (Message message : messages){
                if (message instanceof Fax) {
                    FaxViewModel faxViewModel = new FaxViewModel();//ViewModelProviders.of(MessagesFragment.this).get(message.getId(), FaxViewModel.class);
                    faxViewModel.setItem((Fax) message);
                    messageViewModels.add(faxViewModel);
                } else if (message instanceof Voicemail) {
                    VoicemailViewModel voicemailViewModel = new VoicemailViewModel(); //ViewModelProviders.of(MessagesFragment.this).get(message.getId(), VoicemailViewModel.class);
                    voicemailViewModel.setItem((Voicemail) message);
                    messageViewModels.add(voicemailViewModel);
                } else if (message instanceof SMS) {
                    SMS smsCurrent = (SMS) message;
                    SMSViewModel smsViewModel = new SMSViewModel(); //ViewModelProviders.of(MessagesFragment.this).get(String.valueOf(smsCurrent.getTimeStamp()), SMSViewModel.class);
                    smsViewModel.setItem(smsCurrent);
                    messageViewModels.add(smsViewModel);
                }
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener {
            private final ItemMessageBinding binding;
            private String id;
            private MessageViewModel item;
            //private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
            private boolean selected;

            ViewHolder(ItemMessageBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                this.itemView.setOnClickListener(this);
                this.itemView.setOnLongClickListener(this);
            }

            void bind(MessageViewModel item) {
                //android.util.Log.d("LiveData01", "binding");
                this.id = item.getID();
                binding.setViewmodel(item);
                binding.executePendingBindings();
                binding.setLifecycleOwner(getViewLifecycleOwner());
               updateBackground();
            }

            private void updateBackground() {
                TypedValue typedValue = new TypedValue();
                requireActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
                int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    if (selectedMessageModels.contains(item)){
                        this.itemView.setBackgroundResource(R.color.oldColorPrimaryLight);
                    }else {
                        if (typedValue.resourceId != 0) {
                            this.itemView.setBackgroundResource(typedValue.resourceId);
                        } else {
                            this.itemView.setBackgroundColor(typedValue.data);
                        }
                    }
                }else {
                    if (item != null && selectedMessageModels.contains(item)) {
                        //this.itemView.setBackgroundResource(selectedMessageModels.contains(item) ? R.color.oldColorPrimaryLight : R.drawable.bg_rectangle_white_ripple);
                        this.itemView.setBackgroundResource( R.color.oldColorPrimaryLight);
                    }else {
                        if (typedValue.resourceId != 0) {
                            this.itemView.setBackgroundResource(typedValue.resourceId);
                        } else {
                            this.itemView.setBackgroundColor(typedValue.data);
                        }
                    }
                }
            }

            void setMessageViewModel(MessageViewModel viewModel) {
                //android.util.Log.d("LiveData01", "setting viewmodel");
                this.item = viewModel;
                bind(item);
            }

            @Override
            public boolean onLongClick(View v) {
                update(item);
                updateBackground();
                return true;
            }

            @Override
            public void onClick(View v) {
                if (isSelectedMode){
                   update(item);
                   updateBackground();
                }else if (id != null && getActivity() != null && addFab.getVisibility() == View.VISIBLE) {;
                    Intent intent;
                    if (item instanceof FaxViewModel) {
                        intent = new Intent(getActivity(), FaxOpenActivity.class);
                    } else if (item instanceof VoicemailViewModel) {
                        intent = new Intent(getActivity(), VoicemailOpenActivity.class);
                    } else if (item instanceof SMSViewModel) {
                        SmsConversationActivity.show(getActivity(), item.getMyNumber(), item.getOtherNumber());
                        SMSRepository.getInstance().setSMSRead(((SMSViewModel) item).getItem());
                        return;
                    } else {
                        Toast.makeText(getContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    intent.putExtra(IntentHelper.MESSAGE_ID, id);
                    Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
                    getActivity().startActivity(intent, transitionBundle);
                }
            }
        }
    }
}
