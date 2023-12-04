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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.databinding.FragmentSearchAllBinding;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.SearchContactsModel;
import com.telebroad.teleconsole.model.SearchSms;
import com.telebroad.teleconsole.viewmodels.ContactViewModel;
import com.telebroad.teleconsole.viewmodels.FaxSearchViewModel;
import com.telebroad.teleconsole.viewmodels.SearchCallHistoryViewModel;
import com.telebroad.teleconsole.viewmodels.SearchSmsMessageViewModel;
import com.telebroad.teleconsole.viewmodels.SearchViewModel;
import com.telebroad.teleconsole.viewmodels.SearchVoicemailViewModel;
import com.telebroad.teleconsole.viewmodels.SmsConversationsSearchViewModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchAllFragment extends Fragment{
    private FragmentSearchAllBinding binding;
    private String query;

    public SearchAllFragment() {}

    public static SearchAllFragment newInstance() {
        return new SearchAllFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchAllBinding.inflate(inflater, container, false);
        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        viewModel.getSearchQuery().observe(getViewLifecycleOwner(), s -> {
            query = s;
        });
        viewModel.getAllEmptyLiveData().observe(requireActivity(), allEmpty -> {
            if (allEmpty) {
                binding.txtResultsNotFound.setVisibility(View.VISIBLE);
                binding.imgResultsNotFound.setVisibility(View.VISIBLE);
            }
        });
        viewModel.getVoicemailsMutableLiveData().observe(getViewLifecycleOwner(), jsonArray -> {
            if (isAdded()) {
                SearchVoicemailsAdapter voicemailsAdapter = new SearchVoicemailsAdapter(new SearchVoicemailsAdapter.SearchDiff(), requireActivity(),query);
                if (jsonArray != null) {
                    binding.txtResultsNotFound.setVisibility(View.GONE);
                    binding.imgResultsNotFound.setVisibility(View.GONE);
                    binding.rvVoicemail.setVisibility(View.VISIBLE);
                    binding.txtVoicemails.setVisibility(View.VISIBLE);
                    binding.txtVoicemailsSeeMore.setVisibility(View.VISIBLE);
                    Gson gson = new Gson();
                    Type userListType = new TypeToken<List<SearchVoicemailViewModel>>() {}.getType();
                    List<SearchVoicemailViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
                    //binding.rvVoicemail.setLayoutManager(new LinearLayoutManager(requireActivity()));
                    binding.rvVoicemail.setLayoutManager(new LinearLayoutManager(requireActivity()) { // Example with GridLayoutManager
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }

                        @Override
                        public boolean canScrollHorizontally() {
                            return false;
                        }
                    });
                    binding.rvVoicemail.setAdapter(voicemailsAdapter);
                    if (users.size() >= 3) voicemailsAdapter.submitList(users.subList(0, 3));
                    else voicemailsAdapter.submitList(users);
                    binding.txtVoicemailsSeeMore.setOnClickListener(v -> {
                        SearchActivity.setCurrentItem(6);
                    });
                } else {
                    binding.rvVoicemail.setVisibility(View.GONE);
                    binding.txtVoicemails.setVisibility(View.GONE);
                    binding.txtVoicemailsSeeMore.setVisibility(View.GONE);
                }
            }
        });
        viewModel.getFaxMutableLiveData().observe(getViewLifecycleOwner(), jsonArray -> {
            if (isAdded()) {
                FaxSearchAdapter faxSearchAdapter = new FaxSearchAdapter(new FaxSearchAdapter.SearchDiff(), requireActivity(),query);
                if (jsonArray != null) {
                    binding.txtResultsNotFound.setVisibility(View.GONE);
                    binding.imgResultsNotFound.setVisibility(View.GONE);
                    binding.rvFax.setVisibility(View.VISIBLE);
                    binding.txtFax.setVisibility(View.VISIBLE);
                    binding.txtFaxSeeMore.setVisibility(View.VISIBLE);
                    Gson gson = new Gson();
                    Type userListType = new TypeToken<List<FaxSearchViewModel>>() {
                    }.getType();
                    List<FaxSearchViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
                    //binding.rvFax.setLayoutManager(new LinearLayoutManager(requireActivity()));
                    binding.rvFax.setLayoutManager(new LinearLayoutManager(requireActivity()) { // Example with GridLayoutManager
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }

                        @Override
                        public boolean canScrollHorizontally() {
                            return false;
                        }
                    });
                    binding.rvFax.setAdapter(faxSearchAdapter);
                    if (users.size() >= 3) faxSearchAdapter.submitList(users.subList(0, 3));
                    else faxSearchAdapter.submitList(users);
                    binding.txtFaxSeeMore.setOnClickListener(v -> {
                        SearchActivity.setCurrentItem(5);
                    });
                } else {
                    binding.rvFax.setVisibility(View.GONE);
                    binding.txtFax.setVisibility(View.GONE);
                    binding.txtFaxSeeMore.setVisibility(View.GONE);
                }
            }
        });
        viewModel.mergedSmsLiveData.observe(getViewLifecycleOwner(), searchSms -> {
            if (isAdded()) {
                SmsConversationsSearchAdapter<SearchSms> conversationsSearchAdapter = new SmsConversationsSearchAdapter<SearchSms>(new SmsConversationsSearchAdapter.SearchDiff<>(), requireActivity(),query);
                if (searchSms != null) {
                    binding.txtResultsNotFound.setVisibility(View.GONE);
                    binding.imgResultsNotFound.setVisibility(View.GONE);
                    binding.rvSmsMessages.setVisibility(View.VISIBLE);
                    binding.txtSmsConversations.setVisibility(View.VISIBLE);
                    binding.smsConversationsSeeMore.setVisibility(View.VISIBLE);
                    binding.rvSmsMessages.setLayoutManager(new LinearLayoutManager(requireActivity()) { // Example with GridLayoutManager
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }

                        @Override
                        public boolean canScrollHorizontally() {
                            return false;
                        }
                    });
                    binding.rvSmsMessages.setAdapter(conversationsSearchAdapter);
                    if (searchSms.size() >= 3) conversationsSearchAdapter.submitList(searchSms.subList(0, 3));
                    else conversationsSearchAdapter.submitList(searchSms);
                    binding.smsConversationsSeeMore.setOnClickListener(v -> {
                        SearchActivity.setCurrentItem(3);
                    });
                } else {
                    viewModel.getSmsConversationsMutableLiveData().observe(requireActivity(), jsonArray -> {
                        if (isAdded()) {
                            SmsConversationsSearchAdapter<SmsConversationsSearchViewModel> conversationsSearchAdapter1 = new SmsConversationsSearchAdapter<SmsConversationsSearchViewModel>(new SmsConversationsSearchAdapter.SearchDiff<SmsConversationsSearchViewModel>(), requireActivity(), query);
                            if (jsonArray != null) {
                                boolean allAreInstances = jsonArray.stream().allMatch(item -> item instanceof SmsConversationsSearchViewModel);
                                if (allAreInstances) {
                                    binding.txtResultsNotFound.setVisibility(View.GONE);
                                    binding.imgResultsNotFound.setVisibility(View.GONE);
                                    binding.rvSmsMessages.setVisibility(View.VISIBLE);
                                    binding.txtSmsConversations.setVisibility(View.VISIBLE);
                                    List<SmsConversationsSearchViewModel> smsConversationsSearchViewModel = (List<SmsConversationsSearchViewModel>) jsonArray;
                                    binding.rvSmsMessages.setLayoutManager(new LinearLayoutManager(requireActivity()));
                                    binding.rvSmsMessages.setAdapter(conversationsSearchAdapter1);
                                    if (smsConversationsSearchViewModel.size() >= 3)
                                        conversationsSearchAdapter1.submitList(smsConversationsSearchViewModel.subList(0, 3));
                                    else {
                                        conversationsSearchAdapter1.submitList(smsConversationsSearchViewModel);
                                    }
                                } else {
                                    binding.rvSmsMessages.setVisibility(View.GONE);
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
                                binding.rvSmsMessages.setVisibility(View.VISIBLE);
                                binding.txtSmsConversations.setVisibility(View.VISIBLE);
                                Gson gson = new Gson();
                                Type userListType = new TypeToken<List<SearchSmsMessageViewModel>>() {}.getType();
                                List<SearchSmsMessageViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
                                binding.rvSmsMessages.setLayoutManager(new LinearLayoutManager(requireActivity()));
                                binding.rvSmsMessages.setAdapter(conversationsSearchAdapter1);
                                //conversationsSearchAdapter1.submitList(users);
                                if (users.size() >= 3) conversationsSearchAdapter1.submitList(users.subList(0, 3));
                                else {
                                    conversationsSearchAdapter1.submitList(users);
                                }
                            } else {
                                binding.rvSmsMessages.setVisibility(View.GONE);
                                binding.txtSmsConversations.setVisibility(View.GONE);
                            }
                        }
                    });
//                    binding.rvSmsMessages.setVisibility(View.GONE);
//                    binding.txtSmsConversations.setVisibility(View.GONE);
//                    binding.smsConversationsSeeMore.setVisibility(View.GONE);
                }
            }
        });
//        viewModel.getSmsConversationsMutableLiveData().observe(getViewLifecycleOwner(), jsonArray -> {
//            if (isAdded()) {
//                SmsConversationsSearchAdapter<? extends SearchSms> conversationsSearchAdapter = new SmsConversationsSearchAdapter<SearchSms>(new SmsConversationsSearchAdapter.SearchDiff<>(), requireActivity(),query);
//                if (jsonArray != null) {
//                    binding.txtResultsNotFound.setVisibility(View.GONE);
//                    binding.imgResultsNotFound.setVisibility(View.GONE);
//                    binding.rvSmsMessages.setVisibility(View.VISIBLE);
//                    binding.txtSmsConversations.setVisibility(View.VISIBLE);
//                    binding.smsConversationsSeeMore.setVisibility(View.VISIBLE);
////                    Gson gson = new Gson();
////                    Type userListType = new TypeToken<List<SmsConversationsSearchViewModel>>() {}.getType();
////                    List<SmsConversationsSearchViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
//                    //binding.rvSmsMessages.setLayoutManager(new LinearLayoutManager(requireActivity()));
//                    binding.rvSmsMessages.setLayoutManager(new LinearLayoutManager(requireActivity()) { // Example with GridLayoutManager
//                        @Override
//                        public boolean canScrollVertically() {
//                            return false;
//                        }
//
//                        @Override
//                        public boolean canScrollHorizontally() {
//                            return false;
//                        }
//                    });
//                    binding.rvSmsMessages.setAdapter(conversationsSearchAdapter);
//                    if (jsonArray.size() >= 3)
//                        conversationsSearchAdapter.submitList(jsonArray.subList(0, 3));
//                    else conversationsSearchAdapter.submitList(jsonArray);
//                    binding.smsConversationsSeeMore.setOnClickListener(v -> {
//                        SearchActivity.setCurrentItem(3);
//                    });
//                } else {
//                    binding.rvSmsMessages.setVisibility(View.GONE);
//                    binding.txtSmsConversations.setVisibility(View.GONE);
//                    binding.smsConversationsSeeMore.setVisibility(View.GONE);
//                }
//            }
//        });
        viewModel.getCdrsMutableLiveData().observe(getViewLifecycleOwner(), jsonArray -> {
            if (isAdded()) {
                CallHistorySearchAdapter callHistorySearchAdapter = new CallHistorySearchAdapter(new CallHistorySearchAdapter.SearchDiff(), requireActivity(),query);
                if (jsonArray != null) {
                    binding.txtResultsNotFound.setVisibility(View.GONE);
                    binding.imgResultsNotFound.setVisibility(View.GONE);
                    binding.rvCallHistory.setVisibility(View.VISIBLE);
                    binding.txtCallHistory.setVisibility(View.VISIBLE);
                    binding.txtCallHistorySeeMore.setVisibility(View.VISIBLE);
                    Gson gson = new Gson();
                    Type userListType = new TypeToken<List<SearchCallHistoryViewModel>>() {}.getType();
                    List<SearchCallHistoryViewModel> users = gson.fromJson(jsonArray.toString(), userListType);
                    // binding.rvCallHistory.setLayoutManager(new LinearLayoutManager(requireActivity()));
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
                    binding.rvCallHistory.setAdapter(callHistorySearchAdapter);
                    if (users.size() >= 3) callHistorySearchAdapter.submitList(users.subList(0, 3));
                    else callHistorySearchAdapter.submitList(users);
                    binding.txtCallHistorySeeMore.setOnClickListener(v -> {
                        SearchActivity.setCurrentItem(2);
                    });
                } else {
                    binding.rvCallHistory.setVisibility(View.GONE);
                    binding.txtCallHistory.setVisibility(View.GONE);
                    binding.txtCallHistorySeeMore.setVisibility(View.GONE);
                }
            }
        });
        viewModel.mergedContactsLiveData.observe(getViewLifecycleOwner(), combinedList1 -> {
            SearchContactsAdapter<Contact> searchAdapter = new SearchContactsAdapter<Contact>(new SearchContactsAdapter.SearchDiff<>(), requireActivity(), query);
            if (combinedList1 != null && combinedList1.size() > 0) {
                binding.txtResultsNotFound.setVisibility(View.GONE);
                binding.imgResultsNotFound.setVisibility(View.GONE);
                binding.txtContacts.setVisibility(View.VISIBLE);
                binding.rvContacts.setVisibility(View.VISIBLE);
                binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.rvContacts.setAdapter(searchAdapter);
                //searchAdapter.submitList(combinedList1);
                if (combinedList1.size() >= 3) searchAdapter.submitList(combinedList1.subList(0, 3));
                else searchAdapter.submitList(combinedList1);
            } else if (combinedList1 == null) {
                viewModel.getJsonArrayMutableLiveData().observe(getViewLifecycleOwner(), jsonArray -> {
                    if (jsonArray != null) {
                        SearchContactsAdapter<SearchContactsModel> searchAdapter1 = new SearchContactsAdapter<SearchContactsModel>(new SearchContactsAdapter.SearchDiff<>(), requireActivity(), query);
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
                        //searchAdapter1.submitList(users);
                        if (users.size() >= 3) searchAdapter1.submitList(users.subList(0, 3));
                        else searchAdapter1.submitList(users);
                    } else {
                        binding.txtContactsSeeMore.setVisibility(View.GONE);
                        binding.txtContacts.setVisibility(View.GONE);
                        binding.rvContacts.setVisibility(View.GONE);
                    }
                });
                viewModel.getContactsModelsLiveData().observe(getViewLifecycleOwner(), contacts -> ContactViewModel.personalContacts.observe(getViewLifecycleOwner(), contacts1 -> {
                    List<Contact> contactsModels = new ArrayList<>();
                    for (Contact c : contacts1) {
                        if (c.getWholeName() != null) {
                            if (c.getWholeName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT).replaceAll("&"," "))) {
                                contactsModels.add(c);
                            }
                        }
                    }
                    if (contactsModels.size() > 0) {
                        SearchContactsAdapter<Contact> searchAdapter2 = new SearchContactsAdapter<Contact>(new SearchContactsAdapter.SearchDiff(), requireActivity(), query);
                        binding.txtResultsNotFound.setVisibility(View.GONE);
                        binding.imgResultsNotFound.setVisibility(View.GONE);
                        binding.txtContacts.setVisibility(View.VISIBLE);
                        binding.rvContacts.setVisibility(View.VISIBLE);
                        binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.rvContacts.setAdapter(searchAdapter2);
                        //searchAdapter2.submitList(contactsModels);
                        if (contactsModels.size() >= 3) searchAdapter2.submitList(contactsModels.subList(0, 3));
                        else searchAdapter2.submitList(contactsModels);
                    } else {
                        binding.txtContactsSeeMore.setVisibility(View.GONE);
                        binding.txtContacts.setVisibility(View.GONE);
                        binding.rvContacts.setVisibility(View.GONE);
                    }
                }));
            }
        });
        binding.txtContactsSeeMore.setOnClickListener(v -> {
            SearchActivity.setCurrentItem(1);
        });
        return binding.getRoot();
    }

    public static class SearchContactsModelDeserializer implements JsonDeserializer<SearchContactsModel> {
        @Override
        public SearchContactsModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            SearchContactsModel model = new SearchContactsModel();
            model.setId(jsonObject.get("id").getAsInt());
            model.setTitle(jsonObject.get("title").getAsString());
            model.setFname(jsonObject.get("fname").getAsString());
            model.setLname(jsonObject.get("lname").getAsString());
            model.setOrganization(jsonObject.get("organization").getAsString());
            if (jsonObject.has("username") && !jsonObject.get("username").isJsonNull()) {
                model.setUsername(jsonObject.get("username").getAsString());
            } else {
                model.setUsername(null);
            }
            model.setEmail(jsonObject.get("email").getAsString());
            model.setPbx_line(jsonObject.get("pbx_line").getAsString());
            if (jsonObject.has("chat_channel") && !jsonObject.get("chat_channel").isJsonNull()) {
                model.setChat_channel(jsonObject.get("chat_channel").getAsString());
            } else {
                model.setChat_channel(null);
            }
            model.setExtension(jsonObject.get("extension").getAsString());
            model.setHome(jsonObject.get("home").getAsString());
            model.setWork(jsonObject.get("work").getAsString());
            model.setMobile(jsonObject.get("mobile").getAsString());
            model.setFax(jsonObject.get("fax").getAsString());
            model.setWebsite(jsonObject.get("website").getAsString());
            if (jsonObject.has("photo") && !jsonObject.get("photo").isJsonNull()) {
                model.setPhoto(jsonObject.get("photo").getAsString());
            } else {
                model.setPhoto(null);
            }
            model.setStatus(jsonObject.get("status").getAsInt());
            model.setStatus_msg(jsonObject.get("status_msg").getAsString());
            model.setContactType(jsonObject.get("contactType").getAsString());
            JsonElement publicElement = jsonObject.get("public");
            if (publicElement != null && publicElement.isJsonPrimitive()) {
                JsonPrimitive primitive = publicElement.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    model.setPublic_Field(primitive.getAsInt());
                } else if (primitive.isString() && !primitive.getAsString().isEmpty()) {
                    try {
                        model.setPublic_Field(Integer.parseInt(primitive.getAsString()));
                    } catch (NumberFormatException e) {
                        model.setPublic_Field(null); // or some default value
                    }
                } else {
                    model.setPublic_Field(null); // or some default value
                }
            } else {
                model.setPublic_Field(null);
            }

            model.setOwned(jsonObject.get("owned").getAsInt());
            model.setColor(jsonObject.get("color").getAsString());
            model.setSpeeddial(jsonObject.get("speeddial").getAsInt());
            model.setW(jsonObject.get("w").getAsInt());
            return model;
        }
    }

}