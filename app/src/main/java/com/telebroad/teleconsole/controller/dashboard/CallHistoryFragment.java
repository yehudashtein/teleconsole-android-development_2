package com.telebroad.teleconsole.controller.dashboard;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.ChooseContactActivity;
import com.telebroad.teleconsole.controller.NewContactActivity;
import com.telebroad.teleconsole.controller.SmsConversationActivity;
import com.telebroad.teleconsole.databinding.ItemMessageBinding;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.CallHistory;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.repositories.CallHistoryRepository;
import com.telebroad.teleconsole.viewmodels.CallHistoryListViewModel;
import com.telebroad.teleconsole.viewmodels.CallHistoryViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallHistoryFragment extends Fragment implements ScrollableFragment {
    private CallHistoryListViewModel callHistoryList;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private String phoneNumberToSave;
    private Menu menu2;
    public CallHistoryFragment() {}
    private boolean isFiltering = false;
    private SearchView searchView;
    private CallHistoryAdapter adapter;

    public static CallHistoryFragment newInstance() {
        return new CallHistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        callHistoryList = ViewModelProviders.of(this).get(CallHistoryListViewModel.class);
    }

@Override
public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.call_history, menu);
//    MenuItem searchItem = menu.findItem(R.id.action_search);
//    searchView = (SearchView) searchItem.getActionView();
//    searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//        @Override
//        public boolean onMenuItemActionExpand(MenuItem item) {
//            searchView.setIconified(false);
//            return true;
//        }
//
//        @Override
//        public boolean onMenuItemActionCollapse(MenuItem item) {
//            if (getActivity() == null) {
//                return true;
//            }
//            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (imm == null) {
//                return true;
//            }
//            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
//            stopFiltering();
//            return true;
//        }
//    });
//    if (searchView == null) {
//        return;
//    }
//    searchView.setOnCloseListener(() -> {
//        searchItem.collapseActionView();
//        stopFiltering();
//        return false;
//    });
//    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//        @Override
//        public boolean onQueryTextSubmit(String query) {
//            return false;
//        }
//
//        @Override
//        public boolean onQueryTextChange(String newText) {
//            if (Strings.isNullOrEmpty(newText)) {
//                stopFiltering();
//            } else {
//                startFiltering();
//                // TODO Replace with Call History
//            }
//            return false;
//        }
//    });
//    searchView.setQueryHint(getActivity().getResources().getString(R.string.search_calls));
}

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            String type = data.getStringExtra(ChooseContactActivity.EXTRA_CHOOSE_CONTACT_TYPE);
            String id = data.getStringExtra(ChooseContactActivity.EXTRA_CHOOSE_CONTACT_ID);
           // android.util.Log.d("ChooseContact01.1", "save number " + phoneNumberToSave + " id " + id + " type " + type);
            NewContactActivity.editContact(getActivity(), type, id, phoneNumberToSave);
            android.util.Log.d("ChooseContact01", "Type = " + type);
            if (type.equals("corporate")) {
              //  android.util.Log.d("ChooseContact01", "Retrying");
                Intent addToExisting = new Intent(getActivity(), ChooseContactActivity.class);
                getActivity().startActivityForResult(addToExisting, 0);
            }
        }
        phoneNumberToSave = null;
    }

    private void stopFiltering() {
       // android.util.Log.d("FILTER01", "Stop filtering");
        isFiltering = false;
        adapter.submitList(adapter.callLogModels);
    }

    private void startFiltering() {
        //android.util.Log.d("FILTER01", "Start filtering");
        isFiltering = true;
        adapter.filter(searchView.getQuery().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(requireActivity(),SearchActivity.class);
                intent.putExtra("itemCurrent",2);
                startActivity(intent);
                break;
            case R.id.action_all:
                callHistoryList.setCallListType(CallHistoryRepository.CallListType.ALL);
                break;
            case R.id.action_missed:
                callHistoryList.setCallListType(CallHistoryRepository.CallListType.MISSED);
                break;
            case R.id.action_incoming:
                callHistoryList.setCallListType(CallHistoryRepository.CallListType.INCOMING);
                break;
            case R.id.action_outgoing:
                callHistoryList.setCallListType(CallHistoryRepository.CallListType.OUTGOING);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_callhistory_list, container, false);
        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.call_history_list);
//        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(callHistoryList::refreshCallHistory);
        FloatingActionButton dialpadButton = view.findViewById(R.id.diapadButton);
//        dialpadButton.setOnClickListener(clickedView -> {
//            new DialPadFragment().show(getActivity().getSupportFragmentManager(), "dial number");
//        });
        DialPadFragment dialPadFragment = DialPadFragment.getInstance();
        dialpadButton.setOnClickListener(clickedView -> {
            dialPadFragment.show(getActivity().getSupportFragmentManager(), "android:switcher:2131362290:3");
        });
        adapter = new CallHistoryAdapter();
        //android.util.Log.d("Layout", "observing call history");
        callHistoryList.callHistoryList.observe(getViewLifecycleOwner(), adapter::setCallHistory);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        recyclerView.scrollToPosition(0);
        super.onResume();
    }

    @Override
    public void onPause() {
       // android.util.Log.d("Detach", "Pausing");
        if (adapter != null) {
            adapter.openMessageModel = null;
            adapter.notifyDataSetChanged();
        }
        super.onPause();
    }

    public static final DiffUtil.ItemCallback<CallHistoryViewModel> DIFF_UTIL = new DiffUtil.ItemCallback<CallHistoryViewModel>() {

        @Override
        public boolean areItemsTheSame(@NonNull CallHistoryViewModel oldItem, @NonNull CallHistoryViewModel newItem) {
            return oldItem.getItem().getCallid().equals(newItem.getItem().getCallid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CallHistoryViewModel oldItem, @NonNull CallHistoryViewModel newItem) {
            // Call histories are immutable.
            return oldItem.getItem().getCallid().equals(newItem.getItem().getCallid());
        }
    };

    @Override
    public RecyclerView recyclerView() {
        return recyclerView;
    }

    class CallHistoryAdapter extends ListAdapter<CallHistoryViewModel, CallHistoryAdapter.ViewHolder> {
        private List<CallHistory> callLogs;
        private final List<CallHistoryViewModel> callLogModels = new ArrayList<>();
        private final List<CallHistoryViewModel> selectedMessageModels = new ArrayList<>();
        private CallHistoryViewModel openMessageModel;
        private boolean isSelectedMode = false;
        private ActionMode selectedMode;
        private final ActionMode.Callback selectedModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.messages_selected, menu);
                menu2 = menu;
                if (selectedMessageModels.size() > 1){
                    menu.findItem(R.id.copy).setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete) {
                    String suffix = selectedMessageModels.size() == 1 ? "" : "s";
                    new MaterialAlertDialogBuilder(getActivity())
                            .setTitle("Delete " + selectedMessageModels.size() + " Call" + suffix + "?")
                            .setMessage("Are you sure you want to delete " + selectedMessageModels.size() + " Call" + suffix + ".")
                            .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                                for (CallHistoryViewModel model : selectedMessageModels) {
                                    callLogModels.remove(model);
                                    model.deleteItem();
                                }
                                selectedMode.finish();
//                                submittingList(callLogModels);
                                notifyDataSetChanged();
                                dialog.dismiss();
                            }))
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create().show();
                }if (item.getItemId() == R.id.copy){
                    if (selectedMessageModels.size() == 1){
                        for (CallHistoryViewModel model : selectedMessageModels){
                           PhoneNumber phoneNumber= model.getOtherNumber();
                           String a = String.valueOf(phoneNumber);
                                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("PhoneNumber",a);
                                clipboardManager.setPrimaryClip(clipData);
                                Toast.makeText(getActivity(), "Copied ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                setSelectedMode(false);
                selectedMessageModels.clear();
//                submittingList(callLogModels);
                adapter.notifyDataSetChanged();
            }
        };

        protected CallHistoryAdapter() {
            super(DIFF_UTIL);
        }

        private void setSelectedMode(boolean on) {
            isSelectedMode = on;
            if (isSelectedMode) {
                selectedMode = ((AppCompatActivity) getActivity()).startSupportActionMode(selectedModeCallback);
            } else {
                selectedMode.finish();
            }
        }

        private void update(CallHistoryViewModel update) {
            if (selectedMessageModels.contains(update)) {
                selectedMessageModels.remove(update);
                if (selectedMessageModels.isEmpty() && selectedMode != null) {
                    setSelectedMode(false);
                }
            } else {
                if (selectedMessageModels.isEmpty()) {
                    setSelectedMode(true);
                }
                selectedMessageModels.add(update);
            }
            if (selectedMode != null) {
                selectedMode.setTitle(selectedMessageModels.size() + " Selected");
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMessageBinding binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (callLogs != null) {
                CallHistoryViewModel current;
                if (openMessageModel == holder.item) {
                    openMessageModel = null;
                }
                current = getCurrentList().get(position);
//                if (isFiltering){
//                    current = filteredCallHistory.get(position);
//                } else {
//                    current = callLogs.get(position);
//                }
                holder.setCallLogViewModel(current);
//                CallHistoryViewModel currentViewModel = ViewModelProviders.of(CallHistoryFragment.this).get(current.getId(), CallHistoryViewModel.class);
//                currentViewModel.setItem(currentViewModel);
//                holder.bind(currentViewModel);
                holder.itemView.setOnClickListener(v -> {
                    if (isSelectedMode) {
                        update(holder.item);
                        holder.updateBackground();
                        if (selectedMessageModels.size() > 1){
                            menu2.removeItem(R.id.copy);
                        }else {
                           MenuItem menuItem = menu2.add(0,R.id.copy,100,"Copy");
                          menuItem.setIcon(R.drawable.ic_baseline_content_copy_white);
                        }
                        return;
                    }
                    if (openMessageModel != null) {
                        if (openMessageModel == holder.item) {
                            openMessageModel = null;
                        } else {
                            ViewHolder oldHolder = null;
                            for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
                                oldHolder = (ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                                if (oldHolder.item == openMessageModel) {
                                    break;
                                }
                            }
                            openMessageModel = holder.item;
                            //android.util.Log.d("CallHistory01", "setting item");
                            if (oldHolder != null) {
                                oldHolder.updateBackground();
                            }
                        }
                    } else {
                        openMessageModel = holder.item;
                    }
                    holder.updateBackground();
                });
                holder.itemView.setOnLongClickListener(ev -> {
                    update(holder.item);
                    holder.updateBackground();
                    if (selectedMessageModels.size() > 1){
                        menu2.removeItem(R.id.copy);
                    }else{
                        menu2.removeItem(R.id.copy);
                        MenuItem menuItem = menu2.add(0,R.id.copy,100,"Copy");
                        menuItem.setIcon(R.drawable.ic_baseline_content_copy_white);
                    }
                    return true;
                });
            }
            if (position >= (getItemCount() - 20) && !isFiltering) {
                callHistoryList.loadMoreHistory(callLogs.size());
            }
        }

        @Override
        public int getItemCount() {
            getCurrentList();
            return getCurrentList().size();
        }

        private void filter(String query) {
//            filteredCallHistory.clear();
            List<CallHistoryViewModel> filteredCallHistory = new ArrayList<>();
            for (CallHistoryViewModel history : callLogModels) {
                if (history.matches(query)) {
                    filteredCallHistory.add(history);
                }
            }
            submitList(filteredCallHistory);
//            notifyDataSetChanged();
        }

        private void mapMessagesToModels(List<CallHistory> callLogs){
            callLogModels.clear();
            for (CallHistory callHistory: callLogs){
                CallHistoryViewModel callHistoryViewModel = new CallHistoryViewModel(); ///ViewModelProviders.of(CallHistoryFragment.this).get(callHistory.getCallid(), CallHistoryViewModel.class);
                callHistoryViewModel.setItem(callHistory);
                callLogModels.add(callHistoryViewModel);
            }
        }
        public void setCallHistory(List<CallHistory> callLogs) {
            refreshLayout.setRefreshing(false);
            if (callLogs != null){
                Collections.sort(callLogs);
                mapMessagesToModels(callLogs);
                if (isFiltering) {;
                    filter(searchView.getQuery().toString());
                } else {
                    submittingList(callLogModels);
//                recyclerView.smoothScrollToPosition(0);
                }
            }
            this.callLogs = callLogs;
            //android.util.Log.d("Layout", "call history set");
        }

        private void submittingList(List<CallHistoryViewModel> list){
            final int offset = recyclerView.computeVerticalScrollOffset();
            //android.util.Log.d("RV set","RecyclerView position before " + offset);
            submitList(list, () -> {
                if (offset == 0) {
                    //android.util.Log.d("RV set","Scrolling ");
                    recyclerView.smoothScrollToPosition(0);
                }
            });
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final ItemMessageBinding binding;
            private CallHistoryViewModel item;
            ViewHolder(ItemMessageBinding binding) {
                super(binding.getRoot());
                if (getView() != null) {
                    binding.setLifecycleOwner(getViewLifecycleOwner());
                }
                this.binding = binding;
            }

            void setCallLogViewModel(CallHistoryViewModel viewModel){
                this.item = viewModel;
                bind(viewModel);
            }
            void bind(CallHistoryViewModel model) {
                this.item = model;
                binding.setViewmodel(model);
                binding.group1.setOnClickListener(v -> {
                    //android.util.Log.d("CH01", "Group 1 clicked");
                    SipManager.getInstance().call(this.item.getOtherNumber().fixed(), getActivity());
                });
                binding.group2.setOnClickListener(v -> SmsConversationActivity.show(getActivity(), this.item.getOtherNumber()));
                binding.group3.setOnClickListener(v -> {
                    List<? extends Contact> contacts = item.getOtherNumber().getMatchedContactsList();
                    Utils.viewContact(contacts, item.getOtherNumber().fixed(), getActivity());
                });
                binding.executePendingBindings();
//                item.updateNameOwner(CallHistoryFragment.this);
                updateBackground();
            }

            private void updateBackground() {
                TypedValue typedValue = new TypedValue();
                requireActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
                //int backgroundResource = R.drawable.bg_rectangle_white_ripple;
                int backgroundResource=0;
                if (item != null) {
                    binding.group2.setVisibility(View.GONE);
                    if (item.equals(openMessageModel)) {
                        this.itemView.setElevation(32);
                       // backgroundResource = R.drawable.bg_rounded_rectangle;
                        if (typedValue.resourceId != 0) {
                            backgroundResource=  typedValue.resourceId;
                        } else {
                            backgroundResource= typedValue.data;
                        }
                        binding.group2.setVisibility(this.item.getOtherNumber().isFull() ? View.VISIBLE : View.GONE);
                        binding.group4.setVisibility(View.VISIBLE);
                        if (item.getOtherNumber().getMatchedContactsList() == null || item.getOtherNumber().getMatchedContactsList().isEmpty()) {
                            binding.imageViewGroup3.setImageResource(R.drawable.ic_person_add);
                            binding.textViewGroup3.setText(R.string.add_contact);
                        } else {
                            binding.imageViewGroup3.setImageResource(R.drawable.ic_person_black);
                            binding.textViewGroup3.setText(R.string.view_contact);
                        }
                    } else {
                        binding.group4.setVisibility(View.GONE);
                        this.itemView.setElevation(0);
                    }
                    if (selectedMessageModels.contains(item)) {
                        backgroundResource = R.color.oldColorPrimaryLight;
                    }
                }
                this.itemView.setBackgroundResource(backgroundResource);
            }
        }
    }
}
