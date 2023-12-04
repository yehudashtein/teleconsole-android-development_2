package com.telebroad.teleconsole.controller.dashboard;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.ContactRecyclerAdapter;
import com.telebroad.teleconsole.controller.ViewContactActivity;
import com.telebroad.teleconsole.viewmodels.ContactViewModel;


public class ContactFragment extends Fragment implements ScrollableFragment {
    private ContactViewModel contacts;
    private boolean isFiltering = false;
    private FloatingActionButton addContactFab;
    private ContactRecyclerAdapter.OnContactSelected contactSelected;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private String identifier = "";
    private ContactRecyclerAdapter adapter;

    public ContactFragment() {
    }

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    public static ContactFragment newInstance(ContactRecyclerAdapter.OnContactSelected contactSelected, String identifier) {
        ContactFragment fragment = new ContactFragment();
        fragment.contactSelected = contactSelected;
        Bundle args = new Bundle();
        args.putString("identifier", identifier);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            identifier = getArguments().getString("identifier");
            // Now use this identifier as needed
        }
        //contacts = ViewModelProviders.of(this).get(ContactViewModel.class);
        contacts = new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()).create(ContactViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        //android.util.Log.d("Contacts Frag", "Creating view");
        Context context = view.getContext();
        if (getArguments() != null) {
            identifier = getArguments().getString("identifier");
            // Now use this identifier as needed
        }
        addContactFab = view.findViewById(R.id.add_contact_fab);
        //addContactFab.setImageResource(R.drawable.ic_baseline_add_24);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            Drawable myFabSrc = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_add_24, null);
            Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
            willBeWhite.mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            addContactFab.setImageDrawable(willBeWhite);
        } else {
            Drawable myFabSrc = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_add_24, null);
            Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
            willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            addContactFab.setImageDrawable(willBeWhite);
        }
        addContactFab.setOnClickListener(v -> {
            new ContactSaveLocationDialog().show(requireActivity().getSupportFragmentManager(), "contacts");
        });
        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        // android.util.Log.d("Contacts Frag", "Creating adapter");
        adapter = new ContactRecyclerAdapter();
        adapter.setOnBottomReached(atBottom -> {
            if (atBottom) {
                addContactFab.setAlpha(0.2f);
            } else {
                addContactFab.setAlpha(1.0f);
            }
        });
        adapter.setOnContactSelected(contact -> {
//            if (addContactFab.getVisibility() == View.GONE) {
//                return;
//            }
            if (contactSelected != null) {
                contactSelected.selected(contact);
            } else {
                Intent viewContactIntent = new Intent(getActivity(), ViewContactActivity.class);
                Bundle transistionBundle = ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
                viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_ID, contact.getID());
                viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_TYPE, contact.getType());
                requireActivity().startActivity(viewContactIntent, transistionBundle);
            }
        });
        contacts.getActiveContacts(this).observe(getViewLifecycleOwner(), adapter::setContacts);
        updateListType(ContactViewModel.ContactListType.ALL);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void addSystemContact() {
        Intent contectIntent = new Intent(Intent.ACTION_INSERT);
        contectIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (getActivity() != null && contectIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(contectIntent);
        }
    }

    public void setAddContactFabVisibile(boolean visibile) {
        if (visibile) {
            addContactFab.setVisibility(View.VISIBLE);
        } else {
            addContactFab.setVisibility(View.GONE);
        }
    }

    public void setContactSelected(ContactRecyclerAdapter.OnContactSelected contactSelected) {
        this.contactSelected = contactSelected;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contact, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        // Create a SearchView and set it as the action view
         searchView = new SearchView(((AppCompatActivity) getActivity()).getSupportActionBar().getThemedContext());
        searchItem.setActionView(searchView);
        if (identifier.equals("SecondCallActivity")) {
            //MenuItem searchItem = menu.findItem(R.id.action_search);
            //searchView = (SearchView) searchItem.getActionView();
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    searchView.setIconified(false);
                    ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    //bar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), android.R.color.white)));
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    stopFiltering();
                    return true;
                }
            });
            if (searchView != null) {
                searchView.setOnCloseListener(() -> {
                    searchItem.collapseActionView();
                    stopFiltering();
                    return false;
                });
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText == null || newText.isEmpty()) {
                            stopFiltering();
                        } else {
                            startFiltering();
                            contacts.searchContact(newText);
                        }
                        return false;
                    }
                });
                searchView.setQueryHint(getActivity().getResources().getString(R.string.search_contacts));
            }
        }
    }

    private void startFiltering() {
        contacts.getActiveContacts(this).removeObservers(this);
        contacts.filteredContacts.observe(this, adapter::setContacts);
        isFiltering = true;
    }

    private void stopFiltering() {
        contacts.filteredContacts.removeObservers(this);
        contacts.getActiveContacts(this).observe(this, adapter::setContacts);
        isFiltering = false;
    }
    private void setupSearchView(MenuItem searchItem) {
        searchView = (SearchView) searchItem.getActionView();
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setIconified(false);
                ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                //bar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), android.R.color.white)));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                stopFiltering();
                return true;
            }
        });

        if (searchView != null) {
            searchView.setOnCloseListener(() -> {
                searchItem.collapseActionView();
                stopFiltering();
                return false;
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText == null || newText.isEmpty()) {
                        stopFiltering();
                    } else {
                        startFiltering();
                        contacts.searchContact(newText);
                    }
                    return false;
                }
            });
            searchView.setQueryHint(getActivity().getResources().getString(R.string.search_contacts));
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if (!identifier.equals("SecondCallActivity")) {
                    Intent intent = new Intent(requireActivity(), SearchActivity.class);
                    intent.putExtra("itemCurrent", 1);
                    startActivity(intent);
                }else {
                   // setupSearchView(item);
                }
               return true;
            case R.id.action_all:
                updateListType(ContactViewModel.ContactListType.ALL);
                return true;
            case R.id.action_personal:
                updateListType(ContactViewModel.ContactListType.PERSONAL);
                return true;
            case R.id.action_company:
                updateListType(ContactViewModel.ContactListType.COMPANY);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateListType(ContactViewModel.ContactListType type) {
        if (isFiltering) {
            contacts.setCurrentType(type);
            contacts.searchContact(searchView.getQuery().toString());
        } else {
            contacts.setActiveContacts(type, this);
        }
    }

    @Override
    public RecyclerView recyclerView() {
        return recyclerView;
    }
}

