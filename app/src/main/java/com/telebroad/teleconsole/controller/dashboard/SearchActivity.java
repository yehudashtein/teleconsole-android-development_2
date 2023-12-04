package com.telebroad.teleconsole.controller.dashboard;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.telebroad.teleconsole.helpers.Utils;
import com.android.volley.Request;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonArray;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.SearchPagerAdapter;
import com.telebroad.teleconsole.databinding.ActivitySearchBinding;
import com.telebroad.teleconsole.helpers.MySuggestionProvider;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.viewmodels.SearchViewModel;

import java.util.HashMap;

public class SearchActivity extends AppCompatActivity   {
    private static ActivitySearchBinding binding;
    private SearchRecentSuggestions suggestions;
    private ProgressBar searchView;
    private ViewPager2 viewPager2;
    private Menu menu;
    private int currentItem;
    private static EditText textView;
    private SearchViewModel viewModel;
    private static final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private static final MutableLiveData<JsonArray> jsonArrayMutableLiveData = new MutableLiveData<>();
    private final static MutableLiveData<JsonArray> cdrsMutableLiveData = new MutableLiveData<>();
    private final static MutableLiveData<JsonArray> smsConversationsMutableLiveData = new MutableLiveData<>();
    private final static MutableLiveData<JsonArray> faxMutableLiveData = new MutableLiveData<>();
    private final static MutableLiveData<JsonArray> voicemailsMutableLiveData = new MutableLiveData<>();
    private static final MediatorLiveData<Boolean> allEmptyLiveData = new MediatorLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        searchView = binding.SearchView;
        viewPager2 = binding.viewPager;
        currentItem = getIntent().getIntExtra("itemCurrent",0);
        setContentView(binding.getRoot());
        viewModel  = new ViewModelProvider(this).get(SearchViewModel.class);
        allEmptyLiveData.removeSource(jsonArrayMutableLiveData);
        allEmptyLiveData.removeSource(cdrsMutableLiveData);
        allEmptyLiveData.removeSource(smsConversationsMutableLiveData);
        allEmptyLiveData.removeSource(faxMutableLiveData);
        allEmptyLiveData.removeSource(voicemailsMutableLiveData);
        allEmptyLiveData.addSource(jsonArrayMutableLiveData, newData -> checkIfAllEmpty());
        allEmptyLiveData.addSource(cdrsMutableLiveData, newData -> checkIfAllEmpty());
        allEmptyLiveData.addSource(smsConversationsMutableLiveData, newData -> checkIfAllEmpty());
        allEmptyLiveData.addSource(faxMutableLiveData, newData -> checkIfAllEmpty());
        allEmptyLiveData.addSource(voicemailsMutableLiveData, newData -> checkIfAllEmpty());
        SearchPagerAdapter searchPagerAdapter = new SearchPagerAdapter(getSupportFragmentManager(),getLifecycle());
        binding.viewPager.setAdapter(searchPagerAdapter);
        if (getSupportActionBar() != null) {getSupportActionBar().setDisplayHomeAsUpEnabled(true);}
            binding.viewPager.setCurrentItem(currentItem);
        binding.viewPager.setPageTransformer(null);
        binding.viewPager.setOffscreenPageLimit(1);
            new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
                if (position==0) tab.setText("all");
                else if (position == 1) tab.setText("CONTACTS");
                else if (position == 2) tab.setText("CALL HISTORY");
                else if (position== 3)tab.setText("sms");
                else if (position==4)tab.setText("fax");
                else if (position==5)tab.setText("voicemails");
            }).attach();
            binding.tabLayout.invalidate();
            TabLayout.Tab tab1 = binding.tabLayout.getTabAt(currentItem);
            if (tab1 != null&& tab1.getCustomView() == null){
                View tabView = ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(0);
                tabView.invalidate();
                tabView.setBackgroundResource(R.drawable.selected_tab);
                LinearLayout tabLayout = (LinearLayout) ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(0);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabLayout.getLayoutParams();
                params.setMargins(6, 12, 6, 12);  // set margins for top, right, bottom, left respectively
                tabLayout.setLayoutParams(params);
            }
            for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
                if (i == currentItem) {
                    TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
                    if (tab != null) {
                        LinearLayout tabLayout = (LinearLayout) ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(i);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabLayout.getLayoutParams();
                        params.setMargins(6, 12, 6, 12);  // set margins for top, right, bottom, left respectively
                        tabLayout.setLayoutParams(params);
                    }
                    if (tab != null && tab.getCustomView() == null) { // Checking if custom view is null to avoid overwriting custom layouts.
                        View tabView = ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(i);
                        tabView.invalidate();
                        tabView.setBackgroundResource(R.drawable.selected_tab);
                    }
                } else {
                    TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
                    if (tab != null) {
                        LinearLayout tabLayout = (LinearLayout) ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(i);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabLayout.getLayoutParams();
                        params.setMargins(6, 12, 6, 12);  // set margins for top, right, bottom, left respectively
                        tabLayout.setLayoutParams(params);
                    }
                    if (tab != null && tab.getCustomView() == null) { // Checking if custom view is null to avoid overwriting custom layouts.
                        View tabView = ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(i);
                        tabView.invalidate();
                        tabView.setBackgroundResource(R.drawable.voice_note_border);
                    }
                }
            }
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View tabView = ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                tabView.invalidate();
                tabView.setBackgroundResource(R.drawable.selected_tab);
                binding.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View tabView = ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                tabView.invalidate();
                tabView.setBackgroundResource(R.drawable.voice_note_border);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = LayoutInflater.from(this);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        View customView = inflater.inflate(R.layout.action_bar_layout, null);
        getSupportActionBar().setCustomView(customView,layoutParams);
        //getSupportActionBar().setCustomView(new View(this));
        getSupportActionBar().getCustomView().setOnClickListener(v -> openSearchView());
        Intent intent = getIntent();
        //suggestions = new SearchRecentSuggestions(this, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            try {doMySearch(query);
            } catch (AuthFailureError e) {e.printStackTrace();}
           // suggestions.saveRecentQuery(query, null);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        textView = getSupportActionBar().getCustomView().findViewById(R.id.my_custom_title_view);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SearchActivity1", "EditText clicked");
            }
        });

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("SearchActivity1", "beforeTextChanged called");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.length() >=3) {
                    Log.d("SearchActivity1", "onTextChanged called");
                    // Create an intent and call onNewIntent
                    Intent intent = new Intent(SearchActivity.this, SearchActivity.class);
                    intent.setAction(Intent.ACTION_SEARCH);
                    intent.putExtra(SearchManager.QUERY, charSequence.toString());
                    onNewIntent(intent);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void openSearchView() {
        if (menu == null) return;
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (!searchView.isIconified()) {
            return;  // Already expanded
        }
        searchView.setIconified(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        this.menu = menu;
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryRefinementEnabled(true);
        searchView.setIconified(false);
        searchView.setFocusableInTouchMode(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (textView != null) {textView.setText(newText);}
                return true;
            }
        });
        int searchPlateId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackground(null);
            searchPlate.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchView.setQuery(textView.getText(), true);
                    searchView.onActionViewCollapsed();
                    return true;
                }
                return false;
            });
        }
        int searchImgId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchHintIcon = searchView.findViewById(searchImgId);
        if (searchHintIcon != null) {
            searchHintIcon.setVisibility(View.GONE);
            searchHintIcon.setImageDrawable(null);
        }

        int searchPlateId1 = getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlateView = searchView.findViewById(searchPlateId1);
        if (searchPlateView != null) {
            searchPlateView.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }
        int submitAreaId = getResources().getIdentifier("android:id/submit_area", null, null);
        View submitAreaView = searchView.findViewById(submitAreaId);
        if (submitAreaView != null) {
            submitAreaView.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }
        searchView.setMaxWidth(Integer.MAX_VALUE);
        return super.onCreateOptionsMenu(menu);
    }

    //int searchButtonId = searchView.getContext().getResources().getIdentifier("android:id/search_voice_btn", null, null);
    //ImageView voiceSearchButton = searchView.findViewById(searchButtonId);
    //voiceSearchButton.setImageResource(R.drawable.ic_mic_blue);

    private void doMySearch(String query) throws AuthFailureError {
        EditText titleTextView = getSupportActionBar().getCustomView().findViewById(R.id.my_custom_title_view);
        titleTextView.setText(query);
        searchView.setVisibility(View.VISIBLE);
        Utils.updateLiveData(searchQuery,query);
        HashMap<String, String> params = new HashMap<>();
        params.put("q",query.trim());
        params.put("limit","100");
        params.put("categories","fax,vox,smsc,smsm,cdrs");
//        StringBuilder queryString = new StringBuilder();
//        for (String key : params.keySet()) {
//            if (queryString.length() > 0) { // add "&" only after the first parameter
//                queryString.append("&");
//            }
//            queryString.append(key).append("=").append(params.get(key));
//        }
        String url = URLHelper.getBaseUrl()+"/search/query?";
        URLHelper.request(Request.Method.GET, url, params, jsonElement -> {
            runOnUiThread(() -> {
                searchView.setVisibility(View.GONE);
                viewPager2.setVisibility(View.VISIBLE);
            });
            JsonArray contactsArray = jsonElement.getAsJsonObject().getAsJsonArray("contacts");
            JsonArray cdrsArray = jsonElement.getAsJsonObject().getAsJsonArray("cdrs");
            JsonArray smsConversationsArray = jsonElement.getAsJsonObject().getAsJsonArray("smsConversations");
            JsonArray faxArray = jsonElement.getAsJsonObject().getAsJsonArray("fax");
            JsonArray voicemailsArray = jsonElement.getAsJsonObject().getAsJsonArray("voicemails");
            if (voicemailsArray.size() > 0) {
                Utils.updateLiveData(voicemailsMutableLiveData,voicemailsArray);
            }else Utils.updateLiveData(voicemailsMutableLiveData,null);
            if (faxArray.size() > 0) {
                Utils.updateLiveData(faxMutableLiveData,faxArray);
            }else Utils.updateLiveData(faxMutableLiveData,null);
            if (smsConversationsArray.size() > 0) {
                Utils.updateLiveData(smsConversationsMutableLiveData,smsConversationsArray);
            }else Utils.updateLiveData(smsConversationsMutableLiveData,null);
            if (cdrsArray.size() > 0) {
                Utils.updateLiveData(cdrsMutableLiveData,cdrsArray);
            }else Utils.updateLiveData(cdrsMutableLiveData,null);
            if (contactsArray.size() > 0) {
                Utils.updateLiveData(jsonArrayMutableLiveData,contactsArray);
            }else Utils.updateLiveData(jsonArrayMutableLiveData,null);
        }, teleConsoleError -> {});
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("SearchActivity1", "onNewIntent called");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("SearchActivity1", "Retrieved query: " + query);
            textView = getSupportActionBar().getCustomView().findViewById(R.id.my_custom_title_view);
            try {viewModel.doMySearch(query,this);
                Log.d("SearchActivity1", "Searching for: " + query);
            } catch (AuthFailureError e) {e.printStackTrace();}
            //suggestions.saveRecentQuery(query, null);
//            try {doMySearch(query);
//            } catch (AuthFailureError e) {e.printStackTrace();}
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        invalidateOptionsMenu();
    }
    private void checkIfAllEmpty() {
        boolean allEmpty = (jsonArrayMutableLiveData.getValue() == null || jsonArrayMutableLiveData.getValue().size() == 0) &&
                        (cdrsMutableLiveData.getValue() == null || cdrsMutableLiveData.getValue().size() == 0) &&
                        (smsConversationsMutableLiveData.getValue() == null || smsConversationsMutableLiveData.getValue().size() == 0) &&
                        (faxMutableLiveData.getValue() == null || faxMutableLiveData.getValue().size() == 0) &&
                        (voicemailsMutableLiveData.getValue() == null || voicemailsMutableLiveData.getValue().size() == 0);

        allEmptyLiveData.setValue(allEmpty);
    }

    public static void setCurrentItem(int i){binding.viewPager.setCurrentItem(i);}
    public static EditText getTextView() {return textView;}
    public static ActivitySearchBinding getBinding() {return binding;}
}