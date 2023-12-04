package com.telebroad.teleconsole.controller.dashboard;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.databinding.FragmentSearchCallHistoryFragentBinding;
import com.telebroad.teleconsole.viewmodels.SearchCallHistoryViewModel;
import com.telebroad.teleconsole.viewmodels.SearchViewModel;

import java.lang.reflect.Type;
import java.util.List;

public class SearchCallHistoryFragment extends Fragment {
    private FragmentSearchCallHistoryFragentBinding binding;
    private String query;
    private static ProgressBar progressBar;
    public SearchCallHistoryFragment() {}

    public static SearchCallHistoryFragment newInstance() {
        return new SearchCallHistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchCallHistoryFragentBinding.inflate(inflater, container, false);
        progressBar = binding.progressBar;
        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        viewModel.getSearchQuery().observe(getViewLifecycleOwner(), s -> {
            query = s;
        });
        viewModel.getCdrsMutableLiveData().observe(requireActivity(), jsonArray -> {
            if (isAdded()) {
                CallHistorySearchAdapter callHistorySearchAdapter = new CallHistorySearchAdapter(new CallHistorySearchAdapter.SearchDiff(), requireActivity(),query);
                if (jsonArray != null) {
                    binding.txtResultsNotFound.setVisibility(View.GONE);
                    binding.imgResultsNotFound.setVisibility(View.GONE);
                    binding.rvCallHistory.setVisibility(View.VISIBLE);
                    binding.txtCallHistory.setVisibility(View.VISIBLE);
                    Gson gson = new Gson();
                    Type userListType = new TypeToken<List<SearchCallHistoryViewModel>>() {}.getType();
                    List<SearchCallHistoryViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
                    binding.rvCallHistory.setLayoutManager(new LinearLayoutManager(requireActivity()));
                    binding.rvCallHistory.setAdapter(callHistorySearchAdapter);
                    callHistorySearchAdapter.submitList(users);
                } else {
                    binding.txtResultsNotFound.setVisibility(View.VISIBLE);
                    binding.imgResultsNotFound.setVisibility(View.VISIBLE);
                    binding.rvCallHistory.setVisibility(View.GONE);
                    binding.txtCallHistory.setVisibility(View.GONE);
                }
            }
        });
        return binding.getRoot();
    }
    public static ProgressBar getProgressBar(){return progressBar;}
}