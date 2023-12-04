package com.telebroad.teleconsole.controller.dashboard;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.databinding.FragmentSmsConversationsBinding;
import com.telebroad.teleconsole.model.SearchSms;
import com.telebroad.teleconsole.viewmodels.SearchSmsMessageViewModel;
import com.telebroad.teleconsole.viewmodels.SearchViewModel;
import com.telebroad.teleconsole.viewmodels.SmsConversationsSearchViewModel;

import java.lang.reflect.Type;
import java.util.List;

public class SearchSmsConversationsFragment extends Fragment {
   private FragmentSmsConversationsBinding binding;
   private String query;
    private static ProgressBar progressBar;

    public SearchSmsConversationsFragment() {}

    public static SearchSmsConversationsFragment newInstance() {
        return new SearchSmsConversationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSmsConversationsBinding.inflate(inflater, container, false);
        progressBar = binding.progressBar;
        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        viewModel.getSearchQuery().observe(requireActivity(), s -> {
            query = s;
        });
        viewModel.mergedSmsLiveData.observe(getViewLifecycleOwner(), searchSms -> {
            if (isAdded()) {
                SmsConversationsSearchAdapter<SearchSms> conversationsSearchAdapter = new SmsConversationsSearchAdapter<SearchSms>(new SmsConversationsSearchAdapter.SearchDiff<>(), requireActivity(),query);
                if (searchSms != null) {
                    binding.txtResultsNotFound.setVisibility(View.GONE);
                    binding.imgResultsNotFound.setVisibility(View.GONE);
                    binding.rvCallHistory.setVisibility(View.VISIBLE);
                    binding.txtSmsConversations.setVisibility(View.VISIBLE);
                    binding.rvCallHistory.setLayoutManager(new LinearLayoutManager(requireActivity()) { // Example with GridLayoutManager
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }

                        @Override
                        public boolean canScrollHorizontally() {
                            return false;
                        }
                    });
                    binding.rvCallHistory.setAdapter(conversationsSearchAdapter);
                    conversationsSearchAdapter.submitList(searchSms);
                } else {
                    viewModel.getSmsConversationsMutableLiveData().observe(requireActivity(), jsonArray -> {
                        if (isAdded()) {
                            SmsConversationsSearchAdapter<SmsConversationsSearchViewModel> conversationsSearchAdapter1 = new SmsConversationsSearchAdapter<SmsConversationsSearchViewModel>(new SmsConversationsSearchAdapter.SearchDiff<SmsConversationsSearchViewModel>(), requireActivity(), query);
                            if (jsonArray != null) {
                                boolean allAreInstances = jsonArray.stream().allMatch(item -> item instanceof SmsConversationsSearchViewModel);
                                if (allAreInstances) {
                                    binding.txtResultsNotFound.setVisibility(View.GONE);
                                    binding.imgResultsNotFound.setVisibility(View.GONE);
                                    binding.rvCallHistory.setVisibility(View.VISIBLE);
                                    binding.txtSmsConversations.setVisibility(View.VISIBLE);
                                    List<SmsConversationsSearchViewModel> smsConversationsSearchViewModel = (List<SmsConversationsSearchViewModel>) jsonArray;
                                    binding.rvCallHistory.setLayoutManager(new LinearLayoutManager(requireActivity()));
                                    binding.rvCallHistory.setAdapter(conversationsSearchAdapter1);
                                    if (smsConversationsSearchViewModel.size() >= 3)
                                        conversationsSearchAdapter1.submitList(smsConversationsSearchViewModel.subList(0, 3));
                                    else {
                                        conversationsSearchAdapter1.submitList(smsConversationsSearchViewModel);
                                    }
                                } else {
                                    binding.rvCallHistory.setVisibility(View.GONE);
                                    binding.txtSmsConversations.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                    viewModel.getSmsMessagesMutableLiveData2().observe(getViewLifecycleOwner(), jsonArray -> {
                        if (isAdded()) {
                            SmsConversationsSearchAdapter<SearchSmsMessageViewModel> conversationsSearchAdapter1 = new SmsConversationsSearchAdapter<SearchSmsMessageViewModel>(new SmsConversationsSearchAdapter.SearchDiff<SearchSmsMessageViewModel>(), requireActivity(), query);
                            if (jsonArray != null) {
                                binding.txtResultsNotFound.setVisibility(View.GONE);
                                binding.imgResultsNotFound.setVisibility(View.GONE);
                                binding.rvCallHistory.setVisibility(View.VISIBLE);
                                binding.txtSmsConversations.setVisibility(View.VISIBLE);
                                Gson gson = new Gson();
                                Type userListType = new TypeToken<List<SearchSmsMessageViewModel>>() {
                                }.getType();
                                List<SearchSmsMessageViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
                                binding.rvCallHistory.setLayoutManager(new LinearLayoutManager(requireActivity()));
                                binding.rvCallHistory.setAdapter(conversationsSearchAdapter1);
                                conversationsSearchAdapter1.submitList(users);
                            } else {
                                binding.txtResultsNotFound.setVisibility(View.VISIBLE);
                                binding.imgResultsNotFound.setVisibility(View.VISIBLE);
                                binding.rvCallHistory.setVisibility(View.GONE);
                                binding.txtSmsConversations.setVisibility(View.GONE);
                            }
                        }
                    });
//                    binding.rvCallHistory.setVisibility(View.GONE);
//                    binding.txtSmsConversations.setVisibility(View.GONE);
                }
            }
        });

        return binding.getRoot();
    }
    public static ProgressBar getProgressBar(){return progressBar;}

}