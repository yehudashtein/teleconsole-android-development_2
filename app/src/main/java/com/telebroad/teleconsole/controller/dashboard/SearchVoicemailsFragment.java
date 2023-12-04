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
import com.telebroad.teleconsole.databinding.FragmentSearchVoicemailsBinding;
import com.telebroad.teleconsole.viewmodels.SearchViewModel;
import com.telebroad.teleconsole.viewmodels.SearchVoicemailViewModel;
import java.lang.reflect.Type;
import java.util.List;

public class SearchVoicemailsFragment extends Fragment {
    private FragmentSearchVoicemailsBinding binding;
    private String query;
    private static ProgressBar progressBar;


    public SearchVoicemailsFragment() {}

    public static SearchVoicemailsFragment newInstance() {
        return new SearchVoicemailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchVoicemailsBinding.inflate(inflater, container, false);
        progressBar = binding.progressBar;
        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        viewModel.getSearchQuery().observe(requireActivity(), s -> {
            query = s;
        });
        viewModel.getVoicemailsMutableLiveData().observe(requireActivity(), jsonArray -> {
            if (isAdded()) {
                SearchVoicemailsAdapter voicemailsAdapter = new SearchVoicemailsAdapter(new SearchVoicemailsAdapter.SearchDiff(), requireActivity(),query);
                if (jsonArray != null) {
                    binding.txtResultsNotFound.setVisibility(View.GONE);
                    binding.imgResultsNotFound.setVisibility(View.GONE);
                    binding.rvContacts.setVisibility(View.VISIBLE);
                    binding.txtVoicemails.setVisibility(View.VISIBLE);
                    Gson gson = new Gson();
                    Type userListType = new TypeToken<List<SearchVoicemailViewModel>>() {}.getType();
                    List<SearchVoicemailViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
                    binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireActivity()));
                    binding.rvContacts.setAdapter(voicemailsAdapter);
                    voicemailsAdapter.submitList(users);
                } else {
                    binding.txtResultsNotFound.setVisibility(View.VISIBLE);
                    binding.imgResultsNotFound.setVisibility(View.VISIBLE);
                    binding.rvContacts.setVisibility(View.GONE);
                    binding.txtVoicemails.setVisibility(View.GONE);
                }
            }
        });
        return binding.getRoot();
    }
    public static ProgressBar getProgressBar(){return progressBar;}
}