package com.telebroad.teleconsole.controller.dashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.databinding.FragmentSearchContactsBinding;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.SearchContactsModel;
import com.telebroad.teleconsole.viewmodels.ContactViewModel;
import com.telebroad.teleconsole.viewmodels.SearchViewModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchContactsFragment extends Fragment {
    private FragmentSearchContactsBinding binding;
    private String query;

    public SearchContactsFragment() {
    }

    public static SearchContactsFragment newInstance() {
        return new SearchContactsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (query != null) {outState.putString("my_key", query);}
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchContactsBinding.inflate(inflater, container, false);
        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        if (savedInstanceState != null) {
            query = savedInstanceState.getString("my_key");
        }
        viewModel.getSearchQuery().observe(requireActivity(), s -> {
            query = s;
        });
        viewModel.mergedContactsLiveData.observe(getViewLifecycleOwner(), combinedList1 -> {
            SearchContactsAdapter<Contact> searchAdapter = new SearchContactsAdapter<Contact>(new SearchContactsAdapter.SearchDiff(), requireActivity(), query);
            if (combinedList1 != null && combinedList1.size() > 0) {
                binding.txtResultsNotFound.setVisibility(View.GONE);
                binding.imgResultsNotFound.setVisibility(View.GONE);
                binding.txtContacts.setVisibility(View.VISIBLE);
                binding.rvContacts.setVisibility(View.VISIBLE);
                binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.rvContacts.setAdapter(searchAdapter);
                searchAdapter.submitList(combinedList1);
            } else if (combinedList1 == null) {
                viewModel.getJsonArrayMutableLiveData().observe(getViewLifecycleOwner(), jsonArray -> {
                    if (jsonArray != null) {
                        SearchContactsAdapter<SearchContactsModel> searchAdapter1 = new SearchContactsAdapter<SearchContactsModel>(new SearchContactsAdapter.SearchDiff(), requireActivity(), query);
                        binding.txtResultsNotFound.setVisibility(View.GONE);
                        binding.imgResultsNotFound.setVisibility(View.GONE);
                        binding.txtContacts.setVisibility(View.VISIBLE);
                        binding.rvContacts.setVisibility(View.VISIBLE);
                        binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.rvContacts.setAdapter(searchAdapter1);
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(SearchContactsModel.class, new SearchAllFragment.SearchContactsModelDeserializer())
                                .create();
                        Type userListType = new TypeToken<List<SearchContactsModel>>() {
                        }.getType();
                        List<SearchContactsModel> users = gson.fromJson(jsonArray.toString(), userListType);
                        searchAdapter1.submitList(users);
                    } else {
                        binding.txtResultsNotFound.setVisibility(View.VISIBLE);
                        binding.imgResultsNotFound.setVisibility(View.VISIBLE);
                        binding.txtContacts.setVisibility(View.GONE);
                        binding.rvContacts.setVisibility(View.GONE);
                    }
                });
                viewModel.getContactsModelsLiveData().observe(getViewLifecycleOwner(), contacts -> ContactViewModel.personalContacts.observe(getViewLifecycleOwner(), contacts1 -> {
                    List<Contact> contactsModels = new ArrayList<>();
                    for (Contact c : contacts1) {
                        if (c.getWholeName() != null) {
                            if (c.getWholeName().toLowerCase(Locale.ROOT)
                                    .contains(query.toLowerCase(Locale.ROOT).replaceAll("&"," "))) {
                                contactsModels.add(c);
                            }
                        }
                    }
                    if (contactsModels.size() > 0) {
                        SearchContactsAdapter<Contact> searchAdapter2 = new SearchContactsAdapter<Contact>(new SearchContactsAdapter.SearchDiff<>(), requireActivity(), query);
                        binding.txtResultsNotFound.setVisibility(View.GONE);
                        binding.imgResultsNotFound.setVisibility(View.GONE);
                        binding.txtContacts.setVisibility(View.VISIBLE);
                        binding.rvContacts.setVisibility(View.VISIBLE);
                        binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.rvContacts.setAdapter(searchAdapter2);
                        searchAdapter2.submitList(contactsModels);
                    } else {
                        binding.txtResultsNotFound.setVisibility(View.VISIBLE);
                        binding.imgResultsNotFound.setVisibility(View.VISIBLE);
                        binding.txtContacts.setVisibility(View.GONE);
                        binding.rvContacts.setVisibility(View.GONE);
                    }
                }));
            }
        });
        return binding.getRoot();
    }
}