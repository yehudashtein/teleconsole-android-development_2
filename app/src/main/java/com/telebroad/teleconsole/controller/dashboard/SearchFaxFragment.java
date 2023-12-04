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
import com.telebroad.teleconsole.databinding.FragmentSearchFaxBinding;
import com.telebroad.teleconsole.viewmodels.FaxSearchViewModel;
import com.telebroad.teleconsole.viewmodels.SearchViewModel;

import java.lang.reflect.Type;
import java.util.List;

public class SearchFaxFragment extends Fragment {
    private FragmentSearchFaxBinding binding;
    private String query;
    private static ProgressBar progressBar;

    public SearchFaxFragment() {}

    public static SearchFaxFragment newInstance() {
        return new SearchFaxFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchFaxBinding.inflate(inflater, container, false);
        progressBar = binding.progressBar;
        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        viewModel.getSearchQuery().observe(requireActivity(), s -> {
            query = s;
        });
        viewModel.getFaxMutableLiveData().observe(requireActivity(), jsonArray -> {
            if (isAdded()) {
                FaxSearchAdapter faxSearchAdapter = new FaxSearchAdapter(new FaxSearchAdapter.SearchDiff(), requireActivity(),query);
                if (jsonArray != null) {
                    binding.txtResultsNotFound.setVisibility(View.GONE);
                    binding.imgResultsNotFound.setVisibility(View.GONE);
                    binding.rvContacts.setVisibility(View.VISIBLE);
                    binding.txtFax.setVisibility(View.VISIBLE);
                    Gson gson = new Gson();
                    Type userListType = new TypeToken<List<FaxSearchViewModel>>() {}.getType();
                    List<FaxSearchViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
                    binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireActivity()));
                    binding.rvContacts.setAdapter(faxSearchAdapter);
                    faxSearchAdapter.submitList(users);
                } else {
                    binding.txtResultsNotFound.setVisibility(View.VISIBLE);
                    binding.imgResultsNotFound.setVisibility(View.VISIBLE);
                    binding.rvContacts.setVisibility(View.GONE);
                    binding.txtFax.setVisibility(View.GONE);
                }
            }
        });
        return binding.getRoot();
    }
    public static ProgressBar getProgressBar(){return progressBar;}

}